package com.google.code.polymate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bson.types.ObjectId;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.code.morphia.Datastore;

class LazyObjectList<referenceType> implements List<referenceType> {

	transient LazyNodeLookup<?> lazyNodeLookup;
	transient String relationshipName;
	transient Datastore ds;
	transient Class<?> referenceType;

	public LazyObjectList(LazyNodeLookup<?> lazyNodeLookup,
			String relationshipName, Datastore ds, Class<?> referenceType) {
		this.lazyNodeLookup = lazyNodeLookup;
		this.relationshipName = relationshipName;
		this.ds = ds;
		this.referenceType = referenceType;
	}

	@Override
	public Iterator<referenceType> iterator() {
		List<referenceType> objList = new ArrayList<referenceType>();
		Iterable<Relationship> relationships = lazyNodeLookup
				.getRelationships(DynamicRelationshipType
						.withName(relationshipName));
		for (Relationship rel : relationships) {
			Node otherNode = rel.getOtherNode(lazyNodeLookup);
			String mongoId = (String) otherNode.getProperty(Polymate.MONGO_ID);
			referenceType obj = (referenceType) ds.get(referenceType,
					new ObjectId(mongoId));
			objList.add(obj);
		}
		return objList.iterator();
	}

	@Override
	public boolean add(referenceType e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(int index, referenceType element) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addAll(Collection<? extends referenceType> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends referenceType> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public referenceType get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<referenceType> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<referenceType> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public referenceType remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public referenceType set(int index, referenceType element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<referenceType> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

}
