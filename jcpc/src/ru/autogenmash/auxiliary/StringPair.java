package ru.autogenmash.auxiliary;

/**
 * @author Dymarchuk Dmitry 06.02.2008 10:29:42
 */
public class StringPair {

	private String _value1;
	private String _value2;

	public StringPair(String value1, String value2) {
		_value1 = value1;
		_value2 = value2;
	}

	public String toString() {
		return _value1 + " = " + _value2;
	}

	public String getValue1() {
		return _value1;
	}

	public String getValue2() {
		return _value2;
	}

	public void setValue1(String value1) {
		_value1 = value1;
	}

	public void setValue2(String value2) {
		_value2 = value2;
	}

	public void reset() {
		_value1 = null;
		_value2 = null;
	}
}
