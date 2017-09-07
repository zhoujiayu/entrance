package com.tencent.wxpay.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.tencent.wxpay.common.HttpsRequest;
import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXPayConfig;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.protocol.prepayid_protocol.PrepayidReqData;
import com.tencent.wxpay.protocol.prepayid_protocol.PrepayidResData;

/**
 * @description 统一下单接口
 *
 */
public class PrepayidService extends BaseService {

	public PrepayidService() {
		super(WXPayConfig.UNI_FIED_ORDER);
	}

	public String request(PrepayidReqData prepayidReqData) throws Exception {

		// --------------------------------------------------------------------
		// 发送HTTPS的Post请求到API地址
		// --------------------------------------------------------------------
		String responseString = sendPost(prepayidReqData);

		return responseString;
	}

	// /**
	// * @param body
	// * @param attach
	// * 附加信息
	// * @param out_trade_no
	// * 订单号
	// * @param total_fee
	// * 价格总计
	// * @param spbill_create_ip
	// * 客户端ip
	// * @param goods_tag
	// * 商品标记
	// * @return
	// * @throws Exception
	// */
	// public PrepayidResData androidPrepay(String body, String attach,
	// String out_trade_no, int total_fee, String spbill_create_ip,
	// String goods_tag) throws Exception {
	// PrepayidReqData params = new PrepayidReqData(
	// WXPayConfig.ANDROID_APP_ID, WXPayConfig.ANDROID_MCH_ID, body,
	// attach, out_trade_no, total_fee, spbill_create_ip, "", "",
	// goods_tag, WXPayConfig.NOTIFY_URL, WXPayConfig.ANDROI_API_KEY);
	// String ret = request(params);
	// PrepayidResData rsp = (PrepayidResData) WXUtil.getObjectFromXML(ret,
	// PrepayidResData.class);
	// return rsp;
	// }
	//
	// /**
	// * @param body
	// * @param attach
	// * 附加信息
	// * @param out_trade_no
	// * 订单号
	// * @param total_fee
	// * 价格总计
	// * @param spbill_create_ip
	// * 客户端ip
	// * @param goods_tag
	// * 商品标记
	// * @return
	// * @throws Exception
	// */
	// public PrepayidResData iphonePrepay(String body, String attach,
	// String out_trade_no, int total_fee, String spbill_create_ip,
	// String goods_tag) throws Exception {
	// PrepayidReqData params = new PrepayidReqData(WXPayConfig.IPHONE_APP_ID,
	// WXPayConfig.IPHONE_MCH_ID, body, attach, out_trade_no,
	// total_fee, spbill_create_ip, "", "", goods_tag,
	// WXPayConfig.NOTIFY_URL, WXPayConfig.IPHONE_API_KEY);
	// String ret = request(params);
	// PrepayidResData rsp = (PrepayidResData) WXUtil.getObjectFromXML(ret,
	// PrepayidResData.class);
	// return rsp;
	// }
	//
	// /**
	// * @param body
	// * @param attach
	// * 附加信息
	// * @param out_trade_no
	// * 订单号
	// * @param total_fee
	// * 价格总计
	// * @param spbill_create_ip
	// * 客户端ip
	// * @param goods_tag
	// * 商品标记
	// * @return
	// * @throws Exception
	// */
	// public PrepayidResData hdPrepay(String body, String attach,
	// String out_trade_no, int total_fee, String spbill_create_ip,
	// String goods_tag) throws Exception {
	// PrepayidReqData params = new PrepayidReqData(WXPayConfig.HD_APP_ID,
	// WXPayConfig.HD_MCH_ID, body, attach, out_trade_no, total_fee,
	// spbill_create_ip, "", "", goods_tag, WXPayConfig.NOTIFY_URL,
	// WXPayConfig.HD_API_KEY);
	// String ret = request(params);
	// PrepayidResData rsp = (PrepayidResData) WXUtil.getObjectFromXML(ret,
	// PrepayidResData.class);
	// return rsp;
	// }

	public static void main(String[] args) throws IllegalAccessException,
			UnrecoverableKeyException, KeyManagementException,
			KeyStoreException, NoSuchAlgorithmException, IOException,
			ParserConfigurationException, SAXException {
		String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		PrepayidReqData params = new PrepayidReqData(WXPayConfig.HD_APP_ID,
				WXPayConfig.HD_MCH_ID, "100010001", "", "100010001", 1, "", "",
				"", "", "127.0.0.1:8080", WXPayConfig.HD_API_KEY);
		HttpsRequest httpRequest = new HttpsRequest();
		String ret = httpRequest.sendPost(url, params);
		System.out.println(ret);
		// Signature.checkIsSignValidFromResponseString(ret,
		// WXPayConfig.HD_API_KEY);
		System.out.println(Signature.checkIsSignValidFromResponseString(ret,
				WXPayConfig.HD_API_KEY));
		PrepayidResData rsp = (PrepayidResData) WXUtil.getObjectFromXML(ret,
				PrepayidResData.class);
	}
}
