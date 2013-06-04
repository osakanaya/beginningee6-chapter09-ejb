package org.beginningee6.book.chapter09.ejb.ex05;


public class InventoryLevelTooLowException extends Exception {

	private static final long serialVersionUID = 1L;

	public InventoryLevelTooLowException() {
		super();
	}

	public InventoryLevelTooLowException(String message, Throwable cause) {
		super(message, cause);
	}

	public InventoryLevelTooLowException(String message) {
		super(message);
	}

	public InventoryLevelTooLowException(Throwable cause) {
		super(cause);
	}
}
