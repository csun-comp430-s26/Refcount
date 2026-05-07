package parser;

import java.util.List;

public record NewExp(String structName, List<StructActualParam> fields) implements Exp {

}
