/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.EigenSystems;
import com.h2o.online.analytics.util.Math2;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.Cognostics;
import com.h2o.online.analytics.util.statistics.Statistics;
import com.h2o.online.data.util.DateUtilities;
import com.h2o.online.data.util.Units;

public class DataTable {
	private int nCols;
	private String[] rowNames;
	private String[] colNames;
	private String[] rawNames;
	private boolean[] isCategorical; // true if element corresponds to a string variable column
	private String[] units; // time, currency, weight, etc.
	private String[] transforms;
	private Map<String, HashMap<String, Integer>> categories;
	private List<Object[]> rowList;
	private int weightVar = -1;
	private int initialYear = -1;
	private int[] facets;
	public boolean isDirected;
	public boolean hasMissingValues;
	public boolean isRagged;
	public static final String NONE = "none", INVERSE = "inverse", LOG = "log", SQRT = "sqrt", ATANH = "arctanh",
			SQR = "square", LOGITP = "logitp", LOGITPCT = "logitpct", LOG2 = "log2", LOG10 = "log10";
	private static final String[] powers = new String[] { INVERSE, LOG, SQRT, SQR };

	private ArrayList<String> categoricalVariableNames;
	private ArrayList<String> continuousVariableNames;
	private String name;
	private String type;
	public static final String COVARIANCE = "covariance", CORRELATION = "correlation", SIMILARITY = "similarity",
			DISSIMILARITY = "dissimilarity", RECTANGULAR = "rectangular", SYMMETRIC = "symmetric", SQUARE = "square",
			TEXT = "text", CROSS = "cross", NEST = "nest", DATA = "data";

	private void copyTableInstanceVariables(DataTable newTable) {
		/* Keep this up to date! See instance variables above. */
		newTable.weightVar = weightVar;
		newTable.facets = facets;
		newTable.isDirected = isDirected;
		newTable.hasMissingValues = hasMissingValues;
		newTable.isRagged = isRagged;
		newTable.initialYear = initialYear;
	}

	/*
	 * A FlatTable is either rectangular or ragged. A Ragged table consists of a
	 * nRows * nCols rectangular table (nCols >= 0) concatenated to a ragged 2D
	 * array. Fields corresponding to columns (colNames, isCategorical, etc.)
	 * are null if nCols == 0.
	 */
	public DataTable(String name, String[] rowNames, String[] colNames, boolean[] isCategorical, String[] units) {
		for (int j = 0; j < colNames.length; j++) {
			if (colNames[j] != null)
				colNames[j] = colNames[j].replaceAll("[^\\x20-\\x7E]", "");
		}
		this.name = name;
		this.rowNames = rowNames;
		this.rawNames = colNames;
		this.isCategorical = isCategorical;
		this.units = units;
		rowList = new ArrayList<Object[]>();
		nCols = 0;
		if (colNames != null)
			nCols = colNames.length;
		if (nCols == 0)
			nCols = isCategorical.length;
		if (this.units == null)
			this.units = new String[nCols];
		for (int j = 0; j < nCols; j++) {
			if (this.units[j] == null)
				this.units[j] = "";
		}
		initialize();
	}

	private void initialize() {
		transforms = new String[nCols];
		categories = new HashMap<String, HashMap<String, Integer>>();
		colNames = new String[nCols];
		if (nCols > 0)
			System.arraycopy(rawNames, 0, colNames, 0, nCols);
		categoricalVariableNames = new ArrayList<String>();
		continuousVariableNames = new ArrayList<String>();
		for (int j = 0; j < nCols; j++) {
			if (isCategorical[j]) {
				HashMap<String, Integer> cats = new HashMap<String, Integer>();
				categories.put(rawNames[j], cats);
				categoricalVariableNames.add(rawNames[j]);
			} else {
				continuousVariableNames.add(rawNames[j]);
			}
		}
	}

	public void addRow(String[] string) {
		/* used for reading data file into table */
		Object[] row = new Object[string.length];
		for (int j = 0; j < string.length; j++) {
			if (isCategorical[j]) {
				if (string[j] == null)
					string[j] = Globals.MISSING_STRING;
				string[j] = string[j].trim();
				row[j] = string[j];
				if (Globals.isMissing(string[j])) {
					row[j] = Globals.MISSING_STRING;
					hasMissingValues = true;
				} else {
					HashMap<String, Integer> cats = categories.get(rawNames[j]);
					if (!cats.containsKey(string[j]))
						cats.put(string[j], new Integer(cats.size()));
				}
			} else {
				try {
					row[j] = new Double(string[j]);
				} catch (NumberFormatException e) {
					row[j] = new Double(Globals.MISSING_VALUE);
					hasMissingValues = true;
				}
			}
		}
		rowList.add(row);
	}

	public void addRow(Object[] row) {
		/* used for all other table builders, including ragged array */
		for (int j = 0; j < row.length; j++) {
			if (row.length == isCategorical.length && row[j] != null && isCategorical[j] && row[j] instanceof String) {
				String string = ((String) row[j]).trim();
				if (!Globals.isMissing(string)) {
					HashMap<String, Integer> cats = categories.get(rawNames[j]);
					if (!cats.containsKey(string))
						cats.put(string, new Integer(cats.size()));
				}
			}
		}
		rowList.add(row);
	}

	public void removeRow(int row) {
		rowList.remove(row);
	}

	public String getName() {
		return name;
	}

	public void setRowNames(String[] rowNames) {
		this.rowNames = rowNames;
	}

	public String[] getRowNames() {
		if (rowNames != null)
			return rowNames;
		else
			return makeRowNames();
	}

	private String[] makeRowNames() {
		int labelVar = findLabelVariable();
		if (labelVar < 0) {
			String[] result = new String[rowList.size()];
			for (int i = 0; i < rowList.size(); i++)
				result[i] = "" + (i + 1);
			return result;
		} else {
			String[] result = new String[getNumRows()];
			for (int i = 0; i < getNumRows(); i++) {
				if (isCategorical[labelVar]) {
					result[i] = (rowList.get(i))[labelVar].toString();
				} else if (units[labelVar].equals(Units.DATE)) {
					Date d = new Date((long) getRawDoubleValue(i, labelVar) * 1000);
					result[i] = DateUtilities.formatMonthDayYear(d);
				} else {
					result[i] = String.valueOf((int) getRawDoubleValue(i, labelVar));
				}
			}
			return result;
		}
	}

