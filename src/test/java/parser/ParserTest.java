package parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

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

public class ParserTest {

    // TESTS FOR parseCommaParam
    @Test
    public void testParseCommaParamEmpty() throws ParseException {
        List<Token> tokens = List.of(); // No tokens
        Parser parser = new Parser(tokens);

        ParseResult<List<Param>> result = parser.parseCommaParam(0);

        assertTrue(result.result().isEmpty());
        assertEquals(0, result.nextPos());
    }

    @Test
    public void testParseCommaParamSingle() throws ParseException {
        List<Token> tokens = List.of(new IntToken(), new IdentifierToken("x"));
        Parser parser = new Parser(tokens);

        ParseResult<List<Param>> result = parser.parseCommaParam(0);

        assertEquals(1, result.result().size());
        assertTrue(result.result().get(0).type() instanceof IntType);
        assertEquals("x", result.result().get(0).identifier());
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseCommaParamMultiple() throws ParseException {
        List<Token> tokens = List.of(
                new IntToken(), new IdentifierToken("x"),
                new CommaToken(),
                new BoolToken(), new IdentifierToken("y"));
        Parser parser = new Parser(tokens);

        ParseResult<List<Param>> result = parser.parseCommaParam(0);

        assertEquals(2, result.result().size());
        assertTrue(result.result().get(0).type() instanceof IntType);
        assertEquals("x", result.result().get(0).identifier());
        assertTrue(result.result().get(1).type() instanceof BoolType);
        assertEquals("y", result.result().get(1).identifier());
        assertEquals(5, result.nextPos());
    }

    // @Test
    // public void testParseCommaParamTrailingComma() {
    // List<Token> tokens = List.of(
    // new IntToken(), new IdentifierToken("x"),
    // new CommaToken());
    // Parser parser = new Parser(tokens);

    // assertThrows(ParseException.class, () -> parser.parseCommaParam(0));
    // }

    // @Test
    // public void testParseCommaParamInvalidAfterComma() {
    // List<Token> tokens = List.of(
    // new IntToken(), new IdentifierToken("x"),
    // new CommaToken(),
    // new CommaToken() // Invalid token after comma
    // );
    // Parser parser = new Parser(tokens);

    // assertThrows(ParseException.class, () -> parser.parseCommaParam(0));
    // }

    // END OF TESTS FOR parseCommaParam
    // TESTS FOR assertTokenHereIs
    @Test
    public void testAssertTokenHereIsSuccess() throws ParseException {
        List<Token> tokens = List.of(new ColonToken());
        Parser parser = new Parser(tokens);

        // Should not throw an exception
        assertDoesNotThrow(() -> parser.assertTokenHereIs(0, new ColonToken()));
    }

    @Test
    public void testAssertTokenHereIsFailure() {
        List<Token> tokens = List.of(new CommaToken());
        Parser parser = new Parser(tokens);

        ParseException exception = assertThrows(ParseException.class,
                () -> parser.assertTokenHereIs(0, new ColonToken()));

        assertTrue(exception.getMessage().contains("Expected token:"));
        assertTrue(exception.getMessage().contains("received token:"));
    }

    @Test
    public void testAssertTokenHereIsOutOfBounds() {
        List<Token> tokens = List.of(new IntToken());
        Parser parser = new Parser(tokens);

        assertThrows(ParseException.class, () -> parser.assertTokenHereIs(5, new ColonToken()));
    }

    // END OF TESTS FOR assertTokenHereIs
    // TESTS FOR parseType
    @Test
    public void testParseTypeInt() throws ParseException {
        List<Token> tokens = List.of(new IntToken());
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof IntType);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeBool() throws ParseException {
        List<Token> tokens = List.of(new BoolToken());
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof BoolType);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeVoid() throws ParseException {
        List<Token> tokens = List.of(new VoidToken());
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof VoidType);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeStruct() throws ParseException {
        List<Token> tokens = List.of(new IdentifierToken("Node"));
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof StructType);
        StructType structType = (StructType) result.result();
        assertEquals("Node", structType.name());
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeInvalidToken() {
        List<Token> tokens = List.of(); // Empty list - no tokens
        Parser parser = new Parser(tokens);

        assertThrows(ParseException.class, () -> parser.parseType(0));
    }

    @Test
    public void testParseTypeUnexpectedToken() {
        List<Token> tokens = List.of(new CommaToken()); // Unexpected token (not int, bool, void, or identifier)
        Parser parser = new Parser(tokens);

        ParseException exception = assertThrows(ParseException.class, () -> parser.parseType(0));
        assertEquals("Expected type at position 0; received: " + new CommaToken().toString(), exception.getMessage());
    }
    // END OF TESTS FOR parseType

    // TESTS FOR parseParam
    @Test
    public void testParseParamIntVar() throws ParseException {
        List<Token> tokens = List.of(new IntToken(), new IdentifierToken("x"));
        Parser parser = new Parser(tokens);

        ParseResult<Param> result = parser.parseParam(0);

        assertTrue(result.result().type() instanceof IntType);
        assertEquals("x", result.result().identifier());
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseParamStructType() throws ParseException {
        List<Token> tokens = List.of(new IdentifierToken("Node"), new IdentifierToken("head"));
        Parser parser = new Parser(tokens);

        ParseResult<Param> result = parser.parseParam(0);

        assertTrue(result.result().type() instanceof StructType);
        assertEquals("Node", ((StructType) result.result().type()).name());
        assertEquals("head", result.result().identifier());
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseParamMissingIdentifier() {
        List<Token> tokens = List.of(new IntToken());
        Parser parser = new Parser(tokens);

        assertThrows(ParseException.class, () -> parser.parseParam(0));
    }

    @Test
    public void testParseParamMissingIdentifierException() {
        List<Token> tokens = List.of(new IntToken(), new CommaToken()); // Comma token instead of identifier
        Parser parser = new Parser(tokens);

        ParseException exception = assertThrows(ParseException.class, () -> parser.parseParam(0));
        assertEquals("Expected identifier for parameter at position 1", exception.getMessage());
    }
    // END OF TESTS FOR parseParam

    // TESTS FOR parseDotExp
    @Test
    public void testParseDotExpSinglePrimary() throws ParseException {
        List<Token> tokens = List.of(new IntegerToken(7));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseDotExp(0);

        assertTrue(result.result() instanceof IntegerExp);
        assertEquals(7, ((IntegerExp) result.result()).integer());
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseDotExpChainedFields() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("a"),
                new DotToken(),
                new IdentifierToken("b"),
                new DotToken(),
                new IdentifierToken("c"));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseDotExp(0);

        Exp root = result.result();
        assertTrue(root instanceof DotExp);
        DotExp outer = (DotExp) root;
        assertEquals("c", outer.field());
        assertTrue(outer.base() instanceof DotExp);
        DotExp inner = (DotExp) outer.base();
        assertEquals("b", inner.field());
        assertTrue(inner.base() instanceof IdentifierExp);
        IdentifierExp id = (IdentifierExp) inner.base();
        assertEquals("a", id.identifier());
        assertEquals(5, result.nextPos());
    }

    @Test
    public void testParseDotExpMissingFieldName() {
        List<Token> tokens = List.of(new IdentifierToken("a"), new DotToken(), new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseException ex = assertThrows(ParseException.class, () -> parser.parseDotExp(0));
        assertTrue(ex.getMessage().contains("Expected field name after '.'"));
    }

    // END OF TESTS FOR parseDotExp

    // TESTS FOR parseMultExp
    @Test
    public void testParseMultExpSingleDotExp() throws ParseException {
        List<Token> tokens = List.of(new IntegerToken(2));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseMultExp(0);

        assertTrue(result.result() instanceof IntegerExp);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseMultExpTimesAndDivide() throws ParseException {
        List<Token> tokens = List.of(
                new IntegerToken(8),
                new StarToken(),
                new IntegerToken(2),
                new SlashToken(),
                new IntegerToken(4));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseMultExp(0);

        Exp root = result.result();
        assertTrue(root instanceof BinopExp);
        BinopExp rightSide = (BinopExp) root;
        assertTrue(rightSide.op() instanceof DivideOp);
        assertTrue(rightSide.right() instanceof IntegerExp);
        assertEquals(4, ((IntegerExp) rightSide.right()).integer());
        assertTrue(rightSide.left() instanceof BinopExp);
        BinopExp mul = (BinopExp) rightSide.left();
        assertTrue(mul.op() instanceof TimesOp);
        assertEquals(5, result.nextPos());
    }

    // END OF TESTS FOR parseMultExp

    // TESTS FOR parseAddExp
    @Test
    public void testParseAddExpPlusAndMinus() throws ParseException {
        List<Token> tokens = List.of(
                new IntegerToken(10),
                new MinusToken(),
                new IntegerToken(3),
                new PlusToken(),
                new IntegerToken(1));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseAddExp(0);

        Exp root = result.result();
        assertTrue(root instanceof BinopExp);
        BinopExp top = (BinopExp) root;
        assertTrue(top.op() instanceof PlusOp);
        assertTrue(top.right() instanceof IntegerExp);
        assertEquals(1, ((IntegerExp) top.right()).integer());
        assertTrue(top.left() instanceof BinopExp);
        BinopExp sub = (BinopExp) top.left();
        assertTrue(sub.op() instanceof MinusOp);
        assertEquals(5, result.nextPos());
    }

    @Test
    public void testParseAddExpRespectsMultPrecedence() throws ParseException {
        List<Token> tokens = List.of(
                new IntegerToken(1),
                new PlusToken(),
                new IntegerToken(2),
                new StarToken(),
                new IntegerToken(3));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseAddExp(0);

        Exp root = result.result();
        assertTrue(root instanceof BinopExp);
        BinopExp sum = (BinopExp) root;
        assertTrue(sum.op() instanceof PlusOp);
        assertTrue(sum.left() instanceof IntegerExp);
        assertEquals(1, ((IntegerExp) sum.left()).integer());
        assertTrue(sum.right() instanceof BinopExp);
        BinopExp prod = (BinopExp) sum.right();
        assertTrue(prod.op() instanceof TimesOp);
        assertEquals(5, result.nextPos());
    }

    // END OF TESTS FOR parseAddExp

    // TESTS FOR parseLessThanExp (relational)
    @Test
    public void testParseRelationalSingleCompare() throws ParseException {
        List<Token> tokens = List.of(
                new IntegerToken(1),
                new LessEqualToken(),
                new IntegerToken(2));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseRelationalExp(0);

        Exp root = result.result();
        assertTrue(root instanceof BinopExp);
        BinopExp le = (BinopExp) root;
        assertTrue(le.op() instanceof LessEqualOp);
        assertTrue(le.left() instanceof IntegerExp);
        assertTrue(le.right() instanceof IntegerExp);
        assertEquals(3, result.nextPos());
    }

    // END OF TESTS FOR parseLessThanExp

    // TESTS FOR parseEqualsExp / parseExp
    @Test
    public void testParseEqualsExpEqualAndNotEqual() throws ParseException {
        List<Token> tokens = List.of(
                new TrueToken(),
                new EqualEqualToken(),
                new FalseToken(),
                new NotEqualToken(),
                new NullToken());
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseEqualsExp(0);

        Exp root = result.result();
        assertTrue(root instanceof BinopExp);
        BinopExp outer = (BinopExp) root;
        assertTrue(outer.op() instanceof NotEqualOp);
        assertTrue(outer.left() instanceof BinopExp);
        BinopExp inner = (BinopExp) outer.left();
        assertTrue(inner.op() instanceof EqualEqualOp);
        assertEquals(5, result.nextPos());
    }

    @Test
    public void testParseExpDelegatesToEquals() throws ParseException {
        List<Token> tokens = List.of(new IntegerToken(42));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> fromExp = parser.parseExp(0);
        ParseResult<Exp> fromEquals = parser.parseEqualsExp(0);

        assertEquals(fromExp.nextPos(), fromEquals.nextPos());
        assertTrue(fromExp.result() instanceof IntegerExp);
        assertEquals(42, ((IntegerExp) fromExp.result()).integer());
    }

    @Test
    public void testParseExpParentheses() throws ParseException {
        List<Token> tokens = List.of(
                new LeftParenToken(),
                new IntegerToken(1),
                new PlusToken(),
                new IntegerToken(2),
                new RightParenToken(),
                new StarToken(),
                new IntegerToken(3));
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parseExp(0);

        Exp root = result.result();
        assertTrue(root instanceof BinopExp);
        BinopExp prod = (BinopExp) root;
        assertTrue(prod.op() instanceof TimesOp);
        assertTrue(prod.left() instanceof ParenExp);
        ParenExp paren = (ParenExp) prod.left();
        assertTrue(paren.exp() instanceof BinopExp);
        BinopExp inner = (BinopExp) paren.exp();
        assertTrue(inner.op() instanceof PlusOp);
        assertEquals(7, result.nextPos());
    }

    // END OF TESTS FOR parseEqualsExp / parseExp

    // TESTS FOR parseStmt
    @Test
    public void testParseStmtBlock() throws ParseException {
        List<Token> tokens = List.of(
                new LeftBraceToken(),
                new BreakToken(),
                new SemicolonToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof BlockStmt);
        BlockStmt block = (BlockStmt) stmt;
        assertEquals(1, block.body().size());
        assertTrue(block.body().get(0) instanceof BreakStmt);
        assertEquals(4, result.nextPos());
    }

    @Test
    public void testParseStmtIfWithElse() throws ParseException {
        List<Token> tokens = List.of(
                new IfToken(),
                new LeftParenToken(),
                new TrueToken(),
                new RightParenToken(),
                new BreakToken(),
                new SemicolonToken(),
                new ElseToken(),
                new BreakToken(),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof IfStmt);
        IfStmt ifStmt = (IfStmt) stmt;
        assertTrue(ifStmt.condition() instanceof TrueExp);
        assertTrue(ifStmt.thenBranch() instanceof BreakStmt);
        assertTrue(ifStmt.elseBranch() instanceof BreakStmt);
        assertEquals(9, result.nextPos());
    }

    @Test
    public void testParseStmtIfWithoutElse() throws ParseException {
        List<Token> tokens = List.of(
                new IfToken(),
                new LeftParenToken(),
                new FalseToken(),
                new RightParenToken(),
                new BreakToken(),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof IfStmt);
        IfStmt ifStmt = (IfStmt) stmt;
        assertTrue(ifStmt.condition() instanceof FalseExp);
        assertTrue(ifStmt.thenBranch() instanceof BreakStmt);
        assertTrue(ifStmt.elseBranch() == null);
        assertEquals(6, result.nextPos());
    }

    @Test
    public void testParseStmtWhile() throws ParseException {
        List<Token> tokens = List.of(
                new WhileToken(),
                new LeftParenToken(),
                new IntegerToken(1),
                new LessToken(),
                new IntegerToken(2),
                new RightParenToken(),
                new BreakToken(),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof WhileStmt);
        WhileStmt loop = (WhileStmt) stmt;
        assertTrue(loop.condition() instanceof BinopExp);
        BinopExp cmp = (BinopExp) loop.condition();
        assertTrue(cmp.op() instanceof LessThanOp);
        assertTrue(loop.body() instanceof BreakStmt);
        assertEquals(8, result.nextPos());
    }

    @Test
    public void testParseStmtBreak() throws ParseException {
        List<Token> tokens = List.of(new BreakToken(), new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        assertTrue(result.result() instanceof BreakStmt);
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseStmtPrintln() throws ParseException {
        List<Token> tokens = List.of(
                new PrintlnToken(),
                new LeftParenToken(),
                new IntegerToken(5),
                new RightParenToken(),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof PrintlnStmt);
        PrintlnStmt println = (PrintlnStmt) stmt;
        assertTrue(println.expression() instanceof IntegerExp);
        assertEquals(5, result.nextPos());
    }

    @Test
    public void testParseStmtReturnVoid() throws ParseException {
        List<Token> tokens = List.of(new ReturnToken(), new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof ReturnStmt);
        ReturnStmt ret = (ReturnStmt) stmt;
        assertTrue(ret.value() == null);
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseStmtReturnWithValue() throws ParseException {
        List<Token> tokens = List.of(new ReturnToken(), new IntegerToken(9), new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof ReturnStmt);
        ReturnStmt ret = (ReturnStmt) stmt;
        assertTrue(ret.value() instanceof IntegerExp);
        assertEquals(9, ((IntegerExp) ret.value()).integer());
        assertEquals(3, result.nextPos());
    }

    @Test
    public void testParseStmtVarDeclPrimitive() throws ParseException {
        List<Token> tokens = List.of(
                new IntToken(),
                new IdentifierToken("x"),
                new AssignToken(),
                new IntegerToken(1),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof VarDeclStmt);
        VarDeclStmt decl = (VarDeclStmt) stmt;
        assertTrue(decl.type() instanceof IntType);
        assertEquals("x", decl.name());
        assertTrue(decl.initializer() instanceof IntegerExp);
        assertEquals(5, result.nextPos());
    }

    @Test
    public void testParseStmtVarDeclStructTyped() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("Node"),
                new IdentifierToken("n"),
                new AssignToken(),
                new NullToken(),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof VarDeclStmt);
        VarDeclStmt decl = (VarDeclStmt) stmt;
        assertTrue(decl.type() instanceof StructType);
        StructType st = (StructType) decl.type();
        assertEquals("Node", st.name());
        assertEquals("n", decl.name());
        assertTrue(decl.initializer() instanceof NullExp);
        assertEquals(5, result.nextPos());
    }

    @Test
    public void testParseStmtAssign() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("y"),
                new AssignToken(),
                new IntegerToken(2),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof AssignStmt);
        AssignStmt assign = (AssignStmt) stmt;
        assertEquals("y", assign.variable());
        assertTrue(assign.expression() instanceof IntegerExp);
        assertEquals(4, result.nextPos());
    }

    @Test
    public void testParseStmtExpressionStatement() throws ParseException {
        List<Token> tokens = List.of(new IdentifierToken("z"), new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        Stmt stmt = result.result();
        assertTrue(stmt instanceof ExprStmt);
        ExprStmt expr = (ExprStmt) stmt;
        assertTrue(expr.expression() instanceof IdentifierExp);
        assertEquals(2, result.nextPos());
    }

    // END OF TESTS FOR parseStmt

    @Test
    public void testParseCommaExpEmpty() throws ParseException {
        List<Token> tokens = List.of(new RightParenToken());
        Parser parser = new Parser(tokens);

        ParseResult<List<Exp>> result = parser.parseCommaExp(0);

        assertTrue(result.result().isEmpty());
        assertEquals(0, result.nextPos());
    }

    @Test
    public void testParseCommaExpTwoArgs() throws ParseException {
        List<Token> tokens = List.of(
                new IntegerToken(1),
                new CommaToken(),
                new TrueToken(),
                new RightParenToken());
        Parser parser = new Parser(tokens);

        ParseResult<List<Exp>> result = parser.parseCommaExp(0);

        assertEquals(2, result.result().size());
        assertTrue(result.result().get(0) instanceof IntegerExp);
        assertTrue(result.result().get(1) instanceof TrueExp);
        assertEquals(3, result.nextPos());
    }

    @Test
    public void testParseStructActualParamsEmpty() throws ParseException {
        List<Token> tokens = List.of(new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<List<StructActualParam>> result = parser.parseStructActualParams(0);

        assertTrue(result.result().isEmpty());
        assertEquals(0, result.nextPos());
    }

    @Test
    public void testParseStructActualParamsTwo() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("a"),
                new ColonToken(),
                new IntegerToken(0),
                new CommaToken(),
                new IdentifierToken("b"),
                new ColonToken(),
                new FalseToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<List<StructActualParam>> result = parser.parseStructActualParams(0);

        assertEquals(2, result.result().size());
        assertEquals("a", result.result().get(0).identifier());
        assertEquals("b", result.result().get(1).identifier());
        assertEquals(7, result.nextPos());
    }

    @Test
    public void testParsePrimaryExpNewEmpty() throws ParseException {
        List<Token> tokens = List.of(
                new NewToken(),
                new IdentifierToken("Node"),
                new LeftBraceToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parsePrimaryExp(0);

        assertTrue(result.result() instanceof NewExp);
        NewExp ne = (NewExp) result.result();
        assertEquals("Node", ne.structName());
        assertTrue(ne.fields().isEmpty());
        assertEquals(4, result.nextPos());
    }

    @Test
    public void testParsePrimaryExpCallEmpty() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("length"),
                new LeftParenToken(),
                new RightParenToken());
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parsePrimaryExp(0);

        assertTrue(result.result() instanceof CallExp);
        CallExp call = (CallExp) result.result();
        assertEquals("length", call.funcName());
        assertTrue(call.arguments().isEmpty());
        assertEquals(3, result.nextPos());
    }

    @Test
    public void testParsePrimaryExpCallTwoArgs() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("f"),
                new LeftParenToken(),
                new IntegerToken(1),
                new CommaToken(),
                new IntegerToken(2),
                new RightParenToken());
        Parser parser = new Parser(tokens);

        ParseResult<Exp> result = parser.parsePrimaryExp(0);

        assertTrue(result.result() instanceof CallExp);
        CallExp call = (CallExp) result.result();
        assertEquals(2, call.arguments().size());
        assertEquals(6, result.nextPos());
    }

    @Test
    public void testParseStructDefMinimal() throws ParseException {
        List<Token> tokens = List.of(
                new StructToken(),
                new IdentifierToken("S"),
                new LeftBraceToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<StructDef> result = parser.parseStructDef(0);

        assertEquals("S", result.result().name());
        assertTrue(result.result().fields().isEmpty());
        assertEquals(4, result.nextPos());
    }

    @Test
    public void testParseStructDefFields() throws ParseException {
        List<Token> tokens = List.of(
                new StructToken(),
                new IdentifierToken("Node"),
                new LeftBraceToken(),
                new IntToken(),
                new IdentifierToken("value"),
                new SemicolonToken(),
                new IdentifierToken("Node"),
                new IdentifierToken("rest"),
                new SemicolonToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<StructDef> result = parser.parseStructDef(0);

        assertEquals("Node", result.result().name());
        assertEquals(2, result.result().fields().size());
        assertEquals("value", result.result().fields().get(0).identifier());
        assertEquals("rest", result.result().fields().get(1).identifier());
        assertEquals(10, result.nextPos());
    }

    @Test
    public void testParseFuncDefEmptyBody() throws ParseException {
        List<Token> tokens = List.of(
                new FuncToken(),
                new IdentifierToken("f"),
                new LeftParenToken(),
                new RightParenToken(),
                new ColonToken(),
                new VoidToken(),
                new LeftBraceToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<FuncDef> result = parser.parseFuncDef(0);

        assertEquals("f", result.result().name());
        assertTrue(result.result().parameters().isEmpty());
        assertTrue(result.result().returnType() instanceof VoidType);
        assertTrue(result.result().body().isEmpty());
        assertEquals(8, result.nextPos());
    }

    @Test
    public void testParseFuncDefOneStmt() throws ParseException {
        List<Token> tokens = List.of(
                new FuncToken(),
                new IdentifierToken("g"),
                new LeftParenToken(),
                new IntToken(),
                new IdentifierToken("x"),
                new RightParenToken(),
                new ColonToken(),
                new IntToken(),
                new LeftBraceToken(),
                new ReturnToken(),
                new IdentifierToken("x"),
                new SemicolonToken(),
                new RightBraceToken());
        Parser parser = new Parser(tokens);

        ParseResult<FuncDef> result = parser.parseFuncDef(0);

        assertEquals("g", result.result().name());
        assertEquals(1, result.result().parameters().size());
        assertEquals(1, result.result().body().size());
        assertTrue(result.result().body().get(0) instanceof ReturnStmt);
        assertEquals(13, result.nextPos());
    }

    @Test
    public void testParseProgramStructFuncAndStmt() throws ParseException {
        List<Token> tokens = List.of(
                new StructToken(),
                new IdentifierToken("Node"),
                new LeftBraceToken(),
                new IntToken(),
                new IdentifierToken("v"),
                new SemicolonToken(),
                new RightBraceToken(),
                new FuncToken(),
                new IdentifierToken("id"),
                new LeftParenToken(),
                new IdentifierToken("Node"),
                new IdentifierToken("n"),
                new RightParenToken(),
                new ColonToken(),
                new IdentifierToken("Node"),
                new LeftBraceToken(),
                new ReturnToken(),
                new IdentifierToken("n"),
                new SemicolonToken(),
                new RightBraceToken(),
                new IntToken(),
                new IdentifierToken("a"),
                new AssignToken(),
                new IntegerToken(1),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        Program program = parser.parseProgram();

        assertEquals(1, program.structDefs().size());
        assertEquals("Node", program.structDefs().get(0).name());
        assertEquals(1, program.funcDefs().size());
        assertEquals("id", program.funcDefs().get(0).name());
        assertEquals(1, program.stmts().size());
        assertTrue(program.stmts().get(0) instanceof VarDeclStmt);
    }

    @Test
    public void testParseStmtCallExpressionStatement() throws ParseException {
        List<Token> tokens = List.of(
                new IdentifierToken("f"),
                new LeftParenToken(),
                new RightParenToken(),
                new SemicolonToken());
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> result = parser.parseStmt(0);

        assertTrue(result.result() instanceof ExprStmt);
        ExprStmt es = (ExprStmt) result.result();
        assertTrue(es.expression() instanceof CallExp);
        assertEquals(4, result.nextPos());
    }

}
