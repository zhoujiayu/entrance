package com.ytsp.entrance.test;

import org.json.JSONObject;

import com.followcode.utils.json.tojosn.copy.EntityConversionJSON;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.util.WebClient;

public class TestUtil {
	static String SERVER_INTERFACE = "";

	public static void postCmd(HeadInfo head, JSONObject body) throws Exception {
		JSONObject jObject = new JSONObject();
		EntityConversionJSON.entityToJSON(head, jObject);
		byte[] rspByte = WebClient.getWebContentByPost(SERVER_INTERFACE,
				body.toString(), jObject.toString());
		String rspString = new String(rspByte);
		// 整体json数据
		JSONObject totalJsonObj = new JSONObject(rspString);
		System.out.println(totalJsonObj.toString());
	}
}
