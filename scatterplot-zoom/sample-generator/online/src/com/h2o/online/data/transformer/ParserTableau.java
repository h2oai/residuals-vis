/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This is a parser for the Tableau transformation language. Its grammar is:<br>
 * <dl>
 * <dt>statement ::= 
 * <dd>variable_list '=' expression
 * <dt>variable_list ::=
 * <dd>variable_name |
 * <dd>variable_name ',' variable_name ...
 * <dt>expression ::= 
 * <dd>value |
 * <dd>constant |
 * <dd>variable |
 * <dd>function |
 * <dd>'(' expression ')' |
 * <dd>unary_operator expression |
 * <dd>expression binary_operator expression |
 * <dd>expression '?' expression ':' expression
 * <dt>unary_operator ::=
 * <dd>'+' | 
 * <dd>'-' | 
 * <dt>binary_operator ::=
 * <dd>'+' | 
 * <dd>'-' | 
 * <dd>'*' | 
 * <dd>'/' | 
 * <dd>'%' | ... (see SyntaxTableau Javadoc for full list)
 * <dt>value ::=
 * <dd>String |
 * <dd>Number |
 * <dd>Date |
 * <dd>Boolean
 * <dt>constant ::=
 * <dd>'PI' |
 * <dd> 'E' | 
 * <dd>'TRUE' | 
 * <dd>'FALSE'
 * <dt>variable = 
 * <dd> element_of_input_variable_names_array
 * <dt>function ::= 
 * <dd> function_name |
 * <dd> function_name '()' |
 * <dd> function_name '(' function_arguments ')'
 * <dt> function_name (See SyntaxTableau Javadoc for full list)
 * <dt> function_arguments::=
 * <dd> expression |
 * <dd> expression ',' expression ...
 * </dl>
 *
 * @author
 * Design: Leland Wilkinson<BR>
 * Code:   Leland Wilkinson<BR>
 *
 * @version 1.0
 */

public class ParserTableau extends Parser {

	/**
	 * @param syntax An instance of <code>SyntaxTableau</code>.
	 */
	ParserTableau(Syntax syntax) {
		this.syntax = syntax;
		scanner = new Scanner(syntax);
		depth = syntax.getParserDepth();
	}

	/**
	 * Parses an array of statements.
	 * @param statements An array of legal statements in the given syntax.
	 * @return a parse tree.
	 */
	public ParseTree parse(String[] statements) throws ParserException {
		ParseTreeNode[] nodes = new ParseTreeNode[statements.length];

		for (int i = 0; i < statements.length; i++) {
			scanner.setNewStatement(statements[i]);
			nodes[i] = statement(statements, i);
		}

		ParseTree pt = new ParseTree(nodes);
		return pt;
	}

	ParseTreeNode statement(String[] statements, int index) throws ParserException {

		/* This node handles statements */

		String functionName = scanner.getFunction();
		scanner.pushBack();
		if (functionName != null
						&& (functionName.equals("if") || functionName.equals("else") || functionName.equals("elseif")
										|| functionName.equals("while") || functionName.equals("delete")
										|| functionName.equals("drop") || functionName.equals("sample")))
			return controlStatement(functionName, statements, index);

		String brace = scanner.getRightBrace();
		if (brace != null) {
			return null;
		}

		/* assignment statements */

		String[] varNames = scanner.getVariableList();
		if (varNames == null)
			throw new ParserException("missing dependent (assignment) variable");
		int nv = varNames.length;
		target = new int[nv];
		for (int i = 0; i < nv; i++)
			target[i] = syntax.getVariableIndex(varNames[i]);

		if (scanner.getAssignmentOperator() == null)
			throw new ParserException("missing assignment operator");
		functionName = scanner.getFunction();
		scanner.pushBack();

		ParseTreeNode eNode = null;
		if (functionName != null && functionName.equals("code"))
			eNode = codeFunction();
		else if (functionName != null && functionName.equals("cut"))
			eNode = cutFunction();
		else
			eNode = expression();

		char c = scanner.getNextCharacter();
		if (c != 0)
			throw new ParserException("unrecognized token --> " + scanner.getSubstring());

		eNode.target = target;
		for (int i = 0; i < nv; i++) {
			if (largestArrayArgument > 1)
				syntax.setVariableLength(target[i], largestArrayArgument);
		}
		largestArrayArgument = 0;
		return eNode;
	}

