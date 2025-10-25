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
     * @param line Línea donde aparece la declaración
     * @param column Columna donde aparece la declaración
     * @return Lista de nodos VarDeclNode
     */
    public static List<VarDeclNode> createVarDeclList(String varListStr, int line, int column) {
        List<VarDeclNode> variables = new ArrayList<>();
        String[] parts = varListStr.split(",");

        for (String part : parts) {
            part = part.trim();
            if (part.contains("=")) {
                // Variable con inicialización
                String[] assignment = part.split("=", 2);
                String varName = assignment[0].trim();
                String initValue = assignment[1].trim();
                ExprNode initExpr = parseSimpleExpression(initValue, line, column);
                variables.add(new VarDeclNode(line, column, varName, initExpr));
            } else {
                // Variable sin inicialización
                variables.add(new VarDeclNode(line, column, part, null));
            }
        }

        return variables;
    }

    /**
     * Versión con valores por defecto para compatibilidad (usar solo en testing)
     * @deprecated Usar createVarDeclList(String, int, int) para incluir información de posición
     */
    @Deprecated
    public static List<VarDeclNode> createVarDeclList(String varListStr) {
        return createVarDeclList(varListStr, -1, -1);
    }

    /**
     * Método auxiliar simple para parsear expresiones básicas.
     * Solo maneja literales simples: números, booleanos y variables.
     *
     * @param expr Expresión como cadena
     * @param line Línea donde aparece la expresión
     * @param column Columna donde aparece la expresión
     * @return Nodo de expresión correspondiente, o null si no se puede parsear
     */
    private static ExprNode parseSimpleExpression(String expr, int line, int column) {
        expr = expr.trim();

        // Booleano true
        if (expr.equals("true")) {
            return new BooleanNode(line, column, true);
        }
        // Booleano false
        else if (expr.equals("false")) {
            return new BooleanNode(line, column, false);
        }
        // Número entero
        else if (expr.matches("\\d+")) {
            return new NumberNode(line, column, Integer.parseInt(expr));
        }
        // Identificador (variable)
        else if (expr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return new VariableNode(line, column, expr);
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
            case "&&": return BinaryOpNode.Operator.AND;
            case "||": return BinaryOpNode.Operator.OR;
            case "==": return BinaryOpNode.Operator.EQ;
            case "<": return BinaryOpNode.Operator.LT;
            case ">": return BinaryOpNode.Operator.GT;
            default: throw new IllegalArgumentException("Operador inválido: " + op);
        }
    }

    /**
     * Método de utilidad para construir fácilmente un BinaryOpNode.
     *
     * @param left Expresión izquierda
     * @param op Operador como string
     * @param right Expresión derecha
     * @param line Línea donde aparece la operación
     * @param column Columna donde aparece la operación
     * @return Nodo de operación binaria
     */
    public static BinaryOpNode createBinaryOp(ExprNode left, String op, ExprNode right, int line, int column) {
        return new BinaryOpNode(line, column, left, stringToOperator(op), right);
    }

    /**
     * Versión con valores por defecto para compatibilidad (usar solo en testing)
     * @deprecated Usar createBinaryOp(ExprNode, String, ExprNode, int, int) para incluir información de posición
     */
    @Deprecated
    public static BinaryOpNode createBinaryOp(ExprNode left, String op, ExprNode right) {
        return createBinaryOp(left, op, right, -1, -1);
    }

    /**
     * Crea un nodo de número con información de posición.
     *
     * @param value Valor numérico
     * @param line Línea donde aparece el número
     * @param column Columna donde aparece el número
     * @return Nodo NumberNode
     */
    public static NumberNode createNumber(int value, int line, int column) {
        return new NumberNode(line, column, value);
    }

    /**
     * Crea un nodo booleano con información de posición.
     *
     * @param value Valor booleano
     * @param line Línea donde aparece el booleano
     * @param column Columna donde aparece el booleano
     * @return Nodo BooleanNode
     */
    public static BooleanNode createBoolean(boolean value, int line, int column) {
        return new BooleanNode(line, column, value);
    }

    /**
     * Crea un nodo de variable con información de posición.
     *
     * @param name Nombre de la variable
     * @param line Línea donde aparece la variable
     * @param column Columna donde aparece la variable
     * @return Nodo VariableNode
     */
    public static VariableNode createVariable(String name, int line, int column) {
        return new VariableNode(line, column, name);
    }

    /**
     * Crea un nodo de declaración de variable sin inicialización.
     *
     * @param name Nombre de la variable
     * @param line Línea donde aparece la declaración
     * @param column Columna donde aparece la declaración
     * @return Nodo VarDeclNode
     */
    public static VarDeclNode createVarDecl(String name, int line, int column) {
        return new VarDeclNode(line, column, name, null);
    }

    /**
     * Crea un nodo de declaración de variable con inicialización.
     *
     * @param name Nombre de la variable
     * @param initValue Expresión de inicialización
     * @param line Línea donde aparece la declaración
     * @param column Columna donde aparece la declaración
     * @return Nodo VarDeclNode
     */
    public static VarDeclNode createVarDecl(String name, ExprNode initValue, int line, int column) {
        return new VarDeclNode(line, column, name, initValue);
    }
}
