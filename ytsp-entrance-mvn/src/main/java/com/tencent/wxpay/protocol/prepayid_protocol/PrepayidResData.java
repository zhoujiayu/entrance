package com.tencent.wxpay.protocol.prepayid_protocol;

public class PrepayidResData {
	/**
	 * 返回状态码 String(16) <br/>
	 * SUCCESS/FAIL
	 */
	private String return_code;
	/**
	 * 返回信息 String(128)<br/>
	 * 返回信息，如非空，为错误原因
	 */
	private String return_msg;

	/** ====以下字段在return_code为SUCCESS的时候有返回==== **/
	/** 公众账号ID String(32) */
	private String appid;
	/** 商户号 String(32) */
	private String mch_id;
	/** 设备号 String(32) */
	public String device_info;
	/** 随机字符串 String(32) */
	private String nonce_str;
	/** 签名 String(32) */
	private String sign;
	/** 业务结果 String(16) SUCCESS/FAIL */
	private String result_code;
	/** 错误代码 String(32) */
	private String err_code;
	/** 错误代码描述 String(128) */
	private String err_code_des;

	/** ====以下字段在return_code 和result_code都为SUCCESS的时候有返回==== **/
	/** 交易类型 String(16) 调用接口提交的交易类型，取值如下：JSAPI，NATIVE，APP */
	private String trade_type;
	/** 预支付交易会话标识 String(64) 微信生成的预支付回话标识，用于后续接口调用中使用，该值有效期为2小时 */
	private String prepay_id;
	/** 二维码链接 String(64) trade_type为NATIVE是有返回，可将该参数值生成二维码展示出来进行扫码支付 */
	private String code_url;

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

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getMch_id() {
		return mch_id;
	}

	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}

	public String getDevice_info() {
		return device_info;
	}

	public void setDevice_info(String device_info) {
		this.device_info = device_info;
	}

	public String getNonce_str() {
		return nonce_str;
	}

	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getResult_code() {
		return result_code;
	}

	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}

	public String getErr_code() {
		return err_code;
	}

	public void setErr_code(String err_code) {
		this.err_code = err_code;
	}

	public String getErr_code_des() {
		return err_code_des;
	}

	public void setErr_code_des(String err_code_des) {
		this.err_code_des = err_code_des;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}

	public String getPrepay_id() {
		return prepay_id;
	}

	public void setPrepay_id(String prepay_id) {
		this.prepay_id = prepay_id;
	}

	public String getCode_url() {
		return code_url;
	}

	public void setCode_url(String code_url) {
		this.code_url = code_url;
	}

}
