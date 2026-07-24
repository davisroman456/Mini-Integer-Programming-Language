package tokenizer;

/** The category of a {@link Token}, used by the parser to determine grammar rules and precedence. */
public enum TokenType {
	/** A variable name, e.g. {@code x}. */
	IDENTIFIER,
	/** An integer literal, e.g. {@code 42}. */
	NUMBER,
	/** The {@code +} operator. */
	PLUS,
	/** The {@code -} operator, used for both subtraction and unary negation. */
	MINUS,
	/** The {@code *} operator. */
	MULTIPLY,
	/** The {@code /} operator. */
	DIVIDE,
	/** The {@code %} operator. */
	MODULO,
	/** The {@code =} assignment operator. */
	EQUALS,
	/** The {@code PRINT} keyword. */
	PRINT,
	/** An invalid/unrecognized character sequence, causing parsing to fail immediately. */
	ERROR
}