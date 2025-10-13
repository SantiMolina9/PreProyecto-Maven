package ast.nodes.expression;

import ast.visitor.ASTVisitor;

/**
 * Nodo que representa un literal booleano.
 */
public class BooleanNode extends ExprNode {
    private boolean value;

    public BooleanNode(boolean value) {
        this.value = value;
    }

    public boolean getValue() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitBoolean(this);
    }
}
