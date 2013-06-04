package org.beginningee6.book.chapter09.ejb.ex01;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.beginningee6.book.chapter09.jpa.ex01.Item01;

/**
 * 
 * コンテナ管理トランザクション（CMT）によりトランザクションを制御する
 * ように実装されたステートレス・セッションBeanのサンプル。
 * 
 * このEJBでは、暗黙的にトランザクションをコミットするように実装した
 * メソッド（addItem）と明示的にトランザクションをロールバックに
 * マークするように実装したメソッド（addItemFail）を実装している。
 * 
 * ＠ResourceアノテーションによりSessionContextを注入した
 * 上で、そのSessionContextに対してsetRollbackOnly()メソッドを
 * 実行することで明示的にトランザクションをロールバックにマーク
 * することができる。
 *
 */
@Stateless
public class InventoryEJB {

	// トランザクションをロールバックにマークするために
	// Session Contextを注入
	@Resource
	private SessionContext ctx;
	
	/**
	 * 永続化されたItem01エンティティの在庫数量をひとつ増やす。
	 * 
	 * @param item 永続化されたItem01エンティティ
	 */
	// ＠TransactionAttributeアノテーションを付与しない場合、
	// デフォルトのREQUIREDが設定される
//	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addItem(Item01 item) {
		item.increaseAvailableStock();
	}
	
	/**
	 * 永続化されたItem01エンティティの在庫数量をひとつ増やす。
	 * 
	 * ただし、プログラム中でトランザクションをロールバック
	 * にマークするステートメントを明示的に追加している。
	 * 
	 * @param item 永続化されたItem01エンティティ
	 */
	// ＠TransactionAttributeアノテーションを付与しない場合、
	// デフォルトのREQUIREDが設定される
//	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addItemFail(Item01 item) {
		item.increaseAvailableStock();
		
		// トランザクションをロールバックにマークする
		ctx.setRollbackOnly();
	}
}
