package cfg;

import java.util.Objects;

/**
 * Representa una definicion alcanzante: el nodo del CFG que define una variable.
 * Se denota como "n:var" (ej. "n2:x" significa que el nodo 2 define la variable x).
 */
public class Definition {

    private final CFGNode node;
    private final String variable;

    public Definition(CFGNode node, String variable) {
        this.node = node;
        this.variable = variable;
    }

    public CFGNode getNode() { return node; }
    public String getVariable() { return variable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Definition)) return false;
        Definition d = (Definition) o;
        return Objects.equals(node, d.node) && Objects.equals(variable, d.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, variable);
    }

    @Override
    public String toString() {
        return "n" + node.getId() + ":" + variable;
    }
}
