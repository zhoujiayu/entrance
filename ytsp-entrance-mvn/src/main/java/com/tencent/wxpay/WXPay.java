package com.tencent.wxpay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.wxpay.business.PayQueryBusiness;
import com.tencent.wxpay.business.PrepayidBusiness;
import com.tencent.wxpay.business.ReverseBusiness;
import com.tencent.wxpay.common.Signature;
import com.tencent.wxpay.common.WXOAuthUtil;
import com.tencent.wxpay.common.WXPayConfig;
import com.tencent.wxpay.common.WXUtil;
import com.tencent.wxpay.protocol.pay_protocol.PayReqData;
import com.tencent.wxpay.protocol.pay_query_protocol.PayQueryReqData;
import com.tencent.wxpay.protocol.prepayid_protocol.PrepayidReqData;
import com.tencent.wxpay.protocol.reverse_protocol.ReverseReqData;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.enums.EbOrderSourceEnum;
import com.ytsp.entrance.util.WXPayCache;

public class WXPay {
	
	/**
	 * 获取PrepayId
	 * 
	 * @param body
	 *            商品描述，不可为空，32位字符
	 * @param attach
	 *            附加数据，可为空
	 * @param out_trade_no
	 *            Ikan订单号
	 * @param total_fee
	 *            支付金额，以分为单位
	 * @param spbill_create_ip
	 *            终端IP，不可为空
	 * @param goods_tag
	 *            商品标记，可为空
	 * @param ebOrderSourceEnum
	 *            订单来源，因为有三个微信商户ID，分别对应IPHONE、ANDROID、HD，根据不同的订单来源生成不同的参数
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	public static String getJSAPIPrepayid(String body, String attach,
			String out_trade_no, int total_fee, String spbill_create_ip,
			String goods_tag, EbOrderSourceEnum ebOrderSourceEnum,String code)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, Exception {
		PrepayidReqData reqData = null;
		
		String openid = getOpenIdByCode(code);
		reqData = new PrepayidReqData(WXPayConfig.WAP_APP_ID,
				WXPayConfig.WAP_MCH_ID, body, attach, out_trade_no, total_fee,
				spbill_create_ip, "", "", goods_tag, WXPayConfig.NOTIFY_URL,
				WXPayConfig.WAP_API_KEY, openid, "JSAPI");
		return new PrepayidBusiness().run(reqData, null,
				WXPayConfig.WAP_API_KEY);

	}
	
	
	/**
	 * 获取PrepayId
	 * 
	 * @param body
	 *            商品描述，不可为空，32位字符
	 * @param attach
	 *            附加数据，可为空
	 * @param out_trade_no
	 *            Ikan订单号
	 * @param total_fee
	 *            支付金额，以分为单位
	 * @param spbill_create_ip
	 *            终端IP，不可为空
	 * @param goods_tag
	 *            商品标记，可为空
	 * @param ebOrderSourceEnum
	 *            订单来源，因为有三个微信商户ID，分别对应IPHONE、ANDROID、HD，根据不同的订单来源生成不同的参数
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	public static String getPrepayid(String body, String attach,
			String out_trade_no, int total_fee, String spbill_create_ip,
			String goods_tag, EbOrderSourceEnum ebOrderSourceEnum,int isUseNewWXPay)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, Exception {
		PrepayidReqData reqData = null;
		//微信合并后支付
		if(isUseNewWXPay == 1){
			reqData = new PrepayidReqData(WXPayConfig.ANDROID_APP_ID,
					WXPayConfig.ANDROID_MCH_ID, body, attach, out_trade_no,
					total_fee, spbill_create_ip, "", "", goods_tag,
					WXPayConfig.NOTIFY_URL, WXPayConfig.ANDROID_API_KEY);
			return new PrepayidBusiness().run(reqData, null,
					WXPayConfig.ANDROID_API_KEY);
		} else {
			if (ebOrderSourceEnum == EbOrderSourceEnum.ANDROID) {
				reqData = new PrepayidReqData(WXPayConfig.ANDROID_APP_ID,
						WXPayConfig.ANDROID_MCH_ID, body, attach, out_trade_no,
						total_fee, spbill_create_ip, "", "", goods_tag,
						WXPayConfig.NOTIFY_URL, WXPayConfig.ANDROID_API_KEY);
				return new PrepayidBusiness().run(reqData, null,
						WXPayConfig.ANDROID_API_KEY);
			} else if (ebOrderSourceEnum == EbOrderSourceEnum.IPHONE
					|| ebOrderSourceEnum == EbOrderSourceEnum.IPAD) {
				reqData = new PrepayidReqData(WXPayConfig.IPHONE_APP_ID,
						WXPayConfig.IPHONE_MCH_ID, body, attach, out_trade_no,
						total_fee, spbill_create_ip, "", "", goods_tag,
						WXPayConfig.NOTIFY_URL, WXPayConfig.IPHONE_API_KEY);
				return new PrepayidBusiness().run(reqData, null,
						WXPayConfig.IPHONE_API_KEY);
			}else {
				reqData = new PrepayidReqData(WXPayConfig.HD_APP_ID,
						WXPayConfig.HD_MCH_ID, body, attach, out_trade_no,
						total_fee, spbill_create_ip, "", "", goods_tag,
						WXPayConfig.NOTIFY_URL, WXPayConfig.HD_API_KEY);
				return new PrepayidBusiness().run(reqData, null,
						WXPayConfig.HD_API_KEY);
			}
		}
	}
	
	
	/**
	* <p>功能描述:移动端网站获取支付请求数据</p>
	* <p>参数：@param prepayId   
	* <p>参数：@param url  当前界面路径
	* <p>参数：@return</p>
	* <p>返回类型：PayReqData</p>
	 */
	public static PayReqData getWapMobilePayReqData(String prepayId,String url) {
		PayReqData data = null;
		// 移动端网站支付
		data = new PayReqData( WXPayConfig.WAP_APP_ID,prepayId,
				WXPayConfig.WAP_MCH_ID, WXPayConfig.WAP_API_KEY,true);
		// 获取jsapi_ticket
		String ticket = "";
		//校验缓存里的ticket是否过期，过期重新获取ticket
		if(isTicketValidate()){
			ticket = getJSAPITicket();
		}else{
			String ticketKey = WXPayCache.getInstance().getTicketKey();
			ticket = WXPayCache.getInstance().getJSAPIticketMap().get(ticketKey);
		}
		data.setJsapi_ticket(ticket);
		// 设置js api签名
		String signValue = WXUtil.getJSAPISignValue(ticket, data.getNoncestr(),
				data.getTimestamp(), url);
//		System.out.println("signValue:"+signValue);
		// 加密签名
		data.setSignature(Signature.getSha1(signValue));
//		System.out.println(data.getSignature());
		return data;

	}
	
