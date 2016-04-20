/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

import java.util.Hashtable;

/**
 * This class implements syntax for parsing and executing Tableau syntax. <code>SyntaxTableau</code> implements 
 * the Tableau statements and operators (using C precedence rules) and library functions,
 * as well as a collection of statistical and mathematical functions.
 * It includes operator overloading
 * (e.g., addition/concatenation under +). The four data types are Number, String, Date, and Boolean. It supports arrays
 * through an <code>Array()</code> function. It does NOT implement
 * array subscripts.
 * <code>SyntaxTableau</code> incorporates the following <code>Syntax</code> settings:
 * <code><pre>
 *  setOpenDelimiters(new String[] {"("});
 *  setCloseDelimiters(new String[] {")"});
 *  setAlphanumericCharacters(new char[] {'_', '$'});
 *  setSequenceOperator("..");
 *  setCaseSensitivity(true);
 *  setBooleanLogic(true);
 * </pre></code>
 * This class (including its inner classes) provides the following statements, operators, 
 * functions, and constants:<br><br>
 * 
  * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>if</code></td>
 *      <td><code>If</code></td>
 *      <td><code>if statement</code></td>
 *      <td><code>expression</code></td>
 *      <td><code>Boolean</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>else</code></td>
 *      <td><code>Else</code></td>
 *      <td><code>else statement</code></td>
 *      <td><code></code></td>
 *      <td><code>Boolean</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>while</code></td>
 *      <td><code>While</code></td>
 *      <td><code>while statement</code></td>
 *      <td><code>expression</code></td>
 *      <td><code>Boolean</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>Precedence</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>+</code></td>
 *      <td><code>UnaryPositive</code></td>
 *      <td><code>unary positive</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>13</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>-</code></td>
 *      <td><code>UnaryNegative</code></td>
 *      <td><code>unary negative</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>13</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>Precedence</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>+</code></td>
 *      <td><code>Add</code></td>
 *      <td><code>addition</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>10</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>-</code></td>
 *      <td><code>Subtract</code></td>
 *      <td><code>subtraction</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>10</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>*</code></td>
 *      <td><code>Multiply</code></td>
 *      <td><code>multiplication</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>11</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>/</code></td>
 *      <td><code>Divide</code></td>
 *      <td><code>division</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>11</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>%</code></td>
 *      <td><code>Remainder</code></td>
 *      <td><code>remainder</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>11</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>^</code></td>
 *      <td><code>Exponent</code></td>
 *      <td><code>exponentiation</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>11</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>&</code></td>
 *      <td><code>And</code></td>
 *      <td><code>logical and</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>|</code></td>
 *      <td><code>Or</code></td>
 *      <td><code>logical or</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>==</code></td>
 *      <td><code>Equal</code></td>
 *      <td><code>equality</code></td>
 *      <td><code>Number | String | Date | Boolean</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>7</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>!=</code></td>
 *      <td><code>NotEqual</code></td>
 *      <td><code>inequality</code></td>
 *      <td><code>Number | String | Date | Boolean</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>7</code></td>
 *    </tr>
 *    <tr>
 *      <td><code><</code></td>
 *      <td><code>LessThan</code></td>
 *      <td><code>less than</code></td>
 *      <td><code>Number | String | Date</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>8</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>></code></td>
 *      <td><code>GreaterThan</code></td>
 *      <td><code>greater than</code></td>
 *      <td><code>Number | String | Date</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>8</code></td>
 *    </tr>
 *    <tr>
 *      <td><code><=</code></td>
 *      <td><code>LessThanOrEqual</code></td>
 *      <td><code>less than or equal to</code></td>
 *      <td><code>Number | String | Date</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>8</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>>=</code></td>
 *      <td><code>GreaterThanOrEqual</code></td>
 *      <td><code>greater than or equal to</code></td>
 *      <td><code>Number | String | Date</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>8</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>record</code></td>
 *      <td><code>Record</code></td>
 *      <td><code>record number</code></td>
 *      <td><code>Number</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>delete</code></td>
 *      <td><code>Delete</code></td>
 *      <td><code>delete current record</code></td>
 *      <td><code>Boolean</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>minParms</b></code></td>
 *      <td><code><b>maxParms</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>abs</code></td>
 *      <td><code>AbsoluteValue</code></td>
 *      <td><code>absolute value</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sign</code></td>
 *      <td><code>Sign</code></td>
 *      <td><code>signum</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sqrt</code></td>
 *      <td><code>SquareRoot</code></td>
 *      <td><code>square root</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>exp</code></td>
 *      <td><code>ExponentialFunction</code></td>
 *      <td><code>exponential</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>log</code></td>
 *      <td><code>Log</code></td>
 *      <td><code>natural log</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>log2</code></td>
 *      <td><code>Log2</code></td>
 *      <td><code>base 2 log</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>log10</code></td>
 *      <td><code>Log10</code></td>
 *      <td><code>decimal log</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>floor</code></td>
 *      <td><code>Floor</code></td>
 *      <td><code>floor</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>ceil</code></td>
 *      <td><code>Ceiling</code></td>
 *      <td><code>ceiling</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>int</code></td>
 *      <td><code>IntegerPart</code></td>
 *      <td><code>integer part</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>round</code></td>
 *      <td><code>Round</code></td>
 *      <td><code>round</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sin</code></td>
 *      <td><code>Sine</code></td>
 *      <td><code>sine</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>cos</code></td>
 *      <td><code>Cosine</code></td>
 *      <td><code>cosine</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>tan</code></td>
 *      <td><code>Tangent</code></td>
 *      <td><code>tangent</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>asin</code></td>
 *      <td><code>ArcSine</code></td>
 *      <td><code>inverse sine</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>acos</code></td>
 *      <td><code>ArcCosine</code></td>
 *      <td><code>inverse cosine</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>atan</code></td>
 *      <td><code>ArcTangent</code></td>
 *      <td><code>inverse tangent</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sinh</code></td>
 *      <td><code>HyperbolicSine</code></td>
 *      <td><code>hyperbolic sine</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>cosh</code></td>
 *      <td><code>HyperbolicCosine</code></td>
 *      <td><code>hyperbolic cosine</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>tanh</code></td>
 *      <td><code>HyperbolicTangent</code></td>
 *      <td><code>hyperbolic tangent</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>atanh</code></td>
 *      <td><code>ArcHyperbolicTangent</code></td>
 *      <td><code>inverse hyperbolic tangent</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>gamma</code></td>
 *      <td><code>Gamma</code></td>
 *      <td><code>gamma function</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>lgamma</code></td>
 *      <td><code>LogGamma</code></td>
 *      <td><code>log gamma function</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>pow</code></td>
 *      <td><code>Power</code></td>
 *      <td><code>power function</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>mod</code></td>
 *      <td><code>Modulo</code></td>
 *      <td><code>modulo function</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>atan2</code></td>
 *      <td><code>Atan2</code></td>
 *      <td><code>inverse tangent</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>Number | Array</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>minParms</b></code></td>
 *      <td><code><b>maxParms</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>standardize</code></td>
 *      <td><code>Standardize</code></td>
 *      <td><code>standardize column(s)</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>unitize</code></td>
 *      <td><code>Unitize</code></td>
 *      <td><code>unitize column(s) [0,1]</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>rank</code></td>
 *      <td><code>Rank</code></td>
 *      <td><code>rank values in column(s)</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sample</code></td>
 *      <td><code>Sample</code></td>
 *      <td><code>sample records</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>dataset</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>minParms</b></code></td>
 *      <td><code><b>maxParms</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>drop</code></td>
 *      <td><code>Drop</code></td>
 *      <td><code>drop column(s) from output</code></td>
 *      <td><code>variable_list</code></td>
 *      <td><code>Boolean</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>cut</code></td>
 *      <td><code>Cut</code></td>
 *      <td><code>cut distribution into fractiles</code></td>
 *      <td><code>number_list</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>code</code></td>
 *      <td><code>Code</code></td>
 *      <td><code>recode function</code></td>
 *      <td><code>"a"="u","b"="v",...</code></td>
 *      <td><code>recoded values</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>lag</code></td>
 *      <td><code>Lag</code></td>
 *      <td><code>lag function</code></td>
 *      <td><code>varname, lagNumber</code></td>
 *      <td><code>value</code></td>
 *      <td><code>1</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>array</code></td>
 *      <td><code>Array</code></td>
 *      <td><code>array function</code></td>
 *      <td><code>expression_list</code></td>
 *      <td><code>Array</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>nonmissing</code></td>
 *      <td><code>NotMissing</code></td>
 *      <td><code>number of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>missing</code></td>
 *      <td><code>Missing</code></td>
 *      <td><code>number of missing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sum</code></td>
 *      <td><code>Sum</code></td>
 *      <td><code>sum of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>product</code></td>
 *      <td><code>Product</code></td>
 *      <td><code>product of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>mean</code></td>
 *      <td><code>Mean</code></td>
 *      <td><code>mean of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>median</code></td>
 *      <td><code>Median</code></td>
 *      <td><code>median of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>sd</code></td>
 *      <td><code>StandardDeviation</code></td>
 *      <td><code>SD of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>se</code></td>
 *      <td><code>StandardError</code></td>
 *      <td><code>SE of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>variance</code></td>
 *      <td><code>Variance</code></td>
 *      <td><code>variance of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>cv</code></td>
 *      <td><code>CoefficientOfVariation</code></td>
 *      <td><code>CV of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>min</code></td>
 *      <td><code>Minimum</code></td>
 *      <td><code>min of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>max</code></td>
 *      <td><code>Maximum</code></td>
 *      <td><code>max of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>range</code></td>
 *      <td><code>Range</code></td>
 *      <td><code>range of nonmissing</code></td>
 *      <td><code>expression_list | Array</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>many</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>minParms</b></code></td>
 *      <td><code><b>maxParms</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>substring</code></td>
 *      <td><code>Substring</code></td>
 *      <td><code>substring</code></td>
 *      <td><code>String, startNumber, lengthNumber</code></td>
 *      <td><code>String</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>lowercase</code></td>
 *      <td><code>LowerCase</code></td>
 *      <td><code>to lower case</code></td>
 *      <td><code>String</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>uppercase</code></td>
 *      <td><code>UpperCase</code></td>
 *      <td><code>to upper case</code></td>
 *      <td><code>String</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>length</code></td>
 *      <td><code>StringLength</code></td>
 *      <td><code>length</code></td>
 *      <td><code>String</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>trim</code></td>
 *      <td><code>Trim</code></td>
 *      <td><code>trim both ends</code></td>
 *      <td><code>String</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>ltrim</code></td>
 *      <td><code>LeftTrim</code></td>
 *      <td><code>trim left end</code></td>
 *      <td><code>String {,leftString}</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>rtrim</code></td>
 *      <td><code>RightTrim</code></td>
 *      <td><code>trim right end</code></td>
 *      <td><code>String {, rightString}</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>index</code></td>
 *      <td><code>IndexOf</code></td>
 *      <td><code>index of first</code></td>
 *      <td><code>targetString, sourceString {, indexNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>replace</code></td>
 *      <td><code>Replace</code></td>
 *      <td><code>replace substring</code></td>
 *      <td><code>targetString, oldString, newString</code></td>
 *      <td><code>String</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>concatenate</code></td>
 *      <td><code>Concatenate</code></td>
 *      <td><code>concatenate strings</code></td>
 *      <td><code>leftString,rightString</code></td>
 *      <td><code>String</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>numbertostring</code></td>
 *      <td><code>NumberToString</code></td>
 *      <td><code>number to string</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>stringtonumber</code></td>
 *      <td><code>StringToNumber</code></td>
 *      <td><code>string to number</code></td>
 *      <td><code>String</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>datetostring</code></td>
 *      <td><code>DateToString</code></td>
 *      <td><code>date to string</code></td>
 *      <td><code>Date</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>stringtodate</code></td>
 *      <td><code>StringToDate</code></td>
 *      <td><code>string to date</code></td>
 *      <td><code>String</code></td>
 *      <td><code>Date</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>levenshtein</code></td>
 *      <td><code>Levenshtein</code></td>
 *      <td><code>Levenshtein distance</code></td>
 *      <td><code>firstString, secondString</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>dameraulevenshtein</code></td>
 *      <td><code>DamerauLevenshtein</code></td>
 *      <td><code>Damerau-Levenshtein distance</code></td>
 *      <td><code>firstString, secondString</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>metaphone</code></td>
 *      <td><code>MetaPhone</code></td>
 *      <td><code>MetaPhone code</code></td>
 *      <td><code>String</code></td>
 *      <td><code>String</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>minParms</b></code></td>
 *      <td><code><b>maxParms</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>date</code></td>
 *      <td><code>DateFunction</code></td>
 *      <td><code>current date</code></td>
 *      <td><code></code></td>
 *      <td><code>Date</code></td>
 *      <td><code>0</code></td>
 *      <td><code>0</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>time</code></td>
 *      <td><code>DateFunction</code></td>
 *      <td><code>current time</code></td>
 *      <td><code></code></td>
 *      <td><code>Date</code></td>
 *      <td><code>0</code></td>
 *      <td><code>0</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Class</b></code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Arguments</b></code></td>
 *      <td><code><b>Result</b></code></td>
 *      <td><code><b>minParms</b></code></td>
 *      <td><code><b>maxParms</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>uniformcdf</code></td>
 *      <td><code>UniformCDF</code></td>
 *      <td><code>uniform cdf</code></td>
 *      <td><code>xNumber {, aNumber, bNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>normalcdf</code></td>
 *      <td><code>NormalCDF</code></td>
 *      <td><code>normal cdf</code></td>
 *      <td><code>xNumber {, muNumber, sigmaNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>tcdf</code></td>
 *      <td><code>TCDF</code></td>
 *      <td><code>Student t cdf</code></td>
 *      <td><code>xNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>fcdf</code></td>
 *      <td><code>FCDF</code></td>
 *      <td><code>Fisher F cdf</code></td>
 *      <td><code>xNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>chisquarecdf</code></td>
 *      <td><code>ChisquareCDF</code></td>
 *      <td><code>chi-square cdf</code></td>
 *      <td><code>xNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>gammacdf</code></td>
 *      <td><code>GammaCDF</code></td>
 *      <td><code>gamma cdf</code></td>
 *      <td><code>xNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>betacdf</code></td>
 *      <td><code>BetaCDF</code></td>
 *      <td><code>beta cdf</code></td>
 *      <td><code>xNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>expocdf</code></td>
 *      <td><code>ExponentialCDF</code></td>
 *      <td><code>exponential cdf</code></td>
 *      <td><code>xNumber, locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>logisticcdf</code></td>
 *      <td><code>LogisticCDF</code></td>
 *      <td><code>logistic cdf</code></td>
 *      <td><code>xNumber, locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>weibullcdf</code></td>
 *      <td><code>WeibullCDF</code></td>
 *      <td><code>Weibull cdf</code></td>
 *      <td><code>xNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>binomialcdf</code></td>
 *      <td><code>BinomialCDF</code></td>
 *      <td><code>binomial cdf</code></td>
 *      <td><code>xNumber, pNumber, qNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>poissoncdf</code></td>
 *      <td><code>PoissonCDF</code></td>
 *      <td><code>Poisson cdf</code></td>
 *      <td><code>xNumber, lambdaNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>uniformdensity</code></td>
 *      <td><code>UniformDensity</code></td>
 *      <td><code>uniform density</code></td>
 *      <td><code>xNumber {, aNumber, bNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>normaldensity</code></td>
 *      <td><code>NormalDensity</code></td>
 *      <td><code>normal density</code></td>
 *      <td><code>xNumber {, muNumber, sigmaNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>tdensity</code></td>
 *      <td><code>TDensity</code></td>
 *      <td><code>Student t density</code></td>
 *      <td><code>xNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>fdensity</code></td>
 *      <td><code>FDensity</code></td>
 *      <td><code>Fisher F density</code></td>
 *      <td><code>xNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>chisquaredensity</code></td>
 *      <td><code>ChisquareDensity</code></td>
 *      <td><code>chi-square density</code></td>
 *      <td><code>xNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>gammadensity</code></td>
 *      <td><code>GammaDensity</code></td>
 *      <td><code>gamma density</code></td>
 *      <td><code>xNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>betadensity</code></td>
 *      <td><code>BetaDensity</code></td>
 *      <td><code>beta density</code></td>
 *      <td><code>xNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>expodensity</code></td>
 *      <td><code>ExponentialDensity</code></td>
 *      <td><code>exponential density</code></td>
 *      <td><code>xNumber, locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>logisticdensity</code></td>
 *      <td><code>LogisticDensity</code></td>
 *      <td><code>logistic density</code></td>
 *      <td><code>xNumber, locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>weibulldensity</code></td>
 *      <td><code>WeibullDensity</code></td>
 *      <td><code>Weibull density</code></td>
 *      <td><code>xNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>binomialdensity</code></td>
 *      <td><code>BinomialDensity</code></td>
 *      <td><code>binomial density</code></td>
 *      <td><code>xNumber, pNumber, qNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>poissondensity</code></td>
 *      <td><code>PoissonDensity</code></td>
 *      <td><code>Poisson density</code></td>
 *      <td><code>xNumber, lambdaNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
  *    <tr>
 *      <td><code>uniformrandom</code></td>
 *      <td><code>UniformRandom</code></td>
 *      <td><code>uniform random number</code></td>
 *      <td><code>{aNumber, bNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>0</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>normalrandom</code></td>
 *      <td><code>NormalRandom</code></td>
 *      <td><code>normal random number</code></td>
 *      <td><code>{muNumber, sigmaNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>0</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>trandom</code></td>
 *      <td><code>TRandom</code></td>
 *      <td><code>Student t random number</code></td>
 *      <td><code>dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>frandom</code></td>
 *      <td><code>FRandom</code></td>
 *      <td><code>Fisher F random number</code></td>
 *      <td><code>df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>chisquarerandom</code></td>
 *      <td><code>ChisquareRandom</code></td>
 *      <td><code>chi-square random number</code></td>
 *      <td><code>dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>gammarandom</code></td>
 *      <td><code>GammaRandom</code></td>
 *      <td><code>gamma random number</code></td>
 *      <td><code>dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>betarandom</code></td>
 *      <td><code>BetaRandom</code></td>
 *      <td><code>beta random number</code></td>
 *      <td><code>df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>exporandom</code></td>
 *      <td><code>ExponentialRandom</code></td>
 *      <td><code>exponential random number</code></td>
 *      <td><code>locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>logisticrandom</code></td>
 *      <td><code>LogisticRandom</code></td>
 *      <td><code>logistic random number</code></td>
 *      <td><code>locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>weibullrandom</code></td>
 *      <td><code>WeibullRandom</code></td>
 *      <td><code>Weibull random number</code></td>
 *      <td><code>df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>binomialrandom</code></td>
 *      <td><code>BinomialRandom</code></td>
 *      <td><code>binomial random number</code></td>
 *      <td><code>pNumber, qNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>poissonrandom</code></td>
 *      <td><code>PoissonRandom</code></td>
 *      <td><code>Poisson random number</code></td>
 *      <td><code>lambdaNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>1</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>uniformcdfinv</code></td>
 *      <td><code>UniformInverseCDF</code></td>
 *      <td><code>uniform inverse cdf</code></td>
 *      <td><code>alphaNumber {, aNumber, bNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>normalcdfinv</code></td>
 *      <td><code>NormalInverseCDF</code></td>
 *      <td><code>normal inverse cdf</code></td>
 *      <td><code>alphaNumber {, muNumber, sigmaNumber}</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>1</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>tcdfinv</code></td>
 *      <td><code>TInverseCDF</code></td>
 *      <td><code>Student t inverse cdf</code></td>
 *      <td><code>alphaNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>fcdfinv</code></td>
 *      <td><code>FInverseCDF</code></td>
 *      <td><code>Fisher F inverse cdf</code></td>
 *      <td><code>alphaNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>chisquarecdfinv</code></td>
 *      <td><code>ChisquareInverseCDF</code></td>
 *      <td><code>chi-square inverse cdf</code></td>
 *      <td><code>alphaNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>gammacdfinv</code></td>
 *      <td><code>GammaInverseCDF</code></td>
 *      <td><code>gamma inverse cdf</code></td>
 *      <td><code>alphaNumber, dfNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>betacdfinv</code></td>
 *      <td><code>BetaInverseCDF</code></td>
 *      <td><code>beta inverse cdf</code></td>
 *      <td><code>alphaNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>expocdfinv</code></td>
 *      <td><code>ExponentialInverseCDF</code></td>
 *      <td><code>exponential inverse cdf</code></td>
 *      <td><code>alphaNumber, locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>logisticcdfinv</code></td>
 *      <td><code>LogisticInverseCDF</code></td>
 *      <td><code>logistic inverse cdf</code></td>
 *      <td><code>alphaNumber, locationNumber, spreadNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>weibullcdfinv</code></td>
 *      <td><code>WeibullInverseCDF</code></td>
 *      <td><code>Weibull inverse cdf</code></td>
 *      <td><code>alphaNumber, df1Number, df2Number</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>binomialcdfinv</code></td>
 *      <td><code>BinomialInverseCDF</code></td>
 *      <td><code>binomial inverse cdf</code></td>
 *      <td><code>alphaNumber, pNumber, qNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>3</code></td>
 *      <td><code>3</code></td>
 *    </tr>
 *    <tr>
 *      <td><code>poissoncdfinv</code></td>
 *      <td><code>PoissonInverseCDF</code></td>
 *      <td><code>Poisson inverse cdf</code></td>
 *      <td><code>alphaNumber, lambdaNumber</code></td>
 *      <td><code>Number</code></td>
 *      <td><code>2</code></td>
 *      <td><code>2</code></td>
 *    </tr>
 * </table>
 * <br>
 * <table border=1>
 *    <tr>
 *      <td><code><b>Name</b/</code></td>
 *      <td><code><b>Description</b></code></td>
 *      <td><code><b>Value</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>TRUE</code></td>
 *      <td><code>true</code></td>
 *      <td><code><b>true</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>FALSE</code></td>
 *      <td><code>false</code></td>
 *      <td><code><b>false</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>PI</code></td>
 *      <td><code>PI</code></td>
 *      <td><code><b>Math.PI</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>E</code></td>
 *      <td><code>Euler constant</code></td>
 *      <td><code><b>Math.E</b></code></td>
 *    </tr>
 *    <tr>
 *      <td><code>?</code></td>
 *      <td><code>missing value</code></td>
 *      <td><code><b>Math.Double.NaN</b></code></td>
 *    </tr>
 * </table>
 * 
 * @author
 * Design:  Leland Wilkinson<BR>
 * Code:   Leland Wilkinson
 * 
 * @version 1.0
 */

