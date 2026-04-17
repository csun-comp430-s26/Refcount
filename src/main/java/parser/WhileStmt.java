package parser;

public record WhileStmt(Exp condition, Stmt body) implements Stmt {

}
