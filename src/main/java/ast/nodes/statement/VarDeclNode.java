package ast.nodes.statement;

import ast.ASTNode;
import ast.nodes.expression.ExprNode;
import ast.visitor.ASTVisitor;

/**
 * Nodo que representa la declaración individual de una variable.
 */
public class VarDeclNode extends ASTNode {
    private String name;
    private ExprNode initialValue;

    public VarDeclNode(String name, ExprNode initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    public String getName() { return name; }
    public ExprNode getInitialValue() { return initialValue; }
    public boolean hasInitialValue() { return initialValue != null; }

    @Override
    public String toString() {
        return hasInitialValue() ? name + " = " + initialValue : name;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitVarDecl(this);
    }
}