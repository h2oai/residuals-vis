/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util;

import java.util.Random;

import com.h2o.online.Globals;

public class Matrices {

	public static double[][] transpose(double[][] a) {
		if (a == null)
			return null;
		double[][] z = new double[a[0].length][a.length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				z[j][i] = a[i][j];
			}
		}
		return z;
	}

	public static double[] negate(double[] a) {
		if (a == null)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = -a[i];
		}
		return z;
	}

	public static double[][] negate(double[][] a) {
		if (a == null)
			return null;
		double[][] z = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				z[i][j] = -a[i][j];
			}
		}
		return z;
	}

	public static double[] matVec(double[][] a) {
		if (a == null)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = a[i][0];
		}
		return z;
	}

	public static double[][] vecMat(double[] a) {
		if (a == null)
			return null;
		double[][] z = new double[a.length][1];
		for (int i = 0; i < a.length; i++) {
			z[i][0] = a[i];
		}
		return z;
	}

	public static double[][] product(double[][] a, double[][] b) {
		if (a == null || b == null || a[0].length != b.length)
			return null;
		double[][] z = new double[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				double sum = 0.0;
				for (int k = 0; k < a[0].length; k++) {
					sum += a[i][k] * b[k][j];
				}
				z[i][j] = sum;
			}
		}
		return z;
	}

	public static double[] product(double[][] a, double[] b) {
		if (a == null || b == null || a[0].length != b.length)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			double sum = 0.0;
			for (int j = 0; j < b.length; j++) {
				sum += a[i][j] * b[j];
			}
			z[i] = sum;
		}
		return z;
	}

	public static double product(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length)
			return Globals.MISSING_VALUE;
		double z = 0;
		for (int i = 0; i < a.length; i++) {
			z += a[i] * b[i];
		}
		return z;
	}

	public static double[][] outerProduct(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		int n = a.length;
		double[][] ab = new double[n][n];
		for (int j = 0; j < n; j++) {
			for (int k = 0; k < n; k++)
				ab[j][k] = a[j] * b[k];
		}
		return ab;
	}

	public static int[][] product(int[][] a, int[][] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		int[][] z = new int[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				int sum = 0;
				for (int k = 0; k < a[0].length; k++) {
					sum += a[i][k] * b[k][j];
				}
				z[i][j] = sum;
			}
		}
		return z;
	}

	public static double[] product(double[] a, double[][] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		double[] z = new double[b[0].length];
		for (int j = 0; j < b[0].length; j++) {
			for (int i = 0; i < a.length; i++) {
				z[j] += a[i] * b[i][j];
			}
		}
		return z;
	}

	public static double[][] product(double s, double[][] a) {
		if (a == null)
			return null;
		double[][] z = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				z[i][j] = s * a[i][j];
			}
		}
		return z;
	}

	public static double[] product(double s, double[] a) {
		if (a == null)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = s * a[i];
		}
		return z;
	}

	public static double[] elementwiseProduct(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		double z[] = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = a[i] * b[i];
		}
		return z;
	}

	public static double[] elementwiseDivide(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = a[i] / b[i];
		}
		return z;
	}

	public static double[][] elementwiseDivide(double a, double[][] b) {
		if (b == null)
			return null;
		double[][] z = new double[b.length][b[0].length];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				z[i][j] = a / b[i][j];
			}
		}
		return z;
	}

	public static double[] elementwiseDivide(double[] a, double b) {
		if (a == null)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = a[i] / b;
		}
		return z;
	}

	public static double[] elementwiseDivide(double a, double[] b) {
		if (b == null)
			return null;
		double[] z = new double[b.length];
		for (int i = 0; i < b.length; i++) {
			z[i] = a / b[i];
		}
		return z;
	}

	public static double[][] sum(double[][] a, double[][] b) {
		if (a == null || b == null || a.length != b.length || a[0].length != b[0].length)
			return null;
		double[][] z = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				z[i][j] = a[i][j] + b[i][j];
			}
		}
		return z;
	}

	public static double[] sum(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = a[i] + b[i];
		}
		return z;
	}

	public static double[] sum(double a, double[] b) {
		if (b == null)
			return null;
		double[] z = new double[b.length];
		for (int i = 0; i < b.length; i++) {
			z[i] = a + b[i];
		}
		return z;
	}

	public static double[] difference(double[] a, double[] b) {
		if (a == null || b == null || a.length != b.length)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = a[i] - b[i];
		}
		return z;
	}

	public static double[][] difference(double[][] a, double[][] b) {
		if (a == null || b == null || a.length != b.length || a[0].length != b[0].length)
			return null;
		double[][] z = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				z[i][j] = a[i][j] + b[i][j];
			}
		}
		return z;
	}

	public static double[] min(double[] a, double[] b) {
		if (a == null || b == null)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			z[i] = Math.min(a[i], b[i]);
		}
		return z;
	}

	public static double min(double[] a) {
		if (a == null)
			return Globals.MISSING_VALUE;
		double z = a[0];
		for (int i = 1; i < a.length; i++) {
			z = Math.min(z, a[i]);
		}
		return z;
	}

	public static double[] sqrt(double[] a) {
		if (a == null)
			return null;
		double[] z = new double[a.length];
		for (int i = 0; i < a.length; i++)
			z[i] = Math.sqrt(a[i]);
		return z;
	}

	public static double[][] diag(double[] a) {
		if (a == null)
			return null;
		int n = a.length;
		double[][] z = new double[n][n];
		for (int i = 0; i < n; i++)
			z[i][i] = a[i];
		return z;
	}

	public static double norm(double[] x) {
		/* excludes missing values */
		if (x == null)
			return Globals.MISSING_VALUE;
		double f = 0;
		for (int j = 0; j < x.length; j++) {
			if (!Globals.isMissing(x[j]))
				f = pythag(f, x[j]);
		}
		return f;
	}

	public static double[] norm(double[][] x) {
		/* excludes missing values */
		if (x == null)
			return null;
		double[] z = new double[x[0].length];
		for (int j = 0; j < x[0].length; j++) {
			for (int i = 0; i < x.length; i++) {
				if (!Globals.isMissing(x[i][j]))
					z[j] = pythag(z[j], x[i][j]);
			}
		}
		return z;
	}

	public static double[] unitize(double[] x) {
		if (x == null)
			return null;
		double[] z = new double[x.length];
		double f = norm(x);
		for (int j = 0; j < x.length; j++)
			z[j] = x[j] / f;
		return z;
	}

	public static double[] getColumnWithLargestNorm(double[][] x) {
		double[] norms = norm(x);
		double max = 0;
		int jmax = 0;
		for (int j = 0; j < norms.length; j++) {
			if (norms[j] > max) {
				max = norms[j];
				jmax = j;
			}
		}
		double[] z = new double[x.length];
		for (int i = 0; i < x.length; i++)
			z[i] = x[i][jmax];
		return z;
	}

	public static double pythag(double a, double b) {
		/* sqrt(a^2 + b^2) without under/overflow. */
		double r;
		if (Math.abs(a) > Math.abs(b)) {
			r = b / a;
			r = Math.abs(a) * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = Math.abs(b) * Math.sqrt(1 + r * r);
		} else {
			r = 0;
		}
		return r;
	}

	public static double quadraticForm(double[] a, double[][] b) {
		double[] z = new double[b.length];
		for (int j = 0; j < b[0].length; j++) {
			for (int k = 0; k < b.length; k++) {
				z[j] += a[k] * b[k][j];
			}
		}
		double result = 0;
		for (int j = 0; j < z.length; j++)
			result += a[j] * z[j];
		return result;
	}

	public static double[] copy(double[] a) {
		if (a == null)
			return null;
		double[] z = new double[a.length];
		System.arraycopy(a, 0, z, 0, a.length);
		return z;
	}

	public static double[][] copy(double[][] a) {
		if (a == null)
			return null;
		double[][] z = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				z[i][j] = a[i][j];
			}
		}
		return z;
	}

	public static double[][] copy(double[][] a, int cols) {
		if (a == null)
			return null;
		double[][] z = new double[a.length][cols];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < cols; j++) {
				z[i][j] = a[i][j];
			}
		}
		return z;
	}

	public static void replaceInfinityWithNaN(double[][] s) {
		for (int i = 1; i < s.length; i++) {
			for (int j = 0; j < i; j++) {
				if (Double.isInfinite(s[i][j]))
					s[i][j] = Globals.MISSING_VALUE;
				s[j][i] = s[i][j];
			}
		}
	}

	public static void replaceInfinityWithDoubleMaxValue(double[][] xx) {
		double maxValue = 0;
		for (int i = 0; i < xx.length; i++) {
			for (int j = 0; j < xx.length; j++) {
				if (!Globals.isMissing(xx[i][j]) && !Double.isInfinite(xx[i][j]))
					maxValue = Math.max(xx[i][j], maxValue);
			}
		}
		for (int i = 0; i < xx.length; i++) {
			for (int j = 0; j < xx.length; j++) {
				if (i != j) {
					if (Globals.isMissing(xx[i][j]) || Double.isInfinite(xx[i][j]))
						xx[i][j] = 2.0 * maxValue;
				}
			}
			xx[i][i] = 0;
		}
	}

	public static void transformSimilaritiesToDistances(double[][] xx) {
		double maxValue = 0;
		for (int i = 0; i < xx.length; i++) {
			for (int j = 0; j < xx.length; j++) {
				if (!Globals.isMissing(xx[i][j]) && !Double.isInfinite(xx[i][j]))
					maxValue = Math.max(xx[i][j], maxValue);
			}
		}
		for (int i = 0; i < xx.length; i++) {
			for (int j = 0; j < xx.length; j++) {
				if (i != j) {
					if (Globals.isMissing(xx[i][j]))
						xx[i][j] = maxValue;
					else if (Double.isInfinite(xx[i][j]))
						xx[i][j] = 0;
					else
						xx[i][j] = maxValue - xx[i][j];
				}
			}
			xx[i][i] = 0;
		}
	}

	public static void replaceColumn(double[][] x, double[] y, int col) {
		if (x == null || y == null || x.length != y.length)
			return;
		for (int i = 0; i < x.length; i++)
			x[i][col] = y[i];
	}

	public static double[][] identityMatrix(int n) {
		double[][] x = new double[n][n];
		for (int i = 0; i < n; i++)
			x[i][i] = 1;
		return x;
	}

	public static double[][] inverse(double[][] x) {
		/*
		 * Inversion of symmetric matrix using elementary row and column operations. Use only when inverse is required.
		 * Use SYMDET
		 * and SYMSOL for solving linear systems. Returns null if determinant <= 0.
		 */
		double determinant = 1.0;
		int n = x.length;
		double[][] a = new double[n][n];
		for (int i = 0; i < n; i++)
			System.arraycopy(x[i], 0, a[i], 0, n);
		double[][] w = new double[n][n];

		if (a[0][0] == 0.)
			return null;

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

		return a;
	}

	public static double[][] sparseRandomProjection(double[][] data, int nDim) {
		/* Johnson-Lindenstrauss lemma */
		int nRows = data.length;
		int nCols = data[0].length;
		double[][] result = new double[nRows][nDim];
		double[][] r = new double[nCols][nDim];
		Random random = Globals.random;
		for (int k = 0; k < nDim; k++) {
			for (int j = 0; j < nCols; j++) {
				r[j][k] = random.nextGaussian();
			}
		}
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				for (int k = 0; k < nDim; k++) {
					result[i][k] += data[i][j] * r[j][k];
				}
			}
		}
		return result;
	}
}