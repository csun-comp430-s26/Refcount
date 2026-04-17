package parser;

public record AssignStmt(String variable, Exp expression) implements Stmt {

}
