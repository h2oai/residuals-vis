/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.graphics;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.Aggregator;
import com.h2o.online.data.util.TextUtilities;

public class Scatterplots extends Graphics {

	public Scatterplots() {
	}

	@SuppressWarnings("unchecked")
	public JSONMap compute(DataTable table, JSONMap parameters, Aggregator compressor) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();

		String[] colNames = table.getColNames();

		/* get parameters */
		int color = 0;
		JSONArray tuples = new JSONArray();

		for (int j = 1; j < nCols; j++) {
			for (int k = j - 1; k < j; k++) {
				JSONArray a = new JSONArray();
				a.add(new Long(j));
				a.add(new Long(k));
				tuples.add(a);
			}
		}

		if (parameters != null) {
			Object o = parameters.get(Globals.JSON_COLORVAR);
			if (o != null)
				color = ((Long) o).intValue();
			JSONArray parameterTuples = (JSONArray) parameters.get(Globals.JSON_TUPLES_LIST);
			if (parameterTuples != null) {
				tuples = parameterTuples;
			}
		}

		String[] plotIDs = new String[tuples.size()];
		for (int tupleIndex = 0; tupleIndex < tuples.size(); tupleIndex++) {
			JSONArray tuple = (JSONArray) tuples.get(tupleIndex);
			String plotID = "";
			for (int k = 0; k < tuple.size(); k++) {
				plotID += colNames[Integer.parseInt(tuple.get(k).toString())];
				if (k < tuple.size() - 1)
					plotID += " vs ";
			}
			plotIDs[tupleIndex] = plotID;
		}

		/* compress data table if necessary */
		List<ArrayList<Integer>> rowIndices = null;
		int[] clusterCounts = null;
		if (nRows > Globals.MAXROWS) {
			if (compressor == null) {
				double[][] x = table.getRawDoubleTable(null);
				compressor = new Aggregator();
				compressor.compute(x);
			}
			rowIndices = compressor.getMemberIndices();
			nRows = rowIndices.size();
			clusterCounts = compressor.getCounts();
		}

		JSONMap scatterplotResults = new JSONMap();
		JSONArray[] coordinates = new JSONArray[3];
		JSONArray counts = new JSONArray();
		JSONMap scatterplot = new JSONMap();
		for (int tupleIndex = 0; tupleIndex < tuples.size(); tupleIndex++) {
			JSONArray tuple = (JSONArray) tuples.get(tupleIndex);
			for (int j = 0; j < tuple.size(); j++) {
				coordinates[j] = new JSONArray();
				for (int i = 0; i < nRows; i++) {
					int index = i;
					if (rowIndices != null)
						index = rowIndices.get(i).get(0); // this is index of exemplar
					if (j == 0 && clusterCounts != null)
						counts.add(clusterCounts[i]);
					int col = ((Long) tuple.get(j)).intValue();
					if (table.isCategoricalVariable(col)) {
						String s = table.getRawStringValue(index, col);
						coordinates[j].add(s);
					} else {
						double x = table.getRawDoubleValue(index, col);
						if (!Double.isNaN(x)) {
							String s = TextUtilities.formatDouble(((Double) x).doubleValue(), 3);
							x = Double.parseDouble(s);
						}
						coordinates[j].add(x);
					}
				}
			}
			scatterplot.put(Globals.JSON_XCOORDINATES_LIST, coordinates[0]);
			scatterplot.put(Globals.JSON_YCOORDINATES_LIST, coordinates[1]);
			if (coordinates[2] != null)
				scatterplot.put(Globals.JSON_ZCOORDINATES_LIST, coordinates[2]);
			scatterplot.put(Globals.JSON_COUNTS_LIST, counts);

			scatterplotResults.put(plotIDs[tupleIndex], scatterplot);
		}
		return scatterplotResults;
	}
}