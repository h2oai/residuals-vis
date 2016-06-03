package com.h2o.online.processors;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONMap;

import com.h2o.online.Globals;
import com.h2o.online.data.DataTable;
import com.h2o.online.data.transformer.Executor;
import com.h2o.online.data.transformer.ExecutorException;
import com.h2o.online.data.transformer.ParseTree;
import com.h2o.online.data.transformer.ParseTreeNode;
import com.h2o.online.data.transformer.Parser;
import com.h2o.online.data.transformer.ParserException;
import com.h2o.online.data.transformer.Syntax;
import com.h2o.online.data.transformer.SyntaxTableau;

public class TransformationProcessor extends Processor {

	public TransformationProcessor() {
	}

	public DataTable compute(DataTable table, JSONMap transformationJSONMap) {
		DataTable outputTable = null;

		JSONMap parameters = (JSONMap) transformationJSONMap.get(Globals.JSON_PARAMETERS);
		JSONArray script = (JSONArray) transformationJSONMap.get(Globals.JSON_SCRIPT);
		/* read and format script */
		List<String> records = new ArrayList<String>();
		for (int j = 0; j < script.size(); j++) {
			String record = (String) script.get(j);
			/* reduce runs of spaces to single space */
			record = record.trim().replaceAll("\\s+", " ");
			record = record.replace("else if", "elseif");
			/* filter out comment */
			if (record.contains("//"))
				record = record.substring(0, record.indexOf("//"));
			/* rearrange braces to canonical form */
			if (record.startsWith("{")) {
				if (!records.isEmpty()) {
					int previousLoc = records.size() - 1;
					String previous = records.get(previousLoc);
					records.set(previousLoc, previous + " {");
				}
			} else if (record.startsWith("}")) {
				records.add("}");
				if (record.length() > 1)
					records.add(record.substring(1).trim());
				/* split lines starting with if, elseif */
			} else if ((record.startsWith("if ") || record.startsWith("elseif ")) && !record.endsWith("{")) {
				char[] c = record.toCharArray();
				int count = 0;
				int loc = record.indexOf("(");
				for (int k = loc; k < c.length; k++) {
					if (c[k] == '(')
						count++;
					if (c[k] == ')')
						count--;
					if (count == 0) {
						records.add(record.substring(0, k + 1));
						if (record.substring(k + 1).length() > 0)
							records.add(record.substring(k + 1));
						break;
					}
				}
				/* split lines starting with else */
			} else if (record.startsWith("else") && record.length() > 4 && !record.endsWith("{")) {
				records.add("else ");
				records.add(record.substring(4));
			} else if (record.length() > 0) {
				records.add(record);
			}
		}
		String[] statements = new String[records.size()];
		statements = records.toArray(statements);

		Syntax syntax = new SyntaxTableau();

		String[] colNames = table.getColNames();
		syntax.setVariableNames(colNames);
		Parser parser = Parser.getInstance(syntax);

		ParseTree pt = null;
		try {
			pt = parser.parse(statements);
		} catch (ParserException se) {
			System.err.println("Syntax exception: " + se.getMessage());
			System.exit(1);
		}

		try {
			outputTable = processData(table, syntax, pt);
		} catch (ExecutorException e) {
			e.printStackTrace();
		}
		return outputTable;
	}

	private DataTable processData(DataTable table, Syntax syntax, ParseTree pt) throws ExecutorException {

		String[] varNames = syntax.getVariableNames();
		int nVars = varNames.length; // includes extra variable names added by parser
		int nRows = table.getNumRows();
		int nCols = table.getNumCols();
		Object[][] inputTable = table.getRawTable();
		Object[][] data = new Object[nRows][nVars];
		boolean[] isDeletedRow = new boolean[nRows];

		/* copy dataTable into data array */
		for (int i = 0; i < nRows; i++) {
			Object[] row = inputTable[i];
			System.arraycopy(row, 0, data[i], 0, nCols);
		}

		/* run Executer on data */
		Executor ex = new Executor(pt, syntax, data);
		for (int i = 0; i < nRows; i++) {
			syntax.isDeleted = false;
			ex.run(i);
			isDeletedRow[i] = syntax.isDeleted;
		}

		data = ex.getData();
		nRows = data.length;

		boolean[] isDroppedColumn = new boolean[nVars];
		List d = ParseTreeNode.dropVariables;
		if (d != null) {
			for (int k = 0; k < d.size(); k++) {
				Integer jk = (Integer) d.get(k);
				isDroppedColumn[jk] = true;
			}
		}

		/* write header */
		ArrayList<String> headerList = new ArrayList<String>();
		for (int j = 0; j < nVars; j++) {
			if (isDroppedColumn[j])
				continue;
			int varLength = syntax.getVariableLength(j);
			if (varLength == 1) {
				headerList.add(varNames[j]);
			} else {
				for (int k = 0; k < varLength; k++) {
					headerList.add(varNames[j] + "[" + (k + 1) + "]");
				}
			}
		}
		nVars = headerList.size();
		boolean[] isCategorical = new boolean[nVars];
		System.arraycopy(table.isCategorical(), 0, isCategorical, 0, nCols);
		String[] units = new String[nVars];
		System.arraycopy(table.getUnits(), 0, units, 0, nCols);
		String[] header = new String[nVars];
		for (int j = 0; j < nVars; j++)
			header[j] = headerList.get(j);
		DataTable outputTable = new DataTable(table.getName(), table.getRowNames(), header, isCategorical, units);

		for (int i = 0; i < nRows; i++) {
			Object[] row = new Object[header.length];
			int nv = 0;
			for (int j = 0; j < nVars; j++) {
				if (isDroppedColumn[j])
					continue;
				if (data[i][j] instanceof Object[]) {
					Object[] vi = (Object[]) data[i][j];
					for (int k = 0; k < vi.length; k++)
						row[nv] = vi[k];
				} else {
					row[nv] = data[i][j];
				}
				nv++;
			}
			if (!isDeletedRow[i])
				outputTable.addRow(row);
		}
		return outputTable;
	}
}
