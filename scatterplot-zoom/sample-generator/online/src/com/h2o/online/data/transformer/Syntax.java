/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;

/**
 * This is the base class for language syntax. <code>Syntax</code> governs the behavior
 * of <code>Parser</code> and <code>Executer</code>. Its subclass, <code>SyntaxTableau</code>, implements the Tableau
 * transformation language. <code>Syntax</code> contains setters to govern its
 * behavior - including symbol, operator, and function names, as well as localization,
 * case sensitivity, and grammar rules. Custom parsers can be constructed by using these
 * setters to customize <code>SyntaxTableau</code>. For example, if you wish to override
 * the default log function in <code>SyntaxTableau</code>, use <code>addFunction("log",MyLog.class)</code> to replace
 * the default <code>Log.class</code>. If you wish only to change the name of the
 * default log function to "logarithm", remove the old function reference
 * with <code>removeFunction("log")</code> and then use <code>addFunction("logarithm",Log.class)</code> to add the new
 * key and its associated class to the library.<br>
 * The subclasses of <code>Syntax</code> are provided for convenience. It should not
 * be necessary to extend <code>Syntax</code> to make a new parser. Instead, use the
 * setters provided in order to customize <code>SyntaxTableau</code>.
 * 
 * @author
 *         Design: Leland Wilkinson<BR>
 *         Code: Leland Wilkinson
 * 
 * @version 1.0
 * @see SyntaxTableau
 */

public abstract class Syntax {

	public boolean isDeleted;

	protected Hashtable functions, unaryOperators, binaryOperators, constants;
	protected Stack<Boolean> ifStack = new Stack<Boolean>();

	private boolean isCaseSensitive = false;
	private boolean hasBooleanLogic = true;

	private String[] openDelimiters = { "(", "{", "[", "<" };
	private String[] closeDelimiters = { ")", "}", "]", ">" };
	private String sequenceOperator = "..";
	private String assignmentOperator = "=";

	private char[] skipOverCharacters = { ' ', '\t' };
	private char[] alphaCharacters = { '_', '$', '.' };
	private char quoteCharacter = '"';
	private char functionParameterSeparator = ',';
	private Object[] missingValues = null;

	private int parserDepth = 13;
	private int maxLag = 0;

	private Locale locale = Locale.getDefault();
	private int dateStyle = DateFormat.SHORT;
	private DateFormat df = DateFormat.getDateInstance(dateStyle, locale);
	private NumberFormat nf = NumberFormat.getInstance();
	{
		nf.setGroupingUsed(false);
	}

	private String[] variableNames = {};
	private int[] variableLengths = {};

	/*-------------------------------------------------------------------

	Setters

	-------------------------------------------------------------------*/

	protected void resetStack() {
		ifStack = new Stack<Boolean>();
	}

	protected void setMaxLag(int maxLag) {
		if (maxLag > this.maxLag)
			this.maxLag = maxLag;
	}

	protected void setParserDepth(int parserDepth) {
		this.parserDepth = parserDepth;
	}

	protected int addVariable(String variableName) {
		int n = variableNames.length;
		String[] tNames = new String[n + 1];
		System.arraycopy(variableNames, 0, tNames, 0, n);
		int[] tLengths = new int[n + 1];
		System.arraycopy(variableLengths, 0, tLengths, 0, n);
		variableNames = tNames;
		variableNames[n] = variableName;
		variableLengths = tLengths;
		variableLengths[n] = 1;
		return n;
	}

	public void setVariableLength(int index, int length) {
		variableLengths[index] = length;
	}

	/**
	 * Sets variable names to a given array of variable names. These names are used by <code>Parser</code> to scan
	 * expressions and by <code>Executer</code> to
	 * reference field names in an input row of data.
	 * 
	 * @param variableNames
	 *            Array of variable names.
	 * 
	 */
	public void setVariableNames(String[] variableNames) {
		this.variableNames = variableNames;
		variableLengths = new int[variableNames.length];
		Arrays.fill(variableLengths, 1);
	}

	/**
	 * Set locale for processing numbers and/or dates. When <code>Syntax</code> is used for
	 * parsing, this determines how number and date constants are read.
	 * When <code>Syntax</code> is used for execution, this determines how numbers and dates
	 * are processed in functions and formatted for output.
	 * 
	 * @param locale
	 *            Locale to be used.
	 * 
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
		nf = NumberFormat.getInstance();
		df = DateFormat.getDateInstance(dateStyle, locale);
	}

	/**
	 * Set date style used by <code>java.text.DateFormat</code> for processing dates.
	 * Values are DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, and DateFormat.FULL.
	 * 
	 * @param dateStyle
	 *            Date style to be used.
	 * 
	 */
	public void setDateStyle(int dateStyle) {
		this.dateStyle = dateStyle;
		df = DateFormat.getDateInstance(dateStyle, locale);
	}

