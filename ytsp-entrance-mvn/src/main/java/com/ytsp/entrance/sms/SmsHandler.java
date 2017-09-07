package com.ytsp.entrance.sms;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

public class SmsHandler {
	private static final Log log = LogFactory.getLog(SmsHandler.class);
	
	/**
	* <p>功能描述:东方网润发送短信</p>
	* <p>参数：@param phone
	* <p>参数：@param msg
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 * @throws JSONException 
	 */
	public static String DFWRSendSms(String phone, String msg){
		Map<String,String> param = SmsUtil.getDFWRSendMessageParam(msg, phone);
		String ret = HttpClientHelper.convertStreamToString(HttpClientHelper.get(
				SmsConfig.DFWR_SEND_MSG_URL, param,"GBK"), "GBK");
		return ret;
	}
	
	
	/**
	* <p>功能描述:鸿联九五发送短信</p>
	* <p>参数：@param phone
	* <p>参数：@param msg
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String HL95SendMsm(String phone, String msg) {
		Map<String,String> param = SmsUtil.getSendMessageParam(msg, phone);
		String ret = HttpClientHelper.convertStreamToString(HttpClientHelper.get(
				SmsConfig.SEND_MSG_URL, param,"GB2312"), "GB2312");
		return ret;
	}
	
	public static boolean sendSms(String mobile, String verifyCode) {
		Map<String, String> paras = new HashMap<String, String>();
		String[][] data = null;
		ArrayList<String[]> para = new ArrayList<String[]>();
		para.add(new String[] {
				"con",
				"【爱看儿童乐园】您的验证码是：%P%，10分钟内有效。如非您本人操作，可忽略本消息。".replace("%P%",
						verifyCode) });
		para.add(new String[] { "mob", mobile });
		para.add(new String[] { "uid", "FR4KCAXT3sEP" });
		para.add(new String[] { "pas", "53m85dsz" });
		para.add(new String[] { "type", "json" });
		paras.put("cid", "aJdl8yvvDvqS");
		paras.put("mob", mobile);
		paras.put("uid", "FR4KCAXT3sEP");
		paras.put("pas", "53m85dsz");
		paras.put("type", "json");
		paras.put("p1", verifyCode);
		data = new String[para.size()][];
		para.toArray(data);
		String r = null;
		try {

			r = HttpClientHelper.convertStreamToString(HttpClientHelper.post(
					"http://api.weimi.cc/2/sms/send.html", paras), "UTF-8");
			if (r != null && r.contains("成功")) {
				System.out.println("SMS--verifyCode:" + verifyCode + ",mobile:"
						+ mobile);
			}else{
				System.out.println(r != null?r.toString() : "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return r != null && r.contains("成功");
	}

	/**
	 * 订单支付成功，短信通知用户
	 * */
	public static boolean sendSmsAfterPay(String mobile, String orderId,
			Double totalPrice) {
		Map<String, String> paras = new HashMap<String, String>();
		String[][] data = null;
		ArrayList<String[]> para = new ArrayList<String[]>();
		String con = "【爱看儿童乐园】亲爱的粑粑麻麻，您的订单%P%已收到，小爱会尽快为您发货。爱看不会以订单无效为由主动要求您提供银行卡信息操作退款，谨防诈骗！任何问题或建议，请致电：400-600-0977。愿您的宝宝健康快乐成长！";
		para.add(new String[] { "con", con });
		para.add(new String[] { "mob", mobile });
		para.add(new String[] { "uid", "FR4KCAXT3sEP" });
		para.add(new String[] { "pas", "53m85dsz" });
		para.add(new String[] { "type", "json" });
		paras.put("cid", "UUzzAGMqPhEv");
		paras.put("mob", mobile);
		paras.put("uid", "FR4KCAXT3sEP");
		paras.put("pas", "53m85dsz");
		paras.put("type", "json");
		paras.put("p1", orderId);
		// paras.put("p2", NumberFormat.priceFormat(totalPrice));
		data = new String[para.size()][];
		para.toArray(data);
		String r = null;
		try {
			// TODO 异常
			r = HttpClientHelper.convertStreamToString(HttpClientHelper.post(
					"http://api.weimi.cc/2/sms/send.html", paras), "UTF-8");
			if (StringUtils.isNotEmpty(r) && r.contains("成功")) {
				System.out.println("SMS--orderId:" + orderId + ",totalPrice:"
						+ totalPrice + ",mobile:" + mobile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtils.isNotEmpty(r) && r.contains("成功");
	}

	/**
	 * 电子票付款成功后发短信通知用户
	 * 
	 * @param mobile
	 *            手机号码
	 * @param amount
	 *            数量
	 * @param exhibition
	 *            会场
	 * @param ticketCode
	 *            验证码
	 * @param time
	 *            入场时间
	 * @param address
	 *            会场地点
	 * @return
	 */
	public static boolean sendSmsAfterPayForTicket(String mobile, int amount,
			String exhibition, String ticketCode, String time, String address) {
		Map<String, String> paras = new HashMap<String, String>();
		String[][] data = null;
		ArrayList<String[]> para = new ArrayList<String[]>();
		StringBuffer sb = new StringBuffer("【爱看儿童乐园】亲爱的粑粑麻麻您好！");
		sb.append("您已成功购买《中国经典动画形象展--%P%》".replace("%P%", exhibition))
				.append("门票%P%张".replace("%P%", String.valueOf(amount)))
				.append("请凭手机号后四位+姓名（%P%）到活动现场兑换门票。".replace("%P%", ticketCode))
				.append("展出时间：%P%，".replace("%P%", time))
				.append("展出地点：%P%。".replace("%P%", address))
				.append("服务电话：400-600-0977。");
		para.add(new String[] { "con", sb.toString() });
		para.add(new String[] { "mob", mobile });
		para.add(new String[] { "uid", "FR4KCAXT3sEP" });
		para.add(new String[] { "pas", "53m85dsz" });
		para.add(new String[] { "type", "json" });
		paras.put("cid", "tdvt6HCB0GSi");
		paras.put("mob", mobile);
		paras.put("uid", "FR4KCAXT3sEP");
		paras.put("pas", "53m85dsz");
		paras.put("type", "json");
		paras.put("p1", exhibition);
		paras.put("p2", String.valueOf(amount));
		paras.put("p3", ticketCode);
		paras.put("p4", time);
		paras.put("p5", address);
		data = new String[para.size()][];
		para.toArray(data);
		String r = null;
		try {
			r = HttpClientHelper.convertStreamToString(HttpClientHelper.post(
					"http://api.weimi.cc/2/sms/send.html", paras), "UTF-8");
			if (StringUtils.isNotEmpty(r) && r.contains("成功")) {
				System.out.println("SMS--" + exhibition + ",mobile:" + mobile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return StringUtils.isNotEmpty(r) && r.contains("成功");
	}

	public static void main(String[] orgs) {
//		SmsHandler.sendSms("18101331762", "5678");
		// SmsHandler.sendSmsAfterPay("18101331762", "",0d);
//		SmsHandler.sendSmsAfterPay("18101331762", "100010", 100d);
//			String ret = SmsHandler.HL95SendMsm("18611027550", "爱看儿童乐园短信测试");
		try {
//			for (int i = 0; i < 5; i++) {
//				Thread.sleep(3000);
				String ret = SmsHandler.DFWRSendSms("18611027550", SmsConfig.SMS_MSG_CONTENT.replaceAll("CODE", "11111"));
				System.out.println(ret);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(obj.toString());
	}

	private static String postHTML(String url, String[][] data, int retry,
			String charset) {
		String r = "";
		HttpClient httpClient = null;
		PostMethod postMethod = null;
		try {
			httpClient = new HttpClient();
			postMethod = new PostMethod(url);
			postMethod.getParams().setParameter(
					HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
			if (data != null) {
				NameValuePair[] values = new NameValuePair[data.length];
				for (int i = 0; i < data.length; i++) {
					String[] d = data[i];
					if (d == null || d.length < 2)
						continue;
					values[i] = new NameValuePair(d[0], d[1]);
				}
				postMethod.setRequestBody(values);
			}
			int retval = httpClient.executeMethod(postMethod);
			if (retval == HttpStatus.SC_OK) {
				r = postMethod.getResponseBodyAsString();
				if (r != null)
					r = r.trim();
			} else if (retval == HttpStatus.SC_REQUEST_TIMEOUT) {
				log.error("HTTP[" + url + "]请求超时：" + postMethod.getStatusLine());
				r = null;
			} else {
				log.error("HTTP[" + url + "]请求失败：" + postMethod.getStatusLine());
				r = null;
			}
		} catch (UnknownHostException e) {
			if (retry >= 10) {
				log.error("域名解析错误10次，返回null：" + e);
				return null;
			}
			try {
				Thread.sleep(500); // 休息0.5秒，10次共5秒
			} catch (InterruptedException e1) {
			}
			return postHTML(url, data, retry + 1, charset);
		} catch (Exception ex) {
			// 有异常返回
			log.error("HTTP[" + url + "]请求失败：" + ex);
			r = null;
		} finally {
			try {
				if (null != postMethod && postMethod.hasBeenUsed()) {
					postMethod.releaseConnection();
				}
			} catch (Exception ex) {
				// logger.error("释放HTTP连接失败");
			}
		}
		return r;
	}
}