	private int findLabelVariable() {
		int nRows = getNumRows();
		int nCols = getNumCols();
		if (nRows < 10 || nCols == 1)
			return -1;
		for (int j = 0; j < getNumCols(); j++) {
			if (units[j].equals(Units.DATE))
				return j;
			Object[] col = getRawColumn(j);
			Set<String> values = new HashSet<String>();
			if (isCategorical[j]) {
				String[] vals = new String[nRows];
				for (int i = 0; i < nRows; i++)
					vals[i] = (String) col[i];
				if (DateUtilities.isDays(vals) || DateUtilities.isMonths(vals) || DateUtilities.isYears(vals))
					return j;
				for (int i = 0; i < nRows; i++)
					values.add(vals[i]);
				if (values.size() == nRows)
					return j;
			}
		}
		return -1;
	}

	public int getTimeColumn() {
		for (int j = 0; j < units.length; j++) {
			if (units[j].equals(Units.DATE))
				return j;
		}
		return -1;
	}

	public String[] getColNames() {
		return colNames;
	}

	public String[] getRawColNames() {
		return rawNames;
	}

	public void setColNames(String[] colNames) {
		this.colNames = colNames;
	}

	public int getColIndex(String name) {
		for (int j = 0; j < colNames.length; j++) {
			if (colNames[j].equals(name))
				return j;
		}
		return -1;
	}

	public int[] getFacets() {
		return facets;
	}

	public void setFacets(int[] facets) {
		this.facets = facets;
	}

	public Map<String, HashMap<String, Integer>> getCategoriesMap() {
		return categories;
	}

	public void setCategoriesMap(Map<String, HashMap<String, Integer>> categories) {
		this.categories = categories;
	}

	public int getCategoryIndex(int col, String cat) {
		HashMap<String, Integer> cats2 = categories.get(rawNames[col]);
		Integer index = cats2.get(cat);
		if (index != null)
			return index.intValue();
		else
			return -1;
	}

	public String[] getCategories(int col) {
		HashMap<String, Integer> cats = categories.get(rawNames[col]);
		if (cats == null)
			return null;
		String[] catStringArray = cats.keySet().toArray(new String[0]);
		return catStringArray;
	}

	public void setCategories(int col, String[] cats) {
		HashMap<String, Integer> catMap = new HashMap<String, Integer>();
		for (int j = 0; j < cats.length; j++)
			catMap.put(cats[j], j);
		categories.put(rawNames[col], catMap);
	}

	public void sortCategoriesNumerically(int col) {
		//		LinkedHashSet<String> lhs = categories.get(rawNames[col]);
		//		if (lhs == null)
		//			return;
		//		String[] cats = lhs.toArray(new String[lhs.size()]);
		//		Arrays.sort(cats, 0, cats.length, new Comparator<Object>() {
		//			@Override
		//			public int compare(Object object1, Object object2) {
		//				Integer first = Integer.parseInt((String) object1);
		//				Integer second = Integer.parseInt((String) object2);
		//				return first.compareTo(second);
		//			}
		//		});
		//		lhs = new LinkedHashSet<String>();
		//		for (int j = 0; j < cats.length; j++)
		//			lhs.add(cats[j]);
		//		categories.put(rawNames[col], lhs);
	}

	public void sortCategoriesAlphabetically(int col) {
		//		LinkedHashSet<String> lhs = categories.get(rawNames[col]);
		//		if (lhs == null)
		//			return;
		//		String[] cats = lhs.toArray(new String[lhs.size()]);
		//		Arrays.sort(cats);
		//		lhs = new LinkedHashSet<String>();
		//		for (int j = 0; j < cats.length; j++)
		//			lhs.add(cats[j]);
		//		categories.put(rawNames[col], lhs);
	}

	public boolean hasRow(int row) {
		return row < rowList.size();
	}

	public Object[] getRawRow(int row) {
		Object[] r = rowList.get(row);
		Object[] result = new Object[r.length];
		System.arraycopy(r, 0, result, 0, r.length);
		return result;
	}

	public Object[] getTransformedRow(int row) {
		Object[] r = rowList.get(row);
		Object[] result = new Object[r.length];
		for (int j = 0; j < r.length; j++) {
			if (j < nCols && r[j] instanceof Double && transforms != null) {
				result[j] = new Double(transform(((Double) r[j]).doubleValue(), j));
			} else {
				result[j] = r[j];
			}
		}
		return result;
	}

	public Object[] getRaggedRow(int row) {
		Object[] r = rowList.get(row);
		Object[] result = new Object[r.length - nCols];
		System.arraycopy(r, nCols, result, 0, r.length - nCols);
		return result;
	}

	public Object[] getRawColumn(int col) {
		Object[] result = new Object[rowList.size()];
		for (int i = 0; i < rowList.size(); i++)
			result[i] = (rowList.get(i))[col];
		return result;
	}

	public String[] getRawStringColumn(int col) {
		String[] result = new String[rowList.size()];
		for (int i = 0; i < rowList.size(); i++)
			result[i] = (String) (rowList.get(i))[col];
		return result;
	}

	public double[] getRawDoubleColumn(int col) {
		if (isCategoricalVariable(col))
			return null;
		double[] result = new double[rowList.size()];
		for (int i = 0; i < rowList.size(); i++)
			try {
				result[i] = ((Double) (rowList.get(i))[col]).doubleValue();
			} catch (Exception e) {
				result[i] = Globals.MISSING_VALUE;
			}
		return result;
	}

	public Object[][] getRawTable() {
		Object[][] result = new Object[rowList.size()][];
		for (int i = 0; i < rowList.size(); i++)
			result[i] = rowList.get(i);
		return result;
	}

