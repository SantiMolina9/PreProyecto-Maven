package cfg;

import java.util.*;

/**
 * Construye el Control Dependence Graph (CDG) a partir del CFG y el PDT.
 *
 * Algoritmo (Ferrante et al., 1987):
 *   Para cada arista (A → B) en el CFG:
 *     Sea L = LCA(A, B) en el PDT
 *     Todos los nodos en el camino de B hasta L (sin incluir L) son control-dependientes de A
 *     Caso especial: si L == A, entonces A tambien es control-dependiente de si mismo
 *                   (ocurre en bucles while: la condicion controla su propia re-evaluacion)
 *
 * Una arista CDG A → Y con etiqueta "True"/"False" significa:
 *   Y se ejecuta solo si A evalua a True/False respectivamente.
 *
 * Nota: los nodos sin aristas CDG entrantes son de ejecucion incondicional
 *   (no dependen de ningun predicado, ej: sentencias de nivel superior).
 */
public class CDGBuilder {

    /** Arista del CDG: del predicado A al nodo dependiente Y, con etiqueta de rama. */
    public static class CDGEdge {
        private final CFGNode predicate;
        private final CFGNode dependent;
        private final String label; // "True", "False", o ""

        public CDGEdge(CFGNode predicate, CFGNode dependent, String label) {
            this.predicate = predicate;
            this.dependent = dependent;
            this.label = label;
        }

        public CFGNode getPredicate() { return predicate; }
        public CFGNode getDependent() { return dependent; }
        public String getLabel()      { return label; }

        @Override
        public String toString() {
            String lbl = label.isEmpty() ? "" : "[" + label + "]";
            return "CDG: n" + predicate.getId() + " --" + lbl + "--> n" + dependent.getId();
        }
    }

    private final List<CFGNode> allNodes;
    private final PostDominatorTreeBuilder pdt;

    // Mapa: predicado -> lista de aristas CDG salientes
    private final Map<CFGNode, List<CDGEdge>> outEdges;

    public CDGBuilder(List<CFGNode> allNodes, PostDominatorTreeBuilder pdt) {
        this.allNodes = allNodes;
        this.pdt = pdt;
        this.outEdges = new LinkedHashMap<>();
        for (CFGNode node : allNodes) {
            outEdges.put(node, new ArrayList<>());
        }
    }

    // ========== Construccion del CDG ==========

    /**
     * Ejecuta el algoritmo de Ferrante para construir el CDG.
     * Procesa cada arista del CFG y determina las dependencias de control resultantes.
     */
    public void build() {
        for (CFGNode a : allNodes) {
            for (CFGNode.CFGEdge cfgEdge : a.getSuccessors()) {
                CFGNode b = cfgEdge.getTo();
                String label = cfgEdge.getLabel();

                CFGNode lca = findLCA(a, b);
                if (lca == null) continue;

                // Todos los nodos en el camino de B hasta LCA (sin LCA) dependen de A
                List<CFGNode> path = pathToLCA(b, lca);
                for (CFGNode dep : path) {
                    addEdge(a, dep, label);
                }

                // Si LCA == A: A tambien es control-dependiente de si mismo (loops)
                if (lca == a) {
                    addEdge(a, a, label);
                }
            }
        }
    }

    private void addEdge(CFGNode predicate, CFGNode dependent, String label) {
        List<CDGEdge> edges = outEdges.get(predicate);
        for (CDGEdge existing : edges) {
            if (existing.getDependent() == dependent && existing.getLabel().equals(label)) {
                return; // evitar duplicados
            }
        }
        edges.add(new CDGEdge(predicate, dependent, label));
    }

    // ========== Auxiliares PDT ==========

    /**
     * Encuentra el LCA (ancestro comun mas profundo) de a y b en el PDT.
     * Sube desde b hasta encontrar un nodo que tambien sea ancestro de a.
     */
    private CFGNode findLCA(CFGNode a, CFGNode b) {
        Set<CFGNode> ancestorsOfA = new LinkedHashSet<>();
        CFGNode curr = a;
        while (curr != null) {
            ancestorsOfA.add(curr);
            curr = pdt.getParent(curr);
        }
        curr = b;
        while (curr != null && !ancestorsOfA.contains(curr)) {
            curr = pdt.getParent(curr);
        }
        return curr;
    }

    /**
     * Retorna el camino desde start hasta lca en el PDT (sin incluir lca).
     */
    private List<CFGNode> pathToLCA(CFGNode start, CFGNode lca) {
        List<CFGNode> path = new ArrayList<>();
        CFGNode curr = start;
        while (curr != null && curr != lca) {
            path.add(curr);
            curr = pdt.getParent(curr);
        }
        return path;
    }

    // ========== Consultas ==========

    /** Retorna el mapa completo predicado -> aristas CDG salientes. */
    public Map<CFGNode, List<CDGEdge>> getAllEdges() {
        return Collections.unmodifiableMap(outEdges);
    }

    /** Retorna los nodos de los que 'node' es control-dependiente (aristas CDG entrantes). */
    public List<CDGEdge> getIncomingDependencies(CFGNode node) {
        List<CDGEdge> result = new ArrayList<>();
        for (List<CDGEdge> edges : outEdges.values()) {
            for (CDGEdge edge : edges) {
                if (edge.getDependent() == node) {
                    result.add(edge);
                }
            }
        }
        return result;
    }

    // ========== Impresion ==========

    /**
     * Imprime el CDG por consola mostrando cada predicado y sus dependientes.
     */
    public void printCDG() {
        System.out.println("\n--- Control Dependence Graph (CDG) ---");

        boolean hasEdges = false;
        for (CFGNode node : allNodes) {
            List<CDGEdge> edges = outEdges.get(node);
            if (!edges.isEmpty()) {
                hasEdges = true;
                System.out.println(node);
                for (CDGEdge edge : edges) {
                    String lbl = edge.getLabel().isEmpty() ? "" : " [" + edge.getLabel() + "]";
                    System.out.println("  --" + lbl + "--> " + edge.getDependent());
                }
            }
        }

        if (!hasEdges) {
            System.out.println("(sin dependencias de control)");
        }

        System.out.println("\nResumen por nodo:");
        System.out.printf("%-40s | %s%n", "Nodo", "Control-dependiente de");
        System.out.println("-".repeat(80));
        for (CFGNode node : allNodes) {
            List<CDGEdge> incoming = getIncomingDependencies(node);
            String deps = incoming.isEmpty()
                    ? "(incondicional)"
                    : formatIncoming(incoming);
            System.out.printf("%-40s | %s%n", formatNode(node), deps);
        }
        System.out.println("-".repeat(80));

        System.out.println("--- Fin CDG ---\n");
    }

    private String formatNode(CFGNode node) {
        String label = node.getLabel().isEmpty() ? "(join)" : node.getLabel();
        return "Node[" + node.getId() + ": " + label + " (" + node.getType() + ")]";
    }

    private String formatIncoming(List<CDGEdge> edges) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CDGEdge e : edges) {
            if (!first) sb.append(", ");
            sb.append("n").append(e.getPredicate().getId());
            if (!e.getLabel().isEmpty()) sb.append("(").append(e.getLabel()).append(")");
            first = false;
        }
        return sb.toString();
    }
}
