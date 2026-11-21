# Mini Integer Programming Language

Inspired by a similar project by [@amarti71](https://github.com/amarti71).

A line-by-line integer programming language built with a custom tokenizer, a Pratt parser, and an
abstract syntax tree (AST) based interpreter. Supports variable assignment, arithmetic with proper
PEMDAS-based operator precedence, exponentiation, grouping with parentheses and brackets, and
PRINT statements. Invalid or malformed statements produce informative error messages instead of
crashing the program, so a session keeps running until `QUIT` is entered or input ends.

## Syntax Rules

`VALUE` is either an integer literal or a variable reference. A variable must be assigned before
it can be used in an expression.

**Operators** (highest to lowest precedence): `^`, then `*`, `/`, `%`, then `+`, `-`. Unlike a
purely left-to-right language, higher-precedence operators are always evaluated first, regardless
of order written. All operators are left-associative except `^`, which is right-associative
(`2^3^2` is `2^(3^2)`). Unary minus is supported (e.g. `-5`, `-x`), including chained negatives
(e.g. `--5`), and binds tighter than `* / %` but looser than `^` (so `-2^2` is `-4`). Spaces are
not significant.

**Grouping:** `()` and `[]` work identically and can be mixed, but every opener must be closed by
its own kind (e.g. `[2 + (3 * 4)]` is valid, `(2 + 3]` is not).

**Exponent edge cases:** a negative exponent truncates to `0` like integer division (except for
bases `1` and `-1`), and `0^0` and `0^` a negative number are errors.

**Assignment:**
```
IDENTIFIER = EXPRESSION
```

**Print:**
```
PRINT EXPRESSION
```

**Other keywords:** `HELP` shows operators, keywords, and example lines. `CLEAR` clears the
screen and resets memory (all variables are forgotten). `QUIT` exits. `PRINT`, `HELP`, `CLEAR`,
and `QUIT` are reserved and can't be used as variable names.

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

```
PRINT 2^3^2
>>> 512
PRINT -2^2
>>> -4
PRINT [2 + (3 * 4)] * 2
>>> 28
```

## Error Handling

```
x = 5 @ 3
ERROR token encountered: "@" Main possible reasons include invalid characters in the statement.

x = 5 +
Expression Error: Expected a number, variable, '-', or an opening bracket but reached end of statement.

x = (2 + 3]
Expression Error: Missing or mismatched closing bracket, expected ')'.

x = 5 / 0
Arithmetic Error: Division by 0.

PRINT y
Retrieval Error: "y" does not exist!!

HELP = 5
Statement Error: Reserved word 'HELP' can't be used inside a statement.

x = 9999999 * 9999999 * 9999999
Arithmetic Error: Result of '*' operation overflowed integer bounds!!
```

## Limitations

- Integers only — no floating-point support; division truncates like Java integer division.
- `CLEAR` uses ANSI escape codes, so it needs an ANSI-compatible terminal (like Windows Terminal).
  In consoles without ANSI support, such as Eclipse's, the codes show up as raw characters, and
  some terminals keep scrollback history even after `CLEAR`.

## Running

**Requires Java 21 or later — the code uses modern switch expressions (`->` and `yield`) and
list methods like `getFirst()`, which aren't supported in older Java versions.**

**Option 1: release jar.** Download `Mini_Integer_Programming_Language.jar` from the
[Releases](https://github.com/davisroman456/Mini-Integer-Programming-Language/releases) page, then:
```
java -jar Mini_Integer_Programming_Language.jar
```

**Option 2: build from source.**
```
git clone https://github.com/davisroman456/Mini-Integer-Programming-Language.git
cd Mini-Integer-Programming-Language
```
Windows:
```
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out main.Main
```
Mac/Linux:
```
javac -d out $(find src -name "*.java")
java -cp out main.Main
```
Type statements one per line after the `> ` prompt. Type `QUIT` to exit, or send EOF (`Ctrl+Z`
then Enter on Windows, `Ctrl+D` on Unix) to end the session.

## A Note on Process

The Pratt parser used here wasn't my first approach. I originally built precedence handling myself
with a three-pass system — folding unary minus, then `* / %`, then `+ -`, over a flat list of
nodes — then moved to recursive descent, with one method per precedence level. Working out those
first versions from scratch is what taught me *why* precedence has to be enforced structurally in
the first place, and I think that groundwork is what made the next approach click as fast as it
did. I moved to Pratt parsing because recursive descent was hard to trace and every new operator
meant a new method, while a Pratt parser keeps precedence and associativity in a single
binding-power table, which made adding `^` and grouping small changes. The original three-pass and
recursive descent versions are preserved in this repo's Git history. The Pratt approach follows
[Demystifying Pratt Parsers](https://martin.janiczek.cz/2023/07/03/demystifying-pratt-parsers.html)
by Martin Janiczek.

## License

Released under the [MIT License](LICENSE).

## Author

Roman Davis
