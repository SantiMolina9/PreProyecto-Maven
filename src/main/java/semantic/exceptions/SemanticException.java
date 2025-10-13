package semantic.exceptions;

/**
 * Excepción para errores semánticos en el compilador.
 */
public class SemanticException extends RuntimeException {
    private int line;
    private int column;

    public SemanticException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    public SemanticException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        if (line >= 0 && column >= 0) {
            return String.format("Semantic Error at line %d, column %d: %s",
                    line, column, getMessage());
        }
        return "Semantic Error: " + getMessage();
    }
}