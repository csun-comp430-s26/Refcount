package parser;

public record ReturnStmt(Exp value) implements Stmt {

    public static ReturnStmt voidReturn() {
        return new ReturnStmt(null);
    }

    public boolean hasValue() {
        return value != null;
    }
}
