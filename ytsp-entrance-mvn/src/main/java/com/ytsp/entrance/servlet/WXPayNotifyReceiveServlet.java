package com.ytsp.entrance.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXPayConfig;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.protocol.notify_protocol.NotifyRspData;
import com.tencent.wxpay.protocol.prepayid_protocol.PrepayidResData;
import com.tencent.wxpay.service.PayNotifyHandler;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.enums.EbOrderSourceEnum;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.v4_0.MemberServiceV4_0;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.alipay.AlipayNotify;

public class WXPayNotifyReceiveServlet extends HttpServlet {

	private static final Logger logger = Logger
			.getLogger(WXPayNotifyReceiveServlet.class);
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean success = false;
		String dataStr = WXUtil.inputStreamToString(request.getInputStream());
		NotifyRspData rspData = (NotifyRspData) WXUtil.getObjectFromXML(
				dataStr, NotifyRspData.class);
		if (rspData == null || rspData.getReturn_code() == null) {
			// TODO 失败
		} else if (rspData.getReturn_code().equals("FAIL")) {
			// TODO 失败
		} else {
			String appKey = WXPayConfig.ANDROID_API_KEY;
			if (rspData.getAppid() == WXPayConfig.ANDROID_APP_ID) {
				appKey = WXPayConfig.ANDROID_API_KEY;
			} else if (rspData.getAppid() == WXPayConfig.IPHONE_APP_ID) {
				appKey = WXPayConfig.IPHONE_API_KEY;
			} else if (rspData.getAppid() == WXPayConfig.HD_APP_ID) {
				appKey = WXPayConfig.HD_API_KEY;
			}
			try {
				if (Signature.checkIsSignValidFromResponseString(dataStr,
						appKey)) {
					if (rspData.getResult_code().equals("SUCCESS")) {
						// TODO 成功
						String out_trade_no = rspData.getOut_trade_no();
						long orderId = Long.parseLong(out_trade_no);
						ApplicationContext ctx = SystemInitialization
								.getApplicationContext();
						EbOrder order = ctx.getBean(EbOrderService.class)
								.retrieveOrder(orderId);
						if (order.getStatus() == EbOrderStatusEnum.ORDERSUCCESS
								|| order.getStatus() == EbOrderStatusEnum.WAIT) {
							// 采用订单号字符串池化对象做锁同步修改订单状态
							String lock = out_trade_no.intern();
							synchronized (lock) {
								ctx.getBean(EbOrderService.class)
										.createOrderPaySuccess(orderId, 3);
							}
							if (order.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
								try {
									ctx.getBean(MemberServiceV4_0.class)
											.saveVipPaySuccess(orderId);
								} catch (BeansException e) {
									e.printStackTrace();
								} catch (SqlException e) {
									e.printStackTrace();
								}
							}
						}
						success = true;
					} else {
						// TODO 失败
					}
				} else {
					// TODO 失败
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				// TODO 失败
			} catch (SAXException e) {
				e.printStackTrace();
				// TODO 失败
			}
		}
		PrintWriter out = response.getWriter();
		response.addHeader("Content-Type", "text/xml");
		// 解决XStream对出现双下划线的bug
		XStream xStreamForRequestPostData = new XStream(new DomDriver("UTF-8",
				new XmlFriendlyNameCoder("-_", "_")));
		String postDataXML = "";
		if (success) {
			// 将要提交给API的数据对象转换成XML格式数据Post给API
			postDataXML = xStreamForRequestPostData.toXML(new NotifyReturnData(
					"SUCCESS"));
		} else {
			postDataXML = xStreamForRequestPostData.toXML(new NotifyReturnData(
					"FAIL"));
		}
		out.write(postDataXML);
		out.flush();
		out.close();

		// String return_code = request.getParameter("return_code");
		// if (StringUtils.isNotEmpty(return_code)
		// && return_code.equals("SUCCESS")) {
		// String out_trade_no = request.getParameter("out_trade_no");
		// if (StringUtils.isNotEmpty(out_trade_no)) {
		// long orderId = Long.parseLong(out_trade_no);
		// ApplicationContext ctx = SystemInitialization
		// .getApplicationContext();
		// EbOrder order = ctx.getBean(EbOrderService.class)
		// .retrieveOrder(orderId);
		// if (order != null) {
		// PayNotifyHandler handler = new PayNotifyHandler(request,
		// response);
		// if (order.getOrderSource() == EbOrderSourceEnum.ANDROID) {
		// handler.setAppKey(WXPayConfig.ANDROID_API_KEY);
		// } else if (order.getOrderSource() == EbOrderSourceEnum.IPHONE) {
		// handler.setAppKey(WXPayConfig.IPHONE_API_KEY);
		// } else {
		// handler.setAppKey(WXPayConfig.HD_API_KEY);
		// }
		// //
		// if (handler.isTenpaySign()) {
		// int total_fee = Integer.valueOf(request.getParameter(
		// "total_fee").toString());
		// double totalFee = total_fee / 100.0;
		// if (order.getTotalPrice() == totalFee) {
		//
		// }
		// }
		// }
		// }
		// }
	}

	class NotifyReturnData {
		private String return_code;
		private String return_msg;

		public String getReturn_code() {
			return return_code;
		}

		public void setReturn_code(String return_code) {
			this.return_code = return_code;
		}

		public String getReturn_msg() {
			return return_msg;
		}

		public void setReturn_msg(String return_msg) {
			this.return_msg = return_msg;
		}

		public NotifyReturnData(String return_code) {
			super();
			this.return_code = return_code;
		}

	}
}