	/**
	 * 获取支付请求数据
	 * 
	 * @param prepayId
	 * @param ebOrderSourceEnum
	 * @return
	 */
	public static PayReqData getPayReqData(String prepayId,
			EbOrderSourceEnum ebOrderSourceEnum,int isUseNewWXPay) {
		PayReqData data = null;
		
		//微信appid合并后支付
		if(isUseNewWXPay == 1){
			data = new PayReqData(prepayId, WXPayConfig.ANDROID_APP_ID,
					WXPayConfig.ANDROID_MCH_ID, WXPayConfig.ANDROID_API_KEY);
		}else{
			if (ebOrderSourceEnum == EbOrderSourceEnum.ANDROID) {
				data = new PayReqData(prepayId, WXPayConfig.ANDROID_APP_ID,
						WXPayConfig.ANDROID_MCH_ID, WXPayConfig.ANDROID_API_KEY);
			} else if (ebOrderSourceEnum == EbOrderSourceEnum.IPHONE
					|| ebOrderSourceEnum == EbOrderSourceEnum.IPAD) {
				data = new PayReqData(prepayId, WXPayConfig.IPHONE_APP_ID,
						WXPayConfig.IPHONE_MCH_ID, WXPayConfig.IPHONE_API_KEY);
			} else {
				data = new PayReqData(prepayId, WXPayConfig.HD_APP_ID,
						WXPayConfig.HD_MCH_ID, WXPayConfig.HD_API_KEY);
			}
		}
		return data;

	}
	
