package com.tencent.wxpay.business;

import org.slf4j.LoggerFactory;

import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXLog;
import com.tencent.wxpay.common.WXPayConfig;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.common.report.ReporterFactory;
import com.tencent.wxpay.common.report.protocol.ReportReqData;
import com.tencent.wxpay.common.report.service.ReportService;
import com.tencent.wxpay.protocol.refund_protocol.RefundReqData;
import com.tencent.wxpay.protocol.refund_protocol.RefundResData;
import com.tencent.wxpay.service.RefundService;

/**
 * User: rizenguo Date: 2014/12/2 Time: 17:51
 */
public class RefundBusiness {

	public RefundBusiness() throws IllegalAccessException,
			ClassNotFoundException, InstantiationException {
		refundService = new RefundService();
	}

	public interface ResultListener {
		// API返回ReturnCode不合法，支付请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问
		void onFailByReturnCodeError(RefundResData refundResData);

		// API返回ReturnCode为FAIL，支付API系统返回失败，请检测Post给API的数据是否规范合法
		void onFailByReturnCodeFail(RefundResData refundResData);

		// 支付请求API返回的数据签名验证失败，有可能数据被篡改了
		void onFailBySignInvalid(RefundResData refundResData);

		// 退款失败
		void onRefundFail(RefundResData refundResData);

		// 退款成功
		void onRefundSuccess(RefundResData refundResData);

	}

	// 打log用
	private static WXLog wXLog = new WXLog(
			LoggerFactory.getLogger(RefundBusiness.class));

	// 执行结果
	private static String result = "";

	private RefundService refundService;

	/**
	 * 调用退款业务逻辑
	 * 
	 * @param refundReqData
	 *            这个数据对象里面包含了API要求提交的各种数据字段
	 * @param resultListener
	 *            业务逻辑可能走到的结果分支，需要商户处理
	 * @throws Exception
	 */
	public void run(RefundReqData refundReqData, ResultListener resultListener,
			String appKey) throws Exception {

		// --------------------------------------------------------------------
		// 构造请求“退款API”所需要提交的数据
		// --------------------------------------------------------------------

		// API返回的数据
		String refundServiceResponseString;

		long costTimeStart = System.currentTimeMillis();

		wXLog.i("退款查询API返回的数据如下：");
		refundServiceResponseString = refundService.request(refundReqData);

		long costTimeEnd = System.currentTimeMillis();
		long totalTimeCost = costTimeEnd - costTimeStart;
		wXLog.i("api请求总耗时：" + totalTimeCost + "ms");

		wXLog.i(refundServiceResponseString);

		// 将从API返回的XML数据映射到Java对象
		RefundResData refundResData = (RefundResData) WXUtil.getObjectFromXML(
				refundServiceResponseString, RefundResData.class);

		ReportReqData reportReqData = new ReportReqData(
				refundReqData.getDevice_info(), WXPayConfig.DOWNLOAD_BILL_API,
				(int) (totalTimeCost), refundResData.getReturn_code(),
				refundResData.getReturn_msg(), refundResData.getResult_code(),
				refundResData.getErr_code(), refundResData.getErr_code_des(),
				refundResData.getOut_trade_no(), WXPayConfig.ip,
				refundReqData.getAppid(), refundReqData.getMch_id(),  appKey);

		long timeAfterReport;
		if (WXPayConfig.useThreadToDoReport) {
			ReporterFactory.getReporter(reportReqData).run();
			timeAfterReport = System.currentTimeMillis();
			WXUtil.log("pay+report总耗时（异步方式上报）："
					+ (timeAfterReport - costTimeStart) + "ms");
		} else {
			ReportService.request(reportReqData);
			timeAfterReport = System.currentTimeMillis();
			WXUtil.log("pay+report总耗时（同步方式上报）："
					+ (timeAfterReport - costTimeStart) + "ms");
		}

		if (refundResData == null || refundResData.getReturn_code() == null) {
			setResult("Case1:退款API请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问",
					WXLog.LOG_TYPE_ERROR);
			resultListener.onFailByReturnCodeError(refundResData);
			return;
		}

		// Debug:查看数据是否正常被填充到scanPayResponseData这个对象中
		// Util.reflect(refundResData);

		if (refundResData.getReturn_code().equals("FAIL")) {
			// /注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
			setResult("Case2:退款API系统返回失败，请检测Post给API的数据是否规范合法",
					WXLog.LOG_TYPE_ERROR);
			resultListener.onFailByReturnCodeFail(refundResData);
		} else {
			wXLog.i("退款API系统成功返回数据");
			// --------------------------------------------------------------------
			// 收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
			// --------------------------------------------------------------------

			if (!Signature.checkIsSignValidFromResponseString(
					refundServiceResponseString, appKey)) {
				setResult("Case3:退款请求API返回的数据签名验证失败，有可能数据被篡改了",
						WXLog.LOG_TYPE_ERROR);
				resultListener.onFailBySignInvalid(refundResData);
				return;
			}

			if (refundResData.getResult_code().equals("FAIL")) {
				wXLog.i("出错，错误码：" + refundResData.getErr_code() + "     错误信息："
						+ refundResData.getErr_code_des());
				setResult("Case4:【退款失败】", WXLog.LOG_TYPE_ERROR);
				// 退款失败时再怎么延时查询退款状态都没有意义，这个时间建议要么再手动重试一次，依然失败的话请走投诉渠道进行投诉
				resultListener.onRefundFail(refundResData);
			} else {
				// 退款成功
				setResult("Case5:【退款成功】", WXLog.LOG_TYPE_INFO);
				resultListener.onRefundSuccess(refundResData);
			}
		}
	}

	public void setRefundService(RefundService service) {
		refundService = service;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		RefundBusiness.result = result;
	}

	public void setResult(String result, String type) {
		setResult(result);
		wXLog.log(type, result);
	}
}
