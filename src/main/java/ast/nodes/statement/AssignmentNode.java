package ast.nodes.statement;

import ast.nodes.expression.ExprNode;
import ast.visitor.ASTVisitor;

/**
 * Nodo que representa una asignación de valor a una variable.
 */
public class AssignmentNode extends StmtNode {
    private String variableName;
    private ExprNode expression;

    public AssignmentNode(int line, int column, String variableName, ExprNode expression) {
        super(line, column);
        this.variableName = variableName;
        this.expression = expression;
    }

    public String getVariableName() { return variableName; }
    public ExprNode getExpression() { return expression; }

    @Override
    public String toString() {
        return "Asignación(" + variableName + " = " + expression + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitAssignment(this);
    }
}
