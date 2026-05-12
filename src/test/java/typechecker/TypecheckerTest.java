package typechecker;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import parser.*;

public class TypecheckerTest {

        @Test
        public void testIntLiteralType() throws TypeErrorException {
                Type t = typechecker.Typechecker.typeof(
                                new IntegerExp(5),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), t);
        }

        @Test
        public void testBoolLiteralType() throws TypeErrorException {
                Type t = Typechecker.typeof(
                                new BoolExp(true),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), t);
        }

        @Test
        public void testAdditionType() throws TypeErrorException {
                Exp exp = new BinopExp(
                                new IntegerExp(1),
                                new PlusOp(),
                                new IntegerExp(2));

                Type t = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), t);
        }

        @Test
        public void testBadAdditionThrows() {
                Exp exp = new BinopExp(
                                new IntegerExp(1),
                                new PlusOp(),
                                new BoolExp(true));

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>()));
        }

        @Test
        public void testDuplicateStructThrows() {
                StructDef s1 = new StructDef("Node", Arrays.asList());
                StructDef s2 = new StructDef("Node", Arrays.asList());

                assertThrows(TypeErrorException.class, () -> Typechecker.buildStructEnv(Arrays.asList(s1, s2)));
        }

        @Test
        public void testDuplicateStructFieldThrows() {
                StructDef node = new StructDef(
                                "Node",
                                Arrays.asList(
                                                new Param(new IntType(), "value"),
                                                new Param(new BoolType(), "value")));

                assertThrows(TypeErrorException.class, () -> Typechecker.buildStructEnv(Arrays.asList(node)));
        }

        @Test
        public void testStructFieldCannotBeVoid() throws TypeErrorException {
                StructDef bad = new StructDef(
                                "Bad",
                                Arrays.asList(new Param(new VoidType(), "x")));

                var structEnv = Typechecker.buildStructEnv(Arrays.asList(bad));

                assertThrows(TypeErrorException.class,
                                () -> Typechecker.typecheckStructs(Arrays.asList(bad), structEnv));
        }

        @Test
        public void testNewStructType() throws TypeErrorException {
                StructDef node = new StructDef(
                                "Node",
                                Arrays.asList(
                                                new Param(new IntType(), "value"),
                                                new Param(new StructType("Node"), "rest")));

                var structEnv = Typechecker.buildStructEnv(Arrays.asList(node));
                Typechecker.typecheckStructs(Arrays.asList(node), structEnv);

                Exp exp = new NewExp(
                                "Node",
                                Arrays.asList(
                                                new StructActualParam("value", new IntegerExp(0)),
                                                new StructActualParam("rest", new NullExp())));

                Type t = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                structEnv,
                                new java.util.HashMap<>());

                assertEquals(new StructType("Node"), t);
        }

        @Test
        public void testNewStructMissingFieldThrows() throws TypeErrorException {
                StructDef node = new StructDef(
                                "Node",
                                Arrays.asList(
                                                new Param(new IntType(), "value"),
                                                new Param(new StructType("Node"), "rest")));

                var structEnv = Typechecker.buildStructEnv(Arrays.asList(node));
                Typechecker.typecheckStructs(Arrays.asList(node), structEnv);

                Exp exp = new NewExp(
                                "Node",
                                Arrays.asList(
                                                new StructActualParam("value", new IntegerExp(0))));

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                structEnv,
                                new java.util.HashMap<>()));
        }

        @Test
        public void testDotExpType() throws TypeErrorException {
                StructDef node = new StructDef(
                                "Node",
                                Arrays.asList(
                                                new Param(new IntType(), "value"),
                                                new Param(new StructType("Node"), "rest")));

                var structEnv = Typechecker.buildStructEnv(Arrays.asList(node));
                Typechecker.typecheckStructs(Arrays.asList(node), structEnv);

                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("list"), new StructType("Node"));

                Exp exp = new DotExp(
                                new IdentifierExp(new Identifier("list")),
                                "rest");

                Type t = Typechecker.typeof(
                                exp,
                                typeEnv,
                                structEnv,
                                new java.util.HashMap<>());

                assertEquals(new StructType("Node"), t);
        }

        @Test
        public void testStructNotEqualNullType() throws TypeErrorException {
                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("list"), new StructType("Node"));

                Exp exp = new BinopExp(
                                new IdentifierExp(new Identifier("list")),
                                new NotEqualOp(),
                                new NullExp());

                Type t = Typechecker.typeof(
                                exp,
                                typeEnv,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), t);
        }

        @Test
        public void testDuplicateVariableDeclarationThrows() {
                Stmt block = new BlockStmt(Arrays.asList(
                                new VarDeclStmt(new IntType(), new Identifier("x"), new IntegerExp(1)),
                                new VarDeclStmt(new IntType(), new Identifier("x"), new IntegerExp(2))));

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckStmt(
                                block,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType()));
        }

        @Test
        public void testBreakOutsideLoopThrows() {
                Stmt stmt = new BreakStmt();

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType()));
        }

        @Test
        public void testBreakInsideLoopPasses() throws TypeErrorException {
                Stmt stmt = new WhileStmt(
                                new BoolExp(true),
                                new BreakStmt());

                Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType());
        }

        @Test
        public void testFunctionCallType() throws TypeErrorException {
                FuncDef f = new FuncDef(
                                "id",
                                Arrays.asList(new Param(new IntType(), "x")),
                                new IntType(),
                                Arrays.asList(new ReturnStmt(new IdentifierExp(new Identifier("x")))));

                var functionEnv = Typechecker.buildFunctionEnv(Arrays.asList(f));

                Exp call = new CallExp(
                                "id",
                                Arrays.asList(new IntegerExp(10)));

                Type t = Typechecker.typeof(
                                call,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                functionEnv);

                assertEquals(new IntType(), t);
        }

        @Test
        public void testFunctionCallWrongArgTypeThrows() throws TypeErrorException {
                FuncDef f = new FuncDef(
                                "id",
                                Arrays.asList(new Param(new IntType(), "x")),
                                new IntType(),
                                Arrays.asList(new ReturnStmt(new IdentifierExp(new Identifier("x")))));

                var functionEnv = Typechecker.buildFunctionEnv(Arrays.asList(f));

                Exp call = new CallExp(
                                "id",
                                Arrays.asList(new BoolExp(true)));

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                call,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                functionEnv));
        }

        @Test
        public void testNonVoidFunctionWithoutReturnThrows() {
                FuncDef f = new FuncDef(
                                "bad",
                                Arrays.asList(),
                                new IntType(),
                                Arrays.asList(new PrintlnStmt(new IntegerExp(1))));

                Program p = new Program(
                                Arrays.asList(),
                                Arrays.asList(f),
                                Arrays.asList());

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckProgram(p));
        }

        @Test
        public void testNonVoidFunctionWithReturnPasses() throws TypeErrorException {
                FuncDef f = new FuncDef(
                                "good",
                                Arrays.asList(),
                                new IntType(),
                                Arrays.asList(new ReturnStmt(new IntegerExp(1))));

                Program p = new Program(
                                Arrays.asList(),
                                Arrays.asList(f),
                                Arrays.asList());

                Typechecker.typecheckProgram(p);
        }

        @Test
        public void testTypeofPlusReturnsInt() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(1), new PlusOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), result);
        }

        @Test
        public void testTypeofMinusReturnsInt() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(5), new MinusOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), result);
        }

        @Test
        public void testTypeofTimesReturnsInt() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(3), new TimesOp(), new IntegerExp(4));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), result);
        }

        @Test
        public void testTypeofDivideReturnsInt() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(8), new DivideOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), result);
        }

        @Test
        public void testTypeofIntEqualsReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(1), new EqualEqualOp(), new IntegerExp(1));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofBoolEqualsReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new BoolExp(true), new EqualEqualOp(), new BoolExp(false));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofIntNotEqualsReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(1), new NotEqualOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofBoolNotEqualsReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new BoolExp(true), new NotEqualOp(), new BoolExp(false));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofLessThanReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(1), new LessThanOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofLessEqualReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(1), new LessEqualOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofGreaterThanReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(3), new GreaterThanOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofGreaterEqualReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new IntegerExp(3), new GreaterEqualOp(), new IntegerExp(2));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofStructEqualsNullReturnsBool() throws TypeErrorException {
                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("x"), new StructType("Node"));

                Exp exp = new BinopExp(
                                new IdentifierExp(new Identifier("x")),
                                new EqualEqualOp(),
                                new NullExp());

                Type result = Typechecker.typeof(
                                exp,
                                typeEnv,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofNullNotEqualsStructReturnsBool() throws TypeErrorException {
                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("x"), new StructType("Node"));

                Exp exp = new BinopExp(
                                new NullExp(),
                                new NotEqualOp(),
                                new IdentifierExp(new Identifier("x")));

                Type result = Typechecker.typeof(
                                exp,
                                typeEnv,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofNullEqualsNullReturnsBool() throws TypeErrorException {
                Exp exp = new BinopExp(new NullExp(), new EqualEqualOp(), new NullExp());

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new BoolType(), result);
        }

        @Test
        public void testTypeofBadBinopThrows() {
                Exp exp = new BinopExp(new IntegerExp(1), new PlusOp(), new BoolExp(true));

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>()));
        }

        @Test
        public void testTypecheckAssignValid() throws TypeErrorException {
                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("x"), new IntType());

                Stmt stmt = new AssignStmt(
                                new Identifier("x"),
                                new IntegerExp(5));

                Typechecker.typecheckStmt(
                                stmt,
                                typeEnv,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType());
        }

        @Test
        public void testTypecheckIfStmtBranchPasses() throws TypeErrorException {
                Stmt stmt = new IfStmt(
                                new BoolExp(true),
                                new BreakStmt(),
                                new BreakStmt());

                Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                true,
                                new VoidType());
        }

        @Test
        public void testTypecheckIfStmtBadConditionThrows() {
                Stmt stmt = new IfStmt(
                                new IntegerExp(1),
                                new BreakStmt(),
                                null);

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType()));
        }

        @Test
        public void testTypecheckExprStmtPasses() throws TypeErrorException {
                Stmt stmt = new ExprStmt(new IntegerExp(10));

                Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType());
        }

        @Test
        public void testTypecheckExprStmtBadExpressionThrows() {
                Stmt stmt = new ExprStmt(new IdentifierExp(new Identifier("missing")));

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType()));
        }

        @Test
        public void testDotExpNonStructThrows() {
                Exp exp = new DotExp(
                                new IntegerExp(5),
                                "x");

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>()));
        }

        @Test
        public void testDotExpUnknownStructThrows() {
                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("a"), new StructType("Missing"));

                Exp exp = new DotExp(
                                new IdentifierExp(new Identifier("a")),
                                "x");

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                exp,
                                typeEnv,
                                new java.util.HashMap<>(), // no struct registered
                                new java.util.HashMap<>()));
        }

        @Test
        public void testDotExpMissingFieldThrows() throws TypeErrorException {
                StructDef node = new StructDef(
                                "Node",
                                java.util.Arrays.asList(
                                                new Param(new IntType(), "value")));

                var structEnv = Typechecker.buildStructEnv(java.util.Arrays.asList(node));

                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("n"), new StructType("Node"));

                Exp exp = new DotExp(
                                new IdentifierExp(new Identifier("n")),
                                "missing");

                assertThrows(TypeErrorException.class,
                                () -> Typechecker.typeof(exp, typeEnv, structEnv, new java.util.HashMap<>()));
        }

        @Test
        public void testDotExpValidField() throws TypeErrorException {
                StructDef node = new StructDef(
                                "Node",
                                java.util.Arrays.asList(
                                                new Param(new IntType(), "value")));

                var structEnv = Typechecker.buildStructEnv(java.util.Arrays.asList(node));

                var typeEnv = new java.util.HashMap<Identifier, Type>();
                typeEnv.put(new Identifier("n"), new StructType("Node"));

                Exp exp = new DotExp(
                                new IdentifierExp(new Identifier("n")),
                                "value");

                Type result = Typechecker.typeof(exp, typeEnv, structEnv, new java.util.HashMap<>());

                assertEquals(new IntType(), result);
        }

        @Test
        public void testParenExpType() throws TypeErrorException {
                Exp exp = new ParenExp(new IntegerExp(10));

                Type result = Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>());

                assertEquals(new IntType(), result);
        }

        private static class FakeExp implements Exp {
        }

        @Test
        public void testUnknownExpThrows() {
                Exp exp = new FakeExp();

                assertThrows(TypeErrorException.class, () -> Typechecker.typeof(
                                exp,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>()));
        }

        @Test
        public void testDuplicateFunctionParameterThrows() {
                FuncDef f = new FuncDef(
                                "f",
                                Arrays.asList(
                                                new Param(new IntType(), "x"),
                                                new Param(new BoolType(), "x")),
                                new VoidType(),
                                Arrays.asList());

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckFunctionDefs(
                                Arrays.asList(f),
                                new java.util.HashMap<>(),
                                Typechecker.buildFunctionEnv(Arrays.asList(f))));
        }

        @Test
        public void testVoidFunctionParameterThrows() {
                FuncDef f = new FuncDef(
                                "f",
                                Arrays.asList(new Param(new VoidType(), "x")),
                                new VoidType(),
                                Arrays.asList());

                assertThrows(TypeErrorException.class, () -> Typechecker.typecheckFunctionDefs(
                                Arrays.asList(f),
                                new java.util.HashMap<>(),
                                Typechecker.buildFunctionEnv(Arrays.asList(f))));
        }

        @Test
        public void testDefinitelyReturnsReturnStmt() {
                assertEquals(true,
                                Typechecker.definitelyReturns(new ReturnStmt(new IntegerExp(1))));
        }

        @Test
        public void testDefinitelyReturnsBlockWithReturn() {
                Stmt block = new BlockStmt(Arrays.asList(
                                new PrintlnStmt(new IntegerExp(1)),
                                new ReturnStmt(new IntegerExp(2))));

                assertEquals(true, Typechecker.definitelyReturns(block));
        }

        @Test
        public void testDefinitelyReturnsEmptyBlockFalse() {
                Stmt block = new BlockStmt(Arrays.asList());

                assertEquals(false, Typechecker.definitelyReturns(block));
        }

        @Test
        public void testDefinitelyReturnsIfWithoutElseFalse() {
                Stmt stmt = new IfStmt(
                                new BoolExp(true),
                                new ReturnStmt(new IntegerExp(1)),
                                null);

                assertEquals(false, Typechecker.definitelyReturns(stmt));
        }

        @Test
        public void testDefinitelyReturnsIfOneBranchFalse() {
                Stmt stmt = new IfStmt(
                                new BoolExp(true),
                                new ReturnStmt(new IntegerExp(1)),
                                new PrintlnStmt(new IntegerExp(2)));

                assertEquals(false, Typechecker.definitelyReturns(stmt));
        }

        @Test
        public void testDefinitelyReturnsNormalStmtFalse() {
                assertEquals(false,
                                Typechecker.definitelyReturns(new PrintlnStmt(new IntegerExp(1))));
        }

        @Test
        public void testTypecheckStmtBlockBranchPasses() throws TypeErrorException {
                Stmt stmt = new BlockStmt(Arrays.asList(
                                new VarDeclStmt(new IntType(), new Identifier("x"), new IntegerExp(1)),
                                new ExprStmt(new IdentifierExp(new Identifier("x")))));

                Typechecker.typecheckStmt(
                                stmt,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType());
        }

        @Test
        public void testTypecheckBlockDoesNotLeakVariables() throws TypeErrorException {
                Stmt block = new BlockStmt(Arrays.asList(
                                new VarDeclStmt(new IntType(), new Identifier("x"), new IntegerExp(1))));

                var env = new java.util.HashMap<Identifier, Type>();

                Typechecker.typecheckStmt(
                                block,
                                env,
                                new java.util.HashMap<>(),
                                new java.util.HashMap<>(),
                                false,
                                new VoidType());

                assertEquals(false, env.containsKey(new Identifier("x")));
        }

        @Test
        public void testAssertValidTypeIntPasses() throws TypeErrorException {
                Typechecker.assertValidType(new IntType(), new java.util.HashMap<>());
        }

        @Test
        public void testAssertValidTypeBoolPasses() throws TypeErrorException {
                Typechecker.assertValidType(new BoolType(), new java.util.HashMap<>());
        }

        @Test
        public void testAssertValidTypeVoidPasses() throws TypeErrorException {
                Typechecker.assertValidType(new VoidType(), new java.util.HashMap<>());
        }

        @Test
        public void testAssertValidTypeKnownStructPasses() throws TypeErrorException {
                var structEnv = new java.util.HashMap<String, java.util.Map<String, Type>>();
                structEnv.put("Node", new java.util.HashMap<>());

                Typechecker.assertValidType(new StructType("Node"), structEnv);
        }

        @Test
        public void testAssertValidTypeUnknownStructThrows() {
                assertThrows(TypeErrorException.class, () -> Typechecker.assertValidType(
                                new StructType("Missing"),
                                new java.util.HashMap<>()));
        }

        @Test
        public void testTypecheckProgramSimple() throws TypeErrorException {
                Program program = new Program(
                                java.util.Arrays.asList(), // no structs
                                java.util.Arrays.asList(), // no functions
                                java.util.Arrays.asList(
                                                new VarDeclStmt(
                                                                new IntType(),
                                                                new Identifier("x"),
                                                                new IntegerExp(1))));

                Typechecker.typecheckProgram(program);
        }

        @Test
        public void testAssertValidFieldTypeKnownStructPasses() throws TypeErrorException {
                var structEnv = new java.util.HashMap<String, java.util.Map<String, Type>>();
                structEnv.put("Node", new java.util.HashMap<>());

                Typechecker.assertValidFieldType(new StructType("Node"), structEnv);
        }

        @Test
        public void testAssertValidFieldTypeUnknownStructThrows() {
                var structEnv = new java.util.HashMap<String, java.util.Map<String, Type>>();

                assertThrows(TypeErrorException.class,
                                () -> Typechecker.assertValidFieldType(new StructType("Missing"), structEnv));
        }
}