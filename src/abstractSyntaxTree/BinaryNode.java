package abstractSyntaxTree;

import exception.LanguageException;

/**
 * Represents a binary arithmetic operation (+, -, *, /, %) between two child nodes. Built by the
 * parser with both operands already known — unlike the original three-pass parsing strategy, which
 * constructed these nodes with null operands first and filled them in during later passes.
 */
public class BinaryNode extends Node {
	private Node left; // TODO Change these just to Node as polymorphism will work as intended 
	private Node right;
	private char operator;
	
	public BinaryNode(char op, Node left, Node right) {
		this.operator = op;
		this.left = left;
		this.right = right;
		
	}
	
	/**
	 * Evaluates both operands, then applies this node's operator.
	 *
	 * @return the result of the operation
	 * @throws exception.LanguageException if either operand is null, or on division/modulo by zero
	 */
	@Override
	public int evaluate() {
		if(left == null || right == null)
			throw new LanguageException("Evaluation Error: Could not identify left and/or right operand for \'"+operator+"\' operator!!");
		int leftResult = left.evaluate();
		int rightResult = right.evaluate();
		int value;
		try {
				value = switch(operator) {
				case '+' -> Math.addExact(leftResult, rightResult);
		        case '-' -> Math.subtractExact(leftResult, rightResult);
		        case '*' -> Math.multiplyExact(leftResult, rightResult);
				case '/' -> { // Checks for division by 0
					if(rightResult == 0)
						throw new LanguageException("Arithmetic Error: Division by 0!!");
					else
						yield leftResult / rightResult;
				}
				case '%' -> { // Checks for modulo 0
					if(rightResult == 0)
						throw new LanguageException("Arithmetic Error: Modulo 0!!");
					else
						yield leftResult % rightResult;
				}
				default -> throw new IllegalStateException("Unexpected operator: " + operator); // If an improper operator has not been caught yet.
			};
		}catch(ArithmeticException e) {
			throw new LanguageException("Arithmetic Error: Result of '" + operator + "' operation overflowed integer bounds!!");
		}
		return value;
	}
	
	public char getOperator() {
		return this.operator;
	}
	
	public void setOperator(char op) {
		this.operator = op;
	}
	
	public void setLeft(Node left) {
		this.left = left;
	}
	
	public void setRight(Node right) {
		this.right = right;
	}
	
	
	
}
