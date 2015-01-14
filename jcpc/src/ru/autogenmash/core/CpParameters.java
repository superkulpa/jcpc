package ru.autogenmash.core;

import java.util.Vector;

import ru.autogenmash.core.utils.compiler.Compiler;

/**
 * Набор параметров.
 * 
 * @author Dymarchuk Dmitry
 * @version unknown
 *          <p>
 *          refactor 06.07.2007 10:09:17
 */
public class CpParameters extends Vector {

	// protected Vector _storage;

	private static final long serialVersionUID = 6211240583116278106L;

	/**
	 * Проверка на существование параметра с именем <b>name</b>. true - если
	 * параметр уже существует, false - иначе.
	 * 
	 * @param name
	 * @return
	 */
	public boolean hasParam(String name) {
		for (int i = 0; i < size(); i++)
			if (getName(i) == name)
				return true;

		return false;
	}

	/**
	 * Возвращает позицию параметра по его имени, -1 если такого нет.
	 * 
	 * @param name
	 * @return
	 */
	public int getParameterPosition(String name) {
		int i;
		boolean hasParameter = false;
		for (i = 0; i < size(); i++) {
			if (getName(i) == name) {
				hasParameter = true;
				break;
			}
		}
		if (hasParameter)
			return i;
		else
			return -1;
	}

	public synchronized boolean add(Object obj) {
		if (obj instanceof CpParametersItem == false)
			throw new IllegalArgumentException("\"" + obj
					+ "\" must be instance of \"" + CpParametersItem.class.getName()
					+ "\"");

		CpParametersItem parameter = (CpParametersItem) obj;
		Object value = checkValue(parameter.getValue());

		if (hasParam(parameter.getName()) == false)
			return add(parameter.getName(), value, parameter.getDescription());

		return false;
	}

	public boolean add(String name, Object value, String description) {
		value = checkValue(value);
		return super.add(new CpParametersItem(name, value, description));
	}

	private Object checkValue(Object value) {
		if (value instanceof Integer) {
			int number = ((Integer) value).intValue();
			value = new Integer(number * Compiler.SIZE_TRANSFORMATION_RATIO);
		} else if (value instanceof Double) {
			double number = ((Double) value).doubleValue();
			value = new Double(number * Compiler.SIZE_TRANSFORMATION_RATIO);
		} else if (value instanceof Long) {
			long number = ((Long) value).longValue();
			value = new Long(number * Compiler.SIZE_TRANSFORMATION_RATIO);
		} else if (value instanceof Float) {
			float number = ((Float) value).floatValue();
			value = new Float(number * Compiler.SIZE_TRANSFORMATION_RATIO);
		}
		return value;
	}

	/**
	 * Возвращает значение параметра по имени.
	 * 
	 * @param name
	 * @return
	 */
	public Object getValue(String name) {
		return ((CpParametersItem) get(getParameterPosition(name))).getValue();
	}

	/**
	 * Возвращает значение параметра на заданной позиции.
	 * 
	 * @param position
	 * @return
	 */
	public Object getValue(int position) {
		return ((CpParametersItem) get(position)).getValue();
	}

	/**
	 * Возвращает имя параметра на заданной позиции.
	 * 
	 * @param position
	 * @return
	 */
	public String getName(int position) {
		return ((CpParametersItem) get(position)).getName();
	}

	/**
	 * Возвращает описание параметра по имени.
	 * 
	 * @param position
	 * @return
	 */
	public String getDescription(String name) {
		if (getParameterPosition(name) >= 0)
			return ((CpParametersItem) get(getParameterPosition(name)))
					.getDescription();
		else
			return null;
	}

	/**
	 * Возвращает описание параметра на заданной позиции.
	 * 
	 * @param position
	 * @return
	 */
	public String getDescription(int position) {
		return ((CpParametersItem) get(position)).getDescription();
	}

	public static class CpParametersItem {
		private String _name;
		private Object _value;
		private String _description;

		public CpParametersItem(String name, Object value, String description) {
			_name = name;
			_value = value;
			_description = description;
		}

		public CpParametersItem(String name, Object value) {
			this(name, value, "");
		}

		public String getName() {
			return _name;
		}

		public Object getValue() {
			return _value;
		}

		public String getDescription() {
			return _description;
		}
	}

}
