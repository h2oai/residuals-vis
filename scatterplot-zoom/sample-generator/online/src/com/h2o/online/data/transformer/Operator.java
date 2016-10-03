/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.lang.reflect.Constructor;
import java.util.Date;

/**
 * This is base class for unary, binary, and n-ary operators.
 *
 * @author
 * Design:  Leland Wilkinson<BR>
 * Code:   Leland Wilkinson
 *
 *@version 1.0
 */
public abstract class Operator extends ResultNode {
	protected String name;
	protected static String defaultMessage = "illegal data type: ";

	protected Operator() {
	}

	protected abstract Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException;

	static Operator buildUnaryOperatorNode(String key, ParseTreeNode[] args, Syntax syntax, int[] target) {
		try {
			Class cl = (Class) syntax.unaryOperators.get(key);
			Constructor co = cl.getConstructor(new Class[] { ParseTreeNode[].class });
			Operator o = (Operator) co.newInstance(new Object[] { args });
			o.target = target;
			return o;
		} catch (Exception exc) {
			throw new IllegalStateException(exc.toString());
		}
	}

	static Operator buildBinaryOperatorNode(String key, ParseTreeNode[] args, Syntax syntax, int[] target) {
		try {
			Class cl = (Class) syntax.binaryOperators.get(key);
			Constructor co = cl.getConstructor(new Class[] { ParseTreeNode[].class });
			Operator o = (Operator) co.newInstance(new Object[] { args });
			o.target = target;
			return o;
		} catch (Exception exc) {
			throw new IllegalStateException(exc.toString());
		}
	}
}

/*------------------------------------------------------------------

Unary operators

-------------------------------------------------------------------*/

class UnaryPositive extends Operator {

	public static int precedence;

	public UnaryPositive(ParseTreeNode[] children) {
		this.children = children;
	}

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
				result[i] = new Double(v);
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(v);
		}
	}
}

class UnaryNegative extends Operator {

	public static int precedence;

	public UnaryNegative(ParseTreeNode[] children) {
		this.children = children;
	}

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
				result[i] = new Double(0. - v);
			}
			return result;
		} else {
			try {
				v = ((Number) parms[0]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(0. - v);
		}
	}
}

/*------------------------------------------------------------------

Binary operators

-------------------------------------------------------------------*/

class Add extends Operator {

	public static int precedence = 10;

	public Add(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) ((Object[]) parms[1])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(v0 + v1);
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(v0 + v1);
		}
	}
}

class Subtract extends Operator {

	public static int precedence = 10;

	public Subtract(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) ((Object[]) parms[1])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(v0 - v1);
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(v0 - v1);
		}
	}
}

class Multiply extends Operator {

	public static int precedence = 11;

	public Multiply(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) ((Object[]) parms[1])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(v0 * v1);
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(v0 * v1);
		}
	}
}

class Divide extends Operator {

	public static int precedence = 11;

	public Divide(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) ((Object[]) parms[1])[i]).doubleValue();
				} catch (ClassCastException cc) {
					throw new ExecutorException(defaultMessage + parms[0].toString());
				}
				result[i] = new Double(v0 / v1);
			}
			return result;
		} else {
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Double(v0 / v1);
		}
	}
}

class Remainder extends Operator {

	public static int precedence = 11;

	public Remainder(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) ((Object[]) parms[1])[i]).doubleValue();
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

class Exponent extends Operator {

	public static int precedence = 12;

