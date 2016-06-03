/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtilities {
	private static SimpleDateFormat[] dateFormats;
	private static final long millisecondsInADay = 86400000L;
	private static DateFormat dfMDY = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
	private static DateFormat dfMY = new SimpleDateFormat("MMM yyyy");
	private static DateFormat dfY = new SimpleDateFormat("yyyy");

	private static SimpleDateFormat[] getFormats() {
		SimpleDateFormat[] format = { new SimpleDateFormat("MMMM dd, yyyy"), new SimpleDateFormat("MMM d, yyyy"),
				new SimpleDateFormat("dd MMM yyyy"), new SimpleDateFormat("dd MMMM yyyy"),
				new SimpleDateFormat("M/d/yy"), new SimpleDateFormat("M/d/yyyy"), new SimpleDateFormat("dd.MM.yyyy"),
				new SimpleDateFormat("MMM-yyyy"), new SimpleDateFormat("MMM-yy"), new SimpleDateFormat("yyyy-M-d"),
				new SimpleDateFormat("M-d-yyyy"), new SimpleDateFormat("M-d-yy"), new SimpleDateFormat("yyyy-MMM"),
				new SimpleDateFormat("yyyy-M-d H:m:s"), new SimpleDateFormat("M-d-yy HH:mm"),
				new SimpleDateFormat("M-d-yyyy HH:mm"), new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"),
				new SimpleDateFormat("M/d/yy"), new SimpleDateFormat("M/d/yyyy"),
				new SimpleDateFormat("MMMM d, yyyyy"), new SimpleDateFormat("MMM d, yyyyy"),
				new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"), new SimpleDateFormat("MMM yyyy"),
				new SimpleDateFormat("MMMM yyyy"), new SimpleDateFormat("ddMMMyy"), new SimpleDateFormat("MMMM yyyy") };

		dateFormats = format;
		return dateFormats;
	}

	public static Date parseString(String string) {
		if (string == null || string.length() == 0)
			return null;

		SimpleDateFormat formats[] = DateUtilities.getFormats();

		for (int i = 0; i < formats.length; i++) {
			try {
				Date date = formats[i].parse(string);
				long time = date.getTime();
				/* restrict between 1653 and 2286 (to avoid turning things like phone numbers into dates) */
				if (time > -10000000000000L && time < 10000000000000L)
					return date;
			} catch (Exception e) {
				// try next format
			}
		}
		return null;
	}

	public static Date parseString(String string, SimpleDateFormat sdf) {
		if (string == null || string.length() == 0 || sdf == null)
			return null;
		try {
			return sdf.parse(string);
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatYear(Date date) {
		if (date == null)
			return "";
		else
			return dfY.format(date);
	}

	public static String formatMonthYear(Date date) {
		if (date == null)
			return "";
		else
			return dfMY.format(date);
	}

	public static String formatMonthDayYear(Date date) {
		if (date == null)
			return "";
		else
			return dfMDY.format(date);
	}

	public static String formatDate(Date date, SimpleDateFormat sdf) {
		if (date == null || sdf == null)
			return "";
		else
			return sdf.format(date);
	}

	public static boolean isDays(String[] string) {
		SimpleDateFormat wksShort = new SimpleDateFormat("EEE");
		SimpleDateFormat wksLong = new SimpleDateFormat("EEEE");
		for (int i = 0; i < string.length; i++) {
			if (parseString(string[i], wksShort) == null && parseString(string[i], wksLong) == null)
				return false;
		}
		return true;
	}

	public static boolean isMonths(String[] string) {
		SimpleDateFormat monthsShort = new SimpleDateFormat("MMM");
		SimpleDateFormat monthsLong = new SimpleDateFormat("MMMM");
		for (int i = 0; i < string.length; i++) {
			if (parseString(string[i], monthsShort) == null && parseString(string[i], monthsLong) == null)
				return false;
			for (int j = 0; j < string[i].length(); j++) {
				if (!Character.isLetter(string[i].charAt(j)))
					return false;
			}
		}
		return true;
	}

	public static boolean isYears(String[] string) {
		double year;
		for (int i = 0; i < string.length; i++) {
			try {
				year = Double.valueOf(string[i]).doubleValue();
			} catch (NumberFormatException e) {
				return false;
			}
			if (year < 1000. || year > 3000.)
				return false;
		}
		return true;
	}

	public static double differenceInDays(Date d1, Date d2) {
		if (d1 == null || d2 == null)
			return 0;
		return ((double) d2.getTime() - (double) d1.getTime()) / millisecondsInADay;
	}

	public static double differenceInSeconds(Date d1, Date d2) {
		if (d1 == null || d2 == null)
			return 0;
		return ((double) d2.getTime() - (double) d1.getTime()) / 1000;
	}

	public static Date addDaysToNow(int days) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, days);
		return c.getTime();
	}
}