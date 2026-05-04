package cfg;

import cfg.CDGBuilder.CDGEdge;
import cfg.CFGNode.CFGEdge;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exporta el CFG a formato DOT (Graphviz) para visualizacion.
 *
 * Uso: generar archivo .dot y renderizar con:
 *   dot -Tpng archivo.dot -o cfg.png
 *   dot -Tsvg archivo.dot -o cfg.svg
 *   dot -Tpdf archivo.dot -o cfg.pdf
 */
public class DOTExporter {

    /**
     * Exporta la lista de nodos del CFG a formato DOT (sin PDOM).
     */
    public static String export(List<CFGNode> nodes) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph CFG {\n");
        sb.append("    // Configuracion general\n");
        sb.append("    rankdir=TB;\n");
        sb.append("    fontname=\"Arial\";\n");
        sb.append("    node [fontname=\"Arial\", fontsize=12];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=10];\n");
        sb.append("\n");

        // Declarar nodos
        for (CFGNode node : nodes) {
            sb.append("    ").append(nodeId(node)).append(" [");

            switch (node.getType()) {
                case ENTRY:
                    sb.append("shape=circle, width=0.3, fixedsize=true, ")
                      .append("style=filled, fillcolor=green, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case EXIT:
                    sb.append("shape=doublecircle, width=0.3, fixedsize=true, ")
                      .append("style=filled, fillcolor=red, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case JOIN:
                    sb.append("shape=point, width=0.15");
                    break;

                case CONDITION:
                    sb.append("shape=diamond, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"").append(escapeLabel(node.getLabel())).append("\"");
                    break;

                case STATEMENT:
                    sb.append("shape=box, style=\"rounded,filled\", fillcolor=lightblue, ")
                      .append("label=\"").append(escapeLabel(node.getLabel())).append("\"");
                    break;
            }

            sb.append("];\n");
        }

        sb.append("\n");

        // Declarar aristas
        for (CFGNode node : nodes) {
            for (CFGEdge edge : node.getSuccessors()) {
                sb.append("    ")
                  .append(nodeId(edge.getFrom()))
                  .append(" -> ")
                  .append(nodeId(edge.getTo()));

                if (edge.getLabel() != null && !edge.getLabel().isEmpty()) {
                    sb.append(" [label=\"").append(edge.getLabel()).append("\"");
                    if ("True".equals(edge.getLabel())) {
                        sb.append(", color=darkgreen, fontcolor=darkgreen");
                    } else if ("False".equals(edge.getLabel())) {
                        sb.append(", color=red, fontcolor=red");
                    }
                    sb.append("]");
                }

                sb.append(";\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Exporta el CFG a formato DOT con Post-Dominadores (PDOM) anotados en cada nodo.
     * Genera ademas una tabla resumen de PDOM.
     */
    public static String exportWithPdom(List<CFGNode> nodes, Map<CFGNode, Set<CFGNode>> pdomMap) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph CFG_PDOM {\n");
        sb.append("    rankdir=TB;\n");
        sb.append("    fontname=\"Arial\";\n");
        sb.append("    node [fontname=\"Arial\", fontsize=11];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=10];\n");
        sb.append("\n");

        // Declarar nodos con info PDOM
        for (CFGNode node : nodes) {
            sb.append("    ").append(nodeId(node)).append(" [");

            String pdomSuffix = "";
            if (pdomMap != null && pdomMap.containsKey(node)) {
                pdomSuffix = "\\nPDOM: " + formatPdomSet(pdomMap.get(node));
            }

            switch (node.getType()) {
                case ENTRY:
                    sb.append("shape=circle, width=0.5, fixedsize=true, ")
                      .append("style=filled, fillcolor=green, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case EXIT:
                    sb.append("shape=doublecircle, width=0.5, fixedsize=true, ")
                      .append("style=filled, fillcolor=red, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case JOIN:
                    sb.append("shape=ellipse, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"n").append(node.getId()).append(" (join)")
                      .append(pdomSuffix).append("\"");
                    break;

                case CONDITION:
                    sb.append("shape=diamond, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"n").append(node.getId()).append(": ")
                      .append(escapeLabel(node.getLabel()))
                      .append(pdomSuffix).append("\"");
                    break;

                case STATEMENT:
                    sb.append("shape=box, style=\"rounded,filled\", fillcolor=lightblue, ")
                      .append("label=\"n").append(node.getId()).append(": ")
                      .append(escapeLabel(node.getLabel()))
                      .append(pdomSuffix).append("\"");
                    break;
            }

            sb.append("];\n");
        }

        sb.append("\n");

        // Declarar aristas
        for (CFGNode node : nodes) {
            for (CFGEdge edge : node.getSuccessors()) {
                sb.append("    ")
                  .append(nodeId(edge.getFrom()))
                  .append(" -> ")
                  .append(nodeId(edge.getTo()));

                if (edge.getLabel() != null && !edge.getLabel().isEmpty()) {
                    sb.append(" [label=\"").append(edge.getLabel()).append("\"");
                    if ("True".equals(edge.getLabel())) {
                        sb.append(", color=darkgreen, fontcolor=darkgreen");
                    } else if ("False".equals(edge.getLabel())) {
                        sb.append(", color=red, fontcolor=red");
                    }
                    sb.append("]");
                }

                sb.append(";\n");
            }
        }

        // Tabla resumen de Post-Dominadores
        if (pdomMap != null) {
            sb.append("\n");
            sb.append("    pdom_table [shape=plaintext, label=<\n");
            sb.append("        <TABLE BORDER=\"1\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">\n");
            sb.append("        <TR><TD COLSPAN=\"2\" BGCOLOR=\"lightgray\"><B>Post-Dominadores (PDOM)</B></TD></TR>\n");
            sb.append("        <TR><TD BGCOLOR=\"lightgray\"><B>Nodo</B></TD><TD BGCOLOR=\"lightgray\"><B>PDOM</B></TD></TR>\n");
            for (CFGNode node : nodes) {
                Set<CFGNode> pdomSet = pdomMap.get(node);
                if (pdomSet != null) {
                    String nodeLabel = node.getLabel().isEmpty()
                        ? "n" + node.getId() + " (join)"
                        : "n" + node.getId() + ": " + node.getLabel();
                    sb.append("        <TR><TD>").append(escapeHtml(nodeLabel))
                      .append("</TD><TD>").append(escapeHtml(formatPdomSet(pdomSet)))
                      .append("</TD></TR>\n");
                }
            }
            sb.append("        </TABLE>\n");
            sb.append("    >];\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Genera el ID unico para un nodo en DOT.
     */
    private static String nodeId(CFGNode node) {
        return "n" + node.getId();
    }

    /**
     * Escapa caracteres especiales para labels en DOT.
     */
    private static String escapeLabel(String label) {
        if (label == null) return "";
        return label.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");
    }

    /**
     * Escapa caracteres especiales para HTML en labels DOT.
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    /**
     * Exporta el Arbol de Post-Dominadores (PDT) a formato DOT.
     * El arbol tiene como raiz al nodo EXIT y cada arista padre->hijo
     * indica que el padre es el post-dominador inmediato del hijo.
     */
    public static String exportPDT(PostDominatorTreeBuilder pdt) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph PDT {\n");
        sb.append("    // Arbol de Post-Dominadores\n");
        sb.append("    rankdir=TB;\n");
        sb.append("    fontname=\"Arial\";\n");
        sb.append("    node [fontname=\"Arial\", fontsize=12];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=10];\n");
        sb.append("    label=\"Arbol de Post-Dominadores (PDT)\";\n");
        sb.append("    labelloc=t;\n");
        sb.append("\n");

        // Recolectar todos los nodos del arbol via BFS desde la raiz
        List<CFGNode> treeNodes = new java.util.ArrayList<>();
        java.util.Queue<CFGNode> queue = new java.util.LinkedList<>();
        queue.add(pdt.getRoot());
        while (!queue.isEmpty()) {
            CFGNode current = queue.poll();
            treeNodes.add(current);
            for (CFGNode child : pdt.getChildren(current)) {
                queue.add(child);
            }
        }

        // Declarar nodos
        for (CFGNode node : treeNodes) {
            sb.append("    ").append(nodeId(node)).append(" [");

            switch (node.getType()) {
                case ENTRY:
                    sb.append("shape=circle, width=0.5, style=filled, fillcolor=green, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case EXIT:
                    sb.append("shape=doublecircle, width=0.5, style=filled, fillcolor=red, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case JOIN:
                    sb.append("shape=ellipse, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"n").append(node.getId()).append(" (join)\"");
                    break;

                case CONDITION:
                    sb.append("shape=diamond, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"n").append(node.getId()).append(": ")
                      .append(escapeLabel(node.getLabel())).append("\"");
                    break;

                case STATEMENT:
                    sb.append("shape=box, style=\"rounded,filled\", fillcolor=lightblue, ")
                      .append("label=\"n").append(node.getId()).append(": ")
                      .append(escapeLabel(node.getLabel())).append("\"");
                    break;
            }

            sb.append("];\n");
        }

        sb.append("\n");

        // Declarar aristas del arbol (padre -> hijo)
        for (CFGNode node : treeNodes) {
            for (CFGNode child : pdt.getChildren(node)) {
                sb.append("    ")
                  .append(nodeId(node))
                  .append(" -> ")
                  .append(nodeId(child))
                  .append(";\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Exporta el Control Dependence Graph (CDG) a formato DOT.
     * Las aristas CDG se muestran como flechas punteadas del predicado al nodo dependiente.
     */
    public static String exportCDG(List<CFGNode> nodes, CDGBuilder cdg) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph CDG {\n");
        sb.append("    // Control Dependence Graph\n");
        sb.append("    rankdir=TB;\n");
        sb.append("    fontname=\"Arial\";\n");
        sb.append("    node [fontname=\"Arial\", fontsize=12];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=10];\n");
        sb.append("    label=\"Control Dependence Graph (CDG)\";\n");
        sb.append("    labelloc=t;\n");
        sb.append("\n");

        // Declarar nodos (mismo estilo que el CFG)
        for (CFGNode node : nodes) {
            sb.append("    ").append(nodeId(node)).append(" [");

            switch (node.getType()) {
                case ENTRY:
                    sb.append("shape=circle, width=0.5, style=filled, fillcolor=green, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case EXIT:
                    sb.append("shape=doublecircle, width=0.5, style=filled, fillcolor=red, ")
                      .append("label=\"").append(node.getLabel()).append("\"");
                    break;

                case JOIN:
                    sb.append("shape=ellipse, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"n").append(node.getId()).append(" (join)\"");
                    break;

                case CONDITION:
                    sb.append("shape=diamond, style=filled, fillcolor=lightyellow, ")
                      .append("label=\"n").append(node.getId()).append(": ")
                      .append(escapeLabel(node.getLabel())).append("\"");
                    break;

                case STATEMENT:
                    sb.append("shape=box, style=\"rounded,filled\", fillcolor=lightblue, ")
                      .append("label=\"n").append(node.getId()).append(": ")
                      .append(escapeLabel(node.getLabel())).append("\"");
                    break;
            }

            sb.append("];\n");
        }

        sb.append("\n");

        // Declarar aristas CDG (punteadas)
        Map<CFGNode, List<CDGEdge>> allEdges = cdg.getAllEdges();
        for (CFGNode node : nodes) {
            List<CDGEdge> edges = allEdges.get(node);
            if (edges == null) continue;
            for (CDGEdge edge : edges) {
                sb.append("    ")
                  .append(nodeId(edge.getPredicate()))
                  .append(" -> ")
                  .append(nodeId(edge.getDependent()))
                  .append(" [style=dashed");

                String lbl = edge.getLabel();
                if (lbl != null && !lbl.isEmpty()) {
                    sb.append(", label=\"").append(lbl).append("\"");
                    if ("True".equals(lbl)) {
                        sb.append(", color=darkgreen, fontcolor=darkgreen");
                    } else if ("False".equals(lbl)) {
                        sb.append(", color=red, fontcolor=red");
                    }
                }

                sb.append("];\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Formatea un conjunto PDOM como string legible: {n0, n1, n3}
     */
    private static String formatPdomSet(Set<CFGNode> set) {
        if (set == null || set.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (CFGNode n : set) {
            if (!first) sb.append(", ");
            sb.append("n").append(n.getId());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
