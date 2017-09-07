package com.ytsp.entrance.util.mail.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.ytsp.db.domain.Customer;
import com.ytsp.entrance.util.mail.MailConfigResource;
import com.ytsp.entrance.util.mail.MailContentService;

/**
 * 邮件内容服务实现
 * */
public class MailContentServiceImpl implements MailContentService {
	private static Logger _log = Logger.getLogger(MailContentServiceImpl.class);
	/**
	 * 读取网页模版
	 * */
	public String readCustomerHtmlContent(String contentType,
			Customer customer, String forgetPasswordCode) throws Exception {
		StringBuffer msgContent = getMailContent(contentType);
		return replaceKeysCustomer(contentType, msgContent.toString(),
				customer, forgetPasswordCode);
	}

	/**
	 * 
	 * */
	public String readParentHtmlContent(String contentType, Customer customer)
			throws IOException, Exception {
		StringBuffer msgContent = getMailContent(contentType);
		return replaceKeysParent(contentType, msgContent.toString(), customer);
	}

	/**
	 * 
	 * */
	public String readHtmlContent(String contentType) throws Exception {
		StringBuffer msgContent = getMailContent(contentType);
		return msgContent.toString();
	}

	private StringBuffer getMailContent(String contentType) throws IOException {
		BufferedReader bufread = null;
		String mailContent = MailConfigResource.getConfig(contentType);
		try {
			String path = this.getClass().getResource("").getPath();
			_log.info(path);
			path = path.substring(0, path.indexOf("/WEB-INF/classes"));
			_log.info(path);
			path+=mailContent;
			_log.info(path);
			// 从资源文件中获取相应的模版信息
			bufread = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), "UTF-8"));
			String temp = null;
			StringBuffer msgContent = new StringBuffer();

			while ((temp = bufread.readLine()) != null) {
				msgContent.append(temp);
			}
			return msgContent;
		} catch (FileNotFoundException e) {
			System.out.println("找不到邮件模版！");
		} finally {
			if (bufread != null)
				bufread.close();
		}
		return null;
	}

	public static String replaceKeysCustomer(String contentType,
			String msgContent, Customer customer, String forgetPasswordCode)
			throws Exception {
		msgContent = replaceAll(msgContent, "${username}",
				customer.getAccount());
		msgContent = replaceAll(msgContent, "${forgetPasswordCode}",
				forgetPasswordCode);
		return msgContent;
	}

	public static String replaceKeysParent(String contentType,
			String msgContent, Customer customer) throws Exception {
		msgContent = replaceAll(msgContent, "${username}",
				customer.getAccount());
		msgContent = replaceAll(msgContent, "${password}", "");
		return msgContent;
	}

	public static String replaceKeysActivity(String contentType,
			String msgContent, Customer customer) throws Exception {
		msgContent = replaceAll(msgContent, "${username}",
				customer.getAccount());
		msgContent = replaceAll(msgContent, "${code}",
				String.valueOf(customer.getId()));
		return msgContent;
	}

	private static String replaceAll(String str1, String str2, String str3) {

		StringBuffer strBuf = new StringBuffer(str1);
		int index = 0;
		while (str1.indexOf(str2, index) != -1) {
			index = str1.indexOf(str2, index);
			strBuf.replace(str1.indexOf(str2, index), str1.indexOf(str2, index)
					+ str2.length(), str3);
			index = index + str3.length();
			str1 = strBuf.toString();
		}
		return strBuf.toString();
	}

	@Override
	/**
	 * 读取网页模版
	 * */
	public String readBindEmailHtmlContent(String contentType,
			String customer, String emailLink) throws Exception {
		StringBuffer msgContent = getMailContent(contentType);
		return replaceKeysBindEmail(contentType, msgContent.toString(),
				customer, emailLink);
	}
	
	public static String replaceKeysBindEmail(String contentType,
			String msgContent, String customer, String emailLink)
					throws Exception {
		msgContent = replaceAll(msgContent, "${username}",
				customer);
		msgContent = replaceAll(msgContent, "${emailLink}",
				emailLink);
		return msgContent;
	}

}
