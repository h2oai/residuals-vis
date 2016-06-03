/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics.regression;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.Nonparametrics;

public class RobustRegression extends Regression {
	/*
	 * Robust Iteratively Reweighted Least Squares
	 */
	@Override
	public void compute(double[][] data, double[] observed, double[] weights, int model) {
	}

	@Override
	public void compute(double[][] data, double[] weight, int iRow1, int iRow2, int degree, int nDim, int method) {
		/*
		 * Computes robust regression coefficients with IRWLS from data matrix.
		 */
		boolean finished;
		int i, i0, iter, k, m1, m2, nm, nRow, nr, nx, maxit, iCol;
		double am, bk, p, ps, q, s, t, wi, wti;

		PolynomialRegression poly = new PolynomialRegression();
		int nCol = data.length;
		iCol = nCol - 1;
		maxit = 10;
		nRow = data[0].length;
		nr = iRow2 - iRow1;
		nx = 1 + (nCol - 1) * degree;
		if (nr <= nx || nr <= 3)
			return;

		double[] wb = new double[nx];
		double[] w = new double[nRow];
		double[] r = new double[nr];
		double[] ar = new double[nr];

		iter = 0;
		am = 0;
		nm = Nonparametrics.freqN(data[iCol], weight, iRow1, iRow2);
		m1 = (nm + 1) / 2;
		m2 = nm - m1 + 1;
		wti = 1.;
		finished = false;
		for (i = 0; i < nRow; i++)
			w[i] = 1.;

		while (!finished) {
			iter++;
			if (iter > maxit)
				return;

			poly.compute(data, w, iRow1, iRow2, degree, nDim, LINEAR);

			t = 0.;
			s = 0.;
			for (k = 0; k < nx; k++) {
				bk = coefficients[k];
				s += Math.abs(bk);
				t += Math.abs(bk - wb[k]);
				wb[k] = bk;
			}
			if (t < Globals.FUZZ * s)
				finished = true;

			i0 = 0;
			for (i = iRow1; i < iRow2; i++) {
				s = prediction(data[0][i], data[iCol - 1][i], degree, nDim, LINEAR);
				r[i0] = data[iCol][i] - s;
				i0++;
			}

			if (iter == 1) {
				System.arraycopy(r, 0, ar, 0, nr);
				Sorts.doubleArraySort(ar, 0, nr);
				am = .5 * (ar[m1 - 1] + ar[m2 - 1]);

				for (i = 0; i < nr; i++)
					ar[i] = Math.abs(r[i] - am);

				Sorts.doubleArraySort(ar, 0, nr);
				am = .5 * (ar[m1 - 1] + ar[m2 - 1]);

				if (am < Globals.FUZZ)
					return;
				am = .6745 / am;
			}

			i0 = 0;
			for (i = iRow1; i < iRow2; i++) {
				p = r[i0] * am;
				q = Math.abs(p);
				wi = 0.;

				if (method == ANDREWS) {
					if (q < 4.207) {
						if (q < Globals.FUZZ)
							wi = 1.;
						ps = p / 1.339;
						if (q > Globals.FUZZ)
							wi = Math.sin(ps) / ps;
					}
				} else if (method == BIWEIGHT) {
					if (q < 4.685) {
						ps = p / 4.685;
						wi = 1. - ps * ps;
						wi = wi * wi;
					}
				} else if (method == CAUCHY) {
					ps = p / 2.385;
					wi = 1. / (1. + ps * ps);
				} else if (method == FAIR) {
					wi = 1. / (1. + q / 1.4);
				} else if (method == HUBER) {
					wi = 1.;
					if (q > 1.345)
						wi = 1.345 / q;
				} else if (method == LOGISTIC) {
					ps = p / 1.205;
					if (ps < Globals.FUZZ)
						wi = 1.;
					else if (ps > Globals.FUZZ) {
						s = Math.exp(ps);
						t = Math.exp(-ps);
						wi = (s - t) / (s + t) / ps; // wi = tanh(ps)/ps
					}
				} else if (method == TALWAR) {
					if (q <= 2.795)
						wi = 1.;
				} else if (method == WELSCH) {
					ps = p / 2.985;
					ps = ps * ps;
					if (ps > 30.)
						wi = 0.;
					if (ps <= 30.)
						wi = Math.exp(-ps);
				}

				if (weight != null)
					wti = weight[i];
				w[i] = wi * wti;
				i0++;
			}
		}
	}
}
