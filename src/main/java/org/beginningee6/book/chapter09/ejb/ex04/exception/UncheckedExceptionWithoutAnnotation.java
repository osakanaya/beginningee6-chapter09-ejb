package org.beginningee6.book.chapter09.ejb.ex04.exception;

/**
 * トランザクションをロールバックにマークする効力を持つ非チェック例外の例。
 * 
 * rollbackオプションにtrueを設定した＠ApplicationExceptionアノテーションを
 * 付与した場合と同じ意味を持つ。
 */
public class UncheckedExceptionWithoutAnnotation extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UncheckedExceptionWithoutAnnotation() {
		super();
	}

	public UncheckedExceptionWithoutAnnotation(String message, Throwable cause) {
		super(message, cause);
	}

	public UncheckedExceptionWithoutAnnotation(String message) {
		super(message);
	}

	public UncheckedExceptionWithoutAnnotation(Throwable cause) {
		super(cause);
	}
}
