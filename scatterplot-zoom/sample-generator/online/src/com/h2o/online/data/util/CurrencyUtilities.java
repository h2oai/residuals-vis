/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

public class CurrencyUtilities {
	private static final String DOLLAR_SIGN = "$", EURO_SIGN = "\u20AC", POUND_SIGN = "\u00A3", YEN_SIGN = "\u00A5",
			YUAN_SIGN = "\u00A5", RENMINBI_SIGN = "\u00A5";

	public static String checkCurrency(String s) {
		if (s == null || s.length() < 2)
			return "";
		if (s.substring(0, 2).contains(DOLLAR_SIGN))
			return Units.DOLLAR;
		else if (s.substring(0, 2).contains(EURO_SIGN))
			return Units.EURO;
		else if (s.substring(0, 2).contains(POUND_SIGN))
			return Units.POUND;
		else if (s.substring(0, 2).contains(YEN_SIGN))
			return Units.YEN;
		else
			return "";
	}

	public static boolean isCurrency(String s) {
		if (s == null || s.length() < 2)
			return false;
		if (s.substring(0, 2).contains(DOLLAR_SIGN) || s.substring(0, 2).contains(EURO_SIGN)
				|| s.substring(0, 2).contains(POUND_SIGN) || s.substring(0, 2).contains(YEN_SIGN))
			return true;
		return false;
	}

	public static String asNumericString(String s, String unit) {
		if (unit == null || s.length() < 2)
			return s;
		String symbol = "";
		if (unit.equals(Units.DOLLAR)) {
			symbol = DOLLAR_SIGN;
		} else if (unit.equals(Units.EURO)) {
			symbol = EURO_SIGN;
		} else if (unit.equals(Units.POUND)) {
			symbol = POUND_SIGN;
		} else if (unit.equals(Units.YEN)) {
			symbol = YEN_SIGN;
		}
		if (symbol.equals(""))
			return s;

		if (s.startsWith(symbol))
			s = s.substring(1);
		else if (s.startsWith("-" + symbol))
			s = "-" + s.substring(2);
		else if (s.startsWith("(" + symbol) && s.endsWith(")"))
			s = "-" + s.substring(2, s.length() - 1);
		return s;
	}

	public static String asCurrency(String s, String unit) {
		String symbol = "";
		if (unit.equals(Units.DOLLAR))
			symbol = DOLLAR_SIGN;
		else if (unit.equals(Units.EURO))
			symbol = EURO_SIGN;
		else if (unit.equals(Units.POUND))
			symbol = POUND_SIGN;
		else if (unit.equals(Units.YEN))
			symbol = YEN_SIGN;

		if (s.startsWith("-"))
			s = s.substring(0, 1) + symbol + s.substring(1);
		else
			s = symbol + s;
		return s;
	}
}