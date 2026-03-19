package tokenizer;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class TokenizerTest {
    @Test
    public void testEmpty() throws TokenizerException {
        assertEquals(Arrays.asList(),
                Tokenizer.tokenize(""));
    }

    @Test
    public void testOnlyWhitespace() throws TokenizerException {
        assertEquals(Arrays.asList(),
                Tokenizer.tokenize("   "));
    }

    @Test
    public void testIdentifier() throws TokenizerException {
        assertEquals(Arrays.asList(new IdentifierToken("x")),
                Tokenizer.tokenize("x"));
    }

    @Test
    public void testInteger() throws TokenizerException {
        assertEquals(Arrays.asList(new IntegerToken(1)),
                Tokenizer.tokenize("1"));
    }

    @Test
    public void testInvalid() throws TokenizerException {
        assertThrows(TokenizerException.class,
                () -> Tokenizer.tokenize("$"));
    }

    // TESTS FOR RESERVED WORDS
    @Test
    public void testTrueToken() throws TokenizerException {
        assertEquals(Arrays.asList(new TrueToken()),
                Tokenizer.tokenize("true"));
    }

    @Test
    public void testFalseToken() throws TokenizerException {
        assertEquals(Arrays.asList(new FalseToken()),
                Tokenizer.tokenize("false"));
    }

    @Test
    public void testBoolToken() throws TokenizerException {
        assertEquals(Arrays.asList(new BoolToken()),
                Tokenizer.tokenize("bool"));
    }

    @Test
    public void testBreakToken() throws TokenizerException {
        assertEquals(Arrays.asList(new BreakToken()),
                Tokenizer.tokenize("break"));
    }

    @Test
    public void testFuncToken() throws TokenizerException {
        assertEquals(Arrays.asList(new FuncToken()),
                Tokenizer.tokenize("func"));
    }

    @Test
    public void testIfToken() throws TokenizerException {
        assertEquals(Arrays.asList(new IfToken()),
                Tokenizer.tokenize("if"));
    }

    @Test
    public void testElseToken() throws TokenizerException {
        assertEquals(Arrays.asList(new ElseToken()),
                Tokenizer.tokenize("else"));
    }

    @Test
    public void testNewToken() throws TokenizerException {
        assertEquals(Arrays.asList(new NewToken()),
                Tokenizer.tokenize("new"));
    }

    @Test
    public void testNullToken() throws TokenizerException {
        assertEquals(Arrays.asList(new NullToken()),
                Tokenizer.tokenize("null"));
    }

    @Test
    public void testPrintlnToken() throws TokenizerException {
        assertEquals(Arrays.asList(new PrintlnToken()),
                Tokenizer.tokenize("println"));
    }

    @Test
    public void testReturnToken() throws TokenizerException {
        assertEquals(Arrays.asList(new ReturnToken()),
                Tokenizer.tokenize("return"));
    }

    @Test
    public void testWhileToken() throws TokenizerException {
        assertEquals(Arrays.asList(new WhileToken()),
                Tokenizer.tokenize("while"));
    }

    @Test
    public void testVoidToken() throws TokenizerException {
        assertEquals(Arrays.asList(new VoidToken()),
                Tokenizer.tokenize("void"));
    }

    @Test
    public void testIntToken() throws TokenizerException {
        assertEquals(Arrays.asList(new IntToken()),
                Tokenizer.tokenize("int"));
    }

    @Test
    public void testStructToken() throws TokenizerException {
        assertEquals(Arrays.asList(new StructToken()),
                Tokenizer.tokenize("struct"));
    }
}