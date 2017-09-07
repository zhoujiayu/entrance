package com.tencent.wxpay.protocol.pay_protocol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.tencent.wxpay.common.RandomStringGenerator;
import com.tencent.wxpay.common.Signature;

/**
 * @description 支付请求
 * 
 */
public class PayReqData {
	/** 公众账号ID */
	private String appid = "";
	/** 商户号 */
	private String partnerid = "";
	/** 预支付交易会话ID */
	private String prepayid = "";
	/** 扩展字段 固定值 */
	private String packageValue = "Sign=WXPay";
	/** 随机字符串 */
	private String noncestr = "";
	/** 时间戳 */
	private String timestamp = "";
	/** 上面字段的签名 */
	private String sign = "";
	/** 该参数是为了生成 js api  加载时候的签名用.jsapi_ticket只会存在7200秒  **/
	private String jsapi_ticket;
	/** 该签名.主要是给加载微信js使用 与 sign 不一样 **/
	private String signature;

	public String getJsapi_ticket() {
		return jsapi_ticket;
	}

	public void setJsapi_ticket(String jsapi_ticket) {
		this.jsapi_ticket = jsapi_ticket;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getPartnerid() {
		return partnerid;
	}

	public void setPartnerid(String partnerid) {
		this.partnerid = partnerid;
	}

	public String getPrepayid() {
		return prepayid;
	}

	public void setPrepayid(String prepayid) {
		this.prepayid = prepayid;
	}

	public String getPackageValue() {
		return packageValue;
	}

	public void setPackageValue(String packageValue) {
		this.packageValue = packageValue;
	}

	public String getNoncestr() {
		return noncestr;
	}

	public void setNoncestr(String noncestr) {
		this.noncestr = noncestr;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public PayReqData() {
		super();
	}

	public PayReqData(String prepayId, String appId, String mchId, String appKey) {
		super();
		this.appid = appId;
		this.partnerid = mchId;
		this.prepayid = prepayId;
		this.noncestr = RandomStringGenerator.getRandomStringByLength(32);
		this.timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		this.sign = Signature.getSign(toMap(), appKey);
	}
	
	public PayReqData(String appId, String prepayId, String mchId, String appKey,boolean isJSAPI) {
		super();
		this.appid = appId;
		this.partnerid = mchId;
		this.prepayid = prepayId;
		this.noncestr = RandomStringGenerator.getRandomStringByLength(32);
		this.timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		if(isJSAPI){
			SortedMap<String, String> params = getJSAPISignParam(prepayId);
			this.sign = Signature.createSign(params, appKey);
		}else{
			this.sign = Signature.getSign(toMap(), appKey);
		}
	}
	
	/**
	* <p>功能描述:获取jsapi签名参数</p>
	* <p>参数：@param prepayId
	* <p>参数：@return</p>
	* <p>返回类型：SortedMap<String,String></p>
	 */
	private SortedMap<String, String> getJSAPISignParam(String prepayId){
		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("appId", this.appid);
		params.put("timeStamp",  this.timestamp);
		params.put("nonceStr", this.noncestr);
		params.put("package", "prepay_id=" + prepayId);
		params.put("signType", "MD5");
		return params;
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Object obj;
			try {
				obj = field.get(this);
				if (obj != null) {
					String f_name = field.getName();
					if (f_name == "packageValue") {
						f_name = "package";
					}
					map.put(f_name, obj);
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
