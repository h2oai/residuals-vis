/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.FunctionEvaluator;
import com.h2o.online.analytics.util.Matrices;
import com.h2o.online.analytics.util.Optimizer;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.WeibullFunction;
import com.h2o.online.analytics.util.statistics.regression.OrdinaryLeastSquaresRegression;
import com.h2o.online.analytics.util.statistics.regression.TrimmedRegression;

public class Cognostics {
	/*
	 * Diagnostic statistical tests. Cognostics is an amalgam of
	 * "Cognitive Diagnostics." Tukey, J.W. and Tukey, P.A. (1985). Computer
	 * graphics and exploratory data analysis: An introduction. In Proceedings
	 * of Annual Conference and Exposition: Computer Graphics, Vol. 3. Silver
	 * Sprint, MD: National Micrographics Association, pp. 773-785.
	 */

	private static Cell[] cells;

	private static class Cell {
		private double determinantOfCovarianceMatrix;
		private double[][] inverseOfCovarianceMatrix;
	}

	public Cognostics() {
	}

	public static boolean isSkewed(double[] x, double[] weights) {
		if (x == null)
			return false;
		double skewness = computeSkewness(x, weights);
		double cutoff = .15;
		if (x.length < 25)
			cutoff = .25;
		return skewness > cutoff;
	}

