package ast.nodes.statement;

import ast.nodes.expression.ExprNode;
import java.util.List;

public class WhileStmtNode extends StmtNode {
    private ExprNode condition;
    private List<StmtNode> body;

    public WhileStmtNode(ExprNode condition, List<StmtNode> body) {
        this.condition = condition;
        this.body = body;
    }

    public ExprNode getCondition() { return condition; }
    public List<StmtNode> getBody() { return body; }

    @Override
    public String toString() {
        return "WhileStmt{condition=" + condition + ", body=" + body + "}";
    }

    @Override
    public Object accept(ast.visitor.ASTVisitor visitor) {
        return visitor.visitWhileStmt(this);
    }
}