package com.tencent.wxpay.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;

import com.tencent.wxpay.common.Signature;

public class PayNotifyHandler {
	private HttpServletRequest request;
	private HttpServletResponse response;
	/** 应答的参数 */
	private Map<String, Object> parameters;
	/** 密钥 */
	private String appKey;

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public PayNotifyHandler(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		super();
		this.request = request;
		this.response = response;
		this.parameters = new HashedMap();
		this.appKey = "";
		ServletInputStream inputStream = request.getInputStream();
		Map m = this.request.getParameterMap();
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			String k = (String) it.next();
			String v = ((String[]) m.get(k))[0];
			this.setParameter(k, v);
		}
	}

	/**
	 * 设置参数值
	 * 
	 * @param parameter
	 *            参数名称
	 * @param parameterValue
	 *            参数值
	 */
	public void setParameter(String parameter, String parameterValue) {
		String v = "";
		if (null != parameterValue) {
			v = parameterValue.trim();
		}
		this.parameters.put(parameter, v);
	}

	/**
	 * 是否财付通签名,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 * 
	 * @return boolean
	 */
	public boolean isTenpaySign() {
		// 算出摘要
		String sign = Signature.getSign(parameters, appKey);
		String tenpaySign = request.getParameter("sign").toUpperCase();
		return tenpaySign.equals(sign);
	}
}
