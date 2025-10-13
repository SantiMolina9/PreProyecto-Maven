package ast.nodes.statement;

import ast.nodes.expression.ExprNode;
import ast.visitor.ASTVisitor;

/**
 * Nodo que representa una expresión como sentencia.
 */
public class ExprStmtNode extends StmtNode {
    private ExprNode expression;

    public ExprStmtNode(ExprNode expression) {
        this.expression = expression;
    }

    public ExprNode getExpression() { return expression; }

    @Override
    public String toString() {
        return "ExprStmt(" + expression + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitExprStmt(this);
    }
}