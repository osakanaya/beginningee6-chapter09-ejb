package org.beginningee6.book.chapter09.ejb.ex02.callee;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.beginningee6.book.chapter09.jpa.ex02.CD02;

/**
 * トランザクション属性がREQUIRES_NEWに設定されたメソッドを実行した時における
 * トランザクションの挙動を確認するためのサンプル。
 * 
 * REQUIRED_CallerEJBクラスのトランザクション属性がREQUIREDのメソッドから
 * トランザクションを開始した状態で、
 * もしくは、
 * SUPPORTS_CallerEJBクラスのトランザクション属性がSUPPORTSのメソッドから
 * トランザクションが開始されていない状態で呼び出されることを想定している。
 * 
 * 【REQUIRES_NEWの特徴】
 * ・メソッドの呼び出し元がトランザクションを開始している場合は、
 * 　その呼び出し元のトランザクションが保留され、このトランザクションとは
 * 　別のトランザクションが自動的に開始される
 * ・メソッドの呼び出し元がトランザクションを開始していない場合は、
 * 　メソッド開始時にトランザクションが自動的に開始される
 * ・メソッド終了時、メソッド呼び出し時に開始されたトランザクションが自動的に
 * 　コミットまたはロールバックされる
 * ・メソッドの呼び出し元がトランザクションを開始している場合は、
 * 　保留されていたトランザクションが再開される。なお、呼び出されたREQUIRES_NEW
 * 　のメソッドでトランザクションがコミットされてもロールバックされても、
 * 　再開されたトランザクションの状態には何ら影響を与えない。
 * 　（つまり、トランザクション実行中のままとなる。）
 * 
 * 基本的にはCD02エンティティの永続化を処理する例となっているが、
 * 永続化して呼び出し元にそのまま返すメソッドと永続化した後トランザクションを
 * ロールバックにマークするメソッドの2つを用意している。
 */
@Stateless
public class REQUIRES_NEW_CalleeEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;

	// トランザクションをロールバックにマークする目的で
	// SessionContextを注入
	@Resource
	private SessionContext ctx;
	
	/**
	 * CD02エンティティを永続化する。
	 * （トランザクション属性：REQUIRES_NEW）
	 * 
	 * @param cd 永続化するCD02エンティティ
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void persist(CD02 cd) {
		em.persist(cd);
	}
	
	/**
	 * CD02エンティティを永続化する。ただし、永続化の後、トランザクションを
	 * ロールバックにマークする。
	 * （トランザクション属性：REQUIRES_NEW）
	 * 
	 * @param cd 永続化するCD02エンティティ
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void persistThenRollback(CD02 cd) {
		em.persist(cd);

		// トランザクションをロールバックにマークする
		ctx.setRollbackOnly();
	}
}
