package com.ytsp.entrance.recharge.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.MonthlyDao;
import com.ytsp.db.dao.RechargeHistoryDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.Monthly;
import com.ytsp.db.domain.RechargeHistory;
import com.ytsp.db.enums.ChargingStatusEnum;
import com.ytsp.entrance.recharge.alipay.model.Notification;
import com.ytsp.entrance.recharge.alipay.security.MD5Signature;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateUtil;

public class AlipayNotifyReceiveServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AlipayNotifyReceiveServlet.class);
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String sign = request.getParameter(Constants.KEY_SIGN);
        String service = request.getParameter(Constants.KEY_SERVICE);
        String v = request.getParameter(Constants.KEY_VERSION);
        String secId = request.getParameter(Constants.KEY_SEC_ID);
        String notifyData = request.getParameter(Constants.KEY_NOTIFY_DATA);
        String verifyData = generateVerifyData(service, v, secId, notifyData);

        log.info("receive alipay notification: " + verifyData);

        try {
            final PrintWriter out = response.getWriter();
            final Notification notify = Notification.unmarshal(notifyData);
            if (MD5Signature.verify(verifyData, sign, Constants.PARTNER_KEY)) {
                int tradeId = notify.getTradeId();
                ApplicationContext ctx = SystemInitialization.getApplicationContext();
                final RechargeHistoryDao dao = ctx.getBean(RechargeHistoryDao.class);
                final RechargeHistory rh = dao.findById(tradeId);
                if (rh == null) {
                    log.info("can NOT find reacharge history for trade id: " + tradeId);
                } else {
                    // maybe the previous notification has already done without notify back alipay
                    ChargingStatusEnum status = rh.getStatus();
                    if (ChargingStatusEnum.TRADE_SUCCESS.equals(status) || ChargingStatusEnum.TRADE_FINISHED.equals(status)) {
                        out.write(Constants.TRADE_RESPOSNE_SUCCESS);
                        return;
                    }

                    final String tradeStatus = notify.getTradeStatus();
                    if (Constants.TRADE_STATUS_SUCCESS.equals(tradeStatus) || Constants.TRADE_STATUS_SUCCESS2.equals(tradeStatus)) {

                        final MonthlyDao mdao = ctx.getBean(MonthlyDao.class);
                        final CustomerDao cdao = ctx.getBean(CustomerDao.class);
                        int cid = rh.getCustomer().getId();
                        Monthly m = mdao.findByUser(cid);

                        int months = rh.getDuration();
//                        int days = rh.getDuration();
                        if (m == null) {
                            Customer c = cdao.findById(cid);
                            Monthly nm = new Monthly();
                            Date now = new Date();
//                            Date expire = DateUtil.addByDays(now, days);
                            Date expire = DateUtil.addByMonths(now, months);
                            nm.setBeginTime(now);
                            nm.setCustomer(c);
                            nm.setExpireTime(expire);
                            mdao.save(nm);
                        } else {
//                            Date expire = DateUtil.addByDays(m.getExpireTime(), days);
                            Date expire = DateUtil.addByMonths(m.getExpireTime(), months);
                            m.setExpireTime(expire);
                            mdao.update(m);
                        }

                        if (Constants.TRADE_STATUS_SUCCESS.equals(tradeStatus)) {
                        	rh.setStatus(ChargingStatusEnum.TRADE_SUCCESS);
                        } else if (Constants.TRADE_STATUS_SUCCESS2.equals(tradeStatus)) {
                        	rh.setStatus(ChargingStatusEnum.TRADE_FINISHED);
                        }
                        dao.update(rh);
                        out.write(Constants.TRADE_RESPOSNE_SUCCESS);
                    }
                }

            } else {
                log.info("接收支付宝系统通知验证签名失败 tradeId: " + notify.getTradeId());
                out.write(Constants.TRADE_RESPOSNE_FAIL);
            }
        } catch (Exception e) {
            log.error("error occur during verifing alipay notification data", e);
        }

    }

    private String generateVerifyData(String service, String v, String secId, String notifyData) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.KEY_SERVICE).append("=").append(service);
        sb.append("&").append(Constants.KEY_VERSION).append("=").append(v);
        sb.append("&").append(Constants.KEY_SEC_ID).append("=").append(secId);
        sb.append("&").append(Constants.KEY_NOTIFY_DATA).append("=").append(notifyData);
        return sb.toString();
    }

}
