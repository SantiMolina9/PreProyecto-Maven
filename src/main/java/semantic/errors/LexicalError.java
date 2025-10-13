package semantic.errors;

class LexicalError extends CompilerError {
    public LexicalError(int line, int column, String message) {
        super(line, column, message, "LEXICAL ERROR");
    }
}