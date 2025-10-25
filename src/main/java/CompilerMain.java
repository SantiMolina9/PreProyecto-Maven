import codegen.CodeGenerator;
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

    // Flag para indicar si estamos en modo test
    private static boolean testMode = false;

    public static void main(String[] args) {

        //Ejecucion hardcodeada
        if (args.length == 0) {
            args = new String[]{"src/main/resources/test_bueno.txt"};
        }

        if (args.length < 1) {
            System.err.println("Usage: java CompilerMain <input_file>");
            exitWithCode(1);
            return;
        }

        String inputFile = args[0];

        try {
            compileFile(inputFile);
        } catch (Exception e) {
            System.err.println("Error de compilacion: " + e.getMessage());
            e.printStackTrace();
            exitWithCode(1);
        }
    }

    /**
     * Activa el modo test (no hace System.exit)
     */
    public static void setTestMode(boolean mode) {
        testMode = mode;
    }

    /**
     * Sale del programa solo si no estamos en modo test
     */
    private static void exitWithCode(int code) {
        if (!testMode) {
            System.exit(code);
        }
    }

    /**
     * Compila un archivo fuente
     */
    public static void compileFile(String filename) throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPILANDO ARCHIVO: " + filename);
        System.out.println("=".repeat(60));

        // Fase 1: Análisis Léxico y Sintáctico
        ProgramNode ast = parseFile(filename);

        if (ast == null) {
            System.err.println("Parseo fallido. Compilacion abortada.");
            return;
        }

        // Fase 2: Análisis Semántico
        ErrorHandler errorHandler = ErrorHandler.getInstance();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();

        ast.accept(semanticAnalyzer);

        // En compileFile(), después del análisis semántico exitoso:
        if (!errorHandler.hasErrors()) {
            // Fase 3: Generación de código
            CodeGenerator codeGenerator = new CodeGenerator(semanticAnalyzer.getSymbolTable());
            String assemblyCode = (String) ast.accept(codeGenerator);

            // Guardar el código assembler
            String asmFilename = filename.replace(".txt", ".asm");
            try (PrintWriter out = new PrintWriter(asmFilename)) {
                out.println(assemblyCode);
            }

            System.out.println("✓ Código assembler generado en: " + asmFilename);
        }

        // Imprimir resumen
        semanticAnalyzer.printSummary();

        // Resultado final
        System.out.println("\n" + "=".repeat(60));
        if (errorHandler.hasErrors()) {
            System.out.println("❌ COMPILACION FALLIDA");
            System.out.println("=".repeat(60));
            exitWithCode(1); // Usa exitWithCode en lugar de System.exit
        } else {
            System.out.println("✓ COMPILACION EXITOSA");
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
        System.out.println("COMPILANDO CODIGO FUENTE");
        System.out.println("=".repeat(60));

        StringReader stringReader = new StringReader(source);
        Lexer lexer = new Lexer(stringReader);
        MiParser parser = new MiParser(lexer);

        Symbol result = parser.parse();

        if (result == null || !(result.value instanceof ProgramNode)) {
            System.err.println("Parseo fallido. Compilacion abortada.");
            return;
        }

        ProgramNode ast = (ProgramNode) result.value;

        ErrorHandler errorHandler = ErrorHandler.getInstance();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();

        ast.accept(semanticAnalyzer);
        semanticAnalyzer.printSummary();

        System.out.println("\n" + "=".repeat(60));
        if (errorHandler.hasErrors()) {
            System.out.println("❌ COMPILACION FALLIDA");
        } else {
            System.out.println("✓ COMPILACION EXITOSA");
        }
        System.out.println("=".repeat(60));
    }
}
