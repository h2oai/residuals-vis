/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.graphics;

import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.TextUtilities;

public class Histobars extends Graphics {

	public Histobars() {
	}

	@SuppressWarnings("unchecked")
	public JSONArray compute(DataTable table, JSONMap parameters) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();
		JSONArray histobarResults = new JSONArray();

		/* get parameters */
		int nBars = 0;
		JSONArray variables = null;
		if (parameters != null) {
			Object o = parameters.get(Globals.JSON_NUMBARS);
			if (o != null)
				nBars = ((Long) o).intValue();
			variables = (JSONArray) parameters.get(Globals.JSON_VARIABLES_LIST);
		}

		int nVars = nCols;
		if (variables != null)
			nVars = variables.size();
		for (int jv = 0; jv < nVars; jv++) {
			int j = jv;
			if (variables != null)
				j = ((Long) variables.get(jv)).intValue();
			JSONMap histobar = new JSONMap();
			JSONArray bins = new JSONArray();
			JSONArray frequencies = new JSONArray();
			if (table.isCategoricalVariable(j)) {
				String[] cats = table.getCategories(j);
				int[] counts = new int[cats.length];
				for (int i = 0; i < nRows; i++) {
					int cell = 0;
					String s = table.getRawStringValue(i, j);
					if (s.length() == 0)
						continue;
					for (int k = 0; k < cats.length; k++) {
						if (s.equals(cats[k])) {
							cell = k;
							break;
						}
					}
					counts[cell]++;
				}
				for (int k = 0; k < cats.length; k++) {
					JSONMap bar = new JSONMap();
					bins.add(cats[k]);
					frequencies.add(counts[k]);
				}
				histobar.put(Globals.JSON_VARNAME, table.getColNames()[j]);
				histobar.put(Globals.JSON_BINS, bins);
				histobar.put(Globals.JSON_COUNTS_LIST, frequencies);
				histobarResults.add(histobar);
			} else {
				if (nBars == 0)
					nBars = (int) (3 + Math.log(nRows) / Math.log(2) * Math.log(nRows) / Math.log(10));
				if (table.isSmallNonnegativeIntegerVariable(j))
					nBars = table.countValues(j) + 2;
				double[] dataLimits = table.getTransformedDataMinMax(j);
				double binWidth = (dataLimits[1] - dataLimits[0]) / nBars;
				if (table.isIndexVariable(j)) {
					nBars = (int) Math.min(10.0, nRows);
					dataLimits[0] = 0;
					dataLimits[1] = 10 + 10 * (nRows / 10);
					binWidth = nRows / nBars;
				}

				Integer[] counts = new Integer[nBars];
				Arrays.fill(counts, 0);
				for (int i = 0; i < nRows; i++) {
					int cell = 0;
					double x = table.getRawDoubleValue(i, j);
					if (!Globals.isMissing(x)) {
						cell = (int) (nBars * (Globals.ONE_MINUS_EPSILON * (x - dataLimits[0]) / (dataLimits[1] - dataLimits[0])));
						counts[cell]++;
					}
				}
				for (int k = 0; k < nBars; k++) {
					JSONArray bar = new JSONArray();
					double bin = dataLimits[0] + (k + 1) * binWidth;
					String b = TextUtilities.formatDouble(((Double) bin).doubleValue(), 3);
					bin = Double.parseDouble(b);
					bins.add(bin);
					frequencies.add(counts[k]);
				}
				histobar.put(Globals.JSON_VARNAME, table.getColNames()[j]);
				histobar.put(Globals.JSON_BINS, bins);
				histobar.put(Globals.JSON_COUNTS_LIST, frequencies);
			}
			histobarResults.add(histobar);
		}
		return histobarResults;
	}
}