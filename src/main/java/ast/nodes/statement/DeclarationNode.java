package ast.nodes.statement;

import ast.visitor.ASTVisitor;
import java.util.List;

/**
 * Nodo que representa una declaración de variable(s).
 */
public class DeclarationNode extends StmtNode {
    private String type;
    private List<VarDeclNode> variables;

    public DeclarationNode(int line, int column, String type, List<VarDeclNode> variables) {
        super(line, column);
        this.type = type;
        this.variables = variables;
    }

    public String getType() { return type; }
    public List<VarDeclNode> getVariables() { return variables; }

    @Override
    public String toString() {
        return "Declaration(" + type + " " + variables + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitDeclaration(this);
    }
}
