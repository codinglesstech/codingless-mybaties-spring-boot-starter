package tech.codingless.core.plugs.mybaties3.util;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class MybatiesDateUtil {
	private static SimpleDateFormat defaultFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat formater1 = new SimpleDateFormat("yyyy/MM/dd");
	private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static SimpleDateFormat dateTimeFormater = new SimpleDateFormat("yyyyMMddHHmmss");
	private static String DATETIME_REGEX = "^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$";
	private final static int MINITE = 1000 * 60;
	private final static int HOUR = MINITE * 60;
	private final static int DAY = HOUR * 24;

	public static String format(Date date) {

		return defaultFormater.format(date);
	}

	public static String formatDate(Date date) {
		return formater1.format(date);
	}

	public static int days(Date from, Date to) {
		if (from.getTime() > to.getTime()) {
			return 0;
		}
		int day = (int) ((to.getTime() - from.getTime()) / DAY);
		// if (day == 0 && from.getDate() != to.getDate()) {
		// day = 1;
		// }
		// 如果天数不在同
		if (((to.getTime() - from.getTime()) % DAY > 10000)) {
			return day + 1;
		}
		return day;
	}

	public static int hours(Time from, Time to) {
		return (int) ((to.getTime() - from.getTime()) / HOUR);
	}

 
	public static Date getMonday(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		int tmp = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 2 - (tmp == 1 ? 7 : 0));
		return cal.getTime();
	}

	public static Date parseUTC(String datestr) {
		try { 
			return UTC.parse(datestr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	 
	public static Date parse(String source) {
		try {
			if (MybatiesStringUtil.isEmpty(source) || !source.matches(DATETIME_REGEX)) {
				return null;
			}
			return defaultFormater.parse(source);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String formatDateTime(Date date) {
		return dateTimeFormater.format(date);
	}

	public static String formatYYYYMMDD(Date date) {
		return yyyyMMdd.format(date);
	}

	 
	public static String formatDate(Date dateValue, String formatPattern) {
		try {
			if (dateValue == null) {
				return null;
			}
			SimpleDateFormat formater = new SimpleDateFormat(formatPattern);
			return formater.format(dateValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	 
	public static Date parseDate(String dateValue, String formatPattern) {
		try {
			if (MybatiesStringUtil.isEmpty(dateValue) || !dateValue.matches(DATETIME_REGEX)) {
				return null;
			}
			SimpleDateFormat formater = new SimpleDateFormat(formatPattern);
			return formater.parse(dateValue);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

 
	public static Date utcToDate(String utcTimeStr) {
		if (MybatiesStringUtil.isEmpty(utcTimeStr)) {
			MybatiesAssertUtil.assertNotEmpty(utcTimeStr, "UTC_TIME_STR_EMPTY");
		}
		if (utcTimeStr.endsWith("Z") || utcTimeStr.length() == 19) {
			return new Date(LocalDateTime.parse(utcTimeStr.substring(0, 19)).toInstant(ZoneOffset.of("Z")).toEpochMilli());
		}
		String offsetStr = utcTimeStr.substring(19);
		return new Date(LocalDateTime.parse(utcTimeStr.substring(0, 19)).toInstant(ZoneOffset.of(offsetStr)).toEpochMilli());
	}

 
	public static String toZeroUtcStr(Date date) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.of("Z"));
		return ldt.toString();
	}

	public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {

		try {
			GregorianCalendar s = new GregorianCalendar();
			s.setTime(date);
			XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
			return calendar;
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
