package semantic.errors;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private List<CompilerError> errors;
    private List<CompilerError> warnings;
    private boolean hasErrors;

    public ErrorHandler() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.hasErrors = false;
    }

    public void addError(CompilerError error) {
        errors.add(error);
        hasErrors = true;
        System.err.println(error.toString());
    }

    public void addWarning(CompilerError warning) {
        warnings.add(warning);
        System.out.println("Warning: " + warning.toString());
    }

    public void addLexicalError(int line, int column, String message) {
        addError(new LexicalError(line, column, message));
    }

    public void addSyntaxError(int line, int column, String message) {
        addError(new SyntaxError(line, column, message));
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
        System.out.println("\n========== Resumen de compilacion ==========");
        System.out.println("Errores: " + errors.size());
        System.out.println("Warnings: " + warnings.size());

        /*if (hasErrors) {
            System.out.println("\nCompilacion fallida!");
        } else {
            System.out.println("\nCompilacion exitosa!");
        }*/
    }

    public void reset() {
        errors.clear();
        warnings.clear();
        hasErrors = false;
    }
}