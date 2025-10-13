package semantic.errors;

class SyntaxError extends CompilerError {
    public SyntaxError(int line, int column, String message) {
        super(line, column, message, "SYNTAX ERROR");
    }
}