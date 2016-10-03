/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import java.util.Arrays;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;

public class Statistics {
	public static final int COUNT = 0, WEIGHTEDCOUNT = 1, MIN = 2, MAX = 3, RANGE = 4, SUM = 5, MEAN = 6, MEDIAN = 7,
			SD = 8, SE = 9, VARIANCE = 10;
	public static final String[] LABELS = new String[] { "Count", "Weighted Count", "Min", "Max", "Range", "Sum",
			"Mean", "Median", "Standard Deviation", "Standard Error", "Variance" };

	private Statistics() {
	}

	public static double[] compute(double[] data, double[] weights) {

		int nRow = data.length;
		double xCount = 0;
		double xWeightedCount = 0;
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double xRange;
		double xSE;
		double xVariance;
		double xSum;
		double xMean;
		double xSD;
		double xMedian;

		double wt = 1.0;
		xSum = 0;
		xMean = 0;
		xVariance = 0;
		for (int i = 0; i < nRow; i++) {
			if (weights != null)
				wt = weights[i];
			if (wt > 0) {
				if (!Globals.isMissing(data[i])) {
					double xi = data[i];
					xCount++;
					xWeightedCount += wt;
					xSum += xi * wt;
					double xd = (xi - xMean) * wt;
					xMean += xd / xWeightedCount;
					xVariance += (xi - xMean) * xd;
					if (xMin > xi)
						xMin = xi;
					if (xMax < xi)
						xMax = xi;
				}
			}
		}

		if (xWeightedCount <= 0.0) {
			xMin = Globals.MISSING_VALUE;
			xMax = Globals.MISSING_VALUE;
			xRange = Globals.MISSING_VALUE;
			xSum = Globals.MISSING_VALUE;
			xMean = Globals.MISSING_VALUE;
			xSD = Globals.MISSING_VALUE;
			xSE = Globals.MISSING_VALUE;
			xVariance = Globals.MISSING_VALUE;
		} else if (xWeightedCount <= 1.0) {
			xRange = xMax - xMin;
			xSD = Globals.MISSING_VALUE;
			xSE = Globals.MISSING_VALUE;
			xVariance = Globals.MISSING_VALUE;
		} else {
			xRange = xMax - xMin;
			xVariance = xVariance / (xWeightedCount - 1.0);
			xSD = Math.sqrt(xVariance);
			xSE = xSD / Math.sqrt(xWeightedCount);
		}
		double[] d = new double[data.length];
		System.arraycopy(data, 0, d, 0, data.length);
		xMedian = Nonparametrics.letter(d, weights, 1, 0, 0)[0][0];
		return new double[] { xCount, xWeightedCount, xMin, xMax, xRange, xSum, xMean, xMedian, xSD, xSE, xVariance };
	}

	public static double[][] compute(double[][] data, double[] weights) {

		int nRow = data.length;
		int nCol = data[0].length;

		double[][] stats = new double[11][nCol];

		double[] xCount = stats[COUNT];
		double[] xWeightedCount = stats[WEIGHTEDCOUNT];
		double[] xMin = stats[MIN];
		double[] xMax = stats[MAX];
		double[] xRange = stats[RANGE];
		double[] xSum = stats[SUM];
		double[] xMean = stats[MEAN];
		double[] xMedian = stats[MEDIAN];
		double[] xSD = stats[SD];
		double[] xSE = stats[SE];
		double[] xVariance = stats[VARIANCE];

		for (int j = 0; j < nCol; j++) {
			xMin[j] = Double.POSITIVE_INFINITY;
			xMax[j] = Double.NEGATIVE_INFINITY;
		}
		double wt = 1.0;

		for (int i = 0; i < nRow; i++) {
			if (weights != null)
				wt = weights[i];
			if (wt > 0) {
				for (int j = 0; j < nCol; j++) {
					if (!Globals.isMissing(data[i][j])) {
						double xi = data[i][j];
						xCount[j]++;
						xWeightedCount[j] += wt;
						xSum[j] += xi * wt;
						double xd = (xi - xMean[j]) * wt;
						xMean[j] += xd / xWeightedCount[j];
						xVariance[j] += (xi - xMean[j]) * xd;
						if (xMin[j] > xi)
							xMin[j] = xi;
						if (xMax[j] < xi)
							xMax[j] = xi;
					}
				}
			}
		}

		for (int j = 0; j < nCol; j++) {
			if (xWeightedCount[j] <= 0.0) {
				xMin[j] = Globals.MISSING_VALUE;
				xMax[j] = Globals.MISSING_VALUE;
				xRange[j] = Globals.MISSING_VALUE;
				xSum[j] = Globals.MISSING_VALUE;
				xMean[j] = Globals.MISSING_VALUE;
				xSD[j] = Globals.MISSING_VALUE;
				xSE[j] = Globals.MISSING_VALUE;
				xVariance[j] = Globals.MISSING_VALUE;
			} else if (xWeightedCount[j] <= 1.0) {
				xRange[j] = xMax[j] - xMin[j];
				xSD[j] = Globals.MISSING_VALUE;
				xSE[j] = Globals.MISSING_VALUE;
				xVariance[j] = Globals.MISSING_VALUE;
			} else {
				xRange[j] = xMax[j] - xMin[j];
				xVariance[j] = xVariance[j] / (xWeightedCount[j] - 1.0);
				xSD[j] = Math.sqrt(xVariance[j]);
				xSE[j] = xSD[j] / Math.sqrt(xWeightedCount[j]);
			}
		}

		double[] d = new double[data.length];
		for (int j = 0; j < nCol; j++) {
			for (int i = 0; i < nRow; i++)
				d[i] = data[i][j];
			xMedian[j] = Nonparametrics.letter(d, weights, 1, 0, 0)[0][0];
		}

		return stats;
	}

