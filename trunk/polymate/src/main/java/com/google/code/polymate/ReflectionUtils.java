package com.google.code.polymate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;

public class ReflectionUtils {

	private ReflectionUtils() {
		throw new AssertionError("Do not instantiate!");
	}

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

	public static <T> Field getAnnotatedField(Class<?> clazz,
			Class<? extends Annotation> annotation) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotation)) {
				return field;
			}
		}
		return null;
	}

}
