/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.h2o.online.Globals;
import com.h2o.online.analytics.util.Math2;
import com.h2o.online.analytics.util.Sorts;
import com.h2o.online.analytics.util.statistics.Probabilities;
import com.h2o.online.analytics.util.statistics.SimilarityFunctions;
import com.h2o.online.analytics.util.statistics.Statistics;
import com.h2o.online.data.util.TextUtilities;

/**
 * This is base class for functions.
 * 
 * @author
 *         Design: Leland Wilkinson<BR>
 *         Code: Leland Wilkinson
 * 
 * @version 1.0
 */
public abstract class Function extends ResultNode {

	static final String defaultMessage = "illegal data type: ";

	static final int UNIFORM = 1, NORMAL = 2, T = 3, F = 4, CHISQUARE = 5, GAMMA = 6, BETA = 7, EXPONENTIAL = 8,
			LOGISTIC = 9, WEIBULL = 10, BINOMIAL = 11, POISSON = 12;

	protected Function() {
	}

	@Override
	protected abstract Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException;

	static Function buildFunctionNode(String key, ParseTreeNode[] args, Syntax syntax, int[] target) {
		Class cl;
		Constructor co;
		try {
			cl = (Class) syntax.functions.get(key);
			co = cl.getConstructor(new Class[] { ParseTreeNode[].class });
			Function f = (Function) co.newInstance(new Object[] { args });
			f.target = target;
			if (args != null) {
				for (int j = 0; j < args.length; j++)
					args[j].parent = f;
			}
			return f;
		} catch (Exception exc) {
			throw new IllegalStateException(exc.toString());
		}
	}
}

/*----------------------------------------------------------------------------------------------*

 Statement functions

 -----------------------------------------------------------------------------------------------*/

class If extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public If(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		boolean b;
		try {
			b = ((Boolean) parms[0]).booleanValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		syntax.ifStack.push(b);
		if (b)
			return new Boolean(true);
		else
			return new Boolean(false);
	}
}

class Else extends Function {

	public static int minParms = 0;
	public static int maxParms = 0;

	public Else(ParseTreeNode[] children) {
		this.children = children;
		hasDelimiters = false;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (syntax.ifStack.empty())
			throw new ExecutorException(defaultMessage + parms[0].toString());
		boolean b = syntax.ifStack.pop();
		if (b)
			return new Boolean(false);
		else
			return new Boolean(true);
	}
}

class ElseIf extends Function {
	public static int minParms = 1;
	public static int maxParms = 1;

	public ElseIf(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (syntax.ifStack.empty())
			throw new ExecutorException(defaultMessage + parms[0].toString());
		boolean b = syntax.ifStack.pop();
		boolean bb;
		try {
			bb = ((Boolean) parms[0]).booleanValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		syntax.ifStack.push(b || bb);
		if (!b && bb)
			return new Boolean(true);
		else
			return new Boolean(false);
	}
}

class While extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public While(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (syntax.getBooleanLogic()) {
			boolean b;
			try {
				b = ((Boolean) parms[0]).booleanValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			syntax.ifStack.push(b);
			if (b)
				return new Boolean(true);
			else
				return new Boolean(false);
		} else {
			double v;
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (v != 0.)
				return parms[1];
			else
				return parms[2];
		}
	}
}

class Drop extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Drop(ParseTreeNode[] children) {
		this.children = children;
		this.isIndexFunction = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (dropVariables == null)
			dropVariables = new ArrayList();
		for (int j = 0; j < parms.length; j++)
			dropVariables.add(parms[j]);
		return null;
	}
}

class Sample extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Sample(ParseTreeNode[] children) {
		this.children = children;
		this.isColumnwiseFunction = true;
		this.isIndexFunction = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		int n = data.length;
		int p = data[0].length;
		int m = ((Long) parms[0]).intValue();
		if (m > n)
			m = n;
		int visited = 0;
		int k = 0;
		Object[][] result = new Object[m][p];
		for (int i = 0; i < n; i++) {
			if (Globals.random.nextDouble() < (double) m / (n - visited)) {
				System.arraycopy(data[i], 0, result[k], 0, p);
				m--;
				k++;
			}
			visited++;
		}
		return result;
	}
}

