package com.ytsp.entrance.util.mail;

import java.io.IOException;

import com.ytsp.db.domain.Customer;

/**
 * 邮件内容服务
 * */
public interface MailContentService {
	/**
	 * 获取客户发送邮件内容（html文件）
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public String readCustomerHtmlContent(String contentType, Customer customer,String forgetPasswordCode)
			throws IOException, Exception;

	/**
	 * 获取家长发送邮件内容（html文件）
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public String readParentHtmlContent(String contentType, Customer customer)
			throws IOException, Exception;

	/**
	 * 获取发送邮件内容（html文件）
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public String readHtmlContent(String contentType) throws IOException,
			Exception;

		/**
	 * 读取网页模版
	 * */
	public String readBindEmailHtmlContent(String contentType,
			String customer, String emailLink) throws Exception ;
}
