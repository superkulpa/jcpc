package ru.autogenmash.macros.cometmacros;

//import java.util.Iterator;
import java.util.Vector;

// класс описывающий параметры макроса
public class JMacrosParameters {

	private Vector instance;

	public JMacrosParameters() {
		// конструктор
		instance = new Vector();
	}

	public void Clear() {
		// очистить список
		instance.clear();
	}

	private boolean CheckForExist(String _name) {
		// проверка на существование параметра с именем _name
		// true - если параметр уже существует, false - иначе

		// Iterator iter = instance.iterator();

		for (int i = 0; i < GetLength(); i++)
			if (GetParameterName(i) == _name)
				return true;

		return false;
	}

	private String GetParameterName(int _position) {
		// возвращает имя параметра расположенного на заданной позиции
		return ((JMacrosParametersItem) instance.get(_position)).GetName();
	}

	private int GetParameterPosition(String _name) {
		// возвращает позицию параметра по его имени, -1 если такого нет
		int i;
		boolean hasParameter = false;
		for (i = 0; i < GetLength(); i++) {
			if (GetParameterName(i) == _name) {
				hasParameter = true;
				break;
			}
		}
		if (hasParameter)
			return i;
		else
			try {
				throw new Exception("WARNING: Parameter with name = '" + _name
						+ "' does not exist in list.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
	}

	public int GetLength() {
		// возвращает длину листа
		return instance.size();
	}

	public boolean AddParameter(String _name, int _value) {
		// добавить параметр true - если параметр добавлен успешно, false -
		// иначе
		if (_name != "") {
			if (!CheckForExist(_name)) {
				JMacrosParametersItem _item = new JMacrosParametersItem(_name,
						_value);
				instance.add(_item);
				return true;
			} else
				try {
					throw new Exception("ERROR! Parameter with name '" + _name
							+ "' already exist!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		} else
			try {
				throw new Exception(
						"ERROR! Parameter name can not be empty string!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return false;
	}

	public int GetValue(String _name) {
		// возвращает значение заданного параметра
		return ((JMacrosParametersItem) instance
				.get(GetParameterPosition(_name))).GetValue();
	}

	public int GetValue(int _position) {
		// возвращает значение параметра на заданной позиции
		return ((JMacrosParametersItem) instance.get(_position)).GetValue();
	}

	public String GetName(int _position) {
		// возвращает имя параметра на заданной позиции
		return ((JMacrosParametersItem) instance.get(_position)).GetName();
	}

	/** ************************************************************************************************************* */
	/**
	 * **************************** Вспомогательные данные
	 * ************************************
	 */
	/** ************************************************************************************************************* */

	// описывает один параметр макроса
	public class JMacrosParametersItem {

		private String name;

		private int value;

		public JMacrosParametersItem(String _name, int _value) {
			// конструктор
			name = _name;
			value = _value;
		}

		public String GetName() {
			// возвращает имя параметра макроса
			return name;
		}

		public int GetValue() {
			// возвращает значение параметра макроса
			return value;
		}
	}

}
