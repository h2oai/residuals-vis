/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

/**
 * Parser syntax exceptions.
 */
public class ParserException extends Exception {
	public ParserException() {
		super();
	}

	/**
	 * @param message Syntax error message.
	 */
	public ParserException(String message) {
		super(message);
	}
}
