package com.ytsp.entrance.util.mail.impl;

import com.ytsp.entrance.util.mail.IMailSendService;
import com.ytsp.entrance.util.mail.MailConfigResource;

public class MailSendServiceImpl implements IMailSendService {
	public boolean sendMail(String mailContent, String subject, String mailTo) {

		MailSendHelper themail = new MailSendHelper(
				MailConfigResource.getConfig(MailConfigResource.SMTP_HOST));
		themail.setNeedAuth(true);
		themail.setNamePass(
				MailConfigResource.getConfig(MailConfigResource.MAIL_USER),
				MailConfigResource.getConfig(MailConfigResource.MAIL_PSW));
		// 设置模板
		try {
			if (themail.setSubject(subject) == false)
				return false;
			if (themail.setBody(mailContent) == false)
				return false;
			if (themail.setTo(mailTo) == false)
				return false;
			if (themail.setFrom(MailConfigResource
					.getConfig(MailConfigResource.MAIL_FROM)) == false)
				return false;
			if (themail.sendout() == false)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 向用户发送所需要的附件
	 * 
	 * @param fileAffix
	 *            附件在文件系统中的物理地址（可能是文件夹或文件）
	 * */
	public boolean sendMail(String mailContent, String subject, String mailTo,
			String fileAffix) {
		MailSendHelper themail = new MailSendHelper(
				MailConfigResource.getConfig(MailConfigResource.SMTP_HOST));
		themail.setNeedAuth(true);
		themail.setNamePass(
				MailConfigResource.getConfig(MailConfigResource.MAIL_USER),
				MailConfigResource.getConfig(MailConfigResource.MAIL_PSW));
		try {
			if (themail.setSubject(subject) == false)
				return false;
			if (themail.setBody(mailContent) == false)
				return false;
			if (themail.addFileAffix(fileAffix) == false)
				return false;
			if (themail.setTo(mailTo) == false)
				return false;
			if (themail.setFrom(MailConfigResource
					.getConfig(MailConfigResource.MAIL_FROM)) == false)
				return false;
			if (themail.sendout() == false)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
