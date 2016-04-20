/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.statistics.Statistics;
import com.h2o.online.data.DataTable;

public class CSVImporter extends DataImporter {

	public CSVImporter() {
	}

	private int nFileCols, nTableCols;
	private boolean isUnformatted;
	private boolean hasHeader;
	private boolean[] isCategorical;
	private String[] units;
	private int[] wordLengths;
	private int wordCount;
	private String[] variableNames;
	private String separator;
	private String[] separators = new String[] { "\t", ",", ";", ":", "|", " " };

	public boolean isTextTable(String fileName) {
		identifySeparator(fileName);
		return isUnformatted;
	}

	public DataTable getFlatTable(String fileName, int[] columnIndices) {
		boolean isRagged = false;
		boolean isCompressed = false;

		nFileCols = identifySeparator(fileName);

		if (columnIndices == null || columnIndices[0] < 0) {
			columnIndices = new int[nFileCols];
			for (int j = 0; j < nFileCols; j++) {
				columnIndices[j] = j;
			}
		}

		nTableCols = columnIndices.length;
		if (nTableCols < 1) {
			System.err.println("no columns passed to parseCsv()");
			return null;
		}

		if (isUnformatted)
			return null;
		computeVariableTypes(fileName, columnIndices);
		if (isCategorical == null)
			return null;

		BufferedReader fileReader;
		String record;
		try {
			fileReader = new BufferedReader(new FileReader(fileName));
			if (hasHeader)
				getHeader(fileReader);
			DataTable table = new DataTable("Data", null, variableNames, isCategorical, units);

			int rec = 0;
			while ((record = getCompleteCSVRecord(fileReader)) != null) {
				rec++;
				String[] dataRow = TextUtilities.split(record, true, separator);
				if (dataRow.length < nFileCols - 5)
					isRagged = true;
				if (dataRow.length < 1)
					break;
				String[] tableRow = codeValues(dataRow, columnIndices);
				if (tableRow != null && tableRow.length > 0) {
					table.addRow(tableRow);
				}
			}
			fileReader.close();
			table.isRagged = isRagged;
			if (isCompressed)
				table.setWeightVarIndex(nFileCols - 1);
			return table;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int identifySeparator(String fileName) {
		int nSeparators = separators.length;
		int nrows = Math.min(1000, (int) countLines(fileName));
		int ncols = 0;
		double[][] sepCounts = new double[nSeparators][nrows];
		BufferedReader fin;
		try {
			fin = new BufferedReader(new FileReader(fileName));
			String record;
			String header = getHeader(fin);
			int nRows = 0;
			Set<Integer> numBlanks = new HashSet<Integer>(); // to identify blank-separated word (text) fields
			int nWords = 0;
			int nDataItems = 0;
			while ((record = getCompleteCSVRecord(fin)) != null) {
				record = TextUtilities.compressSpaces(record);
				record = record.replaceAll("\".*?\"", "~"); // separate quoted strings
				if (record.length() == 0) {
					for (int j = 0; j < nSeparators; j++)
						sepCounts[j][nRows] = 0;
					continue;
				}
				String[] blanks = record.split(" ");
				numBlanks.add(blanks.length);
				nDataItems += blanks.length;
				for (int j = 0; j < blanks.length; j++) {
					try {
						Double.parseDouble(blanks[j]);
					} catch (NumberFormatException e2) {
						nWords++;
					}
				}
				for (int j = 0; j < nSeparators; j++) {
					String key = separators[j];
					String token = "[^" + key + "]";
					sepCounts[j][nRows] = record.replaceAll(token, "").length();
				}
				nRows++;
				if (nRows >= nrows)
					break;
			}
			double[] x = new double[nRows];
			separator = "";
			double minRange = Double.POSITIVE_INFINITY;
			int bestSeparator = -1;
			for (int j = 0; j < nSeparators; j++) {
				System.arraycopy(sepCounts[j], 0, x, 0, nRows);
				double[] stats = Statistics.compute(x, null);
				ncols = 1 + (int) stats[Statistics.MEAN];
				double range = stats[Statistics.RANGE];
				if (ncols > 1 && range < minRange) {
					bestSeparator = j;
					minRange = range;
				}
			}
			if (bestSeparator >= 0 && minRange < 3)
				separator = separators[bestSeparator];

			if (!header.contains(separator))
				isUnformatted = true;
			else if (separator.equals("")) { // text file has blanks separating words
				if (numBlanks.size() == 0 || numBlanks.size() > 1) {
					isUnformatted = true;
				} else {
					isUnformatted = false;
					ncols = 1;
				}
			} else {
				isUnformatted = false;
			}
			fin.close();

			/* check if there are more variable names than fields */
			fin = new BufferedReader(new FileReader(fileName));
			record = getHeader(fin);
			record = TextUtilities.compressSpaces(record);
			record = record.replaceAll("\".*?\"", "~"); // separate quoted strings
			String token = "[^" + separator + "]";
			int nFields = 1;
			if (!separator.equals(""))
				nFields = 1 + record.replaceAll(token, "").length();
			if (nFields > ncols)
				ncols = nFields;
			fin.close();
		} catch (IOException ie) {
			System.err.println("I/O exception in identifySeparator");
		}
		return ncols;
	}

	private String getHeader(BufferedReader reader) {
		return getCompleteCSVRecord(reader);
	}

	private void computeVariableTypes(String fileName, int[] columnIndices) {
		long nRecords = countLines(fileName);
		int mRows = (int) Math.min(100L, nRecords);
		isCategorical = new boolean[nTableCols];
		units = new String[nTableCols];
		Arrays.fill(units, "");
		int[] categorical = new int[nTableCols];
		int[] numerical = new int[nTableCols];
		int[] temporal = new int[nTableCols];
		boolean[] isZip = new boolean[nTableCols];
		Arrays.fill(isZip, true);
		hasHeader = true;

		BufferedReader fin;
		try {
			fin = new BufferedReader(new FileReader(fileName));
			String record = getHeader(fin);
			String[] varNames = TextUtilities.split(record, true, separator);
			recodeIllegalVariableNames(varNames, columnIndices);
			int nrows = 0;
			while ((record = getCompleteCSVRecord(fin)) != null) {
				String[] row = TextUtilities.split(record, false, separator);
				computeUnits(row, categorical, numerical, temporal, isZip, columnIndices);
				nrows++;
				if (nrows > mRows)
					break;
			}
			fin.close();
			int cutoff = nrows / 4;
			if (nrows < 10)
				cutoff = nrows;
			for (int j = 0; j < nTableCols; j++) {
				if (units[j].equals(Units.ZIPCODE)) {
					if (!isZip[j]) {
						units[j] = Units.NUMBER;
						categorical[j] = 0;
					}
				}
				if (categorical[j] > 0 && numerical[j] < cutoff) {
					isCategorical[j] = true;
				} else if (temporal[j] > 0 && numerical[j] < cutoff) {
					units[j] = Units.DATE;
				}
			}
		} catch (IOException ie) {
			System.err.println("I/O exception in computeVariableTypes");
			isCategorical = null;
		}
	}

	private void computeUnits(String[] row, int[] categorical, int[] numerical, int[] temporal, boolean[] isZip,
			int[] columnIndices) {
		int nv = Math.min(variableNames.length, row.length);
		for (int j = 0; j < nv; j++) {
			int col = columnIndices[j];
			if (variableNames[j].toLowerCase().endsWith("$")) {
				categorical[j]++;
				units[j] = Units.CATEGORY;
			} else {
				units[j] = Units.getUnits(row[col]);
				if (!units[j].equals(Units.ZIPCODE) && !row[col].equals(""))
					isZip[j] = false;
				if (units[j].equals(Units.DATE))
					temporal[j]++;
				else if (units[j].equals(Units.NUMBER))
					numerical[j]++;
				else if (Units.isCategoricalUnit(units[j]))
					categorical[j]++;
			}
		}
	}

	private void recodeIllegalVariableNames(String[] varNames, int[] columnIndices) {
		for (int j = 0; j < nFileCols; j++) {
			String name = varNames[j];
			if (name == null || name.length() == 0 || TextUtilities.isNumber(name))
				hasHeader = false;
		}
		variableNames = new String[nTableCols];
		for (int j = 0; j < nTableCols; j++) {
			String name = varNames[columnIndices[j]];
			if (hasHeader) {
				/* remove special characters */
				name = name.replaceAll("(\\W|^_)*", "");
				name = "_" + name;
				/* check for duplicates */
				for (int k = j + 1; k < varNames.length; k++) {
					if (name.equals(varNames[k]))
						varNames[k] += k;
				}
			} else {
				name = "Variable" + (j + 1);
			}
			variableNames[j] = name;
		}
	}

	private String getCompleteCSVRecord(BufferedReader reader) {
		String record = "";
		while (countQuotes(record) % 2 != 0) {
			String rec = "";
			try {
				rec = reader.readLine();
			} catch (IOException e) {
				return null;
			}
			if (rec == null)
				return null;
			record = record + " " + rec;
		}
		if (record.length() > 0)
			return record;
		else
			return null;
	}

	private int countQuotes(String record) {
		if (record == "")
			return -1;
		char[] r = record.toCharArray();
		int count = 0;
		for (int i = 0; i < r.length; i++) {
			if (r[i] == '"')
				count++;
		}
		return count;
	}

	private String[] codeValues(String[] s, int[] columnIndices) {
		String[] result = new String[nTableCols];
		Arrays.fill(result, " ");
		for (int j = 0; j < nTableCols; j++) {
			int col = columnIndices[j];
			if (units[j].equals(Units.US_PHONE_NUMBER)) {
				result[j] = TextUtilities.parseUSPhoneNumber(s[col]);
			} else if (units[j].equals(Units.DATE)) {
				Date d = DateUtilities.parseString(s[col]);
				if (d != null)
					result[j] = Long.toString(d.getTime() / 1000);
				else
					result[j] = ".";
			} else if (isCategorical[j]) {
				result[j] = s[col];
				result[j] = result[j].replaceAll("\"\"", "\"");
				if (TextUtilities.isMissingValueSymbol(result[j]))
					result[j] = Globals.MISSING_STRING;
			} else {
				result[j] = TextUtilities.parseNumber(s[col], units[j]);
			}
		}
		return result;
	}

	private static long countLines(String fileName) {
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			System.err.println("File " + fileName + " not found.");
			System.exit(1);
		}
		LineNumberReader lnr = new LineNumberReader(fr);
		try {
			lnr.skip(Long.MAX_VALUE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1 + lnr.getLineNumber();
	}
}