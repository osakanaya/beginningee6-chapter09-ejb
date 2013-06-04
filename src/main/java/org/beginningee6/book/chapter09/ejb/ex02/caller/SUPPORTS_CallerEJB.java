package org.beginningee6.book.chapter09.ejb.ex02.caller;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.ejb.ex02.callee.MANDATORY_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.NEVER_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.NOT_SUPPORTED_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.REQUIRED_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.REQUIRES_NEW_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.SUPPORTS_CalleeEJB;
import org.beginningee6.book.chapter09.jpa.ex02.Book02;
import org.beginningee6.book.chapter09.jpa.ex02.CD02;

/**
 * トランザクション属性がSUPPORTSのEJBメソッドから、つまり、クライアントが
 * トランザクションを開始していない状態で以下のトランザクション属性を
 * 持つ別のEJBメソッドを実行した時のトランザクションの振る舞いを確認するための
 * サンプル。
 * 
 * ・REQUIRED
 * ・REQUIRES_NEW
 * ・SUPPORTS
 * ・MANDATORY
 * ・NOT_SUPPORTED
 * ・NEVER
 * 
 * 原則として、それぞれのトランザクション属性ごとに、以下の処理パターンをそれぞれの
 * EJBメソッドとして実装している。
 * 
 * １．（１）呼び出し元EJB：呼び出し先EJBのメソッドを呼び出す。
 * 　　（２）呼び出し先EJB：CD02エンティティを永続化する。
 * 　　（３）呼び出し元EJB：Book02エンティティを永続化する。
 * 
 * ２．（１）呼び出し元EJB：呼び出し先EJBのメソッドを呼び出す。
 * 　　（２）呼び出し先EJB：CD02エンティティを永続化する。
 * 　　（３）呼び出し先EJB：トランザクションをロールバックにマークする。
 * 　　（４）呼び出し元EJB：Book02エンティティを永続化する。
 * 
 * ３．（１）呼び出し元EJB：呼び出し先EJBのメソッドを呼び出す。
 * 　　（２）呼び出し先EJB：CD02エンティティを永続化する。
 * 　　（３）呼び出し元EJB：Book02エンティティを永続化する。
 * 　　（４）呼び出し元EJB：トランザクションをロールバックにマークする。
 *
 */
