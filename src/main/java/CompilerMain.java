import com.ejemplo.parser.MiParser;
import ast.nodes.program.ProgramNode;
import cfg.CFGBuilder;
import cfg.DOTExporter;
import cfg.PostDominatorComputer;
import cfg.PostDominatorTreeBuilder;

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
            // FASE 3: Computo de Post-Dominadores
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("FASE 3: POST-DOMINADORES");
            System.out.println("=".repeat(60));

            PostDominatorComputer pdomComputer = new PostDominatorComputer(
                    cfgBuilder.getAllNodes(),
                    cfgBuilder.getExitNode()
            );
            pdomComputer.compute();
            pdomComputer.printPdom();

            // ========================================
            // FASE 4: Arbol de Post-Dominadores (PDT)
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("FASE 4: ARBOL DE POST-DOMINADORES (PDT)");
            System.out.println("=".repeat(60));

            PostDominatorTreeBuilder pdtBuilder = new PostDominatorTreeBuilder(
                    cfgBuilder.getAllNodes(),
                    cfgBuilder.getExitNode(),
                    pdomComputer.getAllPdom()
            );
            pdtBuilder.build();
            pdtBuilder.printTree();

            // ========================================
            // FASE 5: Exportar CFG + PDOM y PDT a DOT (Graphviz)
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("FASE 5: GENERACION DOT (Graphviz)");
            System.out.println("=".repeat(60));

            // Exportar CFG con PDOM
            String dot = DOTExporter.exportWithPdom(
                    cfgBuilder.getAllNodes(),
                    pdomComputer.getAllPdom()
            );

            String dotFile = inputFile.replace(".txt", ".dot");
            try (PrintWriter out = new PrintWriter(dotFile)) {
                out.print(dot);
            }

            System.out.println("Archivo DOT del CFG+PDOM generado: " + dotFile);

            // Exportar Arbol de Post-Dominadores
            String pdtDot = DOTExporter.exportPDT(pdtBuilder);

            String pdtDotFile = inputFile.replace(".txt", "_pdt.dot");
            try (PrintWriter out = new PrintWriter(pdtDotFile)) {
                out.print(pdtDot);
            }

            System.out.println("Archivo DOT del PDT generado: " + pdtDotFile);

            System.out.println("\nContenido del archivo DOT (PDT):");
            System.out.println("-".repeat(40));
            System.out.println(pdtDot);
            System.out.println("-".repeat(40));

            // ========================================
            // Resumen
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("COMPLETADO EXITOSAMENTE");
            System.out.println("=".repeat(60));
            System.out.println("\nArchivos generados:");
            System.out.println("  CFG + PDOM: " + dotFile);
            System.out.println("  PDT:        " + pdtDotFile);
            System.out.println("\nPara visualizar, ejecutar:");
            System.out.println("  dot -Tpng " + dotFile + " -o cfg.png");
            System.out.println("  dot -Tpng " + pdtDotFile + " -o pdt.png");
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
