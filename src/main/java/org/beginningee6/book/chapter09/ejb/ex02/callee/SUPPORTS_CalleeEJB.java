package org.beginningee6.book.chapter09.ejb.ex02.callee;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.jpa.ex02.CD02;

/**
 * トランザクション属性がSUPPORTSに設定されたメソッドを実行した時における
 * トランザクションの挙動を確認するためのサンプル。
 * 
 * REQUIRED_CallerEJBクラスのトランザクション属性がREQUIREDのメソッドから
 * トランザクションを開始した状態で、
 * もしくは、
 * SUPPORTS_CallerEJBクラスのトランザクション属性がSUPPORTSのメソッドから
 * トランザクションが開始されていない状態で呼び出されることを想定している。
 * 
 * 【SUPPORTSの特徴】
 * ・メソッドの呼び出し元でトランザクションを開始している場合は、
 * 　その呼び出し元のトランザクションで処理が行われる
 * ・メソッドの呼び出し元でトランザクションを開始していない場合は、
 * 　そのままトランザクションが無い状態で処理が進められる
 * 
 * 基本的にはCD02エンティティの永続化を処理する例となっているが、
 * 永続化して呼び出し元にそのまま返すメソッドと永続化した後、コンテナ管理
 * トランザクションにより制御されるトランザクションにおいてトランザクションを
 * ロールバックする効果を持つ非チェック例外をスローするメソッドの2つを用意している。
 */
@Stateless
public class SUPPORTS_CalleeEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;

	// 自力でトランザクションの開始とロールバック／コミットを
	// 行うため、UserTransactionを注入
	@Inject
	private UserTransaction userTransaction;
	
	/**
	 * CD02エンティティを永続化する。
	 * （トランザクション属性：SUPPORTS）
	 * 
	 * @param cd 永続化するCD02エンティティ
	 * @throws Exception トランザクションの開始、コミットに失敗した時
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void persist(CD02 cd) throws Exception {
		// メソッドの呼び出し元でトランザクションを開始していない場合、
		// データの永続化を行うために自分でトランザクションの開始を
		// 行うようにしている
		int txStatusWhenCalled = userTransaction.getStatus();
		if (txStatusWhenCalled == Status.STATUS_NO_TRANSACTION) {
			userTransaction.begin();
			em.joinTransaction();
		}

		em.persist(cd);

		// メソッドの呼び出し元でトランザクションを開始していない場合、
		// データの永続化を行うために自分でトランザクションのコミットを
		// 行うようにしている
		if (txStatusWhenCalled == Status.STATUS_NO_TRANSACTION) {
			userTransaction.commit();
		}
	}
	
	/**
	 * CD02エンティティを永続化する。
	 * （トランザクション属性：SUPPORTS）
	 * 
	 * ただし、CD02エンティティの永続化に際しては、自力でトランザクションの
	 * の開始を行い、永続化した後は明示的にロールバックを行う。
	 * 
	 * また、コンテナ管理トランザクションにより制御されるトランザクションにおいて
	 * トランザクションをロールバックする効果を持つ非チェック例外をスローする。
	 * 
	 * @param cd 永続化するCD02エンティティ
	 * @throws Exception トランザクションの開始、コミットに失敗した時
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void persistThenRollback(CD02 cd) throws Exception {
		// メソッドの呼び出し元でトランザクションを開始していない場合、
		// データの永続化を行うために自分でトランザクションの開始を
		// 行うようにしている
		int txStatusWhenCalled = userTransaction.getStatus();
		if (txStatusWhenCalled == Status.STATUS_NO_TRANSACTION) {
			userTransaction.begin();
			em.joinTransaction();
		}
		em.persist(cd);

		// メソッドの呼び出し元でトランザクションを開始していない場合、
		// データの永続化を行うために自分でトランザクションのロールバックを
		// 行うようにしている
		if (txStatusWhenCalled == Status.STATUS_NO_TRANSACTION) {
			userTransaction.rollback();
		}

		// このメソッドがトランザクションを開始している
		// クライアントから呼び出されている状態で、
		// SessionContext.setRollbackOnly()により
		// トランザクションをロールバックにマークすることは
		// EJB3.1の仕様上認められていないため、
		// 非チェック例外であるRuntimeExceptionをスローことで
		// トランザクションをロールバックにマークされる
		// 状況を作り出す。
		
		// このメソッドがトランザクションを開始していない
		// クライアントから呼び出されている状態では、
		// この例外をスローしても、CD02エンティティの永続化は
		// 自力でロールバックしない限り、ロールバックされることなく、
		// そのままコミットされる
		throw new RuntimeException();
	}
}
