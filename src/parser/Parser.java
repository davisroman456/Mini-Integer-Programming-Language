package parser;
import java.util.ArrayList;
import abstractSyntaxTree.*;
import exception.LanguageException;
import interpreter.Interpreter;
import tokenizer.*;

/**
 * Parses a tokenized statement into an executable abstract syntax tree and immediately evaluates it.
 * Supports two statement forms: assignment ({@code identifier = expression}) and printing
 * ({@code PRINT expression}). Expression parsing uses Pratt parsing.
 *  @see <a href="https://martin.janiczek.cz/2023/07/03/demystifying-pratt-parsers.html">Demystifying Pratt Parsers</a>
 *      by Martin Janiczek.
 */
public class Parser {
	/** Minimum binding power for the operand of a unary minus: above {@code * / %} (20), at or below {@code ^} (30). */
	private static final int PREFIX_POWER = 25;
	
	
	/**
	 * Entry point for parsing a single tokenized statement. Scans for ERROR tokens first to fail
	 * fast on invalid input, then dispatches to either assignment or print parsing based on the
	 * shape of the token list, and evaluates the resulting node immediately.
	 *
	 * @param tokens the tokens produced by {@link tokenizer.Tokenizer#tokenize(String)}
	 * @throws exception.LanguageException if an ERROR token is present or the statement doesn't
	 *         match a valid assignment or print form
	 */
	public static void parse(ArrayList<Token> tokens) { // Maybe check for multiple PRINT or '=' in the statement for immediate error encounter via counter that musn't exceed
		// First look for any ERROR tokens to end parsing early
		boolean foundErrorToken = false;
		Token errorToken = null;
		for(int i = 0; !foundErrorToken && i < tokens.size(); i++) {
			if(tokens.get(i).getTokenType() == TokenType.ERROR) {
				foundErrorToken = true;
				errorToken = tokens.get(i);
			}
		}
		if(foundErrorToken) { // The second an ERROR token is found throw an exception and stop parsing
			throw new LanguageException("ERROR token encountered: \"" + errorToken.getValue() +"\" Main possible reasons include invalid characters in the statement.");
		}
		
		// Determines whether to parse in regards to printing or assigning
		if(tokens.getFirst().getTokenType() == TokenType.PRINT) {
			Interpreter.evaluateNode(parsePrint(tokens));
			
		} else if (tokens.size() > 2 && tokens.get(1).getTokenType() == TokenType.EQUALS) {
			Interpreter.evaluateNode(parseAssignment(tokens));
			
		} else { // Just simply an invalid statement
			throw new LanguageException("Statement Error: Invalid statement. (Proper statements e.g. \"x = 9 + 3\", \"PRINT x - 10\")");
		}
		
	}
	
	/**
	 * Parses a full expression starting at the beginning of {@code tokens} and verifies every token
	 * was consumed, which catches a stray closer (e.g. {@code "x = 2 + 3)"}).
	 *
	 * @param tokens the tokens making up the expression
	 * @return the root node of the resulting expression tree
	 * @throws exception.LanguageException if the expression is malformed or tokens remain unconsumed
	 */
	private static Node parseExpression(ArrayList<Token> tokens) {
		int[] pos = { 0 };
		Node result = parseExpr(tokens, pos, 0); // 
		if(pos[0] != tokens.size())
			throw new LanguageException("Expression Error: Unexpected token encountered after parsing expression.");
		return result;
	}
	
	/**
	 * Core of the Pratt parser. Parses one prefix expression, then keeps folding in infix operators
	 * while their binding power is at least {@code minPower}. A recursive call with a higher
	 * {@code minPower} stops the right operand sooner (left associativity), while passing the
	 * operator's own power lets the same operator continue on the right (right associativity, used by
	 * {@code ^}). Non-operators such as {@code )} and {@code ]} have a binding power of -1, so the loop
	 * stops at them and leaves them for the caller to check.
	 *
	 * @param tokens the full token list being parsed
	 * @param pos single-element cursor holding the index of the next unconsumed token, advanced as tokens are consumed
	 * @param minPower the lowest binding power this call may consume; the top-level call uses 0
	 * @return the root node of the expression parsed at this level
	 * @throws exception.LanguageException if the expression is malformed
	 */
	private static Node parseExpr(ArrayList<Token> tokens, int[] pos, int minPower) {
		Node left = parsePrefix(tokens, pos);

		while(pos[0] < tokens.size()) {
			TokenType opType = tokens.get(pos[0]).getTokenType(); // peek at the NEXT unconsumed token
			int power = bindingPower(opType);

			if(power < minPower) // not an operator (-1) or binds too loosely for this call
				break;

			pos[0]++; // consume the operator
			int rightPower = (opType == TokenType.CARET) ? power : power + 1; // right-assoc ^, left-assoc the rest
			Node right = parseExpr(tokens, pos, rightPower);
			left = new BinaryNode(operatorChar(opType), left, right);
		}
		return left;
	}
	
