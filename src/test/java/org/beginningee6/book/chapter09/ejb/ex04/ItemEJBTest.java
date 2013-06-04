package org.beginningee6.book.chapter09.ejb.ex04;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithApplicationAnnotationRollbackTrue;
import org.beginningee6.book.chapter09.ejb.ex04.exception.CheckedExceptionWithoutAnnotation;
import org.beginningee6.book.chapter09.ejb.ex04.exception.UncheckedExceptionWithApplicationAnnotationRollbackTrue;
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
 * チェック例外および非チェック例外をスローしたときのトランザクションの
 * ロールバックを確認するためのサンプル。
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
				.addPackage(CheckedExceptionWithoutAnnotation.class.getPackage())
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
	ItemEJB itemEJB;	// ステートレス・セッションBanを注入

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
	 * EJBのメソッド実行で＠ApplicationExceptionアノテーションが付与されない
	 * チェック例外がスローされた時にトランザクションがロールバックされない
	 * ことを確認するテスト。
	 */
	@Test
	public void testInnerEJBThrowsCheckedExceptionWithoutAnnotation() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		///// テスト /////
		
		Item01 returned = itemEJB.ejbMethodThrowsCheckedExceptionWithoutAnnotation(item);

		///// 検証 /////
		
		// Item01エンティティが永続化されたことを確認する
		assertThat(returned.getId(), 				is(notNullValue()));
		assertThat(returned.getTitle(), 			is("The Hitchhiker's Guide to the Galaxy"));
		assertThat(returned.getPrice(), 			is(12.5F));
		assertThat(returned.getDescription(), 		is("Science fiction comedy book"));
		assertThat(returned.getAvailableInStock(), 	is(0));
		
		Item01 persisted = em.find(Item01.class, returned.getId());
		assertThat(persisted.getId(), is(returned.getId()));
		
	}

	/**
	 * EJBのメソッド実行でrollbackオプションがtrueの＠ApplicationException
	 * アノテーションが付与されたチェック例外がスローされた時に
	 * トランザクションがロールバックされることを確認するテスト。
	 */
	@Test
	public void testInnerEJBThrowsCheckedExceptionWithApplicationAnnotationRollbackTrue() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		///// テスト /////
		
		try {
			@SuppressWarnings("unused")
			Item01 returned = itemEJB.ejbMethodThrowsCheckedExceptionWithApplicationAnnotationRollbackTrue(item);
			fail("Should throw exception");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(CheckedExceptionWithApplicationAnnotationRollbackTrue.class)));
		}

		///// 検証 /////

		// Item01エンティティが永続化されないことを確認する
		List<Item01> persisted = 
			em.createNamedQuery("Item01.findAllItems", Item01.class)
				.getResultList();

		assertThat(persisted.size(), is(0));
	}

	/**
	 * EJBのメソッド実行でrollbackオプションがfalseの＠ApplicationException
	 * アノテーションが付与されたチェック例外がスローされた時に
	 * トランザクションがロールバックされないことを確認するテスト。
	 */
	@Test
	public void testInnerEJBThrowsCheckedExceptionWithApplicationAnnotationRollbackFalse() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		///// テスト /////
		
		Item01 returned = itemEJB.ejbMethodThrowsCheckedExceptionWithApplicationAnnotationRollbackFalse(item);

		///// 検証 /////

		// Item01エンティティが永続化されたことを確認する
		assertThat(returned.getId(), 				is(notNullValue()));
		assertThat(returned.getTitle(), 			is("The Hitchhiker's Guide to the Galaxy"));
		assertThat(returned.getPrice(), 			is(12.5F));
		assertThat(returned.getDescription(), 		is("Science fiction comedy book"));
		assertThat(returned.getAvailableInStock(), 	is(0));
		
		Item01 persisted = em.find(Item01.class, returned.getId());
		assertThat(persisted.getId(), is(returned.getId()));
		
	}
	
	/**
	 * EJBのメソッド実行で＠ApplicationExceptionアノテーションが付与されない
	 * 非チェック例外がスローされた時にトランザクションがロールバックされる
	 * ことを確認するテスト。
	 */
	@Test
	public void testInnerEJBThrowsUncheckedExceptionWithoutAnnotation() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		///// テスト /////
		
		try {
			@SuppressWarnings("unused")
			Item01 returned = itemEJB.ejbMethodThrowsUncheckedExceptionWithApplicationAnnotationRollbackTrue(item);
			fail("Should throw exception");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(UncheckedExceptionWithApplicationAnnotationRollbackTrue.class)));
		}

		///// 検証 /////

		// Item01エンティティが永続化されないことを確認する
		List<Item01> persisted = 
			em.createNamedQuery("Item01.findAllItems", Item01.class)
				.getResultList();

		assertThat(persisted.size(), is(0));
		
	}

	/**
	 * EJBのメソッド実行でrollbackオプションがtrueの＠ApplicationException
	 * アノテーションが付与された非チェック例外がスローされた時に
	 * トランザクションがロールバックされることを確認するテスト。
	 */
	@Test
	public void testInnerEJBThrowsUncheckedExceptionWithApplicationAnnotationRollbackTrue() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		///// テスト /////
		
		try {
			@SuppressWarnings("unused")
			Item01 returned = itemEJB.ejbMethodThrowsUncheckedExceptionWithApplicationAnnotationRollbackTrue(item);
			fail("Should throw exception");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(UncheckedExceptionWithApplicationAnnotationRollbackTrue.class)));
		}

		///// 検証 /////
		
		// Item01エンティティが永続化されないことを確認する
		List<Item01> persisted = 
			em.createNamedQuery("Item01.findAllItems", Item01.class)
				.getResultList();

		assertThat(persisted.size(), is(0));
	}

	/**
	 * EJBのメソッド実行でrollbackオプションがfalseの＠ApplicationException
	 * アノテーションが付与された非チェック例外がスローされた時に
	 * トランザクションがロールバックされないことを確認するテスト。
	 */
	@Test
	public void testInnerEJBThrowsUncheckedExceptionWithApplicationAnnotationRollbackFalse() throws Exception {
		
		///// 準備 /////
		
		Item01 item = new Item01();
		item.setTitle("The Hitchhiker's Guide to the Galaxy");
		item.setPrice(12.5F);
		item.setDescription("Science fiction comedy book");

		///// テスト /////
		
		Item01 returned = itemEJB.ejbMethodThrowsUncheckedExceptionWithApplicationAnnotationRollbackFalse(item);

		///// 検証 /////

		// Item01エンティティが永続化されたことを確認する
		assertThat(returned.getId(), 				is(notNullValue()));
		assertThat(returned.getTitle(), 			is("The Hitchhiker's Guide to the Galaxy"));
		assertThat(returned.getPrice(), 			is(12.5F));
		assertThat(returned.getDescription(), 		is("Science fiction comedy book"));
		assertThat(returned.getAvailableInStock(), 	is(0));
		
		Item01 persisted = em.find(Item01.class, returned.getId());
		assertThat(persisted.getId(), is(returned.getId()));
		
	}

}
