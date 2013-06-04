package org.beginningee6.book.chapter09.ejb.ex05;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.jpa.ex01.Item01;
import org.beginningee6.book.chapter09.jpa.ex01.StockAvailabilityException;

/**
 * 
 * Bean管理トランザクション（BMT）によりトランザクションを制御する
 * ように実装されたステートレス・セッションBeanのサンプル。
 * 
 * Bean管理トランザクション（BMT）によりトランザクションを制御する
 * ように実装した場合、EJBコンテナはメソッド実行に関して、
 * トランザクションの制御には全く関与しない。
 * 
 * 代わりに、EJBメソッドの中で自分でトランザクションの開始、および、
 * トランザクションのコミット／ロールバックを行うステートメントを
 * 含める必要がある。
 * 
 * Bean管理トランザクションによりトランザクションを制御する場合、
 * 以下の対応が必要になる。
 * 
 * １．EJBのクラス宣言に＠TransactionManagementアノテーションを付与し、
 * 　　トランザクション管理の種類としてTransactionManagementType.BEANを
 * 　　指定する。
 * ２．トランザクションを自力で管理するためのUserTransactionを＠Resource
 * 　　アノテーションにより注入する。
 * ３．EJBメソッドの中でUserTransaction.begin()を実行し、
 * 　　トランザクションを明示的に開始する。
 * ４．データベースなどのトランザクションリソースに更新を加えた後に、
 * 　　UserTransactionのcommit()メソッドまたはrollback()メソッドを
 * 　　実行して明示的にコミットあるいはロールバックする。
 * 
 * 以下のサンプルは、コンテナ管理トランザクション（CMT）による
 * トランザクション制御を前提として実装された
 * org.beginningee6.book.chapter09.ejb.ex04パッケージでのEJBの実装を
 * Bean管理トランザクション（BMT）によるトランザクション制御を前提とするように
 * 実装を変換したものとなっている。
 * 
 */
@Stateless
// Bean管理トランザクションによるトランザクション制御を指定
@TransactionManagement(TransactionManagementType.BEAN)
public class ItemEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;

	// トランザクションを制御するためのUserTransactionを
	// 注入する
	@Resource
	UserTransaction userTransaction;
	
	@EJB
	private InventoryEJB inventoryEJB;
	
	/**
	 * Item01エンティティを永続化する。
	 * 
	 * JPAを使ってItem01エンティティを永続化するだけでなく、
	 * InventoryEJB.addItem()メソッドを実行して永続化した
	 * Item01エンティティの在庫数量をひとつ増やすことも
	 * 行っている。
	 * 
	 * @param item 永続化するItem01エンティティ
	 * @return 永続化されたItem01エンティティ
	 */
    public Item01 createItem(Item01 item)  {
    	try {
    		// トランザクションを開始
	    	userTransaction.begin();
	    	
	    	// Item01エンティティを永続化
	        em.persist(item);
	        // 在庫数量をひとつ増やす
	        inventoryEJB.addItem(item);
	
	        // トランザクションをコミット
	        userTransaction.commit();
    	} catch (Exception e) {
    		try {
    			// 例外がスローされた場合は、トランザクションをロールバック
				userTransaction.rollback();
			} catch (Exception ex) {
				// nullを返しちゃうのがいいのかどうかは別として・・・
				return null;
			}
    	}

    	return item;
    }
    
    /**
     * Item01エンティティの在庫数量を指定された数量だけ増やす。
     * 
     * @param item 在庫数量を増やすItem01エンティティ
     * @param stock 在庫数量の増分
     * @return 在庫数量が増加したItem01エンティティ
     */
    public Item01 addAvailableStock(Item01 item, int stock) throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
    	try {
    		// トランザクションを開始
	    	userTransaction.begin();
	    	
	    	// 在庫数量を指定した数だけ増やす
	    	int currentStock = item.getAvailableInStock();
	    	item.setAvailableInStock(currentStock + stock);
	    	
	    	em.merge(item);
	    	
	    	// トランザクションをコミットする
	    	userTransaction.commit();
    	} catch (Exception e) {
    		try {
    			// 例外がスローされた場合は、トランザクションをロールバック
				userTransaction.rollback();
			} catch (Exception ex) {
				// nullを返しちゃうのがいいのかどうかは別として・・・
				return null;
			}
    	}

    	return item;
    }
    
    /**
     * Item01エンティティの”在庫引き当て処理”を行う。
     * （実際は、在庫数量を－１する）
     * 
     * @param item 在庫を引き当てるItem01エンティティ
     * @return 在庫引き当て後のItem01エンティティ
     * @throws InventoryLevelTooLowException 在庫数量が１のItem01エンティティで処理した場合
     * @throws StockAvailabilityException 在庫数量が０のItem01エンティティで処理した場合
     */
    public Item01 sellOneItem(Item01 item) throws InventoryLevelTooLowException, StockAvailabilityException {
    	Item01 soldItem = null;
    	try {
    		// トランザクションを開始する
    		userTransaction.begin();
    		
    		// 在庫数量を－１する
        	soldItem = em.find(Item01.class, item.getId());
        	soldItem.decreaseAvailableStock();
        	
        	// Item01エンティティの在庫数量をチェックする
        	// 在庫数量が０の場合、InventoryLevelTooLowExceptionが
        	// スローされる。
        	inventoryEJB.checkInventoryLevel(soldItem);

        	// トランザクションをコミットする
        	userTransaction.commit();
    	} catch (Exception e) {
    		try {
    			// InventoryLevelTooLowExceptionや、その他の
    			// トランザクション制御、EJBやJPAの処理に係る
    			// チェック例外がスローされた場合はトランザクションを
    			// 明示的にロールバックする
				userTransaction.rollback();
			} catch (Exception ex) {
				// nullを返しちゃうのがいいのかどうかは別として・・・
				return null;
			}
    	}
    	
    	return soldItem;
    }
}
