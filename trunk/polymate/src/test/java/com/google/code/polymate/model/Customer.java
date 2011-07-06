/*
 *	Copyright (c) 2011 Marc Mai
 *
 *	Licensed under the MIT license: 
 *	http://www.opensource.org/licenses/mit-license.php
 *
 */
package com.google.code.polymate.model;

import java.util.LinkedList;
import java.util.List;

import org.bson.types.ObjectId;
import org.neo4j.graphdb.Node;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Transient;
import com.google.code.polymate.NodeReference;
import com.google.code.polymate.UnderlyingNode;

/**
 * 
 * TODO javadoc
 * 
 * <br>
 * <br>
 * <small> Copyright (c) 2011 Marc Mai <br />
 * https://code.google.com/p/polymate/ </small> <br>
 * 
 * @author mai.marc@gmail.com<br>
 *         </i></small>
 */
@Entity
@SuppressWarnings("unused")
public class Customer {

	@Id
	private ObjectId id;
	private String name;
	@UnderlyingNode
	@Transient
	private Node node;
	@NodeReference(type = Order.class)
	@Transient
	private List<Order> orders = new LinkedList<Order>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

}
