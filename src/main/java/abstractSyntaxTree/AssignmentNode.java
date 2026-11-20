package abstractSyntaxTree;

import exception.LanguageException;
import interpreter.Interpreter;


public class AssignmentNode extends Node {
	private VariableNode variable;
	private Node expression;
	
	/**
	 * @param variable the target variable node
	 * @param expression the expression whose result will be assigned
	 * @throws exception.LanguageException if either argument is null, or {@code variable} isn't a VariableNode
	 */
	public AssignmentNode(Node variable, Node expression){
		if(variable == null || expression == null)
			throw new LanguageException("Assignment Error: Could not identify any variable and/or expression!!");
		else if(!(variable instanceof VariableNode))
			throw new LanguageException("Assignment Error: Can not assign to a non-variable type!!");
		
		this.variable = (VariableNode) variable;
		this.expression = expression;
		
	}
	
	/**
	 * Evaluates the expression and stores the result under the target variable's name.
	 *
	 * @return the assigned value
	 */
	@Override
	public int evaluate() {
		int expressionResult = expression.evaluate();
		Interpreter.setVariable(variable.getVariableName(), expressionResult);
		return expressionResult; // A return isn't really needed
	}
	
	public void setVariable(Node variable) {
		if(!(variable instanceof VariableNode))
			throw new LanguageException("Assignment Error: Can not assign to a non-variable type!!");
		this.variable = (VariableNode) variable;
	}
	
	public void setExpression(Node expression) {
		this.expression = expression;
	}

}
