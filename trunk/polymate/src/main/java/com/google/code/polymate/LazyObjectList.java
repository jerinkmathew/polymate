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
	private List<referenceType> objList = new ArrayList<referenceType>();

	public LazyObjectList(LazyNodeLookup<?> lazyNodeLookup,
			String relationshipName, Datastore ds, Class<?> referenceType) {
		this.lazyNodeLookup = lazyNodeLookup;
		this.relationshipName = relationshipName;
		this.ds = ds;
		this.referenceType = referenceType;
	}

	private List<referenceType> list() {
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
		return objList;
	}

	@Override
	public Iterator<referenceType> iterator() {
		return list().iterator();
	}

	@Override
	public boolean add(referenceType e) {
		return list().add(e);
	}

	@Override
	public void add(int index, referenceType element) {
		list().add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends referenceType> c) {
		return list().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends referenceType> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		list().clear();
	}

	@Override
	public boolean contains(Object o) {
		return list().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list().containsAll(c);
	}

	@Override
	public referenceType get(int index) {
		return list().get(index);
	}

	@Override
	public int indexOf(Object o) {
		return list().indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list().isEmpty();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list().lastIndexOf(o);
	}

	@Override
	public ListIterator<referenceType> listIterator() {
		return list().listIterator();
	}

	@Override
	public ListIterator<referenceType> listIterator(int index) {
		return list().listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return list().remove(o);
	}

	@Override
	public referenceType remove(int index) {
		return list().remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list().retainAll(c);
	}

	@Override
	public referenceType set(int index, referenceType element) {
		return list().set(index, element);
	}

	@Override
	public int size() {
		return list().size();
	}

	@Override
	public List<referenceType> subList(int fromIndex, int toIndex) {
		return list().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list().toArray(a);
	}

}
