package ast;

import ast.visitor.ASTVisitor;

/**
 * Clase base abstracta para todos los nodos del Árbol Sintáctico Abstracto (AST).
 * Implementa el patrón Visitor para facilitar el recorrido y procesamiento del árbol.
 */
public abstract class ASTNode {
    protected int line;
    protected int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() { return line; }

    public int getColumn() { return column; }

    /**
     * Representación en cadena del nodo.
     * @return String que representa el nodo
     */
    public abstract String toString();

    /**
     * Método para aceptar visitantes (patrón Visitor).
     * @param visitor El visitante que procesará este nodo
     * @return El resultado del procesamiento del visitante
     */
    public abstract Object accept(ASTVisitor visitor);
}
