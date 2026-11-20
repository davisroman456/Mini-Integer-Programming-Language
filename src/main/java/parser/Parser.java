package parser;
import java.util.ArrayList;
import abstractSyntaxTree.*;
import exception.LanguageException;
import interpreter.Interpreter;
import tokenizer.*;

/**
 * Parses a tokenized statement into an executable abstract syntax tree and immediately evaluates it.
 * Supports two statement forms: assignment ({@code identifier = expression}) and printing
 * ({@code PRINT expression}). Expression parsing uses recursive descent, with one method per
 * precedence level, so operator precedence (unary minus, then * / %, then + -) falls naturally out
 * of the call structure rather than being enforced by repeated passes over a flat list.
 */
public class Parser {
	
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
	
	private static Node parseExpression(ArrayList<Token> tokens) {
		int[] pos = { 0 };
		Node result = parseExpr(tokens, pos, 0); // 
		if(pos[0] != tokens.size())
			throw new LanguageException("Expression Error: Unexpected token encountered after parsing expression.");
		return result;
	}
	
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
	 * Returns char representation of a particular token type.
	 *
	 * @param type - The token type.
	 * @return char - character representation of a token type.
	 * @throws exception.LanguageException default version
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
	 * Parses a prefixed token or a number or an identifier.
	 *
	 * @param tokens - ArrayList of Tokens.
	 * @param pos - mutable "pass-by-refrence" index pointer of {@code tokens}.
	 * @return Node - can be a Binary Node, Integer Node, or Variable Node.
	 * @throws exception.LanguageException If any parsing issues arise.
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
			throw new LanguageException("Expression Error: Expected a number, variable, '-', or an opening bracket but found incompatible token!!");
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
			throw new LanguageException("Assignment Error: Can not assign to a non-variable type!!");
		tokens.removeFirst(); // Do it again to leave just the expression
		if(tokens.isEmpty())
			throw new LanguageException("Expression Error: Missing expression.");
		return new AssignmentNode(new VariableNode(variableToken.getValue()),parseExpression(tokens));
	}
	
} 
