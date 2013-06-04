package org.beginningee6.book.chapter09.ejb.ex03;

import javax.ejb.ApplicationException;

/**
 * トランザクションをロールバックにマークする効力を持つチェック例外
 * のサンプル。
 * 
 * トランザクションをロールバックにマークする効力を持たせるには、
 * rollbackオプションにtrueを設定した＠ApplicationExceptionアノテーション
 * を付与する。
 * 
 * このアノテーションが付与されない、もしくは、rollbackオプションにfalseを
 * 設定した＠ApplicationExceptionアノテーションが付与されたチェック例外には、
 * 従来のEJB仕様と同様に、トランザクションをロールバックにマークする効力は
 * ない。
 *
 */
@ApplicationException(rollback = true)	// トランザクションをロールバックにマークするよう設定
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
