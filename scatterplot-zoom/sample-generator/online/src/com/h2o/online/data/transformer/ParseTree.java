/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

/**
 * This class contains a parse tree.<br>
 *
 * @author
 * Design:  Leland Wilkinson<BR>
 * Code:   Leland Wilkinson
 *
 *@version 1.0
 */

public class ParseTree {

	ParseTreeNode[] nodes;

	ParseTree(ParseTreeNode[] nodes) {
		this.nodes = nodes;
	}
}
