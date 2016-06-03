package com.h2o.online.analytics.util;

import java.util.ArrayList;
import java.util.List;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.statistics.CorrespondenceAnalysis;
import com.h2o.online.data.DataTable;

public class CategoricalToContinuousMap {
	/*
	 * Uses Correspondence Analysis to map categorical variables to continuous
	 * variables Returns nRows by nCatVars matrix of MCA variables If there are
	 * no categorical variables in data (nCatVars == 0), returns null
	 */

	public static double[][] compute(DataTable table, int nRows, int nCols) {
		double[][] result = null;
		if (table.getCategoriesMap() == null || table.getCategoriesMap().isEmpty())
			return result;
		int nCatVars = table.getCategoricalVariableNames().length;
		int[] nCats = new int[nCatVars];
		int ny = 0;
		int nc = 0;
		int firstCategoricalVariable = -1;
		List<Integer> categoricalIndices = new ArrayList<Integer>();
		for (int j = 0; j < nCols; j++) {
			if (table.isCategoricalVariable(j)) {
				categoricalIndices.add(j);
				if (firstCategoricalVariable < 0)
					firstCategoricalVariable = j;
				nCats[nc] = table.countValues(j);
				if (nCats[nc] > Globals.TOO_BIG)
					nCats[nc] = 1;
				ny += nCats[nc];
				nc++;
			}
		}

		/*
		 * if not enough degrees of freedom to compute eigendecomposition, use
		 * category indices
		 */
		if (nc == 1 && ny == 1) {
			double[][] scores = new double[nRows][1];
			for (int i = 0; i < nRows; i++) {
				String cat = table.getRawStringValue(i, firstCategoricalVariable);
				scores[i][0] = table.getCategoryIndex(firstCategoricalVariable, cat);
			}
			return scores;
		}

		double[][] measures = new double[nRows][nCatVars];
		nc = 0;
		for (int j = 0; j < nCatVars; j++) {
			double[][] yy = new double[nCats[j]][nCats[j]];

			/* collect indicators */
			for (int i = 0; i < nRows; i++) {
				int[] ycode = codeRow(table, nCats, categoricalIndices.get(j), i, j);
				for (int k = 0; k < nCats[j]; k++) {
					for (int m = 0; m < nCats[j]; m++)
						yy[k][m] += ycode[k] * ycode[m];
				}
			}

			CorrespondenceAnalysis ca = new CorrespondenceAnalysis();
			ca.computeMultiple(yy, nRows, nCatVars);

			double[] scores = computeScores(table, nCats, categoricalIndices.get(j), nRows, j, ca.getYCoordinates());
			for (int i = 0; i < nRows; i++) {
				measures[i][j] = scores[i];
			}
			nc += nCats[j];
		}
		return measures;
	}

	private static int[] codeRow(DataTable table, int[] nCats, int categoricalIndex, int row, int col) {
		int[] ycode = new int[nCats[col]];
		String category = (String) table.getRawRow(row)[categoricalIndex];
		if (Globals.isMissing(category))
			return new int[0];
		int categoryIndex = table.getCategoryIndex(categoricalIndex, category);
		if (categoryIndex >= 0) {
			if (nCats[col] > 1) {
				ycode[categoryIndex] = 1;
			} else {
				ycode[categoryIndex] = categoryIndex;
			}
		}
		return ycode;
	}

	private static double[] computeScores(DataTable data, int[] nCats, int categoryIndex, int nRows, int col, double[][] components) {
		double[] scores = new double[nRows];
		for (int i = 0; i < nRows; i++) {
			int[] ycode = codeRow(data, nCats, categoryIndex, i, col);
			for (int k = 0; k < nCats[col]; k++) {
				scores[i] += ycode[k] * components[k][0];
			}
		}
		return scores;
	}
}