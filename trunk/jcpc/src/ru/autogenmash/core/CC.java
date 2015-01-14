package ru.autogenmash.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.TreeMap;

import ru.autogenmash.core.utils.StringUtils;

/**
 * Управляющая команда.
 */
public class CC implements Cloneable {

	public static final int X = 101;
	public static final int Y = 102;
	public static final int Z = 103;
	public static final int C = 104;
	public static final int A = 105;
	// public static final int P = 105;
	// public static final int U = 106;
	// public static final int W = 107;
	public static final int M = 108;
	public static final int G = 110;
	public static final int D = 111;
	public static final int F = 112;
	// public static final int FILTER = 115;
	// public static final int USER = 116; // просто любой символ(ы)
	// public static final int PARK = 117;
	public static final int N = 118;
	public static final int I = 119;
	public static final int J = 120;
	public static final int T = 121;
	// public static final int V = 122;
	public static final int H = 123;
	// public static final int FCORRECTION = 124;
	public static final int L = 125;
	public static final int R = 126;
	// public static final int S = 127;
	public static final int INFO = 128;
	public static final int SUB = 129;
	public static final int K = 130;
	
	public static final int[] GEO_COMMANDS = { X, Y, I, J, A/* , Z, C, P, U, W */};
	public static final int[] CUT_ON_COMMANDS = { 71, 72, 73, 78, 81, 82 };
	public static final int[] CUT_OFF_COMMANDS = { 74, 75, 79, 83, 84, 85 };

	private static final Field[] PUBLIC_FIELDS = CC.class.getFields();
	private static final TreeMap PUBLIC_FIELDS_MAP = new TreeMap();
	private static final TreeMap PUBLIC_FIELDS_MAP_INVERTED = new TreeMap();

	static {
		Arrays.sort(GEO_COMMANDS);
		Arrays.sort(CUT_ON_COMMANDS);
		Arrays.sort(CUT_OFF_COMMANDS);

		try {
			for (int i = 0; i < PUBLIC_FIELDS.length; i++) {
				Field field = PUBLIC_FIELDS[i];
				String name = field.getName();
				if (name.indexOf("COMMAND") >= 0)
					continue;
				int value = field.getInt(name);
				if (value < 100 || value > 199)
					continue;
				Integer index = new Integer(value);
				PUBLIC_FIELDS_MAP.put(name, index);
				PUBLIC_FIELDS_MAP_INVERTED.put(index, name);
			}
		} catch (Throwable e) {
			if (e instanceof IllegalArgumentException == false)
				e.printStackTrace();
		}
	}

	public static TreeMap getPublicFieldsMap() {
		return PUBLIC_FIELDS_MAP;
	}

	public static TreeMap getPublicFieldsMapInverted() {
		return PUBLIC_FIELDS_MAP_INVERTED;
	}

	/**
	 * Получить имя управляющей команды по ее типу.
	 * 
	 * @param command
	 * @return Имя команды или null если команды с заданным типом не существует.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String getCommandName(int command) {
		for (int i = 0; i < PUBLIC_FIELDS.length; i++) {
			try {
				if (PUBLIC_FIELDS[i].getInt(null) == command)
					return PUBLIC_FIELDS[i].getName();
			} catch (IllegalArgumentException e) {
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}
		}

		return null;
	}

	public static int getType(String commandName) {
		return ((Integer) PUBLIC_FIELDS_MAP.get(commandName)).intValue();
	}

	public static String getName(int type) {
		return (String) PUBLIC_FIELDS_MAP_INVERTED.get(new Integer(type));
	}

	private int _type;
	private int _data;
	private String _description;

	public CC(int type, int data, String description) {
		_type = type;
		_data = data;
		_description = description;
	}

	public CC(int type, int data) {
		this(type, data, getName(type));
	}

	public CC(String type, int data, String description) {
		this(((Integer) PUBLIC_FIELDS_MAP.get(type.toUpperCase())).intValue(),
				data, description);
	}

	public Object clone() throws CloneNotSupportedException {
		return new CC(_type, _data, _description);
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CC == false)
			return false;

		CC cc2 = (CC) obj;
		return _type == cc2.getType() & _data == cc2.getData();
	}

	public String toString() {
		return generateCPString();
	}

	public int getType() {
		return _type;
	}

	public void setType(int type) {
		_type = type;
	}

	public boolean isGeo() {
		return (Arrays.binarySearch(GEO_COMMANDS, _type) >= 0);
	}

	public boolean isCutOn() {
		return (_type == M) && (Arrays.binarySearch(CUT_ON_COMMANDS, _data) >= 0);
	}

	public boolean isCutOff() {
		return (_type == M) && (Arrays.binarySearch(CUT_OFF_COMMANDS, _data) >= 0);
	}

	public int getData() {
		return _data;
	}

	public void setData(int data) {
		_data = data;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * Проверить является ли CC нулевым перемещением или нет.
	 * 
	 * @return
	 */
	public boolean checkFor0Movement() {
		if (Arrays.binarySearch(GEO_COMMANDS, _type) >= 0) {
			if (_data == 0)
				return true;
		}
		return false;
	}

	/**
	 * Представить управляющую команду в виде строки.
	 * 
	 * @return
	 */
	public String generateCPString() {
		String res = "";

		switch (_type) {
		// case PARK:
		// if (_description == null)
		// throw new FilterException("Park value is null");
		// if (_description.trim().equals(""))
		// throw new FilterException("Park value is undefined");
		// res += _description;
		// break;
		// case FILTER:
		// if (_description == null)
		// throw new FilterException("Filter value is null");
		// if (_description.trim().equals(""))
		// throw new FilterException("Filter value is undefined");
		// res += _description;
		// break;
		// case USER:
		// res += _description;
		// break;
		// case R:
		// res += "R" + _description;
		// break;
		case L:
			res += "L" + _description;
			break;
		case INFO:
      res += "INFO" + _description;
      break;
		case G:
			res += "G" + StringUtils.extractNumber(_data, 2, '0');
			break;
		default:
			res += getCommandName(_type) + _data;
		}

		return res;
	}

	public void scale(double k) {
		if (isGeo())
			_data *= k;
	}

	public void invert() {
		_data = -_data;
	}

}
