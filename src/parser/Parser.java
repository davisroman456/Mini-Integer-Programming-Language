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
			throw new LanguageException("Statement Error: Invalid statement!! (Proper statements e.g. \"x = 9 + 3\", \"PRINT x - 10\"");
		}
		
	}
	/*
	 * ORIGINAL STRATEGY (kept for reference) — three-pass reduction over a flat ArrayList of nodes.
	 * This was my first working solution and is what taught me how operator precedence actually
	 * gets enforced structurally (unary minus, then * / %, then + -, each pass folding the list
	 * down further). It works correctly, but I moved off of it for a few reasons:
	 *
	 * 1. Big-O: each pass is O(n), and there are 3 fixed passes, but every fold/remove shifts the
	 *    ArrayList, so in the worst case (long chains of the same precedence level) this behaves
	 *    closer to O(n^2) than O(n) as the statement grows, since remove() on an ArrayList is O(n)
	 *    itself and happens repeatedly inside each pass.
	 * 2. It doesn't extend well. Adding a new precedence level (or parentheses) means adding another
	 *    whole pass and more index-shifting logic, rather than just adding one more method to a chain.
	 * 3. Mutating a shared list in place while iterating over it (with manual i-- compensation) is
	 *    fragile and hard to reason about/debug compared to structured recursive calls.
	 *
	 * Replaced below with a recursive descent parser (Expression -> Term -> Factor), which walks the
	 * token list once, left to right, with precedence falling naturally out of the call structure
	 * instead of being enforced via repeated passes.
	 */
	/*
	private static Node parseExpression(ArrayList<Token> tokens) {
		// 1st. create node relation ship for an integer or variable preceded by a minus symbol (negative). Make binaryNode: op = '*', left = -1, right = node.value
		// x = -1 + 5 * 2 - 8  IDENTIFIER(x) EQUALS(=) MINUS(-) NUMBER(1) PLUS(+) NUMBER(5) MULTIPLY(*) NUMBER(2) MINUS(-) NUMBER(8)*
		ArrayList<Node> astNodes = new ArrayList<>(tokens.size()); // tokens past '=' or "PRINT"
		// Create specific nodes based on token type and keep references null for now
		Iterator<Token> tokenIterator = tokens.iterator();
		
		while(tokenIterator.hasNext()){ // Populate linked list with unreferenced nodes
			Token tempToken = tokenIterator.next();
			switch(tempToken.getTokenType()){
			case IDENTIFIER:
				astNodes.add(new VariableNode(tempToken.getValue()));
				break;
			case NUMBER:
				astNodes.add(new IntegerNode(Integer.parseInt(tempToken.getValue())));
				break;
			case PLUS:
				astNodes.add(new BinaryNode('+', null, null));
				break;
			case MINUS: // Keep in mind of negative unary-minus numbers
				astNodes.add(new BinaryNode('-', null, null));
				break;
			case MULTIPLY:
				astNodes.add(new BinaryNode('*', null, null));
				break;
			case DIVIDE:
				astNodes.add(new BinaryNode('/', null, null));
				break;
			case MODULO:
				astNodes.add(new BinaryNode('%', null, null));
				break;
			default:
				throw new LanguageException("Expression Error: Incompatible token encountered in the expression!!");
			}
		}
		// Loop once for unary minus
		for(int i = 0; i < astNodes.size(); i++) {
			Node tempNode = astNodes.get(i);
			if(tempNode instanceof BinaryNode) {
				BinaryNode tempBinaryNode = (BinaryNode) tempNode;
				if(tempBinaryNode.getOperator() == '-') {
					if(i != 0 && i != astNodes.size() - 1) {
						Node leftNode = astNodes.get(i-1);
						Node rightNode = astNodes.get(i+1);
						if(!(leftNode instanceof IntegerNode || leftNode instanceof VariableNode) &&
						  (rightNode instanceof IntegerNode || rightNode instanceof VariableNode)) {
							tempBinaryNode.setOperator('*');
							tempBinaryNode.setLeft(new IntegerNode(-1));
							tempBinaryNode.setRight(rightNode);
							astNodes.remove(i + 1);
						}
					}else if(i == 0 && i != astNodes.size() - 1) {
						Node rightNode = astNodes.get(i+1);
						if(rightNode instanceof IntegerNode || rightNode instanceof VariableNode) {
							tempBinaryNode.setOperator('*');
							tempBinaryNode.setLeft(new IntegerNode(-1));
							tempBinaryNode.setRight(rightNode);
							astNodes.remove(i + 1);
						}
					}
					
				}
			}
		} // End of first parse loop
		
		// Loop twice for Multiplication, Division, and Modulo
		for(int i = 0; i < astNodes.size(); i++) {
			Node tempNode = astNodes.get(i);
			if(tempNode instanceof BinaryNode) {
				BinaryNode tempBinaryNode = (BinaryNode) tempNode;
				char operator = tempBinaryNode.getOperator();
				if(i != 0 && i != astNodes.size() - 1) {
					if(operator == '*' || operator == '/' || operator == '%') {
						Node leftNode = astNodes.get(i-1);
						Node rightNode = astNodes.get(i+1);
						if(isOperand(leftNode) && isOperand(rightNode)) {
							tempBinaryNode.setLeft(leftNode);
							tempBinaryNode.setRight(rightNode);
							astNodes.remove(i + 1);
							astNodes.remove(i - 1);
							i--; // Since element left of current Node is removed, list shifts down. Index tracker has to shift down to compensate
						}
					}
				}
			}
		} // End of second parse loop
		
		// Loop thrice for Addition and Subtraction
		for(int i = 0; i < astNodes.size(); i++) {
			Node tempNode = astNodes.get(i);
			if(tempNode instanceof BinaryNode) {
				BinaryNode tempBinaryNode = (BinaryNode) tempNode;
				char operator = tempBinaryNode.getOperator();
				if(i != 0 && i != astNodes.size() - 1) {
					if(operator == '+' || operator == '-') {
						Node leftNode = astNodes.get(i-1);
						Node rightNode = astNodes.get(i+1);
						if(isOperand(leftNode) && isOperand(rightNode)) {
							tempBinaryNode.setLeft(leftNode);
							tempBinaryNode.setRight(rightNode);
							astNodes.remove(i + 1);
							astNodes.remove(i - 1);
							i--; // Since element left of current Node is removed, list shifts down. Index tracker has to shift down to compensate
						}
					}
				}
			}
		} // End of third parse loop
		
		// Should be a single node remaining
		if(astNodes.size() == 1)
			return astNodes.getFirst();
		else
			throw new LanguageException("Expression Error: Could not completely parse expression.");
	}
	
	
	private static boolean isOperand(Node node) {
	    return node instanceof IntegerNode || node instanceof VariableNode || node instanceof BinaryNode;
	}
	*/
	
	// NEW STRATEGY: recursive descent, one method per precedence level.
	// Grammar (lowest to highest precedence):
	//   expression := term (('+' | '-') term)*
	//   term       := factor (('*' | '/' | '%') factor)*
	//   factor     := NUMBER | IDENTIFIER | '-' factor
	// 'pos' is a single-element array acting as a mutable cursor into 'tokens',
	// since Java can't pass a primitive int by reference.

	/**
	 * Parses a full arithmetic expression starting at the beginning of {@code tokens}, and verifies
	 * every token was consumed (catching cases like a trailing operator, e.g. {@code "x = 5 +"}).
	 *
	 * @param tokens the tokens making up the expression
	 * @return the root node of the resulting expression tree
	 * @throws exception.LanguageException if the expression is malformed or tokens remain unconsumed
	 */
	private static Node parseExpression(ArrayList<Token> tokens) {
		int[] pos = { 0 };
		Node result = parseExpression(tokens, pos);
		if(pos[0] != tokens.size())
			throw new LanguageException("Expression Error: Unexpected token encountered after parsing expression.");
		return result;
	}

	/**
	 * Grammar rule: {@code expression := term (('+' | '-') term)*}.
	 * Parses one or more terms combined by addition/subtraction, left-associatively.
	 *
	 * @param tokens the full token list being parsed
	 * @param pos single-element cursor tracking the current position in {@code tokens}, advanced
	 *        as tokens are consumed (Java has no pass-by-reference for primitives, so this array
	 *        is the shared mutable cursor across all three grammar-level methods)
	 * @return the node representing this expression
	 */
	private static Node parseExpression(ArrayList<Token> tokens, int[] pos) {
		Node left = parseTerm(tokens, pos);
		while(pos[0] < tokens.size() &&
			(tokens.get(pos[0]).getTokenType() == TokenType.PLUS || tokens.get(pos[0]).getTokenType() == TokenType.MINUS)) {
			char operator = switch(tokens.get(pos[0]).getTokenType()) {
			    case PLUS -> '+';
			    default -> '-';
			};
			pos[0]++;
			Node right = parseTerm(tokens, pos);
			left = new BinaryNode(operator, left, right);
		}
		return left;
	}

	/**
	 * Grammar rule: {@code term := factor (('*' | '/' | '%') factor)*}.
	 * Parses one or more factors combined by multiplication/division/modulo, left-associatively.
	 * Called from {@link #parseExpression(ArrayList, int[])} so that these operators always end up
	 * nested more deeply in the tree than + or -, giving them higher evaluation precedence.
	 *
	 * @param tokens the full token list being parsed
	 * @param pos shared cursor into {@code tokens}
	 * @return the node representing this term
	 */
	private static Node parseTerm(ArrayList<Token> tokens, int[] pos) {
		Node left = parseFactor(tokens, pos);
		while(pos[0] < tokens.size() &&
			(tokens.get(pos[0]).getTokenType() == TokenType.MULTIPLY ||
			 tokens.get(pos[0]).getTokenType() == TokenType.DIVIDE ||
			 tokens.get(pos[0]).getTokenType() == TokenType.MODULO)) {
			TokenType type = tokens.get(pos[0]).getTokenType();
			char operator = switch(type) {
			    case MULTIPLY -> '*';
			    case DIVIDE -> '/';
			    default -> '%';
			};
			pos[0]++;
			Node right = parseFactor(tokens, pos);
			left = new BinaryNode(operator, left, right);
		}
		return left;
	}

	/**
	 * Grammar rule: {@code factor := NUMBER | IDENTIFIER | '-' factor}.
	 * The base case of the recursive descent: parses a single number or variable, or a unary minus
	 * applied to another factor (handled recursively, so chained negatives like {@code "--5"} work
	 * without special-casing).
	 *
	 * @param tokens the full token list being parsed
	 * @param pos shared cursor into {@code tokens}
	 * @return the node representing this factor
	 * @throws exception.LanguageException if the current token isn't a number, identifier, or minus
	 */
	private static Node parseFactor(ArrayList<Token> tokens, int[] pos) {
		if(pos[0] >= tokens.size())
			throw new LanguageException("Expression Error: Expected a number, variable, or '-' but reached end of statement.");

		Token token = tokens.get(pos[0]);
		switch(token.getTokenType()) {
		case MINUS:
			pos[0]++;
			Node operand = parseFactor(tokens, pos); // unary minus, handles chained negatives like "--5" too
			return new BinaryNode('*', new IntegerNode(-1), operand);
		case NUMBER:
			pos[0]++;
			int numVal;
			try {
				numVal = Integer.parseInt(token.getValue());
			}catch(NumberFormatException e) {
				throw new LanguageException("Statement Error: Integer Overflow Error or could not parse integer!!");
			}
			return new IntegerNode(numVal);
		case IDENTIFIER:
			pos[0]++;
			return new VariableNode(token.getValue());
		default:
			throw new LanguageException("Expression Error: Expected a number, variable, or '-' but found incompatible token!!");
		}
	}
	
	/**
	 * Strips the leading PRINT token and parses the remainder as an expression to print.
	 *
	 * @param tokens the tokens of a PRINT statement, PRINT token still included at index 0
	 * @return a PrintNode wrapping the parsed expression
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
	 * @return an AssignmentNode wrapping the target variable and parsed expression
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