	public double[][] getRawDoubleTable(int[] variables) {
		/* codes categorical variables as indices of categories */
		int nRows = getNumRows();
		int nCols = getNumCols();
		double[][] x = new double[nRows][nCols];
		int nVars = nCols;
		if (variables != null)
			nVars = variables.length;
		for (int i = 0; i < nRows; i++) {
			for (int jv = 0; jv < nVars; jv++) {
				int j = jv;
				if (variables != null)
					j = variables[jv];
				if (isMissing(i, j))
					continue;
				if (isCategoricalVariable(j)) {
					String category = (String) getRawRow(i)[j];
					int categoryIndex = getCategoryIndex(j, category);
					x[i][j] = categoryIndex;
				} else {
					x[i][j] = getTransformedDoubleValue(i, j);
				}
			}
		}
		double[][] stats = Statistics.compute(x, null);
		for (int i = 0; i < nRows; i++) {
			for (int jv = 0; jv < nVars; jv++) {
				int j = jv;
				if (variables != null)
					j = variables[jv];
				/* unitize only continuous variables so compression will be done within each category */
				if (!isCategoricalVariable(j))
					x[i][j] = (x[i][j] - stats[Statistics.MIN][j]) / stats[Statistics.RANGE][j];
			}
		}
		return x;
	}

	public double getRawDoubleValue(int row, int col) {
		Object[] o = rowList.get(row);
		if (col < nCols && isCategorical[col])
			return Globals.MISSING_VALUE;
		else
			try {
				return ((Double) o[col]).doubleValue();
			} catch (Exception e) {
				return Globals.MISSING_VALUE;
			}
	}

	public double[][] getRawDoubleValues() {
		int nRows = getNumRows();
		int nCols = getNumCols();
		double[][] result = new double[nRows][];
		for (int row = 0; row < nRows; row++) {
			Object[] o = rowList.get(row);
			double[] rowdata = new double[o.length];
			for (int j = 0; j < o.length; j++) {
				if (j < nCols && isCategorical[j])
					rowdata[j] = Globals.MISSING_VALUE;
				else
					rowdata[j] = ((Double) o[j]).doubleValue();
			}
			result[row] = rowdata;
		}
		if (nRows > 1 && nRows == nCols)
			fillMissingTriangleBelowOrAboveDiagonal(result);
		return result;
	}

	public String getRawStringValue(int row, int col) {
		Object[] r = rowList.get(row);
		if (r == null || r[col] == null)
			return "";
		else
			return r[col].toString();
	}

	public double getTransformedDoubleValue(int row, int col) {
		double x = getRawDoubleValue(row, col);
		if (transforms == null || transforms[col] == null)
			return x;
		else
			return transform(x, col);
	}

	private double transform(double x, int col) {
		String transf = transforms[col];
		if (transf == null)
			return x;
		else if (transf.equals(INVERSE))
			x = 1 / x;
		else if (transf.equals(LOG))
			x = Math.log(x);
		else if (transf.equals(LOG2))
			x = Math.log(x) / Math.log(2);
		else if (transf.equals(LOG10))
			x = Math.log(x) / Math.log(10);
		else if (transf.equals(SQRT))
			x = Math.sqrt(x);
		else if (transf.equals(SQR))
			x = x * x;
		else if (transf.equals(ATANH))
			x = Math2.atanh(x);
		else if (transf.equals(LOGITP))
			x = Math.log(x / (1 - x));
		else if (transf.equals(LOGITPCT))
			x = Math.log((x / 100.0) / (1.0 - x / 100.0));
		if (Double.isInfinite(x))
			x = Globals.MISSING_VALUE;
		return x;
	}

	public double getLogBase(int col) {
		if (transforms == null || transforms[col] == null)
			return 0;
		if (transforms[col].equals(LOG2))
			return 2;
		if (transforms[col].equals(LOG10))
			return 10;
		return 1;
	}

	public void setValue(double x, int row, int col) {
		(rowList.get(row))[col] = new Double(x);
	}

	public void setValue(String s, int row, int col) {
		(rowList.get(row))[col] = s;
	}

	public boolean[] isCategorical() {
		return isCategorical;
	}

	public String[] getUnits() {
		return units;
	}

	public void setUnits(String[] units) {
		this.units = units;
		if (this.units == null || this.units.length != nCols)
			this.units = new String[nCols];
		for (int j = 0; j < nCols; j++) {
			if (this.units[j] == null)
				this.units[j] = "";
		}
	}

	public boolean isTimeSeriesColumn(int col) {
		int nRows = getNumRows();
		if (!getMatrixType().equals(RECTANGULAR) || nRows < Globals.TOO_SMALL || isSorted(col) || nRows > 10000
				|| isCategoricalVariable(col) || countValues(col) < Globals.TOO_SMALL)
			return false;
		double[] x = new double[nRows];
		for (int i = 0; i < nRows; i++)
			x[i] = getRawDoubleValue(i, col);
		double[] bg = Cognostics.computeBreuschGodfreyStatistic(x, getWeights());
		return (bg[0] > .05 && bg[1] < .01);
	}

	public String getMatrixType() {
		if (type != null)
			return type;
		double[][] t = getRawDoubleValues();
		if (isSquare(t)) {
			if (isSymmetric(t)) {
				if (isCorrelation(t)) {
					type = CORRELATION;
					return type;
				}
				if (isCovariance(t)) {
					type = COVARIANCE;
					return type;
				}
				if (isDissimilarity(t)) {
					type = DISSIMILARITY;
					return type;
				}
				type = SIMILARITY;
				return type;
			}
			type = SQUARE;
			return type;
		}
		type = RECTANGULAR;
		return type;
	}

	private void fillMissingTriangleBelowOrAboveDiagonal(double[][] t) {
		boolean isMissingAboveDiagonal = true;
		boolean isMissingBelowDiagonal = true;
		for (int i = 1; i < getNumCols(); i++) {
			for (int j = 0; j < i; j++) {
				if (!Globals.isMissing(t[i][j]))
					isMissingBelowDiagonal = false;
				if (!Globals.isMissing(t[j][i]))
					isMissingAboveDiagonal = false;
			}
		}

		if (!isMissingBelowDiagonal && !isMissingAboveDiagonal || isMissingBelowDiagonal && isMissingAboveDiagonal)
			return;

		for (int i = 1; i < getNumCols(); i++) {
			for (int j = 0; j < i; j++) {
				if (isMissingBelowDiagonal)
					t[i][j] = t[j][i];
				if (isMissingAboveDiagonal)
					t[j][i] = t[i][j];
			}
		}
	}

