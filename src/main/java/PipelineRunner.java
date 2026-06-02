import ast.nodes.program.ProgramNode;
import cfg.*;
import com.ejemplo.parser.MiParser;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import java_cup.runtime.Symbol;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.*;

public class PipelineRunner extends Task<Void> {

    private final String inputFile;
    private final Integer sliceCriterionId;
    private final List<GraphStage> stages;
    private final Runnable onStageUpdate;

    public PipelineRunner(String inputFile, Integer sliceCriterionId,
                          List<GraphStage> stages, Runnable onStageUpdate) {
        this.inputFile = inputFile;
        this.sliceCriterionId = sliceCriterionId;
        this.stages = stages;
        this.onStageUpdate = onStageUpdate;
    }

    @Override
    protected Void call() throws Exception {
        try {
            Graphviz.useEngine(new GraphvizCmdLineEngine());
        } catch (Exception e) {
            System.err.println("Graphviz dot no disponible: " + e.getMessage());
        }

        // Fase 1: Parsing
        markRunning(0);
        ProgramNode ast = parseFile(inputFile);
        if (ast == null) throw new Exception("Parseo fallido: no se generó AST.");
        markDoneText(0, "Parseo exitoso.\n\nAST:\n" + ast.toString());

        // Fase 2: CFG
        markRunning(1);
        CFGBuilder cfgBuilder = new CFGBuilder();
        cfgBuilder.build(ast);
        markDone(1, DOTExporter.export(cfgBuilder.getAllNodes()));

        // Fase 3: CFG + PDOM
        markRunning(2);
        PostDominatorComputer pdomComputer = new PostDominatorComputer(
                cfgBuilder.getAllNodes(), cfgBuilder.getExitNode());
        pdomComputer.compute();
        markDone(2, DOTExporter.exportWithPdom(cfgBuilder.getAllNodes(), pdomComputer.getAllPdom()));

        // Fase 4: PDT
        markRunning(3);
        PostDominatorTreeBuilder pdtBuilder = new PostDominatorTreeBuilder(
                cfgBuilder.getAllNodes(), cfgBuilder.getExitNode(), pdomComputer.getAllPdom());
        pdtBuilder.build();
        markDone(3, DOTExporter.exportPDT(pdtBuilder));

        // Fase 5: CDG
        markRunning(4);
        CDGBuilder cdgBuilder = new CDGBuilder(
                cfgBuilder.getAllNodes(), pdtBuilder,
                cfgBuilder.getEntryNode(), cfgBuilder.getExitNode());
        cdgBuilder.build();
        markDone(4, DOTExporter.exportCDG(cfgBuilder.getAllNodes(), cdgBuilder));

        // Fase 6: Reaching Definitions (texto)
        markRunning(5);
        ReachingDefinitionsComputer rdComputer = new ReachingDefinitionsComputer(cfgBuilder.getAllNodes());
        rdComputer.compute();
        markDoneText(5, formatReachingDefs(cfgBuilder.getAllNodes(), rdComputer));

        // Fase 7: DDG
        markRunning(6);
        DDGBuilder ddgBuilder = new DDGBuilder(cfgBuilder.getAllNodes(), rdComputer.getInSets());
        ddgBuilder.build();
        markDone(6, DOTExporter.exportDDG(cfgBuilder.getAllNodes(), ddgBuilder));

        // Fase 8: PDG (CDG + DDG)
        markRunning(7);
        markDone(7, DOTExporter.exportPDG(cfgBuilder.getAllNodes(), cdgBuilder, ddgBuilder));

        // Fases 9 y 10: Slicing (opcional)
        if (sliceCriterionId != null) {
            ProgramSlicer slicer = new ProgramSlicer(cfgBuilder.getAllNodes(), cdgBuilder, ddgBuilder);
            CFGNode criterion = slicer.findById(sliceCriterionId);
            if (criterion != null) {
                Set<CFGNode> slice = slicer.slice(criterion);
                markRunning(8);
                markDone(8, DOTExporter.exportSliceHighlighted(cfgBuilder.getAllNodes(), slice, criterion));
                markRunning(9);
                markDone(9, DOTExporter.exportSlicedCFG(cfgBuilder.getAllNodes(), slice, criterion, slicer));
            } else {
                markDoneText(8, "Nodo ID=" + sliceCriterionId + " no encontrado en el CFG.\n\nNodos disponibles:\n"
                        + listNodes(cfgBuilder.getAllNodes()));
                markSkipped(9);
            }
        } else {
            markSkipped(8);
            markSkipped(9);
        }

        return null;
    }

