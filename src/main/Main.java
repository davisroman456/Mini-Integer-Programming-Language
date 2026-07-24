package main;

import java.util.Scanner;

import exception.LanguageException;
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
 *  @author Roman Davis
 *  @version 1.0
 * */
public class Main {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.println("- - - - - - - - - - - - - - - - Mini Integer Language - - - - - - - - - - - - - - - - -");
		System.out.println("Operators : +, -, *, /, %  (This language does not have functionality for parentheses)*");
		System.out.println("Keyword(s): PRINT,         (Displays the result of the expression)");
		System.out.println("            QUIT           (Quits application)");
		System.out.println("Example lines:");
		System.out.println("	x = 4 + 9 * 2");
		System.out.println("	y = -2");
		System.out.println("	PRINT x + y");
		System.out.println("	>>> 20");
		System.out.println("	QUIT");
		System.out.println("=======================================================================================\n");
		boolean quitApp = false;
		while(!quitApp && input.hasNextLine()){
			try {
				String statement = input.nextLine();
				if(statement.equals("QUIT"))
					quitApp = true;
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

}
