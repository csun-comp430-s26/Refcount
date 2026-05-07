package parser;

import java.util.ArrayList;
import java.util.List;

import tokenizer.AssignToken;
import tokenizer.BoolToken;
import tokenizer.BreakToken;
import tokenizer.ColonToken;
import tokenizer.CommaToken;
import tokenizer.DotToken;
import tokenizer.ElseToken;
import tokenizer.EqualEqualToken;
import tokenizer.FalseToken;
import tokenizer.FuncToken;
import tokenizer.FuncToken;
import tokenizer.GreaterEqualToken;
import tokenizer.GreaterToken;
import tokenizer.IdentifierToken;
import tokenizer.IfToken;
import tokenizer.IntToken;
import tokenizer.IntegerToken;
import tokenizer.LeftBraceToken;
import tokenizer.LeftParenToken;
import tokenizer.LessEqualToken;
import tokenizer.LessToken;
import tokenizer.MinusToken;
import tokenizer.NewToken;
import tokenizer.NotEqualToken;
import tokenizer.NullToken;
import tokenizer.PlusToken;
import tokenizer.PlusToken;
import tokenizer.PrintlnToken;
import tokenizer.ReturnToken;
import tokenizer.RightBraceToken;
import tokenizer.RightParenToken;
import tokenizer.SemicolonToken;
import tokenizer.SlashToken;
import tokenizer.StarToken;
import tokenizer.StructToken;
import tokenizer.Token;
import tokenizer.TrueToken;
import tokenizer.VoidToken;
import tokenizer.WhileToken;

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

    private ParseResult<List<Stmt>> parseStmtListUntilRightBrace(final int startPos) throws ParseException {
        final List<Stmt> body = new ArrayList<>();
        int pos = startPos;
        while (!(getToken(pos) instanceof RightBraceToken)) {
            final ParseResult<Stmt> inner = parseStmt(pos);
            body.add(inner.result());
            pos = inner.nextPos();
        }
        return new ParseResult<>(body, pos + 1);
    }

    // primaryExp::= i | `true` | `false` | var | `null` | `(` exp `)`
    // | `new` structname `{` struct_actual_params `}` | funcname `(` comma_exp `)`
    public ParseResult<Exp> parsePrimaryExp(final int startPos) throws ParseException {
        final Token firstToken = getToken(startPos);

        if (firstToken instanceof IntegerToken intToken) {
            return new ParseResult<>(new IntegerExp(intToken.value()), startPos + 1);

        } else if (firstToken instanceof TrueToken) {
            return new ParseResult<>(new TrueExp(), startPos + 1);

        } else if (firstToken instanceof FalseToken) {
            return new ParseResult<>(new FalseExp(), startPos + 1);

        } else if (firstToken instanceof NewToken) {
            final Token structNameTok = getToken(startPos + 1);
            if (!(structNameTok instanceof IdentifierToken id)) {
                throw new ParseException("Expected struct name after 'new' at position " + (startPos + 1));
            }
            assertTokenHereIs(startPos + 2, new LeftBraceToken());
            final ParseResult<List<StructActualParam>> fields = parseStructActualParams(startPos + 3);
            assertTokenHereIs(fields.nextPos(), new RightBraceToken());
            return new ParseResult<>(new NewExp(id.name(), fields.result()), fields.nextPos() + 1);

            // Use IdentifierToken for variables, since VarToken is only used for variable
            // declarations
        } else if (firstToken instanceof IdentifierToken varToken) {
            if (startPos + 1 < tokens.size() && getToken(startPos + 1) instanceof LeftParenToken) {
                final ParseResult<List<Exp>> args = parseCommaExp(startPos + 2);
                assertTokenHereIs(args.nextPos(), new RightParenToken());
                return new ParseResult<>(new CallExp(varToken.name(), args.result()), args.nextPos() + 1);
            }
            return new ParseResult<>(new IdentifierExp(varToken.name()), startPos + 1);

        } else if (firstToken instanceof NullToken) {
            return new ParseResult<>(new NullExp(), startPos + 1);
            // expresssion inside parens
        } else if (firstToken instanceof LeftParenToken) {
            final ParseResult<Exp> exp = parseExp(startPos + 1);
            assertTokenHereIs(exp.nextPos(), new RightParenToken());
            return new ParseResult<>(new ParenExp(exp.result()), exp.nextPos() + 1);
        } else {
            throw new ParseException("Expected primary expression at position " + startPos + "; received: "
                    + firstToken.toString());
        }
    }

    // each level uses the previous level to parse its subexpressions, and then
    // checks for the appropriate operator tokens in between

    // dot_exp ::= primary_exp (`.` var)*
    public ParseResult<Exp> parseDotExp(final int startPos) throws ParseException {
        ParseResult<Exp> current = parsePrimaryExp(startPos);
        int pos = current.nextPos();
        while (pos < tokens.size() && getToken(pos) instanceof DotToken) {
            pos++;
            final Token fieldTok = getToken(pos);
            if (!(fieldTok instanceof IdentifierToken id)) {
                throw new ParseException("Expected field name after '.' at position " + pos);
            }
            current = new ParseResult<>(new DotExp(current.result(), id.name()), pos + 1);
            pos = current.nextPos();
        }
        return current;
    }

    // mult_exp ::= dot_exp ((`*` | `/`) dot_exp)*
    public ParseResult<Exp> parseMultExp(final int startPos) throws ParseException {
        ParseResult<Exp> left = parseDotExp(startPos);
        int pos = left.nextPos();
        while (pos < tokens.size()) {
            final Token opTok = getToken(pos);
            final Op op;
            if (opTok instanceof StarToken) {
                op = new TimesOp();
            } else if (opTok instanceof SlashToken) {
                op = new DivideOp();
            } else {
                break;
            }
            final ParseResult<Exp> right = parseDotExp(pos + 1);
            left = new ParseResult<>(new BinopExp(left.result(), op, right.result()), right.nextPos());
            pos = left.nextPos();
        }
        return left;
    }

    // add_exp ::= mult_exp ((`+` | `-`) mult_exp)*
    public ParseResult<Exp> parseAddExp(final int startPos) throws ParseException {
        ParseResult<Exp> left = parseMultExp(startPos);
        int pos = left.nextPos();
        while (pos < tokens.size()) {
            final Token opTok = getToken(pos);
            final Op op;
            if (opTok instanceof PlusToken) {
                op = new PlusOp();
            } else if (opTok instanceof MinusToken) {
                op = new MinusOp();
            } else {
                break;
            }
            final ParseResult<Exp> right = parseMultExp(pos + 1);
            left = new ParseResult<>(new BinopExp(left.result(), op, right.result()), right.nextPos());
            pos = left.nextPos();
        }
        return left;
    }

    // relational_exp ::= add_exp ( (`<` | `<=` | `>` | `>=`) add_exp )  (at most one compare; PDF)
    public ParseResult<Exp> parseRelationalExp(final int startPos) throws ParseException {
        final ParseResult<Exp> left = parseAddExp(startPos);
        final int pos = left.nextPos();
        if (pos >= tokens.size()) {
            return left;
        }
        final Token opTok = getToken(pos);
        final Op op;
        if (opTok instanceof LessToken) {
            op = new LessThanOp();
        } else if (opTok instanceof LessEqualToken) {
            op = new LessEqualOp();
        } else if (opTok instanceof GreaterToken) {
            op = new GreaterThanOp();
        } else if (opTok instanceof GreaterEqualToken) {
            op = new GreaterEqualOp();
        } else {
            return left;
        }
        final ParseResult<Exp> right = parseAddExp(pos + 1);
        return new ParseResult<>(new BinopExp(left.result(), op, right.result()), right.nextPos());
    }

    public ParseResult<Exp> parseLessThanExp(final int startPos) throws ParseException {
        return parseRelationalExp(startPos);
    }

    // equals_exp ::= relational_exp ( (`==` | `!=`) relational_exp )*
    public ParseResult<Exp> parseEqualsExp(final int startPos) throws ParseException {
        ParseResult<Exp> left = parseRelationalExp(startPos);
        int pos = left.nextPos();
        while (pos < tokens.size()) {
            final Token opTok = getToken(pos);
            final Op op;
            if (opTok instanceof EqualEqualToken) {
                op = new EqualEqualOp();
            } else if (opTok instanceof NotEqualToken) {
                op = new NotEqualOp();
            } else {
                break;
            }
            final ParseResult<Exp> right = parseRelationalExp(pos + 1);
            left = new ParseResult<>(new BinopExp(left.result(), op, right.result()), right.nextPos());
            pos = left.nextPos();
        }
        return left;
    }

    // parseExp: exp ::= equals_exp
    public ParseResult<Exp> parseExp(final int startPos) throws ParseException {
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
        final Token first = getToken(startPos);

        if (first instanceof LeftBraceToken) {
            final ParseResult<List<Stmt>> block = parseStmtListUntilRightBrace(startPos + 1);
            return new ParseResult<>(new BlockStmt(block.result()), block.nextPos());
        }

        if (first instanceof IfToken) {
            assertTokenHereIs(startPos + 1, new LeftParenToken());
            final ParseResult<Exp> cond = parseExp(startPos + 2);
            assertTokenHereIs(cond.nextPos(), new RightParenToken());
            final ParseResult<Stmt> thenBranch = parseStmt(cond.nextPos() + 1);
            int afterThen = thenBranch.nextPos();
            if (afterThen < tokens.size() && getToken(afterThen) instanceof ElseToken) {
                final ParseResult<Stmt> elseBranch = parseStmt(afterThen + 1);
                return new ParseResult<>(new IfStmt(cond.result(), thenBranch.result(), elseBranch.result()),
                        elseBranch.nextPos());
            }
            return new ParseResult<>(new IfStmt(cond.result(), thenBranch.result()), afterThen);
        }

        if (first instanceof WhileToken) {
            assertTokenHereIs(startPos + 1, new LeftParenToken());
            final ParseResult<Exp> cond = parseExp(startPos + 2);
            assertTokenHereIs(cond.nextPos(), new RightParenToken());
            final ParseResult<Stmt> body = parseStmt(cond.nextPos() + 1);
            return new ParseResult<>(new WhileStmt(cond.result(), body.result()), body.nextPos());
        }

        if (first instanceof BreakToken) {
            assertTokenHereIs(startPos + 1, new SemicolonToken());
            return new ParseResult<>(new BreakStmt(), startPos + 2);
        }

        if (first instanceof PrintlnToken) {
            assertTokenHereIs(startPos + 1, new LeftParenToken());
            final ParseResult<Exp> arg = parseExp(startPos + 2);
            assertTokenHereIs(arg.nextPos(), new RightParenToken());
            assertTokenHereIs(arg.nextPos() + 1, new SemicolonToken());
            return new ParseResult<>(new PrintlnStmt(arg.result()), arg.nextPos() + 2);
        }

        if (first instanceof ReturnToken) {
            if (startPos + 1 < tokens.size() && getToken(startPos + 1) instanceof SemicolonToken) {
                return new ParseResult<>(new ReturnStmt(null), startPos + 2);
            }
            final ParseResult<Exp> value = parseExp(startPos + 1);
            assertTokenHereIs(value.nextPos(), new SemicolonToken());
            return new ParseResult<>(new ReturnStmt(value.result()), value.nextPos() + 1);
        }

        if (first instanceof IntToken || first instanceof BoolToken || first instanceof VoidToken) {
            final ParseResult<Type> typeRes = parseType(startPos);
            final Token nameTok = getToken(typeRes.nextPos());
            if (!(nameTok instanceof IdentifierToken id)) {
                throw new ParseException("Expected identifier after type at position " + typeRes.nextPos());
            }
            assertTokenHereIs(typeRes.nextPos() + 1, new AssignToken());
            final ParseResult<Exp> init = parseExp(typeRes.nextPos() + 2);
            assertTokenHereIs(init.nextPos(), new SemicolonToken());
            return new ParseResult<>(new VarDeclStmt(typeRes.result(), id.name(), init.result()),
                    init.nextPos() + 1);
        }

        if (first instanceof IdentifierToken idFirst) {
            if (startPos + 2 < tokens.size()
                    && getToken(startPos + 1) instanceof IdentifierToken idSecond
                    && getToken(startPos + 2) instanceof AssignToken) {
                final ParseResult<Type> structType = parseType(startPos);
                final Token nameTok = getToken(structType.nextPos());
                if (!(nameTok instanceof IdentifierToken varName)) {
                    throw new ParseException("Expected variable name at position " + structType.nextPos());
                }
                assertTokenHereIs(structType.nextPos() + 1, new AssignToken());
                final ParseResult<Exp> init = parseExp(structType.nextPos() + 2);
                assertTokenHereIs(init.nextPos(), new SemicolonToken());
                return new ParseResult<>(new VarDeclStmt(structType.result(), varName.name(), init.result()),
                        init.nextPos() + 1);
            }
            if (startPos + 1 < tokens.size() && getToken(startPos + 1) instanceof AssignToken) {
                final ParseResult<Exp> rhs = parseExp(startPos + 2);
                assertTokenHereIs(rhs.nextPos(), new SemicolonToken());
                return new ParseResult<>(new AssignStmt(idFirst.name(), rhs.result()), rhs.nextPos() + 1);
            }
        }

        final ParseResult<Exp> exp = parseExp(startPos);
        assertTokenHereIs(exp.nextPos(), new SemicolonToken());
        return new ParseResult<>(new ExprStmt(exp.result()), exp.nextPos() + 1);
    }

    // param :: = type var
    public ParseResult<Param> parseParam(final int startPos) throws ParseException {
        // get the type first
        // get the type first
        final ParseResult<Type> typeRes = parseType(startPos);

        final Token varToken = getToken(typeRes.nextPos());
        if (varToken instanceof IdentifierToken identifierToken) {
            return new ParseResult<>(new Param(typeRes.result(), identifierToken.name()), typeRes.nextPos() + 1);
        } else {
            throw new ParseException("Expected identifier for parameter at position " + typeRes.nextPos());
        }
    }

    // comma_param ::= [param (`,` param)*]


    public ParseResult<List<Param>> parseCommaParam(final int startPos) throws ParseException {
        if (startPos >= tokens.size()) {
            return new ParseResult<>(new ArrayList<>(), startPos);
        }
        if (getToken(startPos) instanceof RightParenToken) {
            return new ParseResult<>(new ArrayList<>(), startPos);
        }
        final List<Param> params = new ArrayList<>();
        ParseResult<Param> cur = parseParam(startPos);
        params.add(cur.result());
        int pos = cur.nextPos();
        while (pos < tokens.size() && getToken(pos) instanceof CommaToken) {
            cur = parseParam(pos + 1);
            params.add(cur.result());
            pos = cur.nextPos();
        }
        return new ParseResult<>(params, pos);
    }

    // structdef ::= `struct` structname `{` (param `;`)* `}`
    public ParseResult<StructDef> parseStructDef(final int startPos) throws ParseException {
        if (!(getToken(startPos) instanceof StructToken)) {
            throw new ParseException("Expected 'struct' at position " + startPos);
        }
        final Token nameTok = getToken(startPos + 1);
        if (!(nameTok instanceof IdentifierToken id)) {
            throw new ParseException("Expected struct name at position " + (startPos + 1));
        }
        assertTokenHereIs(startPos + 2, new LeftBraceToken());
        final List<Param> fields = new ArrayList<>();
        int pos = startPos + 3;
        while (!(getToken(pos) instanceof RightBraceToken)) {
            final ParseResult<Param> p = parseParam(pos);
            fields.add(p.result());
            assertTokenHereIs(p.nextPos(), new SemicolonToken());
            pos = p.nextPos() + 1;
        }
        return new ParseResult<>(new StructDef(id.name(), fields), pos + 1);
    }

    // fdef ::= `func` funcname `(` comma_param `)` `:` type `{` stmt* `}`
    public ParseResult<FuncDef> parseFuncDef(final int startPos) throws ParseException {
        if (!(getToken(startPos) instanceof FuncToken)) {
            throw new ParseException("Expected 'func' at position " + startPos);
        }
        final Token nameTok = getToken(startPos + 1);
        if (!(nameTok instanceof IdentifierToken id)) {
            throw new ParseException("Expected function name at position " + (startPos + 1));
        }
        assertTokenHereIs(startPos + 2, new LeftParenToken());
        final ParseResult<List<Param>> params = parseCommaParam(startPos + 3);
        assertTokenHereIs(params.nextPos(), new RightParenToken());
        final int afterParen = params.nextPos() + 1;
        assertTokenHereIs(afterParen, new ColonToken());
        final ParseResult<Type> retType = parseType(afterParen + 1);
        assertTokenHereIs(retType.nextPos(), new LeftBraceToken());
        final ParseResult<List<Stmt>> body = parseStmtListUntilRightBrace(retType.nextPos() + 1);
        return new ParseResult<>(new FuncDef(id.name(), params.result(), retType.result(), body.result()),
                body.nextPos());
    }

    // struct_actual_param ::= var `:` exp
    public ParseResult<StructActualParam> parseStructActualParam(final int startPos) throws ParseException {
        final Token varToken = getToken(startPos);
        if (varToken instanceof IdentifierToken identifierToken) {
            assertTokenHereIs(startPos + 1, new ColonToken());
            final ParseResult<Exp> exp = parseExp(startPos + 2);
            return new ParseResult<>(new StructActualParam(identifierToken.name(), exp.result()), exp.nextPos());

        } else {
            throw new ParseException("Expected identifier for struct actual parameter at position " + startPos);
        }
    }

    // struct_actual_params ::= [struct_actual_param (`,` struct_actual_param)*]
    public ParseResult<List<StructActualParam>> parseStructActualParams(final int startPos) throws ParseException {
        if (startPos >= tokens.size()) {
            throw new ParseException("Expected struct field list at position " + startPos);
        }
        if (getToken(startPos) instanceof RightBraceToken) {
            return new ParseResult<>(new ArrayList<>(), startPos);
        }
        final List<StructActualParam> params = new ArrayList<>();
        ParseResult<StructActualParam> cur = parseStructActualParam(startPos);
        params.add(cur.result());
        int pos = cur.nextPos();
        while (pos < tokens.size() && getToken(pos) instanceof CommaToken) {
            cur = parseStructActualParam(pos + 1);
            params.add(cur.result());
            pos = cur.nextPos();
        }
        return new ParseResult<>(params, pos);
    }

    // comma_exp ::= [exp (`,` exp)*]
    public ParseResult<List<Exp>> parseCommaExp(final int startPos) throws ParseException {
        if (startPos >= tokens.size()) {
            return new ParseResult<>(new ArrayList<>(), startPos);
        }
        if (getToken(startPos) instanceof RightParenToken) {
            return new ParseResult<>(new ArrayList<>(), startPos);
        }
        final List<Exp> exps = new ArrayList<>();
        ParseResult<Exp> cur = parseExp(startPos);
        exps.add(cur.result());
        int pos = cur.nextPos();
        while (pos < tokens.size() && getToken(pos) instanceof CommaToken) {
            cur = parseExp(pos + 1);
            exps.add(cur.result());
            pos = cur.nextPos();
        }
        return new ParseResult<>(exps, pos);
    }

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
    public Program parseProgram() throws ParseException {
        final List<StructDef> structDefs = new ArrayList<>();
        final List<FuncDef> funcDefs = new ArrayList<>();
        final List<Stmt> stmts = new ArrayList<>();
        int currentPos = 0;
        while (currentPos < tokens.size()) {
            final Token next = getToken(currentPos);
            if (next instanceof StructToken) {
                final ParseResult<StructDef> sd = parseStructDef(currentPos);
                structDefs.add(sd.result());
                currentPos = sd.nextPos();
            } else if (next instanceof FuncToken) {
                final ParseResult<FuncDef> fd = parseFuncDef(currentPos);
                funcDefs.add(fd.result());
                currentPos = fd.nextPos();
            } else {
                final ParseResult<Stmt> stmt = parseStmt(currentPos);
                stmts.add(stmt.result());
                currentPos = stmt.nextPos();
            }
        }
        return new Program(structDefs, funcDefs, stmts);
    }

    public static Program parseProgram(final List<Token> tokens) throws ParseException {
        return new Parser(tokens).parseProgram();
    }

}