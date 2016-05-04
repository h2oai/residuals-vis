/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Vector;

/**
 * Scanner extracts a specified type of token from a string starting at a preset position.
 *
 * @author
 * Design: Leland Wilkinson<BR>
 * Code:   Leland Wilkinson
 *
 * @version 1.0
 */

class Scanner {

	private String statement;
	private char[] buffer;
	private int parsePosition, previousPosition;
	private Syntax syntax;
	private ParsePosition pp = new ParsePosition(0);
	private NumberFormat nf;
	private DateFormat df;
	protected boolean hasMoreToScan;
	private static int ASSIGNMENT = 1, OPEN = 2, CLOSE = 3, UNARY_OPERATOR = 4, BINARY_OPERATOR = 5, CONSTANT = 6,
					VARIABLE = 7, FUNCTION = 8, STRING = 9, SEQUENCE = 10, LEFT_BRACE = 11, RIGHT_BRACE = 12;

	Scanner(Syntax syntax) {
		this.syntax = syntax;
		nf = syntax.getNumberFormat();
		df = syntax.getDateFormat();
	}

	Scanner(Syntax syntax, String expression) {
		this.syntax = syntax;
		nf = syntax.getNumberFormat();
		df = syntax.getDateFormat();
		setNewStatement(expression);
	}

	protected void setNewStatement(String statement) {
		if (syntax.isCaseSensitive())
			this.statement = statement;
		else
			this.statement = statement.toLowerCase();
		buffer = this.statement.toCharArray();
		parsePosition = 0;
		hasMoreToScan = true;
	}

	protected String getSubstring() {
		return statement.substring(parsePosition);
	}

	protected String getAssignmentOperator() {
		return getToken(ASSIGNMENT, 0);
	}

	protected char getFunctionParameterSeparator() {
		previousPosition = parsePosition;
		char c = getNextCharacter();
		if (c == 0)
			return 0;
		else if (syntax.isFunctionParameterSeparator(c))
			return c;
		else {
			pushBack();
			return 0;
		}
	}

	protected String getOpenDelimiter(int index) {
		return getToken(OPEN, index);
	}

	protected String getCloseDelimiter(int index) {
		return getToken(CLOSE, index);
	}

	protected String getLeftBrace() {
		return getToken(LEFT_BRACE, 0);
	}

	protected String getRightBrace() {
		return getToken(RIGHT_BRACE, 0);
	}

	protected String getSequenceOperator() {
		return getToken(SEQUENCE, 0);
	}

	protected String getBinaryOperator() {
		return getToken(BINARY_OPERATOR, 0);
	}

	protected String getUnaryOperator() {
		return getToken(UNARY_OPERATOR, 0);
	}

	protected Object getConstant() {
		String key = getToken(CONSTANT, 0);
		if (key == null)
			return null;
		else
			return syntax.getConstant(key);
	}

	protected String getVariable() {
		return getToken(VARIABLE, 0);
	}

	protected String[] getVariableList() {

		/* Get a list of variables, possibly containing one or more sequence operators */

		String[] varList = null;
		Vector v = new Vector();
		int prePosition = parsePosition;

		while (hasMoreToScan) {
			varList = getVariableRange();
			if (varList != null) {
				for (int i = 0; i < varList.length; i++)
					v.add(varList[i]);
			} else {
				String name = getWord();
				if (name == null) {
					previousPosition = prePosition;
					pushBack();
					return null;
				}
				v.add(name);
			}
			if (getFunctionParameterSeparator() == 0)
				break;
		}

		int nv = v.size();
		varList = new String[nv];
		v.copyInto(varList);
		for (int i = 0; i < nv; i++) {
			if (!syntax.isVariable(varList[i]))
				syntax.addVariable(varList[i]);
		}

		setStringBufferParsePosition();
		return varList;
	}

	protected String[] getVariableRange() {

		/* Get a range of variables defined by sequence operator */

		int prePosition = parsePosition;

		String startName = getVariable();

		if (startName == null) {
			previousPosition = prePosition;
			pushBack();
			return null;
		}

		if (getSequenceOperator() == null) {
			previousPosition = prePosition;
			pushBack();
			return null;
		}

		String endName = getVariable();

		if (endName == null) {
			previousPosition = prePosition;
			pushBack();
			return null;
		}

		int startIndex = syntax.getVariableIndex(startName);
		int endIndex = syntax.getVariableIndex(endName);

		if (startIndex >= endIndex) {
			previousPosition = prePosition;
			pushBack();
			return null;
		}

		int nv = endIndex - startIndex + 1;
		String[] varRange = new String[nv];

		int iv = startIndex;
		for (int i = 0; i < nv; i++) {
			varRange[i] = syntax.getVariableName(iv);
			iv++;
		}

		setStringBufferParsePosition();
		return varRange;
	}

	protected String getFunction() {
		return getToken(FUNCTION, 0);
	}