	public static double standardizedSkewness(double[] data, double[] weights) {
		double skewness = 0;
		int n = 0;
		double mean = 0;
		double wt = 1;
		for (int i = 0; i < data.length; i++) {
			if (weights != null)
				wt = weights[i];
			double x = data[i];
			if (!Globals.isMissing(x)) {
				mean += wt * x;
				n += wt;
			}
		}
		if (n < 3)
			return Globals.MISSING_VALUE;
		mean /= n;
		double sd = 0;
		for (int i = 0; i < data.length; i++) {
			if (weights != null)
				wt = weights[i];
			double x = data[i];
			if (!Globals.isMissing(x)) {
				sd += wt * (x - mean) * (x - mean);
				skewness += wt * (x - mean) * (x - mean) * (x - mean);
			}
		}
		sd = Math.sqrt(sd / (n - 1));
		skewness = skewness / ((n - 1) * sd * sd * sd);
		skewness /= Math.sqrt(6.0 / n);
		return skewness;
	}

	public static double bowleySkewness(double[] data, double[] weights) {
		/*
		 * Bowley's robust measure of skewness
		 */
		if (weights == null) {
			weights = new double[data.length];
			Arrays.fill(weights, 1);
		}
		int n = Nonparametrics.freqN(data, weights, 0, 0);
		double q1 = Nonparametrics.freqI(data, weights, 0, 0, n / 10);
		double q2 = Nonparametrics.freqI(data, weights, 0, 0, n / 2);
		double q3 = Nonparametrics.freqI(data, weights, 0, 0, (9 * n) / 10);
		if (q3 == q1)
			return 0;
		else
			return (q3 + q1 - 2 * q2) / (q3 - q1);
	}

