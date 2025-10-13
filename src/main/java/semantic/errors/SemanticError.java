package semantic.errors;

public class SemanticError extends CompilerError {
    public SemanticError(int line, int column, String message) {
        super(line, column, message, "SEMANTIC ERROR");
    }
}