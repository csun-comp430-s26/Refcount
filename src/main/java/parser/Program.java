package parser;

import java.util.List;

public record Program(List<StructDef> structDefs, List<FuncDef> funcDefs, List<Stmt> stmts) {

}
