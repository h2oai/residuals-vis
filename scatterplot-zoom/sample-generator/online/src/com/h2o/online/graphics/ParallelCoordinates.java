package com.h2o.online.graphics;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.Aggregator;
import com.h2o.online.data.util.TextUtilities;

public class ParallelCoordinates extends Graphics {

	public ParallelCoordinates() {
	}

	@SuppressWarnings("unchecked")
	public JSONMap compute(DataTable table, JSONMap parameters, Aggregator compressor) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();
		if (nCols > Globals.MAXDIMENSIONS)
			return new JSONMap();

		/* get parameters */
		JSONArray variables = null;
		if (parameters != null) {
			variables = (JSONArray) parameters.get(Globals.JSON_VARIABLES_LIST);
		}
		int nVars = nCols;
		if (variables != null)
			nVars = variables.size();

		/* compress data table if necessary */
		List<ArrayList<Integer>> rowIndices = null;
		int[] counts = null;
		if (nRows > Globals.MAXROWS) {
			if (compressor == null) {
				int[] vars = null;
				if (variables != null) {
					vars = new int[variables.size()];
					for (int i = 0; i < variables.size(); i++)
						vars[i] = ((Long) variables.get(i)).intValue();
				}
				double[][] x = table.getRawDoubleTable(vars);
				compressor = new Aggregator();
				compressor.compute(x);
			}
			rowIndices = compressor.getMemberIndices();
			nRows = rowIndices.size();
			counts = compressor.getCounts();
		}

		JSONArray pcPlot = new JSONArray();
		for (int i = 0; i < nRows; i++) {
			JSONArray profile = new JSONArray();
			int index = i;
			if (rowIndices != null)
				index = rowIndices.get(i).get(0); // this is index of exemplar
			for (int jv = 0; jv < nVars; jv++) {
				int j = jv;
				if (variables != null)
					j = ((Long) variables.get(jv)).intValue();
				if (table.isCategoricalVariable(j)) {
					String s = table.getRawStringValue(index, j);
					profile.add(s);
				} else {
					double x = table.getRawDoubleValue(index, j);
					if (!Double.isNaN(x)) {
						String s = TextUtilities.formatDouble(((Double) x).doubleValue(), 3);
						x = Double.parseDouble(s);
					}
					profile.add(x);
				}
			}
			if (counts != null)
				profile.add(counts[i]);
			pcPlot.add(profile);
		}
		JSONArray varNames = new JSONArray();
		for (int jv = 0; jv < nVars; jv++) {
			int j = jv;
			if (variables != null)
				j = ((Long) variables.get(jv)).intValue();
			varNames.add(table.getColNames()[j]);
		}
		JSONMap parallelCoordinateResults = new JSONMap();
		parallelCoordinateResults.put(Globals.JSON_VARNAMES, varNames);
		parallelCoordinateResults.put(Globals.JSON_PROFILES_LIST, pcPlot);
		return parallelCoordinateResults;
	}
}
