package semantic.errors;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static ErrorHandler instance;
    private List<CompilerError> errors;
    private List<CompilerError> warnings;
    private boolean hasErrors;

    // Constructor privado para patrón Singleton
    private ErrorHandler() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.hasErrors = false;
    }

    // Método para obtener la instancia única
    public static ErrorHandler getInstance() {
        if (instance == null) {
            instance = new ErrorHandler();
        }
        return instance;
    }

    public void addError(CompilerError error) {
        errors.add(error);
        hasErrors = true;
        System.err.println(error.toString());
    }

    public void addWarning(CompilerError warning) {
        warnings.add(warning);
        System.out.println(warning.toString());
    }

    public void addLexicalError(int line, int column, String message) {
        addError(new LexicalError(line, column, message));
    }

    public void addSyntaxError(int line, int column, String message) {
        addError(new SyntaxError(line, column, message));
    }

    // Agregar método para errores semánticos
    public void addSemanticError(int line, int column, String message) {
        addError(new SemanticError(line, column, message));
    }

    // Agregar método para errores de tipos
    public void addTypeError(int line, int column, String message) {
        addError(new TypeError(line, column, message));
    }

    // Método genérico para agregar cualquier tipo de error
    public void addError(int line, int column, String message, String errorType) {
        addError(new CompilerError(line, column, message, errorType));
    }

    // Método genérico para agregar advertencias
    public void addWarning(int line, int column, String message) {
        addWarning(new CompilerError(line, column, message, "ADVERTENCIA"));
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public List<CompilerError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<CompilerError> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public void printSummary() {
        System.out.println("\n========== Resumen de Compilación ==========");
        System.out.println("Errores: " + errors.size());
        System.out.println("Advertencias: " + warnings.size());

        if (hasErrors) {
            System.out.println("\n¡Compilación fallida!");
        } else {
            System.out.println("\n¡Compilación exitosa!");
        }
    }

    public void reset() {
        errors.clear();
        warnings.clear();
        hasErrors = false;
    }
}
