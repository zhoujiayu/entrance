package com.tencent.wxpay.business;

import org.slf4j.LoggerFactory;

import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXLog;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.protocol.reverse_protocol.ReverseReqData;
import com.tencent.wxpay.protocol.reverse_protocol.ReverseResData;
import com.tencent.wxpay.service.ReverseService;

public class ReverseBusiness {
	public ReverseBusiness() throws IllegalAccessException,
			ClassNotFoundException, InstantiationException {
		reverseService = new ReverseService();
	}

	private ReverseService reverseService;
	// 打log用
	private static WXLog wXLog = new WXLog(
			LoggerFactory.getLogger(PrepayidBusiness.class));
	// 每次调用订单查询API时的等待时间，因为当出现支付失败的时候，如果马上发起查询不一定就能查到结果，所以这里建议先等待一定时间再发起查询

	// 是否需要再调一次撤销，这个值由撤销API回包的recall字段决定
	private boolean needRecallReverse = false;
	// 每次调用撤销API的等待时间
	private int waitingTimeBeforeReverseServiceInvoked = 5000;

	/**
	 * 进行一次撤销操作
	 *
	 * @param outTradeNo
	 *            商户系统内部的订单号,32个字符内可包含字母, [确保在商户系统唯一]
	 * @return 该订单是否支付成功 1、关闭成功 2、交易不存在 3、交易状态不符合 4、调用微信参数问题 5、系统错误
	 * @throws Exception
	 */
	public int doOneReverse(ReverseReqData reverseReqData, String appKey)
			throws Exception {
//		sleep(waitingTimeBeforeReverseServiceInvoked);// 等待一定时间再进行查询，避免状态还没来得及被更新
		String reverseResponseString;
		reverseResponseString = reverseService.request(reverseReqData);

		wXLog.i("撤销API返回的数据如下：");
		wXLog.i(reverseResponseString);
		if (!Signature.checkIsSignValidFromResponseString(
				reverseResponseString, appKey)) {
			wXLog.i("支付订单查询请求签名错误");
			return 4;
		}
		// 将从API返回的XML数据映射到Java对象
		ReverseResData reverseResData = (ReverseResData) WXUtil
				.getObjectFromXML(reverseResponseString, ReverseResData.class);
		if (reverseResData == null) {
			wXLog.i("支付订单撤销请求逻辑错误，请仔细检测传过去的每一个参数是否合法");
			return 4;
		}
		if (reverseResData.getReturn_code().equals("FAIL")) {
			// 注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
			wXLog.i("关闭订单:"+reverseReqData.getOut_trade_no()+"支付订单撤销API系统返回失败，失败信息为：" + reverseResData.getReturn_msg());
			return 5;
		} else {
			if (reverseResData.getResult_code().equals("FAIL")) {
				wXLog.i("撤销出错，错误码：" + reverseResData.getErr_code()
						+ "     错误信息：" + reverseResData.getErr_code_des());
				String errCode = reverseResData.getErr_code();
				//订单已支付，不能发起关单
				if("ORDERPAID".equals(errCode)){
					return 3;
				}else if("SYSTEMERROR".equals(errCode)){//系统错误
					return 5;
				}else if("ORDERNOTEXIST".equals(errCode)){//订单不存在
					return 2;
				}else if("ORDERCLOSED".equals(errCode)){//订单已关闭
					return 1;
				}else if("SIGNERROR".equals(errCode)
						||"REQUIRE_POST_METHOD".equals(errCode)
						||"XML_FORMAT_ERROR".equals(errCode)){//参数错误
					return 4;
				}
			} else {
				// 查询成功，打印交易状态
				wXLog.i("支付订单撤销成功");
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 由于有的时候是因为服务延时，所以需要商户每隔一段时间（建议5秒）后再进行查询操作，
	 * 是否需要继续循环调用撤销API由撤销API回包里面的recall字段决定。
	 *
	 * @param outTradeNo
	 *            商户系统内部的订单号,32个字符内可包含字母, [确保在商户系统唯一]
	 * @throws InterruptedException
	 */
	public void doReverseLoop(ReverseReqData reverseReqData, String appKey)
			throws Exception {
		// 初始化这个标记
		needRecallReverse = true;
		// 进行循环撤销，直到撤销成功，或是API返回recall字段为"Y"
		while (needRecallReverse) {
			if (doOneReverse(reverseReqData, appKey) == 1) {
				return;
			}
		}
	}

}
