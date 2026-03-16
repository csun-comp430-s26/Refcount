package tokenizer;

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
            } else if (identifierOrReservedWord.equals("var")) {
                return Optional.of(new VarToken());
            } else if (identifierOrReservedWord.equals("type")) {
                return Optional.of(new TypeToken());
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
            } else if (identifierOrReservedWord.equals("bool")) {
                return Optional.of(new BoolToken());
            } else if (identifierOrReservedWord.equals("break")) {
                return Optional.of(new BreakToken());

            } else if (identifierOrReservedWord.equals("func")) {
                return Optional.of(new FuncToken());
            } else {

                return Optional.of(new IdentifierToken(identifierOrReservedWord));
            }
        } else {
            return Optional.empty();
        }
    }
}
