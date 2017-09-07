package com.ytsp.entrance.util.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class MailConfigResource {

	public static final String SMTP_HOST = "smtp_host";
	public static final String MAIL_FROM = "mail_from";
	public static final String MAIL_USER = "mail_user";
	public static final String MAIL_PSW = "mail_psw";
	public static final String HOST = "host";
	public static final String FORGET_PWD = "forget_password";
	public static final String FORGET_PWD_PARENT = "forget_password_parent";
	public static final String BIND_EMAIL = "bind_email";
	public static final String INSUFFICIENT_BALANCE = "insufficient_balance";

	private static Logger _log = Logger.getLogger(MailConfigResource.class);
	private static String configFile = "mail";
	private static Map<String, String> configs = new HashMap<String, String>(0);

	public static String getConfig(String key) {
		if (configs.size() <= 0)
			loadConfig();
		String value = configs.get(key);
		if (value == null)
			value = "";
		return value;
	}

	private static synchronized void loadConfig() {
		initConfig(configFile + ".properties");
	}

	private static void initConfig(String name) {
		InputStream is = null;
		Properties props = new Properties();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			classLoader = MailConfigResource.class.getClassLoader();
		}
		is = classLoader.getResourceAsStream(name);
		if (is != null) {
			try {
				props.load(is);

			} catch (IOException e) {
				_log.error("loadConfig()", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					_log.error("loadConfig()", e);
				}
			}
		}
		if (props.size() < 1) {
			return;
		}
		synchronized (configs) {
			Iterator names = props.keySet().iterator();
			while (names.hasNext()) {
				String key = (String) names.next();
				configs.put(key, props.getProperty(key));
			}
		}
	}
}