/*----------------------------------------------------------------------------------------------*

 Zero-parameter functions

 -----------------------------------------------------------------------------------------------*/

class Record extends Function {

	public static int minParms = 0;
	public static int maxParms = 0;

	public Record(ParseTreeNode[] children) {
		this.children = children;
		hasDelimiters = false;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		return new Double(row);
	}
}

class Delete extends Function {

	public static int minParms = 0;
	public static int maxParms = 0;

	public Delete(ParseTreeNode[] children) {
		this.children = children;
		hasDelimiters = false;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		syntax.isDeleted = true;
		return null;
	}
}

/*----------------------------------------------------------------------------------------------*

 Columnwise functions

 -----------------------------------------------------------------------------------------------*/

class Standardize extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Standardize(ParseTreeNode[] children) {
		this.children = children;
		this.isColumnwiseFunction = true;
		this.isIndexFunction = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		int n = data.length;
		int p = parms.length;
		double[][] x = new double[n][p];
		for (int m = 0; m < p; m++) {
			int j = (Integer) parms[m];
			for (int i = 0; i < n; i++) {
				x[i][m] = (Double) data[i][j];
			}
		}

		Statistics.standardize(x, null);

		Object[][] result = new Object[n][p];
		for (int m = 0; m < p; m++) {
			for (int i = 0; i < n; i++) {
				result[i][m] = x[i][m];
			}
		}
		return result;
	}
}

class Unitize extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Unitize(ParseTreeNode[] children) {
		this.children = children;
		this.isColumnwiseFunction = true;
		this.isIndexFunction = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		int n = data.length;
		int p = parms.length;
		double[][] x = new double[n][p];
		for (int m = 0; m < p; m++) {
			int j = (Integer) parms[m];
			for (int i = 0; i < n; i++) {
				x[i][m] = (Double) data[i][j];
			}
		}

		Statistics.unitize(x, null);

		Object[][] result = new Object[n][p];
		for (int m = 0; m < p; m++) {
			for (int i = 0; i < n; i++) {
				result[i][m] = x[i][m];
			}
		}
		return result;
	}
}

class Rank extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Rank(ParseTreeNode[] children) {
		this.children = children;
		this.isColumnwiseFunction = true;
		this.isIndexFunction = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		int n = data.length;
		int p = parms.length;
		Object[][] result = new Object[n][p];
		for (int m = 0; m < p; m++) {
			double[] x = new double[n];
			int j = (Integer) parms[m];
			for (int i = 0; i < n; i++)
				x[i] = (Double) data[i][j];
			double[] y = Sorts.rank(x);
			for (int i = 0; i < n; i++)
				result[i][m] = y[i];
		}
		return result;
	}
}

/*----------------------------------------------------------------------------------------------*

 Single-parameter functions

 -----------------------------------------------------------------------------------------------*/

class AbsoluteValue extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public AbsoluteValue(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.abs(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.abs(v));
		}
	}
}

class Sign extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Sign(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.signum(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.signum(v));
		}
	}
}

class SquareRoot extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public SquareRoot(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.sqrt(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.sqrt(v));
		}
	}
}

class ExponentialFunction extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public ExponentialFunction(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.exp(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.exp(v));
		}
	}
}

class Log extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Log(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.log(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.log(v));
		}
	}
}

class Log2 extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Log2(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.log(v) / Math.log(2.));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.log(v) / Math.log(2.));
		}
	}
}

class Log10 extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Log10(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.log(v) / Math.log(10.));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.log(v) / Math.log(10.));
		}
	}
}

class Floor extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Floor(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.floor(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.floor(v));
		}
	}
}

class Ceiling extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Ceiling(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.ceil(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.ceil(v));
		}
	}
}

class IntegerPart extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public IntegerPart(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				if (v > 0)
					result[i] = new Double(Math.floor(v));
				else
					result[i] = new Double(Math.ceil(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (v > 0.)
				return new Double(Math.floor(v));
			else
				return new Double(Math.ceil(v));
		}
	}
}

