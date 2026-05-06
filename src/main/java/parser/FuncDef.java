package parser;

import java.util.List;

public record FuncDef(
        String name,
        List<Param> params,
        Type returnType,
        List<Stmt> body) {
}
