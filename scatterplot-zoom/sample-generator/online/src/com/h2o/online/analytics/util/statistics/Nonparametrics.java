/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;

public class Nonparametrics {

	private Nonparametrics() {
	}

	public static double[][] letter(double[] data, double[] weight, int depth, int iRow1, int iRow2) {
		/*
		 * Tukey letter values returned after m successive splits. If depth==1, median is returned after one split.
		 */
		if (iRow1 == iRow2) {
			iRow1 = 0;
			iRow2 = data.length;
		}
		double[][] letterValues = new double[2][depth];

		int n = freqN(data, weight, iRow1, iRow2);

		int k = n;
		for (int i = 0; i < depth; i++) {
			int j = k / 2;
			int m1 = j;
			int m2 = k - j - 1;
			double d1 = freqI(data, weight, iRow1, iRow2, m1);
			double d2 = freqI(data, weight, iRow1, iRow2, m2);
			letterValues[0][i] = (d1 + d2) / 2.;
			if (i > iRow2 - iRow1)
				letterValues[0][i] = letterValues[0][i - 1];
			m1 = n - k + j;
			m2 = n - j - 1;
			d1 = freqI(data, weight, iRow1, iRow2, m1);
			d2 = freqI(data, weight, iRow1, iRow2, m2);
			letterValues[1][i] = (d1 + d2) / 2.;
			if (i > iRow2 - iRow1)
				letterValues[1][i] = letterValues[1][i - 1];
			k = (k + 1) / 2;
		}

		return letterValues;
	}

	public static double[][] fractiles(double[] data) {
		/*
		 * fractiles of values in an array (.001, .01, .05, .1, .2, .25, .3, .4, .5, .6, .7, .75, .8, .9, .95, .99,
		 * .999).
		 * fractiles returned [2][data.length] array (f[i] in first column, values in second)
		 */
		double[] p = new double[] { .001, .01, .05, .1, .2, .25, .3, .4, .5, .6, .7, .75, .8, .9, .95, .99, .999 };
		int n = data.length;
		int pn = p.length;
		int[] index = Sorts.indexedDoubleArraySort(data, 0, 0);
		double[][] percent = new double[pn][2];
		for (int i = 0; i < pn; i++) {
			percent[i][0] = p[i];
			percent[i][1] = data[index[(int) (n * p[i])]];
		}
		return percent;
	}

	public static double fractile(double x, double[][] fractiles) {
		/*
		 * fractile of a value, as referenced in an array of fractiles.
		 */
		for (int i = 0; i < fractiles.length; i++) {
			if (x < fractiles[i][1])
				return fractiles[i][0];
		}
		return .9999;
	}

	public static double freqI(double[] data, double[] weight, int iRow1, int iRow2, int k) {
		/*
		 * find index into sorted array linked to frequencies
		 */
		if (iRow1 == iRow2) {
			iRow1 = 0;
			iRow2 = data.length;
		}
		int[] index = Sorts.indexedDoubleArraySort(data, iRow1, iRow2);
		double wt = 1.0;
		double n = 0.0;

		int i;
		for (i = iRow1; i < iRow2; i++) {
			if (weight != null)
				wt = weight[index[i]];
			if (!Globals.isMissing(data[index[i]]) && wt > 0)
				n += wt;
			if (n > k)
				break;
		}
		if (i == iRow2)
			return Globals.MISSING_VALUE;
		else
			return data[index[i]];
	}

	public static int freqN(double[] data, double[] weight, int iRow1, int iRow2) {
		/*
		 * find total number of cases under frequency weighting (excluding missing)
		 */
		if (iRow1 == iRow2) {
			iRow1 = 0;
			iRow2 = data.length;
		}
		double wt = 1.0;
		double n = 0.0;
		for (int i = iRow1; i < iRow2; i++) {
			if (weight != null)
				wt = weight[i];
			if (!Globals.isMissing(data[i]) && wt > 0)
				n += wt;
		}
		return (int) n;
	}

	public static int depthFinder(int n, double proportion) {
		/* Hofmann Kafadar Wickham depth for letter value box plots */
		return (int) (1 + Math.log(n) / Math.log(2) - proportion * Math.log(n) / Math.log(2));
	}

	public static double medianAbsoluteDeviation(double[] data, double[] weight, double location) {
		/*
		 * The Median Absolute Deviation (MAD) of a variable from a location measure.
		 */
		double wt = 1.0;
		int n = data.length;
		double[] deviation = new double[n];

		int[] index = Sorts.indexedDoubleArraySort(data, 0, 0);
		int nd = 0;
		for (int i = 0; i < n; i++) {
			double xi = data[index[i]];
			if (weight != null)
				wt = weight[index[i]];
			if (!Globals.isMissing(xi) && wt > 0) {
				deviation[nd] = Math.abs(xi - location) * wt;
				nd++;
			}
		}

		Sorts.doubleArraySort(deviation, 0, nd);
		int j = nd / 2;
		int k = nd - j - 1;
		return (deviation[j] + deviation[k]) / 2.;
	}

	public static void monotonicRegression(double[] x, double[] y, double[] yhat, boolean isDescending) {
		/*
		 * Used for nonmetric multidimensional scaling
		 * Kruskal secondary approach to ties
		 */
		int n = x.length;
		double[] ranks = Sorts.rank(x);
		for (int i = 0; i < n; i++) {
			ranks[i] += y[i] / 1000;
			if (isDescending)
				ranks[i] = -ranks[i];
		}
		int[] ind = Sorts.indexedDoubleArraySort(ranks, 0, 0);

		double[] ytmp = new double[n + 1];
		double tmp = 0;
		for (int i = 0; i < n; i++) {
			tmp += y[ind[i]];
			ytmp[i + 1] = tmp;
		}
		int ifirst = 0;
		int ilast = 0;
		do {
			double slope = Double.POSITIVE_INFINITY;
			for (int i = ifirst + 1; i <= n; i++) {
				tmp = (ytmp[i] - ytmp[ifirst]) / (i - ifirst);
				if (tmp < slope) {
					slope = tmp;
					ilast = i;
				}
			}
			for (int i = ifirst; i < ilast; i++)
				yhat[ind[i]] = (ytmp[ilast] - ytmp[ifirst]) / (ilast - ifirst);
			ifirst = ilast;
		} while (ifirst < n);
	}
}
