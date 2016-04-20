/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

import java.util.Date;

public class Units {
	public static final String DATE = "date", US_PHONE_NUMBER = "us_phone_number", ZIPCODE = "zipcode",
			IP_ADDRESS = "ip_address", PERCENT = "percent", CATEGORY = "category", NUMBER = "number";
	public static final String DOLLAR = "dollar", EURO = "euro", POUND = "pound", YEN = "yen", YUAN = "yuan",
			RENMINBI = "renminbi";

	public static String getUnits(String s) {
		s = TextUtilities.trimQuotes(s).trim();

		if (TextUtilities.isMissingValueSymbol(s))
			return "";

		String unit = CurrencyUtilities.checkCurrency(s);
		if (!unit.equals(""))
			return unit;

		Date d = DateUtilities.parseString(s);
		if (d != null)
			return DATE;

		if (TextUtilities.isPercent(s))
			return PERCENT;

		if (TextUtilities.isIPAddress(s))
			return IP_ADDRESS;

		if (TextUtilities.isZipCode(s))
			return ZIPCODE;

		if (TextUtilities.parseUSPhoneNumber(s) != null)
			return US_PHONE_NUMBER;

		if (TextUtilities.isNumber(s))
			return NUMBER;
		else
			return CATEGORY;
	}

	public static boolean isCategoricalUnit(String s) {
		return s.equals(CATEGORY) || s.equals(US_PHONE_NUMBER) || s.equals(IP_ADDRESS) || s.equals(ZIPCODE);
	}
}