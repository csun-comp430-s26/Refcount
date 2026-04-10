package parser;

// struct_actual_param ::= var `:` exp, leave out the colon token 
public record StructActualParam(String identifier, Exp exp) {

}
