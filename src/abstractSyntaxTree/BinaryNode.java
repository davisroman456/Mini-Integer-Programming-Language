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
	 * Evaluates both operands, then applies this node's operator (+, -, *, /, %, ^).
	 * <p>
	 * Exponentiation rules: a negative exponent yields 0 (integer truncation of 1/base^n), except
	 * for bases 1 and -1, which give 1 and (-1)^n respectively for any exponent. {@code 0^0} and
	 * {@code 0^negative} are treated as errors.
	 *
	 * @return the result of the operation
	 * @throws exception.LanguageException if either operand is null, on division/modulo by zero,
	 *         on {@code 0^0} or {@code 0^negative}, or if the result overflows integer bounds
	 * @throws IllegalStateException if the operator is not a recognized arithmetic operator
	 */
	@Override
	public int evaluate() {
		if(left == null || right == null)
			throw new LanguageException("Evaluation Error: Could not identify left and/or right operand for \'"+operator+"\' operator.");
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
						throw new LanguageException("Arithmetic Error: Division by 0.");
					else
						yield leftResult / rightResult;
				}
				case '%' -> { // Checks for modulo 0
					if(rightResult == 0)
						throw new LanguageException("Arithmetic Error: Modulo by 0.");
					else
						yield leftResult % rightResult;
				}
				case '^' ->{
					if(leftResult == 0 && rightResult <= 0)
						throw new LanguageException("Arithmetic Error: Division by 0 or Indeterminate in exponentiation evaluation.");
					else if(leftResult == 1)
						yield 1;
					else if(leftResult == -1)
						yield (rightResult % 2 == 0) ? 1 : -1;	// Even exponent -> 1, odd -> -1 
					else
						yield exp(leftResult, rightResult);
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
	
	/**
	 * {@summary Evaluates a base integer raised to a particular integer power.}
	 * Uses exponentiation by squaring to get O(log n) time complexity
	 * @param base - The number that is getting exponentiated.
	 * @param power - The number that the base is getting raised to.
	 * @return integer result of exponentiation
	 * @throws ArithmeticException When multiplication exceeds the bounds of Integer MAX and MIN values.
	 * @see <a href="https://mpark.github.io/programming/2014/08/18/exponentiation-by-squaring/">Exponentiation by squaring</a>
	 * */
	private static int exp(int base, int power) throws ArithmeticException {
		if (power < 0) return 0;	// Integer division just yields 0 (e.g. "2^(-1)" = "1/2" which is 0)
		int result = 1;
		
		while(power > 0) {
			if(power % 2 == 1) 
				result = Math.multiplyExact(result, base);
			if(power > 1)
				base = Math.multiplyExact(base, base);
			
			power /= 2;
			
		}
		
		return result;
	}
	
	
	
}
