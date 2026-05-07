package parser;

import java.util.List;

public record FuncDef(String name, List<Param> parameters, Type returnType, List<Stmt> body) {

}