	/**
	 * Set whether or not numerical grouping will be used by java.text.NumberFormat.
	 * Default setting is false.
	 * 
	 * @param isGroupingUsed
	 *            Whether grouping is to be used.
	 * 
	 */
	public void setNumericalGrouping(boolean isGroupingUsed) {
		nf.setGroupingUsed(isGroupingUsed);
	}

	/**
	 * Set case sensitivity used by parser.
	 * 
	 * @param isCaseSensitive
	 *            Determines case sensitivity.
	 * 
	 */
	public void setCaseSensitivity(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}

	/**
	 * Set list of characters to be skipped over when scanning for next token in a string.
	 * The default skip-over characters are tab (<code>/t</code>) and blank space (<code>' '</code>).
	 * 
	 * @param skipOverCharacters
	 *            Array of skip-over characters.
	 * 
	 */
	public void setSkipOverCharacters(char[] skipOverCharacters) {
		this.skipOverCharacters = skipOverCharacters;
	}

	/**
	 * Set list of characters that are considered alphanumeric for purposes of defining
	 * a legal variable name. These always include letters of the alphabet and numerals (using the
	 * Java method <code>Character.isLetterOrDigit()</code>),
	 * plus this list. The default alphanumeric characters in this list are underscore (<code>_</code>)
	 * and dollar sign (<code>$</code>).
	 * 
	 * @param alphaCharacters
	 *            Array of extra alphanumeric characters.
	 * 
	 */
	public void setAlphanumericCharacters(char[] alphaCharacters) {
		this.alphaCharacters = alphaCharacters;
	}

	/**
	 * Set missing values to be used in addition to <code>Double.NaN</code>.
	 * 
	 * @param missingValues
	 *            Array of extra missing values).
	 * 
	 */
	public void setMissingValues(Object[] missingValues) {
		this.missingValues = missingValues;
	}

	/**
	 * Set quote character used to delimit strings input to parser.
	 * The default quote character is a double-quotation mark (<code>"</code>).
	 * 
	 * @param quoteCharacter
	 *            Character used to delimit strings.
	 * 
	 */
	public void setQuoteCharacter(char quoteCharacter) {
		this.quoteCharacter = quoteCharacter;
	}

	/**
	 * Set character used to separate arguments in function parameter lists.
	 * The default separator character is a comma (<code>,</code>).
	 * 
	 * @param functionParameterSeparator
	 *            Character used to separate arguments.
	 * 
	 */
	public void setFunctionParameterSeparator(char functionParameterSeparator) {
		this.functionParameterSeparator = functionParameterSeparator;
	}

	/**
	 * Set character used to denote variable lists through a range operation.
	 * The default sequence operator is a double-period (<code>..</code>), as in <code>a .. z</code>.
	 * 
	 * @param sequenceOperator
	 *            String of characters used to denote a range of variables.
	 * 
	 */
	public void setSequenceOperator(String sequenceOperator) {
		this.sequenceOperator = sequenceOperator;
	}

	/**
	 * Set list of characters used as open delimiters, such as parenthesis (<code>(</code>), brace(<code>{</code>),
	 * bracket(<code>[</code>), and
	 * angle-bracket (<code><</code>).
	 * 
	 * @param openDelimiters
	 *            Array of open delimiters (default = <code>{"("}</code>).
	 * 
	 */
	public void setOpenDelimiters(String[] openDelimiters) {
		this.openDelimiters = openDelimiters;
	}

	/**
	 * Set list of characters used as close delimiters, such as parenthesis (<code>)</code>), brace(<code>}</code>),
	 * bracket(<code>]</code>), and
	 * angle-bracket (<code>></code>).
	 * 
	 * @param closeDelimiters
	 *            Array of close delimiters (default = <code>{")"}</code>).
	 * 
	 */
	public void setCloseDelimiters(String[] closeDelimiters) {
		this.closeDelimiters = closeDelimiters;
	}

	/**
	 * Set whether booleans are used in logical operations.
	 * If <code>booleanLogic == true, FALSE = Boolean(false)</code> and <code>TRUE = Boolean(true)</code><br>
	 * If <code>booleanLogic = false, FALSE = Number(0), TRUE = </code> any other number<br>
	 * 
	 * @param booleanLogic
	 *            Whether booleans are used.
	 * 
	 */
	public void setBooleanLogic(boolean booleanLogic) {
		this.hasBooleanLogic = booleanLogic;
	}

