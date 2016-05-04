package com.h2o.online.processors;

import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.analytics.anomalies.Anomalies;
import com.h2o.online.data.DataTable;

public class AnalyticProcessor extends Processor {
	JSONMap results = new JSONMap();

	public AnalyticProcessor() {
	}

	public JSONMap compute(DataTable table, JSONMap analyticJSONMap) {

		JSONMap parameters = (JSONMap) analyticJSONMap.get(Globals.JSON_PARAMETERS);
		String subtype = (String) analyticJSONMap.get(Globals.JSON_SUBTYPE);
		if (subtype.equalsIgnoreCase(Globals.JSON_ANOMALIES)) {
			Anomalies anomalies = new Anomalies();
			JSONMap anomaliesResults = anomalies.compute(table, parameters);
			results.put(Globals.JSON_ANOMALIES, anomaliesResults);
		} else if (subtype.equalsIgnoreCase(Globals.JSON_PREDICTIONS)) {

		}
		return results;
	}
}
