package org.beginningee6.book.chapter09.ejb.ex03;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.jpa.ex01.Item01;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * rollbackオプションにfalseを設定した＠ApplicationExceptionアノテーションが
 * 付与されたチェック例外をスローされた時にトランザクションがロールバック
 * されることを確認するテスト。
 */
@RunWith(Arquillian.class)
public class ItemEJBTest {
	private static final Logger logger = Logger.getLogger(ItemEJBTest.class
			.getName());

	@Deployment
	public static Archive<?> createDeployment() {
		File[] dependencyLibs 
			= Maven
				.configureResolver()				
				.fromFile("D:\\apache-maven-3.0.3\\conf\\settings.xml")
				.resolve("org.beginningee6.book:beginningee6-chapter09-jpa:0.0.1-SNAPSHOT")
				.withTransitivity()
				.asFile();

		WebArchive archive = ShrinkWrap
				.create(WebArchive.class)
				.addPackage(ItemEJB.class.getPackage())
				.addAsLibraries(dependencyLibs)
				.addAsWebInfResource("jbossas-ds.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction userTransaction;

	@EJB
	ItemEJB itemEJB;	// ステートレス・セッションBeanを注入

	@Before
	public void setUp() throws Exception {
		clearData();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Item01").executeUpdate();
		userTransaction.commit();
	}

	/**
	 * ItemEJB.createItem()メソッドによりItem01エンティティを
	 * 永続化するテスト。
	 */
	@Test
	public void testCreateAnItem() throws Exception {
		
        ///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

        ///// テスト /////
		
		Item01 returned = itemEJB.createItem(item);

        ///// 検証 /////
		
		// Item01エンティティが永続化されていることをIDの付番により確認
		assertThat(returned.getId(), 				is(notNullValue()));

		// 在庫数量がInventoryEJB.addItem()メソッドにより１に
		// 更新されていることを確認
		assertThat(returned.getAvailableInStock(), 	is(1));

		// 他のフィールドの値についても正しく永続化されていることを確認
		assertThat(returned.getTitle(), 			is("The Hitchhiker's Guide to the Galaxy"));
		assertThat(returned.getPrice(), 			is(12.5F));
		assertThat(returned.getDescription(), 		is("Science fiction comedy book"));
		
		// データベースからの直接検索により、永続化と在庫数量の更新が
		// 確実に行われていることを再度確認
		Item01 persisted = em.find(Item01.class, returned.getId());
		assertThat(persisted.getAvailableInStock(), is(1));
		
	}

	/**
	 * ItemEJB.addAvailableStock()メソッドによりItem01エンティティの
	 * 在庫数量を更新するテスト。
	 */
	@Test
	public void testAddAvailableStock() throws Exception {
		
        ///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		Item01 created = itemEJB.createItem(item);

        ///// テスト /////
		
		Item01 stocked = itemEJB.addAvailableStock(created, 5);

        ///// 検証 /////
		
		// 在庫数量が＋５されていることを確認
		assertThat(stocked.getId(), 				is(created.getId()));
		assertThat(stocked.getAvailableInStock(), 	is(1 + 5));
		
		// データベースからの直接検索により、在庫数量の更新が確実に
		// 行われていることを再度確認
		Item01 persisted = em.find(Item01.class, created.getId());
		assertThat(persisted.getAvailableInStock(), is(1 + 5));
	}

	/**
	 * ItemEJB.sellOneItem()メソッドにより在庫数量が２のItem01エンティティ
	 * に対して在庫を引き当てるテスト。
	 * 
	 * InventoryLevelTooLowExceptionはスローされないため、在庫数量が
	 * －１され、そのトランザクションはコミットされる。
	 */
	@Test
	public void testSellOneItemWhenStockAvailable() throws Exception {
		
        ///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		Item01 created = itemEJB.createItem(item);
		// 在庫数量は２
		created = itemEJB.addAvailableStock(created, 1);
		
        ///// テスト /////
		
		Item01 sold = itemEJB.sellOneItem(created);

        ///// 検証 /////

		// 在庫数量が－１されていること（２→１）を確認
		assertThat(sold.getId(), 				is(created.getId()));
		assertThat(sold.getAvailableInStock(), 	is(1));
		
		// データベースからの直接検索により、在庫数量の更新が確実に
		// 行われていることを再度確認
		Item01 persisted = em.find(Item01.class, created.getId());
		assertThat(persisted.getAvailableInStock(), is(1));
	}
	
	/**
	 * ItemEJB.sellOneItem()メソッドにより在庫数量が１のItem01エンティティ
	 * に対して在庫を引き当てるテスト。
	 * 
	 * InventoryLevelTooLowExceptionがスローされるため、トランザクションは
	 * ロールバックされる。
	 * 
	 */
	@Test
	public void testSellOneItemWhenStockNotAvailable() throws Exception {
		
        ///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		// 在庫数量は１
		Item01 created = itemEJB.createItem(item);
		
        ///// テスト /////
		
		try {
			@SuppressWarnings("unused")
			Item01 sold = itemEJB.sellOneItem(created);
		} catch (Exception e) {
			// 在庫数量１からの引き当てはできないため、例外がスローされる
			assertThat(e, is(instanceOf(InventoryLevelTooLowException.class)));
		}
		
        ///// 検証 /////

		// 在庫数量が１のままであることを確認
		Item01 persisted = em.find(Item01.class, created.getId());
		assertThat(persisted.getAvailableInStock(), is(1));
	}

}
