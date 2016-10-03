package com.h2o.online;

/**
 * Copyright (c) 2016 H2O.ai
 */
import java.text.DateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;
import org.json.simple.JSONPrettyPrint;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.h2o.online.data.DataTable;
import com.h2o.online.data.util.CSVImporter;
import com.h2o.online.processors.AnalyticProcessor;
import com.h2o.online.processors.DataProcessor;
import com.h2o.online.processors.GraphicsProcessor;
import com.h2o.online.processors.TransformationProcessor;

public class Main {

	public static void main(String[] args) {
		/*
		 * Driver for DaVi
		 * 
		 * @SuppressWarnings("unchecked") used in various classes because org.json.simple does not use Generics
		 */
		DataTable table;

		if (args.length < 1) {
			System.out.println("{}");
			System.exit(666);
		}

		JSONMap inputJSONMap = importJSON(args[0]);
		System.out.println(JSONPrettyPrint.toJSONString(inputJSONMap));

		String uri = (String) inputJSONMap.get("uri");
		int[] columnIndices = getColumnIndices(inputJSONMap);

		Long t0 = System.currentTimeMillis();
		CSVImporter dataImporter = new CSVImporter();
		table = dataImporter.getFlatTable(uri, columnIndices);

		Long t1 = System.currentTimeMillis();
		//		System.out.println("Import time:" + (t1 - t0));

		JSONMap graphics = (JSONMap) inputJSONMap.get(Globals.JSON_GRAPHICS);
		JSONMap analytics = (JSONMap) inputJSONMap.get(Globals.JSON_ANALYTICS);
		JSONMap transformations = (JSONMap) inputJSONMap.get(Globals.JSON_TRANSFORMATIONS);
		JSONMap data = (JSONMap) inputJSONMap.get(Globals.JSON_DATA);

		JSONMap results = new JSONMap();

		if (transformations != null) {
			TransformationProcessor transformationProcessor = new TransformationProcessor();
			table = transformationProcessor.compute(table, transformations);
		}
		if (data != null) {
			DataProcessor dataProcessor = new DataProcessor();
			results = dataProcessor.compute(table, data);
		}
		if (graphics != null) {
			GraphicsProcessor graphicsProcessor = new GraphicsProcessor();
			results = graphicsProcessor.compute(table, graphics);
		}
		if (analytics != null) {
			AnalyticProcessor analyticProcessor = new AnalyticProcessor();
			results = analyticProcessor.compute(table, analytics);
		}
		if (results != null) {
			Date date = new Date();
			results.put(Globals.JSON_DATE,
					DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date));

			String json = JSONPrettyPrint.toJSONString(results);
			System.out.println(json);
		}

		Long t2 = System.currentTimeMillis();
	}

	private static JSONMap importJSON(String inputJSONString) {

		JSONParser jsonParser = new JSONParser();
		JSONMap inputJSONMap = null;
		try {
			inputJSONMap = (JSONMap) jsonParser.parse(inputJSONString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return inputJSONMap;
	}

	private static int[] getColumnIndices(JSONMap inputJSONMap) {
		JSONArray columns = (JSONArray) inputJSONMap.get(Globals.JSON_COLUMNS);
		if (columns == null)
			return null;
		int[] columnIndices = new int[columns.size()];
		for (int j = 0; j < columns.size(); j++)
			columnIndices[j] = (((Long) columns.get(j)).intValue());
		return columnIndices;
	}
}
