/*
 *	Copyright (c) 2011 Marc Mai
 *
 *	Licensed under the MIT license: 
 *	http://www.opensource.org/licenses/mit-license.php
 *
 */
package com.google.code.polymate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.google.code.morphia.Morphia;
import com.google.code.polymate.model.Customer;
import com.google.code.polymate.model.Order;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * 
 * Test-case for basic CRUD operations with Polymate.
 * 
 * <br>
 * <br>
 * <small> Copyright (c) 2011 Marc Mai <br />
 * https://code.google.com/p/polymate/ </small> <br>
 * 
 * @author mai.marc@gmail.com<br>
 *         </i></small>
 */
public class CRUDTests {

	private static final String NEO_DB_DIR = "test";
	private static final String MONGO_DB_NAME = "test";
	private Mongo mongo;
	private Morphia morphia;
	private EmbeddedGraphDatabase neo;
	private Polymate polymate;

	@Before
	public void setUp() throws UnknownHostException, MongoException {
		// set up mongo + morphia
		mongo = new Mongo();
		morphia = new Morphia();
		morphia.map(Customer.class).map(Order.class);
		// set up neo
		neo = new EmbeddedGraphDatabase(NEO_DB_DIR);
		polymate = new Polymate(mongo, MONGO_DB_NAME, neo);
	}

	@After
	public void tearDown() throws IOException {
		mongo.dropDatabase(MONGO_DB_NAME);
		mongo.close();
		neo.shutdown();
		FileUtils.deleteDirectory(new File(NEO_DB_DIR));
	}

	@Test
	public void testAddCustomer() {
		Long start = System.currentTimeMillis();
		Customer customer = new Customer();
		customer.setName("Test Customer");
		assertNotNull(polymate.add(customer));
		System.out.println("Took: " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		Iterable<Customer> result = polymate.find(Customer.class);
		System.out.println("Took: " + (System.currentTimeMillis() - start));

		assertNotNull(result);
		assertTrue(result.iterator().hasNext());
		Customer customer2 = result.iterator().next();
		assertNotNull(customer2);
		assertEquals(customer.getName(), customer2.getName());
	}

	@Test
	public void testAddAllCustomers() {
		List<Customer> customers = new ArrayList<Customer>();
		Long start = System.currentTimeMillis();
		for (int i = 1; i <= 10000; i++) {
			Customer customer = new Customer();
			customer.setName("Customer_" + i);
			customers.add(customer);
		}
		polymate.addAll(customers);
		System.out.println("Took: " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		Iterable<Customer> result = polymate.find(Customer.class);
		System.out.println("Took: " + (System.currentTimeMillis() - start));

		assertNotNull(result);
		assertTrue(result.iterator().hasNext());
		Customer customer2 = result.iterator().next();
		assertNotNull(customer2);
	}

	@Test
	public void testAddOrder() {
		Order order = new Order();
		order.setOrderNumber("Order 1");
		assertNotNull(polymate.add(order));

		Iterable<Order> result = polymate.find(Order.class);
		assertNotNull(result);
		assertTrue(result.iterator().hasNext());
		Order order2 = result.iterator().next();
		assertNotNull(order2);
		assertEquals(order.getOrderNumber(), order2.getOrderNumber());
	}

}
