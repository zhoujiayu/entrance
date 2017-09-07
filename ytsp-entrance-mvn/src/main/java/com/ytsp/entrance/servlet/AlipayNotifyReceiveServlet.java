package com.ytsp.entrance.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.entrance.recharge.alipay.Constants;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.v4_0.MemberServiceV4_0;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.alipay.AlipayNotify;

public class AlipayNotifyReceiveServlet extends HttpServlet {

	private static final Logger logger = Logger
			.getLogger(AlipayNotifyReceiveServlet.class);
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
			sb.append(name + "=" + valueStr);
			if (iter.hasNext())
				sb.append(",");
		}
		sb.append("}");

		// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		// 商户订单号

		String out_trade_no = new String(request.getParameter("out_trade_no")
				.getBytes("ISO-8859-1"), "UTF-8");

		// 支付宝交易号

		String trade_no = new String(request.getParameter("trade_no").getBytes(
				"ISO-8859-1"), "UTF-8");

		// 交易状态
		String trade_status = new String(request.getParameter("trade_status")
				.getBytes("ISO-8859-1"), "UTF-8");

		// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

		if (AlipayNotify.verify(params)) {// 验证成功
			// ////////////////////////////////////////////////////////////////////////////////////////
			// 请在这里加上商户的业务逻辑程序代码

			// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
			logger.info(sb.toString());
			long orderId = Long.parseLong(out_trade_no);
			if (trade_status.equals("TRADE_FINISHED")
					|| trade_status.equals("TRADE_SUCCESS")) {
				// 判断该笔订单是否在商户网站中已经做过处理
				// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
				// 如果有做过处理，不执行商户的业务程序
				ApplicationContext ctx = SystemInitialization
						.getApplicationContext();
				try {
					JSONObject order = ctx.getBean(EbOrderService.class)
							.getOrderById(orderId);

					int status = order.getInt("status");
					if (status == EbOrderStatusEnum.ORDERSUCCESS.getValue()
							|| status == EbOrderStatusEnum.WAIT.getValue()) {
						// 采用订单号字符串池化对象做锁同步修改订单状态
						ctx.getBean(EbOrderService.class)
								.createOrderPaySuccess(orderId, 1);
						if (order.getInt("orderType") == EbOrderTypeEnum.VIPMEMBER
								.getValue())
							ctx.getBean(MemberServiceV4_0.class)
									.saveVipPaySuccess(orderId);
					}
				} catch (Exception e) {
					logger.error("ERROR dealing with alipay callback, orderId=="
							+ orderId);
				}
				// 注意：
				// 该种交易状态只在两种情况下出现
				// 1、开通了普通即时到账，买家付款成功后。
				// 2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
			}
			// { TRADE_SUCCESS
			// 判断该笔订单是否在商户网站中已经做过处理
			// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
			// 如果有做过处理，不执行商户的业务程序
			// ApplicationContext ctx =
			// SystemInitialization.getApplicationContext();
			// ctx.getBean(EbOrderService.class).createOrderPaySuccess(orderId);
			// 注意：
			// 该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。
			// }

			// ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——

			response.getWriter().println("success"); // 请不要修改或删除

			// ////////////////////////////////////////////////////////////////////////////////////////
		} else {// 验证失败
			response.getWriter().println("fail");
		}
	}

	private String generateVerifyData(String service, String v, String secId,
			String notifyData) {
		StringBuilder sb = new StringBuilder();
		sb.append(Constants.KEY_SERVICE).append("=").append(service);
		sb.append("&").append(Constants.KEY_VERSION).append("=").append(v);
		sb.append("&").append(Constants.KEY_SEC_ID).append("=").append(secId);
		sb.append("&").append(Constants.KEY_NOTIFY_DATA).append("=")
				.append(notifyData);
		return sb.toString();
	}

}
