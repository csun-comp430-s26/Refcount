package parser;

public record EqualsExp(Exp left, Equals equals, Exp right) implements Exp {
    
}
