package com.h2o.online.processors;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.TextUtilities;

public class DataProcessor extends Processor {

	JSONMap results = new JSONMap();

	public DataProcessor() {
	}

	public JSONMap compute(DataTable table, JSONMap dataJSONMap) {
		int nInputRows = table.getNumRows();
		int nInputCols = table.getNumCols();

		JSONMap parameters = (JSONMap) dataJSONMap.get(Globals.JSON_PARAMETERS);
		int row1 = 0;
		int row2 = nInputRows;
		int col1 = 0;
		int col2 = nInputCols;
		Long r1 = null;
		Long r2 = null;
		Long c1 = null;
		Long c2 = null;
		if (parameters != null) {
			r1 = (Long) parameters.get(Globals.JSON_STARTROW);
			r2 = (Long) parameters.get(Globals.JSON_ENDROW);
			c1 = (Long) parameters.get(Globals.JSON_STARTCOLUMN);
			c2 = (Long) parameters.get(Globals.JSON_ENDCOLUMN);
		}
		if (r1 != null)
			row1 = r1.intValue();
		if (r2 != null)
			row2 = r2.intValue();
		if (c1 != null)
			col1 = c1.intValue();
		if (c2 != null)
			col2 = c2.intValue();
		JSONArray variableLabels = new JSONArray();
		String[] labels = table.getColNames();
		for (int j = col1; j < col2; j++) {
			variableLabels.add(labels[j]);
		}
		results.put(Globals.JSON_VARNAMES, variableLabels);
		JSONArray rows = new JSONArray();
		for (int row = row1; row < row2; row++) {
			JSONArray values = new JSONArray();
			Object[] rawRow = table.getRawRow(row);
			for (int col = col1; col < col2; col++) {
				String value = "";
				if (table.isCategoricalVariable(col)) {
					value = (String) rawRow[col];
					values.add(value);
				} else {
					double x = ((Double) rawRow[col]).doubleValue();
					if (!Double.isNaN(x)) {
						value = TextUtilities.formatDouble(x, 3);
						x = Double.parseDouble(value);
					}
					values.add(x);
				}
			}
			rows.add(values);
		}
		results.put(Globals.JSON_PARAMETERS, parameters);
		results.put(Globals.JSON_DATATABLE, rows);
		return results;
	}
}
