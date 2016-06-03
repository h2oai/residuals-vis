/**
 * Copyright (c) 2013 Leland Wilkinson
 */
package com.h2o.online.data.transformer;

/**
 * Executer exceptions.
 */
public class ExecutorException extends Exception {
	public ExecutorException() {
		super();
	}

	/**
	 * @param message Execution error message.
	 */
	public ExecutorException(String message) {
		super(message);
		System.out.println(message);
	}
}
