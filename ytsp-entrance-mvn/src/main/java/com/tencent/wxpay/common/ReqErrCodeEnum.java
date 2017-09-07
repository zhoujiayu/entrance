package com.tencent.wxpay.common;

public enum ReqErrCodeEnum {
	NOAUTH(1, "NOAUTH", "商户无此接口权限", "商户未开通此接口权限"), NOTENOUGH(2, "NOTENOUGH",
			"余额不足", "用户帐号余额不足"), ORDERPAID(3, "ORDERPAID", "商户订单已支付",
			"商户订单已支付，无需重复操作"), ORDERCLOSED(4, "ORDERCLOSED", "订单已关闭",
			"当前订单已关闭，无法支付"), SYSTEMERROR(5, "SYSTEMERROR", "系统错误", "系统超时"), APPID_NOT_EXIST(
			6, "APPID_NOT_EXIST", "APPID不存在", "参数中缺少APPID"), MCHID_NOT_EXIST(7,
			"MCHID_NOT_EXIST", "MCHID不存在", "参数中缺少MCHID"), APPID_MCHID_NOT_MATCH(
			8, "APPID_MCHID_NOT_MATCH", "appid和mch_id不匹配", "appid和mch_id不匹配"), LACK_PARAMS(
			9, "LACK_PARAMS", "缺少参数", "缺少必要的请求参数"), OUT_TRADE_NO_USED(10,
			"OUT_TRADE_NO_USED", "商户订单号重复", "同一笔交易不能多次提交"), SIGNERROR(11,
			"SIGNERROR", "签名错误", "参数签名结果不正确"), XML_FORMAT_ERROR(12,
			"XML_FORMAT_ERROR", "XML格式错误", "XML格式错误"), REQUIRE_POST_METHOD(13,
			"REQUIRE_POST_METHOD", "请使用post方法", "未使用post传递参数 "), POST_DATA_EMPTY(
			14, "POST_DATA_EMPTY", "post数据为空", "post数据不能为空"), NOT_UTF8(15,
			"NOT_UTF8", "编码格式错误", "未使用指定编码格式");

	private Integer value;
	private String text;
	private String description;
	private String resone;

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResone() {
		return resone;
	}

	public void setResone(String resone) {
		this.resone = resone;
	}

	ReqErrCodeEnum(Integer value, String text, String description, String resone) {
		this.value = value;
		this.text = text;
		this.description = description;
		this.resone = resone;
	}

	public static ReqErrCodeEnum textOf(String text) {
		ReqErrCodeEnum[] values = ReqErrCodeEnum.values();
		for (ReqErrCodeEnum e : values) {
			if (e.getText().equals(text)) {
				return e;
			}
		}
		return null;
	}
}
