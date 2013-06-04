package org.beginningee6.book.chapter09.ejb.ex04.exception;

import javax.ejb.ApplicationException;

/**
 * トランザクションをロールバックにマークする効力を持つ非チェック例外の例。
 * 
 * ＠ApplicationExceptionアノテーションを付与しない、通常の非チェック例外の定義と
 * 同様の意味を持つ。
 */
@ApplicationException(rollback = true)
public class UncheckedExceptionWithApplicationAnnotationRollbackTrue extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UncheckedExceptionWithApplicationAnnotationRollbackTrue() {
		super();
	}

	public UncheckedExceptionWithApplicationAnnotationRollbackTrue(String message, Throwable cause) {
		super(message, cause);
	}

	public UncheckedExceptionWithApplicationAnnotationRollbackTrue(String message) {
		super(message);
	}

	public UncheckedExceptionWithApplicationAnnotationRollbackTrue(Throwable cause) {
		super(cause);
	}
}
