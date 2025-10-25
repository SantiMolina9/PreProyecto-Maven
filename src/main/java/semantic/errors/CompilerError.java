package semantic.errors;

public class CompilerError {
    protected int line;
    protected int column;
    protected String message;
    protected String errorType;

    public CompilerError(int line, int column, String message, String errorType) {
        this.line = line;
        this.column = column;
        this.message = message;
        this.errorType = errorType;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return String.format("[%s] Línea %d, Columna %d: %s",
                errorType, line, column, message);
    }
}
