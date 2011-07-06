package com.google.code.polymate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;

public class ReflectionUtils {

	private ReflectionUtils() {
		throw new AssertionError("Do not instantiate!");
	}

	/**
	 * TODO Javadoc.
	 * 
	 * @param <T>
	 * @param object
	 * @return
	 */
	public static <T> ObjectId getIdValue(T object) {
		Field idField = getAnnotatedField(object.getClass(), Id.class);
		if (idField == null) {
			throw new RuntimeException(
					"No @id-field is present for the given object");
		}
		try {
			idField.setAccessible(true);
			return (ObjectId) idField.get(object);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("The @id-field should have the type "
					+ ObjectId.class.getName());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * TODO Javadoc. Only returns the first field found for the given
	 * annotation. To retrieve all fields of a class annotated with the given
	 * annotation, use getAnnotatedFields instead.
	 * 
	 * @param <T>
	 * @param clazz
	 * @param annotation
	 * @return
	 */
	public static <T> Field getAnnotatedField(Class<?> clazz,
			Class<? extends Annotation> annotation) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @param <T>
	 * @param clazz
	 * @param annotation
	 * @return
	 */
	public static <T> Iterable<Field> getAnnotatedFields(Class<?> clazz,
			Class<? extends Annotation> annotation) {
		List<Field> annotededFields = new ArrayList<Field>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation)) {
				annotededFields.add(field);
			}
		}
		return annotededFields;
	}

}
