package parser;

import java.util.ArrayList;
import java.util.List;

import tokenizer.BoolToken;
import tokenizer.ColonToken;
import tokenizer.CommaToken;
import tokenizer.FalseToken;
import tokenizer.IdentifierToken;
import tokenizer.IntToken;
import tokenizer.IntegerToken;
import tokenizer.LeftParenToken;
import tokenizer.NullToken;
import tokenizer.Token;
import tokenizer.TrueToken;
import tokenizer.VoidToken;

public class Parser {
    private final List<Token> tokens;

    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token getToken(final int pos) throws ParseException {
        if (pos < 0 || pos >= tokens.size()) {
            String message = ("Position out of bounds: " + pos);
            throw new ParseException(message);
        } else
            return tokens.get(pos);
    }

    // primaryExp::= i(integer) | `true` | `false` | var | `null` | `(` exp
    // `)`|`new` structname `{` struct_actual_params `}` |funcname `(` comma_exp `)`
    // intToken trueToken falseToken varToken nullToken parenExp
    public ParseResult<Exp> parsePrimaryExp(final int startPos) throws ParseException {
        final Token firstToken = getToken(startPos);

        if (firstToken instanceof IntegerToken intToken) {
            return new ParseResult<>(new IntegerExp(intToken.value()), startPos + 1);

        } else if (firstToken instanceof TrueToken) {
            return new ParseResult<>(new TrueExp(), startPos + 1);

        } else if (firstToken instanceof FalseToken) {
            return new ParseResult<>(new FalseExp(), startPos + 1);
            // Use IdentifierToken for variables, since VarToken is only used for variable
            // declarations
        } else if (firstToken instanceof IdentifierToken varToken) {
            return new ParseResult<>(new IdentifierExp(varToken.name()), startPos + 1);

        } else if (firstToken instanceof NullToken) {
            return new ParseResult<>(new NullExp(), startPos + 1);
            // expresssion inside parens
        } else if (firstToken instanceof LeftParenToken) {
            final ParseResult<Exp> exp = parseExp(startPos + 1);

        } else {
            throw new ParseException("Expected primary expression at position " + firstToken.toString());
        }
        throw new ParseException("parsePrimaryExp not finished yet");
    }

    // each level uses the previous level to parse its subexpressions, and then
    // checks for the appropriate operator tokens in between

    // Can you work on this section since it involves symbols and operators

    // dot_exp ::= primary_exp (`.` var)*
    public ParseResult<Exp> parseDotExp(final int startPos) throws ParseException {
        // still needs to be implemented
        throw new ParseException("parseDotExp not implemented yet");
    }

    // mult_exp ::= dot_exp ((`*` | `/`) dot_exp)*
    public ParseResult<Exp> parseMultExp(final int startPos) throws ParseException {
        // still needs to be implemented
        throw new ParseException("parseMultExp not implemented yet");
    }

    // add_exp ::= mult_exp ::= dot_exp ((`+` | `-`) mult_exp)*
    public ParseResult<Exp> parseAddExp(final int startPos) throws ParseException {
        // still needs to be implemented
        throw new ParseException("parseAddExp not implemented yet");
    }

    // less_than_exp ::= add_exp [`<` add_exp]*
    public ParseResult<Exp> parseLessThanExp(final int startPos) throws ParseException {
        // still needs to be implemented
        throw new ParseException("parseLessThanExp not implemented yet");
    }

    // equals_exp ::= less_than_exp ((`==` | `!=`) less_than_exp)*
    public ParseResult<Exp> parseEqualsExp(final int startPos) throws ParseException {
        // still needs to be implemented
        throw new ParseException("parseEqualsExp not implemented yet");
    }

    // parseExp: exp ::= equals_exp
    public ParseResult<Exp> parseExp(final int startPos) throws ParseException {
        // still needs to be implemented
        return parseEqualsExp(startPos);
    }

    // parse statments
    // stmt ::= type var `=` exp `;` | Variable declaration
    // var `=` exp `;` | Assignment
    // `if` `(` exp `)` stmt [`else` stmt] | if
    // `while` `(` exp `)` stmt | while
    // `break` `;` | break
    // `println` `(` exp `)` | Printing something
    // `{` stmt* `}` | Block
    // `return` [exp] `;` | Return
    // exp `;` Expression statements
    public ParseResult<Stmt> parseStmt(final int startPos) throws ParseException {
        // still needs to be implemented
        throw new ParseException("parseStmt not implemented yet");
    }

