package tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Tokenizer {
    public final String input;
    private int position;

    public Tokenizer(final String input) {
        this.input = input;
        position = 0;
    }

    public void skipWhitespace() {
        while (position < input.length() &&
                Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }

    // assumes position is initially in bounds
    // read symbol tokens
    public Optional<Token> readSymbol() {
        char c = input.charAt(position);

        // Two character symbols need to be checked before single character symbols
        if (position + 1 < input.length()) {
            char next = input.charAt(position + 1);
            if (c == '<' && next == '=') {
                position += 2;
                return Optional.of(new LessEqualToken());
            }
            if (c == '>' && next == '=') {
                position += 2;
                return Optional.of(new GreaterEqualToken());
            }
            if (c == '=' && next == '=') {
                position += 2;
                return Optional.of(new EqualEqualToken());
            }
            if (c == '!' && next == '=') {
                position += 2;
                return Optional.of(new NotEqualToken());
            }
        }

        // Single-character symbols (switch expression: every arm must yield a value)
        return switch (c) {
            case '+' -> advance(new PlusToken());
            case '-' -> advance(new MinusToken());
            case '*' -> advance(new StarToken());
            case '/' -> advance(new SlashToken());
            case '<' -> advance(new LessToken());
            case '>' -> advance(new GreaterToken());
            case '=' -> advance(new AssignToken());
            case '{' -> advance(new LeftBraceToken());
            case '}' -> advance(new RightBraceToken());
            case '(' -> advance(new LeftParenToken());
            case ')' -> advance(new RightParenToken());
            case ';' -> advance(new SemicolonToken());
            case ':' -> advance(new ColonToken());
            case ',' -> advance(new CommaToken());
            case '.' -> advance(new DotToken());
            default -> Optional.empty();
        };
    }

    private Optional<Token> advance(final Token token) {
        position++;
        return Optional.of(token);
    }

    // assumes position is initially in bounds
    // reads integer tokens

    public Optional<Token> readInteger() {
        String digits = "";
        char c = ' ';
        while (position < input.length() &&
                Character.isDigit(c = input.charAt(position))) {
            digits += c;
            position++;
        }

        if (digits.length() > 0) {
            return Optional.of(new IntegerToken(Integer.parseInt(digits)));
        } else {
            return Optional.empty();
        }
    }

    // assumes position is initially in bounds
    public Optional<Token> readIdentifierOrReservedWord() {
        char c = input.charAt(position);
        if (Character.isLetter(c)) {
            String identifierOrReservedWord = "" + c;
            position++;

            while (position < input.length() &&
                    Character.isLetterOrDigit(c = input.charAt(position))) {
                identifierOrReservedWord += c;
                position++;
            }

            if (identifierOrReservedWord.equals("true")) {
                return Optional.of(new TrueToken());
            } else if (identifierOrReservedWord.equals("false")) {
                return Optional.of(new FalseToken());
            } else if (identifierOrReservedWord.equals("void")) {
                return Optional.of(new VoidToken());
            } else if (identifierOrReservedWord.equals("while")) {
                return Optional.of(new WhileToken());
            } else if (identifierOrReservedWord.equals("struct")) {
                return Optional.of(new StructToken());
            } else if (identifierOrReservedWord.equals("null")) {
                return Optional.of(new NullToken());
            } else if (identifierOrReservedWord.equals("new")) {
                return Optional.of(new NewToken());
            } else if (identifierOrReservedWord.equals("println")) {
                return Optional.of(new PrintlnToken());
            } else if (identifierOrReservedWord.equals("return")) {
                return Optional.of(new ReturnToken());
            } else if (identifierOrReservedWord.equals("if")) {
                return Optional.of(new IfToken());
            } else if (identifierOrReservedWord.equals("else")) {
                return Optional.of(new ElseToken());
            } else if (identifierOrReservedWord.equals("bool")) {
                return Optional.of(new BoolToken());
            } else if (identifierOrReservedWord.equals("break")) {
                return Optional.of(new BreakToken());
            } else if (identifierOrReservedWord.equals("func")) {
                return Optional.of(new FuncToken());
            } else if (identifierOrReservedWord.equals("int")) {
                return Optional.of(new IntToken());
            } else {

                return Optional.of(new IdentifierToken(identifierOrReservedWord));
            }
        } else {
            return Optional.empty();
        }

    }

    // returns empty if there are no remaining tokens
    public Optional<Token> readToken() throws TokenizerException {
        skipWhitespace();
        if (position >= input.length()) {
            return Optional.empty();
        } else {
            Optional<Token> retval = readSymbol();
            if (retval.isPresent()) {
                return retval;
            }
            retval = readInteger();
            if (retval.isPresent()) {
                return retval;
            }
            retval = readIdentifierOrReservedWord();
            if (retval.isPresent()) {
                return retval;
            } else {
                throw new TokenizerException("Found unrecognized character: " +
                        input.charAt(position) + " at position " +
                        position);
            }
        }
    }

    // returns a list of all tokens in the input string
    public List<Token> tokenize() throws TokenizerException {
        final List<Token> retval = new ArrayList<Token>();
        Optional<Token> token = readToken();
        while (token.isPresent()) {
            retval.add(token.get());
            token = readToken();
        }
        return retval;
    }

    // static method that tokenizes an input string and returns a list of tokens
    public static List<Token> tokenize(final String input)
            throws TokenizerException {
        return new Tokenizer(input).tokenize();
    }

    public static void main(String[] args) throws TokenizerException {
        if (args.length == 1) {
            System.out.println(tokenize(args[0]));
        } else {
            System.out.println("Needs a string to tokenize");
        }
    }
}
