package parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import tokenizer.BoolToken;
import tokenizer.ColonToken;
import tokenizer.CommaToken;
import tokenizer.IdentifierToken;
import tokenizer.IntToken;
import tokenizer.Token;
import tokenizer.VoidToken;

public class ParserTest {

    // TESTS FOR assertTokenHereIs
    @Test
    public void testAssertTokenHereIsSuccess() throws ParseException {
        List<Token> tokens = List.of(new ColonToken());
        Parser parser = new Parser(tokens);

        // Should not throw an exception
        assertDoesNotThrow(() -> parser.assertTokenHereIs(0, new ColonToken()));
    }

    @Test
    public void testAssertTokenHereIsFailure() {
        List<Token> tokens = List.of(new CommaToken());
        Parser parser = new Parser(tokens);

        ParseException exception = assertThrows(ParseException.class,
                () -> parser.assertTokenHereIs(0, new ColonToken()));

        assertTrue(exception.getMessage().contains("Expected token:"));
        assertTrue(exception.getMessage().contains("received token:"));
    }

    @Test
    public void testAssertTokenHereIsOutOfBounds() {
        List<Token> tokens = List.of(new IntToken());
        Parser parser = new Parser(tokens);

        assertThrows(ParseException.class, () -> parser.assertTokenHereIs(5, new ColonToken()));
    }

    // END OF TESTS FOR assertTokenHereIs
    // TESTS FOR parseType
    @Test
    public void testParseTypeInt() throws ParseException {
        List<Token> tokens = List.of(new IntToken());
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof IntType);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeBool() throws ParseException {
        List<Token> tokens = List.of(new BoolToken());
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof BoolType);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeVoid() throws ParseException {
        List<Token> tokens = List.of(new VoidToken());
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof VoidType);
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeStruct() throws ParseException {
        List<Token> tokens = List.of(new IdentifierToken("Node"));
        Parser parser = new Parser(tokens);

        ParseResult<Type> result = parser.parseType(0);

        assertTrue(result.result() instanceof StructType);
        StructType structType = (StructType) result.result();
        assertEquals("Node", structType.name());
        assertEquals(1, result.nextPos());
    }

    @Test
    public void testParseTypeInvalidToken() {
        List<Token> tokens = List.of(); // Empty list - no tokens
        Parser parser = new Parser(tokens);

        assertThrows(ParseException.class, () -> parser.parseType(0));
    }

    @Test
    public void testParseTypeUnexpectedToken() {
        List<Token> tokens = List.of(new CommaToken()); // Unexpected token (not int, bool, void, or identifier)
        Parser parser = new Parser(tokens);

        ParseException exception = assertThrows(ParseException.class, () -> parser.parseType(0));
        assertEquals("Expected type at position 0; received: " + new CommaToken().toString(), exception.getMessage());
    }
    // END OF TESTS FOR parseType

    // TESTS FOR parseParam
    @Test
    public void testParseParamIntVar() throws ParseException {
        List<Token> tokens = List.of(new IntToken(), new IdentifierToken("x"));
        Parser parser = new Parser(tokens);

        ParseResult<Param> result = parser.parseParam(0);

        assertTrue(result.result().type() instanceof IntType);
        assertEquals("x", result.result().identifier());
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseParamStructType() throws ParseException {
        List<Token> tokens = List.of(new IdentifierToken("Node"), new IdentifierToken("head"));
        Parser parser = new Parser(tokens);

        ParseResult<Param> result = parser.parseParam(0);

        assertTrue(result.result().type() instanceof StructType);
        assertEquals("Node", ((StructType) result.result().type()).name());
        assertEquals("head", result.result().identifier());
        assertEquals(2, result.nextPos());
    }

    @Test
    public void testParseParamMissingIdentifier() {
        List<Token> tokens = List.of(new IntToken());
        Parser parser = new Parser(tokens);

        assertThrows(ParseException.class, () -> parser.parseParam(0));
    }

    @Test
    public void testParseParamMissingIdentifierException() {
        List<Token> tokens = List.of(new IntToken(), new CommaToken()); // Comma token instead of identifier
        Parser parser = new Parser(tokens);

        ParseException exception = assertThrows(ParseException.class, () -> parser.parseParam(0));
        assertEquals("Expected identifier for parameter at position 1", exception.getMessage());
    }
    // END OF TESTS FOR parseParam

}
