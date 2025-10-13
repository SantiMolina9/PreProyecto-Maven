package ast.nodes.expression;

import ast.visitor.ASTVisitor;

/**
 * Nodo que representa una operación binaria.
 */
public class BinaryOpNode extends ExprNode {
    public enum Operator {
        PLUS, MINUS, TIMES, DIVIDE
    }

    private ExprNode left;
    private Operator operator;
    private ExprNode right;

    public BinaryOpNode(ExprNode left, Operator operator, ExprNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExprNode getLeft() { return left; }
    public Operator getOperator() { return operator; }
    public ExprNode getRight() { return right; }

    @Override
    public String toString() {
        String op = "";
        switch (operator) {
            case PLUS: op = "+"; break;
            case MINUS: op = "-"; break;
            case TIMES: op = "*"; break;
            case DIVIDE: op = "/"; break;
        }
        return "(" + left + " " + op + " " + right + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitBinaryOp(this);
    }
}
