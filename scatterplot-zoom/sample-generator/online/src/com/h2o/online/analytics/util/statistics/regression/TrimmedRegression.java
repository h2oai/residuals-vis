package com.h2o.online.analytics.util.statistics.regression;

import com.h2o.online.analytics.util.statistics.Statistics;

public class TrimmedRegression extends Regression {

	@Override
	public void compute(double[][] xData, double[] yData, double[] weights, int model) {
	}

	@Override
	public void compute(double[][] data, double[] weight, int iRow1, int iRow2, int degree, int nDim, int model) {
	}

	public double[][] compute(double[] data, double proportion) {
		int nRows = data.length;
		double[] z = new double[nRows];
		System.arraycopy(data, 0, z, 0, nRows);
		Statistics.standardize(z, null);
		double[][] y = new double[nRows][2];
		double[] weights = new double[nRows];
		double[] dataMin = new double[2];
		double[] dataMax = new double[2];
		dataMin[0] = 0;
		dataMax[0] = nRows;
		dataMin[1] = Double.POSITIVE_INFINITY;
		dataMax[1] = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < nRows; i++) {
			weights[i] = 1;
			y[i][0] = i;
			y[i][1] = z[i];
			z[i] = y[i][1];
			dataMin[1] = Math.min(dataMin[1], z[i]);
			dataMax[1] = Math.max(dataMax[1], z[i]);
		}

		int degree = 1;
		boolean isMarronCorrection = false;
		double bandwidth = 0;
		int neighbors = 0;
		boolean isBounded = true;
		int numKnot = 0;
		int window = NonparametricRegression.KNN;
		int kernel = NonparametricRegression.BIWEIGHT;
		int smoother = Regression.TRIM;
		return NonparametricRegression.kernelSmooth(y, weights, dataMin, dataMax, null, null, numKnot, smoother,
				degree, kernel, neighbors, window, proportion, bandwidth, isBounded, isMarronCorrection, true);
	}
}
