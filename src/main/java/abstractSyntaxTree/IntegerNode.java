package abstractSyntaxTree;

/** A leaf node representing a literal integer constant. */
public class IntegerNode extends Node {
	private int value;
	
	public IntegerNode(int value) {
		this.value = value;
	}
	
	/** @return the constant value this node holds */
	@Override
	public int evaluate() {
		return value;
	}
}
