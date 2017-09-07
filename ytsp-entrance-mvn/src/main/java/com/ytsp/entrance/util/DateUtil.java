/* 
 * $Id: DateUtil.java 1275 2011-09-26 06:47:28Z jeff $ * 
 * All rights reserved 
 */

package com.ytsp.entrance.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	private DateUtil() {
	}

	public static Date addByDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, days);
		return c.getTime();
	}

	public static Date addByMonths(Date date, int months) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, months);
		return c.getTime();
	}

	public static Integer getAge(Date birthDay) throws Exception {
		Calendar calNow = Calendar.getInstance();
		Calendar calBirth = Calendar.getInstance();
		calBirth.setTime(birthDay);
//		if (calNow.before(calBirth)) {
//			throw new IllegalArgumentException(
//					"The birthDay is before Now.It's unbelievable!");
//		}

		int yearNow = calNow.get(Calendar.YEAR);
		int monthNow = calNow.get(Calendar.MONTH);
		int dayOfMonthNow = calNow.get(Calendar.DAY_OF_MONTH);

		int yearBirth = calBirth.get(Calendar.YEAR);
		int monthBirth = calBirth.get(Calendar.MONTH);
		int dayOfMonthBirth = calBirth.get(Calendar.DAY_OF_MONTH);

		int age = yearNow - yearBirth;

		if (monthNow <= monthBirth) {
			if (monthNow == monthBirth) {
				if (dayOfMonthNow < dayOfMonthBirth) {
					age--;
				}
			} else {
				age--;
			}
		}
		return age;
	}

	// -- 从现在到结束时间的时间
	public static String remainTimeNowToEndTime(Date endTime) {
		return remainTime(new java.util.Date(), endTime);
	}

	// -- 从结束时间到现在的时间
	public static String remainTimeEndTimeToNow(Date endTime) {
		return remainTime2(endTime, new java.util.Date());
	}

	// -- 两个时间比较,剩余时间
	public static String remainTime(Date startTime, Date endTime) {
		StringBuffer remainTimeStr = new StringBuffer();
		if (startTime != null && endTime != null) {
			long between = (endTime.getTime() - startTime.getTime()) / 1000;
			if (between > 0) {
				long days = between / (24 * 60 * 60);
				long hour = between % (24 * 60 * 60) / 3600;
				long minute = between % (60 * 60) / 60;
				long second = between % 60;
				if (days > 0) {
					remainTimeStr.append(days + "天");
				}
				if (hour > 0) {
					remainTimeStr.append(hour + "小时");
				}
				if (minute > 0) {
					remainTimeStr.append(minute + "分");
				}
				if (second > 0) {
					remainTimeStr.append(second + "秒");
				}
			} else {
				remainTimeStr.append("已经停止");
			}
		} else {
			remainTimeStr.append("已经停止");
		}
		return remainTimeStr.toString();
	}

	// -- 两个时间比较,剩余时间
	public static String remainTime2(Date startTime, Date endTime) {
		long between = (endTime.getTime() - startTime.getTime()) / 1000;
		if (between > 0) {
			long days = between / (24 * 60 * 60);
			long hour = between % (24 * 60 * 60) / 3600;
			long minute = between % (60 * 60) / 60;
			if (days > 0) {
				return (days + "天前");
			}
			if (hour > 0) {
				return (hour + "小时前");
			}
			if (minute > 0) {
				return (minute + "分钟前");
			}
		}
		return "";
	}

	public static Calendar getYesterday(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar;
	}
	
	public static void main(String[] args) {
		// Date birthDay = DateFormatter.string2Date("2013-01-01");
		// try {
		// System.out.println(DateUtil.getAge(birthDay));
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.out.println("The birthDay is before Now.It's unbelievable!");
		// }
		
//		Date date = DateFormatter.string2Date("2012-04-26 19:43","yyyy-MM-dd hh:mm");
//		System.out.println(DateUtil.remainTimeEndTimeToNow(date));
		Calendar cal = Calendar.getInstance();
		cal = DateUtil.getYesterday(cal.getTime());
		System.out.println(cal.get(Calendar.YEAR) + " " + (cal.get(Calendar.MONTH) + 1) + " " + (cal.get(Calendar.DAY_OF_MONTH)));
	}
}
