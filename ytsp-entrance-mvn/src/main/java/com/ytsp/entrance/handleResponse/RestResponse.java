package com.ytsp.entrance.handleResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.entrance.errorcode.ErrorCode;
import com.ytsp.entrance.service.exception.ServiceException;


public class RestResponse<T>{
	
	private String retCode;
	private String retInfo;


	T vo;
	
	/**
	* <p>功能描述:将结果转换成JSONObject</p>
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：JSONObject</p>
	 */
	public JSONObject convertJSONObject() throws JSONException{
		Gson gson = new Gson();
		return new JSONObject(gson.toJson(this));
	}
	
	public RestResponse() {
		this.retCode = ErrorCode.RESP_CODE_OK;
		this.retInfo = ErrorCode.RESP_INFO_OK;
	}
	
	public RestResponse(String retCode) {
		super();
		this.retCode = retCode;
	}
	
	public RestResponse(String retCode, String retInfo) {
		super();
		this.retCode = retCode;
		this.retInfo = retInfo;
	}
	
	public RestResponse(int retCode) {
		super();
		this.retCode = String.valueOf(retCode);
	}
	
	public RestResponse(int retCode, String retInfo) {
		super();
		this.retCode = String.valueOf(retCode);
		this.retInfo = retInfo;
	}

	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public String getRetInfo() {
		return retInfo;
	}

	public void setRetInfo(String retInfo) {
		this.retInfo = retInfo;
	}
	
	public void save(String retCode, String retInfo) {
		this.retCode = retCode;
		this.retInfo = retInfo;
	}
	
	public void save(int retCode, String retInfo) {
		this.retCode = String.valueOf(retCode);
		this.retInfo = retInfo;
	}
	
	public RestResponse<T> parse(ServiceException se) {
		this.retCode = String.valueOf(se.getErrorCode());
		this.retInfo = se.getMessage();
		return this;
	}

	public T getVo() {
		return vo;
	}

	public void setVo(T vo) {
		this.vo = vo;
	}


}
