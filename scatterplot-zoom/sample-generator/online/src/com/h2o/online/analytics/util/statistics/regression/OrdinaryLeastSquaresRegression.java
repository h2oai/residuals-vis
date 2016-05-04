/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics.regression;

import com.h2o.online.Globals;
import com.h2o.online.analytics.Analytic;
import com.h2o.online.analytics.util.statistics.LinearSystems;
import com.h2o.online.analytics.util.statistics.Probabilities;
import com.h2o.online.analytics.util.statistics.Statistics;

public class OrdinaryLeastSquaresRegression extends Regression {
	public double[][] xData;
	public double[] yData;
	private double[] xxDiag;
	private boolean isHCC;

	/*
	 * Ordinary Least Squares (with first column of xData containing constant)
	 */
	public OrdinaryLeastSquaresRegression() {
	}

	public OrdinaryLeastSquaresRegression(boolean isHeteroscedasticityConsistentCovariances) {
		isHCC = isHeteroscedasticityConsistentCovariances;
	}

	@Override
	public void compute(double[][] data, double[] weight, int iRow1, int iRow2, int degree, int nDim, int model) {
	}

	public void compute(double[] xD, double[] yD, double[] weights, int model) {
		/* bivariate regression */
		nRows = xD.length;
		nD = 2;
		xData = new double[nRows][2];
		yData = new double[nRows];
		for (int i = 0; i < nRows; i++) {
			xData[i][0] = 1;
			xData[i][1] = xD[i];
			yData[i] = yD[i];
		}
		this.weights = weights;
		xmean = new double[nD];
		xx = new double[nD][nD];
		xxDiag = new double[nD];
		xy = new double[nD];
		ymean = 0;
		sumwt = 0;
		sst = 0;
		for (int i = 0; i < nRows; i++) {
			double wi = 1;
			if (weights != null)
				wi = weights[i];
			if (!Globals.isMissing(yData[i]) && Analytic.isComplete(xData[i], wi)) {
				double y = yData[i];
				if (model == LOG) {
					if (y > 0)
						y = Math.log(y);
					else
						continue;
				}
				sumwt += wi;
				double yd = wi * (y - ymean);
				ymean += yd / sumwt;
				sst += (yData[i] - ymean) * yd;
				for (int j = 1; j < nD; j++) {
					double xd = wi * (xData[i][j] - xmean[j]);
					xmean[j] += xd / sumwt;
					xy[j] += xd * (y - ymean);
					for (int k = 1; k <= j; k++) {
						xx[k][j] += xd * (xData[i][k] - xmean[k]);
					}
				}
			}
		}
		for (int j = 0; j < nD; j++)
			xxDiag[j] = xx[j][j];
		xx[0][0] = sumwt;
		pivot = LinearSystems.symdet(xx);
		if (pivot >= 0)
			return;

		for (int j = 1; j < nD; j++)
			xx[j][0] = xmean[j];

		coefficients = new double[nD];
		LinearSystems.symsol(xx, xy, coefficients);

		coefficients[0] = ymean;
		for (int j = 1; j < nD; j++)
			coefficients[0] -= xmean[j] * coefficients[j];

		computeResiduals();
		computeAssociatedStatistics();
		computeCorrelations();
	}

	@Override
	public void compute(double[][] xData, double[] yData, double[] weights, int model) {
		/* xData is stored row-wise */
		nRows = xData.length;
		for (int i = 0; i < nRows; i++) {
			if (xData[i] != null) {
				nD = xData[i].length;
				break;
			}
		}
		this.xData = xData;
		this.yData = yData;
		this.weights = weights;
		xmean = new double[nD];
		xx = new double[nD][nD];
		xxDiag = new double[nD];
		xy = new double[nD];
		ymean = 0;
		sumwt = 0;
		sst = 0;
		for (int i = 0; i < nRows; i++) {
			double wi = 1;
			if (weights != null)
				wi = weights[i];
			if (!Globals.isMissing(yData[i]) && Analytic.isComplete(xData[i], wi)) {
				double y = yData[i];
				if (model == LOG) {
					if (y > 0)
						y = Math.log(y);
					else
						continue;
				}
				sumwt += wi;
				double yd = wi * (y - ymean);
				ymean += yd / sumwt;
				sst += (yData[i] - ymean) * yd;
				for (int j = 1; j < nD; j++) {
					double xd = wi * (xData[i][j] - xmean[j]);
					xmean[j] += xd / sumwt;
					xy[j] += xd * (y - ymean);
					for (int k = 1; k <= j; k++) {
						xx[k][j] += xd * (xData[i][k] - xmean[k]);
					}
				}
			}
		}
		for (int j = 0; j < nD; j++)
			xxDiag[j] = xx[j][j];
		xx[0][0] = sumwt;
		pivot = LinearSystems.symdet(xx);
		if (pivot >= 0)
			return;

		for (int j = 1; j < nD; j++)
			xx[j][0] = xmean[j];

		coefficients = new double[nD];
		LinearSystems.symsol(xx, xy, coefficients);

		coefficients[0] = ymean;
		for (int j = 1; j < nD; j++)
			coefficients[0] -= xmean[j] * coefficients[j];

		computeResiduals();
		computeAssociatedStatistics();
		computeCorrelations();
	}

