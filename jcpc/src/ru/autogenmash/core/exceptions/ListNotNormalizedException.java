package ru.autogenmash.core.exceptions;

/**
 * Возникает, при попытке компиляции не нормализованного CPList-а.
 * 
 * @author Dymarchuk Dmitry
 * @version 11.05.2007 16:15:36
 */
public class ListNotNormalizedException extends Exception {
	private static final long serialVersionUID = -3779979158085691823L;

	/**
	 * @param message
	 */
	public ListNotNormalizedException(String message) {
		super(message);
	}
}