	public Exponent(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		double v0, v1;
		if (parms[0] instanceof Object[]) {
			int np = ((Object[]) parms[0]).length;
			Object[] result = new Object[np];
			for (int i = 0; i < np; i++) {
				try {
					v0 = ((Number) ((Object[]) parms[0])[i]).doubleValue();
					v1 = ((Number) ((Object[]) parms[1])[i]).doubleValue();
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

class And extends Operator {

	public static int precedence = 3;

	public And(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (syntax.getBooleanLogic()) {
			boolean v0, v1;
			try {
				v0 = ((Boolean) parms[0]).booleanValue();
				v1 = ((Boolean) parms[1]).booleanValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Boolean(v0 && v1);
		} else {
			double v0, v1;
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (v0 * v1 == 0.)
				return new Double(0.);
			else
				return new Double(1.);
		}
	}
}

class Or extends Operator {

	public static int precedence = 2;

	public Or(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		if (syntax.getBooleanLogic()) {
			boolean v0, v1;
			try {
				v0 = ((Boolean) parms[0]).booleanValue();
				v1 = ((Boolean) parms[1]).booleanValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			return new Boolean(v0 || v1);
		} else {
			double v0, v1;
			try {
				v0 = ((Number) parms[0]).doubleValue();
				v1 = ((Number) parms[1]).doubleValue();
			} catch (ClassCastException cc) {
				throw new ExecutorException(defaultMessage + parms[0].toString());
			}
			if (v0 + v1 == 0.)
				return new Double(0.);
			else
				return new Double(1.);
		}
	}
}

class Equal extends Operator {

	public static int precedence = 7;

	public Equal(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object o0 = parms[0];
		Object o1 = parms[1];
		if ((o0 instanceof Number) && (o1 instanceof Number)) {
			double v0 = ((Number) o0).doubleValue();
			double v1 = ((Number) o1).doubleValue();
			if (syntax.getBooleanLogic())
				return new Boolean(v0 == v1);
			else {
				if (v0 == v1)
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof String) && (o1 instanceof String)) {
			String v0 = (String) o0;
			String v1 = (String) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(v0.equals(v1));
			else {
				if (v0.equals(v1))
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Date) && (o1 instanceof Date)) {
			Date v0 = (Date) o0;
			Date v1 = (Date) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(v0.equals(v1));
			else {
				if (v0.equals(v1))
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Boolean) && (o1 instanceof Boolean)) {
			boolean b0 = ((Boolean) o0).booleanValue();
			boolean b1 = ((Boolean) o1).booleanValue();
			if (syntax.getBooleanLogic())
				return new Boolean(b0 == b1);
			else {
				if (b0 == b1)
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else
			throw new ExecutorException(defaultMessage + parms[0].toString());
	}
}

class NotEqual extends Operator {

	public static int precedence = 7;

	public NotEqual(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object o0 = parms[0];
		Object o1 = parms[1];
		if ((o0 instanceof Number) && (o1 instanceof Number)) {
			double v0 = ((Number) o0).doubleValue();
			double v1 = ((Number) o1).doubleValue();
			if (syntax.getBooleanLogic())
				return new Boolean(v0 != v1);
			else {
				if (v0 != v1)
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof String) && (o1 instanceof String)) {
			String v0 = (String) o0;
			String v1 = (String) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(!v0.equals(v1));
			else {
				if (!v0.equals(v1))
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Date) && (o1 instanceof Date)) {
			Date v0 = (Date) o0;
			Date v1 = (Date) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(!v0.equals(v1));
			else {
				if (!v0.equals(v1))
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Boolean) && (o1 instanceof Boolean)) {
			boolean b0 = ((Boolean) o0).booleanValue();
			boolean b1 = ((Boolean) o1).booleanValue();
			if (syntax.getBooleanLogic())
				return new Boolean(b0 != b1);
			else {
				if (b0 != b1)
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else
			throw new ExecutorException(defaultMessage + parms[0].toString());
	}
}

class LessThan extends Operator {

	public static int precedence = 8;

	public LessThan(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object o0 = parms[0];
		Object o1 = parms[1];
		if ((o0 instanceof Number) && (o1 instanceof Number)) {
			double v0 = ((Number) o0).doubleValue();
			double v1 = ((Number) o1).doubleValue();
			if (syntax.getBooleanLogic())
				return new Boolean(v0 < v1);
			else {
				if (v0 < v1)
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Date) && (o1 instanceof Date)) {
			Date v0 = (Date) o0;
			Date v1 = (Date) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(v0.before(v1));
			else {
				if (v0.before(v1))
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof String) && (o1 instanceof String)) {
			String v0 = (String) o0;
			String v1 = (String) o1;
			if (syntax.getBooleanLogic()) {
				if (collator.compare(v0, v1) < 0)
					return new Boolean(true);
				else
					return new Boolean(false);
			} else {
				if (collator.compare(v0, v1) < 0)
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else
			throw new ExecutorException(defaultMessage + parms[0].toString());
	}
}

class GreaterThan extends Operator {

	public static int precedence = 8;

	public GreaterThan(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object o0 = parms[0];
		Object o1 = parms[1];
		if ((o0 instanceof Number) && (o1 instanceof Number)) {
			double v0 = ((Number) o0).doubleValue();
			double v1 = ((Number) o1).doubleValue();
			if (syntax.getBooleanLogic())
				return new Boolean(v0 > v1);
			else {
				if (v0 > v1)
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Date) && (o1 instanceof Date)) {
			Date v0 = (Date) o0;
			Date v1 = (Date) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(v0.after(v1));
			else {
				if (v0.after(v1))
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof String) && (o1 instanceof String)) {
			String v0 = (String) o0;
			String v1 = (String) o1;
			if (syntax.getBooleanLogic()) {
				if (collator.compare(v0, v1) > 0)
					return new Boolean(true);
				else
					return new Boolean(false);
			} else {
				if (collator.compare(v0, v1) > 0)
					return new Double(1.);
				else
					return new Double(0.);
			}
		} else
			throw new ExecutorException(defaultMessage + parms[0].toString());
	}
}

class LessThanOrEqual extends Operator {

	public static int precedence = 8;

	public LessThanOrEqual(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object o0 = parms[0];
		Object o1 = parms[1];
		if ((o0 instanceof Number) && (o1 instanceof Number)) {
			double v0 = ((Number) o0).doubleValue();
			double v1 = ((Number) o1).doubleValue();
			if (syntax.getBooleanLogic())
				return new Boolean(v0 <= v1);
			else {
				if (v0 <= v1)
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Date) && (o1 instanceof Date)) {
			Date v0 = (Date) o0;
			Date v1 = (Date) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(v0.before(v1) || v0.equals(v1));
			else {
				if (v0.before(v1) || v0.equals(v1))
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof String) && (o1 instanceof String)) {
			String v0 = (String) o0;
			String v1 = (String) o1;
			if (syntax.getBooleanLogic()) {
				if (collator.compare(v0, v1) > 0)
					return new Boolean(false);
				else
					return new Boolean(true);
			} else {
				if (collator.compare(v0, v1) > 0)
					return new Double(0.);
				else
					return new Double(1.);
			}
		} else
			throw new ExecutorException(defaultMessage + parms[0].toString());
	}
}

class GreaterThanOrEqual extends Operator {

	public static int precedence = 8;

	public GreaterThanOrEqual(ParseTreeNode[] children) {
		this.children = children;
	}

	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object o0 = parms[0];
		Object o1 = parms[1];
		if ((o0 instanceof Number) && (o1 instanceof Number)) {
			double v0 = ((Number) o0).doubleValue();
			double v1 = ((Number) o1).doubleValue();
			if (syntax.getBooleanLogic())
				return new Boolean(v0 >= v1);
			else {
				if (v0 >= v1)
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof Date) && (o1 instanceof Date)) {
			Date v0 = (Date) o0;
			Date v1 = (Date) o1;
			if (syntax.getBooleanLogic())
				return new Boolean(v0.after(v1) || v0.equals(v1));
			else {
				if (v0.after(v1) || v0.equals(v1))
					return new Double(1.0);
				else
					return new Double(0.);
			}
		} else if ((o0 instanceof String) && (o1 instanceof String)) {
			String v0 = (String) o0;
			String v1 = (String) o1;
			if (syntax.getBooleanLogic()) {
				if (collator.compare(v0, v1) < 0)
					return new Boolean(false);
				else
					return new Boolean(true);
			} else {
				if (collator.compare(v0, v1) < 0)
					return new Double(0.);
				else
					return new Double(1.);
			}
		} else
			throw new ExecutorException(defaultMessage + parms[0].toString());
	}
}