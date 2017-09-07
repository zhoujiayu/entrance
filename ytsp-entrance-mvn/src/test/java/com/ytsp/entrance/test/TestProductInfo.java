package com.ytsp.entrance.test;

import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.entrance.command.base.HeadInfo;

public class TestProductInfo {

	private static int COMMANDCODE = 4007;

	public static void main(String[] args) {

		try {
			HeadInfo head = getHeadInfo();
			JSONObject bodyJson = getBodyInfo();
			SendPostRequst.sendPostRequest(head, bodyJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HeadInfo getHeadInfo() {
		HeadInfo head = new HeadInfo();
		head.setCommandCode(COMMANDCODE);
		head.setUid(10002182);
		head.setPlatform("ipad");
		return head;
	}

	private static JSONObject getBodyInfo() throws JSONException {
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("productCode", 3001367);
		return bodyJson;
	}

}
