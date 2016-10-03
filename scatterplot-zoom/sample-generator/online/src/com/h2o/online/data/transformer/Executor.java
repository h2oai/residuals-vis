/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

/**
 * This is the class for executing parsed statements.
 *
 * @author
 * Design:  Leland Wilkinson<BR>
 * Code:   Leland Wilkinson
 *
 *@version 1.0
 */

public class Executor {

	private ParseTreeNode[] nodes;
	private Object[][] data;
	private Syntax syntax;
	public boolean isDeletedRow;

	/**
	 * @param tree Parse tree (output from <code>ExpressionParser</code> instance).
	 * @param syntax An instance of <code>SyntaxTableau</code>.
	 * @param dataRow instance of <code>DataRow</code>.
	 */
	public Executor(ParseTree tree, Syntax syntax, Object[][] data) {
		this.syntax = syntax;
		this.data = data;
		nodes = tree.nodes;
	}

	public Object[][] getData() {
		return data;
	}

	/**
	 * Run method.
	 * @param row a row of the data matrix.
	 */
	public final void run(int row) throws ExecutorException {
		if (row >= data.length) {
			syntax.isDeleted = true;
			return;
		}
		Object result = null;
		syntax.resetStack();
		int nv = nodes.length;
		for (int i = 0; i < nv; i++) {
			ParseTreeNode ni = nodes[i];
			if (ni == null || !ni.isExecutable)
				continue;
			if (ni instanceof If || ni instanceof Else || ni instanceof ElseIf) {
				Boolean nib = (Boolean) ni.getResult(data, syntax, row);
				for (int j = ni.blockInitialStatement; j < ni.blockTerminalStatement; j++) {
					if (nodes[j] != null)
						nodes[j].isExecutable = nib;
				}
			} else if (ni instanceof While) {
				/* user is responsible for not making infinite loop */
				while ((Boolean) ni.getResult(data, syntax, row)) {
					for (int j = ni.blockInitialStatement; j < ni.blockTerminalStatement; j++) {
						ParseTreeNode nj = nodes[j];
						if (nj != null)
							result = nj.getResult(data, syntax, row);
						for (int k = 0; k < nj.target.length; k++)
							data[row][nj.target[k]] = result;
					}
				}
				i = ni.blockTerminalStatement - 1;
			} else if (ni.isColumnwiseFunction) {
				result = ni.getResult(data, syntax, row);
				if (result != null) {
					if (ni.target != null) {
						for (int k = 0; k < ni.target.length; k++) {
							for (int j = 0; j < data.length; j++) {
								data[j][ni.target[k]] = ((Object[][]) result)[j][k];
							}
						}
					} else {
						data = (Object[][]) result;
					}
				}
				ni.isExecutable = false;
			} else {
				result = ni.getResult(data, syntax, row);
				if (result != null && ni.target != null) {
					for (int k = 0; k < ni.target.length; k++) {
						if (result instanceof Object[] && ((Object[]) result).length == ni.target.length) {
							data[row][ni.target[k]] = ((Object[]) result)[k];
						} else {
							data[row][ni.target[k]] = result;
						}
					}
				}
			}
		}
	}
}
