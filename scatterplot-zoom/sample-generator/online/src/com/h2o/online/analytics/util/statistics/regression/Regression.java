/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics.regression;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.LinearSystems;
import com.h2o.online.analytics.util.statistics.Statistics;

public abstract class Regression {
	/*
	 * Base class for regression
	 */
	public double ymean, sumwt, sse, ssr, sst, dfr, dfe, msr, mse, rsq, fstat, probF;
	public int pivot;
	public double[] coefficients;
	public double[] estimates;
	public double[] residuals;
	public double[] observed;
	public double[] orders;
	public double[] leverages;
	public double[] standardErrors;
	public double[] tStatistics;
	public double[] probabilities;
	public double[] lowerCI;
	public double[] upperCI;
	public double[] xmean;
	public double[] vif;
	public boolean[] isSignificant;
	public double[][] covariances;
	public double[][] correlations;
	public double[][] smooth;
	public double[] weights;
	public double[][] xx;
	public double[] xy;
	public double[][] acf, pacf;
	public double phi, theta;
	public boolean isAR1, isMA1;
	public boolean isConverged;
	public int nRows;
	public int nLags = 11;
	public int nD;
	public static final int ANDREWS = 1, FAIR = 2, HUBER = 3, TALWAR = 4, WELSCH = 5, LOGISTIC = 6, EPANECHNIKOV = 7,
			BIWEIGHT = 8, TRIWEIGHT = 9, TRICUBE = 10, GAUSSIAN = 11, CAUCHY = 12, FIXED = 13, KNN = 14, UNIFORM = 15,
			LOESS = 16, POLYNOMIAL = 17, LOG = 18, POWER = 19, LINEAR = 20, QUADRATIC = 21, CUBIC = 22, MEAN = 23,
			MEDIAN = 24, MODE = 25, TRIM = 26;

	public Regression() {
	}

	public abstract void compute(double[][] xData, double[] yData, double[] weights, int model);

	public abstract void compute(double[][] data, double[] weight, int iRow1, int iRow2, int degree, int nDim, int model);

	public double prediction(double sx, double sz, int degree, int nDim, int model) {
		/* Computes predicted value from sx or from tuple (sx, sz) */
		double sf, sw;

		if (coefficients == null)
			return Globals.MISSING_VALUE;
		if (coefficients.length == 0)
			return Globals.MISSING_VALUE;
		if (coefficients.length == 1)
			return coefficients[0];

		sf = 0;
		if (model == LOG)
			sf = coefficients[0] + coefficients[1] * Math.log(sx);

		else if (model == POWER)
			sf = Math.exp(coefficients[0]) * Math.pow(sx, coefficients[1]);

		else if (model == POLYNOMIAL) {
			int m = 1;
			sf = coefficients[0];
			for (int j = 0; j < degree; j++) {
				for (int k = 0; k < nDim; k++) {
					sw = sx;
					if (k > 0)
						sw = sz;
					if (m < coefficients.length) // check in case collinear variables were dropped in polynomialWLS
						sf += coefficients[m] * Math.pow(sw, j + 1);
					m++;
				}
			}
		}
		return sf;
	}

	public double computeLeverage(double[] xval) {
		LinearSystems.symsol(xx, xval, xmean);
		double sum = 0;
		for (int i = 0; i < xmean.length; i++)
			sum += xmean[i] * xval[i];
		return sum;
	}

	public void computeCorrelations() {
		if (standardErrors == null || correlations == null)
			return;
		for (int j = 0; j < nD; j++) {
			for (int k = 0; k < nD; k++)
				correlations[j][k] = covariances[j][k] / (standardErrors[j] * standardErrors[k]);
			correlations[j][j] = 1;
		}
	}

	public void autoCorrelationFunction(double[] x, double[] weights) {
		/* computes both ACF and PACF (standard errors in acf[.][1]) */
		acf = Statistics.autoCorrelationFunction(x, weights, nLags);
		computeARMAParameters();
		pacf = Statistics.partialAutoCorrelationFunction(acf, x.length, nLags);
	}

	private void computeARMAParameters() {
		phi = acf[0][0];
		double p = Math.min(.49, Math.abs(phi));
		theta = (.0074092025 + .86469487 * p - 1.5883536 * p * p) / (1.0 - 2.3837594 * p + .95115984 * p * p);
		if (phi < 0)
			theta = -theta;
	}

	public void identifyProcess() {
		if (acf == null || pacf == null)
			return;
		if (Math.abs(acf[0][0]) > acf[1][0] && Math.abs(acf[1][0]) > acf[2][0] && Math.abs(acf[2][0]) > acf[3][0]
				&& Math.abs(acf[2][0]) > acf[2][1] && Math.abs(pacf[0][0]) > pacf[0][1]
				&& Math.abs(pacf[1][0]) < pacf[1][1] && Math.abs(pacf[2][0]) < pacf[2][1]) {
			isAR1 = true;
			isMA1 = false;
		} else if (Math.abs(acf[0][0]) > acf[0][1] && Math.abs(acf[1][0]) < acf[1][1]
				&& Math.abs(acf[2][0]) < acf[2][1] && Math.abs(pacf[0][0]) > pacf[1][0]
				&& Math.abs(pacf[1][0]) > pacf[2][0] && Math.abs(pacf[2][0]) > pacf[2][1]) {
			isMA1 = true;
			isAR1 = false;
		} else {
			isAR1 = true;
			isMA1 = false;
		}
	}

	public double[][] buildWeightMatrix() {
		//		System.out.println("phi, theta, AR1, MA1 " + phi + " " + theta + " " + isAR1 + " " + isMA1);
		double[][] w = new double[nRows][nRows];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nRows; j++) {
				if (isAR1) {
					if (i == j)
						w[i][i] = 1;
					else
						w[i][j] = Math.pow(phi, Math.abs(i - j));
				} else if (isMA1) {
					if (i == j)
						w[i][i] = 1 + theta * theta;
					else if (Math.abs(i - j) == 1)
						w[i][j] = theta;
				}
			}
		}
		return w;
	}

	public static double[][] sortAndTranspose(double[][] data, double[] weights) {
		int[] index = Sorts.indexedDoubleMatrixSort(data, 0, 0, 0);
		double[][] pts = new double[data[0].length][data.length];
		double[] wt = null;
		if (weights != null)
			wt = new double[weights.length];
		for (int i = 0; i < data.length; i++) {
			if (weights != null)
				wt[i] = weights[index[i]];
			if (pts.length == 2) {
				pts[0][i] = data[index[i]][0];
				pts[1][i] = data[index[i]][1];
			} else {
				pts[0][i] = data[index[i]][0];
				pts[1][i] = data[index[i]][2];
				pts[2][i] = data[index[i]][1];
			}
		}
		if (weights != null)
			System.arraycopy(wt, 0, weights, 0, wt.length);
		return pts;
	}
}
