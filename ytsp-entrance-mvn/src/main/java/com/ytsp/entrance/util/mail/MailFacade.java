package com.ytsp.entrance.util.mail;


/**
 * 邮件发送的入口类 
 */
public class MailFacade {
	/**
	 * 发送邮件（简单文本） 
	 */
	public static boolean sendMail(String mailContent,String subject,String mailTo) {
		return MailServiceFactory.getMailSendService().sendMail(mailContent,subject, mailTo);
	}
	/**
	 * 发送附件
	 * **/
	public static boolean sendMail(String mailContent,String subject,String mailTo,String fileAffix) {
		return MailServiceFactory.getMailSendService().sendMail(mailContent,subject, mailTo,fileAffix);
	}
	
}
