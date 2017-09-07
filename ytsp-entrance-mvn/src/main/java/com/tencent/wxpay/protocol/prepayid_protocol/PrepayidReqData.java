package com.tencent.wxpay.protocol.prepayid_protocol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.tencent.wxpay.common.RandomStringGenerator;
import com.tencent.wxpay.common.Signature;

public class PrepayidReqData {
	/** 公众账号ID String(32) (必填) */
	private String appid;
	/** 商户号 String(32) (必填) */
	private String mch_id;
	/** 设备号 String(32) (非必填) */
	private String device_info;
	/** 随机字符串 String(32) (必填) */
	private String nonce_str;
	/** 签名 String(32) (必填) */
	private String sign;
	/** 商品描述 String(32) (必填) */
	private String body;
	/** 商品详情 String(8192) (非必填) */
	private String detail;
	/** 附加数据 String(127) (非必填) */
	private String attach;
	/** 商户订单号 String(32) (必填) */
	private String out_trade_no;
	/** 货币类型 String(16) (非必填) */
	private String fee_type = "CNY";
	/** 总金额 (必填) Int 单位分 */
	private int total_fee;
	/** 终端IP String(16) (必填) */
	private String spbill_create_ip;
	/** 交易起始时间 String(14) (非必填) */
	private String time_start;
	/** 交易结束时间 String(14) (非必填) */
	private String time_expire;
	/** 商品标记 String(32) (非必填) */
	private String goods_tag;
	/** 通知地址 String(256) (必填) 接收微信支付异步通知回调地址 */
	private String notify_url;
	/** 交易类型 String(16) (必填) 取值如下：JSAPI，NATIVE，APP，WAP */
	private String trade_type = "APP";
	/** 商品ID String(32) (非必填) trade_type=NATIVE，此参数必传。此id为二维码中包含的商品ID，商户自行定义 */
	private String product_id;
	/** 用户标识 String(128) (非必填) trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。 */
	private String openid;

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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getFee_type() {
		return fee_type;
	}

	public void setFee_type(String fee_type) {
		this.fee_type = fee_type;
	}

	public int getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}

	public String getTime_start() {
		return time_start;
	}

	public void setTime_start(String time_start) {
		this.time_start = time_start;
	}

	public String getTime_expire() {
		return time_expire;
	}

	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}

	public String getGoods_tag() {
		return goods_tag;
	}

	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	/**
	 * @param appid
	 *            公众账号ID String(32) (必填)
	 * @param mch_id
	 *            商户号 String(32) (必填)
	 * @param body
	 *            商品描述 String(32) (必填)
	 * @param attach
	 *            附加数据 String(127) (非必填)
	 * @param out_trade_no
	 *            商户订单号 String(32) (必填)
	 * @param total_fee
	 *            总金额 (必填) Int 单位分
	 * @param spbill_create_ip
	 *            终端IP String(16) (必填)
	 * @param time_start
	 *            交易起始时间 String(14) (非必填)
	 * @param time_expire
	 *            交易结束时间 String(14) (非必填)
	 * @param goods_tag
	 *            商品标记 String(32) (非必填)
	 * @param notify_url
	 *            支付回调
	 * @param appKey
	 *            密钥
	 */
	public PrepayidReqData(String appid, String mch_id, String body,
			String attach, String out_trade_no, int total_fee,
			String spbill_create_ip, String time_start, String time_expire,
			String goods_tag, String notify_url, String appKey) {
		super();
		this.appid = appid;
		this.mch_id = mch_id;
		this.nonce_str = RandomStringGenerator.getRandomStringByLength(32);
		this.body = body;
		this.attach = attach;
		this.out_trade_no = out_trade_no;
		this.total_fee = total_fee;
		this.spbill_create_ip = spbill_create_ip;
		this.time_start = time_start;
		this.time_expire = time_expire;
		this.goods_tag = goods_tag;
		// 通知
		this.notify_url = notify_url;
		this.sign = Signature.getSign(toMap(), appKey);
	}

	/**
	 * @param appid
	 *            公众账号ID String(32) (必填)
	 * @param mch_id
	 *            商户号 String(32) (必填)
	 * @param body
	 *            商品描述 String(32) (必填)
	 * @param attach
	 *            附加数据 String(127) (非必填)
	 * @param out_trade_no
	 *            商户订单号 String(32) (必填)
	 * @param total_fee
	 *            总金额 (必填) Int 单位分
	 * @param spbill_create_ip
	 *            终端IP String(16) (必填)
	 * @param time_start
	 *            交易起始时间 String(14) (非必填)
	 * @param time_expire
	 *            交易结束时间 String(14) (非必填)
	 * @param goods_tag
	 *            商品标记 String(32) (非必填)
	 * @param notify_url
	 *            支付回调
	 * @param appKey
	 *            密钥
	 */
	public PrepayidReqData(String appid, String mch_id, String body,
			String attach, String out_trade_no, int total_fee,
			String spbill_create_ip, String time_start, String time_expire,
			String goods_tag, String notify_url, String appKey,String openId,String tradeType) {
		super();
		this.appid = appid;
		this.mch_id = mch_id;
		this.nonce_str = RandomStringGenerator.getRandomStringByLength(32);
		this.body = body;
		this.attach = attach;
		this.out_trade_no = out_trade_no;
		this.openid = openId;
		this.trade_type = tradeType;
		this.total_fee = total_fee;
		this.spbill_create_ip = spbill_create_ip;
		this.time_start = time_start;
		this.time_expire = time_expire;
		this.goods_tag = goods_tag;
		// 通知
		this.notify_url = notify_url;
		this.sign = Signature.getSign(toMap(), appKey);
	}
	
	public PrepayidReqData() {
		super();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Object obj;
			try {
				obj = field.get(this);
				if (obj != null) {
					map.put(field.getName(), obj);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}
