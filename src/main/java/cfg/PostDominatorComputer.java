package cfg;

import java.util.*;

/**
 * Computa los Post-Dominadores (PDOM) de un CFG.
 *
 * Algoritmo (segun clase - slides):
 *   Para computar postdominadores de un CFG G, se obtiene el CFG reverso
 *   invirtiendo todas las aristas, y se corre el algoritmo DOM sobre el CFG reverso.
 *
 *   En el CFG reverso el nodo de entrada es el EXIT del CFG original.
 *   Los predecesores de un nodo x en el CFG reverso = sucesores de x en el original.
 *
 *   Algoritmo DOM iterativo (punto fijo) aplicado al CFG reverso:
 *
 *     PDOM(exit) = {exit}
 *     Para todo nodo x != exit:  PDOM(x) = N  (todos los nodos)
 *
 *     Hasta que no haya cambios:
 *       Para todo nodo x != exit:
 *         PDOM(x) = {x} U (interseccion de PDOM(s) para todo sucesor s de x)
 *
 *   Al terminar: nodo d en PDOM(n) sii d post-domina a n.
 */
public class PostDominatorComputer {

    private final List<CFGNode> allNodes;
    private final CFGNode exitNode;
    private final Map<CFGNode, Set<CFGNode>> pdom;

    public PostDominatorComputer(List<CFGNode> allNodes, CFGNode exitNode) {
        this.allNodes = allNodes;
        this.exitNode = exitNode;
        this.pdom = new LinkedHashMap<>();
    }

    // ========== Computo principal ==========

    /**
     * Ejecuta el algoritmo iterativo de punto fijo para calcular PDOM.
     */
    public void compute() {
        // --- Inicializacion ---
        // PDOM(exit) = {exit}
        Set<CFGNode> exitPdom = new LinkedHashSet<>();
        exitPdom.add(exitNode);
        pdom.put(exitNode, exitPdom);

        // PDOM(x) = N para todo x != exit
        Set<CFGNode> universe = new LinkedHashSet<>(allNodes);
        for (CFGNode node : allNodes) {
            if (node != exitNode) {
                pdom.put(node, new LinkedHashSet<>(universe));
            }
        }

        // --- Iteracion hasta punto fijo ---
        boolean changed = true;
        int iteration = 0;
        while (changed) {
            changed = false;
            iteration++;

            for (CFGNode node : allNodes) {
                if (node == exitNode) continue;

                // Sucesores en el CFG original = predecesores en el CFG reverso
                List<CFGNode> successors = getSuccessors(node);

                // PDOM(x) = {x} U (interseccion PDOM(s) para todo sucesor s)
                Set<CFGNode> newPdom = new LinkedHashSet<>();
                newPdom.add(node); // cada nodo se post-domina a si mismo

                if (!successors.isEmpty()) {
                    // Empezar con el PDOM del primer sucesor y hacer interseccion
                    Set<CFGNode> intersection = new LinkedHashSet<>(pdom.get(successors.get(0)));
                    for (int i = 1; i < successors.size(); i++) {
                        intersection.retainAll(pdom.get(successors.get(i)));
                    }
                    newPdom.addAll(intersection);
                }

                if (!newPdom.equals(pdom.get(node))) {
                    pdom.put(node, newPdom);
                    changed = true;
                }
            }
        }

        System.out.println("PDOM convergido en " + iteration + " iteracion(es).");
    }

    // ========== Consultas ==========

    /**
     * Retorna el conjunto PDOM de un nodo.
     */
    public Set<CFGNode> getPdom(CFGNode node) {
        return Collections.unmodifiableSet(pdom.get(node));
    }

    /**
     * Retorna el mapa completo PDOM (nodo -> conjunto de post-dominadores).
     */
    public Map<CFGNode, Set<CFGNode>> getAllPdom() {
        return Collections.unmodifiableMap(pdom);
    }

    // ========== Impresion ==========

    /**
     * Imprime la tabla de post-dominadores por consola.
     */
    public void printPdom() {
        System.out.println("\n--- Post-Dominadores (PDOM) ---");
        System.out.printf("%-35s | %s%n", "Nodo", "PDOM");
        System.out.println("-".repeat(80));

        for (CFGNode node : allNodes) {
            Set<CFGNode> set = pdom.get(node);
            System.out.printf("%-35s | %s%n", formatNode(node), formatSet(set));
        }

        System.out.println("-".repeat(80));
        System.out.println("--- Fin PDOM ---\n");
    }

    // ========== Auxiliares ==========

    private List<CFGNode> getSuccessors(CFGNode node) {
        List<CFGNode> result = new ArrayList<>();
        for (CFGNode.CFGEdge edge : node.getSuccessors()) {
            result.add(edge.getTo());
        }
        return result;
    }

    private String formatNode(CFGNode node) {
        String label = node.getLabel().isEmpty() ? "(join)" : node.getLabel();
        return "Node[" + node.getId() + ": " + label + " (" + node.getType() + ")]";
    }

    private String formatSet(Set<CFGNode> set) {
        if (set == null || set.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (CFGNode n : set) {
            if (!first) sb.append(", ");
            String label = n.getLabel().isEmpty() ? "(join)" : n.getLabel();
            sb.append(n.getId()).append(":").append(label);
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
