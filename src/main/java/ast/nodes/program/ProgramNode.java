package ast.nodes.program;

import ast.ASTNode;
import ast.visitor.ASTVisitor;

/**
 * Nodo raíz que representa un programa completo.
 */
public class ProgramNode extends ASTNode {
    private FunctionDefNode mainFunction;

    public ProgramNode(int line, int column, FunctionDefNode mainFunction) {
        super(line, column);
        this.mainFunction = mainFunction;
    }

    public FunctionDefNode getMainFunction() {
        return mainFunction;
    }

    @Override
    public String toString() {
        return "Program(" + mainFunction + ")";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitProgram(this);
    }
}
