package com.ytsp.entrance.test;

import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.entrance.command.base.HeadInfo;

public class TestH5Update {

	private static int COMMANDCODE = 5113;

	public static void main(String[] args) {

		try {
			HeadInfo head = getHeadInfo();
			JSONObject bodyJson = getBodyInfo();
			//{"RESPONSE_CODE_INFO":"获取更新信息成功！","RESPONSE_BODY":{"h5Version":1,"md5Code":"abbc2f2b60178143530e672e542111c1","downloadUrl":"http://172.16.168.11/Web_app.zip","version":"5.0.2","size":681395},"RESPONSE_CODE":200}
			SendPostRequst.sendPostRequest(head, bodyJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HeadInfo getHeadInfo() {
		HeadInfo head = new HeadInfo();
		head.setCommandCode(COMMANDCODE);
		head.setUid(10002182);
		head.setPlatform("iphone");
		return head;
	}

	private static JSONObject getBodyInfo() throws JSONException {
		JSONObject bodyJson = new JSONObject();
		bodyJson.put("h5Version", 1);
		return bodyJson;
	}

}
