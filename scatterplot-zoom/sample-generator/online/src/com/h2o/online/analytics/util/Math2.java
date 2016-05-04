/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util;

import com.h2o.online.Globals;

public class Math2 {
	private Math2() {
	}

	public static double sinh(double arg) {
		return (Math.exp(arg) - Math.exp(-arg)) / 2;
	}

	public static double cosh(double arg) {
		return (Math.exp(arg) + Math.exp(-arg)) / 2;
	}

	public static double tanh(double arg) {
		return sinh(arg) / cosh(arg);
	}

	public static double asinh(double arg) {
		return Math.log(arg + Math.sqrt(arg * arg + 1.0));
	}

	public static double acosh(double arg) {
		return Math.log(arg + Math.sqrt(arg * arg - 1.0));
	}

	public static double atanh(double arg) {
		return 0.5 * Math.log((1.0 + arg) / (1.0 - arg));
	}

	public static double logSumOfExponentials(double a, double b) {
		double r;
		if (a > b)
			r = a + Math.log(1 + Math.exp(b - a));
		else
			r = b + Math.log(1 + Math.exp(a - b));
		return r;
	}

	public static double logDifferenceOfExponentials(double a, double b) {
		double r;
		if (a > b)
			r = a + Math.log(1 - Math.exp(b - a));
		else
			r = Globals.MISSING_VALUE;
		return r;
	}

	public static double logGamma(double x) {
		double[] cof = { 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155,
				0.1208650973866179e-2, -0.5395239384953e-5 };
		double y = x;
		double tmp = x + 5.5;
		tmp -= (x + 0.5) * Math.log(tmp);
		double sum = 1.000000000190015;
		for (int j = 0; j < 6; j++)
			sum += cof[j] / ++y;
		return -tmp + Math.log(2.5066282746310005 * sum / x);
	}

	public static double logFactorial(double n) {
		if (n < 0)
			return Globals.MISSING_VALUE;
		else
			return logGamma(n + 1.0);
	}

	public static double min(double[] data) {
		double result = Globals.MISSING_VALUE;
		int index = 0;
		// Get first valid value, if any
		while (Globals.isMissing(result) && index < data.length) {
			result = data[index++];
		}
		// Compare with rest
		while (index < data.length) {
			if (!Globals.isMissing(data[index])) {
				result = Math.min(result, data[index]);
			}
			index++;
		}
		return result;
	}

	public static double max(double[] data) {
		double result = Globals.MISSING_VALUE;
		int index = 0;
		// Get first valid value, if any
		while (Globals.isMissing(result) && index < data.length) {
			result = data[index++];
		}
		// Compare with rest
		while (index < data.length) {
			if (!Globals.isMissing(data[index])) {
				result = Math.max(result, data[index]);
			}
			index++;
		}
		return result;
	}

	public static double range(double[] x) {
		return max(x) - min(x);
	}

	public static int indexOfLargestElement(double[] x) {
		if (x == null)
			return -1;
		double maxval = Double.NEGATIVE_INFINITY;
		int i = -1;
		for (int j = 0; j < x.length; j++) {
			if (!Globals.isMissing(x[j]) && x[j] > maxval) {
				maxval = x[j];
				i = j;
			}
		}
		return i;
	}
}
