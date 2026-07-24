# Mini Integer Programming Language

A line-by-line integer programming language built with a custom tokenizer, a recursive descent
parser, and an abstract syntax tree (AST) based interpreter. Supports variable assignment,
arithmetic with proper PEMDAS-based operator precedence, and PRINT statements. Invalid or
malformed statements produce informative error messages instead of crashing the program, so a
session keeps running until `QUIT` is entered or input ends.

## Syntax Rules

`VALUE` is either an integer literal or a variable reference. A variable must be assigned before
it can be used in an expression.

**Operators** (highest to lowest precedence): `*`, `/`, `%` before `+`, `-`. Unlike a purely
left-to-right language, multiplication/division/modulo are always evaluated before addition/
subtraction, regardless of order written. Unary minus is supported (e.g. `-5`, `-x`), including
chained negatives (e.g. `--5`). Spaces are not significant.

**Assignment:**
```
IDENTIFIER = EXPRESSION
```

**Print:**
```
PRINT EXPRESSION
```

**Quit:**
```
QUIT
```

## Example Cases

```
x = 3
y = x * 2
PRINT y * x + 2
>>> 20
```
`y * x + 2` evaluates as `(y * x) + 2`, since `*` binds tighter than `+`.

```
x = 10
y = -x + 5
PRINT y
>>> -5
```

## Error Handling

```
x = 5 @ 3
ERROR token encountered: "@" Main possible reasons include invalid characters in the statement.

x = 5 +
Expression Error: Expected a number, variable, or '-' but reached end of statement.

x = 5 / 0
Arithmetic Error: Division by 0!!

PRINT y
Retrieval Error: "y" does not exist!!

x = 9999999 * 9999999 * 9999999
Arithmetic Error: Result of '*' operation overflowed integer bounds!!
```

## Limitations

- No parentheses — expressions can't be grouped, so `(4 + 9) * 2` isn't currently expressible.
- No exponentiation.
- Integers only — no floating-point support; division truncates like Java integer division.

## Running

```
java -jar Mini_Integer_Programming_Language.jar
```

Type statements one per line. Type `QUIT` to exit, or send EOF (`Ctrl+Z` then Enter on Windows,
`Ctrl+D` on Unix) to end the session.

## Author

Roman Davis
