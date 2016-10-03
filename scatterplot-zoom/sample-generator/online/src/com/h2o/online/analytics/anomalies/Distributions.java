/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.anomalies;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.analytics.Analytic;
import com.h2o.online.analytics.util.statistics.Cognostics;
import com.h2o.online.data.DataTable;
import com.h2o.online.graphics.DotPlots;

@SuppressWarnings("unchecked")
public class Distributions extends Analytic {

	public Distributions() {
	}

	public JSONMap compute(DataTable table, JSONMap parameters) {
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();

		JSONArray variables = (JSONArray) parameters.get(Globals.JSON_VARIABLES_LIST);

		JSONMap results = new JSONMap();
		JSONArray skewed = new JSONArray();
		JSONArray spiked = new JSONArray();
		JSONArray multimodal = new JSONArray();

		int nVars = nCols;
		if (variables != null)
			nVars = variables.size();
		for (int jv = 0; jv < nVars; jv++) {
			int j = jv;
			if (variables != null)
				j = ((Long) variables.get(jv)).intValue();
			if (!table.isCategoricalVariable(j)) {
				double[] column = table.getRawDoubleColumn(j);
				if (Cognostics.isSkewed(column, null))
					skewed.add(new Long(j));
				if (Cognostics.isSpiked(column, null))
					spiked.add(new Long(j));
				if (Cognostics.isMultiModal(column, null))
					multimodal.add(new Long(j));
			}
		}
		if (skewed.size() > 0) {
			JSONArray skewedPlots = new JSONArray();
			for (int j = 0; j < skewed.size(); j++) {
				int col = ((Long) skewed.get(j)).intValue();
				JSONMap parms = createJSONMapForSingleColumn(col);
				DotPlots dotPlots = new DotPlots();
				skewedPlots.add(dotPlots.compute(table, parms));
			}
			JSONMap skewedResults = new JSONMap();
			skewedResults.put(Globals.JSON_COLINDICES_LIST, skewed);
			skewedResults.put(Globals.JSON_DOTPLOTS_LIST, skewedPlots);
			results.put(Globals.JSON_SKEWED, skewedResults);
		}
		if (spiked.size() > 0) {
			JSONArray spikedPlots = new JSONArray();
			for (int j = 0; j < spiked.size(); j++) {
				int col = ((Long) spiked.get(j)).intValue();
				JSONMap parms = createJSONMapForSingleColumn(col);
				DotPlots dotPlots = new DotPlots();
				spikedPlots.add(dotPlots.compute(table, parms));
			}
			JSONMap spikedResults = new JSONMap();
			spikedResults.put(Globals.JSON_COLINDICES_LIST, spiked);
			spikedResults.put(Globals.JSON_DOTPLOTS_LIST, spikedPlots);
			results.put(Globals.JSON_SPIKED, spikedResults);
		}
		if (multimodal.size() > 0) {
			JSONArray multimodalPlots = new JSONArray();
			for (int j = 0; j < multimodal.size(); j++) {
				int col = ((Long) multimodal.get(j)).intValue();
				JSONMap parms = createJSONMapForSingleColumn(col);
				DotPlots dotPlots = new DotPlots();
				multimodalPlots.add(dotPlots.compute(table, parms));
			}
			JSONMap multimodalResults = new JSONMap();
			multimodalResults.put(Globals.JSON_COLINDICES_LIST, multimodal);
			multimodalResults.put(Globals.JSON_DOTPLOTS_LIST, multimodalPlots);
			results.put(Globals.JSON_MULTIMODAL, multimodalResults);
		}
		return results;
	}
}
