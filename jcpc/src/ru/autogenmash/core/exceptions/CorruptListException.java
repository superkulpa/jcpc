package ru.autogenmash.core.exceptions;

/**
 * Возникает, когда УП имеет недопустимые символы на определенном этапе.
 * 
 * @author Dymarchuk Dmitry
 * @version 11.05.2007 16:11:44
 */
public class CorruptListException extends Exception {
	private static final long serialVersionUID = 6378565813739201444L;

	public CorruptListException(String message) {
		super(message);
	}

}
