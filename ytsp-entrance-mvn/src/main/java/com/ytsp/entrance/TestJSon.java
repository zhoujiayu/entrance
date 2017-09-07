package com.ytsp.entrance;

import org.json.JSONException;
import org.json.JSONObject;

public class TestJSon {

	/**
	 * @param args
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws JSONException {
		JSONObject obj =  new JSONObject("{commandCode:903;timestamp:20130624131036;rd:6803;sig:MD5;ver:1.0;version:3.0;platform:ipad;vpn:iPad;screenWidth:768;screenHeight:1024;uniqueId:93f8ab75f8b41708e9f6471cf88a0041201286b6;otherInfo:APP_IPAD;uid:0;ip:118.75.241.68;appDiv:IKAN}");
		System.err.println(obj);
	}

}
