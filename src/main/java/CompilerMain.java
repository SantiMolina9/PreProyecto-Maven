import com.ejemplo.parser.MiParser;
import ast.nodes.program.ProgramNode;
import semantic.SemanticAnalyzer;
import semantic.errors.ErrorHandler;

import java.io.*;
import java_cup.runtime.Symbol;

/**
 * Clase principal del compilador.
 * Coordina las fases de análisis léxico, sintáctico y semántico.
 */
public class CompilerMain {

    public static void main(String[] args) {

        //Ejecucion hardcodeda
        if (args.length == 0) {
            args = new String[]{"src/main/resources/test_malo.txt"};
        }

        if (args.length < 1) {
            System.err.println("Usage: java CompilerMain <input_file>");
            System.exit(1);
        }

        String inputFile = args[0];

        try {
            compileFile(inputFile);
        } catch (Exception e) {
            System.err.println("Compilation error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Compila un archivo fuente
     */
    public static void compileFile(String filename) throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPILING FILE: " + filename);
        System.out.println("=".repeat(60));

        // Fase 1: Análisis Léxico y Sintáctico
        ProgramNode ast = parseFile(filename);

        if (ast == null) {
            System.err.println("Parsing failed. Compilation aborted.");
            return;
        }

        System.out.println("\n✓ Parsing completed successfully!");
        System.out.println("AST: " + ast);

        // Fase 2: Análisis Semántico
        ErrorHandler errorHandler = new ErrorHandler();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(errorHandler);

        ast.accept(semanticAnalyzer);

        // Imprimir resumen
        semanticAnalyzer.printSummary();

        // Resultado final
        System.out.println("\n" + "=".repeat(60));
        if (errorHandler.hasErrors()) {
            System.out.println("❌ COMPILATION FAILED");
            System.out.println("=".repeat(60));
            System.exit(1);
        } else {
            System.out.println("✓ COMPILATION SUCCESSFUL");
            System.out.println("=".repeat(60));
        }
    }

    /**
     * Realiza el parsing de un archivo
     */
    private static ProgramNode parseFile(String filename) throws Exception {
        FileReader fileReader = new FileReader(filename);
        Lexer lexer = new Lexer(fileReader);
        MiParser parser = new MiParser(lexer);

        Symbol result = parser.parse();

        if (result != null && result.value instanceof ProgramNode) {
            return (ProgramNode) result.value;
        }

        return null;
    }

    /**
     * Compila desde una cadena (útil para testing)
     */
    public static void compileString(String source) throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPILING SOURCE CODE");
        System.out.println("=".repeat(60));

        StringReader stringReader = new StringReader(source);
        Lexer lexer = new Lexer(stringReader);
        MiParser parser = new MiParser(lexer);

        Symbol result = parser.parse();

        if (result == null || !(result.value instanceof ProgramNode)) {
            System.err.println("Parsing failed. Compilation aborted.");
            return;
        }

        ProgramNode ast = (ProgramNode) result.value;
        System.out.println("\n✓ Parsing completed successfully!");

        ErrorHandler errorHandler = new ErrorHandler();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(errorHandler);

        ast.accept(semanticAnalyzer);
        semanticAnalyzer.printSummary();

        System.out.println("\n" + "=".repeat(60));
        if (errorHandler.hasErrors()) {
            System.out.println("❌ COMPILATION FAILED");
        } else {
            System.out.println("✓ COMPILATION SUCCESSFUL");
        }
        System.out.println("=".repeat(60));
    }
}