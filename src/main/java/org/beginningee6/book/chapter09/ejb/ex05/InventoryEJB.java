package org.beginningee6.book.chapter09.ejb.ex05;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.jpa.ex01.Item01;
import org.beginningee6.book.chapter09.jpa.ex01.StockAvailabilityException;

/**
 * 在庫数量の管理を行うEJB
 * 
 * このEJBもBean管理トランザクション（BMT）によるトランザクション制御を
 * 前提とするように実装されている。
 */
@Stateless
// Bean管理トランザクションによるトランザクション制御を指定
@TransactionManagement(TransactionManagementType.BEAN)
public class InventoryEJB {

	// トランザクションを制御するためのUserTransactionを
	// 注入する
	@Resource
	private UserTransaction userTransaction;
	
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
	public void checkInventoryLevel(Item01 item) throws InventoryLevelTooLowException, StockAvailabilityException, IllegalStateException, SystemException {
		if (item.getAvailableInStock() == 0) {
			// 在庫引き当て後のItem01エンティティの在庫数量が０の場合、
			// トランザクションをロールバックにマークし、チェック例外を
			// スローする
			userTransaction.setRollbackOnly();

			throw new InventoryLevelTooLowException();
		}
	}
}
