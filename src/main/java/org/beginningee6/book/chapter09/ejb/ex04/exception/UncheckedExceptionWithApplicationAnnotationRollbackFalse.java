package org.beginningee6.book.chapter09.ejb.ex04.exception;

import javax.ejb.ApplicationException;

/**
 * トランザクションをロールバックにマークする効力を持たない非チェック例外の例。
 * 
 * 過去のEJB仕様では、非チェック例外は無条件にトランザクションをロールバックに
 * マークする効力を持つものとして規定されていた。
 * 
 * JavaEE6では、rollbackオプションにfalseを設定した＠ApplicationException
 * アノテーションを付与することで、この効力を打ち消すことができる。
 */
@ApplicationException(rollback = false)
public class UncheckedExceptionWithApplicationAnnotationRollbackFalse extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UncheckedExceptionWithApplicationAnnotationRollbackFalse() {
		super();
	}

	public UncheckedExceptionWithApplicationAnnotationRollbackFalse(String message, Throwable cause) {
		super(message, cause);
	}

	public UncheckedExceptionWithApplicationAnnotationRollbackFalse(String message) {
		super(message);
	}

	public UncheckedExceptionWithApplicationAnnotationRollbackFalse(Throwable cause) {
		super(cause);
	}
}
