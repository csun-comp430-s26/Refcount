package typechecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.AssignStmt;
import parser.BinopExp;
import parser.BlockStmt;
import parser.BoolExp;
import parser.BoolType;
import parser.BreakStmt;
import parser.CallExp;
import parser.DivideOp;
import parser.DotExp;
import parser.EqualEqualOp;
import parser.Exp;
import parser.ExprStmt;
import parser.FuncDef;
import parser.GreaterEqualOp;
import parser.GreaterThanOp;
import parser.Identifier;
import parser.IdentifierExp;
import parser.IfStmt;
import parser.IntType;
import parser.IntegerExp;
import parser.LessEqualOp;
import parser.LessThanOp;
import parser.MinusOp;
import parser.NewExp;
import parser.NotEqualOp;
import parser.NullExp;
import parser.NullType;
import parser.Param;
import parser.ParenExp;
import parser.PlusOp;
import parser.PrintlnStmt;
import parser.Program;
import parser.ReturnStmt;
import parser.Stmt;
import parser.StructActualParam;
import parser.StructDef;
import parser.StructType;
import parser.TimesOp;
import parser.Type;
import parser.VarDeclStmt;
import parser.VoidType;
import parser.WhileStmt;

public class Typechecker {

    // seperate Map for Types, Structs, and Funcs
    public static void typecheckProgram(final Program program)
            throws TypeErrorException {

        // Collect struct names / fields
        final Map<String, Map<String, Type>> structEnv = buildStructEnv(program.structDefs());

        // Validate struct definitions
        typecheckStructs(program.structDefs(), structEnv);

        // collect function signatures
        final Map<String, FuncDef> functionEnv = buildFunctionEnv(program.funcDefs());

        // validate function definitions
        typecheckFunctionDefs(program.funcDefs(), structEnv, functionEnv);

        // Typecheck top-level entry-point statements
        Map<Identifier, Type> typeEnv = new HashMap<>();

        for (final Stmt stmt : program.stmts()) {
            typeEnv = typecheckStmt(
                    stmt,
                    typeEnv,
                    structEnv,
                    functionEnv,
                    false,
                    new VoidType());
        }

    }

    // function to build a struct into the struct env
    // Since Struct and Param use String in Parser, String is used here
    public static Map<String, Map<String, Type>> buildStructEnv(
            final List<StructDef> structDefs)
            throws TypeErrorException {
        Map<String, Map<String, Type>> structEnv = new HashMap<>();

        for (StructDef def : structDefs) {
            if (structEnv.containsKey(def.name())) {
                throw new TypeErrorException("Duplicate struct: " + def.name());
            }

            Map<String, Type> fieldMap = new HashMap<>();

            for (Param field : def.fields()) {
                if (fieldMap.containsKey(field.identifier())) {
                    throw new TypeErrorException("Duplicate field: " + field.identifier());
                }

                fieldMap.put(field.identifier(), field.type());
            }

            structEnv.put(def.name(), fieldMap);
        }
        return structEnv;
    }

    // helper to check if the strucDef is valid
    public static void typecheckStructs(
            final List<StructDef> structDefs,
            final Map<String, Map<String, Type>> structEnv)
            throws TypeErrorException {

        for (final StructDef structDef : structDefs) {
            for (final Param field : structDef.fields()) {
                assertValidFieldType(field.type(), structEnv);
            }
        }
    }

    // helper function to check if the fields in the struct definition are valid
    public static void assertValidFieldType(
            final Type type,
            final Map<String, Map<String, Type>> structEnv)
            throws TypeErrorException {

        if (type instanceof IntType || type instanceof BoolType) {
            return;
        }

        if (type instanceof VoidType) {
            throw new TypeErrorException("Struct fields cannot have type void");
        }

        if (type instanceof StructType structType) {
            if (!structEnv.containsKey(structType.name())) {
                throw new TypeErrorException("Unknown struct type: " + structType.name());
            }
            return;
        }

        throw new TypeErrorException("Invalid field type: " + type);
    }

    // helper to build the fucntions for the program
    public static Map<String, FuncDef> buildFunctionEnv(
            final List<FuncDef> functionDefs)
            throws TypeErrorException {

        final Map<String, FuncDef> functionEnv = new HashMap<>();

        for (final FuncDef funcDef : functionDefs) {
            final String name = funcDef.name();

            if (functionEnv.containsKey(name)) {
                throw new TypeErrorException("Duplicate function: " + name);
            }

            functionEnv.put(name, funcDef);
        }

        return functionEnv;
    }

