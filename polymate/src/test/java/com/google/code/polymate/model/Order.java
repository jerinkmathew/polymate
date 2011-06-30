/*
 *	Copyright (c) 2011 Marc Mai
 *
 *	Licensed under the MIT license: 
 *	http://www.opensource.org/licenses/mit-license.php
 *
 */
package com.google.code.polymate.model;

import org.bson.types.ObjectId;
import org.neo4j.graphdb.Node;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Transient;
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
public class Order {

	@Id
	private ObjectId id;
	private String orderNumber;
	@Transient
	@UnderlyingNode
	private Node node;

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

}
