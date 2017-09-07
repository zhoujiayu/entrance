package com.tencent.wxpay.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * User: rizenguo Date: 2014/12/10 Time: 15:16 这里定义服务层需要请求器标准接口
 */
public interface IServiceRequest {

	/**
	 * 通过Https往API post xml数据
	 *
	 * @param url
	 *            API地址
	 * @param xmlObj
	 *            要提交的XML数据对象
	 * @return API回包的实际数据
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 */
	public String sendPost(String url, Object xmlObj);

}
