package ru.autogenmash.core.exceptions;

/**
 * @author Dymarchuk Dmitry 27.03.2009 15:17:42
 */
public class InvalidFileFormatException extends Exception {

	private static final long serialVersionUID = -2612145713790743239L;

	public InvalidFileFormatException() {
	}

	public InvalidFileFormatException(String message) {
		super(message);
	}

//	public InvalidFileFormatException(Throwable cause) {
//		super(cause);
//	}
//
//	public InvalidFileFormatException(String message, Throwable cause) {
//		super(message, cause);
//	}

}
