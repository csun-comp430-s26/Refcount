package parser;

public record IfStmt(Exp condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {

    public IfStmt(final Exp condition, final Stmt thenBranch) {
        this(condition, thenBranch, null);
    }
}
