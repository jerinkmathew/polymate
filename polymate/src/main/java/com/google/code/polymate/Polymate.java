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
import org.neo4j.graphdb.DynamicRelationshipType;
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

	protected static final String MONGO_ID = "mongoId";
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
		mongoIdIndex = neo.index().forNodes(MONGO_ID);

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
	public <T> T save(T object) {
		Transaction tx = neo.beginTx();
		try {
			// TODO handle update of existing datasets
			T result = add(object);
			tx.success();
			return result;
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
	public <T> void saveAll(List<T> objects) {
		Transaction tx = neo.beginTx();
		try {
			for (T obj : objects) {
				add(obj);
			}
			tx.success();
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

	private <T, V> T add(T object) throws IllegalArgumentException,
			IllegalAccessException {
		// TODO handle Node creation failure
		ds.save(object);

		Field nodeField = ReflectionUtils.getAnnotatedField(object.getClass(),
				UnderlyingNode.class);
		if (nodeField != null) {
			// create UnderlyingNode
			Node node = neo.createNode();
			ObjectId idValue = ReflectionUtils.getIdValue(object);
			node.setProperty(MONGO_ID, idValue.toString());
			mongoIdIndex.add(node, MONGO_ID, idValue.toString());
			node.createRelationshipTo(getClassNode(object.getClass()),
					Relation.HAS_CLASS);

			// check for NodeReferences
			Iterable<Field> nodeReferenceFields = ReflectionUtils
					.getAnnotatedFields(object.getClass(), NodeReference.class);
			for (Field nodeReferenceField : nodeReferenceFields) {
				nodeReferenceField.setAccessible(true);
				Object nodeReferenceFieldValue = nodeReferenceField.get(object);
				if (nodeReferenceFieldValue != null) {
					if (nodeReferenceFieldValue instanceof Iterable) {
						// TODO avoid unchecked cast
						for (V obj : (Iterable<V>) nodeReferenceFieldValue) {
							obj = save(obj);
							Node referencedNode = getNode(obj);
							if (referencedNode == null) {
								throw new RuntimeException(
										"Referenced Classes must have a "
												+ Node.class.getName()
												+ "-property annotated with "
												+ UnderlyingNode.class
														.getName());
							}
							node.createRelationshipTo(referencedNode,
									DynamicRelationshipType
											.withName(nodeReferenceField
													.getName()));
						}
					}
					// handle case of single object references
				}
			}
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
	public <T, V> Iterable<T> find(Class<T> clazz) {
		List<T> results = new ArrayList<T>();
		Field nodeField = ReflectionUtils.getAnnotatedField(clazz,
				UnderlyingNode.class);
		Iterable<Field> nodeRefFields = ReflectionUtils.getAnnotatedFields(
				clazz, NodeReference.class);
		for (T obj : ds.find(clazz).asList()) {
			try {
				// ObjectId idValue = getIdValue(obj);
				// Node node = mongoIdIndex.get("mongoId", idValue.toString())
				// .getSingle();
				// injectNode(obj, node, nodeField);
				LazyNodeLookup<T> lazyNodeLookup = new LazyNodeLookup<T>(
						mongoIdIndex, obj);
				injectNode(obj, lazyNodeLookup, nodeField);

				// TODO add LazyList to referenced nodes
				for (Field nodeRefField : nodeRefFields) {
					Class<?> referenceType = nodeRefField.getAnnotation(
							NodeReference.class).type();
					nodeRefField.setAccessible(true);
					Object list = nodeRefField.get(obj);
					if (list instanceof List) {
						LazyObjectList lazyObjectIterable = new LazyObjectList(
								lazyNodeLookup, nodeRefField.getName(), ds,
								referenceType);
						nodeRefField.set(obj, lazyObjectIterable);
					}

				}

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

	private <T> Node getNode(T obj) throws IllegalArgumentException,
			IllegalAccessException {
		Field nodeField = ReflectionUtils.getAnnotatedField(obj.getClass(),
				UnderlyingNode.class);
		if (nodeField != null) {
			nodeField.setAccessible(true);
			return (Node) nodeField.get(obj);
		}
		return null;
	}

	public void get(ObjectId mongoId) {

	}

}
