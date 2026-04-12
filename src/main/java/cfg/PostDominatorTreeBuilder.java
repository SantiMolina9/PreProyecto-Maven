package cfg;

import java.util.*;

/**
 * Construye el Arbol de Post-Dominadores (PDT) a partir de los conjuntos PDOM.
 *
 * Algoritmo BuildDtree (segun rep-analysis-soft.pdf, Figura 11):
 *
 *   Entrada: Conjunto de nodos N, nodo raiz n0 (EXIT para post-dom),
 *            y D(n) = conjunto de post-dominadores de n, para cada nodo n.
 *   Salida:  Arbol de post-dominadores PDT.
 *
 *   1. Sea n0 la raiz de PDT (EXIT)
 *   2. Poner n0 en la cola Q
 *   3. Para cada nodo n: D(n) = D(n) - {n}   (remover el propio nodo)
 *   4. Mientras Q no este vacia:
 *        m = siguiente nodo en Q (remover de Q)
 *        Para cada nodo n en N tal que D(n) no esta vacio:
 *          Si D(n) contiene m:
 *            D(n) = D(n) - {m}
 *            Si D(n) quedo vacio:
 *              Agregar n como hijo de m en PDT
 *              Agregar n a Q
 *
 * El post-dominador inmediato de un nodo n es su padre en el arbol.
 */
public class PostDominatorTreeBuilder {

    private final List<CFGNode> allNodes;
    private final CFGNode exitNode;
    private final Map<CFGNode, Set<CFGNode>> pdom;

    // Resultado: arbol como mapa padre -> lista de hijos
    private final Map<CFGNode, List<CFGNode>> children;
    // Resultado: mapa nodo -> padre (post-dominador inmediato)
    private final Map<CFGNode, CFGNode> parent;

    public PostDominatorTreeBuilder(List<CFGNode> allNodes, CFGNode exitNode,
                                     Map<CFGNode, Set<CFGNode>> pdom) {
        this.allNodes = allNodes;
        this.exitNode = exitNode;
        this.pdom = pdom;
        this.children = new LinkedHashMap<>();
        this.parent = new LinkedHashMap<>();
    }

    /**
     * Ejecuta el algoritmo BuildDtree para construir el arbol de post-dominadores.
     */
    public void build() {
        // Inicializar lista de hijos para todos los nodos
        for (CFGNode node : allNodes) {
            children.put(node, new ArrayList<>());
        }

        // Paso 3: D(n) = PDOM(n) - {n} para cada nodo
        Map<CFGNode, Set<CFGNode>> d = new LinkedHashMap<>();
        for (CFGNode node : allNodes) {
            Set<CFGNode> pdomSet = pdom.get(node);
            if (pdomSet != null) {
                Set<CFGNode> copy = new LinkedHashSet<>(pdomSet);
                copy.remove(node); // remover el propio nodo
                d.put(node, copy);
            } else {
                d.put(node, new LinkedHashSet<>());
            }
        }

        // Pasos 1-2: EXIT es la raiz, ponerlo en la cola
        Queue<CFGNode> queue = new LinkedList<>();
        queue.add(exitNode);

        // Paso 4: procesar la cola
        while (!queue.isEmpty()) {
            CFGNode m = queue.poll();

            for (CFGNode n : allNodes) {
                Set<CFGNode> dn = d.get(n);
                if (dn == null || dn.isEmpty()) continue;

                if (dn.contains(m)) {
                    dn.remove(m);
                    if (dn.isEmpty()) {
                        // n es hijo de m en el arbol
                        children.get(m).add(n);
                        parent.put(n, m);
                        queue.add(n);
                    }
                }
            }
        }
    }

    /**
     * Retorna los hijos de un nodo en el arbol de post-dominadores.
     */
    public List<CFGNode> getChildren(CFGNode node) {
        return children.getOrDefault(node, Collections.emptyList());
    }

    /**
     * Retorna el padre (post-dominador inmediato) de un nodo.
     */
    public CFGNode getParent(CFGNode node) {
        return parent.get(node);
    }

    /**
     * Retorna la raiz del arbol (EXIT).
     */
    public CFGNode getRoot() {
        return exitNode;
    }

    /**
     * Retorna el mapa completo de hijos.
     */
    public Map<CFGNode, List<CFGNode>> getAllChildren() {
        return Collections.unmodifiableMap(children);
    }

    /**
     * Imprime el arbol de post-dominadores por consola.
     */
    public void printTree() {
        System.out.println("\n--- Arbol de Post-Dominadores (PDT) ---");
        System.out.println("Raiz: " + formatNode(exitNode));
        System.out.println();
        printSubtree(exitNode, "", true);
        System.out.println("--- Fin PDT ---\n");
    }

    private void printSubtree(CFGNode node, String prefix, boolean isLast) {
        String connector = isLast ? "+-- " : "|-- ";
        System.out.println(prefix + connector + formatNode(node));

        List<CFGNode> childList = children.get(node);
        if (childList == null) return;

        for (int i = 0; i < childList.size(); i++) {
            String newPrefix = prefix + (isLast ? "    " : "|   ");
            printSubtree(childList.get(i), newPrefix, i == childList.size() - 1);
        }
    }

    private String formatNode(CFGNode node) {
        String label = node.getLabel().isEmpty() ? "(join)" : node.getLabel();
        return "n" + node.getId() + ": " + label + " [" + node.getType() + "]";
    }
}
