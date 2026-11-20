package main;

import java.util.Scanner;

import exception.LanguageException;
import interpreter.Interpreter;
import parser.Parser;
import tokenizer.Tokenizer;

/**
 *  {@summary Mini Integer Programming Language with minimal capabilities.}
 *  Reads statements from standard input line by line, tokenizes and parses each one into
 *  a functional abstract syntax tree for proper PEMDAS-based arithmetic evaluation, supporting integer
 *  assignment and PRINT statements. Invalid or malformed statements are reported with informative,
 *  targeted error messages via {@link exception.LanguageException} rather than crashing the program,
 *  allowing the session to continue until the user issues QUIT or input ends.
 *  
 *  Date started: July 17, 2026. Date of completion: July 20, 2026.
 *  Major Update: November 20, 2026 (Parentheses & Brackets, Exponentiation, HELP & CLEAR keyword, and text colors).
 *  @author Roman Davis
 *  @version 2.0
 * */
public class Main {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		printHelp(); 
		boolean quitApp = false;
		while(!quitApp && input.hasNextLine()){
			try {
				String statement = input.nextLine();
				if(statement.trim().equals("QUIT"))
					quitApp = true;
				else if(statement.trim().equals("HELP"))
					printHelp();
				else if(statement.trim().equals("CLEAR")) {
					clearScreen();
					if(!Interpreter.isVariablesEmpty())
						Interpreter.clearVariables();
				}
				else
					Parser.parse(Tokenizer.tokenize(statement));
			}catch(LanguageException le) {
				System.err.println(le.getMessage());
			}catch(IllegalStateException e) {
				System.out.println(e.getMessage());
			}
		}
	
		input.close();
		// End of program
	}
	
	public static void printHelp() {
		System.out.println("""
		        +======================================== HELP =========================================+
		        |Operators          : + (Addition),                                                     |
		        |                     - (Subtraction),                                                  |
		        |                     * (Multiplication),                                               |
		        |                     / (Integer Division),                                             |
		        |                     % (Modulo),                                                       |
		        |                     ^ (Exponentiation)                                                |
		        |Expression Grouping: (), []                                                            |
		        |Keywords           : PRINT, (Displays the result of the expression)                    |
		        |                     HELP,  (Displays Operators, Keywords, and Example lines)          |
		        |                     CLEAR, (Clears the standard output and memory)                    |
		        |                     QUIT   (Quits application)                                        |
		        |Example lines:                                                                         |
		        |   x = (4 + 9) * 2                                                                     |
		        |   y = -2                                                                              |
		        |   PRINT x + y                                                                         |
		        |   >>> 24                                                                              |
		        |   QUIT                                                                                |
		        +=======================================================================================+\n""");

	}
	
	public static void clearScreen() {
	    System.out.print("\033[H\033[2J");
	    System.out.flush();
	}


}
