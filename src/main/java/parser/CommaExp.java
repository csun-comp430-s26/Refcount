package parser;

public record CommaExp(Exp left, Comma comma, Exp right) implements Exp {

}
