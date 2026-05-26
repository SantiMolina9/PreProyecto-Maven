package cfg;

import ast.nodes.expression.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Extrae el conjunto de variables referenciadas en una expresion AST.
 * Usado para calcular los conjuntos usedVars de cada nodo CFG.
 */
public class VariableExtractor {

    public static Set<String> extract(ExprNode expr) {
        Set<String> vars = new HashSet<>();
        collect(expr, vars);
        return vars;
    }

    private static void collect(ExprNode expr, Set<String> vars) {
        if (expr == null) return;
        if (expr instanceof VariableNode) {
            vars.add(((VariableNode) expr).getName());
        } else if (expr instanceof BinaryOpNode) {
            collect(((BinaryOpNode) expr).getLeft(), vars);
            collect(((BinaryOpNode) expr).getRight(), vars);
        } else if (expr instanceof UnaryOpNode) {
            collect(((UnaryOpNode) expr).getOperand(), vars);
        }
        // NumberNode y BooleanNode no referencian variables
    }
}
