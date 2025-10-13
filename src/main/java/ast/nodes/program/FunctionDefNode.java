package ast.nodes.program;

import ast.ASTNode;
import ast.nodes.statement.StmtNode;
import ast.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * Nodo que representa la definición de una función.
 */
public class FunctionDefNode extends ASTNode {
    private String returnType;
    private String functionName;
    private List<ParamNode> parameters;
    private List<StmtNode> statements;

    public FunctionDefNode(String returnType, String functionName,
                           List<ParamNode> parameters, List<StmtNode> statements) {
        this.returnType = returnType;
        this.functionName = functionName;
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        this.statements = statements;
    }

    public String getReturnType() { return returnType; }
    public String getFunctionName() { return functionName; }
    public List<ParamNode> getParameters() { return parameters; }
    public List<StmtNode> getStatements() { return statements; }

    @Override
    public String toString() {
        return "FunctionDef(" + returnType + " " + functionName +
                "(" + parameters + ") {" + statements + "})";
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitFunctionDef(this);
    }
}
