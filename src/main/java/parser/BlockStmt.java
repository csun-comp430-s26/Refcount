package parser;

import java.util.List;

public record BlockStmt(List<Stmt> body) implements Stmt {

}