	protected Object getNumber() {
		previousPosition = parsePosition;
		char c = getNextCharacter();
		if (c == 0)
			return null;
		parsePosition--;
		setStringBufferParsePosition();
		String s = statement.toUpperCase();

		/* keep Java number parser from gobbling up separator (comma) */

		int pos = s.indexOf(syntax.getFunctionParameterSeparator(), parsePosition);
		if (pos > 0)
			s = s.substring(0, pos);

		Number n = null;
		try {
			n = nf.parse(s, pp);
		} catch (Exception pe) {
			parsePosition = previousPosition;
		}
		setCharBufferParsePosition();
		return n;
	}

	protected Object getDate() {
		previousPosition = parsePosition;
		char c = getNextCharacter();
		if (c == 0)
			return null;
		parsePosition--;
		setStringBufferParsePosition();
		String s = statement;

		/* keep Java date parser from gobbling up separator (comma) */

		int pos = s.indexOf(syntax.getFunctionParameterSeparator(), parsePosition);
		if (pos > 0)
			s = s.substring(0, pos);

		Date d = null;
		try {
			d = df.parse(s, pp);
		} catch (Exception pe) {
			parsePosition = previousPosition;
		}
		setCharBufferParsePosition();
		return d;
	}

	protected String getString() {
		String key = getToken(STRING, 0);
		return key;
	}

	protected Object[] getKeyValuePair() {
		String keyWord = getWord();
		if (keyWord == null)
			keyWord = getQuotedWord();
		Object key;
		Number n;
		try {
			n = nf.parse(keyWord);
			key = new Double(n.doubleValue());
		} catch (Exception pe) {
			key = keyWord;
		}

		char c = getNextCharacter();
		if (c != '=')
			return null;
		String valueWord = getWord();
		if (valueWord == null)
			valueWord = getQuotedWord();
		Object value;
		try {
			n = nf.parse(valueWord);
			value = new Double(n.doubleValue());
		} catch (Exception pe) {
			value = valueWord;
		}
		return new Object[] { key, value };
	}

	protected String getWord() {
		String s = "";
		previousPosition = parsePosition;
		char c = getNextCharacter();
		if (c == 0)
			return null;
		while (syntax.isAlphanumericCharacter(c)) {
			s += c;
			c = getCharacter();
		}

		parsePosition--;
		setStringBufferParsePosition();
		if (parsePosition >= buffer.length)
			hasMoreToScan = false;
		if (s.equals(""))
			return null;
		else
			return s;
	}

	protected String getQuotedWord() {
		String s = "";
		previousPosition = parsePosition;
		char c = getNextCharacter();
		if (c == 0 || c != '"')
			return null;
		c = getNextCharacter();
		while (c != 0 && c != '"') {
			s += c;
			c = getCharacter();
		}
		if (c != '"' || s.equals("")) {
			pushBack();
			return null;
		} else {
			return s;
		}
	}

	protected void pushBack() {
		parsePosition = previousPosition;
		setStringBufferParsePosition();
		hasMoreToScan = true;
	}

	private void setStringBufferParsePosition() {
		pp.setIndex(parsePosition);
	}

	private void setCharBufferParsePosition() {
		parsePosition = pp.getIndex();
	}

	protected char getNextCharacter() {
		char c = 0;
		do {
			c = getCharacter();
		} while (syntax.isSkipOverCharacter(c));
		return c;
	}

	private char getCharacter() {
		char c = 0;
		if (parsePosition < buffer.length) {
			c = buffer[parsePosition];
			parsePosition++;
		}
		return c;
	}

	private String getToken(int type, int index) {
		String result = null;
		String s = "";
		int position = parsePosition;
		previousPosition = parsePosition;
		char c = getNextCharacter();
		if (c == 0)
			return null;
		if (type == STRING) {
			if (syntax.isQuoteCharacter(c)) {
				c = getCharacter();
				while (c != 0 && !syntax.isQuoteCharacter(c)) {
					s += c;
					c = getCharacter();
				}
				result = s;
				position = parsePosition;
			} else
				position = previousPosition;
		} else {
			do {
				s += c;
				if (matchString(type, index, s)) {
					result = s;
					position = parsePosition;
				}
				c = getCharacter();
			} while (c != 0);
		}

		parsePosition = position;
		if (parsePosition >= buffer.length)
			hasMoreToScan = false;
		setStringBufferParsePosition();
		return result;
	}

	private boolean matchString(int type, int index, String key) {
		if (type == ASSIGNMENT)
			return syntax.isAssignmentOperator(key);
		if (type == OPEN)
			return syntax.isOpenDelimiter(key, index);
		if (type == CLOSE)
			return syntax.isCloseDelimiter(key, index);
		if (type == LEFT_BRACE)
			return key.equals("{");
		if (type == RIGHT_BRACE)
			return key.equals("}");
		if (type == UNARY_OPERATOR)
			return syntax.isUnaryOperator(key);
		if (type == BINARY_OPERATOR)
			return syntax.isBinaryOperator(key);
		if (type == SEQUENCE)
			return syntax.isSequenceOperator(key);
		if (type == CONSTANT)
			return syntax.isConstant(key);
		if (type == VARIABLE)
			return syntax.isVariable(key);
		if (type == FUNCTION)
			return syntax.isFunction(key);
		return false;
	}
}
