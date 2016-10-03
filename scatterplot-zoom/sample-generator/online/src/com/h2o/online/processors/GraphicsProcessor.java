package com.h2o.online.processors;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;
import com.h2o.online.graphics.DotPlots;
import com.h2o.online.graphics.Histobars;
import com.h2o.online.graphics.ParallelCoordinates;
import com.h2o.online.graphics.Scatterplots;

public class GraphicsProcessor extends Processor {
	JSONMap results = new JSONMap();

	public GraphicsProcessor() {
	}

	public JSONMap compute(DataTable table, JSONMap graphicsJSONMap) {
		JSONMap parameters = (JSONMap) graphicsJSONMap.get(Globals.JSON_PARAMETERS);
		String subtype = (String) parameters.get(Globals.JSON_SUBTYPE);
		if (subtype.equalsIgnoreCase(Globals.JSON_HISTOBARS)) {
			Histobars histobars = new Histobars();
			JSONArray histobarsResults = histobars.compute(table, parameters);
			results.put(Globals.JSON_HISTOBARS_LIST, histobarsResults);
		} else if (subtype.equalsIgnoreCase(Globals.JSON_SCATTERPLOTS)) {
			Scatterplots scatterplots = new Scatterplots();
			JSONMap scatterplotsResults = scatterplots.compute(table, parameters, null);
			results.put(Globals.JSON_SCATTERPLOTS_LIST, scatterplotsResults);
		} else if (subtype.equalsIgnoreCase(Globals.JSON_DOTPLOTS)) {
			DotPlots dotplots = new DotPlots();
			JSONArray dotplotsResults = dotplots.compute(table, parameters);
			results.put(Globals.JSON_DOTPLOTS_LIST, dotplotsResults);
		} else if (subtype.equalsIgnoreCase(Globals.JSON_PARALLELCOORDINATES)) {
			ParallelCoordinates parallelCoordinates = new ParallelCoordinates();
			JSONMap parallelCoordinatesResults = parallelCoordinates.compute(table, parameters, null);
			results.put(Globals.JSON_PARALLELCOORDINATES_LIST, parallelCoordinatesResults);
		}
		return results;
	}
}
