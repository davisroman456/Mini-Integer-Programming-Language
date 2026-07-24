package exception;

/**
 * Thrown for any recognized language-level error — invalid statements, tokenizing failures,
 * arithmetic errors (e.g. division by zero), or references to undefined variables — as opposed to
 * unexpected internal bugs, which should surface as unchecked exceptions of other types.
 */
@SuppressWarnings("serial")
public class LanguageException extends RuntimeException {

	public LanguageException() {
		// TODO Auto-generated constructor stub
		super("Statement Error: Issue found when tokenizing or parsing statement!!");
	}

	public LanguageException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LanguageException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LanguageException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public LanguageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