	ParseTreeNode controlStatement(String type, String[] statements, int index) throws ParserException {

		/* This node handles control statements. */

		ParseTreeNode result = null;
		int initialStatement = index + 1;
		int terminalStatement = index + 1;
		if (type.equals("if") || type.equals("elseif") || type.equals("while"))
			result = expression();
		if (type.equals("else") || type.equals("delete") || type.equals("drop") || type.equals("sample"))
			result = function();
		if (scanner.hasMoreToScan) {
			String s = scanner.getLeftBrace();
			if (s != null) {
				int leftCount = 1;
				int rightCount = 0;
				if (scanner.hasMoreToScan)
					throw new ParserException("unrecognized token --> " + scanner.getSubstring());
				for (int i = index + 1; i < statements.length; i++) {
					leftCount += statements[i].length() - statements[i].replace("{", "").length();
					rightCount -= statements[i].length() - statements[i].replace("}", "").length();
					if (leftCount + rightCount == 0)
						break;
					terminalStatement++;
				}
			} else {
				throw new ParserException("unrecognized token --> " + scanner.getSubstring());
			}
		} else {
			initialStatement = index + 1;
			terminalStatement = index + 2;
		}
		scanner.setNewStatement(statements[index]);
		result.blockInitialStatement = initialStatement;
		result.blockTerminalStatement = terminalStatement;
		return result;
	}

	ParseTreeNode codeFunction() throws ParserException {
		scanner.getFunction();
		if (scanner.getOpenDelimiter(0) == null)
			throw new ParserException("missing parenthesis for code()");
		ParseTreeNode codeNode = Function.buildFunctionNode("code", null, syntax, target);
		Map map = new HashMap();
		while (scanner.getCloseDelimiter(0) == null && scanner.hasMoreToScan) {
			Object[] keyValue = scanner.getKeyValuePair();
			map.put(keyValue[0], keyValue[1]);
			scanner.getFunctionParameterSeparator();
		}
		((Code) codeNode).recodes = map;
		return codeNode;
	}

	ParseTreeNode cutFunction() throws ParserException {
		scanner.getFunction();
		if (scanner.getOpenDelimiter(0) == null)
			throw new ParserException("missing parenthesis for cut()");
		ParseTreeNode cutNode = Function.buildFunctionNode("cut", null, syntax, target);
		List<Double> cuts = new ArrayList<Double>();
		Double v;
		while (scanner.getCloseDelimiter(0) == null && scanner.hasMoreToScan) {
			v = ((Number) scanner.getNumber()).doubleValue();
			cuts.add(v);
			scanner.getFunctionParameterSeparator();
		}
		Object[] c = cuts.toArray();
		double[] cutpoints = new double[c.length];
		for (int j = 0; j < cutpoints.length; j++) {
			cutpoints[j] = Double.NaN;
			if (c[j] != null)
				cutpoints[j] = ((Double) c[j]).doubleValue();
		}
		((Cut) cutNode).cutpoints = cutpoints;
		return cutNode;
	}

	ParseTreeNode expression() throws ParserException {

		/* This node handles expressions */

		ParseTreeNode result = recurse(2);

		while (true) {
			String o = scanner.getBinaryOperator();
			if (o == null)
				break;
			if (syntax.getOperatorPrecedence(o) == 1) {
				ParseTreeNode[] args = { result, expression(), expression() };
				result = Operator.buildBinaryOperatorNode(o, args, syntax, target);
			} else {
				scanner.pushBack();
				break;
			}
		}

		return result;
	}

