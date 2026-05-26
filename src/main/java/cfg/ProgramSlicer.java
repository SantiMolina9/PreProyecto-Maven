package cfg;

import java.util.*;

/**
 * Computa el backward program slice a partir de un criterio (nodo del CFG).
 *
 * Algoritmo: BFS hacia atras sobre el PDG (CDG + DDG).
 *   - CDG hacia atras: dado N, agregar todos los predicados A tales que A->N existe en el CDG
 *                     (A controla a N)
 *   - DDG hacia atras: dado N, agregar todos los nodos D tales que D->N existe en el DDG
 *                     (D define una variable que N usa)
 *
 * El conjunto de nodos alcanzados forma el slice.
 */
public class ProgramSlicer {

    private final List<CFGNode> allNodes;
    private final CDGBuilder cdg;
    private final DDGBuilder ddg;

    // DDG invertido: para cada nodo U, lista de aristas DDG que apuntan a U
    private final Map<CFGNode, List<DDGBuilder.DDGEdge>> reverseDDG = new HashMap<>();

    public ProgramSlicer(List<CFGNode> allNodes, CDGBuilder cdg, DDGBuilder ddg) {
        this.allNodes = allNodes;
        this.cdg = cdg;
        this.ddg = ddg;
        for (DDGBuilder.DDGEdge edge : ddg.getEdges()) {
            reverseDDG.computeIfAbsent(edge.getTo(), k -> new ArrayList<>()).add(edge);
        }
    }

    // ========== Slicing ==========

    /**
     * Computa el backward slice desde el nodo criterio.
     * Retorna el conjunto de nodos CFG que forman el slice.
     */
    public Set<CFGNode> slice(CFGNode criterion) {
        Set<CFGNode> slice = new LinkedHashSet<>();
        Deque<CFGNode> worklist = new ArrayDeque<>();

        slice.add(criterion);
        worklist.add(criterion);

        while (!worklist.isEmpty()) {
            CFGNode node = worklist.poll();

            // Backward CDG: predicados que controlan este nodo
            for (CDGBuilder.CDGEdge edge : cdg.getIncomingDependencies(node)) {
                CFGNode predicate = edge.getPredicate();
                if (slice.add(predicate)) {
                    worklist.add(predicate);
                }
            }

            // Backward DDG: definiciones que alcanzan y son usadas por este nodo
            for (DDGBuilder.DDGEdge edge : reverseDDG.getOrDefault(node, Collections.emptyList())) {
                CFGNode def = edge.getFrom();
                if (slice.add(def)) {
                    worklist.add(def);
                }
            }
        }

        return slice;
    }

    // ========== Utilidades ==========

    /** Busca un CFGNode por su ID. Retorna null si no existe. */
    public CFGNode findById(int id) {
        for (CFGNode n : allNodes) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    /** Retorna el slice ordenado por ID de nodo. */
    public List<CFGNode> getSliceOrdered(Set<CFGNode> slice) {
        List<CFGNode> sorted = new ArrayList<>(slice);
        sorted.sort(Comparator.comparingInt(CFGNode::getId));
        return sorted;
    }

    // ========== Impresion ==========

    public void printSlice(CFGNode criterion, Set<CFGNode> slice) {
        System.out.println("\n--- Program Slice ---");
        System.out.printf("Criterio: n%d [%s] (%s)%n",
                criterion.getId(),
                criterion.getLabel().isEmpty() ? "(join)" : criterion.getLabel(),
                criterion.getType());
        System.out.printf("Nodos en el slice: %d de %d%n", slice.size(), allNodes.size());
        System.out.println();

        for (CFGNode n : getSliceOrdered(slice)) {
            String label = n.getLabel().isEmpty() ? "(join)" : n.getLabel();
            String marker = (n == criterion) ? " <-- CRITERIO" : "";
            System.out.printf("  n%-3d %-30s [%s]%s%n",
                    n.getId(), label, n.getType(), marker);
        }
        System.out.println("--- Fin Program Slice ---\n");
    }

    // ========== Reconstruccion de aristas para CFG del slice ==========

    /**
     * Reconstruye las aristas del CFG restringido al slice.
     *
     * Para cada nodo N en el slice y cada arista CFG (N->S, label L):
     *   - Si S pertenece al slice: agrega arista (N->S, L)
     *   - Si S no pertenece al slice: BFS hacia adelante a traves de nodos fuera del slice
     *     hasta encontrar nodos del slice; agrega aristas (N->encontrado, L)
     *
     * De esta forma se preserva la conectividad del programa aun cuando se eliminan
     * instrucciones intermedias.
     */
    public List<CFGNode.CFGEdge> buildSlicedEdges(Set<CFGNode> slice) {
        List<CFGNode.CFGEdge> result = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // evitar duplicados (from:to:label)

        for (CFGNode n : slice) {
            for (CFGNode.CFGEdge outEdge : n.getSuccessors()) {
                collectSliceSuccessors(n, outEdge.getTo(), outEdge.getLabel(), slice, result, seen);
            }
        }

        return result;
    }

    private void collectSliceSuccessors(CFGNode origin, CFGNode current, String label,
                                        Set<CFGNode> slice, List<CFGNode.CFGEdge> result,
                                        Set<String> seen) {
        Deque<CFGNode> queue = new ArrayDeque<>();
        Set<CFGNode> visited = new HashSet<>();
        visited.add(origin); // no volver por donde vinimos
        queue.add(current);

        while (!queue.isEmpty()) {
            CFGNode node = queue.poll();
            if (!visited.add(node)) continue;

            if (slice.contains(node)) {
                String key = origin.getId() + ":" + node.getId() + ":" + label;
                if (seen.add(key)) {
                    result.add(new CFGNode.CFGEdge(origin, node, label));
                }
                // No continuar mas alla de nodos del slice
            } else {
                // Continuar explorando a traves de nodos fuera del slice
                for (CFGNode.CFGEdge e : node.getSuccessors()) {
                    queue.add(e.getTo());
                }
            }
        }
    }
}
