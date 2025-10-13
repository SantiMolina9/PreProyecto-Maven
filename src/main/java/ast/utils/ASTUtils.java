package ast.utils;

import ast.nodes.statement.VarDeclNode;
import ast.nodes.expression.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase con métodos utilitarios para trabajar con el AST.
 */
public class ASTUtils {

    /**
     * Crea una lista de declaraciones de variables a partir de una cadena.
     * Formato esperado: "var1, var2 = 5, var3"
     *
     * @param varListStr Cadena con las declaraciones separadas por comas
     * @return Lista de nodos VarDeclNode
     */
    public static List<VarDeclNode> createVarDeclList(String varListStr) {
        List<VarDeclNode> variables = new ArrayList<>();
        String[] parts = varListStr.split(",");

        for (String part : parts) {
            part = part.trim();
            if (part.contains("=")) {
                // Variable con inicialización
                String[] assignment = part.split("=", 2);
                String varName = assignment[0].trim();
                String initValue = assignment[1].trim();
                ExprNode initExpr = parseSimpleExpression(initValue);
                variables.add(new VarDeclNode(varName, initExpr));
            } else {
                // Variable sin inicialización
                variables.add(new VarDeclNode(part, null));
            }
        }

        return variables;
    }

    /**
     * Método auxiliar simple para parsear expresiones básicas.
     * Solo maneja literales simples: números, booleanos y variables.
     *
     * @param expr Expresión como cadena
     * @return Nodo de expresión correspondiente, o null si no se puede parsear
     */
    private static ExprNode parseSimpleExpression(String expr) {
        expr = expr.trim();

        // Booleano true
        if (expr.equals("true")) {
            return new BooleanNode(true);
        }
        // Booleano false
        else if (expr.equals("false")) {
            return new BooleanNode(false);
        }
        // Número entero
        else if (expr.matches("\\d+")) {
            return new NumberNode(Integer.parseInt(expr));
        }
        // Identificador (variable)
        else if (expr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return new VariableNode(expr);
        }

        // Para expresiones más complejas, retorna null
        return null;
    }

    /**
     * Convierte un string de operador a un Operator enum.
     *
     * @param op Operador como string ("+", "-", "*", "/")
     * @return El operador correspondiente
     * @throws IllegalArgumentException si el operador no es válido
     */
    public static BinaryOpNode.Operator stringToOperator(String op) {
        switch (op) {
            case "+": return BinaryOpNode.Operator.PLUS;
            case "-": return BinaryOpNode.Operator.MINUS;
            case "*": return BinaryOpNode.Operator.TIMES;
            case "/": return BinaryOpNode.Operator.DIVIDE;
            default: throw new IllegalArgumentException("Operador inválido: " + op);
        }
    }

    /**
     * Método de utilidad para construir fácilmente un BinaryOpNode.
     *
     * @param left Expresión izquierda
     * @param op Operador como string
     * @param right Expresión derecha
     * @return Nodo de operación binaria
     */
    public static BinaryOpNode createBinaryOp(ExprNode left, String op, ExprNode right) {
        return new BinaryOpNode(left, stringToOperator(op), right);
    }
}