    private void markRunning(int idx) {
        Platform.runLater(() -> {
            stages.get(idx).setStatus(GraphStage.Status.RUNNING);
            onStageUpdate.run();
        });
        sleep(200);
    }

    private void markDone(int idx, String dot) {
        Image img = renderDot(dot);
        Platform.runLater(() -> {
            GraphStage s = stages.get(idx);
            s.setDotSource(dot);
            s.setRenderedImage(img);
            // If rendering failed, store dot as text fallback
            if (img == null && dot != null) {
                s.setTextContent("[Graphviz no disponible — DOT source:]\n\n" + dot);
            }
            s.setStatus(GraphStage.Status.DONE);
            onStageUpdate.run();
        });
    }

    private void markDoneText(int idx, String text) {
        Platform.runLater(() -> {
            GraphStage s = stages.get(idx);
            s.setTextContent(text);
            s.setStatus(GraphStage.Status.DONE);
            onStageUpdate.run();
        });
    }

    private void markSkipped(int idx) {
        Platform.runLater(() -> {
            stages.get(idx).setStatus(GraphStage.Status.SKIPPED);
            onStageUpdate.run();
        });
    }

    private Image renderDot(String dotSource) {
        try {
            guru.nidi.graphviz.parse.Parser parser = new guru.nidi.graphviz.parse.Parser();
            MutableGraph g = parser.read(dotSource);
            RenderedImage ri = Graphviz.fromGraph(g).width(900).render(Format.PNG).toImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(ri, "png", baos);
            return new Image(new ByteArrayInputStream(baos.toByteArray()));
        } catch (Exception e) {
            System.err.println("Render fallido: " + e.getMessage());
            return null;
        }
    }

    private String formatReachingDefs(List<CFGNode> nodes, ReachingDefinitionsComputer rd) {
        Map<CFGNode, Set<Definition>> inSets = rd.getInSets();
        Map<CFGNode, Set<Definition>> outSets = rd.getOutSets();
        StringBuilder sb = new StringBuilder("=== REACHING DEFINITIONS ===\n\n");
        for (CFGNode node : nodes) {
            sb.append(String.format("n%d  [%s]  (%s)%n",
                    node.getId(), node.getLabel(), node.getType()));
            sb.append("  IN:  ").append(formatDefSet(inSets.getOrDefault(node, Collections.emptySet()))).append("\n");
            sb.append("  OUT: ").append(formatDefSet(outSets.getOrDefault(node, Collections.emptySet()))).append("\n\n");
        }
        return sb.toString();
    }

    private String formatDefSet(Set<Definition> defs) {
        if (defs.isEmpty()) return "{ }";
        StringBuilder sb = new StringBuilder("{ ");
        for (Definition d : defs) {
            sb.append("n").append(d.getNode().getId()).append(":").append(d.getVariable()).append("  ");
        }
        sb.append("}");
        return sb.toString();
    }

    private String listNodes(List<CFGNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (CFGNode n : nodes) {
            sb.append(String.format("  n%-3d  %s  [%s]%n", n.getId(), n.getLabel(), n.getType()));
        }
        return sb.toString();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private ProgramNode parseFile(String filename) throws Exception {
        try (FileReader fileReader = new FileReader(filename)) {
            Lexer lexer = new Lexer(fileReader);
            MiParser parser = new MiParser(lexer);
            Symbol result = parser.parse();
            if (result != null && result.value instanceof ProgramNode) {
                return (ProgramNode) result.value;
            }
            return null;
        }
    }
}
