/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

/**
 * This is the parser base class.
 *
 * @author
 * Design: Leland Wilkinson<BR>
 * Code:   Leland Wilkinson
 * 
 * @version 1.0
 */

public abstract class Parser {

	protected Syntax syntax;
	protected Scanner scanner;
	protected int depth;
	protected int[] target;
	protected int largestArrayArgument;

	protected Parser() {
	}

	/**
	 * Parses an array of statements.
	 * @param statements An array of legal statements.
	 * @return a parse tree.
	 */
	public abstract ParseTree parse(String[] statements) throws ParserException;

	/**
	 * Factory method produces a Parser instance.
	 * @param syntax A SyntaxTableau instance.
	 * @return an instance of Parser.
	 */
	public static Parser getInstance(Syntax syntax) {
		return new ParserTableau(syntax);
	}
}
