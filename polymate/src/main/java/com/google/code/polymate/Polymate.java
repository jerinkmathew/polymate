/*
 *	Copyright (c) 2011 Marc Mai
 *
 *	Licensed under the MIT license: 
 *	http://www.opensource.org/licenses/mit-license.php
 *
 */
package com.google.code.polymate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.polymate.model.Customer;
import com.google.code.polymate.model.Order;
import com.mongodb.Mongo;

/**
 * 
 * Polymate represents the main entry point for the Polymate-Framework. All
 * CRUD-Operations are handled by this Class.
 * 
 * <br>
 * <br>
 * <small> Copyright (c) 2011 Marc Mai <br />
 * https://code.google.com/p/polymate/ </small> <br>
 * 
 * @author mai.marc@gmail.com<br>
 *         </i></small>
 */
public class Polymate {

	private static final String CLASS_NAME = "className";
	// private Mongo mongo;
	private Morphia morphia;
	private Datastore ds;
	private GraphDatabaseService neo;
	private Index<Node> mongoIdIndex;
	private Map<String, Node> clazzNodes = new HashMap<String, Node>();

	public Polymate(Mongo mongo, String mongoDbName, GraphDatabaseService neo) {
		// this.mongo = mongo;
		this.neo = neo;

		morphia = new Morphia();
		// TODO map classes
		morphia.map(Customer.class).map(Order.class);
		ds = morphia.createDatastore(mongo, mongoDbName);

		// index
		mongoIdIndex = neo.index().forNodes("mongoId");

		// init existing class-nodes
		initClazzNodes();
	}

	/**
	 * Adds a given Object to the underlying datastores. The Object's Class has
	 * to be mapped with Morphia-Annotations and has to have an @Id-property of
	 * the type org.bson.types.ObjectId.
	 * 
	 * @param <T>
	 * @param object
	 *            the object to persist.
	 * @return the persisted object with it's neo4j-node representation <br />
	 *         TODO or null, if an error occured.
	 */
	public <T> T add(T object) {
		Transaction tx = neo.beginTx();
		try {
			return add(object, tx);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(
					"Each entity class must have an @Id-property of the Type "
							+ ObjectId.class.getName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			tx.finish();
		}
	}

	/**
	 * TODO
	 * 
	 * @param <T>
	 * @param objects
	 */
	public <T> void addAll(List<T> objects) {
		Transaction tx = neo.beginTx();
		try {
			for (T obj : objects) {
				add(obj, tx);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(
					"Each entity class must have an @Id-property of the Type "
							+ ObjectId.class.getName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			tx.finish();
		}
	}

	private <T> T add(T object, Transaction tx)
			throws IllegalArgumentException, IllegalAccessException {
		// TODO handle Node creation failure
		ds.save(object);
		Field nodeField = ReflectionUtils.getAnnotatedField(object.getClass(),
				UnderlyingNode.class);
		if (nodeField != null) {
			Node node = neo.createNode();
			ObjectId idValue = ReflectionUtils.getIdValue(object);
			node.setProperty("mongoId", idValue.toString());
			mongoIdIndex.add(node, "mongoId", idValue.toString());
			node.createRelationshipTo(getClassNode(object.getClass()),
					Relation.HAS_CLASS);
			tx.success();
			injectNode(object, node, nodeField);
		}
		return object;
	}

	/**
	 * Finds all datastore-entries of the given Class, if they exist. The Class
	 * has to be properly mapped with Morphia-annotations ans has to have an
	 * 
	 * @Id-property of the type org.bson.types.ObjectId.
	 * 
	 * @param <T>
	 * @param clazz
	 *            the Class to search for.
	 * @return all datastore entries of the given Class.
	 */
	public <T> Iterable<T> find(Class<T> clazz) {
		List<T> results = new ArrayList<T>();
		Field nodeField = ReflectionUtils.getAnnotatedField(clazz,
				UnderlyingNode.class);
		for (T obj : ds.find(clazz).asList()) {
			try {
				// ObjectId idValue = getIdValue(obj);
				// Node node = mongoIdIndex.get("mongoId", idValue.toString())
				// .getSingle();
				// injectNode(obj, node, nodeField);
				injectNode(obj, new LazyNodeLookup<T>(mongoIdIndex, obj),
						nodeField);
				results.add(obj);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(
						"Each entity class must have an @Id-property of the Type "
								+ ObjectId.class.getName());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return results;
	}

	private Node getClassNode(Class<? extends Object> clazz) {
		Node clazzNode = clazzNodes.get(clazz.getName());
		if (clazzNode == null) {
			clazzNode = neo.createNode();
			clazzNode.setProperty(CLASS_NAME, clazz.getName());
			clazzNode.createRelationshipTo(neo.getReferenceNode(),
					Relation.CLASS);
			clazzNodes.put(clazz.getName(), clazzNode);
		}
		return clazzNode;
	}

	private <T> void injectNode(T object, Node node, Field nodeField)
			throws IllegalArgumentException, IllegalAccessException {
		if (nodeField != null) {
			nodeField.setAccessible(true);
			nodeField.set(object, node);
		} else {
			throw new RuntimeException("Each class that shall be saved with "
					+ Polymate.class.getSimpleName()
					+ " must declare a field of the type "
					+ Node.class.getName() + " annotated with "
					+ UnderlyingNode.class.getName());
		}
	}

	private void initClazzNodes() {
		for (Relationship rel : neo.getReferenceNode().getRelationships(
				Relation.CLASS)) {
			Node clazzNode = rel.getStartNode();
			clazzNodes.put((String) clazzNode.getProperty(CLASS_NAME),
					clazzNode);
		}
	}

}
