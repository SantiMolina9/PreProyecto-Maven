import com.ejemplo.parser.MiParser;
import ast.nodes.program.ProgramNode;
import cfg.CFGBuilder;
import cfg.DOTExporter;
import cfg.PostDominatorComputer;

import java.io.*;
import java_cup.runtime.Symbol;

/**
 * Clase principal del CFG Builder.
 * Pipeline: Codigo fuente -> Lexer -> Parser -> AST -> CFG -> DOT (Graphviz)
 */
public class CompilerMain {

    public static void main(String[] args) {

        // Si no se pasa argumento, usar archivo de test por defecto
        if (args.length == 0) {
            args = new String[]{"src/main/resources/test_cfg.txt"};
        }

        if (args.length < 1) {
            System.err.println("Uso: java CompilerMain <archivo_entrada>");
            System.exit(1);
            return;
        }

        String inputFile = args[0];

        try {
            // ========================================
            // FASE 1: Analisis Lexico y Sintactico
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("FASE 1: ANALISIS LEXICO Y SINTACTICO");
            System.out.println("=".repeat(60));

            ProgramNode ast = parseFile(inputFile);

            if (ast == null) {
                System.err.println("Error: Parseo fallido. Abortando.");
                System.exit(1);
                return;
            }

            System.out.println("Parseo exitoso.");
            System.out.println("AST: " + ast);

            // ========================================
            // FASE 2: Construccion del CFG
            // ========================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("FASE 2: CONSTRUCCION DEL CFG");
            System.out.println("=".repeat(60));

            CFGBuilder cfgBuilder = new CFGBuilder();
            cfgBuilder.build(ast);

            System.out.println("CFG construido exitosamente.");
            System.out.println("Total de nodos: " + cfgBuilder.getAllNodes().size());

            // Imprimir representacion textual del CFG
            cfgBuilder.printCFG();

            // ========================================
            // FASE 3: Exportar a DOT (Graphviz)
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("FASE 3: GENERACION DOT (Graphviz)");
            System.out.println("=".repeat(60));

            String dot = DOTExporter.export(cfgBuilder.getAllNodes());

            // Guardar archivo DOT
            String dotFile = inputFile.replace(".txt", ".dot");
            try (PrintWriter out = new PrintWriter(dotFile)) {
                out.print(dot);
            }

            System.out.println("Archivo DOT generado: " + dotFile);
            System.out.println("\nContenido del archivo DOT:");
            System.out.println("-".repeat(40));
            System.out.println(dot);
            System.out.println("-".repeat(40));

            // ========================================
            // FASE 4: Computo de Post-Dominadores
            // ========================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("FASE 4: POST-DOMINADORES");
            System.out.println("=".repeat(60));

            PostDominatorComputer pdomComputer = new PostDominatorComputer(
                    cfgBuilder.getAllNodes(),
                    cfgBuilder.getExitNode()
            );
            pdomComputer.compute();
            pdomComputer.printPdom();

            // ========================================
            // Resumen
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("COMPLETADO EXITOSAMENTE");
            System.out.println("=".repeat(60));
            System.out.println("\nPara visualizar el CFG, ejecutar:");
            System.out.println("  dot -Tpng " + dotFile + " -o cfg.png");
            System.out.println("  dot -Tsvg " + dotFile + " -o cfg.svg");
            System.out.println("  dot -Tpdf " + dotFile + " -o cfg.pdf");
            System.out.println("\nO pegar el contenido DOT en: https://dreampuf.github.io/GraphvizOnline/");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parsea un archivo fuente y retorna el AST.
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
}
