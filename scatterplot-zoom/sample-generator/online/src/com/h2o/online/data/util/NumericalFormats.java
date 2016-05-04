/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.h2o.online.Globals;
import com.h2o.online.analytics.Analytic;
import com.h2o.online.data.DataTable;

public class NumericalFormats {
	protected String title;
	protected Analytic analytic;
	protected boolean isARCSIN;
	protected boolean isNothingNoteworthy;
	protected int nDeletions;
	private static int digits = 3;

	static NumberFormat nf = NumberFormat.getNumberInstance();
	static DecimalFormat df = (DecimalFormat) nf;
	private static DecimalFormatSymbols ds = df.getDecimalFormatSymbols();
	private static final char DECIMAL_SEPARATOR = ds.getDecimalSeparator();

	public void setDigits(int digits) {
		NumericalFormats.digits = digits;
	}

	public void incrementDigits() {
		digits++;
		if (digits > 9)
			digits = -1;
	}

	public int getDigits() {
		return digits;
	}

	public static String formatDoubleWithCommas(double d) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		String pattern = "###,###,###.##";
		df.applyPattern(pattern);
		return df.format(d);
	}

	public static String formatDouble(double x, int digits, boolean isScientific) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		if (digits < 0)
			isScientific = true;
		if (isScientific) {
			df = new DecimalFormat("0.###E0");
		} else {
			df.setMaximumFractionDigits(digits);
			df.setMinimumFractionDigits(digits);
		}
		return df.format(x);
	}

	public static String formatInteger(int x) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		return df.format(x);
	}

	public static String formatDoubleAsIntegerWithoutCommas(double x) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setGroupingUsed(false);
		return df.format((int) x);
	}

	public static boolean useScientificNotation(double data) {
		double ld = Math.abs(Math.log10(Math.abs(data)));
		if (ld < Globals.MAXDIGITS)
			return false;
		else
			return true;
	}

	public static boolean useScientificNotation(double[] data) {
		int nearZero = 0;
		for (int i = 0; i < data.length; i++) {
			double ld = Math.log10(Math.abs(data[i]));
			if (ld > Globals.MAXDIGITS)
				return true;
			else if (ld < -Globals.MAXDIGITS)
				nearZero++;
		}
		if (nearZero == data.length)
			return true;
		else
			return false;
	}

	public static int getTableDigits(DataTable table) {
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < table.getNumRows(); i++) {
			for (int j = 0; j < table.getNumCols(); j++) {
				double x = table.getTransformedDoubleValue(i, j);
				if (!Globals.isMissing(x))
					list.add(x);
			}
		}
		Object[] o = list.toArray();
		double[] data = new double[o.length];
		for (int j = 0; j < o.length; j++)
			data[j] = ((Double) o[j]).doubleValue();
		return computePrecision(data, true);
	}

	public static int computePrecision(double[] data, boolean isTable) {
		if (data == null || data.length == 0)
			return 0;
		int dMax = 0;
		DecimalFormat dfLong = (DecimalFormat) NumberFormat.getInstance();
		dfLong.setMaximumFractionDigits(Globals.MAXDIGITS);
		DecimalFormat dfShort = (DecimalFormat) NumberFormat.getInstance();
		boolean isUnderflow = true;
		boolean isOverflow = false;
		for (int i = 0; i < data.length; i++) {
			if (Globals.isMissing(data[i]))
				continue;
			if (!Double.isInfinite(data[i]) && Math.abs(data[i]) > 1.0e10)
				isOverflow = true;
			String s = dfLong.format(data[i]);
			int d = countTrailingNonzeroDigits(s);
			if (d > dMax)
				dMax = d;
			dfShort.setMaximumFractionDigits(digits);
			s = dfShort.format(data[i]);
			d = countTrailingNonzeroDigits(s);
			if (d != 0)
				isUnderflow = false;
		}
		if ((isTable && isUnderflow) || isOverflow)
			return -1;
		else
			return dMax;
	}

	public static int countTrailingNonzeroDigits(String s) {
		int count = 0;
		boolean foundNonZeroValue = false;
		boolean hasDecimal = false;
		for (int i = s.length() - 1; i > 0; i--) {
			char d = s.charAt(i);
			if (d == DECIMAL_SEPARATOR) {
				hasDecimal = true;
				break;
			} else if (d == '0') {
				if (foundNonZeroValue)
					count++;
			} else {
				foundNonZeroValue = true;
				count++;
			}
		}
		if (!hasDecimal)
			count = 0;
		return count;
	}
}
