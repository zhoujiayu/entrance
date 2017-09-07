package com.ytsp.entrance.recharge.alipay;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.MemberShipFeeDao;
import com.ytsp.db.domain.MemberShipFee;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.recharge.alipay.model.PayChannelResult;
import com.ytsp.entrance.system.SystemInitialization;

public class RechargeServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(RechargeServlet.class);
    private static final long serialVersionUID = 1L;

    public RechargeServlet() {
        super();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final ApplicationContext ac = SystemInitialization.getApplicationContext();
        final AlipayService as = ac.getBean(AlipayService.class);

//        final String name = request.getParameter("uname");
//        final String psw = request.getParameter("psw");
//        if (name == null || psw == null) {
//            throw new RuntimeException("illegal parameters");
//        }
//
//        CustomerService cs = SystemInitialization.getApplicationContext().getBean(CustomerService.class);
//        Customer customer = null;
//        try {
//            String md5Pwd = MD5Util.md5ToHex(psw.trim().getBytes());
//            customer = cs.findCustomerByAccountAndPassword(name, md5Pwd);
//        } catch (NoSuchAlgorithmException e) {
//            log.error("", e);
//        } catch (Exception e) {
//            log.error("", e);
//        }
//
//        if (customer == null) {
//            RequestDispatcher dispatcher = request.getRequestDispatcher("/rc/log-in-er.html");
//            dispatcher.forward(request, response);
//            return;
//        }
//
//        Integer id = customer.getId();
//        request.setAttribute("cus", customer);
        
        final MemberShipFeeDao msfDao = ac.getBean(MemberShipFeeDao.class);
        try {
            List<MemberShipFee> fees = msfDao.getAll();
            request.setAttribute("fees", fees);
        } catch (SqlException e) {
            throw new ServletException(e);
        }
        assert msfDao != null;
        final String method = request.getParameter("method");
        final String uid = request.getParameter("uid");    
        if (StringUtil.isNullOrEmpty(uid)) {
            throw new ServletException("uid missing");
        }
        try {
            PayChannelResult channels = as.getPayChannel(PayChannelResult.class, String.valueOf(uid));
            request.setAttribute("ch", channels);
            request.setAttribute("method", method);

        } catch (AlipayException e) {
            log.error("get channels from alipay failed", e);
        } catch (Exception e) {
            log.error("get channels from alipay failed", e);
        }

//        RequestDispatcher dispatcher = request.getRequestDispatcher("/rc/mobiless.jsp");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/prepay.jsp");
        dispatcher.forward(request, response);
    }

}
