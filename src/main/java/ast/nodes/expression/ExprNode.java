package ast.nodes.expression;

import ast.ASTNode;

/**
 * Clase base abstracta para todos los nodos de expresiones.
 */
public abstract class ExprNode extends ASTNode {
    public ExprNode(int line, int column) {
        super(line, column);
    }
}
