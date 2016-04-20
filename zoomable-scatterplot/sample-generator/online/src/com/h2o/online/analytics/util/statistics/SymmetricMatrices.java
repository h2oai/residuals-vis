/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.util.statistics;

import java.util.Set;
import java.util.TreeSet;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Matrices;
import com.h2o.online.analytics.util.Sorts;

public class SymmetricMatrices {

	private SymmetricMatrices() {
	}

	public static double[][] compute(double[][] data, double[] weights, boolean[] isCategorical,
			SimilarityFunctions sf, boolean isByRows) {
		/*
		 * data[n][p] input
		 * isByRows true returns xx[n][n], false returns xx[p][p]
		 */
		if (sf == null)
			return null;
		else if (sf.equals(SimilarityFunctions.PRE))
			return proportionalReductionInError(data, weights, isCategorical);
		else if (isByRows)
			return betweenRows(data, weights, sf);
		else
			return betweenCols(data, weights, sf);
	}

	private static double[][] betweenCols(double[][] data, double[] weights, SimilarityFunctions sf) {
		double wt = 1.0;
		int n = data.length;
		int p = data[0].length;
		double[][] x = data;
		if (sf.equals(SimilarityFunctions.SPEARMAN_CORRELATION))
			x = rankCols(data);
		double[] wcounts = new double[p];
		double[] means = new double[p];
		double[][] xx = new double[p][p];

		if (sf.equals(SimilarityFunctions.NCD) || sf.equals(SimilarityFunctions.CCF)) {
			x = Matrices.transpose(x);
			for (int j = 0; j < p; j++) {
				xx[j][j] = 0;
				for (int k = 0; k < j; k++) {
					xx[j][k] = sf.compute(x[j], x[k]);
					xx[k][j] = xx[j][k];
				}
			}
			return xx;
		}

		for (int i = 0; i < n; i++) {
			if (weights != null)
				wt = weights[i];
			if (wt > 0) {
				for (int j = 0; j < p; j++) {
					if (!Globals.isMissing(x[i][j])) {
						wcounts[j] += wt;
						double xj = wt * (x[i][j] - means[j]);
						means[j] += xj / wcounts[j];
						for (int k = 0; k <= j; k++) {
							if (!Globals.isMissing(x[i][k])) {
								double xk = x[i][k] - means[k];
								double diff = x[i][j] - x[i][k];
								double prod = xj * xk;
								xx[j][k] += sf.update(diff, prod, wt);
								xx[k][j] = xx[j][k];
							}
						}
					}
				}
			}
		}
		for (int j = 1; j < p; j++) {
			for (int k = 0; k < j; k++) {
				xx[j][k] = sf.normalize(xx[j][k], wcounts[j], xx[j][j], xx[k][k]);
				xx[k][j] = xx[j][k];
			}
		}
		for (int j = 0; j < p; j++)
			xx[j][j] = sf.normalize(xx[j][j], wcounts[j], xx[j][j], xx[j][j]);

		return xx;
	}

	private static double[][] betweenRows(double[][] data, double[] weights, SimilarityFunctions sf) {
		double wt = 1.0;
		int n = data.length;
		int p = data[0].length;
		double[][] x = data;
		if (sf.equals(SimilarityFunctions.SPEARMAN_CORRELATION))
			x = rankRows(data);
		double[] counts = new double[n];
		double[] means = new double[n];
		double[][] xx = new double[n][n];
		for (int i = 0; i < n; i++) {
			if (weights != null && (Globals.isMissing(weights[i]) || weights[i] <= 0))
				fillMissingRowCol(xx, i);
		}

		if (sf.equals(SimilarityFunctions.NCD) || sf.equals(SimilarityFunctions.CCF)) {
			for (int j = 0; j < n; j++) {
				xx[j][j] = 0;
				for (int k = 0; k < j; k++) {
					xx[j][k] = sf.compute(x[j], x[k]);
					xx[k][j] = xx[j][k];
				}
			}
			return xx;
		}

		for (int j = 0; j < p; j++) {
			for (int i = 0; i < n; i++) {
				if (!Globals.isMissing(x[i][j])) {
					counts[i]++;
					double xj = (x[i][j] - means[i]);
					means[i] += xj / counts[i];
					for (int k = 0; k <= i; k++) {
						if (!Globals.isMissing(x[k][j])) {
							double xk = x[k][j] - means[k];
							double diff = x[i][j] - x[k][j];
							double prod = xj * xk;
							xx[i][k] += sf.update(diff, prod, wt);
							xx[k][i] = xx[i][k];
						}
					}
				}
			}
		}
		for (int j = 1; j < n; j++) {
			for (int k = 0; k < j; k++) {
				xx[j][k] = sf.normalize(xx[j][k], counts[j], xx[j][j], xx[k][k]);
				xx[k][j] = xx[j][k];
			}
		}
		for (int j = 0; j < n; j++)
			xx[j][j] = sf.normalize(xx[j][j], counts[j], xx[j][j], xx[j][j]);

		return xx;
	}

	private static void fillMissingRowCol(double[][] xx, int rowcol) {
		int n = xx.length;
		for (int j = 0; j < n; j++) {
			for (int k = 0; k < n; k++) {
				if (j == rowcol || k == rowcol)
					xx[j][k] = Globals.MISSING_VALUE;
			}
		}
	}

