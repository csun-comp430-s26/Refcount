package parser;

public record AssignStmt(Identifier variable, Exp expression) implements Stmt {

}
