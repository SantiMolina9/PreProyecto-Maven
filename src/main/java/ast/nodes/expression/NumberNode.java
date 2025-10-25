package ast.nodes.expression;

import ast.visitor.ASTVisitor;

/**
 * Nodo que representa un literal numérico.
 */
public class NumberNode extends ExprNode {
    private int value;

    public NumberNode(int line, int column, int value) {
        super(line, column);
        this.value = value;
    }

    public int getValue() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitNumber(this);
    }
}