public class SyntaxTableau extends Syntax {

	public SyntaxTableau() {

		setOpenDelimiters(new String[] { "(" });
		setCloseDelimiters(new String[] { ")" });
		setSequenceOperator("..");
		setCaseSensitivity(true);
		setBooleanLogic(true);

		unaryOperators = new Hashtable();
		unaryOperators.put("+", UnaryPositive.class);
		unaryOperators.put("-", UnaryNegative.class);

		binaryOperators = new Hashtable();
		binaryOperators.put("+", Add.class);
		binaryOperators.put("-", Subtract.class);
		binaryOperators.put("*", Multiply.class);
		binaryOperators.put("/", Divide.class);
		binaryOperators.put("%", Remainder.class);
		binaryOperators.put("^", Exponent.class);
		binaryOperators.put("&", And.class);
		binaryOperators.put("|", Or.class);
		binaryOperators.put("==", Equal.class);
		binaryOperators.put("!=", NotEqual.class);
		binaryOperators.put("<", LessThan.class);
		binaryOperators.put(">", GreaterThan.class);
		binaryOperators.put("<=", LessThanOrEqual.class);
		binaryOperators.put(">=", GreaterThanOrEqual.class);

		functions = new Hashtable();
		functions.put("if", If.class);
		functions.put("elseif", ElseIf.class);
		functions.put("else", Else.class);
		functions.put("while", While.class);
		functions.put("sample", Sample.class);

		functions.put("drop", Drop.class);
		functions.put("standardize", Standardize.class);
		functions.put("unitize", Unitize.class);
		functions.put("rank", Rank.class);

		functions.put("record", Record.class);
		functions.put("delete", Delete.class);
		functions.put("sample", Sample.class);

		functions.put("cut", Cut.class);
		functions.put("code", Code.class);
		functions.put("lag", Lag.class);
		functions.put("date", DateFunction.class);
		functions.put("time", DateFunction.class);
		functions.put("abs", AbsoluteValue.class);
		functions.put("sign", Sign.class);
		functions.put("sqrt", SquareRoot.class);
		functions.put("exp", ExponentialFunction.class);
		functions.put("log", Log.class);
		functions.put("log2", Log2.class);
		functions.put("log10", Log10.class);
		functions.put("floor", Floor.class);
		functions.put("ceil", Ceiling.class);
		functions.put("int", IntegerPart.class);
		functions.put("round", Round.class);
		functions.put("mod", Modulo.class);
		functions.put("sin", Sine.class);
		functions.put("cos", Cosine.class);
		functions.put("tan", Tangent.class);
		functions.put("asin", ArcSine.class);
		functions.put("acos", ArcCosine.class);
		functions.put("atan", ArcTangent.class);
		functions.put("atan2", Atan2.class);
		functions.put("sinh", HyperbolicSine.class);
		functions.put("cosh", HyperbolicCosine.class);
		functions.put("tanh", HyperbolicTangent.class);
		functions.put("atanh", HyperbolicArcTangent.class);
		functions.put("lgamma", LogGamma.class);
		functions.put("gamma", Gamma.class);
		functions.put("pow", Power.class);

		functions.put("array", Array.class);
		functions.put("nonmissing", NotMissing.class);
		functions.put("missing", Missing.class);
		functions.put("sum", Sum.class);
		functions.put("product", Product.class);
		functions.put("mean", Mean.class);
		functions.put("median", Median.class);
		functions.put("sd", StandardDeviation.class);
		functions.put("se", StandardError.class);
		functions.put("variance", Variance.class);
		functions.put("cv", CoefficientOfVariation.class);
		functions.put("min", Minimum.class);
		functions.put("max", Maximum.class);
		functions.put("range", Range.class);

		functions.put("substring", Substring.class);
		functions.put("lowercase", LowerCase.class);
		functions.put("uppercase", UpperCase.class);
		functions.put("length", StringLength.class);
		functions.put("trim", Trim.class);
		functions.put("ltrim", LeftTrim.class);
		functions.put("rtrim", RightTrim.class);
		functions.put("index", IndexOf.class);
		functions.put("concat", Concatenate.class);
		functions.put("numbertostring", NumberToString.class);
		functions.put("stringtonumber", StringToNumber.class);
		functions.put("datetostring", DateToString.class);
		functions.put("stringtodate", StringToDate.class);
		functions.put("replace", Replace.class);
		functions.put("levenshtein", Levenshtein.class);
		functions.put("dameraulevenshtein", DamerauLevenshtein.class);
		functions.put("metaphone", MetaPhone.class);

		functions.put("uniformcdf", UniformCDF.class);
		functions.put("normalcdf", NormalCDF.class);
		functions.put("tcdf", TCDF.class);
		functions.put("fcdf", FCDF.class);
		functions.put("chisquarecdf", ChisquareCDF.class);
		functions.put("gammacdf", GammaCDF.class);
		functions.put("betacdf", BetaCDF.class);
		functions.put("expocdf", ExponentialCDF.class);
		functions.put("logisticcdf", LogisticCDF.class);
		functions.put("weibullcdf", WeibullCDF.class);
		functions.put("binomialcdf", BinomialCDF.class);
		functions.put("poissoncdf", PoissonCDF.class);
		functions.put("uniformdensity", UniformDensity.class);
		functions.put("normaldensity", NormalDensity.class);
		functions.put("tdensity", TDensity.class);
		functions.put("fdensity", FDensity.class);
		functions.put("chisquaredensity", ChisquareDensity.class);
		functions.put("gammadensity", GammaDensity.class);
		functions.put("betadensity", BetaDensity.class);
		functions.put("expodensity", ExponentialDensity.class);
		functions.put("logisticdensity", LogisticDensity.class);
		functions.put("weibulldensity", WeibullDensity.class);
		functions.put("binomialdensity", BinomialDensity.class);
		functions.put("poissondensity", PoissonDensity.class);
		functions.put("uniformcdfinv", UniformInverseCDF.class);
		functions.put("normalcdfinv", NormalInverseCDF.class);
		functions.put("tcdfinv", TInverseCDF.class);
		functions.put("fcdfinv", FInverseCDF.class);
		functions.put("chisquarecdfinv", ChisquareInverseCDF.class);
		functions.put("gammacdfinv", GammaInverseCDF.class);
		functions.put("betacdfinv", BetaInverseCDF.class);
		functions.put("expocdfinv", ExponentialInverseCDF.class);
		functions.put("logisticcdfinv", LogisticInverseCDF.class);
		functions.put("weibullcdfinv", WeibullInverseCDF.class);
		functions.put("binomialcdfinv", BinomialInverseCDF.class);
		functions.put("poissoncdfinv", PoissonInverseCDF.class);
		functions.put("uniformrandom", UniformRandom.class);
		functions.put("normalrandom", NormalRandom.class);
		functions.put("trandom", TRandom.class);
		functions.put("frandom", FRandom.class);
		functions.put("chisquarerandom", ChisquareRandom.class);
		functions.put("gammarandom", GammaRandom.class);
		functions.put("betarandom", BetaRandom.class);
		functions.put("exporandom", ExponentialRandom.class);
		functions.put("logisticrandom", LogisticRandom.class);
		functions.put("weibullrandom", WeibullRandom.class);
		functions.put("binomialrandom", BinomialRandom.class);
		functions.put("poissonrandom", PoissonRandom.class);

		constants = new Hashtable();
		constants.put("TRUE", new Boolean(true));
		constants.put("FALSE", new Boolean(false));
		constants.put("PI", new Double(Math.PI));
		constants.put("E", new Double(Math.E));
		constants.put("?", new Double(Double.NaN));

	}
}
