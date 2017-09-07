package com.ytsp.entrance.recharge;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.RechargeHistoryDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.RechargeHistory;
import com.ytsp.db.enums.ChargingFromEnum;
import com.ytsp.db.enums.ChargingStatusEnum;
import com.ytsp.db.enums.ChargingTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.recharge.alipay.AlipayService;
import com.ytsp.entrance.recharge.alipay.AlipayTradeException;
import com.ytsp.entrance.recharge.alipay.model.ErrorCode;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.MD5;

public class PayServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(PayServlet.class);

    public PayServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
// 支付宝余额-》1.余额支付，储蓄卡-》2.网银支付，信用卡-》4.信用支付，充值-》10.点卡支付
//        System.out.println(request.getParameter("type"));
//        System.out.println(request.getParameter("channel"));
//        System.out.println(request.getParameter("duration"));
//        System.out.println(request.getParameter("cardNum"));
//        System.out.println(request.getParameter("cardPsw"));
//        System.out.println(request.getParameter("from"));
//        System.out.println(request.getParameter("account"));
//        System.out.println(request.getParameter("psw"));
//        System.out.println(request.getParameter("price"));

        final ApplicationContext ctx = SystemInitialization.getApplicationContext();
        final CustomerService customerSvc = ctx.getBean(CustomerService.class);
        final RechargeHistoryDao rdao = ctx.getBean(RechargeHistoryDao.class);

        final String type = request.getParameter("type");
        final String account = request.getParameter("account");
        final String password = request.getParameter("psw");
        if (StringUtil.isNullOrEmpty(account) || StringUtil.isNullOrEmpty(password)) {
            throw new ServletException("insufficient account info for account " + account);
        }
        Customer customer = null;
        try {
            String md5Pwd = MD5.code(password.trim());
            customer = customerSvc.findCustomerByAccountAndPassword(account, md5Pwd);
        } catch (Exception e) {
            log.error("", e);
        }
        if (customer == null) {
            throw new ServletException("账号或密码错误：" + account);
        }

        if ("alipay".equals(type)) {
            final String from = request.getParameter("from");
            if (StringUtil.isNullOrEmpty(from)) {
                throw new ServletException("field 'from' is missing");
            }
            ChargingFromEnum cfe = null;
            try {
                cfe = ChargingFromEnum.valueOf(from);
            } catch (IllegalArgumentException e) {
                throw new ServletException("unknow 'from': " + from, e);
            }
            final String subject = request.getParameter("subject");
            if (StringUtil.isNullOrEmpty(subject)) {
                throw new ServletException("field 'subject' is missing");
            }
            final String price = request.getParameter("price");
            if (StringUtil.isNullOrEmpty(price)) {
                throw new ServletException("field 'price' is missing");
            }
            final String durationStr = request.getParameter("duration");
            int duration = 0;
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                throw new ServletException("illegal duration: " + durationStr);
            }
            if (StringUtil.isNullOrEmpty(durationStr)) {
                throw new ServletException("field 'duration' is missing");
            }
            final String channel = request.getParameter("channel");
            String payMethod = null;
            if (channel == null) {
                payMethod = "1";
            } else if (channel.contains("CREDIT")) {
                payMethod = "4";
            } else if (channel.contains("DEBIT")) {
                payMethod = "2";
            } else {
                throw new ServletException("unknow channel: " + channel);
            }
            final AlipayService as = ctx.getBean(AlipayService.class);
            assert as != null;
            RechargeHistory rh = new RechargeHistory();

            Double p = null;
            try {
                p = Double.valueOf(price);
            } catch (NumberFormatException e) {
                throw new ServletException("invalid price: " + price, e);
            }
            rh.setCustomer(customer);
            rh.setFrom(cfe);
            rh.setTime(new Date());
            rh.setType(ChargingTypeEnum.MONTH);
            rh.setStatus(ChargingStatusEnum.WAIT_BUYER_PAY);
            rh.setDuration(duration);
            rh.setPayMethod(payMethod);
            rh.setRechargeSubject(subject);
            rh.setRechargeAmount(new BigDecimal(p));
            try {
                rdao.save(rh);
            } catch (SqlException e) {
                throw new ServletException(e);
            }

            int tradeId = rh.getId();
            try {
                String url = as.trade(String.valueOf(tradeId), channel, String.valueOf(customer.getId()), subject, price);
                response.sendRedirect(url);

            } catch (AlipayTradeException e) {
//                ErrorCode error = e.getError();
                // TODO
                final ErrorCode er = e.getError();
                if (er == null) {
                    throw new ServletException("充值时发生异常", e);
                } else {
                    throw new ServletException(er.getMsg(), e);
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }

        } else if ("prepaid".equals(type)) {
            final String cardNum = request.getParameter("cardNum");
            final String cardPsw = request.getParameter("cardPsw");
            if (StringUtil.isNullOrEmpty(cardNum) || StringUtil.isNullOrEmpty(cardPsw)) {
                throw new ServletException("insufficient prepaid card info: " + cardNum + ":" + cardPsw);
            }
            
            RechargeService rs = ctx.getBean(RechargeService.class);
            Map<String, Object> m = new HashMap<String, Object>();
            m.put(com.ytsp.entrance.recharge.Constants.PARAMS_PREPAID_CARD_CODE, cardNum);
            m.put(com.ytsp.entrance.recharge.Constants.PARAMS_PREPAID_CARD_PASSWORD, cardPsw);
            try {
                rs.recharge(customer.getId(), RechargeType.PREPAID_CARD, m);
                RequestDispatcher dispatch = request.getRequestDispatcher("recharge-s.jsp?uid=" + customer.getId());
                dispatch.forward(request, response);
            } catch (RechargeException e) {
                // TODO
                throw new ServletException(e.getStatus().getDesc(), e);
            }
        } else {
            throw new ServletException("unknow type: " + type);
        }

    }

}
