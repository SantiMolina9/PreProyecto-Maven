package cfg;

import java.util.*;

/**
 * Algoritmo iterativo de Reaching Definitions (Data Flow Analysis).
 *
 * Es un analisis forward-may:
 *   GEN(n)  = { (n, v) } si n define la variable v, {} en caso contrario
 *   KILL(n) = todas las definiciones de v en otros nodos, si n define v
 *   IN(n)   = U OUT(pred(n))
 *   OUT(n)  = GEN(n) U (IN(n) - KILL(n))
 *
 * Se itera hasta punto fijo (convergencia).
 */
public class ReachingDefinitionsComputer {

    private final List<CFGNode> nodes;

    private final Map<CFGNode, Set<Definition>> genSets  = new LinkedHashMap<>();
    private final Map<CFGNode, Set<Definition>> killSets = new LinkedHashMap<>();
    private final Map<CFGNode, Set<Definition>> inSets   = new LinkedHashMap<>();
    private final Map<CFGNode, Set<Definition>> outSets  = new LinkedHashMap<>();

    public ReachingDefinitionsComputer(List<CFGNode> nodes) {
        this.nodes = nodes;
    }

    public void compute() {
        // Paso 1: recolectar todas las definiciones del programa
        List<Definition> allDefs = new ArrayList<>();
        for (CFGNode n : nodes) {
            if (n.getDefinedVar() != null) {
                allDefs.add(new Definition(n, n.getDefinedVar()));
            }
        }

        // Paso 2: calcular GEN y KILL para cada nodo
        for (CFGNode n : nodes) {
            Set<Definition> gen  = new HashSet<>();
            Set<Definition> kill = new HashSet<>();

            if (n.getDefinedVar() != null) {
                String v = n.getDefinedVar();
                gen.add(new Definition(n, v));
                // KILL: todas las demas definiciones de la misma variable
                for (Definition d : allDefs) {
                    if (d.getVariable().equals(v) && !d.getNode().equals(n)) {
                        kill.add(d);
                    }
                }
            }

            genSets.put(n, gen);
            killSets.put(n, kill);
        }

        // Paso 3: inicializar OUT(n) = GEN(n), IN(n) = {}
        for (CFGNode n : nodes) {
            outSets.put(n, new HashSet<>(genSets.get(n)));
            inSets.put(n, new HashSet<>());
        }

        // Paso 4: iterar hasta punto fijo
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CFGNode n : nodes) {
                // IN(n) = U OUT(pred(n))
                Set<Definition> newIn = new HashSet<>();
                for (CFGNode.CFGEdge e : n.getPredecessors()) {
                    newIn.addAll(outSets.get(e.getFrom()));
                }

                // OUT(n) = GEN(n) U (IN(n) - KILL(n))
                Set<Definition> inMinusKill = new HashSet<>(newIn);
                inMinusKill.removeAll(killSets.get(n));
                Set<Definition> newOut = new HashSet<>(genSets.get(n));
                newOut.addAll(inMinusKill);

                if (!newOut.equals(outSets.get(n))) {
                    changed = true;
                }

                inSets.put(n, newIn);
                outSets.put(n, newOut);
            }
        }
    }

    public Map<CFGNode, Set<Definition>> getInSets()   { return inSets; }
    public Map<CFGNode, Set<Definition>> getOutSets()  { return outSets; }
    public Map<CFGNode, Set<Definition>> getGenSets()  { return genSets; }
    public Map<CFGNode, Set<Definition>> getKillSets() { return killSets; }

    public void printReachingDefs() {
        System.out.println("\n--- Reaching Definitions ---");

        // Encabezado de la tabla
        System.out.printf("%-20s %-20s %-35s %-35s %-35s %-35s%n",
                "Nodo", "Label", "GEN", "KILL", "IN", "OUT");
        System.out.println("-".repeat(175));

        for (CFGNode n : nodes) {
            String nodeStr  = "n" + n.getId();
            String label    = n.getLabel().isEmpty() ? "(join)" : n.getLabel();
            if (label.length() > 18) label = label.substring(0, 15) + "...";

            System.out.printf("%-20s %-20s %-35s %-35s %-35s %-35s%n",
                    nodeStr,
                    label,
                    formatSet(genSets.get(n)),
                    formatSet(killSets.get(n)),
                    formatSet(inSets.get(n)),
                    formatSet(outSets.get(n)));
        }

        System.out.println("--- Fin Reaching Definitions ---\n");
    }

    private static String formatSet(Set<Definition> set) {
        if (set == null || set.isEmpty()) return "{}";
        List<String> sorted = new ArrayList<>();
        for (Definition d : set) sorted.add(d.toString());
        Collections.sort(sorted);
        return "{" + String.join(", ", sorted) + "}";
    }
}
