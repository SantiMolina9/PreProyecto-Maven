package semantic.errors;

public class TypeError extends CompilerError {
    public TypeError(int line, int column, String message) {
        super(line, column, message, "TYPE_ERROR");
    }
}
