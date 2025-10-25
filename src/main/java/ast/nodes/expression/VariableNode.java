package ast.nodes.expression;

import ast.visitor.ASTVisitor;

/**
 * Nodo que representa una referencia a una variable.
 */
public class VariableNode extends ExprNode {
    private String name;

    public VariableNode(int line, int column, String name) {
        super(line, column);
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitVariable(this);
    }
}
