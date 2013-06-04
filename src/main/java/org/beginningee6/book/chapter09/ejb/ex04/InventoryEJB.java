package org.beginningee6.book.chapter09.ejb.ex04;

import javax.ejb.Stateless;

import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithApplicationAnnotationRollbackFalse;
import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithApplicationAnnotationRollbackTrue;
import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithoutAnnotation;
import org.beginningee6.book.chapter09.ejb.ex04.exception.UncheckedExceptionWithApplicationAnnotationRollbackFalse;
import org.beginningee6.book.chapter09.ejb.ex04.exception.UncheckedExceptionWithApplicationAnnotationRollbackTrue;
import org.beginningee6.book.chapter09.ejb.ex04.exception.UncheckedExceptionWithoutAnnotation;

@Stateless
public class InventoryEJB {

	/**
	 * ＠ApplicationExceptionアノテーションが
	 * 付与されないチェック例外をスローする。
	 */
	public void throwCheckedExceptionWithoutAnnotation()
			throws CheckedExceptionWithoutAnnotation {

		throw new CheckedExceptionWithoutAnnotation();
	}

	/**
	 * rollbackオプションがtrueの＠ApplicationExceptionアノテーションが
	 * 付与されたチェック例外をスローする。
	 */
	public void throwCheckedExceptionWithApplicationAnnotationRollbackTrue()
			throws CheckedExceptionWithApplicationAnnotationRollbackTrue {

		throw new CheckedExceptionWithApplicationAnnotationRollbackTrue();
	}

	/**
	 * rollbackオプションがfalseの＠ApplicationExceptionアノテーションが
	 * 付与されたチェック例外をスローする。
	 */
	public void throwCheckedExceptionWithApplicationAnnotationRollbackFalse()
			throws CheckedExceptionWithApplicationAnnotationRollbackFalse {

		throw new CheckedExceptionWithApplicationAnnotationRollbackFalse();
	}

	/**
	 * ＠ApplicationExceptionアノテーションが
	 * 付与されない非チェック例外をスローする。
	 */
	public void throwUncheckedExceptionWithoutAnnotation() {
		throw new UncheckedExceptionWithoutAnnotation();
	}

	/**
	 * rollbackオプションがtrueの＠ApplicationExceptionアノテーションが
	 * 付与された非チェック例外をスローする。
	 */
	public void throwUncheckedExceptionWithApplicationAnnotationRollbackTrue() {
		throw new UncheckedExceptionWithApplicationAnnotationRollbackTrue();
	}

	/**
	 * rollbackオプションがfalseの＠ApplicationExceptionアノテーションが
	 * 付与された非チェック例外をスローする。
	 */
	public void throwUncheckedExceptionWithApplicationAnnotationRollbackFalse() {
		throw new UncheckedExceptionWithApplicationAnnotationRollbackFalse();
	}

}