	public static double[] LMoments(double[] x, double[] weights, int nMoments) {
		/*
		 * Hosking, J.R.M., (1990).
		 * L-Moments: Analysis and estimation of distributions using linear combination of order statistics.
		 * J.Royal Stat. Soc. Ser. B 52, 105-124.
		 * Leland Wilkinson
		 */
		int n = 0;
		double wt = 1.0;
		for (int i = 0; i < x.length; i++) {
			if (weights != null)
				wt = weights[i];
			if (!Globals.isMissing(x[i]) && !Globals.isMissing(wt))
				n++;
		}
		int[] ind = Sorts.indexedDoubleArraySort(x, 0, 0);
		double[] moments = new double[nMoments];
		double temp;
		double[][] coef = new double[2][nMoments];
		for (int j = 2; j < nMoments; j++) {
			temp = 1.0 / (j * (n - j));
			coef[0][j] = (j + j - 1) * temp;
			coef[1][j] = (j - 1) * (n + j - 1) * temp;
		}
		temp = -n - 1;
		double c = 1.0 / (n - 1);
		int nhalf = n / 2;
		double s;
		for (int i = 0; i < nhalf; i++) {
			temp += 2.0;
			if (weights != null)
				wt = weights[i];
			if (!Globals.isMissing(x[ind[i]]) && wt > 0) {
				double xi = x[ind[i]];
				double xii = x[ind[n - i - 1]];
				double termp = xi + xii;
				double termn = xi - xii;
				moments[0] += termp;
				double s1 = 1;
				s = temp * c;
				moments[1] += s * termn;
				for (int j = 2; j < nMoments; j += 2) {
					double s2 = s1;
					s1 = s;
					s = coef[0][j] * temp * s1 - coef[1][j] * s2;
					moments[j] += s * termp;
					if (j < nMoments - 1) {
						s2 = s1;
						s1 = s;
						s = coef[0][j + 1] * temp * s1 - coef[1][j + 1] * s2;
						moments[j + 1] += s * termn;
					}
				}
			}
		}
		if (n != nhalf + nhalf) {
			double term = x[nhalf];
			s = 1;
			moments[0] += term;
			for (int j = 2; j < nMoments; j += 2) {
				s = -coef[1][j] * s;
				moments[j] += s * term;
			}
		}
		/* L-moment ratios */
		moments[0] /= n;
		if (moments[1] != 0) {
			for (int j = 2; j < nMoments; j++)
				moments[j] /= moments[1];
			moments[1] /= n;
		}
		return moments;
	}

