/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.graphics;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.Statistics;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.TextUtilities;

public class DotPlots {
	/* Wilkinson, L (1999). Dot plots. The American Statistician, 53, 276-281. */
	/* this implementation works for either numeric or string columns */

	public DotPlots() {
	}

	@SuppressWarnings("unchecked")
	public JSONArray compute(DataTable table, JSONMap parameters) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();

		/* get parameters */
		int maxDots = Globals.MAXDOTS;
		JSONArray variables = null;
		if (parameters != null) {
			Object o = parameters.get(Globals.JSON_MAXDOTS);
			if (o != null)
				maxDots = ((Long) o).intValue();
			variables = (JSONArray) parameters.get(Globals.JSON_VARIABLES_LIST);
		}

		JSONArray dotplotResults = new JSONArray();
		int nVars = nCols;
		if (variables != null)
			nVars = variables.size();
		for (int jv = 0; jv < nVars; jv++) {
			int j = jv;
			if (variables != null)
				j = ((Long) variables.get(jv)).intValue();
			double[] x = new double[nRows];
			if (table.isCategoricalVariable(j)) {
				for (int i = 0; i < nRows; i++) {
					if (table.isMissing(i, j))
						continue;
					String category = (String) table.getRawRow(i)[j];
					int categoryIndex = table.getCategoryIndex(j, category);
					x[i] = categoryIndex;
				}
			} else {
				for (int i = 0; i < nRows; i++)
					x[i] = table.getTransformedDoubleValue(i, j);
			}
			Statistics.unitize(x, null);
			/* h is bandwidth -- see Wilkinson paper */
			double h = .1 / Math.log(nRows + 10);
			int[] index = Sorts.indexedDoubleArraySort(x, 0, 0);
			/* z is location of last element in running tally */
			double z = Double.NEGATIVE_INFINITY;
			/* v is position on X axis of a dot stack; it is centered at average in x coordinates of all dots in stack */
			Double xValue = new Double(0);

			JSONMap dotplot = new JSONMap();
			JSONArray xCoordinates = new JSONArray();
			JSONArray stacks = new JSONArray();
			JSONArray stack = null;

			int maxStack = 0;
			for (int i = 0; i < nRows; i++) {
				if (Globals.isMissing(x[index[i]]))
					continue;
				if (x[index[i]] > z + h) {
					/* new x location is greater than x location of prior stack of dots */
					if (stack != null) {
						/* add stack of dots to dotplot before starting new stack */
						xValue /= stack.size();
						String s = "";
						if (table.isCategoricalVariable(j)) {
							s = (String) table.getRawRow(i)[j];
							xCoordinates.add(s);
						} else {
							if (!Double.isNaN(xValue)) {
								s = TextUtilities.formatDouble(xValue.doubleValue(), 3);
								xValue = Double.parseDouble(s);
							}
							xCoordinates.add(xValue);
						}
						stacks.add(stack);
						maxStack = Math.max(maxStack, stack.size());
					}
					/* start new stack at new x location */
					stack = new JSONArray();
					stack.add(index[i]);
					if (table.isCategoricalVariable(j))
						xValue = (double) table.getCategoryIndex(j, (String) table.getRawRow(i)[j]);
					else
						xValue = table.getTransformedDoubleValue(index[i], j);
					z = x[index[i]];
				} else {
					stack.add(index[i]);
					if (table.isCategoricalVariable(j))
						xValue += (double) table.getCategoryIndex(j, (String) table.getRawRow(i)[j]);
					else
						xValue += table.getTransformedDoubleValue(index[i], j);
				}
			}
			/* take care of last stack */
			maxStack = Math.max(maxStack, stack.size());
			xValue /= stack.size();
			String s = TextUtilities.formatDouble((xValue).doubleValue(), 3);
			xCoordinates.add(Double.parseDouble(s));
			stacks.add(stack);

			if (maxStack < maxDots) {
				/* add dotplot to results */
				dotplot.put(Globals.JSON_VARNAME, table.getColNames()[j]);
				dotplot.put(Globals.JSON_XCOORDINATES_LIST, xCoordinates);
				dotplot.put(Globals.JSON_STACKS_LIST, stacks);
				dotplotResults.add(dotplot);
			} else {
				/* partition large stacks into arrays of smaller arrays (one dot represents maxDots) */
				JSONArray compressed = new JSONArray();
				for (int k = 0; k < stacks.size(); k++) {
					JSONArray indices = (JSONArray) stacks.get(k);
					JSONArray ja = new JSONArray();
					for (int i = 0; i < indices.size(); i += maxDots)
						ja.add(indices.subList(i, Math.min(i + maxDots, indices.size())));
					compressed.add(ja);
					dotplot.put(Globals.JSON_VARNAME, table.getColNames()[j]);
					dotplot.put(Globals.JSON_XCOORDINATES_LIST, xCoordinates);
					dotplot.put(Globals.JSON_STACKS_LIST, compressed);
					dotplotResults.add(dotplot);
				}
			}
		}

		return dotplotResults;
	}
}
