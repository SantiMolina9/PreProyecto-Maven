package ast.nodes.statement;

import ast.nodes.expression.ExprNode;
import java.util.List;

public class IfStmtNode extends StmtNode {
    private ExprNode condition;
    private List<StmtNode> thenBranch;
    private List<StmtNode> elseBranch;

    public IfStmtNode(int line, int column, ExprNode condition, List<StmtNode> thenBranch, List<StmtNode> elseBranch) {
        super(line, column);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ExprNode getCondition() { return condition; }
    public List<StmtNode> getThenBranch() { return thenBranch; }
    public List<StmtNode> getElseBranch() { return elseBranch; }
    public boolean hasElseBranch() { return elseBranch != null && !elseBranch.isEmpty(); }

    @Override
    public String toString() {
        return "IfStmt{condition=" + condition +
                ", then=" + thenBranch +
                ", else=" + elseBranch + "}";
    }

    @Override
    public Object accept(ast.visitor.ASTVisitor visitor) {
        return visitor.visitIfStmt(this);
    }
}
