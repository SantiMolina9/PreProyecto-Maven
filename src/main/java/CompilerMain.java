import com.ejemplo.parser.MiParser;
import ast.nodes.program.ProgramNode;
import cfg.CDGBuilder;
import cfg.CFGBuilder;
import cfg.CFGNode;
import cfg.DDGBuilder;
import cfg.DOTExporter;
import cfg.PostDominatorComputer;
import cfg.PostDominatorTreeBuilder;
import cfg.ProgramSlicer;
import cfg.ReachingDefinitionsComputer;

import java.util.Set;

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
            args = new String[]{"src/main/resources/cfg/test_cfg.txt"};
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

            // Calcular directorio de salida para archivos DOT
            File inputFileObj = new File(inputFile);
            String baseName = inputFileObj.getName().replace(".txt", "");
            File dotOutputDir = new File(inputFileObj.getParent(), "dot");
            dotOutputDir.mkdirs();
            String dotBase = new File(dotOutputDir, baseName).getPath();

            // Exportar CFG con PDOM
            String dot = DOTExporter.exportWithPdom(
                    cfgBuilder.getAllNodes(),
                    pdomComputer.getAllPdom()
            );

            String dotFile = dotBase + ".dot";
            try (PrintWriter out = new PrintWriter(dotFile)) {
                out.print(dot);
            }

            System.out.println("Archivo DOT del CFG+PDOM generado: " + dotFile);

            // Exportar Arbol de Post-Dominadores
            String pdtDot = DOTExporter.exportPDT(pdtBuilder);

            String pdtDotFile = dotBase + "_pdt.dot";
            try (PrintWriter out = new PrintWriter(pdtDotFile)) {
                out.print(pdtDot);
            }

            System.out.println("Archivo DOT del PDT generado: " + pdtDotFile);

            // ========================================
            // FASE 6: Control Dependence Graph (CDG)
            // ========================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("FASE 6: CONTROL DEPENDENCE GRAPH (CDG)");
            System.out.println("=".repeat(60));

            CDGBuilder cdgBuilder = new CDGBuilder(cfgBuilder.getAllNodes(), pdtBuilder,
                    cfgBuilder.getEntryNode(), cfgBuilder.getExitNode());
            cdgBuilder.build();
            cdgBuilder.printCDG();

            String cdgDot = DOTExporter.exportCDG(cfgBuilder.getAllNodes(), cdgBuilder);

            String cdgDotFile = dotBase + "_cdg.dot";
            try (PrintWriter out = new PrintWriter(cdgDotFile)) {
                out.print(cdgDot);
            }

            System.out.println("Archivo DOT del CDG generado: " + cdgDotFile);

            // ========================================
            // FASE 7: Reaching Definitions
            // ========================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("FASE 7: REACHING DEFINITIONS");
            System.out.println("=".repeat(60));

            ReachingDefinitionsComputer rdComputer = new ReachingDefinitionsComputer(
                    cfgBuilder.getAllNodes()
            );
            rdComputer.compute();
            rdComputer.printReachingDefs();

            // ========================================
            // FASE 8: Data Dependence Graph (DDG)
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("FASE 8: DATA DEPENDENCE GRAPH (DDG)");
            System.out.println("=".repeat(60));

            DDGBuilder ddgBuilder = new DDGBuilder(
                    cfgBuilder.getAllNodes(),
                    rdComputer.getInSets()
            );
            ddgBuilder.build();
            ddgBuilder.printDDG();

            String ddgDot = DOTExporter.exportDDG(cfgBuilder.getAllNodes(), ddgBuilder);

            String ddgDotFile = dotBase + "_ddg.dot";
            try (PrintWriter out = new PrintWriter(ddgDotFile)) {
                out.print(ddgDot);
            }

            System.out.println("Archivo DOT del DDG generado: " + ddgDotFile);

            // Exportar Program Dependence Graph (PDG = CDG + DDG)
            String pdgDot = DOTExporter.exportPDG(cfgBuilder.getAllNodes(), cdgBuilder, ddgBuilder);

            String pdgDotFile = dotBase + "_pdg.dot";
            try (PrintWriter out = new PrintWriter(pdgDotFile)) {
                out.print(pdgDot);
            }

            System.out.println("Archivo DOT del PDG generado: " + pdgDotFile);

            // ========================================
            // FASE 9: Program Slicing
            // ========================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("FASE 9: PROGRAM SLICING");
            System.out.println("=".repeat(60));

            ProgramSlicer slicer = new ProgramSlicer(
                    cfgBuilder.getAllNodes(), cdgBuilder, ddgBuilder
            );

            String sliceFile = null;
            String slicedCfgFile = null;

            if (args.length >= 2) {
                int criterionId;
                try {
                    criterionId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Error: el criterio de slice debe ser un numero entero (ID de nodo).");
                    criterionId = -1;
                }

                CFGNode criterion = (criterionId >= 0) ? slicer.findById(criterionId) : null;

                if (criterion == null) {
                    System.err.println("Error: nodo con ID " + criterionId + " no encontrado.");
                    printAvailableNodes(cfgBuilder.getAllNodes());
                } else {
                    Set<CFGNode> slice = slicer.slice(criterion);
                    slicer.printSlice(criterion, slice);

                    // DOT 1: CFG completo con slice resaltado
                    String sliceDot = DOTExporter.exportSliceHighlighted(
                            cfgBuilder.getAllNodes(), slice, criterion
                    );
                    sliceFile = dotBase + "_slice.dot";
                    try (PrintWriter out = new PrintWriter(sliceFile)) {
                        out.print(sliceDot);
                    }
                    System.out.println("Archivo DOT del slice (highlighted) generado: " + sliceFile);

                    // DOT 2: CFG reducido al slice
                    String slicedCfgDot = DOTExporter.exportSlicedCFG(
                            cfgBuilder.getAllNodes(), slice, criterion, slicer
                    );
                    slicedCfgFile = dotBase + "_sliced_cfg.dot";
                    try (PrintWriter out = new PrintWriter(slicedCfgFile)) {
                        out.print(slicedCfgDot);
                    }
                    System.out.println("Archivo DOT del CFG reducido generado: " + slicedCfgFile);
                }
            } else {
                System.out.println("Slicing no ejecutado. Para activarlo:");
                System.out.println("  Uso: java CompilerMain <archivo> <nodeId>");
                System.out.println();
                printAvailableNodes(cfgBuilder.getAllNodes());
            }

            // ========================================
            // Resumen
            // ========================================
            System.out.println("=".repeat(60));
            System.out.println("COMPLETADO EXITOSAMENTE");
            System.out.println("=".repeat(60));
            System.out.println("\nArchivos generados:");
            System.out.println("  CFG + PDOM: " + dotFile);
            System.out.println("  PDT:        " + pdtDotFile);
            System.out.println("  CDG:        " + cdgDotFile);
            System.out.println("  DDG:        " + ddgDotFile);
            System.out.println("  PDG:        " + pdgDotFile);
            if (sliceFile != null) {
                System.out.println("  Slice:      " + sliceFile);
                System.out.println("  Sliced CFG: " + slicedCfgFile);
            }
            System.out.println("\nPara visualizar, ejecutar:");
            System.out.println("  dot -Tpng " + dotFile + " -o cfg.png");
            System.out.println("  dot -Tpng " + pdtDotFile + " -o pdt.png");
            System.out.println("  dot -Tpng " + cdgDotFile + " -o cdg.png");
            System.out.println("  dot -Tpng " + ddgDotFile + " -o ddg.png");
            System.out.println("  dot -Tpng " + pdgDotFile + " -o pdg.png");
            if (sliceFile != null) {
                System.out.println("  dot -Tpng " + sliceFile + " -o slice.png");
                System.out.println("  dot -Tpng " + slicedCfgFile + " -o sliced_cfg.png");
            }
            System.out.println("\nO pegar el contenido DOT en: https://dreampuf.github.io/GraphvizOnline/");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printAvailableNodes(java.util.List<CFGNode> nodes) {
        System.out.println("Nodos disponibles como criterio de slice:");
        for (CFGNode n : nodes) {
            String label = n.getLabel().isEmpty() ? "(join)" : n.getLabel();
            System.out.printf("  n%-3d %-30s [%s]%n", n.getId(), label, n.getType());
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
