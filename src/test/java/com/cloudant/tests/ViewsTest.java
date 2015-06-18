/*
 * Copyright (C) 2011 lightcouch.org
 *
 * Modifications for this distribution by IBM Cloudant, Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudant.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lightcouch.DocumentConflictException;
import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.View;
import com.cloudant.client.api.model.Page;
import com.cloudant.client.api.model.ViewResult;
import com.google.gson.JsonObject;

public class ViewsTest {

	
	private static Database db;
	private CloudantClient account;


	@Before
	public  void setUp() {
		account = CloudantClientHelper.getClient();

		db = account.database("lightcouch-db-test", true);

		db.syncDesignDocsWithDb();
	}

	@After
	public void tearDown(){
		account.deleteDB("lightcouch-db-test");
		account.shutdown();
	}


	@Test
	public void queryView() {
		init();
		List<Foo> foos = db.view("example/foo")
				.includeDocs(true)
				.query(Foo.class);
		assertThat(foos.size(), not(0));
	}

	@Test
	public void byKey() {
		init();
		List<Foo> foos = db.view("example/foo")
				.includeDocs(true)
				.key("key-1")
				.query(Foo.class);
		assertThat(foos.size(), is(1));
	}

	@Test
	public void byKeys() {
		init();
		List<Foo> foos = db.view("example/foo")
				.includeDocs(true)
				.keys(new Object[] {"key-1", "key-2"})
				.query(Foo.class);
		assertThat(foos.size(), is(2));
	}
	
	@Test
	public void byStartAndEndKey() {
		init();
		List<Foo> foos = db.view("example/foo")
				.startKey("key-1")
				.endKey("key-2")
				.includeDocs(true)
				.query(Foo.class);
		assertThat(foos.size(), is(2));
	}

	@Test
	public void byComplexKey() {
		init();
		int[] complexKey = new int[] { 2011, 10, 15 };
		List<Foo> foos = db.view("example/by_date")
				.key(complexKey)
//				.key(2011, 10, 15) 
				.includeDocs(true)
				.reduce(false)
				.query(Foo.class);
		assertThat(foos.size(), is(2));
	}

	@Test
	public void byComplexKeys() {
		init();
		int[] complexKey1 = new int[] { 2011, 10, 15 };
		int[] complexKey2 = new int[] { 2013, 12, 17 };
		List<Foo> foos = db.view("example/by_date")
				.keys(new Object[] {complexKey1, complexKey2})
				.includeDocs(true)
				.reduce(false)
				.query(Foo.class);
		assertThat(foos.size(), is(3));
	}
	@Test
	public void viewResultEntries() {
		init();
		ViewResult<int[], String, Foo> viewResult = db.view("example/by_date")
				.reduce(false)
				.queryView(int[].class, String.class, Foo.class);
		assertThat(viewResult.getRows().size(), is(3));
	}

	@Test
	public void scalarValues() {
		init();
		int allTags = db.view("example/by_tag").queryForInt();
		assertThat(allTags, is(4));

		long couchDbTags = db.view("example/by_tag")
				.key("couchdb")
				.queryForLong();
		assertThat(couchDbTags, is(2L));

		String javaTags = db.view("example/by_tag")
				.key("java")
				.queryForString();
		assertThat(javaTags, is("1"));
	}

	@Test(expected = NoDocumentException.class)
	public void viewWithNoResult_throwsNoDocumentException() {
		init();
		db.view("example/by_tag")
		.key("javax")
		.queryForInt();
	}

	@Test
	public void groupLevel() {
		init();
		ViewResult<int[], Integer, Foo> viewResult = db
				.view("example/by_date")
				.groupLevel(2)
				.queryView(int[].class, Integer.class, Foo.class);
		assertThat(viewResult.getRows().size(), is(2));
	}

	@Test
	public void allDocs() {
		init();
		db.save(new Foo());
		List<JsonObject> allDocs = db.view("_all_docs")
				.query(JsonObject.class);
		assertThat(allDocs.size(), not(0));
	}

	/**
	 * @param index the index to encode.
	 * @return a three character string representing the given {@code index}.
	 */
	private static String docTitle(int index) {
		return String.format("%03d", index);
	}

	/**
	 * Checks the document titles are as expected on the given {@code page}.
	 * @param page the page to check.
	 * @param docCount the total number of documents in the view.
	 * @param docsPerPage the number of documents per page.
	 * @param pageNumber the page number of the {@code page}.
	 * @param descending whether the view is descending or not.
	 */
	private static void checkDocumentTitles(Page page, int docCount, int docsPerPage, int pageNumber, boolean descending) {
		int offset = (pageNumber - 1) * docsPerPage + 1;
		if (descending) {
			offset = docCount + 1 - offset;
		}
		List<Foo> resultList = page.getResultList();
		for (int i=0; i< resultList.size(); ++i) {
			assertEquals("Document titles do not match", docTitle(descending ? offset-- : offset++), resultList.get(i).getTitle());
		}
	}

	/**
	 * Checks various aspects of the given {@code page} are as expected.
	 * @param page the page to check.
	 * @param docCount the total number of documents in the view.
	 * @param docsPerPage the number of documents per page.
	 * @param pageNumber the page number of the {@code page}.
	 * @param descending whether the view is descending or not.
	 */
	private static void checkPage(Page page, int docCount, int docsPerPage, int pageNumber, boolean descending) {
		if (pageNumber == 1) {
			assertFalse(page.isHasPrevious());
		} else {
			assertTrue(page.isHasPrevious());
		}

		double numberOfPages = docCount / (double)docsPerPage;
		if (pageNumber >= numberOfPages) {
			assertFalse(page.isHasNext());
		} else {
			assertTrue(page.isHasNext());
		}

		int startIndex = (pageNumber - 1) * docsPerPage + 1;
		assertThat(page.getResultFrom(), is(startIndex));
		assertThat(page.getResultTo(), is(Math.min(startIndex + docsPerPage - 1, docCount)));
		assertThat(page.getPageNumber(), is(pageNumber));
		if (page.isHasNext() || docCount % docsPerPage == 0) {
			assertThat(page.getResultList().size(), is(docsPerPage));
		} else {
			assertThat(page.getResultList().size(), is(docCount % docsPerPage));
		}
		checkDocumentTitles(page, docCount, docsPerPage, pageNumber, descending);
	}

	/**
	 * Check that we can page through a view in ascending order where the number of results
	 * is an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses the same {@link View} instance for each page query.
	 */
	@Test
	public void paginationAscendingReusingView() {
		View view = db.view("example/foo")
			.reduce(false);
		checkPagination(false, 6, 2, view);
	}

	/**
	 * Check that we can page through a view in descending order where the number of results
	 * is an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses the same {@link View} instance for each page query.
	 */
	@Test
	public void paginationDescendingReusingView() {
		View view = db.view("example/foo")
			.reduce(false)
			.descending(true);
		checkPagination(true, 6, 2, view);
	}

	/**
	 * Check that we can page through a view in ascending order where the number of results
	 * is not an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses the same {@link View} instance for each page query.
	 */
	@Test
	public void paginationAscendingReusingViewPartialLastPage() {
		View view = db.view("example/foo")
			.reduce(false);
		checkPagination(false, 5, 2, view);
	}

	/**
	 * Check that we can page through a view in descending order where the number of results
	 * is not an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses the same {@link View} instance for each page query.
	 */
	@Test
	public void paginationDescendingReusingViewPartialLastPage() {
		View view = db.view("example/foo")
			.reduce(false)
			.descending(true);
		checkPagination(true, 5, 2, view);
	}

	/**
	 * Check that we can page through a view in ascending order where the number of results
	 * is an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses a new {@link View} instance for each page query.
	 */
	@Test
	public void paginationAscendingNewView() {
		checkPagination(false, 6, 2, null);
	}

	/**
	 * Check that we can page through a view in descending order where the number of results
	 * is an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses a new {@link View} instance for each page query.
	 */
	@Test
	public void paginationDescendingNewView() {
		checkPagination(true, 6, 2, null);
	}

	/**
	 * Check that we can page through a view in ascending order where the number of results
	 * is not an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses a new {@link View} instance for each page query.
	 */
	@Test
	public void paginationAscendingNewViewPartialLastPage() {
		checkPagination(false, 5, 2, null);
	}

	/**
	 * Check that we can page through a view in descending order where the number of results
	 * is not an exact multiple of the number of pages. Check each page contains the documents
	 * we expect. This test uses a newfuture {@link View} instance for each page query.
	 */
	@Test
	public void paginationDescendingNewViewPartialLastPage() {
		checkPagination(true, 5, 2, null);
	}

	/**
	 * This helper function uses the given {@code view} to query for a page of
	 * results. The {@code param} indicates whether we should be getting a next or previous
	 * page. If {@code view} is null, a new {@link View} is created to perform the query,
	 * otherwise the given {@code view} is used.
	 * @param expectedPageNumber the page number of the page we expect to be returned.
	 * @param param the request parameter to use to query a page, or {@code null} to return the first page.
	 * @param descending true if the view should be created in descending order, and false otherwise.
	 * @param docCount the total number of documents in the view.
	 * @param docsPerPage the maximum number of documents per page in the view.
	 * @param view the {@link View} object to use to perform the query, or {@code null} to create a new {@link View}
	 * @return the page of results.
	 */
	private static Page queryAndCheckPage(int expectedPageNumber, String param, boolean descending, int docCount, int docsPerPage, View view) {
		View queryView = view;
		if (queryView == null) {
			queryView = db.view("example/foo")
				.reduce(false)
				.descending(descending);
		}
		Page page = queryView.queryPage(docsPerPage, param, Foo.class);
		checkPage(page, docCount, docsPerPage, expectedPageNumber, descending);
		return page;
	}

	/**
	 * This tests that paging of the view with multiple changes of direction works.
	 * <p>
	 * Checks that after each page is returned it is as expected. The paging is done
	 * in the following order:
	 * <ol>
	 *     <li>get the first page of results;</li>
	 *     <li>page forward until the last page is reached;</li>
	 *     <li>page backward until the first page is reached;</li>
	 *     <li>page forward until the last page is reached;</li>
	 *     <li>page backward until the first page is reached.</li>
	 * </ol>
	 * @param descending true if the view is in descending order, and false otherwise.
	 * @param docCount the total number of documents in the view.
	 * @param docsPerPage the maximum number of documents per page in the view.
	 * @param view the {@link View} object to use to perform the query.
	 */
	private void checkPagination(boolean descending, int docCount, int docsPerPage, View view) {
		for (int i = 0; i < docCount; ++i) {
			Foo foo = new Foo(generateUUID(), docTitle(i + 1));
			db.save(foo);
		}

		int numberOfPages = (int)Math.ceil(docCount / (double)docsPerPage);

		// Get the first page of results.
		Page page = queryAndCheckPage(1, null, descending, docCount, docsPerPage, view);

		page = checkPagesForward(numberOfPages, page, descending, docCount, docsPerPage, view);
		page = checkPagesBackward(numberOfPages, page, descending, docCount, docsPerPage, view);
		page = checkPagesForward(numberOfPages, page, descending, docCount, docsPerPage, view);
		checkPagesBackward(numberOfPages, page, descending, docCount, docsPerPage, view);
	}

	/**
	 * Check all the pages going forwards until we reach the last page. This assumes the given
	 * {@code page} is the first page of results.
	 * @param numberOfPages the total number of pages in the {@link View}
	 * @param page the first page of results in the view.
	 * @param descending true if the view is in descending order, and false otherwise.
	 * @param docCount the total number of documents in the view.
	 * @param docsPerPage the maximum number of documents per page.
	 * @param view the {@link View} object to use to perform the query.
	 * @return the last page in the view.
	 */
	private Page checkPagesForward (int numberOfPages, Page page, boolean descending, int docCount, int docsPerPage, View view) {
		for (int i = 2; i <= numberOfPages; ++i) {
			page = queryAndCheckPage(i, page.getNextParam(), descending, docCount, docsPerPage, view);
		}
		return page;
	}

	/**
	 * Check all the pages going backwards until we reach the first page. This assumes the
	 * given {@code page} is the last page of results.
	 * @param numberOfPages the total number of pages in the {@link View}
	 * @param page the last page of results in the view.
	 * @param descending true if the view is in descending order, and false otherwise.
	 * @param docCount the total number of documents in the view.
	 * @param docsPerPage the maximum number of documents per page.
	 * @param view the {@link View} object to use to perform the query.
	 * @return the first page in the view
	 */
	private Page checkPagesBackward (int numberOfPages, Page page, boolean descending, int docCount, int docsPerPage, View view) {
		for (int i = numberOfPages - 1; i > 0; --i) {
			page = queryAndCheckPage(i, page.getPreviousParam(), descending, docCount, docsPerPage, view);
		}
		return page;
	}

	private static void init() {
		try {
			Foo foo = null;

			foo = new Foo("id-1", "key-1");
			foo.setTags(Arrays.asList(new String[] { "couchdb", "views" }));
			foo.setComplexDate(new int[] { 2011, 10, 15 });
			db.save(foo);

			foo = new Foo("id-2", "key-2");
			foo.setTags(Arrays.asList(new String[] { "java", "couchdb" }));
			foo.setComplexDate(new int[] { 2011, 10, 15 });
			db.save(foo);

			foo = new Foo("id-3", "key-3");
			foo.setComplexDate(new int[] { 2013, 12, 17 });
			db.save(foo);

		} catch (DocumentConflictException e) {
		}
	}

	private static String generateUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
