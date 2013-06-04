package org.beginningee6.book.chapter09.ejb.ex04.exception;

import javax.ejb.ApplicationException;

/**
 * トランザクションをロールバックにマークする効力を持つチェック例外の例。
 */
@ApplicationException(rollback = true)
public class CheckedExceptionWithApplicationAnnotationRollbackTrue extends Exception {

	private static final long serialVersionUID = 1L;

	public CheckedExceptionWithApplicationAnnotationRollbackTrue() {
		super();
	}

	public CheckedExceptionWithApplicationAnnotationRollbackTrue(String message, Throwable cause) {
		super(message, cause);
	}

	public CheckedExceptionWithApplicationAnnotationRollbackTrue(String message) {
		super(message);
	}

	public CheckedExceptionWithApplicationAnnotationRollbackTrue(Throwable cause) {
		super(cause);
	}
}
