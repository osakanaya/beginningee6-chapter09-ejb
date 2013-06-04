package org.beginningee6.book.chapter09.ejb.ex02.caller;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.beginningee6.book.chapter09.ejb.ex02.callee.MANDATORY_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.NEVER_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.NOT_SUPPORTED_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.REQUIRED_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.REQUIRES_NEW_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.callee.SUPPORTS_CalleeEJB;
import org.beginningee6.book.chapter09.jpa.ex02.Book02;
import org.beginningee6.book.chapter09.jpa.ex02.CD02;

/**
 * トランザクション属性がREQUIREDのEJBメソッドから、つまり、クライアントが
 * トランザクションを開始している状態で以下のトランザクション属性を
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
public class REQUIRED_CallerEJB {
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
	
	@Resource
	private SessionContext ctx;
	
	/**
	 * ==========================================================================
	 * 呼び出し元：REQUIRED→呼び出し先：REQUIREDの確認
	 * ==========================================================================
	 */
	
	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （単一トランザクションのコミットの結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void REQUIRED_CalleePersist_REQUIRED_CallerPersist(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始
		
		// 同じトランザクションコンテキストでCD02エンティティを永続化
		requiredCalleeEJB.persist(cd);
		
		// 同じトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がコミットされる
	}

	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * 呼び出し元でのBook02エンティティの永続化で例外がスローされる。
	 * Book02エンティティとCD02エンティティの両方とも永続化されない。
	 * （単一トランザクションのロールバックの結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void REQUIRED_CalleePersistAndRollback_REQUIRED_CallerPersist(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		requiredCalleeEJB.persistThenRollback(cd);
		
		// 前段のEJB実行でトランザクションがロールバックにマークされている。
		// ロールバックにマークされている状態でBook02エンティティを
		// 永続化することはできないため、例外がスローされる。
		em.persist(book);
	}

	/**
	 * ３．呼び出し元でトランザクションがロールバックにマークされる例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方とも永続化されない。
	 * （単一トランザクションのロールバックの結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void REQUIRED_CalleePersist_REQUIRED_CallerPersistAndRollback(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		requiredCalleeEJB.persist(cd);
		
		// 同じトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// トランザクションをロールバックにマークすることによって
		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がロールバックされる
		ctx.setRollbackOnly();
	}
	
	/**
	 * ==========================================================================
	 * 呼び出し元：REQUIRED→呼び出し先：REQUIRES_NEWの確認
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void REQUIRES_NEW_CalleePersist_REQUIRED_CallerPersist(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティの
		// 永続化がコミットされる
		// （メソッド開始時のトランザクションは一時保留となり、
		// 別のトランザクションが開始～コミットされる）
		requiresNewCalleeEJB.persist(cd);
		
		// メソッド開始時のトランザクションが再開される

		// メソッド開始時のトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);

		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がコミットされる
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void REQUIRES_NEW_CalleePersistAndRollback_REQUIRED_CallerPersist(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティの
		// 永続化がロールバックされる
		// （メソッド開始時のトランザクションは一時保留となり、
		// 別のトランザクションが開始～ロールバックされる）
		requiresNewCalleeEJB.persistThenRollback(cd);
		
		// メソッド開始時のトランザクションが再開される

		// 前段のEJB実行でロールバックされていても、
		// このEJBのトランザクションの状態には影響されない。
		// このため、Bookエンティティは永続化できる。
		em.persist(book);

		// メソッド終了時にこのEJBによるBook02エンティティ
		// の永続化がコミットされる
	}

	/**
	 * ３．呼び出し元でトランザクションがロールバックにマークされる例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void REQUIRES_NEW_CalleePersist_REQUIRED_CallerPersistAndRollback(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティの
		// 永続化がコミットされる
		// （メソッド開始時のトランザクションは一時保留となり、
		// 別のトランザクションが開始～コミットされる）
		requiresNewCalleeEJB.persist(cd);
		
		// メソッド開始時のトランザクションが再開される

		em.persist(book);
		
		// トランザクションをロールバックにマークすることによって
		// メソッド終了時にこのEJBによるBook02エンティティの
		// 永続化がロールバックされる
		ctx.setRollbackOnly();
	}

	/**
	 * ==========================================================================
	 * 呼び出し元：REQUIRED→呼び出し先：SUPPORTSの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （単一トランザクションのコミット結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void SUPPORTS_CalleePersist_REQUIRED_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		supportsCalleeEJB.persist(cd);
		
		// 同じトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がコミットされる
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * ※厳密には、呼び出し先では、SessionContext.setRollbackOnly()による
	 * 　ロールバックへのマークを行っておらず、非チェック例外をスロー
	 * 　することによるロールバックへのマークを行っている。
	 * 
	 * 【結果】
	 * 呼び出し元でのBook02エンティティの永続化で例外がスローされる。
	 * Book02エンティティとCD02エンティティの両方とも永続化されない。
	 * （単一トランザクションのロールバック結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void SUPPORTS_CalleePersistAndRollback_REQUIRED_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		try {
			supportsCalleeEJB.persistThenRollback(cd);
		} catch (RuntimeException e) {}

		// 前段のEJB実行がRuntimeExceptionをスローすることにより、
		// トランザクションがロールバックにマークされている。
		// ロールバックにマークされている状態でBookエンティティを
		// 永続化することはできないため、例外がスローされる。		
		em.persist(book);
	}

	/**
	 * ３．呼び出し元でトランザクションがロールバックにマークされる例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方とも永続化されない。
	 * （単一トランザクションのロールバック結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void SUPPORTS_CalleePersist_REQUIRED_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		supportsCalleeEJB.persist(cd);
		
		// 同じトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// トランザクションをロールバックにマークすることによって
		// メソッド終了時にREQUIRED_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がロールバックされる
		ctx.setRollbackOnly();
	}
	
	/**
	 * ==========================================================================
	 * 呼び出し元：REQUIRED→呼び出し先：MANDATORYの確認
	 * ==========================================================================
	 */

	/**
	 * １．呼び出し先、呼び出し元ともに処理を正常に完了する例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方が永続化される
	 * （単一トランザクションのコミット結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void MANDATORY_CalleePersist_REQUIRED_CallerPersist(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始
		
		// 同じトランザクションコンテキストでCD02エンティティを永続化
		mandatoryCalleeEJB.persist(cd);
		
		// 同じトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// メソッド終了時にMANDATORY_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がコミットされる
	}
	
	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * 呼び出し元でのBook02エンティティの永続化で例外がスローされる。
	 * Book02エンティティとCD02エンティティの両方とも永続化されない。
	 * （単一トランザクションのロールバック結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void MANDATORY_CalleePersistAndRollback_REQUIRED_CallerPersist(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		mandatoryCalleeEJB.persistThenRollback(cd);
		
		// 前段のEJB実行でトランザクションがロールバックにマークされている。
		// ロールバックにマークされている状態でBook02エンティティを
		// 永続化することはできないため、例外がスローされる。
		em.persist(book);
	}

	/**
	 * ３．呼び出し元でトランザクションがロールバックにマークされる例
	 * 
	 * 【結果】
	 * Book02エンティティとCD02エンティティの両方とも永続化されない。
	 * （単一トランザクションのロールバック結果として）
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void MANDATORY_CalleePersist_REQUIRED_CallerPersistAndRollback(Book02 book, CD02 cd) {
		// メソッド開始によりトランザクション開始

		// 同じトランザクションコンテキストでCD02エンティティを永続化
		mandatoryCalleeEJB.persist(cd);
		
		// 同じトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// トランザクションをロールバックにマークすることによって
		// メソッド終了時にMANDATORY_CalleeEJBによるCD02エンティティと
		// このEJBによるBook02エンティティの両方の永続化がロールバックされる
		ctx.setRollbackOnly();
	}

	/**
	 * ==========================================================================
	 * 呼び出し元：REQUIRED→呼び出し先：NOT_SUPPORTEDの確認
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void NOT_SUPPORTED_CalleePersist_REQUIRED_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// メソッド開始によりトランザクション開始

		// メソッド開始時のトランザクションを一時保留にし、
		// トランザクション無しでCD02エンティティを永続化
		notSupportedCalleeEJB.persist(cd);
		
		// メソッド開始時のトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// メソッド終了時にこのEJBによるBook02エンティティの
		// 永続化のみがコミットされる
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void NOT_SUPPORTED_CalleePersistAndRollback_REQUIRED_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// メソッド開始によりトランザクション開始

		// メソッド開始時のトランザクションを一時保留にし、
		// トランザクション無しでCD02エンティティを永続化
		// （呼び出し先のEJBで明示的に永続化をロールバック）
		try {
			notSupportedCalleeEJB.persistThenRollback(cd);
		} catch (RuntimeException e) {}

		// 前段のEJB実行がRuntimeExceptionをスローしても、
		// このEJBのトランザクション状態には影響を与えない。
		// 
		// 従って、メソッド終了時にこのEJBによるBook02エンティティの
		// 永続化のみがコミットされる
		em.persist(book);
	}

	/**
	 * ３．呼び出し元でトランザクションがロールバックにマークされる例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void NOT_SUPPORTED_CalleePersist_REQUIRED_CallerPersistAndRollback(Book02 book, CD02 cd) throws Exception {
		// メソッド開始によりトランザクション開始

		// メソッド開始時のトランザクションを一時保留にし、
		// トランザクション無しでCD02エンティティを永続化
		notSupportedCalleeEJB.persist(cd);
		
		// メソッド開始時のトランザクションコンテキストでBook02エンティティを永続化
		em.persist(book);
		
		// トランザクションをロールバックにマークすることによって
		// メソッド終了時にこのEJBによるBook02エンティティの永続化のみが
		// ロールバックされる。NOT_SUPPORTED_CalleeEJBの永続化は
		// ロールバックされない。
		ctx.setRollbackOnly();
	}

	/**
	 * ==========================================================================
	 * 呼び出し元：REQUIRED→呼び出し先：NEVERの確認
	 * ==========================================================================
	 */

	/**
	 * １．トランザクションを開始した呼び出し先からトランザクション属性がNEVERの
	 * 　　メソッドを呼び出した例
	 * 
	 * 【結果】
	 * 例外がスローされる。
	 * Book02エンティティもCD02エンティティも永続化されない。
	 * 
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void NEVER_CalleePersist_REQUIRED_CallerPersist(Book02 book, CD02 cd) throws Exception {
		// REQUIREDであるため、このメソッドが開始された時点で
		// トランザクションが開始される

		// CD02エンティティを永続化
		// （呼び出し先のメソッドではメソッド開始時に呼び出し側でトランザクションが
		// 開始されていないことを期待する）
		neverCalleeEJB.persist(cd);
		
		// トランザクションが開始されている状態でneverCalleeEJB1を実行しているため、
		// 例外がスローされるはず。
		// 従って、Book02エンティティの永続化は処理されないはずである。
		em.persist(book);
	}
}
