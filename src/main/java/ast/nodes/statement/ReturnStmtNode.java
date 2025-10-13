package ast.nodes.statement;

import ast.nodes.expression.ExprNode;
import ast.visitor.ASTVisitor;

/**
 * Nodo que representa una sentencia return.
 */
public class ReturnStmtNode extends StmtNode {
    private ExprNode expression;

    public ReturnStmtNode(ExprNode expression) {
        this.expression = expression;
    }

    public ExprNode getExpression() { return expression; }
    public boolean hasExpression() { return expression != null; }

    @Override
    public String toString() {
        return hasExpression() ? "Return(" + expression + ")" : "Return()";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitReturnStmt(this);
    }
}