	/**
	 * Sets assignment operator.
	 * 
	 * @param assignmentOperator
	 *            String containing the operator (e.g., "<-" or "=").
	 * 
	 */
	public void setAssignmentOperator(String assignmentOperator) {
		this.assignmentOperator = assignmentOperator;
	}

	/**
	 * Adds a unary operator class to library of operator classes.
	 * Here is an example of a class to implement a unary operator that cumulates a series from 0
	 * to the integral value of a number. <code><pre>
	 * <font color="green">
	 * import com.spss.parser.*;
	 * public class Cumulate extends Operator {
	 * 
	 *     public static int precedence = 1;
	 *     public Cumulate(Node[] children) {
	 *         this.children = children;
	 *     }
	 *     public Object compute(Object[] parms, Data data, Syntax syntax) throws ExecException {
	 *         long v = -1;
	 *         if (parms[0] instanceof Number)
	 *             v =  ((Number) parms[0]).longValue();
	 *         if (v >= 0)
	 *             return new Long(v * (v + 1) / 2);
	 *         else
	 *             throw new ExecException("illegal value");
	 *     }
	 * }
	 * </font>
	 * </pre></code>
	 * 
	 * @param key
	 *            The name of the operator in the selected syntax (e.g., "<code>@</code>").
	 * @param o
	 *            The class name of the operator (e.g., <code>Cumulate.class</code>).
	 * 
	 */
	public void addUnaryOperator(String key, Class o) {
		unaryOperators.put(key, o);
	}

	/**
	 * Adds a binary operator class to library of operator classes.
	 * Here is an example of a class to implement an approximately equal function. This function
	 * tests equality within a delta neighborhood. The <code>precedence</code> field is required.
	 * It is needed to position the operator within the precedence hierarchy. The <code>delta</code> field is optional.
	 * It is used to set the delta neighborhood value. <code><pre>
	 * <font color="green">
	 * import com.spss.parser.*;
	 * public class ApproximatelyEqual extends Operator {
	 * 
	 *     public static int precedence = 7;
	 *     public static double delta = .00001;
	 * 
	 *     public ApproximatelyEqual(Node[] children) {
	 *         this.children = children;
	 *     }
	 * 
	 *     public Object compute(Object[] parms, Data data, Syntax syntax) throws ExecException {
	 *         Object o0 = parms[0];
	 *         Object o1 = parms[1];
	 *         if ((o0 instanceof Number) && (o1 instanceof Number)) {
	 *             double v0 = ((Number) o0).doubleValue();
	 *             double v1 = ((Number) o1).doubleValue();
	 *             double fuzz = delta * Math.min(Math.abs(v0), Math.abs(v1));
	 *             if (syntax.getBooleanLogic()) {
	 *                 if (Math.abs(v0 - v1) < fuzz)
	 *                     return new Boolean(true);
	 *                 else
	 *                     return new Boolean(false);
	 *             }
	 *             else {
	 *                 if (Math.abs(v0 - v1) < fuzz)
	 *                     return new Double(1.);
	 *                 else
	 *                     return new Double(0.);
	 *             }
	 *         }
	 *         else
	 *             throw new ExecException("requires numerical arguments");
	 *      }
	 * }
	 * </font>
	 * </pre></code>
	 * 
	 * @param key
	 *            The name of the operator in the selected syntax (e.g., "<code>=~</code>").
	 * @param o
	 *            The class name of the operator (e.g., <code>ApproximatelyEqual.class</code>).
	 * 
	 */
	public void addBinaryOperator(String key, Class o) {
		binaryOperators.put(key, o);
	}

	/**
	 * Adds a function class to library of function classes.
	 * Here is an example of a class to implement a function that takes first-differences of a series.
	 * The parameter <code>minParms</code> is required. It is set to 2 so that the parser will raise
	 * an exception if fewer arguments are fed to it. The parameter <code>maxParms</code> is also required.
	 * It is set to infinity so that any number of arguments will be accepted. <code><pre>
	 * <font color="green">
	 * import com.spss.parser.*;
	 * public class Difference extends Function {
	 * 
	 *     public static int minParms = 2;
	 *     public static int maxParms = Integer.MAX_VALUE;
	 * 
	 *     public Difference(Node[] children) {
	 *         this.children = children;
	 *     }
	 * 
	 *     public Object compute(Object[] parms, Data data, Syntax syntax) throws ExecException {
	 *         Double[] result = new Double[parms.length-1];
	 *         for (int i = 0; i < parms.length-1; i++) {
	 *             try {
	 *                 double v = ((Number) parms[i+1]).doubleValue() -
	 *                            ((Number) parms[i]).doubleValue();
	 *                 result[i] = new Double(v);
	 *             } catch (ClassCastException cc) {
	 *                 throw new ExecException(defaultMessage);
	 *             }
	 *         }
	 *         return result;
	 *     }
	 * }
	 * </font>
	 * </pre></code>
	 * 
	 * @param key
	 *            The name of the function in the selected syntax (e.g., "<code>sqrt</code>").
	 * @param f
	 *            The class name of the function (e.g., <code>SquareRoot.class</code>).
	 * 
	 */
	public void addFunction(String key, Class f) {
		functions.put(key, f);
	}

