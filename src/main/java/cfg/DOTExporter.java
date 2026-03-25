package cfg;

import cfg.CFGNode.CFGEdge;

import java.util.List;

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
     * Exporta la lista de nodos del CFG a formato DOT.
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
}