	/**
	 * Converts an infix operator token type into the operator character a {@link BinaryNode} stores.
	 *
	 * @param type the operator token type
	 * @return the matching operator character
	 * @throws exception.LanguageException if {@code type} is not an infix operator
	 */
	private static char operatorChar(TokenType type) {
		switch(type) {
		case PLUS:
			return '+';
		case MINUS:
			return '-';
		case MULTIPLY:
			return '*';
		case DIVIDE:
			return '/';
		case MODULO:
			return '%';
		case CARET:
			return '^';
		default:
			throw new LanguageException();
		
		}
		
	}
	
	/**
	 * Returns binding power of a token type, if else, return '-1'.
	 *
	 * @param type - the token type.
	 * @return Integer of the binding power of a particular token type.
	 */
	private static int bindingPower(TokenType type) {
		switch(type) {
		case PLUS, MINUS:
			return 10;
		case MULTIPLY, DIVIDE, MODULO:
			return 20;
		case CARET:
			return 30;
		default:
			return -1;
		}
	}
	
	/**
	 * Parses whatever can start an expression: a number, a variable, a unary minus, or a grouped
	 * expression in {@code ()} or {@code []}. A closing bracket can never start an expression, so
	 * it falls through to the error case.
	 *
	 * @param tokens the full token list being parsed
	 * @param pos shared cursor into {@code tokens}
	 * @return the node for this prefix expression
	 * @throws exception.LanguageException if no tokens remain, the token cannot start an expression,
	 *         an integer literal is out of range, or a group is missing or has a mismatched closer
	 */
	private static Node parsePrefix(ArrayList<Token> tokens, int[] pos) {
		if(pos[0] >= tokens.size())
			throw new LanguageException("Expression Error: Expected a number, variable, or '-' but reached end of statement.");

		Token token = tokens.get(pos[0]);
		switch(token.getTokenType()) {
		case LPAREN:
		case LBRACKET: {
			pos[0]++; // consume the opener
			Node inner = parseExpr(tokens, pos, 0); // fresh expression inside the group
			TokenType closer = (token.getTokenType() == TokenType.LPAREN) ? TokenType.RPAREN : TokenType.RBRACKET;
			if(pos[0] >= tokens.size() || tokens.get(pos[0]).getTokenType() != closer)
				throw new LanguageException("Expression Error: Missing or mismatched closing bracket, expected '"
						+ (closer == TokenType.RPAREN ? ")" : "]") + "'.");
			pos[0]++; // consume the closer
			return inner;
		}
		case MINUS: {
			pos[0]++;
			Node operand = parseExpr(tokens, pos, PREFIX_POWER);
			return new BinaryNode('*', new IntegerNode(-1), operand);
		}
		case NUMBER:
			pos[0]++;
			int numVal;
			try {
				numVal = Integer.parseInt(token.getValue());
			}catch(NumberFormatException e) {
				throw new LanguageException("Statement Error: Integer Overflow Error or could not parse integer.");
			}
			return new IntegerNode(numVal);
		case IDENTIFIER:
			pos[0]++;
			return new VariableNode(token.getValue());
		default:
			throw new LanguageException("Expression Error: Expected a number, variable, '-', or an opening bracket but found incompatible token.");
		}
	}
	
	/**
	 * Strips the leading PRINT token and parses the remainder as an expression to print.
	 *
	 * @param tokens - the tokens of a PRINT statement, PRINT token still included at index 0
	 * @return PrintNode wrapping the parsed expression
	 * @throws exception.LanguageException if no expression follows PRINT
	 */
	private static PrintNode parsePrint(ArrayList<Token> tokens) {
		tokens.removeFirst(); // excludes "PRINT" token
		if(tokens.isEmpty())
			throw new LanguageException("Expression Error: Missing expression.");
		return new PrintNode(parseExpression(tokens));
		
	}
	
	/**
	 * Parses an assignment statement of the form {@code identifier = expression}.
	 *
	 * @param tokens the tokens of an assignment statement, identifier and EQUALS still included
	 * @return AssignmentNode wrapping the target variable and parsed expression
	 * @throws exception.LanguageException if the first token isn't an identifier or no expression follows
	 */
	private static AssignmentNode parseAssignment(ArrayList<Token> tokens) {
		Token variableToken = tokens.removeFirst(); // Remove but also save it when returning AssignmentNode
		if(variableToken.getTokenType() != TokenType.IDENTIFIER)
			throw new LanguageException("Assignment Error: Can not assign to a non-variable type.");
		tokens.removeFirst(); // Do it again to leave just the expression
		if(tokens.isEmpty())
			throw new LanguageException("Expression Error: Missing expression.");
		return new AssignmentNode(new VariableNode(variableToken.getValue()),parseExpression(tokens));
	}
	
} 
