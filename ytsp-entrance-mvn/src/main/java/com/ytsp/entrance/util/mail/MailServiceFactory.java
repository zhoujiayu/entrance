package com.ytsp.entrance.util.mail;

import com.ytsp.entrance.util.mail.impl.MailContentServiceImpl;
import com.ytsp.entrance.util.mail.impl.MailSendServiceImpl;

/**
 * 邮件服务工厂
 * */
public class MailServiceFactory {
	private static IMailSendService mailSendService = new MailSendServiceImpl();

	public static IMailSendService getMailSendService() {
		return mailSendService;
	}

	private static MailContentService mailContentService = new MailContentServiceImpl();

	public static MailContentService getMailContentService() {
		return mailContentService;
	}
}