@Stateless
public class SUPPORTS_CallerEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;

	// 呼び出し先のEJBへの参照を注入
	// トランザクション属性がREQUIREDのメソッドが定義されている
	@EJB
	private REQUIRED_CalleeEJB requiredCalleeEJB;

	// 呼び出し先のEJBへの参照を注入
	// トランザクション属性がREQUIRES_NEWのメソッドが定義されている
	@EJB
	private REQUIRES_NEW_CalleeEJB requiresNewCalleeEJB;

	// 呼び出し先のEJBへの参照を注入
	// トランザクション属性がSUPPORTSのメソッドが定義されている
	@EJB
	private SUPPORTS_CalleeEJB supportsCalleeEJB;

	// 呼び出し先のEJBへの参照を注入
	// トランザクション属性がMANDATORYのメソッドが定義されている
	@EJB
	private MANDATORY_CalleeEJB mandatoryCalleeEJB;

	// 呼び出し先のEJBへの参照を注入
	// トランザクション属性がNOT_SUPPORTEDのメソッドが定義されている
	@EJB
	private NOT_SUPPORTED_CalleeEJB notSupportedCalleeEJB;
	
	// 呼び出し先のEJBへの参照を注入
	// トランザクション属性がNEVERのメソッドが定義されている
	@EJB
	private NEVER_CalleeEJB neverCalleeEJB;
	
	@Inject
	private UserTransaction userTransaction;
	
	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：REQUIREDの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （ただし、それぞれのエンティティが対応する別々の
	 * 　トランザクションをコミットした結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void REQUIRED_CalleePersist_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドでトランザクションを開始～コミット）
		requiredCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void REQUIRED_CalleePersistAndRollback_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// 
		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティの
		// 永続化がロールバックされる
		// （呼び出し先のメソッドでトランザクションを開始～ロールバック）
		requiredCalleeEJB.persistThenRollback(cd);
		
		// 前段のEJB実行でトランザクションがロールバックされているが、
		// そもそもこのEJBのメソッド実行ではトランザクションが
		// 開始されていないので、呼び出し先のEJBでのトランザクションの
		// ロールバックは、このEJBのトランザクション状態には
		// 何ら影響を及ぼさない。
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ３．呼び出し元でトランザクションをロールバックする例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void REQUIRED_CalleePersist_SUPPORTS_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドでトランザクションを開始～コミット）
		requiredCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～ロールバックを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.rollback();
	}
	
	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：REQUIRES_NEWの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （ただし、それぞれのエンティティが対応する別々の
	 * 　トランザクションをコミットした結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void REQUIRES_NEW_CalleePersist_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドでトランザクションを開始～コミット）
		requiresNewCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void REQUIRES_NEW_CalleePersistAndRollback_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// 
		// メソッド終了時にREQUIRES_NEW_CalleeEJBによるCD02エンティティの
		// 永続化がロールバックされる
		// （呼び出し先のメソッドでトランザクションを開始～ロールバック）
		requiresNewCalleeEJB.persistThenRollback(cd);
		
		// 前段のEJB実行でトランザクションがロールバックされているが、
		// そもそもこのEJBのメソッド実行ではトランザクションが
		// 開始されていないので、呼び出し先のEJBでのトランザクションの
		// ロールバックは、このEJBのトランザクション状態には
		// 何ら影響を及ぼさない。
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ３．呼び出し元でトランザクションをロールバックする例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void REQUIRES_NEW_CalleePersist_SUPPORTS_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドでトランザクションを開始～コミット）
		requiresNewCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～ロールバックを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.rollback();
	}

	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：SUPPORTSの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （ただし、それぞれのエンティティが対応する別々の
	 * 　トランザクションをコミットした結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void SUPPORTS_CalleePersist_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではトランザクションは開始されない）
		supportsCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * ※厳密には、呼び出し先はSUPPORTEDのため、トランザクション無しでの
	 * 　処理が行われるため「呼び出し先でトランザクションをロールバックに
	 * 　マークする」という概念が存在しない。
	 * 　ここでは、コンテナ管理トランザクションにより制御されるトランザクションに
	 * 　おいてトランザクションをロールバックする効果を一般的に持つ非チェック例外を
	 * 　スローしてみて、結果がどうなるかを検証しようとしている。
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void SUPPORTS_CalleePersistAndRollback_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// メソッド終了時にSUPPORTS_CalleeEJBによるCD02エンティティの
		// が永続化がロールバックされる。
		// （呼び出し先のメソッドではトランザクションは開始されない。
		// 仮に永続化を自力でコミットしたとして、このような状況で
		// 非チェック例外をスローしてもSUPPORTS_CalleeEJB内での
		// データの永続化はロールバックされず、コミットされたままとなる）
		try {
			supportsCalleeEJB.persistThenRollback(cd);
		} catch (RuntimeException e) {
		}
		
		// 前段のEJB実行で非チェック例外がスローされているが、
		// そもそもこのメソッド実行ではトランザクションが
		// 開始されていないので、スローされた例外は
		// トランザクションの状態に何ら影響を及ぼさない。
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ３．呼び出し元でトランザクションをロールバックする例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void SUPPORTS_CalleePersist_SUPPORTS_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではトランザクションは開始されない）
		supportsCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～ロールバックを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.rollback();

	}

	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：MANDATORYの確認
	 * ==========================================================================
	 */

	/**
	 * １．トランザクションを開始していない呼び出し先からトランザクション属性が
	 * 　　MANDATORYのメソッドを呼び出した例
	 * 
	 * 【結果】
	 * 例外がスローされる。
	 * Book02エンティティもCD02エンティティも永続化されない。
	 * 
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void MANDATORY_CalleePersist_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではメソッド開始時に呼び出し側でトランザクションが
		// 開始されていることを期待する）
		mandatoryCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：NOT_SUPPORTEDの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （ただし、それぞれのエンティティが対応する別々の
	 * 　トランザクションをコミットした結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void NOT_SUPPORTED_CalleePersist_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではトランザクションは開始されない）
		notSupportedCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * ※厳密には、呼び出し先はNOT_SUPPORTEDのため、トランザクション無しでの
	 * 　処理が行われるため「呼び出し先でトランザクションをロールバックに
	 * 　マークする」という概念が存在しない。
	 * 　ここでは、コンテナ管理トランザクションにより制御されるトランザクションに
	 * 　おいてトランザクションをロールバックする効果を一般的に持つ非チェック例外を
	 * 　スローしてみて、結果がどうなるかを検証しようとしている。
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void NOT_SUPPORTED_CalleePersistAndRollback_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// メソッド終了時にNOT_SUPPORTED_CalleeEJBによるCD02エンティティの
		// が永続化がロールバックされる。
		// （呼び出し先のメソッドではトランザクションは開始されない。
		// 仮に永続化を自力でコミットしたとして、このような状況で
		// 非チェック例外をスローしてもNOT_SUPPORTED_CalleeEJB内での
		// データの永続化はロールバックされず、コミットされたままとなる）
		try {
			notSupportedCalleeEJB.persistThenRollback(cd);
		} catch (RuntimeException e) {
		}
		
		// 前段のEJB実行で非チェック例外がスローされているが、
		// そもそもこのメソッド実行ではトランザクションが
		// 開始されていないので、スローされた例外は
		// トランザクションの状態に何ら影響を及ぼさない。
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ３．呼び出し元でトランザクションをロールバックする例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void NOT_SUPPORTED_CalleePersist_SUPPORTS_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではトランザクションは開始されない）
		notSupportedCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～ロールバックを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.rollback();

	}

	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：NEVERの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （ただし、それぞれのエンティティが対応する別々の
	 * 　トランザクションをコミットした結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void NEVER_CalleePersist_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではトランザクションは開始されない）
		neverCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * ※厳密には、呼び出し先はNEVERのため、トランザクション無しでの
	 * 　処理が行われるため「呼び出し先でトランザクションをロールバックに
	 * 　マークする」という概念が存在しない。
	 * 　ここでは、コンテナ管理トランザクションにより制御されるトランザクションに
	 * 　おいてトランザクションをロールバックする効果を一般的に持つ非チェック例外を
	 * 　スローしてみて、結果がどうなるかを検証しようとしている。
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void NEVER_CalleePersistAndRollback_SUPPORTS_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// メソッド終了時にNEVER_CalleeEJBによるCD02エンティティの
		// が永続化がロールバックされる。
		// （呼び出し先のメソッドではトランザクションは開始されない。
		// 仮に永続化を自力でコミットしたとして、このような状況で
		// 非チェック例外をスローしてもNEVER_CalleeEJB内での
		// データの永続化はロールバックされず、コミットされたままとなる）
		try {
			neverCalleeEJB.persistThenRollback(cd);
		} catch (RuntimeException e) {
		}
		
		// 前段のEJB実行で非チェック例外がスローされているが、
		// そもそもこのメソッド実行ではトランザクションが
		// 開始されていないので、スローされた例外は
		// トランザクションの状態に何ら影響を及ぼさない。
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～コミットを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.commit();
	}

	/**
	 * ３．呼び出し元でトランザクションをロールバックする例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void NEVER_CalleePersist_SUPPORTS_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// SUPPORTSであるため、このメソッドが開始された時点では
		// トランザクションが開始されていない

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではトランザクションは開始されない）
		neverCalleeEJB.persist(cd);
		
		// トランザクションが開始されていないため、
		// エンティティを永続化するには、自分でトランザクションの
		// 開始～ロールバックを行う必要がある。
		userTransaction.begin();
		em.joinTransaction();
		em.persist(book);
		userTransaction.rollback();

	}

}