class Round extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Round(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.round(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.round(v));
		}
	}
}

class Sine extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Sine(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.sin(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.sin(v));
		}
	}
}

class Cosine extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Cosine(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.cos(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.cos(v));
		}
	}
}

class Tangent extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Tangent(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.tan(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.tan(v));
		}
	}
}

class ArcSine extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public ArcSine(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.asin(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.asin(v));
		}
	}
}

class ArcCosine extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public ArcCosine(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.acos(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.acos(v));
		}
	}
}

class ArcTangent extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public ArcTangent(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.atan(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.atan(v));
		}
	}
}

class HyperbolicSine extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public HyperbolicSine(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double((Math.exp(v) - Math.exp(-v)) / 2.);
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double((Math.exp(v) - Math.exp(-v)) / 2.);
		}
	}
}

class HyperbolicCosine extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public HyperbolicCosine(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double((Math.exp(v) + Math.exp(-v)) / 2.);
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double((Math.exp(v) + Math.exp(-v)) / 2.);
		}
	}
}

class HyperbolicTangent extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public HyperbolicTangent(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double((Math.exp(v) - Math.exp(-v)) / (Math.exp(v) + Math.exp(-v)));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double((Math.exp(v) - Math.exp(-v)) / (Math.exp(v) + Math.exp(-v)));
		}
	}
}

class HyperbolicArcTangent extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public HyperbolicArcTangent(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(.5 * Math.log((1. + v) / (1. - v)));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(.5 * Math.log((1. + v) / (1. - v)));
		}
	}
}

class Gamma extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Gamma(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Probabilities.gammaFunction(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Probabilities.gammaFunction(v));
		}
	}
}

class LogGamma extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public LogGamma(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v = ((Number) ((Object[]) parms[0])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math2.logGamma(v));
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math2.logGamma(v));
		}
	}
}

/*----------------------------------------------------------------------------------------------*

 Multi-parameter math functions

 -----------------------------------------------------------------------------------------------*/

class Power extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public Power(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) parms[1]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.pow(v0, v1));
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.pow(v0, v1));
		}
	}
}

class Modulo extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public Modulo(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) parms[1]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(v0 % v1);
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(v0 % v1);
		}
	}
}

class Atan2 extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public Atan2(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) parms[1]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(Math.atan2(v0, v1));
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(Math.atan2(v0, v1));
		}
	}
}

/*----------------------------------------------------------------------------------------------*

 Variable length multi-parameter functions

 -----------------------------------------------------------------------------------------------*/

class Array extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Array(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		return parms;
	}
}

class Lag extends Function {

	public static int minParms = 1;
	public static int maxParms = 2;

	public Lag(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		return null;
	}
}

class Code extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;
	public Map recodes; // this Map contains Doubles or Strings

	public Code(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		int nVars = target.length;
		Object[] result = new Object[nVars];
		for (int i = 0; i < nVars; i++) {
			Object key = data[row][target[i]];
			result[i] = recodes.get(key);
		}
		return result;
	}
}

class Cut extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;
	public double[] cutpoints;

	public Cut(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v;
		int nVars = target.length;
		Object[] result = new Object[nVars];
		for (int i = 0; i < nVars; i++) {
			v = (Double) data[row][target[i]];
			for (int j = 0; j < cutpoints.length; j++) {
				if (v < cutpoints[j]) {
					v = j + 1;
					break;
				}
			}
			result[i] = v;
		}
		return result;
	}
}

class Sum extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Sum(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double sum = 0.;
		double v;
		boolean isOK = false;
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v)) {
				isOK = true;
				sum += v;
			}
		}
		if (isOK)
			return new Double(sum);
		else
			return new Double(Double.NaN);
	}
}

class Product extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Product(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double product = 1.;
		double v;
		boolean isOK = false;
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v)) {
				isOK = true;
				product *= v;
			}
		}
		if (isOK)
			return new Double(product);
		else
			return new Double(Double.NaN);
	}
}

