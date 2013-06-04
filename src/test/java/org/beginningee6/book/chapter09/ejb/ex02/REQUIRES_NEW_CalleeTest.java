package org.beginningee6.book.chapter09.ejb.ex02;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter09.ejb.ex02.callee.REQUIRES_NEW_CalleeEJB;
import org.beginningee6.book.chapter09.ejb.ex02.caller.REQUIRED_CallerEJB;
import org.beginningee6.book.chapter09.ejb.ex02.caller.SUPPORTS_CallerEJB;
import org.beginningee6.book.chapter09.jpa.ex02.Book02;
import org.beginningee6.book.chapter09.jpa.ex02.CD02;
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
 * トランザクション属性がREQUIREDのEJBメソッド（トランザクションを開始している状態の
 * クライアント）およびSUPPORTSのEJBメソッド（トランザクションを開始していない状態の
 * クライアント）から、トランザクション属性がREQUIRES_NEWのEJBメソッドを実行した時の
 * トランザクションの振る舞いを確認するテスト。
 *
 */
@RunWith(Arquillian.class)
public class REQUIRES_NEW_CalleeTest {
	private static final Logger logger = Logger.getLogger(REQUIRES_NEW_CalleeTest.class
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
				.addPackage(REQUIRES_NEW_CalleeEJB.class.getPackage())
				.addPackage(REQUIRED_CallerEJB.class.getPackage())
				.addAsLibraries(dependencyLibs)
				.addAsWebInfResource("jbossas-ds.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction userTransaction;

	// 呼び出し元のEJB（トランザクションを開始するクライアント）への参照を注入
	// トランザクション属性がREQUIREDのメソッドが定義されている
	@EJB
	REQUIRED_CallerEJB required_callerEJB;

	// 呼び出し元のEJB（トランザクションを開始しないクライアント）への参照を注入
	// トランザクション属性がSUPPORTSのメソッドが定義されている
	@EJB
	SUPPORTS_CallerEJB supports_callerEJB;
	
	private Book02 book;
	private CD02 cd;

	private TypedQuery<Book02> bookQuery;
	private TypedQuery<CD02> cdQuery;

	@Before
	public void setUp() throws Exception {
		clearData();
		setUpEntities();
		setUpQueries();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Book02").executeUpdate();
		em.createQuery("DELETE FROM CD02").executeUpdate();
		userTransaction.commit();
	}
	
	private void setUpEntities() {
		book = new Book02();
        book.setTitle("The Hitchhiker's Guide to the Galaxy");
        book.setPrice(12.5F);
        book.setDescription("Science fiction comedy book");
        book.setIsbn("1-84023-742-2");
        book.setNbOfPage(354);
        book.setIllustrations(false);

        cd = new CD02(
        		"Title 1",
        		10.0F,
        		"Title 1 Description",
        		null,
        		"Music Company 1",
        		1,
        		100.0F,
        		"male");
	}
	
	private void setUpQueries() {
		bookQuery = em.createNamedQuery("Book02.findAllBooks", Book02.class);
		cdQuery = em.createNamedQuery("CD02.findAllCDs", CD02.class);
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
	@Test
	public void testREQUIRES_NEW_Callee_REQUIRED_Caller_Both_Persist() throws Exception {
		
        ///// テスト /////
		
		required_callerEJB.REQUIRES_NEW_CalleePersist_REQUIRED_CallerPersist(book, cd);

        ///// 検証 /////
		
		// Book02エンティティとCD02エンティティが永続化されたことを確認
		assertThat(bookQuery.getResultList().size(), is(1));
		assertThat(bookQuery.getResultList().get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
		
		assertThat(cdQuery.getResultList().size(), is(1));
		assertThat(cdQuery.getResultList().get(0).getTitle(), is("Title 1"));
	}

	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@Test
	public void testREQUIRES_NEW_Callee_Rollback_REQUIRED_Caller_Persist() throws Exception {
		
        ///// テスト /////
		
		required_callerEJB.REQUIRES_NEW_CalleePersistAndRollback_REQUIRED_CallerPersist(book, cd);

        ///// 検証 /////
		
		// Book02エンティティのみが永続化されたことを確認
		assertThat(bookQuery.getResultList().size(), is(1));
		assertThat(bookQuery.getResultList().get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
		
		assertThat(cdQuery.getResultList().size(), is(0));
	}

	/**
	 * ３．呼び出し元でトランザクションがロールバックにマークされる例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@Test
	public void testREQUIRES_NEW_Callee_Persist_REQUIRED_Caller_PersistAndRollback() throws Exception {
		
        ///// テスト /////
		
		required_callerEJB.REQUIRES_NEW_CalleePersist_REQUIRED_CallerPersistAndRollback(book, cd);

        ///// 検証 /////
		
		// CD02エンティティのみが永続化されたことを確認
		assertThat(bookQuery.getResultList().size(), is(0));
		
		assertThat(cdQuery.getResultList().size(), is(1));
		assertThat(cdQuery.getResultList().get(0).getTitle(), is("Title 1"));
	}

	/**
	 * ==========================================================================
	 * 呼び出し元：SUPPORTS→呼び出し先：REQUIRES_NEWの確認
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
	@Test
	public void testREQUIRES_NEW_Callee_SUPPORTS_Caller_Both_Persist() throws Exception {
		
        ///// テスト /////
		
		supports_callerEJB.REQUIRES_NEW_CalleePersist_SUPPORTS_CallerPersist(book, cd);

        ///// 検証 /////
		
		// Book02エンティティとCD02エンティティが永続化されたことを確認
		assertThat(bookQuery.getResultList().size(), is(1));
		assertThat(bookQuery.getResultList().get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
		
		assertThat(cdQuery.getResultList().size(), is(1));
		assertThat(cdQuery.getResultList().get(0).getTitle(), is("Title 1"));
	}

	/**
	 * ２．呼び出し先でトランザクションがロールバックにマークされる例
	 * 　　（呼び出し元はその後永続化を行おうとする）
	 * 
	 * 【結果】
	 * Book02エンティティのみが永続化される。
	 */
	@Test
	public void testREQUIRES_NEW_Callee_Rollback_SUPPORTS_Caller_Persist() throws Exception {
		
        ///// テスト /////
		
		supports_callerEJB.REQUIRES_NEW_CalleePersistAndRollback_SUPPORTS_CallerPersist(book, cd);

        ///// 検証 /////

		// Book02エンティティのみが永続化されたことを確認
		assertThat(bookQuery.getResultList().size(), is(1));
		assertThat(bookQuery.getResultList().get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
		
		assertThat(cdQuery.getResultList().size(), is(0));
	}

	/**
	 * ３．呼び出し元でトランザクションをロールバックする例
	 * 
	 * 【結果】
	 * CD02エンティティのみが永続化される。
	 */
	@Test
	public void testREQUIRES_NEW_Callee_Persist_SUPPORTS_Caller_PersistAndRollback() throws Exception {
		
        ///// テスト /////
		
		supports_callerEJB.REQUIRES_NEW_CalleePersist_SUPPORTS_CallerPersistAndRollback(book, cd);

        ///// 検証 /////

		// CD02エンティティのみが永続化されたことを確認
		assertThat(bookQuery.getResultList().size(), is(0));
		
		assertThat(cdQuery.getResultList().size(), is(1));
		assertThat(cdQuery.getResultList().get(0).getTitle(), is("Title 1"));
	}
}
