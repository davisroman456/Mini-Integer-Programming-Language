package tokenizer;

import java.util.ArrayList;

import exception.LanguageException;

public class Tokenizer {
	
	/** 
	 * Method seeks to tokenize a statement into an ArrayList for interpreting.
	 * The method properly goes through a statement that was input and breaks it down into tokens to be parsed.
	 * 
	 * @param statement - A statement, of type 'String', that will be tokenized
	 * @return An ArrayList of tokens broken down from the statement
	 */
	public static ArrayList<Token> tokenize(String statement) {
		if(statement.isEmpty())
			throw new LanguageException("Statement Error: Empty statement.");
		ArrayList<Token> tokens = new ArrayList<>();
		if(isLetter(statement, 0)) { // Checks if statement is valid to begin with
			int scanIndx = 0;
			
			while(scanIndx < statement.length()) { // Iterate through the whole statement
				String tokenString = "";
				Token token = null;
				String currCharacter = statement.substring(scanIndx, scanIndx + 1);
				if(isLetter(statement, scanIndx)) { // If it starts with a letter it is an "IDENTIFIER"
					int startIndx = scanIndx;
					do {
						scanIndx++;
					}while(scanIndx < statement.length() && isLetterOrDigit(statement,scanIndx)); 
					// Keep going until a character that cannot belong to an IDENTIFIER is reached or end of statement
					
					tokenString = statement.substring(startIndx, scanIndx);
					
					if(tokenString.equals("PRINT")) // If this identifier matches the "PRINT" keyword, create "PRINT" type token instead of "IDENTIFIER"
						token = new Token(TokenType.PRINT, "PRINT");
					else
						token = new Token(TokenType.IDENTIFIER, tokenString);
					
				} else if(isDigit(statement, scanIndx)){ // If it starts with a digit it is an "NUMBER"
					int startIndx = scanIndx;
					do {
						scanIndx++;
					}while(scanIndx < statement.length() && isDigit(statement,scanIndx));
					// Keep going until a character that cannot belong to an NUMBER is reached or end of statement
					
					tokenString = statement.substring(startIndx, scanIndx);
					token = new Token(TokenType.NUMBER, tokenString);
					
				} else if(currCharacter.equals("+")) { // Add "PLUS" token
					scanIndx++;
					token = new Token(TokenType.PLUS, "+");
					
				} else if(currCharacter.equals("-")) { // Add "MINUS" token
					scanIndx++;
					token = new Token(TokenType.MINUS, "-");
					
				} else if(currCharacter.equals("*")) { // Add "MULTIPLY" token
					scanIndx++;
					token = new Token(TokenType.MULTIPLY, "*");
					
				} else if(currCharacter.equals("/")) { // Add "DIVIDE" token
					scanIndx++;
					token = new Token(TokenType.DIVIDE, "/");
					
				} else if(currCharacter.equals("%")) { // Add "MODULO" token
					scanIndx++;
					token = new Token(TokenType.MODULO, "%");
					
				} else if(currCharacter.equals("=")) { // Add "EQUALS" token
					scanIndx++;
					token = new Token(TokenType.EQUALS, "=");
					
				} else { // Try to catch for any errors
					if(isErrorChar(statement, scanIndx)) {
						// So if the character is not a space (" ") or any proper token above, create an "ERROR" token
						int startIndx = scanIndx;
						do {
							scanIndx++;
						}while(scanIndx < statement.length() && isErrorChar(statement, scanIndx));
						// Keep going until a character that cannot belong to an ERROR is reached or end of statement
						
						tokenString = statement.substring(startIndx, scanIndx);
						token = new Token(TokenType.ERROR, tokenString);
						
					}else
						scanIndx++; // If all is fine just skip to the next index
				}
				
				if(token != null) { // If a token was made, add it to the list
					tokens.add(token);
					
				}
			} // While loop end
		}else {
			throw new LanguageException("Statement Error: Statement must be either a PRINT statement or assignment statement AND statement must not begin with an invalid character!!");
		}
		
		return tokens;
	}
	
	/**
	 * Method returns whether the character at the specific index in the statement is a "letter".
	 * Examples: "a", "B", "c"
	 * @param statement - the statement
	 * @param index - the specific index of the character in the statement
	 * @return whether the character at the specific index in the statement is a "letter" or not
	 * */
	private static boolean isLetter(String statement, int index) {
		
		char letter = statement.charAt(index);
		return Character.isLetter(letter);
	}
	
	/**
	 * Method returns whether the character at the specific index in the statement is a "letter" OR "digit".
	 * Examples: "6", "B", "c", "0"
	 * @param statement - the statement
	 * @param index - the specific index of the character in the statement
	 * @return whether the character at the specific index in the statement is a "letter" OR "digit" or not
	 * */
	private static boolean isLetterOrDigit(String statement, int index) {
		
		char letNum = statement.charAt(index);
		return Character.isLetterOrDigit(letNum);
	}
	
	/**
	 * Method returns whether the character at the specific index in the statement is a "number"
	 * Examples: "0", "5", "9"
	 * @param statement - the statement
	 * @param index - the specific index of the character in the statement
	 * @return whether the character at the specific index in the statement is a "number" or not
	 * */
	private static boolean isDigit(String statement, int index) {
		
		char number = statement.charAt(index);
		return Character.isDigit(number);
	}
	
	/**
	 * Method returns whether the character at the specific index in the statement is an "erroneous character"
	 * Examples: "@", "\", "{", "$"
	 * @param statement - the statement
	 * @param index - the specific index of the character in the statement
	 * @return whether the character at the specific index in the statement is an "erroneous character" or not
	 * */
	private static boolean isErrorChar(String statement, int index) {
		boolean isLettNum = isLetterOrDigit(statement, index);
		boolean isArithOp = false;
		
		// Go through and make sure if the character is an arithmetic operator or not
		char arithmeticOperator = statement.charAt(index);
		switch(arithmeticOperator) {
			case '+': isArithOp = true;
					break;
			case '-': isArithOp = true;
					break;
			case '*': isArithOp = true;
					break;
			case '/': isArithOp = true;
					break;
			case '%': isArithOp = true;
					break;
			case '=': isArithOp = true;
					break;
			default: isArithOp = false;
		}
		// If it is not a letter, digit, arithmetic operator, or a space, then it is a true erroneous character
		return !isLettNum && !isArithOp && (statement.charAt(index) != ' ');
	}
	

}
