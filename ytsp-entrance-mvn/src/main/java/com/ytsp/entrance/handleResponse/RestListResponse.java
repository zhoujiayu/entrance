package com.ytsp.entrance.handleResponse;

import java.util.List;

import com.ytsp.entrance.service.exception.ServiceException;

public class RestListResponse<T> {
	private String retCode;
	private String retInfo;
	List<T> voList;

	public RestListResponse() {
	}

	public RestListResponse(String retCode) {
		super();
		this.retCode = retCode;
	}

	public RestListResponse(String retCode, String retInfo) {
		super();
		this.retCode = retCode;
		this.retInfo = retInfo;
	}

	public RestListResponse(int retCode) {
		super();
		this.retCode = String.valueOf(retCode);
	}

	public RestListResponse(int retCode, String retInfo) {
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

	public RestListResponse<T> parse(ServiceException se) {
		this.retCode = String.valueOf(se.getErrorCode());
		this.retInfo = se.getMessage();
		return this;
	}

	public List<T> getVoList() {
		return voList;
	}

	public void setVoList(List<T> voList) {
		this.voList = voList;
	}

}
