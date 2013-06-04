package org.beginningee6.book.chapter09.ejb.ex04.exception;

import javax.ejb.ApplicationException;

/**
 * トランザクションをロールバックにマークする効力を持たないチェック例外の例。
 * 
 * ＠ApplicationExceptionアノテーションを付与しない、通常のチェック例外の定義と
 * 同様の意味を持つ。
 */
@ApplicationException(rollback = false)
public class CheckedExceptionWithApplicationAnnotationRollbackFalse extends Exception {

	private static final long serialVersionUID = 1L;

	public CheckedExceptionWithApplicationAnnotationRollbackFalse() {
		super();
	}

	public CheckedExceptionWithApplicationAnnotationRollbackFalse(String message, Throwable cause) {
		super(message, cause);
	}

	public CheckedExceptionWithApplicationAnnotationRollbackFalse(String message) {
		super(message);
	}

	public CheckedExceptionWithApplicationAnnotationRollbackFalse(Throwable cause) {
		super(cause);
	}
}
