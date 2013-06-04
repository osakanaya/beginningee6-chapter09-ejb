package org.beginningee6.book.chapter09.ejb.ex02.callee;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.jpa.ex02.CD02;

/**
 * トランザクション属性がNOT_SUPPORTEDに設定されたメソッドを実行した時における
 * トランザクションの挙動を確認するためのサンプル。
 * 
 * REQUIRED_CallerEJBクラスのトランザクション属性がREQUIREDのメソッドから
 * トランザクションを開始した状態で、
 * もしくは、
 * SUPPORTS_CallerEJBクラスのトランザクション属性がSUPPORTSのメソッドから
 * トランザクションが開始されていない状態で呼び出されることを想定している。
 * 
 * 【NOT_SUPPORTEDの特徴】
 * ・メソッドの呼び出し元でトランザクションを開始している場合は、
 * 　その呼び出し元のトランザクションが保留され、
 * 　トランザクションが無い状態で処理が進められる
 * ・メソッドの呼び出し元でトランザクションを開始していない場合は、
 * 　そのままトランザクションが無い状態で処理が進められる
 * ・メソッドの処理が終了すると、メソッドの呼び出し元で保留されていた
 * 　トランザクションが再開される
 * 
 * 基本的にはCD02エンティティの永続化を処理する例となっているが、
 * 永続化して呼び出し元にそのまま返すメソッドと永続化した後、コンテナ管理
 * トランザクションにより制御されるトランザクションにおいてトランザクションを
 * ロールバックする効果を持つ非チェック例外をスローするメソッドの2つを用意している。
 */
@Stateless
public class NOT_SUPPORTED_CalleeEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;

	// 自力でトランザクションの開始とロールバック／コミットを
	// 行うため、UserTransactionを注入
	@Inject
	private UserTransaction userTransaction;
	
	/**
	 * CD02エンティティを永続化する。
	 * （トランザクション属性：NOT_SUPPORTED）
	 * 
	 * @param cd 永続化するCD02エンティティ
	 * @throws Exception トランザクションの開始、コミットに失敗した時
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void persist(CD02 cd) throws Exception {
		
		// トランザクションが開始されていない状態のため、
		// 明示的にトランザクションの開始とコミットを行う
		userTransaction.begin();
		em.joinTransaction();
		em.persist(cd);
		userTransaction.commit();
	}
	
	/**
	 * CD02エンティティを永続化する。
	 * （トランザクション属性：NOT_SUPPORTED）
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void persistThenRollback(CD02 cd) throws Exception {
		// トランザクションが開始されていない状態のため、
		// 明示的にトランザクションの開始とロールバックを行う
		userTransaction.begin();
		em.joinTransaction();
		em.persist(cd);
		userTransaction.rollback();

		// NOT_SUPPORTEDのメソッドはトランザクションが開始されないため
		// 「トランザクションをロールバックにマークする」という
		// 概念は無いが、呼び出し元のEJBのトランザクションへの影響を
		// 確認するために、非チェック例外であるRuntimeExceptionをスローする。
		throw new RuntimeException();
	}
}
