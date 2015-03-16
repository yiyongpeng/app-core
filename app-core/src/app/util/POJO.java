package app.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单数据对象
 * 
 * @author yiyongpeng
 * 
 */
public abstract class POJO {

	@Override
	public String toString() {
		return toString(this);
	}

	public static String toString(Object obj) {
		return toString(obj, new HashMap<Object, Object>(2));
	}

	public static String toString(Object obj, Map<Object, Object> already) {
		already.put(obj, null);
		StringBuffer sb = new StringBuffer("{");
		Map<Object, Object> thisAlready = new HashMap<Object, Object>();
		toString(obj, obj.getClass(), sb, already, thisAlready);
		sb.append("}");
		return sb.toString();
	}

	public static void toString(Object obj, Class<?> clazz, StringBuffer sb,
			Map<Object, Object> already, Map<Object, Object> thisAlready) {
		if (clazz == null)
			return;
		if (clazz.isArray() || clazz.isPrimitive()) {
			appendValue(obj, sb, already);
			return;
		}
		String name = null;
		Object value = null;
		Method[] fields = clazz.getMethods();
		try {
			for (Method method : fields)
				if (method.getParameterTypes().length == 0
						&& method.getReturnType() != void.class
						&& method.getModifiers() != Modifier.STATIC) {
					name = method.getName();

					// 排除当前对象的重复方法
					if (thisAlready.containsKey(name))
						continue;
					thisAlready.put(name, null);

					if (name.startsWith("get")) {
						if ((value = method.invoke(obj)) != obj) {
							if (value != null && already.containsKey(value)) {
								continue;
							}
							if (value != null && value instanceof POJO) {
								already.put(value, null);
							}
							if (sb.length() > 1)
								sb.append(", ");
							sb.append(name.substring(3, 4).toLowerCase());
							sb.append(name.substring(4));
							sb.append("=");
							if (value == null) {
								sb.append("null");
							} else {
								appendValue(value, sb, already);
							}
						}
					} else if (name.startsWith("is")
							&& (method.getReturnType() == boolean.class || method
									.getReturnType() == Boolean.class)) {
						value = method.invoke(obj);
						if (sb.length() > 1)
							sb.append(", ");
						sb.append(name.substring(2, 3).toLowerCase());
						sb.append(name.substring(3));
						sb.append("=");
						sb.append(value);
					}
				}
			toString(obj, clazz.getSuperclass(), sb, already, thisAlready);
			toString(obj, clazz.getInterfaces(), sb, already, thisAlready);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void appendValue(Object value, StringBuffer sb,
			Map<Object, Object> already) {
		boolean array = value != null ? !(value instanceof Class)
				&& value.getClass().isArray() : false;
		boolean flag = value != null && !(value instanceof POJO)
				&& array == false;
		if (flag)
			sb.append("\"");
		if (array) {
			Class<?> clazz = value.getClass();
			if (clazz == boolean[].class) {
				boolean[] arry = (boolean[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == char[].class) {
				char[] arry = (char[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == byte[].class) {
				byte[] arry = (byte[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == short[].class) {
				short[] arry = (short[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == int[].class) {
				int[] arry = (int[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == long[].class) {
				long[] arry = (long[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == float[].class) {
				float[] arry = (float[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == double[].class) {
				double[] arry = (double[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == String[].class) {
				String[] arry = (String[]) value;
				sb.append(Arrays.toString(arry));
			} else if (value instanceof Object[]) {
				Object[] arry = (Object[]) value;
				sb.append("[");
				for (int i = 0; i < arry.length; i++) {
					if (i > 0)
						sb.append(", ");
					appendValue(arry[i], sb, already);
				}
				sb.append("]");
			} else {
				sb.append(value.toString());
			}
		} else if (value instanceof POJO) {
			sb.append(toString(value, already));
		} else {
			sb.append(value);
		}
		if (flag)
			sb.append("\"");
	}

	public static void toString(Object obj, Class<?>[] clases, StringBuffer sb,
			Map<Object, Object> already, Map<Object, Object> thisAlready) {
		for (Class<?> clazz : clases)
			toString(obj, clazz, sb, already, thisAlready);
	}

}