class Mean extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Mean(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double sum = 0.;
		double count = 0.;
		double v;
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v)) {
				sum += v;
				count++;
			}
		}
		return new Double(sum / count);
	}
}

class Median extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Median(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		Double missing = new Double(Double.NaN);
		int[] index = Sorts.indexedArraySort(arg);
		int n = arg.length;
		int m = 0;
		for (int i = 0; i < arg.length; i++) {
			if (arg[index[i]].equals(missing))
				m++;
			else
				break;
		}

		if (n == m)
			return null;

		n = n - m;
		int j = m + n / 2;
		int k = m + m + n - j - 1;

		if (arg[0] instanceof Number)
			return new Double((((Number) arg[index[j]]).doubleValue() + ((Number) arg[index[k]]).doubleValue()) / 2.);
		else
			return new Double(Double.NaN);
	}
}

class StandardDeviation extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public StandardDeviation(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double sd = 0., count = 0., xd = 0., xi = 0., mean = 0.;
		for (int i = 0; i < arg.length; i++) {
			try {
				xi = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(xi)) {
				count++;
				xd = (xi - mean);
				mean += xd / count;
				sd += (xi - mean) * xd;
			}
		}
		if (count > 1.)
			sd = Math.sqrt(sd / (count - 1.));
		else
			sd = Double.NaN;

		return new Double(sd);
	}
}

class StandardError extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public StandardError(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double sd = 0., count = 0., xd = 0., xi = 0., mean = 0.;
		for (int i = 0; i < arg.length; i++) {
			try {
				xi = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(xi)) {
				count++;
				xd = (xi - mean);
				mean += xd / count;
				sd += (xi - mean) * xd;
			}
		}
		if (count > 1.)
			sd = Math.sqrt(sd / (count - 1.));
		else
			sd = Double.NaN;

		return new Double(sd / Math.sqrt(count));
	}
}

class CoefficientOfVariation extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public CoefficientOfVariation(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double sd = 0., count = 0., xd = 0., xi = 0., mean = 0.;
		for (int i = 0; i < arg.length; i++) {
			try {
				xi = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(xi)) {
				count++;
				xd = (xi - mean);
				mean += xd / count;
				sd += (xi - mean) * xd;
			}
		}
		if (count > 1.)
			sd = Math.sqrt(sd / (count - 1.));
		else
			sd = Double.NaN;

		return new Double(sd / mean);
	}
}

class Variance extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Variance(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double sd = 0., count = 0., xd = 0., xi = 0., mean = 0., variance = 0.;
		for (int i = 0; i < arg.length; i++) {
			try {
				xi = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(xi)) {
				count++;
				xd = (xi - mean);
				mean += xd / count;
				sd += (xi - mean) * xd;
			}
		}
		if (count > 1.)
			variance = sd / (count - 1.);
		else
			variance = Double.NaN;

		return new Double(variance);
	}
}

class Missing extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Missing(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double count = 0.;
		double v;
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (Double.isNaN(v))
				count++;
		}
		return new Double(count);
	}
}

class NotMissing extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public NotMissing(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double count = 0.;
		double v;
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v))
				count++;
		}
		return new Double(count);
	}
}

class Minimum extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Minimum(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double min = Double.POSITIVE_INFINITY;
		double v;
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v)) {
				if (v < min)
					min = v;
			}
		}
		if (min != Double.POSITIVE_INFINITY)
			return new Double(min);
		else
			return new Double(Double.NaN);
	}
}

class Maximum extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Maximum(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double max = Double.NEGATIVE_INFINITY;
		double v;
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v)) {
				if (v > max)
					max = v;
			}
		}
		if (max != Double.NEGATIVE_INFINITY)
			return new Double(max);
		else
			return new Double(Double.NaN);
	}
}

class Range extends Function {

	public static int minParms = 1;
	public static int maxParms = Integer.MAX_VALUE;

