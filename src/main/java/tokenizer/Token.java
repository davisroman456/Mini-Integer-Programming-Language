package tokenizer;

/**
 * An immutable pairing of a {@link TokenType} and the raw substring it was scanned from,
 * produced by {@link Tokenizer#tokenize(String)} and consumed by {@link parser.Parser}.
 */
public class Token {
	private TokenType type;
	private String value;
	
	/**
	 * @param type the category this token belongs to
	 * @param value the exact substring of the original statement this token was scanned from
	 */
	public Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
		
	}
	
	// Getters
	public TokenType getTokenType() {
		return this.type;
	}
	
	public String getValue() {
		return this.value;	
	}
	
}
