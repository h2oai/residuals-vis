/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util;

import com.h2o.online.Globals;

public class EigenSystems {

	private EigenSystems() {
	}

	public static void eigenSymmetric(double[][] a, double[][] eigenvectors, double[] eigenvalues) {
		/*
		 * eigenvalues and eigenvectors for dense system (A-lambda*I)X = 0
		 * returns eigenvectors in COLUMNS
		 * Leland Wilkinson
		 */
		double[] e = new double[eigenvalues.length];
		tred2(a, eigenvectors, eigenvalues, e);
		imtql2(eigenvectors, eigenvalues, e);
	}

	public static void eigenAsymmetric(double a[][], double[][] b, double[][] eigenvectors, double[] eigenvalues) {
		/*
		 * Asymmetric eigenproblem (A-lambda*B)V = 0.
		 * returns eigenvectors in COLUMNS
		 * Leland Wilkinson
		 */
		boolean singular;
		double[] d = new double[eigenvalues.length];
		double[] e = new double[eigenvalues.length];

		singular = reduc(a, b, d);
		if (singular)
			return;
		tred2(a, eigenvectors, eigenvalues, e);
		imtql2(eigenvectors, eigenvalues, e);
		rebak(b, d, eigenvectors);
	}

	private static void tred2(double[][] a, double[][] z, double[] d, double[] e) {
		/*
		 * Tridiagonalization of symmetric matrix. This generateMethod is a
		 * translation of the Algol procedure tred2(), Wilkinson and Reinsch,
		 * Handbook for Auto. Comp., Vol II-Linear Algebra, (1971).
		 * Converted to Java by Leland Wilkinson.
		 */

		int n = d.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++)
				z[i][j] = a[i][j];
		}
		if (n > 1) {
			for (int i = n - 1; i > 0; i--) {
				int im1 = i - 1;
				double h = 0.0;
				double scale = 0.0;
				if (im1 > 0) {
					for (int k = 0; k <= im1; k++)
						scale += Math.abs(z[i][k]);
				}
				if (scale == 0.0)
					e[i] = z[i][im1];
				else {
					for (int k = 0; k <= im1; k++) {
						z[i][k] /= scale;
						h += z[i][k] * z[i][k];
					}
					double f = z[i][im1];
					double g = f < 0.0 ? Math.sqrt(h) : -Math.sqrt(h);
					e[i] = scale * g;
					h -= f * g;
					z[i][im1] = f - g;
					f = 0.0;
					for (int j = 0; j <= im1; j++) {
						z[j][i] = z[i][j] / (scale * h);
						g = 0.0;
						for (int k = 0; k <= j; k++)
							g += z[j][k] * z[i][k];
						int jp1 = j + 1;
						if (im1 >= jp1) {
							for (int k = jp1; k <= im1; k++)
								g += z[k][j] * z[i][k];
						}
						e[j] = g / h;
						f += e[j] * z[i][j];
					}
					double hh = f / (h + h);
					for (int j = 0; j <= im1; j++) {
						f = z[i][j];
						g = e[j] - hh * f;
						e[j] = g;
						for (int k = 0; k <= j; k++)
							z[j][k] = z[j][k] - f * e[k] - g * z[i][k];
					}
					for (int k = 0; k <= im1; k++)
						z[i][k] *= scale;
				}
				d[i] = h;
			}
		}
		d[0] = 0.0;
		e[0] = 0.0;
		for (int i = 0; i < n; i++) {
			int im1 = i - 1;
			if (d[i] != 0.0) {
				for (int j = 0; j <= im1; j++) {
					double g = 0.0;
					for (int k = 0; k <= im1; k++)
						g += z[i][k] * z[k][j];
					for (int k = 0; k <= im1; k++)
						z[k][j] -= g * z[k][i];
				}
			}
			d[i] = z[i][i];
			z[i][i] = 1.0;
			if (im1 >= 0) {
				for (int j = 0; j <= im1; j++) {
					z[i][j] = 0.0;
					z[j][i] = 0.0;
				}
			}
		}
	}

	private static int imtql2(double[][] z, double[] d, double[] e) {
		/*
		 * Implicit QL iterations on tridiagonalized matrix. This method
		 * is a translation of the Algol procedure imtql2(), Wilkinson and
		 * Reinsch, Handbook for Auto. Comp., Vol II-Linear Algebra, (1971).
		 * Converted to Java by Leland Wilkinson.
		 */
		double EPSILON = 1.0e-15;
		int n = d.length;
		if (n == 1)
			return 0;
		for (int i = 1; i < n; i++)
			e[i - 1] = e[i];
		e[n - 1] = 0.0;
		int mm = 0;

		for (int l = 0; l < n; l++) {
			int lp1 = l + 1;
			int j = 0;
			for (;;) {
				for (int m = l; m < n; m++) {
					mm = m;
					if (m == n - 1)
						break;
					if (Math.abs(e[m]) <= EPSILON * (Math.abs(d[m]) + Math.abs(d[m + 1])))
						break;
				}
				int m = mm;
				double p = d[l];
				if (m == l)
					break;
				if (j == 30)
					return l;
				j++;
				double g = (d[lp1] - p) / (2.0 * e[l]);
				double r = Math.sqrt(g * g + 1.0);
				g = d[m] - p + e[l] / (g + (g < 0.0 ? -r : r));
				double s = 1.0;
				double c = 1.0;
				p = 0.0;
				int mml = m - l;
				for (int ii = 1; ii <= mml; ii++) {
					int i = m - ii;
					int ip1 = i + 1;
					double f = s * e[i];
					double b = c * e[i];
					if (Math.abs(f) >= Math.abs(g)) {
						c = g / f;
						r = Math.sqrt(c * c + 1.0);
						e[ip1] = f * r;
						s = 1.0 / r;
						c *= s;
					} else {
						s = f / g;
						r = Math.sqrt(s * s + 1.0);
						e[ip1] = g * r;
						c = 1.0 / r;
						s *= c;
					}
					g = d[ip1] - p;
					r = (d[i] - g) * s + 2.0 * c * b;
					p = s * r;
					d[ip1] = g + p;
					g = c * r - b;
					for (int k = 0; k < n; k++) {
						f = z[k][ip1];
						z[k][ip1] = s * z[k][i] + c * f;
						z[k][i] = c * z[k][i] - s * f;
					}
				}
				d[l] -= p;
				e[l] = g;
				e[m] = 0.0;
			}
		}
		eigsort(d, z, null);
		return 0;
	}

	private static final boolean reduc(double[][] a, double[][] b, double[] d) {
		/*
		 * Reduction of asymmetric eigensystem (A - lambda*B)x = 0. This method
		 * is a translation of the Algol procedure reduc(), Wilkinson and
		 * Reinsch, Handbook for Auto. Comp., Vol II-Linear Algebra, (1971).
		 * Converted to Java by Leland Wilkinson.
		 */
		double x = 0;
		double y = 0;
		double xl = Double.NEGATIVE_INFINITY;
		double xs = Double.POSITIVE_INFINITY;
		int n = a.length;
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				x = b[i][j];
				for (int k = i - 1; k >= 0; k--)
					x = x - b[i][k] * b[j][k];
				if (i == j) {
					if (x <= 0)
						return true;
					xs = Math.min(x, xs);
					xl = Math.max(x, xl);
					d[i] = Math.sqrt(x);
					y = d[i];
				} else {
					b[j][i] = x / y;
				}
			}
		}
		if (xl / xs > Globals.BIG)
			return true;
		for (int i = 0; i < n; i++) {
			y = d[i];
			for (int j = i; j < n; j++) {
				x = a[i][j];
				for (int k = i - 1; k >= 0; k--)
					x = x - b[i][k] * a[j][k];
				a[j][i] = x / y;
			}
		}
		for (int j = 0; j < n; j++) {
			for (int i = j; i < n; i++) {
				x = a[i][j];
				for (int k = i - 1; k >= j; k--)
					x = x - a[k][j] * b[i][k];
				for (int k = j - 1; k >= 0; k--)
					x = x - a[j][k] * b[i][k];
				a[i][j] = x / d[i];
			}
		}
		return false;
	}

	private static final void rebak(double[][] b, double[] d, double[][] z) {
		/*
		 * Back solution of asymmetric eigensystem (A - lambda*B)x = 0. This
		 * method is a translation of the Algol procedure rebak(), Wilkinson and
		 * Reinsch, Handbook for Auto. Comp., Vol II-Linear Algebra, (1971).
		 * Converted to Java by Leland Wilkinson July 1999.
		 */
		double x;
		int n = d.length;
		for (int j = 0; j < n; j++) {
			for (int i = n - 1; i >= 0; i--) {
				x = z[i][j];
				for (int k = i + 1; k < n; k++)
					x -= b[k][i] * z[k][j];
				z[i][j] = x / d[i];
			}
		}
	}

	public static final int svd(double[][] a, double[][] u, double[][] v, double[] d) {

		boolean cancel;

		/*
		 * This method is a translation of the Algol procedure svd,
		 * Num. Math. 14, 403-420(1970) by Golub and Reinsch.
		 * Handbook for Auto. Comp., Vol II-Linear Algebra, 134-151(1971).
		 * 
		 * This method determines the singular value decomposition
		 * a=usv' of a real m by n rectangular matrix. Householder
		 * bidiagonalization and a variant of the QR algorithm are used.
		 * 
		 * on input
		 * 
		 * a contains the rectangular input matrix to be decomposed.
		 * 
		 * on output
		 * 
		 * a is unaltered.
		 * 
		 * d contains the n (non-negative) singular values of a (the
		 * diagonal elements of s). If an
		 * error exit is made, the singular values should be correct
		 * for indices 1 .. rank.
		 * 
		 * u contains the matrix u (orthogonal column vectors) of the
		 * decomposition. If an error exit is made, the columns of u
		 * corresponding to indices of correct singular values should
		 * be correct.
		 * 
		 * v contains the matrix v (orthogonal) of the decomposition.
		 * If an error exit is made, the columns of v corresponding
		 * to indices of correct singular values should be correct.
		 * 
		 * calls pythag for sqrt(a*a + b*b) .
		 * 
		 * Questions and comments should be directed to Burton S. Garbow,
		 * Mathematics and Computer Science Div, Argonne National Laboratory
		 * 
		 * This version dated August 1983.
		 * 
		 * Converted to Java by Leland Wilkinson
		 * 
		 * ------------------------------------------------------------------
		 */

		int rank = 0;
		int m = a.length;
		int n = a[0].length;

		if (m < n)
			return 0;

		double[] rv1 = new double[n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++)
				u[i][j] = a[i][j];
		}
		/* .......... Householder reduction to bidiagonal form .......... */
		double g = 0.0;
		double scale = 0.0;
		double anorm = 0.0;
		double x = 0.0;
		int l = 0;

		for (int i = 0; i < n; i++) {
			l = i + 1;
			rv1[i] = scale * g;
			g = 0.0;
			double s = 0.0;
			scale = 0.0;
			if (i < m) {
				for (int k = i; k < m; k++)
					scale += Math.abs(u[k][i]);

				if (scale != 0.0) {
					for (int k = i; k < m; k++) {
						u[k][i] /= scale;
						s += u[k][i] * u[k][i];
					}

					double f = u[i][i];
					g = (f < 0.0 ? Math.sqrt(s) : -Math.sqrt(s));
					double h = f * g - s;
					u[i][i] = f - g;
					for (int j = l; j < n; j++) {
						s = 0.0;
						for (int k = i; k < m; k++)
							s += u[k][i] * u[k][j];
						f = s / h;
						for (int k = i; k < m; k++)
							u[k][j] += f * u[k][i];
					}
					for (int k = i; k < m; k++)
						u[k][i] *= scale;
				}
			}

			d[i] = scale * g;
			g = 0.0;
			s = 0.0;
			scale = 0.0;
			if (i < m && i < n - 1) {
				for (int k = l; k < n; k++)
					scale += Math.abs(u[i][k]);

				if (scale != 0.0) {
					for (int k = l; k < n; k++) {
						u[i][k] /= scale;
						s += u[i][k] * u[i][k];
					}

					double f = u[i][l];
					g = (f < 0.0 ? Math.sqrt(s) : -Math.sqrt(s));
					double h = f * g - s;
					u[i][l] = f - g;

					for (int k = l; k < n; k++)
						rv1[k] = u[i][k] / h;

					for (int j = l; j < m; j++) {
						s = 0.0;
						for (int k = l; k < n; k++)
							s += u[j][k] * u[i][k];
						for (int k = l; k < n; k++)
							u[j][k] += s * rv1[k];
					}
					for (int k = l; k < n; k++)
						u[i][k] *= scale;
				}
			}
			x = Math.max(x, Math.abs(d[i]) + Math.abs(rv1[i]));
			if (x > anorm)
				anorm = x;
		}

		/* .......... accumulation of right-hand transformations .......... */
		for (int i = n - 1; i >= 0; i--) {
			if (i < n - 1) {
				if (g != 0.0) {
					for (int j = l; j < n; j++) {
						/* .......... double division avoids possible underflow .......... */
						v[j][i] = (u[i][j] / u[i][l]) / g;
					}

					for (int j = l; j < n; j++) {
						double s = 0.0;

						for (int k = l; k < n; k++)
							s += u[i][k] * v[k][j];

						for (int k = l; k < n; k++)
							v[k][j] += s * v[k][i];
					}
				}
				for (int j = l; j < n; j++) {
					v[i][j] = 0.0;
					v[j][i] = 0.0;
				}
			}
			v[i][i] = 1.0;
			g = rv1[i];
			l = i;
		}

		/* .......... accumulation of left-hand transformations .......... */
		int mn = n;
		if (m < n)
			mn = m;

		for (int i = mn - 1; i >= 0; i--) {
			l = i + 1;
			g = d[i];
			for (int j = l; j < n; j++)
				u[i][j] = 0.0;

			if (g != 0.0) {
				for (int j = l; j < n; j++) {
					double s = 0.0;
					for (int k = l; k < m; k++)
						s += u[k][i] * u[k][j];
					double f = (s / u[i][i]) / g;
					for (int k = i; k < m; k++)
						u[k][j] += f * u[k][i];
				}
				for (int j = i; j < m; j++)
					u[j][i] /= g;
			} else {
				for (int j = i; j < m; j++)
					u[j][i] = 0.0;
			}

			u[i][i] += 1.0;
		}

		/* .......... diagonalization of the bidiagonal form .......... */
		int nm = 0;
		for (int k = n - 1; k >= 0; k--) {
			for (int its = 1; its <= 30; its++) {
				cancel = true;
				/*
				 * .......... test for splitting.
				 * for l=k step -1 until 1 do -- ..........
				 */
				for (l = k; l >= 0; l--) {
					nm = l - 1;
					if (Math.abs(rv1[l] + anorm) == anorm) {
						cancel = false;
						break;
					}
					if (Math.abs(d[nm] + anorm) == anorm) {
						break;
					}
				}

				/* .......... cancellation of rv1(l) if l greater than 1 .......... */
				if (cancel) {
					double c = 0.0;
					double s = 1.0;

					for (int i = l; i <= k; i++) {
						double f = s * rv1[i];
						rv1[i] = c * rv1[i];
						if (Math.abs(f + anorm) == anorm)
							break;

						g = d[i];
						double h = pythag(f, g);
						d[i] = h;
						c = g / h;
						s = -f / h;
						for (int j = 0; j < m; j++) {
							double y = u[j][nm];
							double z = u[j][i];
							u[j][nm] = y * c + z * s;
							u[j][i] = -y * s + z * c;
						}
					}
				}

				/* .......... test for convergence .......... */
				double z = d[k];
				if (l == k) {
					if (z < 0.0) {
						d[k] = -z;
						for (int j = 0; j < n; j++)
							v[j][k] = -v[j][k];
					}
					break; //  normal exit
				}
				if (its == 30) { // exceeded iterations
					break;
				}

				x = d[l];
				nm = k - 1;
				double y = d[nm];
				g = rv1[nm];
				double h = rv1[k];
				double f = 0.5 * (((g + z) / h) * ((g - z) / y) + y / h - h / y);
				g = pythag(f, 1.0);
				double tmp = (f < 0.0 ? -g : g);
				f = x - (z / x) * z + (h / x) * (y / (f + tmp) - h);

				/* .......... next qr transformation .......... */
				double c = 1.0;
				double s = 1.0;

				for (int j = l; j <= nm; j++) {
					int i = j + 1;
					g = rv1[i];
					y = d[i];
					h = s * g;
					g = c * g;
					z = pythag(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = -x * s + g * c;
					h = y * s;
					y = y * c;
					for (int jj = 0; jj < n; jj++) {
						x = v[jj][j];
						z = v[jj][i];
						v[jj][j] = x * c + z * s;
						v[jj][i] = -x * s + z * c;
					}
					z = pythag(f, h);
					d[j] = z;
					/* .......... rotation can be arbitrary if z is zero .......... */
					if (z != 0.0) {
						c = f / z;
						s = h / z;
					}
					f = c * g + s * y;
					x = -s * g + c * y;
					for (int jj = 0; jj < m; jj++) {
						y = u[jj][j];
						z = u[jj][i];
						u[jj][j] = y * c + z * s;
						u[jj][i] = -y * s + z * c;
					}
				}

				rv1[l] = 0.0;
				rv1[k] = f;
				d[k] = x;
			}
		}

		eigsort(d, u, v);

		/* compute rank */
		rank = 0;
		for (int i = 0; i < n; i++) {
			if (d[i] < Globals.EPSILON)
				d[i] = 0.;
			if (d[i] > 0.)
				rank++;
		}

		return (rank);
	}

	private static final double pythag(double a, double b) {

		/*
		 * Pythagorean theorem with overflow protection.
		 * Finds sqrt(a^2+b^2) without overflow or destructive underflow.
		 * Converted to Java by Leland Wilkinson.
		 */

		double p, r, s, t, u;

		p = Math.max(Math.abs(a), Math.abs(b));
		if (p != 0.0) {
			r = Math.pow(Math.min(Math.abs(a), Math.abs(b)) / p, 2);
			for (;;) {
				t = 4.0 + r;
				if (t == 4.0)
					break;
				s = r / t;
				u = 1.0 + 2.0 * s;
				p = u * p;
				r = Math.pow(s / u, 2) * r;
			}
		}
		return (p);
	}

	private static final void eigsort(double[] a, double[][] u, double[][] v) {
		/*
		 * Sorting by eigenvalues (descending)
		 * Leland Wilkinson
		 */
		int l;
		int n = a.length;
		for (l = 1; l <= n; l = 3 * l + 1)
			;
		while (l > 2) {
			l = l / 3;
			int k = n - l;
			for (int j = 0; j < k; j++) {
				int i = j;
				while (i >= 0) {
					int ip1 = i + l;
					if (a[i] < a[ip1] || Globals.isMissing(a[i])) {
						double p = a[i];
						a[i] = a[ip1];
						a[ip1] = p;
						if (u != null) {
							for (int ii = 0; ii < n; ii++) {
								p = u[ii][i];
								u[ii][i] = u[ii][ip1];
								u[ii][ip1] = p;
							}
						}
						if (v != null) {
							for (int ii = 0; ii < n; ii++) {
								p = v[ii][i];
								v[ii][i] = v[ii][ip1];
								v[ii][ip1] = p;
							}
						}
						i = i - l;
					} else
						break;
				}
			}
		}
	}
}
