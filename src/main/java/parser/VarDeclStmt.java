package parser;

public record VarDeclStmt(Type type, Identifier var, Exp initializer) implements Stmt {

}
