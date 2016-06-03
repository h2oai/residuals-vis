/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.EigenSystems;
import com.h2o.online.analytics.util.Matrices;

public class LinearSystems {
	private static double[] diag;

	private LinearSystems() {
	}

	public static int symdet(double[][] a) {
		/*
		 * LDL' Cholesky decomposition of symmetric matrix. Upper half of matrix is input. This remains unaltered. Lower
		 * half of
		 * matrix is output. Algorithm adapted from Wilkinson and Reinsch Algol routine SYMDET. Use symsol() to solve
		 * sets of
		 * linear equations.
		 */
		int n = a.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++) {
				double x = a[j][i];
				if (i == j) {
					for (int k = j - 1; k >= 0; k--) {
						double y = a[i][k];
						double z = y * a[k][k];
						a[i][k] = z;
						x -= y * z;
					}
					if (x < Globals.EPSILON)
						return i;
					a[i][i] = 1.0 / x;
				} else {
					for (int k = j - 1; k >= 0; k--)
						x -= a[i][k] * a[j][k];
					a[i][j] = x;
				}
			}
		}
		return -1;
	}

	public static void symsol(double[][] a, double[] b, double[] x) {
		/*
		 * Solution to linear system Ax = b using decomposed matrix a. This algorithm adapted from Wilkinson and Reinsch
		 * Algol
		 * routine SYMSOL. Use symdet() to decompose matrix a. High accuracy: no square roots on diagonal
		 */
		int n = a.length;
		for (int i = 0; i < n; i++) {
			double y = b[i];
			for (int k = i - 1; k >= 0; k--)
				y -= a[i][k] * x[k];
			x[i] = y;
		}
		for (int i = n - 1; i >= 0; i--) {
			double y = x[i] * a[i][i];
			for (int k = i + 1; k < n; k++)
				y -= a[k][i] * x[k];
			x[i] = y;
		}
	}

	public static double[] trisol(double a[][], double b[]) {
		/* Solution to linear system Ax = b, where A is lower triangular */
		int n = b.length;
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			if (Globals.isMissing(b[i])) {
				x[i] = Globals.MISSING_VALUE;
			} else {
				double sum = 0.0;
				for (int j = 0; j < i; j++) {
					if (!Globals.isMissing(x[j]))
						sum += a[i][j] * x[j];
				}
				x[i] = (b[i] - sum) / a[i][i];
			}
		}
		return x;

	}

	public static double inverse(double[][] a) {
		/*
		 * Inversion of symmetric matrix using elementary row and column operations. Use only when inverse is required.
		 * Use SYMDET
		 * and SYMSOL for solving linear systems. Returns determinant (if d <= 0, inversion failed). This method is
		 * in-place
		 * (destructive) on a.
		 */
		double determinant = 1.0;
		int n = a.length;
		double[][] w = new double[n][n];

		if (a[0][0] == 0.)
			return 0.;

		for (int i = 0; i < n; i++) {
			for (int k = 0; k < n - 1; k++)
				w[k][0] = a[k + 1][0] / a[0][0];
			w[n - 1][0] = 1.0 / a[0][0];

			determinant *= a[0][0];

			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					if (j < n - 1) {
						if (k < n - 1)
							w[k][j + 1] = a[k + 1][j + 1] - a[0][j + 1] * w[k][0];
						else
							w[k][j + 1] = -a[0][j + 1] * w[k][0];
					}
				}
			}

			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					if (j < n - 1)
						a[k][j] = w[k][j + 1];
					else
						a[k][j] = w[k][0];
				}
			}
		}

		return determinant;
	}

	public static double[][] cholesky(double[][] a) {
		/* Lower triangular Cholesky decomposition with square roots on diagonal */
		int n = a.length;
		double[][] x = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++) {
				double sum = 0.0;
				for (int k = 0; k < j; k++)
					sum += x[i][k] * x[j][k];
				if (i == j)
					x[i][i] = Math.sqrt(a[i][i] - sum);
				else
					x[i][j] = 1.0 / x[j][j] * (a[i][j] - sum);
			}
		}
		return x;

	}

	public static double pivot(int n, double[] a, int[] p) {
		/*
		 * Pivot a symmetric matrix in lower-triangular storage mode, with tolerance checks.
		 * 
		 * K.N. Berk (1977). Tolerance and condition in regression computations. Journal of the American Statistical
		 * Association,
		 * 72, 360, 863-866.
		 * 
		 * a[n*(n+1)/2] is lower-triangular matrix to pivot (including diagonal) One row/col of a (usually the last) is
		 * dependent
		 * variable p[n] is pivot vector for diagonal elements of a (1=pivot, 0=do not, 2=dependent var) n is number of
		 * rows/cols
		 * in a
		 * 
		 * Returns the log of the determinant. This method is in-place (destructive) on a.
		 */
		int iv = -1; // index of dependent diagonal element in v
		int ia = -1; // index of first element of dependent row in a
		int nPivots = 0;
		double detlog = 0;
		double[] u = new double[n]; // temporary array
		double[] v = new double[n]; // temporary array
		double tol = Globals.FUZZ;

		int ii = -1;
		for (int i = 0; i < n; i++) {
			ii += i + 1;
			v[i] = a[ii];
			if (Math.abs(v[i]) < Globals.EPSILON)
				v[i] = 0;
			if (p[i] != 0) {
				nPivots++;
				if (p[i] == 2) {
					nPivots--;
					v[i] = 0;
					iv = i;
					ia = ii - i;
				}
			}
		}
		if (nPivots == 0)
			nPivots = -1;

		int k;
		int m;
		int mm;
		int im;
		double q;
		while (true) {
			k = -1;
			ii = -1;
			im = -1;
			m = 1;
			mm = ia - 1;
			q = 0;
			boolean checkTolerance = true;

			/* find the next diagonal element to pivot */
			for (int i = 0; i < n; i++) {
				ii += i + 1;
				mm += m;
				if (i >= iv)
					m = i + 1;
				if (p[i] * v[i] == 0 || p[i] >= 2)
					continue;
				if (p[i] >= 0) {
					double h = Math.abs(a[ii] / v[i]);
					if (h < tol)
						continue;
					if (iv >= 0)
						h = a[mm] * a[mm] / a[ii];
					if (h < q)
						continue;
					q = h;
				}
				k = i;
				im = ii - i - 1;
				if (p[i] < 0) {
					checkTolerance = false;
					break;
				}
			}

			/* check tolerances */
			boolean skipPivot = false;
			if (checkTolerance) {
				if (k < 0)
					break;
				ii = -1;
				int kdiag = (k + 1) * (k + 2) / 2 - 1;
				for (int i = 0; i < n; i++) {
					ii += i + 1;
					if (p[i] == 2 || v[i] == 0 || a[ii] >= 0)
						continue;
					int j = Math.max(k, i);
					j = (j + 1) * (j - 2) / 2 + k + i + 1;
					q = (a[ii] - a[j]) * (a[ii] - a[j]) / a[kdiag];
					if (q * v[i] > -1 / tol)
						continue;
					p[k] = 3;
					skipPivot = true;
					break;
				}
			}
			if (skipPivot)
				continue;

			/* pivot */
			nPivots--;
			m = 1;
			for (int i = 0; i < n; i++) {
				im += m;
				if (i >= k)
					m = i + 1;
				u[i] = a[im];
				a[im] = 0;
			}
			double b = u[k];
			if (p[k] > 0 && b != 0)
				detlog += Math.log(Math.abs(b));
			u[k] = -p[k];
			p[k] = 0;
			int mr = -1;
			double am = 0;
			for (int i = 0; i < n; i++) {
				double y = u[i] / b;
				if (Math.abs(y) < Globals.EPSILON)
					y = 0;
				for (int j = 0; j <= i; j++) {
					mr++;
					am = a[mr];
					a[mr] -= y * u[j];
				}
				if (i != k && v[i] != 0 && am * a[mr] <= 0) {
					a[mr] = Math.abs(v[i] * tol / 2);
					if (am < 0)
						a[mr] = -a[mr];
				}
			}
		}

		/* finished */
		for (int i = 0; i < n; i++) {
			if (p[i] == 3)
				p[i] = 1;
		}
		if (detlog == 0)
			return Globals.MISSING_VALUE;
		else
			return detlog;
	}

	public static double[][] positiveDefinite(double[][] x) {
		/*
		 * Return positive definite version of singular matrix by setting zero or negative eigenvalues to FUZZ
		 * Leland Wilkinson
		 */
		int n = x.length;
		double[][] eigvec = new double[n][n];
		double[] eigval = new double[n];
		EigenSystems.eigenSymmetric(x, eigvec, eigval);
		if (eigval[n - 1] > Globals.FUZZ)
			return x;

		double[][] xp = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					double lambda = Math.max(eigval[k], Globals.FUZZ);
					xp[i][j] += lambda * eigvec[i][k] * eigvec[j][k];
				}
			}
		}
		return xp;
	}

	public static double[] gensol(double[][] A, double[] b) {
		/*
		 * Least squares solution of Ax = b, where A is rectangular real matrix
		 * Uses QR decomposition
		 */
		int m = A.length;
		int n = A[0].length;
		if (b.length != m)
			return null;

		double[][] QR = QRDecomposition(A);
		if (QR == null)
			return null;

		double[] x = new double[m];
		System.arraycopy(b, 0, x, 0, m);

		/* Compute Y = Q'b */
		for (int k = 0; k < n; k++) {
			double s = 0.0;
			for (int i = k; i < m; i++) {
				s += QR[i][k] * x[i];
			}
			s = -s / QR[k][k];
			for (int i = k; i < m; i++) {
				x[i] += s * QR[i][k];
			}
		}
		/* Solve Rx = y; */
		for (int k = n - 1; k >= 0; k--) {
			x[k] /= diag[k];
			for (int i = 0; i < k; i++) {
				x[i] -= x[k] * QR[i][k];
			}
		}
		if (n < m) {
			double[] xb = new double[n];
			System.arraycopy(x, 0, xb, 0, n);
			return xb;
		}
		return x;
	}

	private static double[][] QRDecomposition(double[][] A) {
		double[][] QR = Matrices.copy(A);
		int m = A.length;
		int n = A[0].length;
		diag = new double[n];

		for (int k = 0; k < n; k++) {
			double nrm = 0;
			for (int i = k; i < m; i++) {
				nrm = Matrices.pythag(nrm, QR[i][k]);
			}

			if (nrm != 0.0) {
				// Form k-th Householder vector.
				if (QR[k][k] < 0) {
					nrm = -nrm;
				}
				for (int i = k; i < m; i++) {
					QR[i][k] /= nrm;
				}
				QR[k][k] += 1.0;

				// Apply transformation to remaining columns.
				for (int j = k + 1; j < n; j++) {
					double s = 0.0;
					for (int i = k; i < m; i++) {
						s += QR[i][k] * QR[i][j];
					}
					s = -s / QR[k][k];
					for (int i = k; i < m; i++) {
						QR[i][j] += s * QR[i][k];
					}
				}
			}
			diag[k] = -nrm;
		}
		for (int j = 0; j < n; j++) {
			if (diag[j] == 0)
				return null;
		}
		return QR;
	}
}
