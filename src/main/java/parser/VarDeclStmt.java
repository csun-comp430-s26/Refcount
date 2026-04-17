package parser;

public record VarDeclStmt(Type type, String name, Exp initializer) implements Stmt {

}
