package org.beginningee6.book.chapter09.ejb.ex04.exception;

/**
 * トランザクションをロールバックにマークする効力を持たないチェック例外の例。
 * 
 * rollbackオプションにfalseを設定した＠ApplicationExceptionアノテーションを
 * 付与した場合と同じ意味を持つ。
 */
public class CheckedExceptionWithoutAnnotation extends Exception {

	private static final long serialVersionUID = 1L;

	public CheckedExceptionWithoutAnnotation() {
		super();
	}

	public CheckedExceptionWithoutAnnotation(String message, Throwable cause) {
		super(message, cause);
	}

	public CheckedExceptionWithoutAnnotation(String message) {
		super(message);
	}

	public CheckedExceptionWithoutAnnotation(Throwable cause) {
		super(cause);
	}
}