	private void computeResiduals() {
		if (coefficients == null)
			return;
		estimates = new double[nRows];
		residuals = new double[nRows];
		observed = new double[nRows];
		leverages = new double[nRows];

		sse = 0;
		dfe = 0;
		for (int i = 0; i < nRows; i++) {
			double wt = 1;
			if (weights != null)
				wt = weights[i];
			double yhat;
			double[] xval = xData[i];
			double y = yData[i];
			estimates[i] = Globals.MISSING_VALUE;
			residuals[i] = Globals.MISSING_VALUE;
			if (Analytic.isComplete(xval, wt)) {
				dfe += wt;
				yhat = 0;
				for (int j = 0; j < nD; j++)
					yhat += coefficients[j] * xval[j];
				observed[i] = y;
				estimates[i] = yhat;
				residuals[i] = y - yhat;
				leverages[i] = computeLeverage(xval);
				if (!Globals.isMissing(residuals[i]))
					sse += wt * residuals[i] * residuals[i];
			}
		}
		dfe = dfe - nD;
		orders = Statistics.orderStatistics(residuals, Probabilities.NORMAL, 0, 1, 0);
	}

	private void computeAssociatedStatistics() {
		if (coefficients == null || pivot >= 0)
			return;
		ssr = sst - sse;
		dfr = nD - 1;
		msr = ssr / dfr;
		mse = sse / dfe;
		rsq = ssr / sst;
		fstat = msr / mse;

		standardErrors = new double[nD];
		tStatistics = new double[nD];
		probabilities = new double[nD];
		lowerCI = new double[nD];
		upperCI = new double[nD];
		covariances = new double[nD][nD];
		correlations = new double[nD][nD];
		vif = null;
		if (nD > 2)
			vif = new double[nD];
		double t = Probabilities.cdfinv(.975, dfe, 0, Probabilities.T);

		olsCovariances(); // need these for vif computation even if hcCovariances results in null
		if (isHCC)
			hcCovariances();
		if (standardErrors == null) {
			return;
		}

		for (int j = 0; j < nD; j++) {
			standardErrors[j] = Math.sqrt(covariances[j][j]);
			tStatistics[j] = coefficients[j] / standardErrors[j];
			probabilities[j] = 1 - Probabilities.cdf(tStatistics[j] * tStatistics[j], 1, dfe, Probabilities.F, false);
			lowerCI[j] = coefficients[j] - t * standardErrors[j];
			upperCI[j] = coefficients[j] + t * standardErrors[j];
		}
		isSignificant = Probabilities.isSignificantBenjaminiHochberg(probabilities);
		probF = 1 - Probabilities.cdf(fstat, dfr, dfe, Probabilities.F, false);
	}

	private void olsCovariances() {
		double[] s = new double[nD];
		for (int j = 0; j < nD; j++) {
			for (int i = 0; i < nD; i++) {
				xy[i] = 0;
				if (i == j)
					xy[i] = 1;
			}
			LinearSystems.symsol(xx, xy, s);
			if (j > 0 && nD > 2)
				vif[j] = xxDiag[j] * s[j];
			for (int k = 0; k < nD; k++) {
				covariances[j][k] = s[k] * mse;
			}
		}
	}

	private void hcCovariances() {
		/*
		 * MacKinnon, J.G. and H. White. (1985).
		 * Some heteroskedasticity consistent covariance matrix estimators with improved finite sample properties.
		 * Journal of Econometrics, 29, 53-57.
		 */
		double[] d = new double[nRows];
		for (int i = 0; i < nRows; i++)
			d[i] = (residuals[i] * residuals[i]) / ((1 - leverages[i]) * (1 - leverages[i]));
		double[][] xdx = new double[nD][nD];
		for (int j = 0; j < nD; j++) {
			for (int k = 0; k < nD; k++) {
				for (int i = 0; i < nRows; i++) {
					xdx[j][k] += xData[i][j] * d[i] * xData[i][k];
				}
			}
		}
		double[][] q = new double[nD][nD];
		for (int j = 0; j < nD; j++)
			LinearSystems.symsol(xx, xdx[j], q[j]);
		double[] r = new double[nD];
		for (int j = 0; j < nD; j++) {
			for (int k = 0; k < nD; k++)
				r[k] = q[k][j];
			LinearSystems.symsol(xx, r, covariances[j]);
			for (int m = 0; m < nD; m++) {
				if (Globals.isMissing(covariances[j][m])) {
					standardErrors = null;
					return;
				}
			}
		}
	}
}