    // helper function for function definitions to see if in scope
    public static void typecheckFunctionDefs(
            final List<FuncDef> functionDefs,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv)
            throws TypeErrorException {

        for (final FuncDef funcDef : functionDefs) {
            Map<Identifier, Type> typeEnv = new HashMap<>();

            // Check parameters
            for (final Param param : funcDef.parameters()) {
                assertValidFieldType(param.type(), structEnv);

                if (typeEnv.containsKey(new Identifier(param.identifier()))) {
                    throw new TypeErrorException(
                            "Duplicate parameter " + param.identifier()
                                    + " in function " + funcDef.name());
                }
                Identifier var = new Identifier(param.identifier());
                typeEnv = addToMap(typeEnv, var, param.type());
            }

            // Check return type
            assertValidType(funcDef.returnType(), structEnv);

            // Check function body
            for (final Stmt stmt : funcDef.body()) {
                typeEnv = typecheckStmt(
                        stmt,
                        typeEnv,
                        structEnv,
                        functionEnv,
                        false,
                        funcDef.returnType());
            }
            if (!(funcDef.returnType() instanceof VoidType)) {
                boolean returns = false;

                for (final Stmt stmt : funcDef.body()) {
                    if (definitelyReturns(stmt)) {
                        returns = true;
                        break;
                    }
                }

                if (!returns) {
                    throw new TypeErrorException(
                            "Function " + funcDef.name() + " may not return a value");
                }
            }
        }
    }

    // help with checking the return type in not void in case of ex: return 1
    public static boolean definitelyReturns(final Stmt stmt) {
        if (stmt instanceof ReturnStmt) {
            return true;
        } else if (stmt instanceof BlockStmt blockStmt) {
            for (final Stmt innerStmt : blockStmt.body()) {
                if (definitelyReturns(innerStmt)) {
                    return true;
                }
            }
            return false;
        } else if (stmt instanceof IfStmt ifStmt) {
            if (ifStmt.elseBranch() == null) {
                return false;
            }

            return definitelyReturns(ifStmt.thenBranch())
                    && definitelyReturns(ifStmt.elseBranch());
        } else {
            return false;
        }
    }

    // helper to check if the Types being added to struct are valid
    public static void assertValidType(
            final Type type,
            final Map<String, Map<String, Type>> structEnv)
            throws TypeErrorException {

        if (type instanceof IntType || type instanceof BoolType) {
            return;
        }

        if (type instanceof VoidType) {
            return;
        }

        if (type instanceof StructType structType) {
            if (!structEnv.containsKey(structType.name())) {
                throw new TypeErrorException(
                        "Unknown struct type: " + structType.name());
            }
            return;
        }

        throw new TypeErrorException("Invalid type: " + type);
    }

