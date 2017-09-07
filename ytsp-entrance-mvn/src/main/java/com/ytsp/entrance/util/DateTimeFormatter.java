package com.ytsp.entrance.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFormatter {

	private static final DateFormat dt_formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String LOCK = "DateTimeFormatterClassLOCK";
	
	public static Date string2DateTime(String value) {
		Date date = null;
		if (value != null) {
			try {
				synchronized (LOCK) {
					date = dt_formater.parse(value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return date;
	}

	public static String dateTime2String(Date date) {
		if (date != null) {
			return dt_formater.format(date);
		}
		return "";
	}
}