/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics.anomalies;

import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.analytics.Analytic;
import com.h2o.online.data.DataTable;

public class Anomalies extends Analytic {

	public Anomalies() {
	}

	@SuppressWarnings("unchecked")
	public JSONMap compute(DataTable table, JSONMap parameters) {
		JSONMap results = new JSONMap();

		Outliers outliers = new Outliers();
		JSONMap outlierResults = outliers.compute(table, parameters);
		if (outlierResults != null && outlierResults.size() > 0)
			results.put(Globals.JSON_OUTLIERS_LIST, outlierResults);

		Distributions distributions = new Distributions();
		JSONMap distributionResults = distributions.compute(table, parameters);
		if (distributionResults != null && distributionResults.size() > 0)
			results.put(Globals.JSON_DISTRIBUTIONS, distributionResults);

		return results;
	}
}