	public static void SteinleyBruscoWeightsStandardize(double[][] X, double[] weights) {
		/*
		 * D. Steinley and M.J. Brusco (2008). A New Variable Weighting and Selection Procedure for K-means Cluster
		 * Analysis.
		 * Multivariate Behavioral Research, 43:77-108.
		 */
		int n = X.length;
		int p = X[0].length;
		double[][] stats = compute(X, weights);
		double[] wt = new double[p];
		double[] M = new double[p];
		double[] RC = new double[p];
		double minM = Double.POSITIVE_INFINITY;
		int minJ = -1;
		for (int j = 0; j < p; j++) {
			M[j] = 12.0 * stats[VARIANCE][j] / (stats[RANGE][j] * stats[RANGE][j]);
			if (M[j] < minM) {
				minJ = j;
				minM = M[j];
			}
		}
		for (int j = 0; j < p; j++)
			RC[j] = M[j] / minM;

		double[][] Z = new double[n][p];
		for (int i = 0; i < n; i++)
			System.arraycopy(X[i], 0, Z[i], 0, p);
		standardize(Z, weights);
		double[][] zstats = compute(Z, weights);
		for (int j = 0; j < p; j++)
			wt[j] = Math.sqrt((RC[j] * zstats[RANGE][minJ] * zstats[RANGE][minJ])
					/ (zstats[RANGE][j] * zstats[RANGE][j]));
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < p; j++) {
				X[i][j] = Z[i][j] * wt[j];
			}
		}
	}

	public static void standardize(double[] data, double mean, double sd, int iRow1, int iRow2) {
		if (sd == 0.0)
			return;
		for (int i = iRow1; i < iRow2; i++)
			data[i] = (data[i] - mean) / sd;
	}

	public static void standardize(double[][] data, double[] weights) {
		double[][] stats = compute(data, weights);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				data[i][j] = (data[i][j] - stats[MEAN][j]) / stats[SD][j];
			}
		}
	}

	public static void standardize(double[] data, double[] weights) {
		double[] stats = compute(data, weights);
		for (int i = 0; i < data.length; i++) {
			if (weights == null || weights[i] > 0)
				data[i] = (data[i] - stats[MEAN]) / stats[SD];
			else
				data[i] = Globals.MISSING_VALUE;
		}
	}

	public static void unitize(double[][] data, double[] weights) {
		double[][] stats = compute(data, weights);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++)
				if (stats[RANGE][j] == 0)
					data[i][j] = Globals.MISSING_VALUE;
				else if (weights == null || weights[i] > 0)
					data[i][j] = (data[i][j] - stats[MIN][j]) / stats[RANGE][j];
				else
					data[i][j] = Globals.MISSING_VALUE;
		}
	}

	public static void unitize(double[] data, double[] weights) {
		double[] stats = compute(data, weights);
		for (int i = 0; i < data.length; i++) {
			if (weights == null || weights[i] > 0)
				data[i] = (data[i] - stats[MIN]) / stats[RANGE];
			else
				data[i] = Globals.MISSING_VALUE;
		}
	}

	public static void quantize(double[][] data, double[] weights, int nQuantiles) {
		for (int j = 0; j < data[0].length; j++) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < data.length; i++) {
				if (!Globals.isMissing(data[i][j]) && (weights == null || weights[i] > 0)) {
					min = Math.min(min, data[i][j]);
					max = Math.max(max, data[i][j]);
				}
			}
			for (int i = 0; i < data.length; i++) {
				if (!Globals.isMissing(data[i][j]) && (weights == null || weights[i] > 0))
					data[i][j] = Math.floor((nQuantiles * (data[i][j] - min) / (max - min)));
				else
					data[i][j] = Globals.MISSING_VALUE;
			}
		}
	}

	public static void quantize(double[] data, double[] weights, int nQuantiles) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < data.length; i++) {
			if (!Globals.isMissing(data[i]) && (weights == null || weights[i] > 0)) {
				min = Math.min(min, data[i]);
				max = Math.max(max, data[i]);
			}
		}
		for (int i = 0; i < data.length; i++) {
			if (!Globals.isMissing(data[i]) && (weights == null || weights[i] > 0))
				data[i] = Math.floor((nQuantiles * (data[i] - min) / (max - min)));
			else
				data[i] = Globals.MISSING_VALUE;
		}
	}

	public static double[] orderStatistics(double[] x, int distribution, double p, double q, int start) {
		/* distribution is value found in Probabilities enumeration */
		int n = x.length;
		double[] orders = new double[n];
		int[] index = Sorts.indexedDoubleArraySort(x, 0, n);
		for (int i = 1; i < n - 1; i++) {
			orders[index[i]] = Globals.MISSING_VALUE;
			if (Globals.isMissing(x[index[i]]))
				continue;
			if (i >= start && i < n - start)
				orders[index[i]] = (i - 0.3175) / (n + 0.365);
		}
		if (start == 0) {
			orders[index[n - 1]] = Math.pow(0.5, 1.0 / n);
			orders[index[0]] = 1 - orders[index[n - 1]];
		} else {
			orders[index[n - 1]] = Globals.MISSING_VALUE;
			orders[index[0]] = Globals.MISSING_VALUE;
		}
		for (int i = 0; i < n; i++)
			orders[i] = Probabilities.cdfinv(orders[i], p, q, distribution);
		return orders;
	}

	public static double pearsonCorrelation(double[] x, double[] y, double[] weights) {
		double coef = 0;
		double xmean = 0;
		double ymean = 0;
		double xsd = 0;
		double ysd = 0;
		double n = 0;
		for (int i = 0; i < x.length; i++) {
			double weight = 1;
			if (weights != null)
				weight = weights[i];
			if (!Globals.isMissing(x[i]) && !Globals.isMissing(y[i]) && weight > 0) {
				n += weight;
				double xm = x[i] - xmean;
				double ym = y[i] - ymean;
				xmean += xm / n;
				ymean += ym / n;
				xsd += xm * (x[i] - xmean);
				ysd += ym * (y[i] - ymean);
				double prod = xm * (y[i] - ymean);
				coef += prod * weight;
			}
		}
		return coef / Math.sqrt(xsd * ysd);
	}

	public static double spearmanCorrelation(double[] x, double[] y, double[] weights) {
		int n = x.length;
		double[] ax = new double[n];
		double[] ay = new double[n];
		for (int i = 0; i < n; i++) {
			ax[i] = x[i];
			ay[i] = y[i];
		}
		double[] rx = Sorts.rank(ax);
		double[] ry = Sorts.rank(ay);
		return pearsonCorrelation(rx, ry, weights);
	}

	public static double[][] tTests(double[] n, double[] means, double[] sumsq) {
		/* computes unequal variance t-test (Welch-Satterthwaite) and returns t below diagonal and df above diagonal */
		double[][] tt = new double[n.length][n.length];
		for (int j = 1; j < n.length; j++) {
			for (int k = 0; k < j; k++) {
				double sj = sumsq[j] / ((n[j] - 1) * n[j]);
				double sk = sumsq[k] / ((n[k] - 1) * n[k]);
				double sj2 = sj * sj;
				double sk2 = sk * sk;
				double t = (means[j] - means[k]) / Math.sqrt(sj + sk);
				double df = (sj + sk) * (sj + sk) / (sj2 / (n[j] - 1) + sk2 / (n[k] - 1));
				tt[j][k] = t;
				tt[k][j] = df;
			}
		}
		return tt;
	}

	public static double[] fTests(double[][] x, int[] groups, double[] wts) {
		/* computes F statistics on groups */
		int nRow = x.length;
		int nCol = x[0].length;
		int nGrp = 0;
		for (int i = 0; i < nRow; i++)
			nGrp = Math.max(groups[i], nGrp);
		nGrp++;
		double[] counts = new double[nGrp];
		double[][] means = new double[nGrp][nCol];
		double wt = 1;
		for (int i = 0; i < nRow; i++) {
			if (wts != null)
				wt = wts[i];
			if (wt > 0) {
				int k = groups[i];
				if (k < 0)
					continue;
				counts[k] += wt;
				for (int j = 0; j < nCol; j++) {
					means[k][j] += x[i][j];
				}
			}
		}
		for (int k = 0; k < nGrp; k++) {
			for (int j = 0; j < nCol; j++) {
				means[k][j] /= counts[k];
			}
		}
		double[][] stats = Statistics.compute(x, wts);
		double[] ssq = new double[nCol];
		double[] fStat = new double[nCol];
		for (int j = 0; j < nCol; j++) {
			for (int k = 0; k < nGrp; k++) {
				double diff = means[k][j] - stats[Statistics.MEAN][j];
				ssq[j] += counts[k] * diff * diff;
			}
			double num = ssq[j] / (nGrp - 1);
			double denom = (nRow - 1) * stats[Statistics.VARIANCE][j] / (nRow - nGrp);
			fStat[j] = num / denom;
		}
		return fStat;
	}

	public static double normalAndersonDarling(double[] z) {
		/* Anderson-Darling test of normality */
		int n = z.length;
		int nn = 0;
		for (int i = 0; i < n; i++) {
			if (!Globals.isMissing(z[i]))
				nn++;
		}
		double[] y = new double[nn];
		nn = 0;
		for (int i = 0; i < n; i++) {
			if (!Globals.isMissing(z[i])) {
				y[nn] = z[i];
				nn++;
			}
		}
		standardize(y, null);
		double[] u = new double[nn];
		for (int i = 0; i < nn; i++)
			u[i] = Probabilities.cdf(y[i], 0, 1, Probabilities.NORMAL, false);
		Sorts.doubleArraySort(u, 0, 0);
		return computeAndersonDarlingStatistic(u);
	}

	public static double computeAndersonDarlingStatistic(double[] u) {
		/*
		 * Anderson, T.W. and Darling, D.A. (1954).
		 * A Test of Goodness-of-Fit.
		 * Journal of the American Statistical Association, 49, 765-769
		 * D'Agostino, R.B. and Stephens, M.A. (1986).
		 * Goodness-of-Fit Techniques.
		 * New York: Marcel Dekker, Table 4.9, page 127.
		 * Coded by Leland Wilkinson
		 */
		double a2 = 0;
		int n = u.length;

		for (int i = 0; i < n - 1; i++) {
			if (!Globals.isMissing(u[i]))
				a2 += (2 * i + 1) * Math.log(u[i]) + (2 * n - 1 - 2 * i) * Math.log(1 - u[i]);
		}

		a2 = -n - (1.0 / n) * a2;
		a2 = a2 * (1 + .75 / n + 2.25 / (n * n));
		double q = 0;
		if (a2 < .2)
			q = -13.436 + 101.14 * a2 - 223.73 * a2 * a2;
		else if (a2 < .34)
			q = -8.318 + 42.796 * a2 - 59.938 * a2 * a2;
		else if (a2 < .6)
			q = .9177 - 4.279 * a2 - 1.38 * a2 * a2;
		else
			q = 1.2937 - 5.709 * a2 + .0186 * a2 * a2;
		double p = Math.exp(q);
		if (a2 < .6)
			p = 1 - p;
		return p;
	}

	public static double runs(double[] data, double[] weights) {
		/* Wald-Wolfowitz runs test */
		double previous = 0;
		int nRun = 0;
		int nPos = 0;
		int n = 0;
		for (int i = 0; i < data.length; i++) {
			double wt = 1;
			if (weights != null)
				wt = weights[i];
			if (wt > 0) {
				n++;
				if (data[i] >= 0 && previous < 0) {
					nRun++;
					nPos++;
				} else if (data[i] < 0 && previous >= 0) {
					nRun++;
				} else if (data[i] >= 0 && previous >= 0) {
					nPos++;
				}
				previous = data[i];
			}
		}
		int nNeg = n - nPos;
		double mu = 1 + (2 * nPos * nNeg) / (double) n;
		double sigma = Math.sqrt((mu - 1) * (mu - 2) / ((double) n - 1));
		double z = (nRun - mu) / sigma;
		return 1 - Probabilities.cdf(Math.abs(z), 0, 1, Probabilities.NORMAL, false);
	}

	public static double[][] autoCorrelationFunction(double[] x, double[] weights, int nLags) {
		/* computes ACF (standard errors in acf[.][1]) */
		int n = x.length;
		double[] xStats = Statistics.compute(x, weights);
		double xMean = xStats[Statistics.MEAN];
		double xVariance = xStats[Statistics.VARIANCE];
		double xN = xStats[Statistics.COUNT];

		double[][] acf = new double[nLags][2];
		double sum = 0;
		for (int j = 1; j < nLags; j++) {
			acf[j - 1][0] = 0;
			for (int i = 0; i < n - j; i++) {
				if (!Globals.isMissing(x[i]) && !Globals.isMissing(x[i + j]))
					acf[j - 1][0] += (x[i] - xMean) * (x[i + j] - xMean);
			}
			acf[j - 1][0] /= xVariance * xN;
			acf[j - 1][1] = Math.sqrt((1 + 2 * sum) / xN);
			sum = 0;
			for (int k = 0; k < j; k++)
				sum += acf[k][0] * acf[k][0];
		}

		return acf;
	}

	public static double[][] partialAutoCorrelationFunction(double[][] acf, double xN, int nLags) {
		double[] wk = new double[nLags];
		double[][] pacf = new double[nLags][2];
		pacf[0] = acf[0];
		for (int j = 1; j < nLags; j++) {
			wk[j - 1] = pacf[j - 1][0];
			if (j > 1) {
				for (int k = 0; k < j / 2; k++) {
					double t1 = wk[k] - wk[j - k - 2] * pacf[j - 1][0];
					double t2 = wk[j - k - 2] - wk[k] * pacf[j - 1][0];
					wk[k] = t1;
					wk[j - k - 2] = t2;
				}
			}
			double sum1 = 0;
			double sum2 = 0;
			for (int i = 0; i < j; i++) {
				sum1 += acf[j - i - 1][0] * wk[i];
				sum2 += acf[i][0] * wk[i];
			}
			pacf[j][0] = (acf[j][0] - sum1) / (1.0 - sum2);
			pacf[j][1] = 1.0 / Math.sqrt(xN);
		}
		return pacf;
	}

	public static double[] crossCorrelationFunction(double[] x, double[] y, double[] weights, int nLags) {
		int nx = x.length;
		int ny = y.length;
		int nxy = Math.min(nx, ny);
		if (nLags == 0)
			nLags = nxy;
		double[] xStats = Statistics.compute(x, weights);
		double xMean = xStats[Statistics.MEAN];
		double xStd = xStats[Statistics.SD];
		double[] yStats = Statistics.compute(y, weights);
		double yMean = yStats[Statistics.MEAN];
		double yStd = yStats[Statistics.SD];

		double[] ccf = new double[nxy];
		int k2 = nLags / 2;
		double denom = xStd * yStd * nxy;
		double sum = 0;
		for (int j = 0; j < k2; j++) {
			int jp = k2 + j;
			int jm = k2 - j;
			int nmj = nxy - j;
			if (jp < nLags)
				ccf[jp] = 0;
			ccf[jm] = 0;
			for (int i = 0; i < nmj; i++) {
				int ipj = i + j;
				if (jp < nLags)
					ccf[jp] += (x[i] - xMean) * (y[i] - yMean);
				if (jp != jm)
					ccf[jm] += (y[i] - yMean) * (x[ipj] - xMean);

			}
			if (jp < nLags)
				ccf[jp] /= denom;
			if (jp != jm)
				ccf[jm] /= denom;
		}
		return ccf;
	}
}
