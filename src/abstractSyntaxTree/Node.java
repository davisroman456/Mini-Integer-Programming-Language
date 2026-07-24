package abstractSyntaxTree;

/**
 * Base type for every node in the abstract syntax tree. Each concrete subclass represents one
 * kind of language construct (a literal, a variable reference, a binary operation, an assignment,
 * or a print statement) and knows how to evaluate itself to an integer result.
 */
public abstract class Node {
	/**
	 * Evaluates this node, recursively evaluating any child nodes first as needed.
	 *
	 * @return the integer result of evaluating this node
	 */
	public abstract int evaluate();
}