    // param :: = type var
    public ParseResult<Param> parseParam(final int startPos) throws ParseException {
        final ParseResult<Type> typeRes = parseType(startPos);

        final Token varToken = getToken(typeRes.nextPos());
        if (varToken instanceof IdentifierToken identifierToken) {
            return new ParseResult<>(new Param(typeRes.result(), identifierToken.name()), typeRes.nextPos() + 1);
        } else {
            throw new ParseException("Expected identifier for parameter at position " + typeRes.nextPos());
        }
    }

    // comma_param ::= [param (`,` param)*]

    // structdef ::= `struct` structname `{` (param `;`)* `}`

    // fdef ::= `func` funcname `(` comma_param `)` `:` type `{` stmt* `}`

    // struct_actual_param ::= var `:` exp
    public ParseResult<StructActualParam> parseStructActualParam(final int startPos) throws ParseException {
        // Parse the variable name (identifier)
        final Token varToken = getToken(startPos);
        if (varToken instanceof IdentifierToken identifierToken) {
            // check if colon token is present
            assertTokenHereIs(startPos + 1, new ColonToken());
            // get
            final ParseResult<Exp> exp = parseExp(startPos + 2);
            return new ParseResult<>(new StructActualParam(identifierToken.name(), exp.result()), exp.nextPos() + 1);

        } else {
            throw new ParseException("Expected identifier for struct actual parameter at position " + startPos);
        }
    }

    // struct_actual_params ::= [struct_actual_param (`,` struct_actual_param)*]
    public ParseResult<List<StructActualParam>> parseStructActualParams(final int startPos) throws ParseException {
        List<StructActualParam> params = new ArrayList<>();
        int currentPos = startPos;
        // Try to parse the first parameter
        try {
            final ParseResult<StructActualParam> firstParam = parseStructActualParam(currentPos);
            params.add(firstParam.result());
            currentPos = firstParam.nextPos();

            // Parse remaining parameters preceded by commas
            // Use AssertTokenHereIs to check for comma
            while (currentPos < tokens.size()) {
                try {
                    assertTokenHereIs(currentPos, new CommaToken());
                    final ParseResult<StructActualParam> nextParam = parseStructActualParam(currentPos + 1);
                    params.add(nextParam.result());
                    currentPos = nextParam.nextPos();
                } catch (Exception e) {
                    throw new ParseException(
                            "Expected comma followed by struct actual parameter at position " + currentPos);
                }

            }
        } catch (final ParseException e) {
            throw new ParseException("Expected struct actual parameter after comma at position " + (currentPos + 1));
        }

        return new ParseResult<>(params, currentPos);
    }
    // comma_exp ::= [exp (`,` exp)*]

    // type ::= `int` | `bool` | `void` | structname |
    public ParseResult<Type> parseType(final int startPos) throws ParseException {
        final Token token = getToken(startPos);

        if (token instanceof IntToken) {
            return new ParseResult<>(new IntType(), startPos + 1);
        } else if (token instanceof BoolToken) {
            return new ParseResult<>(new BoolType(), startPos + 1);
        } else if (token instanceof VoidToken) {
            return new ParseResult<>(new VoidType(), startPos + 1);
        } else if (token instanceof IdentifierToken structToken) {
            // struct type - use the identifier as the struct name
            return new ParseResult<>(new StructType(structToken.name()), startPos + 1);
        } else {
            throw new ParseException("Expected type at position " + startPos + "; received: " + token.toString());
        }

    }

    // check that the token at position pos is the expected token, and throw an
    // exception if not
    public void assertTokenHereIs(final int pos, final Token expected) throws ParseException {
        final Token actual = getToken(pos);
        if (!actual.equals(expected)) {
            String message = ("Expected token: " + expected.toString() + "; received token: " + actual.toString());
            throw new ParseException(message);
        }
    }

    // program ::= structdef* fdef* stmt* (stmt* is the entry point)
    // This will have to be changed eventually to handle struct and function
    // definitions,
    // but we can start with just parsing statements for now and then add the other
    // pieces in later.
    public Program parseProgram() throws ParseException {
        // start with stmt* so I think we can use the exmaple one if not we have to
        // modify it
        List<Stmt> stmts = new ArrayList<>();
        int currentPos = 0;
        while (currentPos < tokens.size()) {
            final ParseResult<Stmt> stmt = parseStmt(currentPos);
            stmts.add(stmt.result());
            currentPos = stmt.nextPos();
        }

        if (currentPos == tokens.size()) {
            return new Program(stmts);
        } else {
            throw new ParseException("Tokens remaining at end " + currentPos);
        }
    }

    public static Program parseProgram(final List<Token> tokens) throws ParseException {
        return new Parser(tokens).parseProgram();
    }

}