	public Range(ParseTreeNode[] children) {
		this.children = children;
		isScalarResult = true;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object[] arg = parms;
		if (parms[0] instanceof Object[])
			arg = ((Object[]) parms[0]);
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		double v;
		for (int i = 0; i < arg.length; i++) {
			try {
				v = ((Number) arg[i]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (!Double.isNaN(v)) {
				if (v < min)
					min = v;
				if (v > max)
					max = v;
			}
		}
		if (min != Double.NEGATIVE_INFINITY && max != Double.NEGATIVE_INFINITY)
			return new Double(max - min);
		else
			return new Double(Double.NaN);
	}
}

/*----------------------------------------------------------------------------------------------*

 String functions

 -----------------------------------------------------------------------------------------------*/

class StringLength extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public StringLength(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		return new Integer(s.length());
	}
}

class Substring extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public Substring(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		int i1, i2;
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		try {
			i1 = ((Number) parms[1]).intValue();
			i2 = ((Number) parms[2]).intValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		if (i1 < 0)
			i1 = 0;
		if (i2 > s.length())
			i2 = s.length();
		return s.substring(i1, i2);
	}
}

class LowerCase extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public LowerCase(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		return s.toLowerCase();
	}
}

class UpperCase extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public UpperCase(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		return s.toUpperCase();
	}
}

class Trim extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public Trim(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		return s.trim();
	}
}

class LeftTrim extends Function {

	public static int minParms = 1;
	public static int maxParms = 2;

	public LeftTrim(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String c = " ";
		if (parms.length > 1)
			c = ((String) parms[1]).toString();
		char[] source = s.toCharArray();
		char[] deleteChar = c.toCharArray();
		int i = 0;
		while (deleteChar[0] == source[i]) {
			i++;
		}
		if (i < source.length)
			return s.substring(i);
		else
			return "";
	}
}

class RightTrim extends Function {

	public static int minParms = 1;
	public static int maxParms = 2;

	public RightTrim(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String c = " ";
		if (parms.length > 1)
			c = ((String) parms[0]).toString();
		char[] source = s.toCharArray();
		char[] deleteChar = c.toCharArray();
		int i = source.length - 1;
		while (deleteChar[0] == source[i]) {
			i--;
		}
		if (i > 0)
			return s.substring(0, i - 1);
		else
			return "";
	}
}

class IndexOf extends Function {

	public static int minParms = 2;
	public static int maxParms = 3;

	public IndexOf(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null || parms[1] == null)
			return null;
		if (!(parms[0] instanceof String || parms[1] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String t = (String) parms[1];
		if (parms.length == 2)
			return new Integer(s.indexOf(t));
		else {
			int i = ((Number) parms[2]).intValue();
			return new Integer(s.indexOf(t, i));
		}
	}
}

class Replace extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public Replace(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null || parms[1] == null || parms[2] == null)
			return null;
		if (!(parms[0] instanceof String || parms[1] instanceof String || parms[2] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String t = (String) parms[1];
		String u = (String) parms[2];
		int i = 0;
		String result = s;
		if (!t.equals(u)) {
			do {
				i = result.indexOf(t);
				if (i >= 0) {
					String temp1 = result.substring(0, i);
					String temp2 = temp1.concat(u);
					result = temp2.concat(result.substring(i + t.length()));
				}
			} while (i >= 0);
		}
		return result;
	}
}

class Concatenate extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public Concatenate(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null || parms[1] == null)
			return null;
		if (!(parms[0] instanceof String || parms[1] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String t = (String) parms[1];
		return s.concat(t);
	}
}

class NumberToString extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public NumberToString(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Number v;
		try {
			v = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return v.toString();
	}
}

class StringToNumber extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public StringToNumber(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		Number result;
		NumberFormat nf = syntax.getNumberFormat();
		ParsePosition pp = new ParsePosition(0);
		try {
			result = nf.parse(s, pp);
		} catch (Exception e) {
			throw new ExecutorException();
		}
		if (pp.getIndex() != s.length())
			throw new ExecutorException();
		return result;
	}
}

class Levenshtein extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;
	SimilarityFunctions sf = SimilarityFunctions.LEVENSHTEIN;

	public Levenshtein(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null || parms[1] == null)
			return null;
		if (!(parms[0] instanceof String || parms[1] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String t = (String) parms[1];
		return sf.compute(s, t);
	}
}

