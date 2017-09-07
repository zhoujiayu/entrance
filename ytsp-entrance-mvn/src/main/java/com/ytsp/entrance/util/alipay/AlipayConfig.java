package com.ytsp.entrance.util.alipay;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。

 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig {

	// ↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088811992971933";
	/**
	 * 商户的私钥
	 * */
	public static String private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMavmfZFNNwyp4E2Z4ooV4dnK5Y+8adhwFvii3elz/Nh2znyiDg8/D/PdO8NqW+UQF7m4wRtzttqtCgGAWo/+tsUqEPbsCfdP+PAePkGrfflQ4LXP0GktoPzOMyoV3wn9lv9rhfxa5XuI1tvpSAE3egW32bihA4RDSPG6vprplVXAgMBAAECgYEAknKIt19XkR69HJ6vPsRxj9pZ5ErM8CU7Ff8r8asEVk7AujuscFdzTs1pUXLuetH5iHoCxxbxLAXAeOfETz6NdXRrYfKDiFgKLaDiDn3lqHJ3wH4omNpYL4Eid9vwhdp0Vasc3c0Bue0BUzJJiTRNmvcGM88JJyrtdk83VK6AWXECQQD1FPjfyrsJU0JYD1WkkjpFnYnAVsSMkFuKx8Tm4Con72gmw3y1vFmc7+gJSn7ZhLTB1KSiboGBzpld8iEMKLljAkEAz4mCC/0GVjoUceEanNu91b687fKNbCxWgc4ox8VBrtOT+T57mHNIPL+qSLRX0Fcdyp9KRcgJap7/Gj5ux13wfQJAQsFqpLJ4zapzCL9siX4XBz1I9y3rLYpolN2jIWvvr58DVIOs5WefVSL3pgg3kxIVljJQgbnt5qial79LBx/UlwJAdE9zU7ata3vHY38txAUe4gCwr4ZFHf/HR547mqIBSrKG9qO+7tSFAo7EJ1Ty9e8s4hbdsDgIlvmerJ2axh7unQJANNVPsjvEHI33Fmdapb97tM+74eV64C2Hfc3hoXYsnQEyspAz6CN+QNdsr/DdwDUtfwVcwaDQxsF3N6OTdmOgSQ==";

	/**
	 * 支付宝的公钥，无需修改该值
	 * */
	public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	// ↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "/home/imagemedia/alipay_logs/";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "utf-8";

	// 签名方式 不需修改
	public static String sign_type = "RSA";
	// 支付宝账号
	public static String seller_email = "service@ikan.cn";
	
	// 帐务明细分页查询请求方法
	public static String account_page_query = "account.page.query";
	// 即时到账有密退款 接口方法名称
	public static String refund_fastpay_by_platform_pwd = "refund_fastpay_by_platform_pwd";
	
	/** 订单有效时长（ 小时） */
	public static int ORDER_EFFECTIVE_TIME = 3;
	
	/** 交易异步通知url */
	public static String notify_url = "http://219.141.176.132:8080/servlet/alipaynotify/";
	/** 正式交易异步通知url */
//	public static String notify_url = "http://entrance.ikan.cn/servlet/alipaynotify/";
	/** 交易同步通知url http://219.141.176.132/web_Mobile/ikan_orderDetail.html*/
	public static String return_url = "http://172.16.218.11/M/ikan_orderDetail.html";
	/** 卖家支付宝帐户 */
	public static String seller_id = "2088811992971933";

	/** 支付宝分配给开发者的应用ID **/
	public static String APP_ID = "2016022101154036";
}
