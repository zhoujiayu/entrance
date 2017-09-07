package com.tencent.wxpay.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import com.tencent.wxpay.common.HttpsRequest;

/**
 * User: rizenguo Date: 2014/12/10 Time: 15:44 服务的基类
 */
public class BaseService {

	// API的地址
	private String apiURL;
	// 发请求的HTTPS请求器
	private IServiceRequest serviceRequest;

	public BaseService(String api) {
		apiURL = api;
		serviceRequest = new HttpsRequest();
	}

	protected String sendPost(Object xmlObj) {
		return serviceRequest.sendPost(apiURL, xmlObj);
	}

	/**
	 * 供商户想自定义自己的HTTP请求器用
	 * 
	 * @param request
	 *            实现了IserviceRequest接口的HttpsRequest
	 */
	public void setServiceRequest(IServiceRequest request) {
		serviceRequest = request;
	}
}
