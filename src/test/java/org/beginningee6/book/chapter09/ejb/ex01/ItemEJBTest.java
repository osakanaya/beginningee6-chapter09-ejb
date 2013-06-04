package org.beginningee6.book.chapter09.ejb.ex01;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
 * 
 * EJBメソッド実行時のトランザクション制御に関するテスト。
 *
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
	ItemEJB itemEJB;	// ステートレス・セッションBeanの注入

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
	 * ２つのEJBメソッドから行ったデータベース更新をともにコミットする例
	 * 
	 * ItemEJB.createItem()によるItem01エンティティの永続化と
	 * InventoryEJB.addItem()によるItem01エンティティの更新が
	 * １つのトランザクションとして扱われ、かつ、この2つの変更が
	 * ともにコミットされていることを確認する。
	 */
	@Test
	public void testCreateAnItem() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

        ///// テスト /////
		
		// ItemEJBを通じてItem01エンティティを永続化・更新する
		Item01 returned = itemEJB.createItem(item);

        ///// 検証 /////
		
		// Item01エンティティが永続化されていることをIDの付番により確認
		assertThat(returned.getId(), 			is(notNullValue()));

		// 在庫数量がInventoryEJB.addItem()メソッドにより１に
		// 更新されていることを確認
		assertThat(returned.getAvailableInStock(), 	is(1));

		// 他のフィールドの値についても正しく永続化されていることを確認
		assertThat(returned.getTitle(), 		is("The Hitchhiker's Guide to the Galaxy"));
		assertThat(returned.getPrice(), 		is(12.5F));
		assertThat(returned.getDescription(), 	is("Science fiction comedy book"));

		// データベースからの直接検索により、永続化と在庫数量の更新が
		// 確実に行われていることを再度確認
		Item01 persisted = em.find(Item01.class, returned.getId());
		assertThat(persisted.getAvailableInStock(), 	is(1));
		
	}

	/**
	 * ２つのEJBメソッドから行ったデータベース更新をともにロールバックする例
	 * 
	 * ItemEJB.createItem()によるItem01エンティティの永続化と
	 * InventoryEJB.addItem()によるItem01エンティティの更新が
	 * １つのトランザクションとして扱われ、かつ、この2つの変更が
	 * ともにコミットされずにロールバックされていることを確認する。
	 */
	@Test
	public void testCreateAnItemFail() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

        ///// テスト /////
		
		// ItemEJBを通じてItem01エンティティを永続化・更新する
		itemEJB.createItemFail(item);

        ///// 検証 /////
		
		// データベースからの直接検索により、永続化と在庫数量の更新が
		// 行われていないkと尾を確認
		TypedQuery<Item01> query = em.createNamedQuery("Item01.findAllItems", Item01.class);
		assertThat(query.getResultList().size(), is(0));
	}

}
