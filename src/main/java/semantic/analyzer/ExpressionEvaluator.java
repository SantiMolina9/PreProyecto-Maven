package semantic.analyzer;

import ast.nodes.expression.*;
import semantic.errors.CompilerError;
import semantic.symboltable.SymbolTable;
import semantic.errors.ErrorHandler;

/**
 * Evaluador de expresiones en tiempo de compilación.
 * Calcula valores constantes cuando es posible (constant folding).
 */
public class ExpressionEvaluator {
    private SymbolTable symbolTable;
    private ErrorHandler errorHandler;

    public ExpressionEvaluator(SymbolTable symbolTable, ErrorHandler errorHandler) {
        this.symbolTable = symbolTable;
        this.errorHandler = errorHandler;
    }

    /**
     * Evalúa una expresión y retorna su valor
     * @return El valor evaluado, o null si no se puede evaluar
     */
    public Object evaluate(ExprNode expr) {
        if (expr == null) {
            return null;
        }

        if (expr instanceof NumberNode) {
            return evaluateNumber((NumberNode) expr);
        } else if (expr instanceof BooleanNode) {
            return evaluateBoolean((BooleanNode) expr);
        } else if (expr instanceof VariableNode) {
            return evaluateVariable((VariableNode) expr);
        } else if (expr instanceof BinaryOpNode) {
            return evaluateBinaryOp((BinaryOpNode) expr);
        }

        return null;
    }

    /**
     * Evalúa un nodo numérico
     */
    private Object evaluateNumber(NumberNode node) {
        return node.getValue();
    }

    /**
     * Evalúa un nodo booleano
     */
    private Object evaluateBoolean(BooleanNode node) {
        return node.getValue();
    }

    /**
     * Evalúa una variable obteniendo su valor de la tabla de símbolos
     */
    private Object evaluateVariable(VariableNode node) {
        String varName = node.getName();
        return symbolTable.getValue(varName);
    }

    /**
     * Evalúa una operación binaria
     */
    private Object evaluateBinaryOp(BinaryOpNode node) {
        Object leftVal = evaluate(node.getLeft());
        Object rightVal = evaluate(node.getRight());

        if (leftVal == null || rightVal == null) {
            return null;
        }

        // Solo soportamos operaciones aritméticas entre enteros por ahora
        if (!(leftVal instanceof Integer) || !(rightVal instanceof Integer)) {
            return null;
        }

        int left = (Integer) leftVal;
        int right = (Integer) rightVal;

        switch (node.getOperator()) {
            case PLUS:
                return left + right;

            case MINUS:
                return left - right;

            case TIMES:
                return left * right;

            case DIVIDE:
                if (right == 0) {
                    errorHandler.addError(
                            new CompilerError(
                                    -1, -1, "Division by zero in constant expression", "ERROR SEMANTICO"
                            )
                    );
                    return 0;
                }
                return left / right;

            default:
                return null;
        }
    }

    /**
     * Verifica si una expresión es constante (puede evaluarse en tiempo de compilación)
     */
    public boolean isConstantExpression(ExprNode expr) {
        if (expr instanceof NumberNode || expr instanceof BooleanNode) {
            return true;
        }

        if (expr instanceof BinaryOpNode) {
            BinaryOpNode binOp = (BinaryOpNode) expr;
            return isConstantExpression(binOp.getLeft()) &&
                    isConstantExpression(binOp.getRight());
        }

        return false;
    }

    /**
     * Intenta realizar constant folding en una expresión
     * @return Una expresión simplificada, o la original si no se puede simplificar
     */
    public ExprNode constantFold(ExprNode expr) {
        if (isConstantExpression(expr)) {
            Object value = evaluate(expr);
            if (value instanceof Integer) {
                return new NumberNode((Integer) value);
            } else if (value instanceof Boolean) {
                return new BooleanNode((Boolean) value);
            }
        }
        return expr;
    }
}