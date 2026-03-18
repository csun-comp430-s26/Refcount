package tokenizer;

public record IdentifierToken(String name) implements Token {

}
// There or structname, var, and funcname which are all identifiers.
// We can use the same token for all of them and determine the type of
// identifier in the parser.