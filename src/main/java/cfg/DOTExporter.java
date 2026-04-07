package cfg;

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
        return export(nodes, null);
    }

    /**
     * Exporta la lista de nodos del CFG a formato DOT, incluyendo
     * opcionalmente la informacion de Post-Dominadores (PDOM) en cada nodo.
     */
    public static String export(List<CFGNode> nodes, Map<CFGNode, Set<CFGNode>> pdomMap) {
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

            // Construir label con PDOM si esta disponible
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
                    if (pdomMap != null) {
                        // Si hay PDOM, mostrar el join como nodo visible con info
                        sb.append("shape=ellipse, style=filled, fillcolor=lightyellow, ")
                          .append("label=\"n").append(node.getId()).append(" (join)")
                          .append(pdomSuffix).append("\"");
                    } else {
                        sb.append("shape=point, width=0.15");
                    }
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
                    // Colorear aristas True/False
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

        // Si hay PDOM, agregar tabla resumen como subgrafo
        if (pdomMap != null) {
            sb.append("\n");
            sb.append("    // Tabla resumen de Post-Dominadores\n");
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
