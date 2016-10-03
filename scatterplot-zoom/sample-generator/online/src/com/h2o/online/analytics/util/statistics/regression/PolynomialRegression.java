/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics.regression;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.statistics.LinearSystems;
import com.h2o.online.analytics.util.statistics.Probabilities;
import com.h2o.online.analytics.util.statistics.Statistics;

public class PolynomialRegression extends Regression {
	/*
	 * Polynomial regression
	 */
	@Override
	public void compute(double[][] data, double[] observed, double[] weights, int model) {
	}

	@Override
	public void compute(double[][] data, double[] weight, int iRow1, int iRow2, int degree, int nDim, int model) {
		/*
		 * Weighted least squares polynomial fit for 2D or 3D model.
		 * This algorithm uses symdet and symsol to solve linear system.
		 * This procedure may go singular for most data after degree=10.
		 * Listwise deletion used (all complete cases).
		 * This procedure returns matrix of coefficients.
		 * For POLYNOMIAL, model (plus error) is:
		 * 2D: y = b0 + b1*x + b2*x^2 + b3*x^3 + ...
		 * 3D: z = b0 + b1*x + b2*y + b3*x^2 + b4*y^2 + b5*x^3 + b6*y^3 + ...
		 * For LOG, model is:
		 * 2D: y = b0 + b1*log(x)
		 * For POWER, model is:
		 * 2D: log(y) = log(b0) + b1*log(x)
		 * Data are stored columnwise.
		 * Dependent variable y is last column of data, namely data[data.length-1][].
		 * Predictors are remaining columns.
		 */
		boolean complete;
		int pivot;

		double wt, sumwt, ymean, xd, yd, di, dk, yi;

		nRows = data[0].length;
		if (iRow2 == 0)
			iRow2 = nRows;
		int nTerms = 1 + nDim * degree;

		if (nTerms < 1)
			return;

		double[] xval = new double[nTerms];
		xmean = new double[nTerms];
		double[] xss = new double[nTerms];
		xx = new double[nTerms][nTerms];
		double[][] backup = new double[nTerms][nTerms];
		xy = new double[nTerms];

		ymean = 0.0;
		sumwt = 0.0;
		wt = 1.0;
		for (int i = iRow1; i < iRow2; i++) {
			if (weight != null)
				wt = weight[i];
			complete = wt > 0;
			for (int j = 0; j < data.length; j++) {
				if (Globals.isMissing(data[j][i]))
					complete = false;
				if ((model == LOG || model == POWER) && data[j][i] <= 0)
					complete = false;
			}

			if (complete) {
				di = data[nDim][i];
				if (model == POWER)
					di = Math.log(di);
				sumwt += wt;
				yi = di;
				yd = wt * (yi - ymean);
				ymean += yd / sumwt;
				sst += (yi - ymean) * yd;

				int m = 1;
				for (int j = 0; j < degree; j++) {
					for (int k = 0; k < nDim; k++) {
						dk = data[k][i];
						if (model == LOG || model == POWER)
							dk = Math.log(dk);
						xval[m] = Math.pow(dk, j + 1);
						m++;
					}
				}

				for (m = 1; m < nTerms; m++) {
					xd = wt * (xval[m] - xmean[m]);
					xmean[m] += xd / sumwt;
					xy[m] += xd * (yi - ymean);
					for (int n = 1; n <= m; n++)
						xx[n][m] += xd * (xval[n] - xmean[n]);
					xss[m] += xd * (xval[m] - xmean[m]);
				}
			}
		}
		xx[0][0] = sumwt;
		coefficients = new double[nTerms];

		if (degree > 0) {
			for (int i = 0; i < nTerms; i++)
				System.arraycopy(xx[i], 0, backup[i], 0, nTerms);

			pivot = 0;
			while (pivot >= 0) {
				pivot = LinearSystems.symdet(xx);

				// IF SINGULAR, REDUCE MODEL BY ELIMINATING HIGHER-ORDER TERMS
				if (pivot >= 0) {
					nTerms--;
					if (nTerms == 0)
						return;
					else
						for (int i = 0; i < nTerms; i++)
							System.arraycopy(backup[i], 0, xx[i], 0, nTerms);
				}
			}

			// FIX MEANS IN DECOMPOSED MATRIX

			for (int j = 1; j < nTerms; j++)
				xx[j][0] = xmean[j];

			LinearSystems.symsol(xx, xy, coefficients);
		}

		coefficients[0] = ymean;
		for (int j = 1; j < nTerms; j++)
			coefficients[0] -= xmean[j] * coefficients[j];

		computeResiduals(data, nDim, model, degree);
		computeAssociatedStatistics(degree, nDim);
		return;
	}

	private void computeResiduals(double[][] data, int nDim, int model, int degree) {
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
			boolean complete = true;
			double[] xval = new double[nDim];
			for (int j = 0; j <= nDim; j++) {
				if (Globals.isMissing(wt) || wt <= 0 || Globals.isMissing(data[j][i]))
					complete = false;
				if ((model == LOG || model == POWER) && data[j][i] <= 0)
					complete = false;
				if (j < nDim)
					xval[j] = data[j][i];
			}

			if (complete) {
				double xi = data[0][i];
				double zi = xi;
				if (nDim > 1)
					zi = data[1][i];
				double yi = data[nDim][i];
				observed[i] = yi;
				if (model == POWER)
					yi = Math.log(yi);
				double yhat = prediction(xi, zi, degree, nDim, model);
				double residual = yi - yhat;
				residuals[i] = residual;
				estimates[i] = yhat;
				dfe++;
				sse += wt * residual * residual;
				//				leverages[i] = computeLeverage(xval);
			}
		}
		orders = Statistics.orderStatistics(residuals, Probabilities.NORMAL, 0, 1, 0);
	}

	private void computeAssociatedStatistics(int degree, int nDim) {
		/* data stored columnwise */

		if (coefficients == null)
			return;

		int nTerms = 1 + nDim * degree;
		ssr = sst - sse;
		dfr = nD - 1;
		msr = ssr / dfr;
		dfe = dfe - nTerms;
		mse = sse / dfe;
		rsq = ssr / sst;
		fstat = msr / mse;
		standardErrors = new double[nTerms];
		tStatistics = new double[nTerms];
		probabilities = new double[nTerms];
		double[] se = new double[nTerms];
		for (int j = 0; j < nTerms; j++) {
			for (int i = 0; i < nTerms; i++) {
				xy[i] = 0;
				if (i == j)
					xy[i] = 1;
			}
			LinearSystems.symsol(xx, xy, se);
			standardErrors[j] = Math.sqrt(se[j] * mse);
			tStatistics[j] = coefficients[j] / standardErrors[j];
			probabilities[j] = 1 - Probabilities.cdf(tStatistics[j] * tStatistics[j], 1, dfe, Probabilities.F, false);
		}
		isSignificant = Probabilities.isSignificantBenjaminiHochberg(probabilities);
	}
}
