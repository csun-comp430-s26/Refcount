package tokenizer;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}