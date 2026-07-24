package abstractSyntaxTree;

/** Represents printing the result of an expression to standard output in {@code >>> value} form. */
public class PrintNode extends Node {
	private Node expression;
	
	public PrintNode(Node expression) {
		this.expression = expression;
		
	}
	
	/**
	 * Evaluates the wrapped expression and prints it.
	 *
	 * @return the evaluated result
	 */
	@Override
	public int evaluate() {
		int evaluation = expression.evaluate();
		System.out.printf(">>> %,d%n", evaluation);
		return evaluation;
	}
	
	public void setExpression(Node expression) {
		this.expression = expression;
	}

}