	private boolean isSquare(double[][] t) {
		if (t == null)
			return false;
		if (t[0] == null)
			return false;
		if (t.length != t[0].length)
			return false;
		for (int j = 0; j < t.length; j++) {
			if (isCategorical[j])
				return false;
		}
		return true;
	}

	private boolean isSymmetric(double[][] t) {
		if (!isSquare(t))
			return false;
		for (int i = 1; i < getNumCols(); i++) {
			for (int j = 0; j < i; j++) {
				if (Globals.isMissing(t[i][j]) || Globals.isMissing(t[j][i]))
					continue;
				if (t[i][j] != t[j][i])
					return false;
			}
		}
		return true;
	}

	private boolean isCorrelation(double[][] t) {
		if (!isSymmetric(t))
			return false;
		for (int i = 0; i < t.length; i++) {
			if (t[i][i] != 1)
				return false;
		}
		for (int i = 1; i < t.length; i++) {
			for (int j = 0; j < i; j++) {
				if (Globals.isMissing(t[i][j]))
					continue;
				if (!Globals.isMissing(t[i][j]) && Math.abs(t[i][j]) > 1)
					return false;
			}
		}
		return isGramian(t);
	}

	private boolean isCovariance(double[][] t) {
		if (!isSymmetric(t))
			return false;
		int p = 0;
		for (int i = 0; i < t.length; i++) {
			if (Globals.isMissing(t[i][i]))
				p++;
			if (t[i][i] <= 0)
				return false;
		}
		if (p == t.length)
			return false;
		for (int i = 1; i < t.length; i++) {
			for (int j = 0; j < i; j++) {
				double tij = t[i][j] / Math.sqrt(t[i][i] * t[j][j]);
				if (!Globals.isMissing(tij) && Math.abs(tij) > 1)
					return false;
			}
		}
		return isGramian(t);
	}

	private boolean isDissimilarity(double[][] t) {
		if (!isSymmetric(t))
			return false;
		for (int i = 0; i < t.length; i++)
			if (t[i][i] != 0 && !Globals.isMissing(t[i][i]))
				return false;
		int n = 0;
		int p = 0;
		/* compute proportion of triads violating triangle inequality */
		for (int i = 1; i < t.length; i++) {
			for (int j = 0; j < i; j++) {
				if (Globals.isMissing(t[i][j]))
					continue;
				for (int k = 0; k < t.length; k++) {
					if (k == i || k == j)
						continue;
					if (Globals.isMissing(t[i][k]) || Globals.isMissing(t[k][j]))
						continue;
					n++;
					if (t[i][j] > t[i][k] + t[k][j])
						p++;
				}
			}
		}
		return (double) p / n <= .1;
	}

	private boolean isGramian(double[][] t) {
		if (!isSymmetric(t))
			return false;
		int n = t.length;
		double[][] eigvec = new double[n][n];
		double[] eigval = new double[n];
		EigenSystems.eigenSymmetric(t, eigvec, eigval);
		return eigval[n - 1] >= -Globals.FUZZ;
	}

	public boolean isCategoricalVariable(int col) {
		return isCategorical[col];
	}

	public boolean isSmallNonnegativeIntegerVariable(int col) {
		/* true if each x[i] is an integer and 0 <= x[i] <= 9 */
		if (isCategorical[col])
			return false;
		for (int i = 0; i < getNumRows(); i++) {
			double x = getRawDoubleValue(i, col);
			if (!Globals.isMissing(x) && getWeight(i) > 0) {
				if (Math.floor(x) != x || x < 0 || x > 9)
					return false;
			}
		}
		return true;
	}

	public boolean hasSmallNumberOfValues(int col) {
		if (isCategorical[col])
			return false;
		Set<Double> ts = new HashSet<Double>();
		for (int i = 0; i < getNumRows(); i++) {
			double x = getRawDoubleValue(i, col);
			if (!Globals.isMissing(x) && getWeight(i) > 0)
				ts.add(new Double(x));
		}
		return ts.size() > 0 && ts.size() < 10;
	}

	public boolean isCountVariable(int col) {
		if (isCategorical[col])
			return false;
		double lambda = 0;
		double sum = 0;
		for (int i = 0; i < getNumRows(); i++) {
			double x = getRawDoubleValue(i, col);
			double weight = getWeight(i);
			if (!Globals.isMissing(x) && weight > 0) {
				if (Math.floor(x) != x || x < 0)
					return false;
				lambda += weight * x;
				sum += weight;
			}
		}
		lambda /= sum;
		boolean isCount = lambda < 50;
		return isCount;
	}

	public int countValues(int col) {
		if (isCategorical[col]) {
			HashMap<String, Integer> cats = categories.get(rawNames[col]);
			return cats.size();
		}
		Map<Object, Integer> values = new HashMap<Object, Integer>();
		for (int i = 0; i < getNumRows(); i++) {
			Object key = (rowList.get(i))[col];
			if (Globals.isMissing((Double) key))
				continue;
			if (values.containsKey(key)) {
				int count = (values.get(key)).intValue();
				values.put(key, new Integer(count + 1));
			} else {
				values.put(key, new Integer(1));
			}
		}
		int nVal = values.size();
		return nVal;
	}

	public int countZeros(int col) {
		if (isCategorical[col])
			return 0;
		int count = 0;
		for (int i = 0; i < getNumRows(); i++) {
			double x = getRawDoubleValue(i, col);
			if (x == 0)
				count++;
		}
		return count;

	}

	public int getWeightVarIndex() {
		return weightVar;
	}

	public void setWeightVarIndex(int weightVar) {
		this.weightVar = weightVar;
	}

	public double getWeight(int row) {
		if (weightVar < 0)
			return 1;
		else {
			return getRawDoubleValue(row, weightVar);
		}
	}

	public double[] getWeights() {
		if (weightVar < 0)
			return null;
		double[] wt = new double[getNumRows()];
		for (int i = 0; i < wt.length; i++)
			wt[i] = ((Double) (rowList.get(i))[weightVar]).doubleValue();
		return wt;
	}

