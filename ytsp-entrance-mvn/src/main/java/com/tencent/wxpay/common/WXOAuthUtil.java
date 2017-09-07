package com.tencent.wxpay.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class WXOAuthUtil {
	
	
	/**
	* <p>功能描述:构建获取openId请求参数</p>
	* <p>参数：@param code
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String buildRequestCodeParam(String code){
		String openParam = "appid=" + WXPayConfig.WAP_APP_ID + "&secret="
				+ WXPayConfig.APP_SECRET + "&code=" + code
				+ "&grant_type=authorization_code";
		return openParam;
	}
	
	
	public static JSONObject  getHttpClientResult(String url,String params) throws JSONException{
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
	
	public static void main(String[] args) {
		
	}
}