    // Type check
    // Use Identifier instead of Variable in our code we can either swap to Variable
    // or work as is
    // structname is a Type
    public static Type typeof(final Exp exp, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv) throws TypeErrorException {
        if (exp instanceof IdentifierExp varExp) {
            if (typeEnv.containsKey(varExp.name())) {
                return typeEnv.get(varExp.name());
            } else {
                throw new TypeErrorException("Not in scope: " + varExp.name());
            }
        } else if (exp instanceof IntegerExp) {
            return new IntType();
        } else if (exp instanceof BoolExp) {
            return new BoolType();
            // exp1 == exp2, exp1 != exp2 (done)
            // exp1 < exp2,exp1 <= exp2,exp1 >= exp2,exp1 > exp2,
            // exp1 +(-) exp 2, exp1 *(/) exp2 (done)
        } else if (exp instanceof BinopExp binopExp) {
            final Type leftType = typeof(binopExp.left(), typeEnv, structEnv, functionEnv);
            final Type rightType = typeof(binopExp.right(), typeEnv, structEnv, functionEnv);

            if ((binopExp.op() instanceof PlusOp || binopExp.op() instanceof MinusOp) && leftType instanceof IntType
                    && rightType instanceof IntType) {
                return new IntType();

            } else if ((binopExp.op() instanceof TimesOp || binopExp.op() instanceof DivideOp)
                    && leftType instanceof IntType && rightType instanceof IntType) {
                return new IntType();
                // compare bool values
            } else if ((binopExp.op() instanceof EqualEqualOp || binopExp.op() instanceof NotEqualOp)
                    && leftType instanceof BoolType && rightType instanceof BoolType) {
                return new BoolType();
                // compare int values
            } else if ((binopExp.op() instanceof EqualEqualOp || binopExp.op() instanceof NotEqualOp)
                    && leftType instanceof IntType && rightType instanceof IntType) {
                return new BoolType();
            } else if ((binopExp.op() instanceof LessThanOp || binopExp.op() instanceof LessEqualOp
                    || binopExp.op() instanceof GreaterThanOp || binopExp.op() instanceof GreaterEqualOp)
                    && leftType instanceof IntType && rightType instanceof IntType) {
                return new BoolType();
                // compare structs for when it iterated through
            } else if ((binopExp.op() instanceof EqualEqualOp || binopExp.op() instanceof NotEqualOp)
                    && ((leftType instanceof StructType && rightType instanceof NullType)
                            || (leftType instanceof NullType && rightType instanceof StructType))) {
                return new BoolType();
                // null comparison
            } else if ((binopExp.op() instanceof EqualEqualOp || binopExp.op() instanceof NotEqualOp)
                    && leftType instanceof NullType && rightType instanceof NullType) {
                return new BoolType();
            } else {
                throw new TypeErrorException("leftType: " + leftType +
                        "rightType: " + rightType +
                        "op: " + binopExp.op() +
                        " is ill typed");
            }

        } else if (exp instanceof NullExp) {
            return new NullType();
        } else if (exp instanceof CallExp callExp) {
            final FuncDef funcDef = functionEnv.get(callExp.funcName());

            if (funcDef == null) {
                throw new TypeErrorException("Unknown function: " + callExp.funcName());
            }

            if (callExp.arguments().size() != funcDef.parameters().size()) {
                throw new TypeErrorException(
                        "Wrong number of arguments to " + callExp.funcName());
            }

            for (int i = 0; i < callExp.arguments().size(); i++) {
                final Type expected = funcDef.parameters().get(i).type();
                final Type actual = typeof(
                        callExp.arguments().get(i),
                        typeEnv,
                        structEnv,
                        functionEnv);

                assertAssignable(expected, actual);
            }

            return funcDef.returnType();
        } else if (exp instanceof NewExp newExp) {
            if (!structEnv.containsKey(newExp.structName())) {
                throw new TypeErrorException("Unknown struct: " + newExp.structName());
            }

            final Map<String, Type> expectedFields = structEnv.get(newExp.structName());
            final Map<String, Type> seen = new HashMap<>();

            for (final StructActualParam param : newExp.fields()) {
                final String fieldName = param.identifier();

                if (!expectedFields.containsKey(fieldName)) {
                    throw new TypeErrorException(
                            "Unknown field " + fieldName + " in struct " + newExp.structName());
                }

                if (seen.containsKey(fieldName)) {
                    throw new TypeErrorException(
                            "Duplicate field " + fieldName + " in new " + newExp.structName());
                }

                final Type expected = expectedFields.get(fieldName);
                final Type actual = typeof(
                        param.exp(),
                        typeEnv,
                        structEnv,
                        functionEnv);

                assertAssignable(expected, actual);

                seen.put(fieldName, actual);
            }

            for (final String field : expectedFields.keySet()) {
                if (!seen.containsKey(field)) {
                    throw new TypeErrorException(
                            "Missing field " + field + " in new " + newExp.structName());
                }
            }

            return new StructType(newExp.structName());
        } else if (exp instanceof DotExp dotExp) {
            final Type targetType = typeof(
                    dotExp.base(),
                    typeEnv,
                    structEnv,
                    functionEnv);

            if (!(targetType instanceof StructType structType)) {
                throw new TypeErrorException("Cannot access field of non-struct type: " + targetType);
            }

            final Map<String, Type> fields = structEnv.get(structType.name());

            if (fields == null) {
                throw new TypeErrorException("Unknown struct type: " + structType.name());
            }

            if (!fields.containsKey(dotExp.field())) {
                throw new TypeErrorException(
                        "Struct " + structType.name() + " has no field " + dotExp.field());
            }

            return fields.get(dotExp.field());
        } else if (exp instanceof ParenExp parenExp) {
            return typeof(
                    parenExp.exp(),
                    typeEnv,
                    structEnv,
                    functionEnv);
        } else {
            throw new TypeErrorException("Unrecognized expression: " + exp);
        }
    }

    // helper function to add to Map since we treat it as immutable becuase of scope
    public static Map<Identifier, Type> addToMap(final Map<Identifier, Type> typeEnv, final Identifier var,
            final Type type) throws TypeErrorException {
        final Map<Identifier, Type> retval = new HashMap<>();
        retval.putAll(typeEnv);
        retval.put(var, type);
        return retval;
    }

    // helper function to compare Types
    public static void assertTypesEqual(final Type expected, final Type received) throws TypeErrorException {
        if (!expected.equals(received)) {
            throw new TypeErrorException(
                    "Expected Type: " + expected.toString() + ", received type: " + received.toString());
        }
    }

    // helper function to check if the assign can be made as in a var being a
    // certain type and cannot be assigned a different type
    public static void assertAssignable(final Type expected, final Type received)
            throws TypeErrorException {
        if (expected.equals(received)) {
            return;
        }

        if (received instanceof NullType && expected instanceof StructType) {
            return;
        }

        throw new TypeErrorException(
                "Expected assignable type: " + expected +
                        ", received type: " + received);
    }

