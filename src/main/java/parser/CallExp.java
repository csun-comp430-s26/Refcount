package parser;

import java.util.List;

public record CallExp(String funcName, List<Exp> arguments) implements Exp {

}
