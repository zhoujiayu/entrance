package com.ytsp.entrance.util.mail;

/**
 * @author zchang
 * @comment 邮件发送接口
 */
public interface IMailSendService {

	/**
	 * 发送邮件（简单文本） 
	 */
	public boolean sendMail(String mailContent,String subject,String mailTo);
	/**
	 * 发送附件文本
	 * */
	public boolean sendMail(String mailContent,String subject,String mailTo,String fileAffix);
}
