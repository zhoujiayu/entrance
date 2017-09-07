package com.tencent.wxpay.business;

import org.slf4j.LoggerFactory;

import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXLog;
import com.tencent.wxpay.common.WXPayConfig;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.common.report.ReporterFactory;
import com.tencent.wxpay.common.report.protocol.ReportReqData;
import com.tencent.wxpay.common.report.service.ReportService;
import com.tencent.wxpay.protocol.prepayid_protocol.PrepayidReqData;
import com.tencent.wxpay.protocol.prepayid_protocol.PrepayidResData;
import com.tencent.wxpay.service.PrepayidService;

/**
 */
public class PrepayidBusiness {

	public PrepayidBusiness() {
		prepayidService = new PrepayidService();
	}

	public interface ResultListener {

		// API返回ReturnCode不合法，支付请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问
		void onFailByReturnCodeError(PrepayidResData prepayidResData);

		// API返回ReturnCode为FAIL，支付API系统返回失败，请检测Post给API的数据是否规范合法
		void onFailByReturnCodeFail(PrepayidResData prepayidResData);

		// API返回的数据签名验证失败，有可能数据被篡改了
		void onFailBySignInvalid(PrepayidResData prepayidResData);

		// 失败
		void onFail(PrepayidResData prepayidResData);

		// 成功
		void onSuccess(PrepayidResData prepayidResData);

	}

	// 打log用
	private static WXLog wXLog = new WXLog(
			LoggerFactory.getLogger(PrepayidBusiness.class));

	private PrepayidService prepayidService;

	/**
	 * 直接执行业务逻辑（包含最佳实践流程）
	 *
	 * @param prepayidReqData
	 *            这个数据对象里面包含了API要求提交的各种数据字段
	 * @param resultListener
	 *            商户需要自己监听业务逻辑可能触发的各种分支事件，并做好合理的响应处理
	 * @throws Exception
	 */
	public String run(PrepayidReqData prepayidReqData,
			ResultListener resultListener, String appKey) throws Exception {
		// 接受API返回
		String payServiceResponseString;
		long costTimeStart = System.currentTimeMillis();
		wXLog.i("统一下单API返回的数据如下：");
		payServiceResponseString = prepayidService.request(prepayidReqData);
		long costTimeEnd = System.currentTimeMillis();
		long totalTimeCost = costTimeEnd - costTimeStart;
		wXLog.i("api请求总耗时：" + totalTimeCost + "ms");
		// 打印回包数据
		wXLog.i(payServiceResponseString);
		// 将从API返回的XML数据映射到Java对象
		PrepayidResData paypreidResData = (PrepayidResData) WXUtil
				.getObjectFromXML(payServiceResponseString,
						PrepayidResData.class);
		// 异步发送统计请求
		ReportReqData reportReqData = new ReportReqData(
				prepayidReqData.getDevice_info(),
				WXPayConfig.DOWNLOAD_BILL_API, (int) (totalTimeCost),
				paypreidResData.getReturn_code(),
				paypreidResData.getReturn_msg(),
				paypreidResData.getResult_code(),
				paypreidResData.getErr_code(),
				paypreidResData.getErr_code_des(), "", WXPayConfig.ip,
				prepayidReqData.getAppid(), prepayidReqData.getMch_id(), appKey);
		long timeAfterReport;
		if (WXPayConfig.useThreadToDoReport) {
			ReporterFactory.getReporter(reportReqData).run();
			timeAfterReport = System.currentTimeMillis();
			wXLog.i("pay+report总耗时（异步方式上报）："
					+ (timeAfterReport - costTimeStart) + "ms");
		} else {
			ReportService.request(reportReqData);
			timeAfterReport = System.currentTimeMillis();
			wXLog.i("pay+report总耗时（同步方式上报）："
					+ (timeAfterReport - costTimeStart) + "ms");
		}

		if (paypreidResData == null || paypreidResData.getReturn_code() == null) {
			wXLog.e("【预支付信息】统一下单请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问");
			if (resultListener != null)
				resultListener.onFailByReturnCodeError(paypreidResData);
			return "";
		}

		if (paypreidResData.getReturn_code().equals("FAIL")) {
			// 注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
			wXLog.e("【预支付信息】统一下单API系统返回失败，请检测Post给API的数据是否规范合法");
			if (resultListener != null)
				resultListener.onFailByReturnCodeFail(paypreidResData);
			return "";
		} else {
			wXLog.i("统一下单API系统成功返回数据");
			// --------------------------------------------------------------------
			// 收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
			// --------------------------------------------------------------------
			if (!Signature.checkIsSignValidFromResponseString(
					payServiceResponseString, appKey)) {
				wXLog.e("【预支付信息】支付请求API返回的数据签名验证失败，有可能数据被篡改了");
				if (resultListener != null)
					resultListener.onFailBySignInvalid(paypreidResData);
				return "";
			}

			// 获取错误码
			String errorCode = paypreidResData.getErr_code();
			// 获取错误描述
			String errorCodeDes = paypreidResData.getErr_code_des();
			if (paypreidResData.getResult_code().equals("SUCCESS")) {
				wXLog.i("【获取预支付信息成功】");
				if (resultListener != null)
					resultListener.onSuccess(paypreidResData);
				return paypreidResData.getPrepay_id();
			} else {
				// 出现业务错误
				wXLog.i("业务返回失败");
				wXLog.i("err_code:" + errorCode);
				wXLog.i("err_code_des:" + errorCodeDes);
				if (resultListener != null)
					resultListener.onFail(paypreidResData);
				return "";
			}
		}
	}

}