	/**
	 * 支付结果查询
	 * @param out_trade_no 商户订单号
	 * @param ebOrderSourceEnum 订单来源
	 * @return int 1、关闭成功 2、交易不存在 3、交易状态不符合 4、调用微信参数问题 5、系统错误
	 * @throws Exception
	 */
	public static int closeTrade(String out_trade_no,
			EbOrderSourceEnum ebOrderSourceEnum,int isUseNewWXPay) throws Exception {
		
		ReverseReqData reqData = null;
		//移动端网站
		if(isUseNewWXPay == 2){
			reqData = new ReverseReqData("", out_trade_no,
					WXPayConfig.WAP_APP_ID, WXPayConfig.WAP_MCH_ID,
					WXPayConfig.WAP_API_KEY);
			return new ReverseBusiness().doOneReverse(reqData,
					WXPayConfig.WAP_API_KEY);
		}
		//微信合并后的支付
		if(isUseNewWXPay == 1){
			reqData = new ReverseReqData("", out_trade_no,
					WXPayConfig.ANDROID_APP_ID, WXPayConfig.ANDROID_MCH_ID,
					WXPayConfig.ANDROID_API_KEY);
			return new ReverseBusiness().doOneReverse(reqData,
					WXPayConfig.ANDROID_API_KEY);
		}else{
			if (ebOrderSourceEnum == EbOrderSourceEnum.ANDROID) {
				reqData = new ReverseReqData("", out_trade_no,
						WXPayConfig.ANDROID_APP_ID, WXPayConfig.ANDROID_MCH_ID,
						WXPayConfig.ANDROID_API_KEY);
				return new ReverseBusiness().doOneReverse(reqData,
						WXPayConfig.ANDROID_API_KEY);
			} else if (ebOrderSourceEnum == EbOrderSourceEnum.IPHONE
					|| ebOrderSourceEnum == EbOrderSourceEnum.IPAD) {
				reqData = new ReverseReqData("", out_trade_no,
						WXPayConfig.IPHONE_APP_ID, WXPayConfig.IPHONE_MCH_ID,
						WXPayConfig.IPHONE_API_KEY);
				return new ReverseBusiness().doOneReverse(reqData,
						WXPayConfig.IPHONE_API_KEY);
			}else if(ebOrderSourceEnum == EbOrderSourceEnum.WAPMOBILE){
				reqData = new ReverseReqData("", out_trade_no,
						WXPayConfig.WAP_APP_ID, WXPayConfig.WAP_MCH_ID,
						WXPayConfig.WAP_API_KEY);
				return new ReverseBusiness().doOneReverse(reqData,
						WXPayConfig.WAP_API_KEY);
			}else {
				reqData = new ReverseReqData("", out_trade_no,
						WXPayConfig.HD_APP_ID, WXPayConfig.HD_MCH_ID,
						WXPayConfig.HD_API_KEY);
				return new ReverseBusiness().doOneReverse(reqData,
						WXPayConfig.HD_API_KEY);
			}
		}
	}
	