	public static double computeSkewness(double[] x, double[] weights) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return Globals.MISSING_VALUE;
		double[] wts = null;
		if (weights != null && weights.length == x.length)
			wts = weights;
		double skewness = Globals.MISSING_VALUE;
		double[] moments = Statistics.LMoments(x, wts, 3);
		skewness = moments[2];
		return skewness;

	}

	public static boolean isOverDispersed(double[] y, double[] yhat, double[] weights) {
		/*
		 * Dean, C. B. (1992). Testing for overdispersion in Poisson and
		 * Binomial regression models. Journal of the American Statistical
		 * Association 87, 451-457.
		 */
		if (y == null || y.length < Globals.TOO_SMALL)
			return false;
		double numerator = 0;
		double denominator = 0;
		double wt = 1;
		for (int i = 0; i < y.length; i++) {
			if (weights != null)
				wt = Math.floor(weights[i]);
			if (wt > 0 && !Globals.isMissing(y[i]) && !Globals.isMissing(yhat[i])) {
				numerator += wt * (y[i] - yhat[i]) * (y[i] - yhat[i]) - y[i];
				denominator += wt * yhat[i] * yhat[i];
			}
		}
		double z = Math.abs(numerator / Math.sqrt(2 * denominator));
		double p = 1 - Probabilities.cdf(z, 0, 1, Probabilities.NORMAL, false);
		return p < .01;
	}

	public static boolean isZeroInflated(double[] x) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return false;
		double proportion = .10;
		int nValues = countValues(x);
		if (nValues < 4)
			return false;
		if (nValues < 10)
			proportion = 2.0 / nValues;
		int n = x.length;
		int zeroCount = 0;
		for (int i = 0; i < n; i++) {
			if (x[i] == 0)
				zeroCount++;
		}
		return (double) zeroCount / n > proportion;
	}

	public static boolean isZeroInflated(double[] y, double[] yhat, double[] weights) {
		/*
		 * Jan van den Broek (1995). A Score Test for Zero Inflation in a
		 * Poisson Distribution. Biometrics, 51, 738-743
		 */
		if (y == null || y.length < Globals.TOO_SMALL) {
			return false;
		}
		double wt = 1;
		double numerator = 0;
		double denominator = 0;
		double one = 1;
		double pzero = 0;
		int zeroCount = 0;
		for (int i = 0; i < y.length; i++) {
			if (weights != null)
				wt = Math.floor(weights[i]);
			if (wt > 0 && !Globals.isMissing(y[i]) && !Globals.isMissing(yhat[i])) {
				if (y[i] == 0)
					zeroCount++;
				pzero = Math.exp(-yhat[i]);
				one = 1;
				if (y[i] == 0)
					one = 0;
				numerator += wt * (one - pzero) * (one - pzero) / (pzero * pzero);
				denominator += wt * ((1 - pzero) / pzero - y[i]);
			}
		}
		double chisq = numerator / denominator;
		double p = 1 - Probabilities.cdf(chisq, 1, 0, Probabilities.CHISQUARE, false);
		double q = computeBrownZhaoTest(y, weights);
		int r = (int) ((y.length - zeroCount) * .10);
		return p < .01 && q < .01 && zeroCount > r;
	}

	private static double computeBrownZhaoTest(double[] x, double[] weights) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return 1;
		double lambda = 0;
		double count = 0;
		double wt = 1;
		double[] y = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			if (weights != null)
				wt = Math.floor(weights[i]);
			double xi = Math.floor(x[i]);
			if (!Globals.isMissing(xi) && wt > 0) {
				y[i] = Math.sqrt(xi + .375);
				lambda += wt * y[i];
				count += wt;
			}
		}
		lambda /= count;
		double t = 0;
		wt = 1;
		for (int i = 0; i < x.length; i++) {
			if (weights != null)
				wt = Math.floor(weights[i]);
			if (!Globals.isMissing(y[i]) && wt > 0)
				t += wt * (y[i] - lambda) * (y[i] - lambda);
		}
		double prob = 1 - Probabilities.cdf(4 * t, count - 1, 0, Probabilities.CHISQUARE, false);
		return prob;
	}

	public static boolean isSpiked(double[] x, double[] weights) {
		return computeSpikes(x);
	}

	public static boolean computeSpikes(double[] x) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return false;
		Map<Double, Integer> values = new TreeMap<Double, Integer>();
		for (int i = 0; i < x.length; i++) {
			if (Globals.isMissing(x[i]))
				continue;
			Double key = new Double(x[i]);
			if (values.containsKey(key)) {
				int count = (values.get(key)).intValue();
				values.put(key, new Integer(count + 1));
			} else {
				values.put(key, new Integer(1));
			}
		}
		int nVal = values.size();
		if (nVal < 2)
			return false;
		int[] counts = new int[nVal];
		Set<Double> keySet = values.keySet();
		Object[] keys = keySet.toArray();
		int maxCount = 0;
		int peak = -1;
		for (int i = 0; i < nVal; i++) {
			counts[i] = (values.get(keys[i])).intValue();
			if (counts[i] > maxCount) {
				maxCount = counts[i];
				peak = i;
			}
		}
		int avgCount = 0;
		int n = 0;
		for (int i = 0; i < nVal; i++) {
			if (i != peak) {
				avgCount += (values.get(keys[i])).intValue();
				n++;
			}
		}
		if (n < 2)
			return false;

		avgCount /= (n - 1);
		return maxCount > 10 * avgCount;
	}

	public static boolean
					isHeteroscedastic(double[] residuals, double[] estimates, double[] leverages, double[] weights) {
		double heteroscedasticity = computeResidualHeteroscedasticity(residuals, estimates, leverages, weights);
		return heteroscedasticity < .01;
	}

	public static double computeResidualHeteroscedasticity(double[] residuals, double[] estimates, double[] leverages,
					double[] weights) {
		/*
		 * Brown, Morton B. and Forsythe, Alan B. (1974), Robust Tests for
		 * Equality of Variances. Journal of the American Statistical
		 * Association, 69, 364-367.
		 */
		double heteroscedasticity = 1;
		if (estimates == null || residuals == null || leverages == null || residuals.length < Globals.TOO_SMALL)
			return Globals.MISSING_VALUE;
		/* standardize the residuals */
		double[] standardized = new double[residuals.length];
		for (int i = 0; i < residuals.length; i++)
			standardized[i] = residuals[i] / Math.sqrt(1 - leverages[i]);
		int[] ind = Sorts.indexedDoubleArraySort(estimates, 0, 0);
		int k = countNumDiscreteValues(estimates, ind);
		double[][] z;
		if (k < Math.min(10, residuals.length / 10.0))
			z = discretePartition(residuals.length, k, ind, estimates, standardized, weights);
		else
			z = continuousPartition(residuals.length, ind, standardized, weights);
		double[] results = OneWayANOVA.compute(z);
		heteroscedasticity = results[3];
		return heteroscedasticity;
	}

	public static double[][] discretePartition(int n, int k, int[] ind, double[] x, double[] y, double[] weights) {
		/* compute over subgroups defined by x */
		double wt = 1;
		int from = 0;
		int to = 1;
		double[][] z = new double[k][];
		int kj = 0;
		for (int i = 0; i < n; i++) {
			double nextValue = Double.NEGATIVE_INFINITY;
			if (i < n - 1)
				nextValue = x[ind[i + 1]];
			if (!Globals.isMissing(x[ind[i]]) && x[ind[i]] != nextValue) {
				double[] block = new double[to - from];
				int m = 0;
				for (int j = from; j < to; j++) {
					if (weights != null)
						wt = weights[ind[j]];
					if (wt > 0)
						block[m] = y[ind[j]];
					else
						block[m] = Globals.MISSING_VALUE;
					m++;
				}
				double[] stats = Statistics.compute(block, null);
				double median = stats[Statistics.MEDIAN];
				z[kj] = new double[m];
				for (int j = 0; j < m; j++)
					z[kj][j] = Math.abs(block[j] - median);
				from = to;
				kj++;
			}
			to++;
		}
		return z;
	}

	public static double[][] continuousPartition(int n, int[] ind, double[] y, double[] weight) {
		/* cut the y distribution into equal-sized groups based on x sort */
		int nBlocks = (int) (n / 10.0);
		nBlocks = Math.min(Math.max(nBlocks, 2), 10);
		int nb = n / nBlocks;
		double wt = 1;
		double[][] blocks = new double[nBlocks][nb];
		double[][] z = new double[nBlocks][];
		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[ind[i]];
			int mb = i % nb;
			int ib = Math.max(Math.min(i / nb, nBlocks - 1), 0);
			if (wt > 0)
				blocks[ib][mb] = y[ind[i]];
			else
				blocks[ib][mb] = Globals.MISSING_VALUE;
		}
		for (int k = 0; k < nBlocks; k++) {
			double[] stats = Statistics.compute(blocks[k], null);
			double median = stats[Statistics.MEDIAN];
			z[k] = new double[nb];
			for (int j = 0; j < nb; j++)
				z[k][j] = Math.abs(blocks[k][j] - median);
		}
		return z;
	}

	public static double[] computeBreuschGodfreyStatistic(double[] x, double[] weights) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return null;
		int nRows = x.length;
		double[] z = new double[nRows];
		System.arraycopy(x, 0, z, 0, nRows);
		TrimmedRegression trim = new TrimmedRegression();
		double[][] values = trim.compute(z, .7);
		for (int i = 0; i < nRows; i++)
			z[i] = values[i][1]; // smoother residuals
		double[][] xd = new double[nRows][3];
		xd[0][0] = Globals.MISSING_VALUE;
		xd[1][0] = Globals.MISSING_VALUE;
		for (int i = 2; i < nRows; i++) {
			xd[i][0] = 1;
			xd[i][1] = z[i - 1];
			xd[i][2] = z[i - 2];
		}
		OrdinaryLeastSquaresRegression ols = new OrdinaryLeastSquaresRegression();
		ols.compute(xd, z, weights, OrdinaryLeastSquaresRegression.LINEAR);
		double chisq = (nRows - 2) * ols.rsq;
		double[] bg = new double[2];
		bg[0] = ols.rsq;
		bg[1] = 1 - Probabilities.cdf(chisq, 2, 1, Probabilities.CHISQUARE, false);
		return bg;
	}

	public static double computeDurbinWatsonStatistic(double[] x) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return Globals.MISSING_VALUE;
		double x0 = x[0];
		double denominator = x0 * x0;
		if (Globals.isMissing(denominator))
			denominator = 0;
		double numerator = 0;
		for (int i = 1; i < x.length; i++) {
			double xi = x[i];
			if (!Globals.isMissing(xi)) {
				denominator += xi * xi;
				double z = xi - x0;
				if (!Globals.isMissing(z))
					numerator += z * z;
				x0 = xi;
			}
		}
		return numerator / denominator;
	}

	public static boolean isAutocorrelated(double[] x, double[] weights) {
		if (isSorted(x) || x.length < Globals.TOO_SMALL)
			return false;
		int nRows = x.length;
		double[] z = new double[nRows];
		System.arraycopy(x, 0, z, 0, nRows);
		double[] breuschGodfrey = computeBreuschGodfreyStatistic(z, weights);
		return breuschGodfrey != null && breuschGodfrey[0] > .05 && breuschGodfrey[1] < .01;
	}

	public static boolean isSorted(double[] x) {
		for (int i = 0; i < x.length - 1; i++) {
			if (x[i] > x[i + 1]) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasOutliers(double[] x, double[] weights) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return false;
		double[] bounds = outlierBounds(x, .05);
		for (int i = 0; i < x.length; i++) {
			if (weights != null && weights[i] > 0) {
				if (x[i] < bounds[0] || x[i] > bounds[1])
					return true;
			}
		}
		return false;
	}

	public static double[] outlierBounds(double[] x, double alpha) {
		if (x == null)
			return null;
		double[] bounds = new double[2];
		if (countValues(x) < Globals.TOO_SMALL) {
			int k = Nonparametrics.depthFinder(x.length, .001) - 1;
			double[][] depths = Nonparametrics.letter(x, null, k, 0, 0);
			bounds[0] = depths[0][k - 1];
			bounds[1] = depths[1][k - 1];
		} else {
			bounds[1] = outlierUpperBound(x, null, alpha);
			double[] y = new double[x.length];
			for (int i = 0; i < x.length; i++)
				y[i] = -x[i];
			bounds[0] = -outlierUpperBound(y, null, alpha);
			if (bounds[0] == bounds[1])
				bounds[0] = Globals.MISSING_VALUE;
		}
		return bounds;
	}

	public static double outlierUpperBound(double[] y, double[] weights, double alpha) {
		/*
		 * Upper tail gaps between values measured against exponential distribution. 
		 * Burridge, P. and Taylor, A.M.R. (2006).
		 * Additive outlier detection via extreme-value theory. 
		 * Journal of Time Series Analysis, 27, 685-701.
		 * Improvements due to K.T. Schwarz (2008). 
		 * Wind Dispersion of Carbon Dioxide Leaking from Underground Sequestration, 
		 * and Outlier Detection in Eddy Covariance Data using Extreme Value Theory.
		 * Dissertation, Physics, UC Berkeley.
		 */
		if (y == null)
			return Double.NaN;
		int n = y.length;
		double[] x = new double[n];
		System.arraycopy(y, 0, x, 0, n); // copy y so as not to change it when called
		Arrays.sort(x);
		double[] gaps = new double[n];
		for (int i = 1; i < n; i++) {
			gaps[i] = x[i] - x[i - 1];
		}
		int n4 = Math.min(50, n / 4);
		int start = Math.max(n / 2, 1);
		double[] ghat = new double[n];
		for (int i = start; i < n; i++) {
			for (int j = 1; j < n4; j++)
				ghat[i] += j * gaps[i - j];
			ghat[i] /= (n4 - 1);
		}
		double logAlpha = Math.log(1.0 / alpha);
		for (int i = start; i < n; i++) {
//			System.out.println(i + " " + x[i] + " " + gaps[i] + " " + ghat[i]);
			if (gaps[i] > logAlpha * ghat[i])
				return x[i - 1];
		}
		return Double.NaN;
	}

	public static double badOutlierUpperBound(double[] y, double[] weights, double alpha) {
		/*
		 * Upper tail gaps between values measured against exponential distribution. 
		 * Burridge, P. and Taylor, A.M.R. (2006).
		 * Additive outlier detection via extreme-value theory. 
		 * Journal of Time Series Analysis, 27, 685-701.
		 * Improvements due to K.T. Schwarz (2008). 
		 * Wind Dispersion of Carbon Dioxide Leaking from Underground Sequestration, 
		 * and Outlier Detection in Eddy Covariance Data using Extreme Value Theory.
		 * Dissertation, Physics, UC Berkeley.
		 */
		if (y == null || y.length < 10)
			return Double.NaN;
		int n = y.length;
		/* make a copy so input y array is not changed in here */
		double[] x = new double[n];
		System.arraycopy(y, 0, x, 0, n);
		Arrays.sort(x);
		/* now do differences */
		for (int i = n - 1; i > 0; i--) {
			x[i] = x[i] - x[i - 1];
		}
		/* Schwarz says 50 is sufficient, but use n/4 if there are fewer cases */
		int n2 = n / 2;
		int n4 = Math.min(50, n / 4);
		/* move up starting point if middle values are zeros */
		for (int i = n / 2; i < n; i++) {
			if (x[i] == 0)
				n2++;
			else
				break;
		}
		if (n2 > n - 10)
			return Double.NaN;
		/* compute Dhat values from spacings */
		double[] dhat = new double[n];
		for (int i = n - 1; i >= n2; i--) {
			for (int j = 1; j < n4; j++) {
				dhat[i] += j * x[i - j];
			}
			dhat[i] /= (n4 - 1);
		}
		double logAlpha = Math.log(1.0 / alpha);
		for (int i = 0; i < x.length; i++)
			System.out.println(i + " " + x[i] + " " + dhat[i]);
		for (int i = n2 + 1; i < n; i++) {
			if (x[i] > logAlpha * dhat[i]) {
				return x[i - 1];
			}
		}
		return Double.NaN;
	}

	public static double weibullOutlierUpperBound(double[] spacings, double[] weights, double alpha) {
		/*
		 * Upper tail gaps between values measured against exponential distribution. 
		 * Schwarz, K.T. (2008).
		 * Wind Dispersion of Carbon Dioxide Leaking from Underground Sequestration, 
		 *   and Outlier Detection in Eddy Covariance Data using Extreme Value Theory
		 * Dissertation, Department of Physics, UC Berkeley.
		 * 
		 * Coded by Leland Wilkinson
		 * Missing values NOT allowed in y!
		 */
		if (spacings == null || spacings.length < 10)
			return Double.NaN;
		int n = spacings.length;
		double[] x = new double[n];
		if (weights == null) {
			weights = new double[n];
			Arrays.fill(weights, 1.0);
		}
		System.arraycopy(spacings, 0, x, 0, n);
		Arrays.sort(x);
		int nn4 = n - n / 4;
		/* fill arrays, ignoring top quarter of values (which might be outliers) */
		double[] fractiles = new double[nn4];
		double[][] xvals = new double[nn4][2];
		for (int i = 1; i < nn4; i++) {
			xvals[i][0] = 1;
			xvals[i][1] = x[i];
			fractiles[i] = (double) i / (double) n;
		}

		FunctionEvaluator evaluator = new WeibullFunction(xvals, fractiles, weights);
		Optimizer optimizer = new Optimizer(evaluator);
		double[] parameterEstimates = evaluator.getInitialParameterEstimates();
		optimizer.levenbergMarquardt(xvals, fractiles, parameterEstimates);
		double cutoff = Double.POSITIVE_INFINITY;
		for (int i = 1; i < nn4; i++) {
			double p = 1 - Math.exp(-Math.pow(xvals[i][1] / parameterEstimates[0], parameterEstimates[1]));
			if (p > 1 - alpha) {
				cutoff = xvals[i - 1][1];
				break;
			}
		}
		return cutoff;
	}

	public static double[] computeCookDistances(double[] residuals, double[] leverages, int nPredictors, double mse) {

		if (residuals == null || leverages == null || residuals.length < Globals.TOO_SMALL)
			return null;
		double[] cook = new double[residuals.length];
		for (int i = 0; i < residuals.length; i++) {
			cook[i] = (residuals[i] * residuals[i] * leverages[i])
							/ (mse * (1 - leverages[i]) * (1 - leverages[i]) * nPredictors);
		}
		return cook;
	}

	public static boolean hasLargeCookDistances(double[] residuals, double[] leverages, int nPredictors, double mse) {
		if (residuals == null || leverages == null || residuals.length < Globals.TOO_SMALL)
			return false;
		double[] cook = computeCookDistances(residuals, leverages, nPredictors, mse);
		double cookCutoff = Probabilities.cook05(residuals.length, nPredictors);
		for (int i = 0; i < cook.length; i++) {
			if (Math.abs(cook[i]) > cookCutoff)
				return true;
		}
		return false;
	}

	public static boolean hasLargeLeverages(double[] leverages) {
		if (leverages == null || leverages.length < Globals.TOO_SMALL)
			return false;
		double leverageCutoff = outlierUpperBound(leverages, null, .01);
		for (int i = 0; i < leverages.length; i++) {
			if (Math.abs(leverages[i]) > leverageCutoff)
				return true;
		}
		return false;
	}

	public static boolean hasMultivariateOutliers(double[][] x, int[] cellIndices) {
		if (x == null || x.length < Globals.TOO_SMALL)
			return false;
		double[] mahalanobisDistances = computeMahalanobisDistances(x, cellIndices);
		double cutoff = outlierUpperBound(mahalanobisDistances, null, .01);
		for (int i = 0; i < x.length; i++) {
			if (mahalanobisDistances[i] > cutoff)
				return true;
		}
		return false;
	}

	public static double[] computeMahalanobisDistances(double[][] x, int[] cellIndices) {
		if (cells == null || x == null || x.length < Globals.TOO_SMALL)
			return null;
		double[] mahalanobisDistances = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			Cell cell = cells[cellIndices[i]];
			double[] res = new double[x.length];
			for (int j = 0; j < res.length; j++)
				res[j] = x[j][i];
			if (cell.determinantOfCovarianceMatrix > 0)
				mahalanobisDistances[i] = Matrices.quadraticForm(res, cell.inverseOfCovarianceMatrix);
			else
				mahalanobisDistances[i] = Globals.MISSING_VALUE;
		}
		return mahalanobisDistances;
	}

	public static boolean[] TrimTopQuarterOfMahalanobisDistances(double[][] x, int[] cellIndices) {
		if (cells == null || x == null || x.length < Globals.TOO_SMALL)
			return null;
		boolean[] delete = new boolean[x.length];
		double[] distances = new double[x.length];

		// for (int i = 0; i < x.length; i++) {
		// Cell cell = cells[cellIndices[i]];
		// double[] res = new double[x.length];
		// for (int j = 0; j < res.length; j++)
		// res[j] = resid[j][i];
		// distances[i] = Matrices.quadraticForm(res,
		// cell.inverseOfCovarianceMatrix);
		// }
		int[] ind = Sorts.indexedDoubleArraySort(distances, 0, 0);
		for (int i = x.length - 1; i > (3 * x.length) / 4; i--)
			delete[ind[i]] = true;
		return delete;
	}

	public static int countValues(double[][] data, int col) {
		/*
		 * get bounded and approximate count of values in data (because we are
		 * using Double as key)
		 */
		int nRows = data.length;
		Map<Double, Integer> values = new TreeMap<Double, Integer>();
		for (int i = 0; i < nRows; i++) {
			if (Globals.isMissing(data[i][col]))
				continue;
			Double key = new Double(data[i][col]);
			if (values.containsKey(key)) {
				int count = (values.get(key)).intValue();
				values.put(key, new Integer(count + 1));
			} else {
				values.put(key, new Integer(1));
			}
			if (values.size() > 25)
				break;
		}
		int nVal = values.size();
		return nVal;

	}

	public static int countValues(double[] data) {
		/*
		 * get bounded and approximate count of values in data (because we are
		 * using Double as key)
		 */
		int nRows = data.length;
		Map<Double, Integer> values = new TreeMap<Double, Integer>();
		for (int i = 0; i < nRows; i++) {
			if (Globals.isMissing(data[i]))
				continue;
			Double key = new Double(data[i]);
			if (values.containsKey(key)) {
				int count = (values.get(key)).intValue();
				values.put(key, new Integer(count + 1));
			} else {
				values.put(key, new Integer(1));
			}
			if (values.size() > 25)
				break;
		}
		int nVal = values.size();
		return nVal;

	}

	private static int countNumDiscreteValues(double[] x) {
		/* x is sorted on input */
		int k = 0;
		double last = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; i++) {
			if (!Globals.isMissing(x[i]) && x[i] != last) {
				last = x[i];
				k++;
			}
		}
		return k;
	}

	private static int countNumDiscreteValues(double[] x, int[] ind) {
		/* ind is a sort-order index into x */
		int k = 0;
		double last = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; i++) {
			if (!Globals.isMissing(x[ind[i]]) && x[ind[i]] != last) {
				last = x[ind[i]];
				k++;
			}
		}
		return k;
	}

	// public boolean hasHeterogeneousCovarianceMatrices(Cell[] cells, int
	// nRows) {
	// /* Box M Test on trimmed covariance matrices */
	// if (cells == null || nRows < Constants.TOO_SMALL)
	// return false;
	// double g = cells.length;
	// double r = cells[0].inverseOfCovarianceMatrix.length;
	// double s = (nRows - g) * Math.log(determinantOfCovarianceMatrix);
	// for (int j = 0; j < cells.length; j++) {
	// Cell cell = cells[j];
	// s -= (cell.nObs - 1) * Math.log(cell.determinantOfCovarianceMatrix);
	// }
	// double sum = 0;
	// for (int i = 0; i < cells.length; i++)
	// sum += 1.0 / (cells[i].nObs - 1);
	// sum -= 1.0 / (nRows - g);
	// double rho = 1 - sum * (2 * r * r + 3 * r - 1) / (6 * (r + 1) * (g - 1));
	// double sum2 = 0;
	// for (int i = 0; i < cells.length; i++)
	// sum2 += 1.0 / ((cells[i].nObs - 1) * (cells[i].nObs - 1));
	// sum2 -= 1.0 / ((nRows - g) * (nRows - g));
	// double tau = sum2 * ((r - 1) * (r - 2) / (6 * (g - 1)));
	// double df1 = (g - 1) * r * (r + 1) / 2;
	// double df2 = (df1 + 2) / Math.abs(tau - (1 - rho) * (1 - rho));
	// double gamma = (rho - df1 / df2) / df1;
	// double f = gamma * s;
	// double prob = 1 - Probabilities.cdf(f, df1, df2, Probabilities.F, false);
	// return prob < .01;
	// }

	// public void computeTrimmedWithinCellStatistics(double[][] residuals, int
	// nCells, boolean[] delete) {
	// if (residuals == null || residuals.length < Constants.TOO_SMALL)
	// return;
	// int nY = residuals.length;
	// int nR = cellIndices.length;
	// cells = new Cell[nCells];
	// for (int j = 0; j < nCells; j++) {
	// cells[j] = new Cell();
	// cells[j].inverseOfCovarianceMatrix = new double[nY][nY];
	// }
	// double[][] inverseCovarianceMatrix = new double[nY][nY];
	//
	// nObs = 0;
	// for (int i = 0; i < nR; i++) {
	// if (delete != null && delete[i])
	// continue;
	// boolean isMissing = false;
	// for (int j = 0; j < nY; j++) {
	// if (Constants.isMissing(resid[j][i]))
	// isMissing = true;
	// }
	// if (isMissing)
	// continue;
	// Cell cell = cells[cellIndices[i]];
	// for (int j = 0; j < nY; j++) {
	// for (int k = 0; k < nY; k++) {
	// double res2 = resid[j][i] * resid[k][i];
	// cell.inverseOfCovarianceMatrix[j][k] += res2;
	// if (!Constants.isMissing(res2))
	// inverseCovarianceMatrix[j][k] += res2;
	// }
	// }
	// cell.nObs++;
	// nObs++;
	// }
	// for (int i = 0; i < cells.length; i++) {
	// Cell cell = cells[i];
	// for (int j = 0; j < nY; j++) {
	// for (int k = 0; k < nY; k++) {
	// cell.inverseOfCovarianceMatrix[j][k] /= (cell.nObs - 1);
	// }
	// }
	// cell.determinantOfCovarianceMatrix =
	// LinearSystems.inverse(cell.inverseOfCovarianceMatrix);
	// }
	// for (int j = 0; j < nY; j++) {
	// for (int k = 0; k < nY; k++) {
	// inverseCovarianceMatrix[j][k] /= (nObs - nCells);
	// }
	// }
	// determinantOfCovarianceMatrix =
	// LinearSystems.inverse(inverseCovarianceMatrix);
	// if (delete == null) {
	// delete = TrimTopQuarterOfMahalanobisDistances();
	// computeTrimmedWithinCellStatistics(nCells, delete);
	// }
	// /* set it extreme because we don't want false alarms */
	// residualCutoffs[1] = Probabilities.cdfinv(.99999, nY, 1,
	// Probabilities.CHISQUARE);
	// }
	//
	// public void computeLeverageOutliers() {
	// if (leverages == null || nCases < Constants.TOO_SMALL)
	// return;
	// leverageCutoff = outlierUpperBound(leverages, .01);
	// }
	//
	// public boolean hasMultimodalResiduals() {
	// return hasMultimodalResiduals;
	// }
	//
	// public void computeResidualMultimodality() {
	// if (residuals == null || nCases < Constants.TOO_SMALL)
	// return;
	// double[] resid = residuals.getUnivariateResiduals();
	// if (resid == null)
	// return;
	// double[] res = new double[resid.length];
	// System.arraycopy(resid, 0, res, 0, res.length);
	// if (isMultiModal(res))
	// hasMultimodalResiduals = true;
	// }

	public static boolean isMultiModal(double[] data, double[] weights) {
		/*
		 * multimodality test based on bump hunting on kernel density estimate
		 * Silverman, B.W. (1981). Using kernel density estimates to investigate
		 * multimodality. Journal of the Royal Statistical Society, 43, 97-99.
		 */
		int nRows = data.length;
		if (data == null || nRows < Globals.TOO_SMALL || Cognostics.countValues(data) < Globals.TOO_SMALL)
			return false;
		double[] z = new double[nRows];
		System.arraycopy(data, 0, z, 0, nRows);
		Statistics.standardize(z, null);
		Sorts.doubleArraySort(z, 0, 0);
		int nx = 100;
		double deltaX = .08;
		double zx = -4.08;
		double tns = .5;
		double tens = .9 * Math.pow(nRows, -.2);
		double t = 2 * tns * tens;
		double t2 = t * t;
		double z1 = .3354102;
		double[] x = new double[nx];
		double[] fx = new double[nx];
		for (int j = 0; j < nx; j++) {
			double zk = 0;
			zx += deltaX;
			for (int i = 0; i < nRows; i++) {
				if (!Globals.isMissing(z[i])) {
					double wt = 1;
					if (weights != null)
						wt = weights[i];
					if (wt > 0) {
						double zp = (zx - z[i]) * (zx - z[i]) / t2;
						if (zp < 5)
							zk += 1. - zp / 5.;
					}
				}
			}
			double zy = zk * (z1 / (nRows * t));
			x[j] = zx;
			fx[j] = zy;
		}
		List<double[]> peaks = findPeaks(x, fx, .1);
		if (peaks.size() > 1)
			return true;
		else
			return false;
	}

	private static List<double[]> findPeaks(double[] x, double[] fx, double deltaF) {
		int nx = x.length;
		double mn = Double.POSITIVE_INFINITY;
		double mx = Double.NEGATIVE_INFINITY;
		double xValley = Globals.MISSING_VALUE;
		double xPeak = Globals.MISSING_VALUE;

		boolean lookformax = true;
		List<double[]> valleys = new ArrayList<double[]>();
		List<double[]> peaks = new ArrayList<double[]>();

		for (int i = 0; i < nx; i++) {
			double z = fx[i];
			if (z > mx) {
				mx = z;
				xPeak = x[i];
			}
			if (z < mn) {
				mn = z;
				xValley = x[i];
			}
			if (lookformax) {
				if (z < mx - deltaF) {
					peaks.add(new double[] { xPeak, mx });
					mn = z;
					xValley = x[i];
					lookformax = false;
				}
			} else {
				if (z > mn + deltaF) {
					valleys.add(new double[] { xValley, mn });
					mx = z;
					xPeak = x[i];
					lookformax = true;
				}
			}
		}
		return peaks;
	}

	public static double[] computeRobustMahalanobisDistances(double[][] data, double[] weights) {
		int n = data.length;
		int p = data[0].length;
		int h = (n + p + 1) / 2;
		double[] wts = new double[n];
		if (weights != null)
			System.arraycopy(weights, 0, wts, 0, n);
		else
			Arrays.fill(wts, 1);
		double[] mahalanobisDistances = new double[n];
		double[][] cov;
		for (int iter = 0; iter < 5; iter++) {
			double[][] data2 = Matrices.copy(data);
			Statistics.standardize(data2, wts);
			cov = SymmetricMatrices.compute(data2, wts, null, SimilarityFunctions.COVARIANCE, false);
			double determinant = LinearSystems.inverse(cov);
			if (Globals.isMissing(determinant) || determinant <= 0)
				cov = Matrices.identityMatrix(p);
			for (int i = 0; i < n; i++)
				mahalanobisDistances[i] = Matrices.quadraticForm(data2[i], cov);
			int[] index = Sorts.indexedDoubleArraySort(mahalanobisDistances, 0, 0);
			if (weights != null)
				System.arraycopy(weights, 0, wts, 0, n);
			else
				Arrays.fill(wts, 1);
			for (int i = h; i < n; i++)
				wts[index[i]] = -1;
		}
		return mahalanobisDistances;
	}

	public static double[] computeMahalanobisDistances(double[][] data, double[] weights) {
		int n = data.length;
		int p = data[0].length;
		double[][] cov = SymmetricMatrices.compute(data, weights, null, SimilarityFunctions.COVARIANCE, false);
		double determinant = LinearSystems.inverse(cov);
		if (Globals.isMissing(determinant) || determinant <= 0)
			cov = Matrices.identityMatrix(p);
		double[] mahalanobisDistances = new double[n];
		for (int i = 0; i < n; i++)
			mahalanobisDistances[i] = Matrices.quadraticForm(data[i], cov);
		return mahalanobisDistances;
	}
}