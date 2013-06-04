package org.beginningee6.book.chapter09.ejb.ex03;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.beginningee6.book.chapter09.jpa.ex01.Item01;
import org.beginningee6.book.chapter09.jpa.ex01.StockAvailabilityException;

/**
 * EJBメソッドの実行において、チェック例外をスローすることで明示的にトランザクションを
 * ロールバックにマークし、メソッド終了時にトランザクションをロールバックする例。
 * 
 * このサンプルでは、sellOneItem()メソッドからチェック例外がスローされるとトランザクション
 * がロールバックにマークされるようになっている。
 * 
 * このメソッドでは、以下の処理を行っている。
 * 
 * １．データベースからItem01エンティティを得る。
 * ２．Item01エンティティの在庫数量をひとつ減らす。（UPDATE文が発行される）
 * ３．InventoryEJBのcheckInventoryLevel()メソッドを呼び出し、在庫数量が
 * 　　０である場合は、チェック例外であるInventoryLvelTooLowExceptionを
 * 　　スローする。
 * 
 * 上記３．において、例外がスローされた場合は、トランザクションがロールバックされ、
 * ２．における在庫数量の更新は取り消される。逆に、例外がスローされない場合は、
 * 在庫数量の更新がコミットされる。
 * 
 * コンテナ管理トランザクションによるトランザクション制御において、EJBのこれまでの
 * 仕様では非チェック例外をスローした場合にのみトランザクションがロールバックに
 * マークされるようになったが、新たにチェック例外でもトランザクションをロールバック
 * にマークすることができるようになった。
 * 
 * なお、その場合、そのチェック例外のクラス宣言に以下のアノテーションを明示的に
 * 付与しておくことが必要になる。
 * 
 * >> ＠ApplicationException(rollback = true)
 *
 */
@Stateless
public class ItemEJB {
	@PersistenceContext(unitName = "Chapter09ProductionPU")
	private EntityManager em;
	
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
    public Item01 createItem(Item01 item) {
    	// Item01エンティティを永続化
        em.persist(item);
        
        // 在庫数量を＋１する
        inventoryEJB.addItem(item);

        return item;
    }

    /**
     * Item01エンティティの在庫数量を指定された数量だけ増やす。
     * 
     * @param item 在庫数量を増やすItem01エンティティ
     * @param stock 在庫数量の増分
     * @return 在庫数量が増加したItem01エンティティ
     */
    public Item01 addAvailableStock(Item01 item, int stock) {
    	int currentStock = item.getAvailableInStock();
    	item.setAvailableInStock(currentStock + stock);
    	
    	em.merge(item);
    	
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
    	Item01 soldItem = em.find(Item01.class, item.getId());

    	// Item01エンティティの在庫数量をひとつ減らす
    	soldItem.decreaseAvailableStock();

    	// Item01エンティティの在庫数量をチェックする
    	// 在庫数量が０の場合、InventoryLevelTooLowExceptionが
    	// スローされ、トランザクションがロールバックにマークされる
    	inventoryEJB.checkInventoryLevel(soldItem);
    	
    	return soldItem;
    }
}
