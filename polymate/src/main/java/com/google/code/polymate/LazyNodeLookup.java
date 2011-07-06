package com.google.code.polymate;

import org.bson.types.ObjectId;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;

class LazyNodeLookup<T> implements Node {

	transient Index<Node> mongoIdIndex;
	transient T parent;
	private Node node;

	public LazyNodeLookup(Index<Node> mongoIdIndex, T parent) {
		this.mongoIdIndex = mongoIdIndex;
		this.parent = parent;
	}

	private Node node() {
		if (node == null) {
			ObjectId idValue = ReflectionUtils.getIdValue(parent);
			node = mongoIdIndex.get("mongoId", idValue.toString()).getSingle();
		}
		return node;
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return node().getGraphDatabase();
	}

	@Override
	public boolean hasProperty(String key) {
		return node().hasProperty(key);
	}

	@Override
	public Object getProperty(String key) {
		return node().getProperty(key);
	}

	@Override
	public Object getProperty(String key, Object defaultValue) {
		return node().getProperty(key, defaultValue);
	}

	@Override
	public void setProperty(String key, Object value) {
		node().setProperty(key, value);
	}

	@Override
	public Object removeProperty(String key) {
		return node().removeProperty(key);
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return node().getPropertyKeys();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Iterable<Object> getPropertyValues() {
		return node().getPropertyValues();
	}

	@Override
	public long getId() {
		return node().getId();
	}

	@Override
	public void delete() {
		node().delete();
	}

	@Override
	public Iterable<Relationship> getRelationships() {
		return node().getRelationships();
	}

	@Override
	public boolean hasRelationship() {
		return node().hasRelationship();
	}

	@Override
	public Iterable<Relationship> getRelationships(RelationshipType... types) {
		return node().getRelationships(types);
	}

	@Override
	public Iterable<Relationship> getRelationships(Direction direction,
			RelationshipType... types) {
		return node().getRelationships(direction, types);
	}

	@Override
	public boolean hasRelationship(RelationshipType... types) {
		return node().hasRelationship(types);
	}

	@Override
	public boolean hasRelationship(Direction direction,
			RelationshipType... types) {
		return node().hasRelationship(direction, types);
	}

	@Override
	public Iterable<Relationship> getRelationships(Direction dir) {
		return node().getRelationships(dir);
	}

	@Override
	public boolean hasRelationship(Direction dir) {
		return node().hasRelationship(dir);
	}

	@Override
	public Iterable<Relationship> getRelationships(RelationshipType type,
			Direction dir) {
		return node().getRelationships(type, dir);
	}

	@Override
	public boolean hasRelationship(RelationshipType type, Direction dir) {
		return node().hasRelationship(type, dir);
	}

	@Override
	public Relationship getSingleRelationship(RelationshipType type,
			Direction dir) {
		return node().getSingleRelationship(type, dir);
	}

	@Override
	public Relationship createRelationshipTo(Node otherNode,
			RelationshipType type) {
		return node().createRelationshipTo(otherNode, type);
	}

	@Override
	public Traverser traverse(Order traversalOrder,
			StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator,
			RelationshipType relationshipType, Direction direction) {
		return node().traverse(traversalOrder, stopEvaluator,
				returnableEvaluator, relationshipType, direction);
	}

	@Override
	public Traverser traverse(Order traversalOrder,
			StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator,
			RelationshipType firstRelationshipType, Direction firstDirection,
			RelationshipType secondRelationshipType, Direction secondDirection) {
		return node().traverse(traversalOrder, stopEvaluator,
				returnableEvaluator, firstRelationshipType, firstDirection,
				secondRelationshipType, secondDirection);
	}

	@Override
	public Traverser traverse(Order traversalOrder,
			StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator,
			Object... relationshipTypesAndDirections) {
		return node().traverse(traversalOrder, stopEvaluator,
				returnableEvaluator, relationshipTypesAndDirections);
	}

	@Override
	public int hashCode() {
		return node().hashCode();
	}

	@Override
	public String toString() {
		return "" + node;
	}

	@Override
	public boolean equals(Object obj) {
		return node().equals(obj);
	}

}
