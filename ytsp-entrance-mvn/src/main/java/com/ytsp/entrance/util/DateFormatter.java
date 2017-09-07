package com.ytsp.entrance.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateFormatter {

	private static final DateFormat y_formater = new SimpleDateFormat("yyyy");
	private static final DateFormat d_formater = new SimpleDateFormat("yyyy-MM-dd");
	private static final String LOCK = "DateFormatterClassLOCK";
	
	public static Date string2Date(String value) throws Exception {
		Date date = null;
		if (value != null&&!value.trim().equals("")) {
			synchronized (LOCK) {
				try {
					date = d_formater.parse(value);
				} catch (Exception e) {
					Logger.getLogger(DateFormatter.class).error("Parse date error");
					throw e;
				}
			}
		}
		return date;
	}
	
	public static Date string2Date(String value, String format) {
		Date date = null;
		if (value != null) {
			try {
				synchronized (LOCK) {
					SimpleDateFormat df = new SimpleDateFormat(format);
					date = df.parse(value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return date;
	}

	public static String date2String(Date date, String format) {
	    SimpleDateFormat df = new SimpleDateFormat(format);
	    return df.format(date);
	}

	public static String date2String(Date date) {
		if (date != null) {
			return d_formater.format(date);
		}
		return "";
	}
	
	public static String date2YearString(Date date) {
		if (date != null) {
			synchronized (LOCK) {
				return y_formater.format(date);
			}
		}
		return "";
	}
}