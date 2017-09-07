package com.ytsp.entrance.util.mail.impl;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

import com.ytsp.entrance.util.mail.MailConfigResource;

public class MailSendHelper {
	private static Logger _log = Logger.getLogger(MailSendHelper.class);

	private MimeMessage mimeMsg; // MIME邮件对象

	private Session session; // 邮件会话对象
	private Properties props; // 系统属性
	private boolean needAuth = false; // smtp是否需要认证

	private String username = ""; // smtp认证用户名和密码
	private String password = "";

	private Multipart mp; // Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象

	public MailSendHelper() {
		setSmtpHost(MailConfigResource.getConfig(MailConfigResource.SMTP_HOST));// 如果没有指定邮件服务器,就从ConfigResource类中获取
		createMimeMessage();
	}

	public MailSendHelper(String smtp) {
		setSmtpHost(smtp);
		createMimeMessage();
	}

	public void setSmtpHost(String hostName) {
		_log.info("设置系统属性：mail.smtp.host = " + hostName);
		if (props == null)
			props = System.getProperties(); // 获得系统属性对象

		props.put("mail.smtp.host", hostName); // 设置SMTP主机
	}

	public boolean createMimeMessage() {
		try {
			_log.info("准备获取邮件会话对象！");
			session = Session.getDefaultInstance(props, null); // 获得邮件会话对象
		} catch (Exception e) {
			_log.error("获取邮件会话对象时发生错误！" + e);
			return false;
		}

		_log.info("准备创建MIME邮件对象！");
		try {
			mimeMsg = new MimeMessage(session); // 创建MIME邮件对象
			mp = new MimeMultipart();

			return true;
		} catch (Exception e) {
			_log.error("创建MIME邮件对象失败！" + e);
			return false;
		}
	}

	public void setNeedAuth(boolean need) {
		_log.info("设置smtp身份认证：mail.smtp.auth = " + need);
		if (props == null)
			props = System.getProperties();

		if (need) {
			props.put("mail.smtp.auth", "true");
		} else {
			props.put("mail.smtp.auth", "false");
		}
	}

	public void setNamePass(String name, String pass) {
		username = name;
		password = pass;
	}

	public boolean setSubject(String mailSubject) {
		_log.info("设置邮件主题！");
		try {
			mimeMsg.setSubject(mailSubject);
			return true;
		} catch (Exception e) {
			_log.error("设置邮件主题发生错误！");
			return false;
		}
	}

	public boolean setBody(String mailTemplate) {
		try {
			BodyPart bp = new MimeBodyPart();
			bp.setContent(mailTemplate, "text/html;charset=utf-8");
			mp.addBodyPart(bp);

			return true;
		} catch (Exception e) {
			System.out.println(e.toString());
			_log.error("设置邮件正文时发生错误！" + e);
			return false;
		}
	}

	public boolean addFileAffix(String filename) {

		_log.info("增加邮件附件：" + filename);
		try {
			BodyPart bp = null;
			FileDataSource fileds = null;
			File dir = new File(filename);
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				if(files == null || files.length == 0){
					return false;
				}
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					fileds = new FileDataSource(f.getPath());
					bp = new MimeBodyPart();
					bp.setDataHandler(new DataHandler(fileds));
					bp.setFileName(MimeUtility.encodeText(fileds.getName()));
					mp.addBodyPart(bp);
					_log.info("增加邮件附件：" + fileds.getName());
				}
			} else {
				fileds = new FileDataSource(filename);
				bp = new MimeBodyPart();
				bp.setDataHandler(new DataHandler(fileds));
				bp.setFileName(MimeUtility.encodeText(fileds.getName()));
				mp.addBodyPart(bp);
			}
			return true;
		} catch (Exception e) {
			_log.error("增加邮件附件：" + filename + "发生错误！" + e);
			return false;
		}
	}

	public boolean setFrom(String from) {
		_log.info("设置发信人！");
		try {
			mimeMsg.setFrom(new InternetAddress(from)); // 设置发信人
			return true;
		} catch (Exception e) {
			_log.error("设置邮件发信人时发生错误！" + e);
			return false;
		}
	}

	public boolean setTo(String to) {
		if (to == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			return true;
		} catch (Exception e) {
			_log.error("设置邮件收信人时发生错误！" + e);
			return false;
		}

	}

	public boolean setCopyTo(String copyto) {
		if (copyto == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.CC,
					(Address[]) InternetAddress.parse(copyto));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean sendout() {
		try {
			mimeMsg.setContent(mp);
			mimeMsg.saveChanges();
			_log.info("正在发送邮件....");

			Session mailSession = Session.getInstance(props, null);
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String) props.get("mail.smtp.host"), username,
					password);
			transport.sendMessage(mimeMsg,
					mimeMsg.getRecipients(Message.RecipientType.TO));
			// transport.send(mimeMsg);

			_log.info("发送邮件成功！");
			transport.close();

			return true;
		} catch (Exception e) {
			_log.error("邮件发送失败！" + e);
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {

		String subject = "“爱看”用户密码找回";
		String mailbody = "“爱看”测试正文";
		String to = "guoyistone@163.com";
		String from = MailConfigResource
				.getConfig(MailConfigResource.MAIL_FROM);
		MailSendHelper themail = new MailSendHelper(
				MailConfigResource.getConfig(MailConfigResource.SMTP_HOST));
		themail.setNeedAuth(true);
		themail.setNamePass(
				MailConfigResource.getConfig(MailConfigResource.MAIL_USER),
				MailConfigResource.getConfig(MailConfigResource.MAIL_PSW));
		if (themail.setSubject(subject) == false)
			return;
		if (themail.setBody(mailbody) == false)
			return;
		if (themail.setTo(to) == false)
			return;
		if (themail.setFrom(from) == false)
			return;
		if (themail.addFileAffix("c:\\TestSendEmail") == false)
			return;
		if (themail.sendout() == false)
			return;
	}
}