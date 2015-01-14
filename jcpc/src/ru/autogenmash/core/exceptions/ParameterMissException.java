package ru.autogenmash.core.exceptions;

/**
 * Возникает при отсутсвии параметра в хранилище.
 * 
 * @author Dymarchuk Dmitry
 * @version 18.05.2007 15:34:25
 */
public class ParameterMissException extends Exception {
	private static final long serialVersionUID = -1546413842287962254L;

	public ParameterMissException() {
	}

	/**
	 * @param message
	 */
	public ParameterMissException(String message) {
		super(message);
	}
}
