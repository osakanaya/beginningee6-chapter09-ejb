package org.beginningee6.book.chapter09.ejb.ex03;

import javax.ejb.Stateless;

import org.beginningee6.book.chapter09.jpa.ex01.Item01;

/**
 * 在庫数量の管理を行うEJB
 */
@Stateless
public class InventoryEJB {

	/**
	 * Item01エンティティの在庫数量をひとつ増やす
	 * @param item 在庫数量を増やすItem01エンティティ
	 */
	public void addItem(Item01 item) {
		item.increaseAvailableStock();
	}

	/**
	 * 在庫引き当て後のItem01エンティティの在庫数量をチェックする
	 * 
	 * @param item 在庫引き当て後のItem01エンティティ
	 * @throws InventoryLevelTooLowException 在庫引き当て後の在庫数量が０の場合
	 */
	public void checkInventoryLevel(Item01 item) throws InventoryLevelTooLowException {
		if (item.getAvailableInStock() == 0) {
			// トランザクションをロールバックさせるチェック例外をスローする
			// このEJBメソッドがトランザクションを開始している別のEJBから
			// 呼び出されている場合は、そのトランザクションがロールバックに
			// マークされる
			throw new InventoryLevelTooLowException();
		}
	}
}
