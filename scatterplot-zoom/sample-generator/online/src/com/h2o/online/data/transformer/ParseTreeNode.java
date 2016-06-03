/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.text.Collator;
import java.util.List;

/**
 * This class contains a node of the parse tree.<br>
 * 
 * @author
 *         Design: Leland Wilkinson<BR>
 *         Code: Leland Wilkinson
 * 
 * @version 1.0
 */

public abstract class ParseTreeNode {

	protected ParseTreeNode[] children;
	protected ParseTreeNode parent;
	protected boolean isColumnwiseFunction;
	protected boolean isIndexFunction;
	protected int[] target;
	protected boolean isExecutable = true;
	protected int blockInitialStatement = 0;
	protected int blockTerminalStatement = 0;
	protected Collator collator = Collator.getInstance(); // needed by Operator

	public static List dropVariables; // Drop function accesses this List

	protected ParseTreeNode() {
	}

	protected abstract Object getResult(Object[][] data, Syntax syntax, int row) throws ExecutorException;

	protected abstract Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException;

	String toText(Syntax syntax) {
		int i;
		String result = "";
		Class cls = this.getClass();
		String name = syntax.getUnaryOperatorName(cls);
		if (name != null && children.length == 1)
			result += name + " " + children[0].toText(syntax);
		else {
			name = syntax.getBinaryOperatorName(cls);
			if (name != null && children.length == 2)
				result += children[0].toText(syntax) + " " + name + " " + children[1].toText(syntax);
			else {
				name = syntax.getFunctionName(cls);
				Function f = syntax.getFunction(name);
				if (name != null) {
					result = name;
					if (f.hasDelimiters)
						result += syntax.getOpenDelimiter(0);
					if (children != null) {
						for (i = 0; i < children.length - 1; i++)
							result += children[i].toText(syntax) + syntax.getFunctionParameterSeparator() + " ";
						if (children.length > 0)
							result += children[children.length - 1].toText(syntax);
					}
					if (f.hasDelimiters)
						result += syntax.getCloseDelimiter(0);
				}
			}
		}
		return result;
	}
}

abstract class ResultNode extends ParseTreeNode {

	private Object result;
	protected boolean isScalarResult = false;
	protected boolean hasDelimiters = true;

	protected ParseTreeNode[] getChildren() {
		return children;
	}

	@Override
	protected Object getResult(Object[][] data, Syntax syntax, int row) throws ExecutorException {
		/* for rowwise functions */
		Object[] parms = null;
		Object childResult = null;
		if (children == null) {
			result = compute(parms, data, syntax, row);
		} else {
			if (children.length > 0)
				childResult = children[0].getResult(data, syntax, row);
			/* Array argument */
			if (childResult instanceof Object[]) {
				parms = new Object[children.length];
				for (int i = 0; i < children.length; i++) {
					Object[] child = (Object[]) children[i].getResult(data, syntax, row);
					parms[i] = child;
				}
				result = compute(parms, data, syntax, row);
				/* All other arguments */
			} else {
				parms = new Object[children.length];
				for (int i = 0; i < parms.length; i++)
					parms[i] = children[i].getResult(data, syntax, row);
				try {
					result = compute(parms, data, syntax, row);
				} catch (Exception e) {
					System.out.println("  encountered when executing this statement: " + this.toText(syntax));
				}
			}
		}

		return result;
	}

	@Override
	protected abstract Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException;
}

class ConstantNode extends ParseTreeNode {

	private Object constant;
	private int variableIndex = -1;
	private int lag = 0;

	ConstantNode(Object constant) {
		this.constant = constant;
	}

	ConstantNode(int variableIndex) {
		this.variableIndex = variableIndex;
	}

	ConstantNode(int variableIndex, int lag) {
		this.variableIndex = variableIndex;
		this.lag = lag;
	}

	@Override
	protected Object getResult(Object[][] data, Syntax syntax, int row) throws ExecutorException {
		Object result;
		if (variableIndex < 0)
			result = constant;
		else if (parent != null && parent.isIndexFunction)
			result = variableIndex;
		else if (lag == 0)
			result = data[row][variableIndex];
		else
			result = getPreviousValue(data, row, lag, variableIndex);
		if (syntax.getMissingValues() != null) {
			if (syntax.isMissing(result))
				result = new Double(Double.NaN);
		}
		return result;
	}

	private Object getPreviousValue(Object[][] data, int row, int lag, int index) throws ExecutorException {
		int ilag = row - lag;
		if (ilag >= 0 && ilag < data.length)
			return data[ilag][index];
		else
			return null;
	}

	@Override
	protected Object compute(Object[] parms, Object[][] data, Syntax syntax, int row) throws ExecutorException {
		return null;
	}
}
