package com.tencent.wxpay.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.xwork.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.ytsp.common.util.StringUtil;

/**
 * @description
 */
public class WXUtil {
	// 打log用
	private static WXLog logger = new WXLog(
			LoggerFactory.getLogger(WXUtil.class));

	/**
	 * 通过反射的方式遍历对象的属性和属性值，方便调试
	 * 
	 * @param o
	 *            要遍历的对象
	 * @throws Exception
	 */
	public static void reflect(Object o) throws Exception {
		Class cls = o.getClass();
		Field[] fields = cls.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			f.setAccessible(true);
		}
	}

	/**
	 * 从输入流读取数据到字节数组
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInput(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len = 0;
		byte[] buffer = new byte[1024];
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		out.close();
		in.close();
		return out.toByteArray();
	}

	/**
	 * 从输入流读取数据到字符串
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}

	/**
	 * 将字符串放进输入流
	 * 
	 * @param sInputString
	 * @return
	 */
	public static InputStream getStringStream(String sInputString) {
		ByteArrayInputStream tInputStringStream = null;
		if (sInputString != null && !sInputString.trim().equals("")) {
			tInputStringStream = new ByteArrayInputStream(
					sInputString.getBytes());
		}
		return tInputStringStream;
	}

	/**
	 * 将xml数据转换成实体类
	 * 
	 * @param xml
	 * @param tClass
	 * @return
	 */
	public static Object getObjectFromXML(String xml, Class tClass) {
		// 将从API返回的XML数据映射到Java对象
		XStream xStreamForResponseData = new XStream();
		xStreamForResponseData.alias("xml", tClass);
		xStreamForResponseData.ignoreUnknownElements();// 暂时忽略掉一些新增的字段
		return xStreamForResponseData.fromXML(xml);
	}

	/**
	 * 获取map中的一个数据
	 * 
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getStringFromMap(Map<String, Object> map, String key,
			String defaultValue) {
		if (key == null || key.length() == 0) {
			return defaultValue;
		}
		String result = (String) map.get(key);
		if (result == null) {
			return defaultValue;
		} else {
			return result;
		}
	}

	/**
	 * 获取map中的一个数据
	 * 
	 * @param map
	 * @param key
	 * @return
	 */
	public static int getIntFromMap(Map<String, Object> map, String key) {
		if (key == null || key.length() == 0) {
			return 0;
		}
		if (map.get(key) == null) {
			return 0;
		}
		return Integer.parseInt((String) map.get(key));
	}

	/**
	 * 读取本地的xml数据，一般用来自测用
	 * 
	 * @param localPath
	 *            本地xml文件路径
	 * @return 读到的xml字符串
	 */
	public static String getLocalXMLString(String localPath) throws IOException {
		return WXUtil.inputStreamToString(WXUtil.class
				.getResourceAsStream(localPath));
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getJSAPISignValue(String ticket,String noncestr,String timestamp,String url){
		String signValue = "jsapi_ticket="+ticket+"&noncestr="+noncestr+"&timestamp="+timestamp+"&url="+url;
		return signValue;
	}
	
	/**
	 * 打log接口
	 * 
	 * @param log
	 *            要打印的log字符串
	 * @return 返回log
	 */
	public static String log(Object log) {
		logger.i(log.toString());
		// System.out.println(log);
		return log.toString();
	}
	
	/**
	* <p>功能描述:根据微信code，获取accessToken,该code只能使用1次，若再次使用会返回错误码</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	 */
	public static JSONObject getWxAccessToken(String code) throws JSONException{
		if(StringUtil.isNullOrEmpty(code)){
			return null;
		}
		String params = "appid=" + WXPayConfig.WAP_APP_ID + "&secret="
				+ WXPayConfig.APP_SECRET + "&code="
				+ StringUtils.trim(code)
				+ "&grant_type=authorization_code";
		JSONObject result = getHttpClientResult(
				WXPayConfig.OAUTH_ACCESS_TOKEN_API, params);
		
		return result;
	}

	/**
	* <p>功能描述:获取微信普通accessToken</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	 */
	public static JSONObject getWxNormalAccessToken() throws JSONException{
		String paramsAccess = "grant_type=client_credential&appid="
				+ WXPayConfig.WAP_APP_ID + "&secret="
				+ WXPayConfig.APP_SECRET;
		JSONObject accessInfo = getHttpClientResult(
				WXPayConfig.ACCESS_TOKEN_API, paramsAccess);
		
		return accessInfo;
	}
	
	/**
	* <p>功能描述:根据accessToken和openId获取微信用户信息</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	 */
	public static JSONObject getWxUserInfo(String accessToken,String openId) throws JSONException{
		if(StringUtil.isNullOrEmpty(accessToken) || StringUtil.isNullOrEmpty(openId)){
			return null;
		}
		String paramsUserInfo = "access_token="
				+ accessToken + "&openid="
				+ openId + "&lang=zh_CN";
		JSONObject userInfo = getHttpClientResult(WXPayConfig.USER_INFO_API,
				paramsUserInfo);
		
		return userInfo;
	}
	
	/**
	* <p>功能描述:根据code获取微信用户信息</p>
	* <p>参数：@param code
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	 */
	public static JSONObject getWxUserInfo(String code) throws JSONException{
		if(StringUtil.isNullOrEmpty(code)){
			return null;
		}
		//获取accessToken
		JSONObject result =  getWxAccessToken(code);
		JSONObject userInfo = null;
		if (result != null) {
			if (!result.has("errcode")) {
				//1、获取普通access_token 2、获取成功 根据openid 和 accesstoken 获取用户基本信息
				JSONObject accessInfo = getWxNormalAccessToken();
				if (accessInfo != null && !accessInfo.has("errcode")) {
					String accessToken = "";
					String openId = "";
					if(accessInfo.has("access_token")){
						accessToken = accessInfo.optString("access_token");
					}
					if(accessInfo.has("openid")){
						openId = accessInfo.optString("openid");
					}
					userInfo = getWxUserInfo(accessToken, openId);
				}else{
					System.out.println("获取普通access_token错误 :code: " + code);
				}
			} else {
				System.out.println("获取access_token错误 :code: " + code);
			}
		}
		
		return userInfo;
	}
	
	/**
	* <p>功能描述:http请求调用</p>
	* <p>参数：@param url
	* <p>参数：@param params
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	 */
	private static JSONObject getHttpClientResult(String url,String params) throws JSONException{
		JSONObject result = null;
		StringBuffer response = new StringBuffer();
		HttpClient client = new HttpClient();
		try {
			URL website = new URL(url+params);
			InputStream in = website.openStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			if(!"".equals(response.toString())){
				result = new JSONObject(response.toString());
			}
			
		} catch (IOException e) {
			
		} finally {
			
		}
		return result;
	}
}