class DamerauLevenshtein extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;
	SimilarityFunctions sf = SimilarityFunctions.DAMERAU;

	public DamerauLevenshtein(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null || parms[1] == null)
			return null;
		if (!(parms[0] instanceof String || parms[1] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		String t = (String) parms[1];
		return sf.compute(s, t);
	}
}

class MetaPhone extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public MetaPhone(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (parms == null || parms[0] == null)
			return null;
		if (!(parms[0] instanceof String))
			throw new ExecutorException(defaultMessage + parms[0].toString());
		String s = (String) parms[0];
		return TextUtilities.metaPhone(s);
	}
}

/*----------------------------------------------------------------------------------------------*

 Time/date functions

 -----------------------------------------------------------------------------------------------*/

class DateFunction extends Function {

	public static int minParms = 0;
	public static int maxParms = 0;

	public DateFunction(ParseTreeNode[] children) {
		this.children = children;
		hasDelimiters = false;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		return new Date();
	}
}

class DateToString extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public DateToString(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Date v;
		try {
			v = (Date) parms[0];
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return v.toString();
	}
}

class StringToDate extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public StringToDate(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Date result;
		DateFormat df = syntax.getDateFormat();
		ParsePosition pp = new ParsePosition(0);
		String s = ((String) parms[0]).toString();
		try {
			result = df.parse(s, pp);
		} catch (Exception e) {
			throw new ExecutorException();
		}
		if (pp.getIndex() != s.length())
			throw new ExecutorException();
		return result;
	}
}

/*----------------------------------------------------------------------------------------------*

 Statistical functions

 -----------------------------------------------------------------------------------------------*/

class UniformCDF extends Function {

	public static int minParms = 1;
	public static int maxParms = 3;

	public UniformCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		v1 = 0.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v2 = 1.;
		if (parms.length > 2) {
			try {
				v2 = ((Number) parms[2]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.cdf(v0, v1, v2, UNIFORM, false));
	}
}

class NormalCDF extends Function {

	public static int minParms = 1;
	public static int maxParms = 3;

	public NormalCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		v1 = 0.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v2 = 1.;
		if (parms.length > 2) {
			try {
				v2 = ((Number) parms[2]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.cdf(v0, v1, v2, NORMAL, false));
	}
}

class TCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public TCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., T, false));
	}
}

class FCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public FCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, F, false));
	}
}

class ChisquareCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public ChisquareCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., CHISQUARE, false));
	}
}

class GammaCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public GammaCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., GAMMA, false));
	}
}

class BetaCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public BetaCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, BETA, false));
	}
}

class ExponentialCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public ExponentialCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, EXPONENTIAL, false));
	}
}

class LogisticCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public LogisticCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, LOGISTIC, false));
	}
}

class WeibullCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public WeibullCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, WEIBULL, false));
	}
}

class BinomialCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public BinomialCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, BINOMIAL, false));
	}
}

class PoissonCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public PoissonCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., POISSON, false));
	}
}

class UniformDensity extends Function {

	public static int minParms = 1;
	public static int maxParms = 3;

	public UniformDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		v1 = 0.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v2 = 1.;
		if (parms.length > 2) {
			try {
				v2 = ((Number) parms[2]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.cdf(v0, v1, v2, UNIFORM, true));
	}
}

class NormalDensity extends Function {

	public static int minParms = 1;
	public static int maxParms = 3;

	public NormalDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		v1 = 0.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v2 = 1.;
		if (parms.length > 2) {
			try {
				v2 = ((Number) parms[2]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.cdf(v0, v1, v2, NORMAL, true));
	}
}

class TDensity extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public TDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., T, true));
	}
}

class FDensity extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public FDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, F, true));
	}
}

class ChisquareDensity extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public ChisquareDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., CHISQUARE, true));
	}
}

class GammaDensity extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public GammaDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., GAMMA, true));
	}
}

class BetaDensity extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public BetaDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, BETA, true));
	}
}

