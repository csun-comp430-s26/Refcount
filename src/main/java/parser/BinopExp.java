package parser;

public record BinopExp(Exp left, Op op, Exp right) implements Exp {

}