	ParseTreeNode recurse(int level) throws ParserException {

		/* This is a recursion of nodes to handle nested expressions */

		ParseTreeNode result;
		ParseTreeNode[] args;

		if (level < depth)
			result = recurse(level + 1);
		else
			result = function();
		while (true) {
			String o = scanner.getBinaryOperator();
			if (o == null)
				break;
			if (syntax.getOperatorPrecedence(o) == level) {
				if (level < depth)
					args = new ParseTreeNode[] { result, recurse(level + 1) };
				else
					args = new ParseTreeNode[] { result, function() };
				result = Operator.buildBinaryOperatorNode(o, args, syntax, target);
			} else {
				scanner.pushBack();
				break;
			}
		}
		return result;
	}

	ParseTreeNode function() throws ParserException {

		/* This node handles functions and unary operators */

		String f = scanner.getFunction();

		if (f != null) {
			Class cl = (Class) syntax.functions.get(f);

			/* LAG function handled as a ConstantNode */

			if (cl.equals(Lag.class)) {
				if (scanner.getOpenDelimiter(0) == null)
					throw new ParserException("missing function parenthesis");
				String v = scanner.getVariable();
				if (v == null)
					throw new ParserException("missing variable name");
				scanner.getFunctionParameterSeparator();
				Object n = scanner.getNumber();
				if (n == null)
					n = new Integer(1);
				if (scanner.getCloseDelimiter(0) == null)
					throw new ParserException("missing closing parenthesis for function");
				int index = syntax.getVariableIndex(v);
				int lag = ((Number) n).intValue();
				syntax.setMaxLag(lag);
				return new ConstantNode(index, lag);

				/* All other functions */

			} else {
				int minp = syntax.getMinFunctionParameters(f);
				int maxp = syntax.getMaxFunctionParameters(f);
				if (minp == 0 && maxp == 0)
					return Function.buildFunctionNode(f, new ParseTreeNode[0], syntax, target);
				if (scanner.getOpenDelimiter(0) == null && minp > 0)
					throw new ParserException("missing function parenthesis");
				int nv = 0;
				Vector v = new Vector();
				while (scanner.getCloseDelimiter(0) == null && scanner.hasMoreToScan) {
					String[] varNames = scanner.getVariableRange();
					if (varNames != null) {
						nv = varNames.length;
						for (int i = 0; i < nv; i++) {
							v.add(new ConstantNode(syntax.getVariableIndex(varNames[i])));
						}
					} else {
						v.add(expression());
					}
					scanner.getFunctionParameterSeparator();
				}
				nv = v.size();
				if (nv < minp || nv > maxp)
					throw new ParserException("Parser error: wrong number of function parameters");
				ParseTreeNode[] args = new ParseTreeNode[nv];
				v.copyInto(args);
				if (cl.equals(Array.class)) {
					for (int k = 0; k < target.length; k++)
						syntax.setVariableLength(target[k], args.length);
				}
				Function g = Function.buildFunctionNode(f, args, syntax, target);
				if (g.isScalarResult)
					largestArrayArgument = 1;
				return g;
			}
		}

		/* unary operator parsed if no function found */

		String o = scanner.getUnaryOperator();
		if (o != null) {
			ParseTreeNode[] args = { function() };
			return Operator.buildUnaryOperatorNode(o, args, syntax, target);
		}
		return element();
	}

	ParseTreeNode element() throws ParserException {

		/* This node handles primitives and recursion of "(<expression>)" */

		ParseTreeNode result = null;

		String v = scanner.getVariable();
		if (v != null) {
			int index = syntax.getVariableIndex(v);
			int argumentLength = syntax.getVariableLength(index);
			if (argumentLength > largestArrayArgument)
				largestArrayArgument = argumentLength;
			return new ConstantNode(index);
		}

		Object n = scanner.getNumber();
		if (n != null)
			return new ConstantNode(n);

		Object d = scanner.getDate();
		if (d != null)
			return new ConstantNode(d);

		Object s = scanner.getString();
		if (s != null)
			return new ConstantNode(s);

		Object c = scanner.getConstant();
		if (c != null)
			return new ConstantNode(c);

		if (scanner.getOpenDelimiter(0) != null) {
			result = expression();
			if (scanner.getCloseDelimiter(0) == null)
				throw new ParserException("mismatched parenthesis");
			return result;
		}

		throw new ParserException("unrecognized token --> " + scanner.getSubstring());
	}
}
