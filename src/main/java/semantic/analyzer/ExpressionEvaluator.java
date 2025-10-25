package semantic.analyzer;

import ast.nodes.expression.*;
import semantic.errors.ErrorHandler;
import semantic.symboltable.SymbolTable;

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
        } else if (expr instanceof UnaryOpNode) {
            return evaluateUnaryOp((UnaryOpNode) expr);
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
        Object value = symbolTable.getValue(varName);

        // Si la variable no tiene valor o no está inicializada
        if (value == null) {
            errorHandler.addWarning(
                    node.getLine(),
                    node.getColumn(),
                    "Variable '" + varName + "' usada en expresión constante pero no tiene valor asignado"
            );
        }

        return value;
    }

    /**
     * Evalúa una operación unaria
     */
    private Object evaluateUnaryOp(UnaryOpNode node) {
        Object operandVal = evaluate(node.getOperand());

        if (operandVal == null) {
            return null;
        }

        switch (node.getOperator()) {
            case NOT:
                if (operandVal instanceof Boolean) {
                    return !(Boolean) operandVal;
                }
                errorHandler.addTypeError(
                        node.getLine(),
                        node.getColumn(),
                        "El operador NOT requiere un operando booleano"
                );
                return null;

            default:
                return null;
        }
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

        // Operaciones aritméticas
        if (node.getOperator() == BinaryOpNode.Operator.PLUS ||
                node.getOperator() == BinaryOpNode.Operator.MINUS ||
                node.getOperator() == BinaryOpNode.Operator.TIMES ||
                node.getOperator() == BinaryOpNode.Operator.DIVIDE) {

            return evaluateArithmeticOp(node, leftVal, rightVal);
        }

        // Operaciones de comparación
        if (node.getOperator() == BinaryOpNode.Operator.LT ||
                node.getOperator() == BinaryOpNode.Operator.GT ||
                node.getOperator() == BinaryOpNode.Operator.EQ) {

            return evaluateComparisonOp(node, leftVal, rightVal);
        }

        // Operaciones lógicas
        if (node.getOperator() == BinaryOpNode.Operator.AND ||
                node.getOperator() == BinaryOpNode.Operator.OR) {

            return evaluateLogicalOp(node, leftVal, rightVal);
        }

        return null;
    }

    /**
     * Evalúa operaciones aritméticas
     */
    private Object evaluateArithmeticOp(BinaryOpNode node, Object leftVal, Object rightVal) {
        if (!(leftVal instanceof Integer) || !(rightVal instanceof Integer)) {
            errorHandler.addTypeError(
                    node.getLine(),
                    node.getColumn(),
                    "Las operaciones aritméticas requieren operandos enteros"
            );
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
                    errorHandler.addSemanticError(
                            node.getRight().getLine(),
                            node.getRight().getColumn(),
                            "División por cero en expresión constante"
                    );
                    return 0;
                }
                return left / right;

            default:
                return null;
        }
    }

    /**
     * Evalúa operaciones de comparación
     */
    private Object evaluateComparisonOp(BinaryOpNode node, Object leftVal, Object rightVal) {
        switch (node.getOperator()) {
            case LT:
                if (leftVal instanceof Integer && rightVal instanceof Integer) {
                    return (Integer) leftVal < (Integer) rightVal;
                }
                break;

            case GT:
                if (leftVal instanceof Integer && rightVal instanceof Integer) {
                    return (Integer) leftVal > (Integer) rightVal;
                }
                break;

            case EQ:
                // EQ funciona para cualquier tipo
                return leftVal.equals(rightVal);

            default:
                return null;
        }

        errorHandler.addTypeError(
                node.getLine(),
                node.getColumn(),
                "Tipos incompatibles en operación de comparación"
        );
        return null;
    }

    /**
     * Evalúa operaciones lógicas
     */
    private Object evaluateLogicalOp(BinaryOpNode node, Object leftVal, Object rightVal) {
        if (!(leftVal instanceof Boolean) || !(rightVal instanceof Boolean)) {
            errorHandler.addTypeError(
                    node.getLine(),
                    node.getColumn(),
                    "Las operaciones lógicas requieren operandos booleanos"
            );
            return null;
        }

        boolean left = (Boolean) leftVal;
        boolean right = (Boolean) rightVal;

        switch (node.getOperator()) {
            case AND:
                return left && right;

            case OR:
                return left || right;

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

        if (expr instanceof UnaryOpNode) {
            UnaryOpNode unaryOp = (UnaryOpNode) expr;
            return isConstantExpression(unaryOp.getOperand());
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
                // Preservar la información de posición del nodo original
                return new NumberNode(expr.getLine(), expr.getColumn(), (Integer) value);
            } else if (value instanceof Boolean) {
                // Preservar la información de posición del nodo original
                return new BooleanNode(expr.getLine(), expr.getColumn(), (Boolean) value);
            }
        }
        return expr;
    }

    /**
     * Evalúa una expresión booleana para condiciones de control de flujo
     * @return true si la condición es verdadera, false en caso contrario
     */
    public boolean evaluateCondition(ExprNode expr) {
        Object value = evaluate(expr);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        errorHandler.addTypeError(
                expr.getLine(),
                expr.getColumn(),
                "La condición debe evaluar a un valor booleano"
        );

        return false;
    }

    /**
     * Calcula el tipo resultante de una expresión
     * @return "int", "bool", o "error" si hay un problema
     */
    public String getExpressionType(ExprNode expr) {
        if (expr instanceof NumberNode) {
            return "int";
        } else if (expr instanceof BooleanNode) {
            return "bool";
        } else if (expr instanceof VariableNode) {
            String varName = ((VariableNode) expr).getName();
            var entry = symbolTable.lookup(varName);
            return entry != null ? entry.getType() : "error";
        } else if (expr instanceof BinaryOpNode) {
            BinaryOpNode binOp = (BinaryOpNode) expr;
            switch (binOp.getOperator()) {
                case PLUS:
                case MINUS:
                case TIMES:
                case DIVIDE:
                    return "int";
                case LT:
                case GT:
                case EQ:
                case AND:
                case OR:
                    return "bool";
                default:
                    return "error";
            }
        } else if (expr instanceof UnaryOpNode) {
            UnaryOpNode unaryOp = (UnaryOpNode) expr;
            if (unaryOp.getOperator() == UnaryOpNode.Operator.NOT) {
                return "bool";
            }
        }

        return "error";
    }
}
