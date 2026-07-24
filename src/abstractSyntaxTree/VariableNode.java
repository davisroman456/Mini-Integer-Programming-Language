package abstractSyntaxTree;

import interpreter.Interpreter;

/** A leaf node representing a reference to a variable, resolved against interpreter state. */
public class VariableNode extends Node {
	private String variableName;
	
	public VariableNode(String name) {
		this.variableName = name;
	}
	
	/**
	 * @return the variable's current value
	 * @throws exception.LanguageException if the variable has never been assigned
	 */
	@Override
	public int evaluate() {
		return Interpreter.getVariableVal(variableName);
	}
	
	/** @return the variable's identifier (name)*/
	public String getVariableName() {
		return this.variableName;
	}
}