class ExponentialDensity extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public ExponentialDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, EXPONENTIAL, true));
	}
}

class LogisticDensity extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public LogisticDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, LOGISTIC, true));
	}
}

class WeibullDensity extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public WeibullDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, WEIBULL, true));
	}
}

class BinomialDensity extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public BinomialDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, v2, BINOMIAL, true));
	}
}

class PoissonDensity extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public PoissonDensity(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdf(v0, v1, 0., POISSON, true));
	}
}

class UniformInverseCDF extends Function {

	public static int minParms = 1;
	public static int maxParms = 3;

	public UniformInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		v1 = 0.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v2 = 1.;
		if (parms.length > 2) {
			try {
				v2 = ((Number) parms[2]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, UNIFORM));
	}
}

class NormalInverseCDF extends Function {

	public static int minParms = 1;
	public static int maxParms = 3;

	public NormalInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		v1 = 0.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v2 = 1.;
		if (parms.length > 2) {
			try {
				v2 = ((Number) parms[2]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, NORMAL));
	}
}

class TInverseCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public TInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, 0., T));
	}
}

class FInverseCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public FInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, F));
	}
}

class ChisquareInverseCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public ChisquareInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, 0., CHISQUARE));
	}
}

class GammaInverseCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public GammaInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, 0., GAMMA));
	}
}

class BetaInverseCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public BetaInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, BETA));
	}
}

class ExponentialInverseCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public ExponentialInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, EXPONENTIAL));
	}
}

class LogisticInverseCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public LogisticInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, LOGISTIC));
	}
}

class WeibullInverseCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public WeibullInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, WEIBULL));
	}
}

class BinomialInverseCDF extends Function {

	public static int minParms = 3;
	public static int maxParms = 3;

	public BinomialInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1, v2;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
			v2 = ((Number) parms[2]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, v2, BINOMIAL));
	}
}

class PoissonInverseCDF extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public PoissonInverseCDF(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.cdfinv(v0, v1, 0., POISSON));
	}
}

class UniformRandom extends Function {

	public static int minParms = 0;
	public static int maxParms = 2;

	public UniformRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		v0 = 0.;
		if (parms.length > 0) {
			try {
				v0 = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v1 = 1.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.random(v0, v1, UNIFORM));
	}
}

class NormalRandom extends Function {

	public static int minParms = 0;
	public static int maxParms = 2;

	public NormalRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		v0 = 0.;
		if (parms.length > 0) {
			try {
				v0 = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		v1 = 1.;
		if (parms.length > 1) {
			try {
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
		}
		return new Double(Probabilities.random(v0, v1, NORMAL));
	}
}

class TRandom extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public TRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, 0., T));
	}
}

class FRandom extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public FRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, v1, F));
	}
}

class ChisquareRandom extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public ChisquareRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, 0., CHISQUARE));
	}
}

class GammaRandom extends Function {

	public static int minParms = 1;
	public static int maxParms = 1;

	public GammaRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, 0., GAMMA));
	}
}

class BetaRandom extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public BetaRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, v1, BETA));
	}
}

class ExponentialRandom extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public ExponentialRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, v1, EXPONENTIAL));
	}
}

class LogisticRandom extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public LogisticRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, v1, LOGISTIC));
	}
}

class WeibullRandom extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public WeibullRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, v1, WEIBULL));
	}
}

class BinomialRandom extends Function {

	public static int minParms = 2;
	public static int maxParms = 2;

	public BinomialRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		try {
			v0 = ((Number) parms[0]).doubleValue();
			v1 = ((Number) parms[1]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, v1, BINOMIAL));
	}
}

class PoissonRandom extends Function {

	public static int minParms = 1;
	public static int maxParms = 2;

	public PoissonRandom(ParseTreeNode[] children) {
		this.children = children;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0;
		try {
			v0 = ((Number) parms[0]).doubleValue();
		} catch (ClassCastException cc) {
			throw new ExecutorException(defaultMessage + parms[0].toString());
		}
		return new Double(Probabilities.random(v0, 0., POISSON));
	}
}
