package ast.nodes.statement;

import ast.ASTNode;

/**
 * Clase base abstracta para todos los nodos de sentencias.
 */
public abstract class StmtNode extends ASTNode {
    public StmtNode(int line, int column) {
        super(line, column);
    }
    // Clase base para todas las sentencias
}