	public int getNumCols() {
		return nCols;
	}

	public int getNumRows() {
		return rowList.size();
	}

	public double[] getTransformedDataMinMax(int col) {
		double min, max;
		if (isCategorical[col]) {
			min = 0;
			max = 0;
		} else {
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < getNumRows(); i++) {
				double wt = getWeight(i);
				double x = getTransformedDoubleValue(i, col);
				if (wt > 0 && !Globals.isMissing(x)) {
					if (x < min)
						min = x;
					if (x > max)
						max = x;
				}
			}
		}
		return new double[] { min, max };
	}

	public double[] getRawDataMinMax(int col) {
		double min, max;
		if (isCategorical[col]) {
			min = 0;
			max = 0;
		} else {
			min = Double.POSITIVE_INFINITY;
			max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < getNumRows(); i++) {
				double wt = getWeight(i);
				double x = getRawDoubleValue(i, col);
				if (wt > 0 && !Globals.isMissing(x)) {
					if (x < min)
						min = x;
					if (x > max)
						max = x;
				}
			}
		}
		return new double[] { min, max };
	}

	public void setPowerTransforms(int[] iXY, boolean useModalTransform) {
		int nXY = iXY.length;
		for (int j = 0; j < nXY; j++)
			pickBestPowerTransform(iXY[j]);

		if (useModalTransform && nXY > 1) {
			Map<String, Integer> counts = new HashMap<String, Integer>();
			for (int j = 0; j < nXY; j++) {
				Object count = counts.get(transforms[iXY[j]]);
				if (count != null)
					counts.put(transforms[iXY[j]], new Integer(((Integer) count).intValue() + 1));
				else
					counts.put(transforms[iXY[j]], new Integer(1));
			}
			String mode = null;
			int freq = 0;
			for (int j = 0; j < nXY; j++) {
				int count = counts.get(transforms[iXY[j]]).intValue();
				if (count > freq) {
					mode = transforms[iXY[j]];
					freq = count;
				}
			}
			if (freq <= nXY / 2)
				mode = null;
			for (int j = 0; j < nXY; j++) {
				colNames[iXY[j]] = rawNames[iXY[j]];
				setTransform(mode, iXY[j]);
			}
		}
	}

	public void setPowerTransforms(int[] iX, int[] iY) {
		if (iX != null) {
			for (int j = 0; j < iX.length; j++)
				pickBestPowerTransform(iX[j]);
		}
		if (iY != null)
			setPowerTransforms(iY, true);
	}

	private void pickBestPowerTransform(int col) {
		if (transforms == null || transforms[col] != null)
			return;
		double logSkew = 0, invSkew = 0, sqrtSkew = 0, sqrSkew = 0;
		if (!isCategorical[col]) {
			if (Math.abs(computeLSkewness(col)) < .15)
				return;
			double rawSkew = Math.abs(computeBowleySkewness(col));
			double[] minmax = getTransformedDataMinMax(col);

			double proportionZeros = (double) countZeros(col) / (double) getNumRows();
			if (minmax[0] == 0 && proportionZeros > .05) {
				logSkew = Double.POSITIVE_INFINITY;
				invSkew = Double.POSITIVE_INFINITY;
				transforms[col] = SQRT;
				sqrtSkew = Math.abs(computeBowleySkewness(col));
				transforms[col] = SQR;
				sqrSkew = Math.abs(computeBowleySkewness(col));
				transforms[col] = NONE;
			} else if (minmax[0] > 0 || proportionZeros < .05) {
				transforms[col] = LOG;
				logSkew = Math.abs(computeBowleySkewness(col));
				transforms[col] = INVERSE;
				invSkew = Math.abs(computeBowleySkewness(col));
				transforms[col] = SQRT;
				sqrtSkew = Math.abs(computeBowleySkewness(col));
				transforms[col] = SQR;
				sqrSkew = Math.abs(computeBowleySkewness(col));
				transforms[col] = NONE;
			} else {
				return;
			}

			double[] skews = new double[] { rawSkew, logSkew, invSkew, sqrtSkew, sqrSkew };
			String[] codes = new String[] { NONE, LOG, INVERSE, SQRT, SQR };
			int[] ind = Sorts.indexedDoubleArraySort(skews, 0, 0);
			int lowestIndex = ind[0];
			double lowestSkew = skews[lowestIndex];
			String lowestCode = codes[lowestIndex];
			if (Math.abs(lowestSkew / rawSkew) < .75)
				setTransform(lowestCode, col);
			if (transforms[col].equals(SQRT) && lowestSkew / logSkew > .75) {
				colNames[col] = rawNames[col];
				setTransform(LOG, col);
			}
			if (transforms[col].equals(NONE))
				transforms[col] = null;
		}
	}

	private double computeBowleySkewness(int col) {
		int n = rowList.size();
		double[] data = new double[n];
		for (int i = 0; i < n; i++)
			data[i] = getTransformedDoubleValue(i, col);
		return Statistics.bowleySkewness(data, getWeights());
	}

	private double computeLSkewness(int col) {
		int n = rowList.size();
		double[] data = new double[n];
		for (int i = 0; i < n; i++)
			data[i] = getTransformedDoubleValue(i, col);
		return Statistics.LMoments(data, null, 3)[2];
	}

	public void setTransformLabel(int col) {
		if (transforms == null)
			return;
		String transf = transforms[col];
		if (transf == null)
			return;
		if (transf.equals(INVERSE))
			colNames[col] = INVERSE + "(" + rawNames[col] + ")";
		else if (transf.equals(LOG) || transf.equals(LOG2) || transf.equals(LOG10))
			colNames[col] = LOG + "(" + rawNames[col] + ")";
		else if (transf.equals(SQRT))
			colNames[col] = SQRT + "(" + rawNames[col] + ")";
		else if (transf.equals(SQR))
			colNames[col] = SQR + "(" + rawNames[col] + ")";
		else if (transf.equals(ATANH))
			colNames[col] = ATANH + "(" + rawNames[col] + ")";
		else if (transf.equals(LOGITP))
			colNames[col] = LOG + "(" + rawNames[col] + "/(1 - " + rawNames[col] + "))";
		else if (transf.equals(LOGITPCT))
			colNames[col] = LOG + "((" + rawNames[col] + "/ 100)" + "/(1 - " + rawNames[col] + "/ 100)" + ")";
	}

	public void clearTransforms() {
		transforms = new String[nCols];
		System.arraycopy(rawNames, 0, colNames, 0, rawNames.length);
	}

	public String[] getTransforms() {
		return transforms;
	}

	public boolean hasTransforms() {
		if (transforms == null)
			return false;
		for (int i = 0; i < transforms.length; i++) {
			if (transforms[i] != null)
				return true;
		}
		return false;
	}

	public void setTransform(String transf, int col) {
		if (transforms == null)
			return;
		if (transf == null || transf.equals(NONE)) {
			transforms[col] = null;
			colNames[col] = rawNames[col];
		} else {
			if (transf.equals(LOG)) {
				double[] minmax = getTransformedDataMinMax(col);
				if (Math.log10(minmax[1]) - Math.log10(minmax[0]) > 3)
					transf = LOG10;
				else
					transf = LOG2;
			}
			transforms[col] = transf;
			setTransformLabel(col);
		}
	}

	public void setTransforms(String transf) {
		clearTransforms();
		for (int j = 0; j < transforms.length; j++) {
			if (j != weightVar)
				setTransform(transf, j);
		}
	}

	public void setTransforms(int power) {
		clearTransforms();
		for (int j = 0; j < transforms.length; j++) {
			if (j != weightVar)
				setTransform(powers[power], j);
		}
	}

	public void setTransforms(String[] transforms) {
		this.transforms = transforms;
	}

	public boolean mayBeCorrelations(int col) {
		double[] minmax = getTransformedDataMinMax(col);
		return minmax[0] >= -1.0 && minmax[1] <= 1.0 && minmax[1] - minmax[0] > 1.0;
	}

	public boolean mayBeProportions(int col) {
		double[] minmax = getTransformedDataMinMax(col);
		return minmax[0] > 0.0 && minmax[1] < 1.0 && minmax[1] - minmax[0] > 0.25;
	}

	public boolean mayBePercentages(int col) {
		double[] minmax = getTransformedDataMinMax(col);
		return minmax[0] > 0.0 && minmax[1] < 100.0 && minmax[1] - minmax[0] > 25.0;
	}

	public String missingPattern(int row, int[] cols) {
		String b = "";
		for (int j = 0; j < cols.length; j++) {
			if (isMissing(row, cols[j]))
				b += "0";
			else
				b += "1";
		}
		return b;
	}

	public boolean isSorted(int col) {
		if (isCategorical[col])
			return false;
		boolean ascending = true, descending = true;
		double previous = getRawDoubleValue(0, col);
		for (int i = 1; i < getNumRows() && (ascending || descending); i++) {
			double current = getRawDoubleValue(i, col);
			ascending = ascending && current >= previous;
			descending = descending && current <= previous;
			previous = current;
		}
		return ascending || descending;
	}

	public boolean isIncreasing(int col) {
		if (isCategorical[col])
			return false;
		double previous = getRawDoubleValue(0, col);
		for (int i = 1; i < getNumRows(); i++) {
			double current = getRawDoubleValue(i, col);
			if (current < previous)
				return false;
			previous = current;
		}
		return true;
	}

	public boolean isIndexVariable(int col) {
		if (!isIntegerVariable(col))
			return false;
		double xm1 = getRawDoubleValue(0, col);
		for (int i = 1; i < getNumRows(); i++) {
			double x = getRawDoubleValue(i, col);
			if ((x - xm1) != 1.0)
				return false;
			xm1 = x;
		}
		return true;
	}

	public boolean isIntegerVariable(int col) {
		for (int i = 0; i < getNumRows(); i++) {
			double x = getRawDoubleValue(i, col);
			if (!Globals.isMissing(x)) {
				if (Math.floor(x) != x)
					return false;
			}
		}
		return true;
	}

	public boolean isMissing(int row, int col) {
		if (isCategorical[col]) {
			String s = getRawStringValue(row, col);
			if (Globals.isMissing(s))
				return true;
		} else {
			double x = getRawDoubleValue(row, col);
			if (Globals.isMissing(x))
				return true;
		}
		return false;
	}

	public List<Double> getMissingNumericValues(int col) {
		if (isCategorical[col])
			return new ArrayList<Double>();
		Set<Double> valueSet = new TreeSet<Double>();
		for (int i = 0; i < rowList.size(); i++) {
			double value = getRawDoubleValue(i, col);
			if (Globals.isMissing(value) || (int) value != value)
				return new ArrayList<Double>();
			valueSet.add(value);
		}
		List<Double> missingValues = new ArrayList<Double>();
		Object[] values = valueSet.toArray();
		int n = values.length;
		int numMissing = Math.min(n / 2, 9);
		for (int i = 0; i < numMissing; i++) {
			Double small = (Double) values[i];
			Double large = (Double) values[n - i - 1];
			if (i == 0 && small == 0 && large > 10)
				missingValues.add(small);
			if (small < 0 && Globals.numericMissingValues.contains(-small))
				missingValues.add(small);
			if (i == 0 && small > 0 && large == 9)
				missingValues.add(large);
			if (large > 9 && Globals.numericMissingValues.contains(large))
				missingValues.add(large);
		}
		return missingValues;
	}

	public void recodeMissingNumericValues(int col, Double missingValue) {
		for (int i = 0; i < rowList.size(); i++) {
			Double value = getRawDoubleValue(i, col);
			if (value.equals(missingValue)) {
				Object[] row = rowList.get(i);
				row[col] = new Double(Globals.MISSING_VALUE);
				rowList.set(i, row);
			}
		}
	}

	public void sort(int col, boolean isDescending) {
		int n = rowList.size();
		int[] index = new int[n];
		if (isCategorical[col]) {

		} else {
			double[] data = new double[n];
			for (int i = 0; i < n; i++)
				data[i] = getTransformedDoubleValue(i, col);
			index = Sorts.indexedDoubleArraySort(data, 0, 0);

		}
		List<Object[]> sortedRowList = new ArrayList<Object[]>();
		String[] sortedRowNames = null;
		if (rowNames != null)
			sortedRowNames = new String[rowNames.length];
		for (int i = 0; i < n; i++) {
			if (isDescending) {
				sortedRowList.add(rowList.get(index[n - i - 1]));
				if (rowNames != null)
					sortedRowNames[i] = rowNames[index[n - i - 1]];
			} else {
				sortedRowList.add(rowList.get(index[i]));
				if (rowNames != null)
					sortedRowNames[i] = rowNames[index[i]];
			}
		}
		rowList = sortedRowList;
		rowNames = sortedRowNames;
	}

	public void reverseOrder() {
		int n = rowList.size();
		List<Object[]> reversedRowList = new ArrayList<Object[]>();
		String[] reversedRowNames = new String[rowNames.length];
		for (int i = 0; i < n; i++) {
			reversedRowList.add(rowList.get(n - i - 1));
			reversedRowNames[i] = rowNames[n - i - 1];
		}
		rowList = reversedRowList;
		rowNames = reversedRowNames;
	}

	public DataTable sampleCasesWithoutReplacement(int nSamp) {
		/* uniform random sample of size nSamp */
		int visited = 0;
		int nRows = getNumRows();
		int ns = nSamp;
		boolean[] isSampled = new boolean[nRows];
		for (int i = 0; i < nRows; i++) {
			if (Globals.random.nextDouble() < (double) ns / (nRows - visited)) {
				isSampled[i] = true;
				ns--;
			}
			visited++;
		}
		String[] oldRowNames = getRowNames();
		String[] newRowNames = null;
		if (oldRowNames != null) {
			newRowNames = new String[nSamp];
			ns = 0;
			for (int i = 0; i < nRows; i++) {
				if (isSampled[i]) {
					newRowNames[ns] = oldRowNames[i];
					ns++;
				}
			}
		}
		DataTable newTable = new DataTable(getName(), newRowNames, rawNames, isCategorical, units);
		for (int i = 0; i < nRows; i++) {
			if (isSampled[i]) {
				Object[] row = getRawRow(i);
				newTable.addRow(row);
			}
		}
		copyTableInstanceVariables(newTable);
		return newTable;
	}

	public DataTable sampleCasesWithReplacement(int nSamp) {
		/* bootstrap random sample of size nSamp */
		int n = getNumRows();
		int[] indices = new int[nSamp];
		for (int i = 0; i < nSamp; i++)
			indices[i] = Globals.random.nextInt(n);

		String[] oldRowNames = getRowNames();
		String[] newRowNames = null;
		if (oldRowNames != null) {
			newRowNames = new String[nSamp];
			for (int i = 0; i < nSamp; i++)
				newRowNames[i] = oldRowNames[indices[i]];
		}
		DataTable newTable = new DataTable(getName(), newRowNames, rawNames, isCategorical, units);
		for (int i = 0; i < nSamp; i++) {
			Object[] row = getRawRow(indices[i]);
			newTable.addRow(row);
		}
		copyTableInstanceVariables(newTable);
		return newTable;
	}

	public DataTable copy(int nRows) {
		/* deep copy of this Table */
		if (nRows == 0)
			nRows = getNumRows();
		DataTable newTable = new DataTable(getName(), getRowNames(), getColNames(), isCategorical(), getUnits());
		for (int i = 0; i < getNumRows(); i++) {
			Object[] row = getRawRow(i);
			newTable.addRow(row);
			if (i >= nRows)
				break;
		}
		copyTableInstanceVariables(newTable);
		return newTable;
	}

	public DataTable eliminateNullRows() {
		if (!getMatrixType().equals(RECTANGULAR))
			return this;
		int nRows = getNumRows();
		int nCols = getNumCols();
		DataTable newTable = new DataTable(getName(), getRowNames(), getColNames(), isCategorical, units);
		for (int i = 0; i < nRows; i++) {
			Object[] row = getRawRow(i);
			boolean isNull = true;
			for (int j = 0; j < nCols; j++) {
				if (row[j] instanceof String) {
					if (!Globals.isMissing((String) row[j]))
						isNull = false;
				} else if (row[j] instanceof Double) {
					if (!Globals.isMissing((Double) row[j]))
						isNull = false;
				}
			}
			if (!isNull)
				newTable.addRow(row);
		}
		copyTableInstanceVariables(newTable);
		return newTable;
	}

	public DataTable eliminateNullColumns() {
		if (!getMatrixType().equals(RECTANGULAR) && !getMatrixType().equals(SQUARE))
			return this;
		int nRows = getNumRows();
		int nCols = getNumCols();
		boolean[] isNull = new boolean[nCols];
		Arrays.fill(isNull, true);
		int nonMissingColumns = 0;
		for (int i = 0; i < nRows; i++) {
			Object[] row = getRawRow(i);
			for (int j = 0; j < nCols; j++) {
				if (row[j] instanceof String) {
					if (!Globals.isMissing((String) row[j]))
						isNull[j] = false;
				} else if (row[j] instanceof Double) {
					if (!Globals.isMissing((Double) row[j]))
						isNull[j] = false;
				}
			}
			nonMissingColumns = 0;
			for (int j = 0; j < nCols; j++) {
				if (!isNull[j])
					nonMissingColumns++;
			}
			if (nonMissingColumns == nCols)
				break;
		}
		if (nonMissingColumns == nCols)
			return this;
		String[] newUnits = new String[nonMissingColumns];
		String[] oldColNames = rawNames;
		String[] newColNames = new String[nonMissingColumns];
		boolean[] newCategorical = new boolean[nonMissingColumns];
		int m = 0;
		for (int j = 0; j < nCols; j++) {
			if (!isNull[j]) {
				newColNames[m] = oldColNames[j];
				newCategorical[m] = isCategorical[j];
				newUnits[m] = units[j];
				m++;
			} else {
				if (j < weightVar)
					weightVar--;
			}
		}
		DataTable newTable = new DataTable(getName(), getRowNames(), newColNames, newCategorical, newUnits);
		for (int i = 0; i < nRows; i++) {
			Object[] row = getRawRow(i);
			Object[] newRow = new Object[nonMissingColumns];
			m = 0;
			for (int j = 0; j < nCols; j++) {
				if (!isNull[j]) {
					newRow[m] = row[j];
					m++;
				}
			}
			newTable.addRow(newRow);
		}
		copyTableInstanceVariables(newTable);
		return newTable;
	}

	public DataTable addColumnToTable(DataTable data, int col) {
		int nCols = getNumCols();
		String[] newUnits = new String[nCols + 1];
		System.arraycopy(units, 0, newUnits, 0, nCols);
		newUnits[nCols] = data.getUnits()[col];
		String[] newColNames = new String[nCols + 1];
		System.arraycopy(colNames, 0, newColNames, 0, nCols);
		newColNames[nCols] = data.getColNames()[col];
		boolean[] newCategorical = new boolean[nCols + 1];
		System.arraycopy(isCategorical, 0, newCategorical, 0, isCategorical.length);
		newCategorical[nCols] = data.isCategoricalVariable(col);
		DataTable newTable = new DataTable(getName(), getRowNames(), newColNames, newCategorical, newUnits);
		copyTableInstanceVariables(newTable);

		String[] newTransforms = new String[transforms.length + 1];
		System.arraycopy(transforms, 0, newTransforms, 0, transforms.length);
		newTable.transforms = newTransforms;
		newTable.categories = new HashMap<String, HashMap<String, Integer>>(categories);
		if (data.isCategorical[col]) {
			HashMap<String, Integer> map = data.categories.get(data.getColNames()[col]);
			newTable.categories.put(data.getColNames()[col], map);
		}
		for (int i = 0; i < data.getNumRows(); i++) {
			Object[] oldRow = getRawRow(i);
			Object[] newRow = new Object[oldRow.length + 1];
			System.arraycopy(oldRow, 0, newRow, 0, oldRow.length);
			newRow[newRow.length - 1] = data.getRawRow(i)[col];
			newTable.addRow(newRow);
		}
		return newTable;
	}

	public DataTable addIndexColumnToTable() {
		int nCols = getNumCols();
		String[] newUnits = new String[nCols + 1];
		System.arraycopy(units, 0, newUnits, 0, nCols);
		newUnits[nCols] = Units.NUMBER;
		String[] newColNames = new String[nCols + 1];
		System.arraycopy(colNames, 0, newColNames, 0, nCols);
		newColNames[nCols] = "Index";
		boolean[] newCategorical = new boolean[nCols + 1];
		System.arraycopy(isCategorical, 0, newCategorical, 0, isCategorical.length);
		newCategorical[nCols] = false;
		DataTable newTable = new DataTable(getName(), getRowNames(), newColNames, newCategorical, newUnits);
		copyTableInstanceVariables(newTable);

		String[] newTransforms = new String[transforms.length + 1];
		System.arraycopy(transforms, 0, newTransforms, 0, transforms.length);
		newTable.transforms = newTransforms;
		newTable.categories = new HashMap<String, HashMap<String, Integer>>(categories);
		for (int i = 0; i < getNumRows(); i++) {
			Object[] oldRow = getRawRow(i);
			Object[] newRow = new Object[oldRow.length + 1];
			System.arraycopy(oldRow, 0, newRow, 0, oldRow.length);
			newRow[newRow.length - 1] = new Double(i);
			newTable.addRow(newRow);
		}
		return newTable;
	}

	public DataTable concatenate(DataTable table2) {
		if (table2 == null)
			return this;
		int nRows1 = getNumRows();
		int nRows2 = table2.getNumRows();
		int minRows = Math.min(nRows1, nRows2);
		int nCols1 = getNumCols();
		int nCols2 = table2.getNumCols();
		int newCols = nCols1 + nCols2;
		String[] newUnits = new String[newCols];
		System.arraycopy(units, 0, newUnits, 0, nCols1);
		System.arraycopy(table2.units, 0, newUnits, nCols1, nCols2);
		String[] newColNames = new String[newCols];
		System.arraycopy(colNames, 0, newColNames, 0, nCols1);
		System.arraycopy(table2.colNames, 0, newColNames, nCols1, nCols2);
		boolean[] newCategorical = new boolean[newCols];
		System.arraycopy(isCategorical, 0, newCategorical, 0, isCategorical.length);
		System.arraycopy(table2.isCategorical, 0, newCategorical, nCols1, nCols2);
		DataTable newTable = new DataTable(getName(), getRowNames(), newColNames, newCategorical, newUnits);
		copyTableInstanceVariables(newTable);

		String[] newTransforms = new String[newCols];
		System.arraycopy(transforms, 0, newTransforms, 0, nCols1);
		newTable.transforms = newTransforms;
		System.arraycopy(table2.transforms, 0, newTransforms, nCols1, nCols2);
		newTable.categories = new HashMap<String, HashMap<String, Integer>>(categories);
		for (int j = 0; j < nCols2; j++) {
			if (table2.isCategorical[j]) {
				HashMap<String, Integer> map = table2.categories.get(table2.getColNames()[j]);
				newTable.categories.put(table2.getColNames()[j], map);
			}
		}
		for (int i = 0; i < minRows; i++) {
			Object[] oldRow = getRawRow(i);
			Object[] newRow = new Object[newCols];
			System.arraycopy(oldRow, 0, newRow, 0, nCols1);
			for (int j = 0; j < nCols2; j++)
				newRow[nCols1 + j] = table2.getRawRow(i)[j];
			newTable.addRow(newRow);
		}
		return newTable;
	}

	public String[] getCategoricalVariableNames() {
		String[] catVarNames = new String[categoricalVariableNames.size()];
		Object[] cn = categoricalVariableNames.toArray();
		for (int j = 0; j < cn.length; j++)
			catVarNames[j] = (String) cn[j];
		return catVarNames;
	}

	public String[] getContinuousVariableNames() {
		String[] conVarNames = new String[continuousVariableNames.size()];
		Object[] cn = continuousVariableNames.toArray();
		for (int j = 0; j < cn.length; j++)
			conVarNames[j] = (String) cn[j];
		return conVarNames;
	}
}