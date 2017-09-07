package com.tencent.wxpay.common;

/**
 * @description 微信支付常量
 * 
 */
public class WXPayConfig {
	/** iPhone appid */
	public static final String IPHONE_APP_ID = "wxcb69e6447e92b4e5";
	/** iPhone 商户号 商户收款账号 */
	public static final String IPHONE_MCH_ID = "1247899201";
	/** iPhone API密钥，在商户平台设置 */
	public static final String IPHONE_API_KEY = "wxKFS2A8V9HZ8L8VWV68KSZGBS32ikan";

	/** Android手机 appid */
	public static final String ANDROID_APP_ID = "wxc17481416ce1fb25";
	/** Android手机 商户号 商户收款账号 */
	public static final String ANDROID_MCH_ID = "1247909601";
	/** Android手机 API密钥，在商户平台设置 */
	public static final String ANDROID_API_KEY = "wxKFS2A8V9HZ8L8VWV68KSZGBS32ikan";

	/** HD（iPad和Android平板） appid */
	public static final String HD_APP_ID = "wx3381c6d526b886f7";
	/** HD（iPad和Android平板） 商户号 商户收款账号 */
	public static final String HD_MCH_ID = "1247914401";
	/** HD（iPad和Android平板） API密钥，在商户平台设置 */
	public static final String HD_API_KEY = "wxKFS2A8V9HZ8L8VWV68KSZGBS32ikan";

	/** 移动网站  appid */
	public static final String WAP_APP_ID = "wx1575d0091374f133";
	/** 移动网站  商户号 商户收款账号 */
	public static final String WAP_MCH_ID = "1331215501";
	/** 移动网站 API密钥，在商户平台设置 */
	public static final String WAP_API_KEY = "wxKFS2A8V9HZ8L8VWV68KSZGBS32ikan";
	/** 移动网站公众平台 应用密钥*/
	public static final String APP_SECRET = "2598805ee8ea253e9b30c6265469ade7";
	
	
	/** 统一下单 */
	public static final String UNI_FIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	/** 查询订单 */
	public static String PAY_QUERY_API = "https://api.mch.weixin.qq.com/pay/orderquery";
	/** 关闭订单 */
	public static String REVERSE_API = "https://api.mch.weixin.qq.com/pay/closeorder";
	/** 申请退款 */
	public static String REFUND_API = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	/** 查询退款 */
	public static String REFUND_QUERY_API = "https://api.mch.weixin.qq.com/pay/refundquery";
	/** 下载对账单 */
	public static String DOWNLOAD_BILL_API = "https://api.mch.weixin.qq.com/pay/downloadbill";
	/** 统计上报API */
	public static String REPORT_API = "https://api.mch.weixin.qq.com/payitil/report";
	// 是否使用异步线程的方式来上报API测速，默认为异步模式
	public static boolean useThreadToDoReport = true;
	// 机器IP
	public static String ip = "";

	/** 支付通知 */
	public static final String NOTIFY_URL = "http://entrance.ikan.cn/entrance/servlet/wxpaynotify/";

	/** HTTPS证书的本地路径 */
	public static String CERT_LOCAL_PATH = "";
	/** HTTPS证书密码，默认密码等于商户号MCHID */
	public static String CERT_PASSWORD = "";
	
	/** 获取access_token */
	public static final String OAUTH_ACCESS_TOKEN_API = "https://api.weixin.qq.com/sns/oauth2/access_token?";
	
	/** 获取普通access_token */
	public static final String ACCESS_TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token?";
	
	/** 获取用户基本信息 */
	public static final String USER_INFO_API = "https://api.weixin.qq.com/cgi-bin/user/info?";
	
	/** 获取code */
	public static final String OAUTH2AUTHORIZE = "https://open.weixin.qq.com/connect/oauth2/authorize?";
	
	/**  获取jsapiticket  **/
	public static final String  TICKET_GETTICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?";
	
}
