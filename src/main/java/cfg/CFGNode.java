package cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Nodo del Control Flow Graph (CFG).
 * Cada nodo representa un statement o punto de control en el programa.
 */
public class CFGNode {

    public enum NodeType {
        ENTRY,      // Punto de entrada del programa
        EXIT,       // Punto de salida del programa
        STATEMENT,  // Sentencia (asignacion, return)
        CONDITION,  // Condicion (if, while)
        JOIN        // Punto de union (merge de branches)
    }

    private int id;
    private String label;
    private NodeType type;
    private List<CFGEdge> successors;
    private List<CFGEdge> predecessors;

    // Para Reaching Definitions: variable definida en este nodo (null si no define ninguna)
    private String definedVar = null;
    // Variables usadas en este nodo (en la expresion del lado derecho o en la condicion)
    private Set<String> usedVars = new HashSet<>();

    public CFGNode(int id, String label, NodeType type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public NodeType getType() { return type; }
    public List<CFGEdge> getSuccessors() { return successors; }
    public List<CFGEdge> getPredecessors() { return predecessors; }

    public String getDefinedVar() { return definedVar; }
    public void setDefinedVar(String definedVar) { this.definedVar = definedVar; }

    public Set<String> getUsedVars() { return usedVars; }
    public void setUsedVars(Set<String> usedVars) { this.usedVars = usedVars; }

    public void addSuccessor(CFGEdge edge) {
        successors.add(edge);
    }

    public void addPredecessor(CFGEdge edge) {
        predecessors.add(edge);
    }

    /**
     * Es un nodo branch (mas de un sucesor)
     */
    public boolean isBranch() {
        return successors.size() > 1;
    }

    /**
     * Es un nodo join (mas de un predecesor)
     */
    public boolean isJoin() {
        return predecessors.size() > 1;
    }

    @Override
    public String toString() {
        return "Node[" + id + ": " + label + " (" + type + ")]";
    }

    // ========== Clase interna para aristas ==========

    /**
     * Arista del CFG que conecta dos nodos.
     * Puede tener un label ("True", "False") para branches condicionales.
     */
    public static class CFGEdge {
        private CFGNode from;
        private CFGNode to;
        private String label; // "True", "False", o "" para aristas normales

        public CFGEdge(CFGNode from, CFGNode to, String label) {
            this.from = from;
            this.to = to;
            this.label = label;
        }

        public CFGNode getFrom() { return from; }
        public CFGNode getTo() { return to; }
        public String getLabel() { return label; }

        @Override
        public String toString() {
            return from.getId() + " --" + (label.isEmpty() ? "" : "[" + label + "]") + "--> " + to.getId();
        }
    }
}