	/**
	 * Adds a constant to library of constants.
	 * 
	 * @param key
	 *            The name of the constant in the selected syntax (e.g., "<code>pi</code>").
	 * @param c
	 *            The constant (e.g., <code>new Double(Math.PI)</code>).
	 * 
	 */
	public void addConstant(String key, Object c) {
		constants.put(key, c);
	}

	/**
	 * Removes a unary operator class from library of operator classes.<br>
	 * 
	 * @param key
	 *            The name of the operator in the selected syntax (e.g., "<code>-</code>").
	 * 
	 */
	public void removeUnaryOperator(String key) {
		unaryOperators.remove(key);
	}

	/**
	 * Removes a binary operator class from library of operator classes.
	 * 
	 * @param key
	 *            The name of the operator in the selected syntax (e.g., "<code>+</code>").
	 * 
	 */
	public void removeBinaryOperator(String key) {
		binaryOperators.remove(key);
	}

	/**
	 * Removes a function class from library of function classes.
	 * 
	 * @param key
	 *            The name of the function in the selected syntax (e.g., "<code>sqrt</code>").
	 * 
	 */
	public void removeFunction(String key) {
		functions.remove(key);
	}

	/**
	 * Removes a constant from library of constants.
	 * 
	 * @param key
	 *            The name of the constant in the selected syntax (e.g., "<code>pi</code>").
	 * 
	 */
	public void removeConstant(String key) {
		constants.remove(key);
	}

	/*-------------------------------------------------------------------

	Getters

	-------------------------------------------------------------------*/

	protected String getAssignmentOperator() {
		return assignmentOperator;
	}

	protected String getUnaryOperatorName(Object cls) {
		for (Enumeration e = unaryOperators.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			if (unaryOperators.get(key) == cls)
				return (String) key;
		}
		return null;
	}

	protected String getBinaryOperatorName(Object cls) {
		for (Enumeration e = binaryOperators.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			if (binaryOperators.get(key) == cls)
				return (String) key;
		}
		return null;
	}

	protected String getFunctionName(Object cls) {
		for (Enumeration e = functions.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			if (functions.get(key) == cls)
				return (String) key;
		}
		return null;
	}

	protected String getConstantName(Object constant) {
		for (Enumeration e = constants.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			if (constants.get(key).equals(constant))
				return (String) key;
		}
		return null;
	}

	protected Object[] getMissingValues() {
		return missingValues;
	}

	protected NumberFormat getNumberFormat() {
		return nf;
	}

	protected DateFormat getDateFormat() {
		return df;
	}

	protected Locale getLocale() {
		return locale;
	}

	protected int getDateStyle() {
		return dateStyle;
	}

	protected int getParserDepth() {
		return parserDepth;
	}

	protected int getMaxLag() {
		return maxLag;
	}

	protected String getOpenDelimiter(int index) {
		return openDelimiters[index];
	}

	protected String getCloseDelimiter(int index) {
		return closeDelimiters[index];
	}

	protected char getFunctionParameterSeparator() {
		return functionParameterSeparator;
	}

	protected String getSequenceOperator() {
		return sequenceOperator;
	}

	protected Operator getBinaryOperator(String key) {
		if (key == null)
			return null;
		else
			return (Operator) binaryOperators.get(key);
	}

	protected Operator getUnaryOperator(String key) {
		if (key == null)
			return null;
		else
			return (Operator) unaryOperators.get(key);
	}

	protected int getOperatorPrecedence(String o) {
		int precedence = 0;
		Class c = (Class) binaryOperators.get(o);
		try {
			Field m = c.getField("precedence");
			precedence = m.getInt(c);
		} catch (Exception e) {
			throw new IllegalStateException();
		}
		return precedence;
	}

	protected Function getFunction(String key) {
		Function f = null;
		if (key == null)
			return f;
		else {
			try {
				Class cl = (Class) functions.get(key);
				Constructor co = cl.getConstructor(new Class[] { ParseTreeNode[].class });
				f = (Function) co.newInstance(new Object[] { null });
			} catch (Exception exc) {
				throw new IllegalStateException(exc.toString());
			}
		}
		return f;
	}