	/**
	 * 支付结果查询
	 * 
	 * @param out_trade_no
	 * @param ebOrderSourceEnum
	 * @return
	 * @throws Exception
	 */
	public static boolean payQuery(String out_trade_no,
			EbOrderSourceEnum ebOrderSourceEnum,int isUseNewWXPay) throws Exception {
		PayQueryReqData reqData = null;
		//移动端网站微信支付
		if(isUseNewWXPay == 2){
			reqData = new PayQueryReqData("", out_trade_no,
					WXPayConfig.WAP_APP_ID, WXPayConfig.WAP_MCH_ID,
					WXPayConfig.WAP_API_KEY);
			return new PayQueryBusiness().doOnePayQuery(reqData,
					WXPayConfig.WAP_API_KEY);
		}
		//微信合并后的支付
		if(isUseNewWXPay == 1){
			reqData = new PayQueryReqData("", out_trade_no,
					WXPayConfig.ANDROID_APP_ID, WXPayConfig.ANDROID_MCH_ID,
					WXPayConfig.ANDROID_API_KEY);
			return new PayQueryBusiness().doOnePayQuery(reqData,
					WXPayConfig.ANDROID_API_KEY);
		} else {
			if (ebOrderSourceEnum == EbOrderSourceEnum.ANDROID) {
				reqData = new PayQueryReqData("", out_trade_no,
						WXPayConfig.ANDROID_APP_ID, WXPayConfig.ANDROID_MCH_ID,
						WXPayConfig.ANDROID_API_KEY);
				return new PayQueryBusiness().doOnePayQuery(reqData,
						WXPayConfig.ANDROID_API_KEY);
			} else if (ebOrderSourceEnum == EbOrderSourceEnum.IPHONE
					|| ebOrderSourceEnum == EbOrderSourceEnum.IPAD) {
				reqData = new PayQueryReqData("", out_trade_no,
						WXPayConfig.IPHONE_APP_ID, WXPayConfig.IPHONE_MCH_ID,
						WXPayConfig.IPHONE_API_KEY);
				return new PayQueryBusiness().doOnePayQuery(reqData,
						WXPayConfig.IPHONE_API_KEY);
			}else if(ebOrderSourceEnum == EbOrderSourceEnum.WAPMOBILE){
				reqData = new PayQueryReqData("", out_trade_no,
						WXPayConfig.WAP_APP_ID, WXPayConfig.WAP_MCH_ID,
						WXPayConfig.WAP_API_KEY);
				return new PayQueryBusiness().doOnePayQuery(reqData,
						WXPayConfig.WAP_API_KEY);
			}else {
				reqData = new PayQueryReqData("", out_trade_no,
						WXPayConfig.HD_APP_ID, WXPayConfig.HD_MCH_ID,
						WXPayConfig.HD_API_KEY);
				return new PayQueryBusiness().doOnePayQuery(reqData,
						WXPayConfig.HD_API_KEY);
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
//		System.out.println(payQuery("1601131629120100", EbOrderSourceEnum.ANDROID));
		//1601110944113100
		//1601131629120100
		String code = "041608adbbb4a01edd755495902c1a3y";
//		getTicket();
//		String params = "appid="+WxOAuthConfig.APPID+"&secret="+WxOAuthConfig.APPSECRET+"&code="+code+"&grant_type=authorization_code";
//		JSONObject result = Util.getHttpClientResult(WxOAuthConfig.OAUTH_ACCESS_TOKEN_API,params);
//		String paramsAccess = "grant_type=client_credential&appid="+WxOAuthConfig.APPID+"&secret="+WxOAuthConfig.APPSECRET;
//		JSONObject accessInfo = Util.getHttpClientResult(WxOAuthConfig.ACCESS_TOKEN_API,paramsAccess);
//		
//		String openid = accessInfo.optString("openid");
//		System.out.println(openid);
//		PrepayidReqData reqData = new PrepayidReqData(WXPayConfig.WAP_APP_ID,
//				WXPayConfig.WAP_MCH_ID, "1604071355470100", "", "1604071355470100",
//				1, "", "", "", "",
//				WXPayConfig.NOTIFY_URL, WXPayConfig.WAP_API_KEY,openid,"JSAPI");
//		String prepayId =  new PrepayidBusiness().run(reqData, null,
//				WXPayConfig.WAP_API_KEY);
		
//		System.out.println(prepayId);
//		System.out.println(payQuery("1604121457440100", EbOrderSourceEnum.ANDROID, 1));
//		System.out.println(closeTrade("1601071121212", EbOrderSourceEnum.ANDROID,false));
//		getOpenIdByCode(code);
//		getJSAPITicket();
		System.out.println((SendGET("https://graph.qq.com/user/get_user_info?access_token=ECBC01054973EB02CDABF118FE028C77&expires_in=7776000&oauth_consumer_key=101171747&openid=C617138E899EF8BCD0B25AFE8F4237D2", "")));
	}
	
	public static String SendGET(String url, String param) {
		String result = "";// 访问返回结果
		BufferedReader read = null;// 读取访问结果

		try {
			// 创建url
			URL realurl = new URL(url + param);
			// 打开连接
			URLConnection connection = realurl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			read = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
			String line;// 循环读取
			while ((line = read.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read != null) {// 关闭流
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}
	
	/**
	* <p>功能描述:根据code获取openId</p>
	* <p>参数：@param code
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private static String getOpenIdByCode(String code){
		
		if(StringUtil.isNullOrEmpty(code)){
			return "";
		}
		String openParam = WXOAuthUtil.buildRequestCodeParam(code);
		//获取access_token
		String openJsonStr = SendGET(WXPayConfig.OAUTH_ACCESS_TOKEN_API, openParam);
		JSONObject result = null;
		String openId = "";
		try {
			result = new JSONObject(openJsonStr);
			if(result.has("openid")){
				openId = result.optString("openid");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return openId;
	}
	
	/**
	* <p>功能描述:获取</p>
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	private static String getJSAPITicket() {
		String tokenParam = "grant_type=client_credential&appid="
				+ WXPayConfig.WAP_APP_ID + "&secret=" + WXPayConfig.APP_SECRET;
		String tokenJsonStr = SendGET(WXPayConfig.ACCESS_TOKEN_API,
				tokenParam);
		try {
			JSONObject json = new JSONObject(tokenJsonStr);
			// 获取access_token
			if(!json.has("errcode")){
				
				String access_token = json.getString("access_token");
				String ticketParam = "access_token=" + access_token + "&type=jsapi";
				String ticketJsonStr = SendGET(WXPayConfig.TICKET_GETTICKET,
						ticketParam);
				JSONObject ticketJson = new JSONObject(ticketJsonStr);
				if(ticketJson.has("ticket")){
					return ticketJson.optString("ticket");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}
	
	/**
	* <p>功能描述:校验缓存里的ticket是否过期</p>
	* <p>参数：@return</p>
	* <p>返回类型：boolean true为过期，false为未过期</p>
	 */
	private static boolean isTicketValidate(){
		Map<String, String> ticketMap = WXPayCache.getInstance().getJSAPIticketMap();
		String time = ticketMap.get("time");
		if(StringUtil.isNullOrEmpty(time)){
			return true;
		}
		Long nowDate = new Date().getTime();
		//校验时效性
		if((nowDate - Long.parseLong(time)) < WXPayCache.getInstance().getLimitTime()){
			return false;
		}
		
		return true;
	}
	
}
