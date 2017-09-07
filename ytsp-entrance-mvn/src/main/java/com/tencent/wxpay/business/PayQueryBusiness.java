package com.tencent.wxpay.business;

import static java.lang.Thread.sleep;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXLog;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.protocol.pay_query_protocol.PayQueryReqData;
import com.tencent.wxpay.protocol.pay_query_protocol.PayQueryResData;
import com.tencent.wxpay.service.PayQueryService;

public class PayQueryBusiness {

	public PayQueryBusiness() {
		payQueryService = new PayQueryService();
	}

	private PayQueryService payQueryService;
	// 打log用
	private static WXLog wXLog = new WXLog(
			LoggerFactory.getLogger(PrepayidBusiness.class));
	// 每次调用订单查询API时的等待时间，因为当出现支付失败的时候，如果马上发起查询不一定就能查到结果，所以这里建议先等待一定时间再发起查询
	private int waitingTimeBeforePayQueryServiceInvoked = 5000;

	/**
	 * 进行一次支付订单查询操作
	 *
	 * @return 该订单是否支付成功
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	public boolean doOnePayQuery(PayQueryReqData payQueryReqData, String appKey)
			throws ParserConfigurationException, IOException, SAXException {
		String payQueryServiceResponseString;
		payQueryServiceResponseString = payQueryService
				.request(payQueryReqData);

		wXLog.i("支付订单查询API返回的数据如下：");
		wXLog.i(payQueryServiceResponseString);
		
		//请求查询时未返回支付信息
		if (payQueryServiceResponseString == null
				|| payQueryServiceResponseString.trim().length() == 0) {
			return false;
		}
		
		if (!Signature.checkIsSignValidFromResponseString(
				payQueryServiceResponseString, appKey)) {
			wXLog.i("支付订单查询请求签名错误");
			return false;
		}
		// 将从API返回的XML数据映射到Java对象
		PayQueryResData payQueryResData = (PayQueryResData) WXUtil
				.getObjectFromXML(payQueryServiceResponseString,
						PayQueryResData.class);
		if (payQueryResData == null || payQueryResData.getReturn_code() == null) {
			wXLog.i("支付订单查询请求逻辑错误，请仔细检测传过去的每一个参数是否合法");
			return false;
		}

		if (payQueryResData.getReturn_code().equals("FAIL")) {
			// 注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
			wXLog.i("支付订单查询API系统返回失败，失败信息为：" + payQueryResData.getReturn_msg());
			return false;
		} else {
			if (payQueryResData.getResult_code().equals("SUCCESS")) {// 业务层成功
				if (payQueryResData.getTrade_state().equals("SUCCESS")) {
					// 表示查单结果为“支付成功”
					wXLog.i("查询到订单支付成功");
					return true;
				} else {
					// 支付不成功
					wXLog.i("查询到订单支付不成功");
					return false;
				}
			} else {
				wXLog.i("查询出错，错误码：" + payQueryResData.getErr_code()
						+ "     错误信息：" + payQueryResData.getErr_code_des());
				return false;
			}
		}

	}

	/**
	 * 由于有的时候是因为服务延时，所以需要商户每隔一段时间（建议5秒）后再进行查询操作，多试几次（建议3次）
	 *
	 * @param loopCount
	 *            循环次数，至少一次
	 * @param outTradeNo
	 *            商户系统内部的订单号,32个字符内可包含字母, [确保在商户系统唯一]
	 * @return 该订单是否支付成功
	 * @throws InterruptedException
	 */
	public boolean doPayQueryLoop(int loopCount,
			PayQueryReqData payQueryReqData, String appKey) throws Exception {
		// 至少查询一次
		if (loopCount == 0) {
			loopCount = 1;
		}
		// 进行循环查询
		for (int i = 0; i < loopCount; i++) {
			sleep(waitingTimeBeforePayQueryServiceInvoked);// 等待一定时间再进行查询，避免状态还没来得及被更新
			if (doOnePayQuery(payQueryReqData, appKey)) {
				return true;
			}
		}
		return false;
	}
}
