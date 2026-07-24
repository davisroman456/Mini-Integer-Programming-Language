package interpreter;

import java.util.HashMap;
import abstractSyntaxTree.*;
import exception.LanguageException;

/**
 * Maintains program state (variable values) and provides the single entry point for evaluating any
 * parsed abstract syntax tree node, relying on polymorphism to dispatch to the correct
 * {@code evaluate()} implementation regardless of node type.
 */
public class Interpreter {
	private static HashMap<String, Integer> variables = new HashMap<>();
	
	/**
	 * Retrieves the current value of a variable.
	 *
	 * @param variableName the variable to look up
	 * @return the variable's current integer value
	 * @throws exception.LanguageException if the variable has never been assigned
	 */
	public static int getVariableVal(String variableName) {
		if(!variables.containsKey(variableName)) { //
			throw new LanguageException("Retrieval Error: \"" + variableName + "\" does not exist!!");
		}
		return variables.get(variableName);
	}
	
	/**
	 * Assigns (or reassigns) a variable's value.
	 *
	 * @param variableName the variable to set
	 * @param value the value to store
	 */
	public static void setVariable(String variableName, int value) {
		variables.put(variableName, value);
	}
	
	/**
	 * Evaluates any AST node polymorphically.
	 *
	 * @param node the node to evaluate (a PrintNode, AssignmentNode, or arithmetic subtree)
	 */
	public static void evaluateNode(Node node) {
		node.evaluate();
	}

}