	protected int getMinFunctionParameters(String f) {
		int np = 0;
		Class c = (Class) functions.get(f);
		try {
			Field m = c.getField("minParms");
			np = m.getInt(c);
		} catch (Exception e) {
			throw new IllegalStateException();
		}
		return np;
	}

	protected int getMaxFunctionParameters(String f) {
		int np = 0;
		Class c = (Class) functions.get(f);
		try {
			Field m = c.getField("maxParms");
			np = m.getInt(c);
		} catch (Exception e) {
			throw new IllegalStateException();
		}
		return np;
	}

	protected Object getConstant(String key) {
		if (key == null)
			return null;
		else
			return constants.get(key);
	}

	protected int getVariableIndex(String key) {
		if (key == null)
			return -1;
		else {
			for (int i = 0; i < variableNames.length; i++) {
				if (key.equals(variableNames[i]))
					return i;
			}
			return -1;
		}
	}

	protected String getVariableName(int index) {
		if (variableNames != null)
			return variableNames[index];
		else
			return null;
	}

	public int getVariableLength(int index) {
		if (variableLengths == null)
			return 1;
		else
			return variableLengths[index];
	}

	/**
	 * Get variable names. The array returned is the union of the input variable names
	 * and any new names produced by the statements given to the parser. If this method is
	 * invoked before the parser is run, it will include only those names originally set
	 * using <code>setVariableNames()</code>.
	 * 
	 * @return array of variable names.
	 * 
	 */
	public String[] getVariableNames() {
		return variableNames;
	}

	/**
	 * Get whether boolean constants are legal.
	 * 
	 * @return true if boolean constants are legal.
	 * 
	 */
	public boolean getBooleanLogic() {
		return hasBooleanLogic;
	}

	/*------------------------------------------------------------------

	Booleans

	-------------------------------------------------------------------*/

	protected boolean isOpenDelimiter(String s, int key) {
		if (s == null)
			return false;
		else if (s.equals(openDelimiters[key]))
			return true;
		else
			return false;
	}

	protected boolean isCloseDelimiter(String s, int key) {
		if (s == null)
			return false;
		else if (s.equals(closeDelimiters[key]))
			return true;
		else
			return false;
	}

	protected boolean isSequenceOperator(String s) {
		if (s == null)
			return false;
		else if (s.equals(sequenceOperator))
			return true;
		else
			return false;
	}

	protected boolean isAssignmentOperator(String s) {
		if (s == null)
			return false;
		else if (s.equals(assignmentOperator))
			return true;
		else
			return false;
	}

	protected boolean isUnaryOperator(String s) {
		if (s == null)
			return false;
		else if (unaryOperators.containsKey(s))
			return true;
		else
			return false;
	}

	protected boolean isBinaryOperator(String s) {
		if (s == null)
			return false;
		else if (binaryOperators.containsKey(s))
			return true;
		else
			return false;
	}

	protected boolean isFunction(String s) {
		if (s == null)
			return false;
		else if (functions.containsKey(s))
			return true;
		else
			return false;
	}

	protected boolean isConstant(String s) {
		if (s == null)
			return false;
		else if (constants.containsKey(s))
			return true;
		else
			return false;
	}

	protected boolean isVariable(String s) {
		if (s == null || variableNames == null)
			return false;
		else {
			for (int i = 0; i < variableNames.length; i++) {
				if (s.equals(variableNames[i]))
					return true;
			}
		}
		return false;
	}

	protected boolean isMissing(Object o) {
		if (o == null)
			return true;
		if (missingValues == null)
			return false;
		for (int i = 0; i < missingValues.length; i++) {
			if (o.equals(missingValues[i]))
				return true;
		}
		return false;
	}

	protected boolean isSkipOverCharacter(char c) {
		for (int i = 0; i < skipOverCharacters.length; i++) {
			if (c == skipOverCharacters[i])
				return true;
		}
		return false;
	}

	protected boolean isAlphanumericCharacter(char c) {
		if (Character.isLetterOrDigit(c))
			return true;
		for (int i = 0; i < alphaCharacters.length; i++) {
			if (c == alphaCharacters[i])
				return true;
		}
		return false;
	}

	protected boolean isQuoteCharacter(char c) {
		if (c == quoteCharacter)
			return true;
		else
			return false;
	}

	protected boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	protected boolean isFunctionParameterSeparator(char c) {
		if (c == functionParameterSeparator)
			return true;
		else
			return false;
	}
}
