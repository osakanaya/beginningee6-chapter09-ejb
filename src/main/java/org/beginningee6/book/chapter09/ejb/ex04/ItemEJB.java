package org.beginningee6.book.chapter09.ejb.ex04;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithApplicationAnnotationRollbackFalse;
import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithApplicationAnnotationRollbackTrue;
import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithoutAnnotation;
import org.beginningee6.book.chapter09.ejb.ex04.exception.UncheckedExceptionWithApplicationAnnotationRollbackFalse;
import org.beginningee6.book.chapter09.jpa.ex01.Item01;

/**
 * チェック例外および非チェック例外をスローしたときのトランザクションの
 * ロールバックを確認するためのサンプル。
 * 
 * InventoryEJBでは、以下の6種類の例外をスローするメソッドがそれぞれの
 * 例外ごとに用意されている。
 * 
 * ・＠ApplicationExceptionアノテーションが
 * 　付与されないチェック例外
 * ・rollbackオプションがtrueの＠ApplicationExceptionアノテーションが
 * 　付与されたチェック例外
 * ・rollbackオプションがfalseの＠ApplicationExceptionアノテーションが
 * 　付与されたチェック例外
 * 
 * ・＠ApplicationExceptionアノテーションが
 * 　付与されない非チェック例外
 * ・rollbackオプションがtrueの＠ApplicationExceptionアノテーションが
 * 　付与された非チェック例外
 * ・rollbackオプションがfalseの＠ApplicationExceptionアノテーションが
 * 　付与された非チェック例外
 */
@Stateless
public class ItemEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;

	@EJB
	private InventoryEJB inventoryEJB;

	/**
	 * Item01トランザクションを永続化する。
	 * 
	 * ＠ApplicationExceptionアノテーションが付与されないチェック例外が
	 * スローされるため、トランザクションを続行することができる。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 * @throws CheckedExceptionWithoutAnnotation
	 */
	public Item01 ejbMethodThrowsCheckedExceptionWithoutAnnotation(Item01 item)
			throws CheckedExceptionWithoutAnnotation {

		try {
			inventoryEJB.throwCheckedExceptionWithoutAnnotation();
		} catch (CheckedExceptionWithoutAnnotation e) {
			// チェック例外なので、トランザクションはアクティブなままになる
		}

		// トランザクションはアクティブなままなので、エンティティの永続化ができる
		em.persist(item);

		return item;
	}

	/**
	 * Item01トランザクションを永続化する。
	 * 
	 * rollbackオプションがtrueの＠ApplicationExceptionアノテーションが
	 * 付与されたチェック例外がスローされるため、トランザクションが
	 * ロールバックにマークされる。
	 * 
	 * このため、Item01エンティティの永続化はデータベースには反映されない。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 * @throws CheckedExceptionWithApplicationAnnotationRollbackTrue
	 */
	public Item01 ejbMethodThrowsCheckedExceptionWithApplicationAnnotationRollbackTrue(
			Item01 item)
			throws CheckedExceptionWithApplicationAnnotationRollbackTrue {

		em.persist(item);

		// チェック例外だがトランザクションをロールバックにマークする機能を持つ
		// 例外スロー時にItem01エンティティの永続化はロールバックされる
		inventoryEJB
				.throwCheckedExceptionWithApplicationAnnotationRollbackTrue();

		return item;

	}

	/**
	 * Item01トランザクションを永続化する。
	 * 
	 * rollbackオプションがfalseの＠ApplicationExceptionアノテーションが
	 * 付与されたチェック例外がスローされるため、トランザクションを続行することができる。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 * @throws CheckedExceptionWithApplicationAnnotationRollbackFalse
	 */
	public Item01 ejbMethodThrowsCheckedExceptionWithApplicationAnnotationRollbackFalse(
			Item01 item)
			throws CheckedExceptionWithApplicationAnnotationRollbackFalse {

		try {
			inventoryEJB
					.throwCheckedExceptionWithApplicationAnnotationRollbackFalse();
		} catch (CheckedExceptionWithApplicationAnnotationRollbackFalse e) {
			// 明示的にトランザクションをロールバックにマークしないように定義された
			// チェック例外なので、トランザクションはアクティブなままになる
		}

		// トランザクションはアクティブなままなので、エンティティの永続化ができる
		em.persist(item);

		return item;
	}

	/**
	 * Item01トランザクションを永続化する。
	 * 
	 * ＠ApplicationExceptionアノテーションが付与されない非チェック例外が
	 * スローされるため、トランザクションはロールバックにマークされる。
	 * 
	 * このため、Item01エンティティの永続化はデータベースには反映されない。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 */
	public Item01 ejbMethodThrowsUncheckedExceptionWithoutAnnotation(Item01 item) {

		em.persist(item);

		// 通常の非チェック例外はスローされるとトランザクションをロールバックにマークする機能を持つ
		// 例外スロー時にItem01エンティティの永続化はロールバックされる
		inventoryEJB.throwUncheckedExceptionWithoutAnnotation();

		return item;
	}

	/**
	 * Item01トランザクションを永続化する。
	 * 
	 * rollbackオプションがtrueの＠ApplicationExceptionアノテーションが
	 * 付与された非チェック例外がスローされるため、トランザクションが
	 * ロールバックにマークされる。
	 * 
	 * このため、Item01エンティティの永続化はデータベースには反映されない。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 */
	public Item01 ejbMethodThrowsUncheckedExceptionWithApplicationAnnotationRollbackTrue(Item01 item) {

		em.persist(item);

		// 明示的にトランザクションをロールバックにマークするように定義された
		// 非チェック例外は通常の非チェック例外と同様に
		// スロー時にItem01エンティティの永続化がロールバックされる
		inventoryEJB
				.throwUncheckedExceptionWithApplicationAnnotationRollbackTrue();

		return item;

	}

	/**
	 * Item01トランザクションを永続化する。
	 * 
	 * rollbackオプションがfalseの＠ApplicationExceptionアノテーションが
	 * 付与された非チェック例外がスローされるため、トランザクションを続行することができる。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 */
	public Item01 ejbMethodThrowsUncheckedExceptionWithApplicationAnnotationRollbackFalse(Item01 item) {

		try {
			inventoryEJB
					.throwUncheckedExceptionWithApplicationAnnotationRollbackFalse();
		} catch (UncheckedExceptionWithApplicationAnnotationRollbackFalse e) {
			// 明示的にトランザクションをロールバックにマークしないように定義された
			// 非チェック例外なので、トランザクションはアクティブなままになる
		}

		// トランザクションはアクティブなままなので、エンティティの永続化ができる
		em.persist(item);

		return item;
	}

}
