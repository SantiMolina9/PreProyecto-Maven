package ast.nodes.expression;

public class UnaryOpNode extends ExprNode {
    public enum Operator {
        NOT
    }

    private Operator operator;
    private ExprNode operand;

    public UnaryOpNode(Operator operator, ExprNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    // Getters
    public Operator getOperator() { return operator; }
    public ExprNode getOperand() { return operand; }

    @Override
    public String toString() {
        return "UnaryOp{" + operator + " " + operand + "}";
    }

    @Override
    public Object accept(ast.visitor.ASTVisitor visitor) {
        return visitor.visitUnaryOp(this);
    }
}