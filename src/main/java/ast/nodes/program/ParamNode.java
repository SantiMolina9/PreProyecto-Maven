package ast.nodes.program;

import ast.ASTNode;
import ast.visitor.ASTVisitor;

/**
 * Nodo que representa un parámetro de función.
 */
public class ParamNode extends ASTNode {
    private String type;
    private String name;

    public ParamNode(int line, int column, String type, String name) {
        super(line, column);
        this.type = type;
        this.name = name;
    }

    public String getType() { return type; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return type + " " + name;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitParam(this);
    }
}
