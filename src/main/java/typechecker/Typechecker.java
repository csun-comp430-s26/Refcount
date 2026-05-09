package typechecker;

import java.util.HashMap;
import java.util.Map;

import parser.AssignStmt;
import parser.BinopExp;
import parser.BoolExp;
import parser.BoolType;
import parser.DivideOp;
import parser.EqualEqualOp;
import parser.Exp;
import parser.GreaterEqualOp;
import parser.GreaterThanOp;
import parser.Identifier;
import parser.IdentifierExp;
import parser.IntType;
import parser.IntegerExp;
import parser.LessEqualOp;
import parser.LessThanOp;
import parser.MinusOp;
import parser.NotEqualOp;
import parser.PlusOp;
import parser.Stmt;
import parser.TimesOp;
import parser.Type;
import parser.VarDeclStmt;

public class Typechecker {

    // Type check
    // Use Identifier instead of Variable in our code we can either swap to Variable
    // or work as is
    // structname is a Type
    public static Type typeof(final Exp exp, final Map<Identifier, Type> typeEnv) throws TypeErrorException {
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
            final Type leftType = typeof(binopExp.left(), typeEnv);
            final Type rightType = typeof(binopExp.right(), typeEnv);

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
            } else {
                throw new TypeErrorException("leftType: " + leftType +
                        "rightType: " + rightType +
                        "op: " + binopExp.op() +
                        " is ill typed");
            }

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

    // helper fucntion to check Variable declaration
    public static Map<Identifier, Type> typecheckVarDecl(final VarDeclStmt stmt, final Map<Identifier, Type> typeEnv)
            throws TypeErrorException {
        final Type receivedType = typeof(stmt.initializer(), typeEnv);
        assertTypesEqual(stmt.type(), receivedType);
        return addToMap(typeEnv, stmt.var(), receivedType);

    }

    // helper function for check Assignment
    public static Map<Identifier, Type> typecheckAssign(final AssignStmt stmt, final Map<Identifier, Type> typeEnv)
            throws TypeErrorException {
        if (typeEnv.containsKey(stmt.variable())) {
            final Type expected = typeEnv.get(stmt.variable());
            assertTypesEqual(expected, typeof(stmt.expression(), typeEnv));
            return typeEnv;
        } else {
            throw new TypeErrorException("Variable not in scope: " + stmt.toString());
        }
    }

    // check statments
    public static Map<Identifier, Type> TypecheckStmt(final Stmt stmt, final Map<Identifier, Type> typeEnv)
            throws TypeErrorException {
        if (stmt instanceof VarDeclStmt) {
            return typecheckVarDecl((VarDeclStmt) stmt, typeEnv);
        } else if (stmt instanceof AssignStmt) {

        } else {
            assert (false);
            throw new TypeErrorException("Unrecognized statement: " + stmt);
        }
    }

}
