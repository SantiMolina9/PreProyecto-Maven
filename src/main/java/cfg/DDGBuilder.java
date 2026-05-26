package cfg;

import java.util.*;

/**
 * Construye el Data Dependence Graph (DDG) a partir de los pares Definicion-Uso.
 *
 * Un par (D, U) existe si:
 *   - U usa la variable v
 *   - D define v
 *   - D esta en IN(U)  (es decir, la definicion de D alcanza U)
 *
 * Las aristas del DDG van de D a U, etiquetadas con el nombre de la variable.
 */
public class DDGBuilder {

    // ========== Arista del DDG ==========

    public static class DDGEdge {
        private final CFGNode from;     // nodo que define la variable
        private final CFGNode to;       // nodo que usa la variable
        private final String variable;  // variable involucrada

        public DDGEdge(CFGNode from, CFGNode to, String variable) {
            this.from = from;
            this.to = to;
            this.variable = variable;
        }

        public CFGNode getFrom()     { return from; }
        public CFGNode getTo()       { return to; }
        public String getVariable()  { return variable; }

        @Override
        public String toString() {
            return "n" + from.getId() + " -[" + variable + "]-> n" + to.getId();
        }
    }

    // ========== Estado ==========

    private final List<CFGNode> nodes;
    private final Map<CFGNode, Set<Definition>> inSets;
    private final List<DDGEdge> edges = new ArrayList<>();

    // Para lookup rapido: defUseByNode[U] = lista de pares (D, v)
    private final Map<CFGNode, List<DDGEdge>> edgesFromNode = new LinkedHashMap<>();

    public DDGBuilder(List<CFGNode> nodes, Map<CFGNode, Set<Definition>> inSets) {
        this.nodes = nodes;
        this.inSets = inSets;
    }

    // ========== Construccion ==========

    public void build() {
        for (CFGNode u : nodes) {
            Set<String> usedVars = u.getUsedVars();
            if (usedVars == null || usedVars.isEmpty()) continue;

            Set<Definition> inSet = inSets.get(u);
            if (inSet == null || inSet.isEmpty()) continue;

            for (String v : usedVars) {
                for (Definition d : inSet) {
                    if (d.getVariable().equals(v)) {
                        DDGEdge edge = new DDGEdge(d.getNode(), u, v);
                        edges.add(edge);
                        edgesFromNode
                            .computeIfAbsent(d.getNode(), k -> new ArrayList<>())
                            .add(edge);
                    }
                }
            }
        }
    }

    // ========== Getters ==========

    public List<DDGEdge> getEdges() { return edges; }

    public List<DDGEdge> getEdgesFrom(CFGNode node) {
        return edgesFromNode.getOrDefault(node, Collections.emptyList());
    }

    // ========== Impresion ==========

    public void printDDG() {
        System.out.println("\n--- Data Dependence Graph (DDG) ---");
        System.out.println("Pares Definicion-Uso:");

        if (edges.isEmpty()) {
            System.out.println("  (sin dependencias de datos)");
        } else {
            // Agrupar por variable para mejor legibilidad
            Map<String, List<DDGEdge>> byVar = new TreeMap<>();
            for (DDGEdge e : edges) {
                byVar.computeIfAbsent(e.getVariable(), k -> new ArrayList<>()).add(e);
            }
            for (Map.Entry<String, List<DDGEdge>> entry : byVar.entrySet()) {
                System.out.println("  Variable '" + entry.getKey() + "':");
                for (DDGEdge e : entry.getValue()) {
                    String defLabel = e.getFrom().getLabel().isEmpty()
                            ? "(join)" : e.getFrom().getLabel();
                    String useLabel = e.getTo().getLabel().isEmpty()
                            ? "(join)" : e.getTo().getLabel();
                    System.out.printf("    (D=n%d: %s, U=n%d: %s)%n",
                            e.getFrom().getId(), defLabel,
                            e.getTo().getId(), useLabel);
                }
            }
        }

        System.out.println("--- Fin DDG ---\n");
    }
}