    // helper fucntion to check Variable declaration
    public static Map<Identifier, Type> typecheckVarDecl(final VarDeclStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv)
            throws TypeErrorException {
        if (typeEnv.containsKey(stmt.var())) {
            throw new TypeErrorException("Duplicate variable declaration: " + stmt.var());
        }

        final Type receivedType = typeof(stmt.initializer(), typeEnv, structEnv, functionEnv);

        assertValidFieldType(stmt.type(), structEnv);
        assertAssignable(stmt.type(), receivedType);

        return addToMap(typeEnv, stmt.var(), stmt.type());

    }

    // helper function for check Assignment
    public static Map<Identifier, Type> typecheckAssign(final AssignStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv)
            throws TypeErrorException {
        if (typeEnv.containsKey(stmt.variable())) {
            final Type expected = typeEnv.get(stmt.variable());
            assertAssignable(expected, typeof(stmt.expression(), typeEnv, structEnv, functionEnv));
            return typeEnv;
        } else {
            throw new TypeErrorException("Variable not in scope: " + stmt.toString());
        }
    }

    // helper function for while statments
    public static Map<Identifier, Type> typecheckWhile(final WhileStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv,
            final boolean inLoop, final Type expectedReturnType)
            throws TypeErrorException {
        assertTypesEqual(new BoolType(), typeof(stmt.condition(), typeEnv, structEnv, functionEnv));
        typecheckStmt(stmt.body(), typeEnv, structEnv, functionEnv, true, expectedReturnType);
        return typeEnv;
    }

    // helper function for If statments
    public static Map<Identifier, Type> typecheckIf(final IfStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv,
            final boolean inLoop, final Type expectedReturnType)
            throws TypeErrorException {
        //
        assertTypesEqual(new BoolType(), typeof(stmt.condition(), typeEnv, structEnv, functionEnv));
        typecheckStmt(stmt.thenBranch(), typeEnv, structEnv, functionEnv, inLoop, expectedReturnType);
        if (stmt.elseBranch() != null) {
            typecheckStmt(stmt.elseBranch(), typeEnv, structEnv, functionEnv, inLoop, expectedReturnType);
        }

        return typeEnv;
    }

    // helper for Block Stmt
    public static Map<Identifier, Type> typecheckBlock(final BlockStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv,
            final boolean inLoop, final Type expectedReturnType) throws TypeErrorException {
        Map<Identifier, Type> localEnv = typeEnv;

        for (final Stmt innerStmt : stmt.body()) {
            localEnv = typecheckStmt(innerStmt, localEnv, structEnv, functionEnv, inLoop, expectedReturnType);
        }

        return typeEnv;
    }

    // helper for Return Stmt
    public static void typecheckReturn(final ReturnStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv,
            final Type expectedReturnType)
            throws TypeErrorException {

        if (stmt.value() == null) {
            if (!(expectedReturnType instanceof VoidType)) {
                throw new TypeErrorException(
                        "Expected return of type " + expectedReturnType);
            }
        } else {
            Type actual = typeof(stmt.value(), typeEnv, structEnv, functionEnv);
            assertAssignable(expectedReturnType, actual);
        }
    }

    // check statments
    public static Map<Identifier, Type> typecheckStmt(final Stmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv,
            final boolean inLoop, final Type expectedReturnType)
            throws TypeErrorException {
        if (stmt instanceof VarDeclStmt) {
            return typecheckVarDecl((VarDeclStmt) stmt, typeEnv, structEnv, functionEnv);
        } else if (stmt instanceof AssignStmt) {
            return typecheckAssign((AssignStmt) stmt, typeEnv, structEnv, functionEnv);
        } else if (stmt instanceof WhileStmt) {
            return typecheckWhile((WhileStmt) stmt, typeEnv, structEnv, functionEnv, inLoop, expectedReturnType);
        } else if (stmt instanceof IfStmt) {
            return typecheckIf((IfStmt) stmt, typeEnv, structEnv, functionEnv, inLoop, expectedReturnType);
        } else if (stmt instanceof BreakStmt) {
            if (inLoop) {
                return typeEnv;
            } else {
                throw new TypeErrorException("Break outside of Loop");
            }
        } else if (stmt instanceof PrintlnStmt printlnStmt) {
            typeof(printlnStmt.expression(), typeEnv, structEnv, functionEnv);
            return typeEnv;
        } else if (stmt instanceof BlockStmt) {
            return typecheckBlock((BlockStmt) stmt, typeEnv, structEnv, functionEnv, inLoop, expectedReturnType);
        } else if (stmt instanceof ReturnStmt) {
            typecheckReturn((ReturnStmt) stmt, typeEnv, structEnv, functionEnv, expectedReturnType);
            return typeEnv;
        } else if (stmt instanceof ExprStmt expStmt) {
            typeof(expStmt.expression(), typeEnv, structEnv, functionEnv);
            return typeEnv;
        } else {
            assert (false);
            throw new TypeErrorException("Unrecognized statement: " + stmt);
        }
    }

}
