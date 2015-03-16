package app.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BeanUtils {
	public static void setValue(boolean superClass, Object obj,
			String fieldName, Object value) throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = (superClass ? obj.getClass().getSuperclass() : obj
				.getClass()).getDeclaredField(fieldName);
		boolean a = field.isAccessible();
		try {
			field.setAccessible(true);
			field.set(obj, value);
		} finally {
			field.setAccessible(a);
		}
	}

	public static Object invoke(boolean superClass, Object obj,
			String methodName, Object... args) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Method method = (superClass ? obj.getClass().getSuperclass() : obj
				.getClass()).getDeclaredMethod(methodName);
		boolean a = method.isAccessible();
		try {
			method.setAccessible(true);
			return method.invoke(obj, args);
		} finally {
			method.setAccessible(a);
		}
	}

	public static Object getValue(boolean superClass, Object obj,
			String fieldName) throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Field field = (superClass ? obj.getClass().getSuperclass() : obj
				.getClass()).getDeclaredField(fieldName);
		boolean a = field.isAccessible();
		try {
			field.setAccessible(true);
			return field.get(obj);
		} finally {
			field.setAccessible(a);
		}
	}
}
