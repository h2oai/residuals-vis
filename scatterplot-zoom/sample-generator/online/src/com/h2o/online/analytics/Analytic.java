/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.analytics;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;

public abstract class Analytic {

	public static boolean isComplete(double[] x) {
		if (x == null)
			return false;
		for (int j = 0; j < x.length; j++) {
			if (Globals.isMissing(x[j])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isComplete(double[] x, double wt) {
		if (Globals.isMissing(wt) || wt <= 0 || x == null)
			return false;
		for (int j = 0; j < x.length; j++) {
			if (Globals.isMissing(x[j])) {
				return false;
			}
		}
		return true;
	}

	protected JSONMap createJSONMapForSingleColumn(int column) {
		JSONMap params = new JSONMap();
		JSONArray col = new JSONArray();
		col.add(new Long(column));
		params.put("variables", col);
		return params;
	}

	protected JSONMap createJSONMapForTuple(int col1, int col2) {
		JSONMap params = new JSONMap();
		JSONArray tuples = new JSONArray();
		JSONArray tuple = new JSONArray();
		tuple.add(new Long(col1));
		tuple.add(new Long(col2));
		tuples.add(tuple);
		params.put("tuples", tuples);
		return params;
	}
}
