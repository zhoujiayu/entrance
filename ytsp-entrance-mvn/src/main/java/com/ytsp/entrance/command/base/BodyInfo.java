package com.ytsp.entrance.command.base;

import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;

public class BodyInfo {

	private JSONObject body;

	public BodyInfo() {
	}

	public BodyInfo(String json) throws Exception {
		if(StringUtil.isNullOrEmpty(json)){
			body = new JSONObject();
		}else{
			body = new JSONObject(json);
		}
	}

	public JSONObject getBodyObject() {
		return body;
	}

	public void setBody(JSONObject body) {
		this.body = body;
	}

}