	private static double[][] rankRows(double[][] data) {
		int n = data.length;
		int p = data[0].length;
		double[][] x = new double[n][p];
		for (int i = 0; i < n; i++)
			x[i] = Sorts.rank(data[i]);
		return x;
	}

	private static double[][] rankCols(double[][] data) {
		int n = data.length;
		int p = data[0].length;
		double[][] x = new double[n][p];
		for (int j = 0; j < p; j++) {
			double[] xr = new double[n];
			for (int i = 0; i < n; i++)
				xr[i] = data[i][j];
			xr = Sorts.rank(xr);
			for (int i = 0; i < n; i++)
				x[i][j] = xr[i];
		}
		return x;
	}

	private static double[][] proportionalReductionInError(double[][] data, double[] weight, boolean[] isCategorical) {
		int n = data.length;
		int p = data[0].length;
		double[][] xx = new double[p][p];
		for (int j = 0; j < p; j++) {
			xx[j][j] = 1;
			for (int k = 0; k < j; k++) {
				if (isCategorical[j] && isCategorical[k])
					xx[j][k] = categoricalCategorical(n, weight, data, j, k);
				else if (isCategorical[j])
					xx[j][k] = categoricalContinuous(n, weight, data, j, k);
				else if (isCategorical[k])
					xx[j][k] = categoricalContinuous(n, weight, data, k, j);
				else
					xx[j][k] = continuousContinuous(n, weight, data, j, k);

				xx[j][k] = Math.sqrt(xx[j][k]);
				xx[k][j] = xx[j][k];
			}
		}
		return xx;
	}

	private static double continuousContinuous(int n, double[] weight, double[][] data, int j, int k) {
		double wt = 1.0;
		double wcount = 0;
		double prod = 0;
		double[] means = new double[2];
		double[] sds = new double[2];
		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k])) {
				wcount += wt;
				means[0] += wt * data[i][j];
				means[1] += wt * data[i][k];
			}
		}
		means[0] /= wcount;
		means[1] /= wcount;
		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k])) {
				prod += wt * (data[i][j] - means[0]) * (data[i][k] - means[1]);
				sds[0] += wt * (data[i][j] - means[0]) * (data[i][j] - means[0]);
				sds[1] += wt * (data[i][k] - means[1]) * (data[i][k] - means[1]);
			}
		}
		return prod * prod / (sds[0] * sds[1]);
	}

	private static double categoricalContinuous(int n, double[] weight, double[][] data, int j, int k) {
		double wt = 1.0;
		int maxCat = 0;
		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k]))
				maxCat = (int) Math.max(maxCat, data[i][j]);
		}

		double wcount = 0;
		double sst = 0;
		double ssw = 0;
		double[] counts = new double[maxCat];
		double[] means = new double[maxCat];
		double grandMean = 0;

		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k])) {
				int cat = (int) data[i][j] - 1;
				means[cat] += wt * data[i][k];
				counts[cat] += wt;
				wcount += wt;
				grandMean += wt * data[i][k];
			}
		}
		for (int i = 0; i < maxCat; i++) {
			means[i] /= counts[i];
			grandMean /= wcount;
		}

		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k])) {
				int cat = (int) data[i][j] - 1;
				ssw += wt * (data[i][k] - means[cat]) * (data[i][k] - means[cat]);
				sst += wt * (data[i][k] - grandMean) * (data[i][k] - grandMean);
			}
		}
		return 1 - ssw / sst;
	}

	private static double categoricalCategorical(int n, double[] weight, double[][] data, int j, int k) {
		double wt = 1.0;
		int nj = 0;
		int nk = 0;
		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k])) {
				nj = (int) Math.max(nj, data[i][j]);
				nk = (int) Math.max(nk, data[i][k]);
			}
		}

		double total = 0;
		double[][] crosstab = new double[nj][nk];
		double[] sumj = new double[nj];
		double[] sumk = new double[nk];

		for (int i = 0; i < n; i++) {
			if (weight != null)
				wt = weight[i];
			if (wt > 0 && !Globals.isMissing(data[i][j]) && !Globals.isMissing(data[i][k])) {
				int catj = (int) data[i][j] - 1;
				int catk = (int) data[i][k] - 1;
				crosstab[catj][catk] += wt;
				sumj[catj] += wt;
				sumk[catk] += wt;
				total += wt;
			}
		}

		double chisq = 0;
		for (int jj = 0; jj < nj; jj++) {
			for (int kk = 0; kk < nk; kk++) {
				double obs = crosstab[jj][kk];
				double exp = sumj[jj] * sumk[kk] / total;
				chisq += (obs - exp) * (obs - exp) / exp;
			}
		}
		double phisq = chisq / total;
		return phisq / Math.min(nj, nk);
	}

	public static double[][] jaccard(Set[] data) {
		/* Jaccard distance */
		int n = data.length;
		double[][] xx = new double[n][n];
		for (int i = 0; i < n; i++) {
			Set a = data[i];
			for (int j = 0; j < i; j++) {
				Set b = data[j];
				Set intersection = new TreeSet(a);
				intersection.retainAll(b);
				Set union = new TreeSet(a);
				union.addAll(b);
				if (union.size() == 0)
					xx[i][j] = 1.0;
				else
					xx[i][j] = 1.0 - (double) intersection.size() / (double) union.size();
				xx[j][i] = xx[i][j];
			}
		}
		return xx;
	}
}
