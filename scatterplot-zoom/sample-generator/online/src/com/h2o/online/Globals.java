/**
 * Copyright (c) 2016 H2O.ai
 */
package com.h2o.online;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public abstract class Globals {
	public static final double EPSILON = 1.0e-15;
	public static final double ONE_PLUS_EPSILON = 1.0 + 1.0e-15;
	public static final double ONE_MINUS_EPSILON = 1.0 - 1.0e-15;
	public static final double FUZZ = 1.0e-7;
	public static final double ONE_PLUS_FUZZ = 1.0 + 1.0e-7;
	public static final double ONE_MINUS_FUZZ = 1.0 - 1.0e-7;
	public static final double OVERFLOW = 1.0e75;
	public static final double EXPONENTIAL_OVERFLOW = 69;
	public static final double BIG = 1.0e12;
	public static final double MAXLOG = 7.09782712893383996732E2;
	public static final double MINLOG = -7.451332191019412076235E2;
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final int NREPS = 7;
	public static final int MAXNODES = 10000;
	public static final int MAXEDGES = 20000;
	public static final int MAXVENN = 12;
	public static final int MAXLETTERS = 30;
	public static int MAXSPLOM = 20;
	public static int MAXDIGITS = 9;
	public static int MAXDOTS = 50;
	public static int MAXFACTOR = 10; // this is cosmetic ;-)
	public static int MAXSUBSETS = 12;
	public static int MAXDIMENSIONS = 50;
	public static int MAXROWS = 2000;
	public static int TOO_BIG = 1000;
	public static int TOO_SMALL = 2;
	public static int NODE = 0;
	public static int ERROR = 1, MEMORY_ERROR = 2, CPUTIME_ERROR = 3, SINGULAR_ERROR = 4, CONVERGENCE_ERROR = 5;
	public static Random random = new Random(13579);
	public static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

	public static final double MISSING_VALUE = Double.NaN;
	public static final String MISSING_STRING = "";

	/* used only for CSV file import (to handle old SPSS and survey files) */
	private static final Double[] numericMissingValueCodes = new Double[] { 91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0, 991.0,
			992.0, 993.0, 994.0, 995.0, 996.0, 997.0, 998.0, 999.0, 9991.0, 9992.0, 9993.0, 9994.0, 9995.0, 9996.0, 9997.0, 9998.0, 9999.0,
			99991.0, 99992.0, 99993.0, 99994.0, 99995.0, 99996.0, 99997.0, 99998.0, 99999.0, 999991.0, 999992.0, 999993.0, 999994.0,
			999995.0, 999996.0, 999997.0, 999998.0, 999999.0 };
	public static final Set<Double> numericMissingValues = new TreeSet<Double>(Arrays.asList(numericMissingValueCodes));

	/* JSON tags */
	public static final String JSON_GRAPHICS = "graphics", JSON_ANALYTICS = "analytics", JSON_TRANSFORMATIONS = "transformationsObject",
			JSON_DATA = "dataObject", JSON_DATE = "date", JSON_COLUMNS = "inputColumnIndicesList", JSON_DATATABLE = "dataTable2DList",
			JSON_PARAMETERS = "parameters", JSON_SUBTYPE = "subtype", JSON_ANOMALIES = "anomalies", JSON_ANOMALIES_LIST = "anomaliesList",
			JSON_PREDICTIONS = "predictions", JSON_STARTROW = "startRow", JSON_ENDROW = "endRow", JSON_STARTCOLUMN = "startColumn",
			JSON_ENDCOLUMN = "endColumn", JSON_VARNAME = "variableName", JSON_VARNAMES = "variableNamesList", JSON_HISTOBARS = "histobars",
			JSON_HISTOBARS_LIST = "histobarList", JSON_BINS = "binList", JSON_SCATTERPLOTS = "scatterplots",
			JSON_SCATTERPLOTS_LIST = "scatterplotList", JSON_DOTPLOTS = "dotplots", JSON_DOTPLOTS_LIST = "dotplotList",
			JSON_PARALLELCOORDINATES = "parallelcoordinates", JSON_PARALLELCOORDINATES_LIST = "parallelcoordinatesList",
			JSON_SCRIPT = "script", JSON_MAXDOTS = "maximum_number_of_dots_in_stack", JSON_VARIABLES_LIST = "variablesList",
			JSON_DEPENDENT_VARIABLES_LIST = "dependentVariablesList", JSON_INDEPENDENT_VARIABLES_LIST = "independentVariablesList",
			JSON_NUMBARS = "numberOfBars", JSON_PROFILES_LIST = "profilesList", JSON_COLORVAR = "colorVariable",
			JSON_TUPLES_LIST = "tuplesList", JSON_OUTLIERS_LIST = "outliersList", JSON_DISTRIBUTIONS = "distributionsList",
			JSON_COLINDICES_LIST = "columnIndicesList", JSON_ROWINDICES_LIST = "rowIndicesList", JSON_SKEWED = "skewed",
			JSON_SPIKED = "spiked", JSON_MULTIMODAL = "multimodal", JSON_XCOORDINATES_LIST = "xCoordinatesList",
			JSON_YCOORDINATES_LIST = "yCoordinatesList", JSON_ZCOORDINATES_LIST = "zCoordinatesList",
			JSON_WEIGHTS_LIST = "caseWeightsList", JSON_COUNTS_LIST = "countsList", JSON_STACKS_LIST = "stacks2DList",
			JSON_UNIVARIATE_OUTLIERS_LIST = "univariateOutliersList", JSON_MULTIVARIATE_OUTLIERS_LIST = "multivariateOutliersList",
			JSON_IDIOSYNCRATIC_OUTLIERS_LIST = "idiosyncraticOutliersList", JSON_TIME_SERIES_OUTLIERS_LIST = "univariateOutliersList",
			JSON_DUPLICATE_ROWS_LIST = "duplicateRowsList";

	public static final boolean isMissing(double x) {
		return Double.isNaN(x);
	}

	public static final boolean isMissing(String s) {
		if (s == null)
			return true;
		else
			return s.trim().length() == 0;
	}
}
