package semantic;

import ast.nodes.program.*;
import ast.nodes.statement.*;
import ast.nodes.expression.*;
import ast.visitor.ASTVisitor;
import semantic.symboltable.*;
import semantic.analyzer.*;
import semantic.errors.*;

import java.util.List;

public class SemanticAnalyzer implements ASTVisitor {
    private SymbolTable symbolTable;
    private ErrorHandler errorHandler;
    private ExpressionEvaluator expressionEvaluator;
    private String currentFunctionReturnType;

    public SemanticAnalyzer(ErrorHandler errorHandler) {
        this.symbolTable = new SymbolTable();
        this.errorHandler = errorHandler;
        this.expressionEvaluator = new ExpressionEvaluator(symbolTable, errorHandler);
        this.currentFunctionReturnType = null;
    }

    // Getters
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public boolean hasErrors() {
        return errorHandler.hasErrors();
    }

    // ========== VISITANTES PARA NODOS DEL PROGRAMA ==========

    @Override
    public Object visitProgram(ProgramNode node) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ANALISIS SEMÁNTICO");
        System.out.println("=".repeat(50));

        node.getMainFunction().accept(this);

        /*System.out.println("\n" + "=".repeat(50));
        System.out.println("ANALISIS SEMANTICO COMPLETADO");
        System.out.println("=".repeat(50));*/

        return null;
    }

    @Override
    public Object visitFunctionDef(FunctionDefNode node) {
        String functionName = node.getFunctionName();
        String returnType = node.getReturnType();

        currentFunctionReturnType = returnType;

        System.out.println("\n→ Analizando función: " + functionName + " (" + returnType + ")");

        symbolTable.enterScope("function_" + functionName);

        // Procesar parámetros
        for (ParamNode param : node.getParameters()) {
            param.accept(this);
        }

        // Procesar cuerpo de la función
        for (StmtNode stmt : node.getStatements()) {
            stmt.accept(this);
        }

        // Verificar return statement
        if (!returnType.equals("void") && !hasReturnStatement(node.getStatements())) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "La función '" + functionName + "' con retorno de tipo '" +
                                    returnType + "' debe tener un valor de retorno",
                            "ERROR SEMANTICO")
            );
        }

        symbolTable.exitScope();
        currentFunctionReturnType = null;

        return null;
    }

    @Override
    public Object visitParam(ParamNode node) {
        String paramName = node.getName();
        String paramType = node.getType();

        if (symbolTable.existsLocal(paramName)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "El parámetro '" + paramName + "' ya fue declarado en este entorno",
                            "ERROR SEMANTICO")
            );
        } else {
            Object defaultValue = TypeChecker.getDefaultValue(paramType);
            symbolTable.declare(paramName, paramType, defaultValue, -1, -1);
            System.out.println("  ✓ Parámetro declarado: " + paramName + " : " + paramType);
        }

        return null;
    }

    // ========== VISITANTES PARA SENTENCIAS ==========

    @Override
    public Object visitDeclaration(DeclarationNode node) {
        String type = node.getType();

        for (VarDeclNode varDecl : node.getVariables()) {
            String varName = varDecl.getName();

            if (symbolTable.existsLocal(varName)) {
                errorHandler.addError(
                        new CompilerError(-1, -1,
                                "La variable '" + varName + "' ya fue declarada en este entorno",
                                "ERROR SEMANTICO")
                );
                continue;
            }

            if (varDecl.hasInitialValue()) {
                ExprNode initExpr = varDecl.getInitialValue();
                String initType = (String) initExpr.accept(this);

                if (!TypeChecker.areTypesCompatible(type, initType)) {
                    errorHandler.addError(
                            new CompilerError(-1, -1,
                                    "No se puede asignar " + initType + " a la variable '" +
                                            varName + "' de tipo " + type,
                                    "ERROR SEMANTICO")
                    );
                }

                Object initValue = expressionEvaluator.evaluate(initExpr);
                symbolTable.declare(varName, type, initValue, -1, -1);
                System.out.println("  ✓ Declarada e inicializada: " + varName + " = " + initValue);
            } else {
                symbolTable.declare(varName, type, -1, -1);
                System.out.println("  ✓ Declarada: " + varName + " : " + type);
                errorHandler.addWarning(
                        new CompilerError(-1, -1,
                                "Variable '" + varName + "' declarada pero no inicializada",
                                "ADVERTENCIA")
                );
            }
        }

        return null;
    }

    @Override
    public Object visitVarDecl(VarDeclNode node) {
        // Este método se llama desde visitDeclaration
        return null;
    }

    @Override
    public Object visitAssignment(AssignmentNode node) {
        String varName = node.getVariableName();
        ExprNode expr = node.getExpression();

        SymbolEntry entry = symbolTable.lookup(varName);
        if (entry == null) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "Variable " + varName + " no declarada",
                            "ERROR SEMANTICO")
            );
            return null;
        }

        String exprType = (String) expr.accept(this);
        if (!TypeChecker.areTypesCompatible(entry.getType(), exprType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "No se puede asignar " + exprType + " a la variable '" +
                                    varName + "' de tipo " + entry.getType(),
                            "ERROR SEMANTICO")
            );
        }

        Object value = expressionEvaluator.evaluate(expr);
        symbolTable.assign(varName, value);
        System.out.println("  → Asignación: " + varName + " = " + value);

        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmtNode node) {
        if (node.hasExpression()) {
            String exprType = (String) node.getExpression().accept(this);

            if (currentFunctionReturnType != null &&
                    !TypeChecker.areTypesCompatible(currentFunctionReturnType, exprType)) {
                errorHandler.addError(
                        new CompilerError(-1, -1,
                                "Tipo de retorno incorrecto: Esperado " + currentFunctionReturnType +
                                        ", se obtuvo " + exprType,
                                "ERROR SEMANTICO")
                );
            }

            Object value = expressionEvaluator.evaluate(node.getExpression());
            System.out.println("  ← Retorna: " + value + " (" + exprType + ")");
            return exprType;
        } else {
            if (currentFunctionReturnType != null &&
                    !currentFunctionReturnType.equals("void")) {
                errorHandler.addError(
                        new CompilerError(-1, -1,
                                "La función debe retornar una variable de tipo " + currentFunctionReturnType,
                                "ERROR SEMANTICO")
                );
            }
            System.out.println("  ← Retorna (vacío)");
            return "void";
        }
    }

    @Override
    public Object visitExprStmt(ExprStmtNode node) {
        return node.getExpression().accept(this);
    }

    @Override
    public Object visitIfStmt(IfStmtNode node) {
        // Verificar que la condición es booleana
        String condType = (String) node.getCondition().accept(this);
        if (!"bool".equals(condType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "La condición en el IF debe ser booleana, pero se obtuvo: " + condType,
                            "ERROR SEMANTICO")
            );
        }

        // Analizar rama then
        symbolTable.enterScope("if_then");
        for (StmtNode stmt : node.getThenBranch()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();

        // Analizar rama else si existe
        if (node.hasElseBranch()) {
            symbolTable.enterScope("if_else");
            for (StmtNode stmt : node.getElseBranch()) {
                stmt.accept(this);
            }
            symbolTable.exitScope();
        }

        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmtNode node) {
        // Verificar que la condición es booleana
        String condType = (String) node.getCondition().accept(this);
        if (!"bool".equals(condType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "La condición en el WHILE debe ser booleana, pero se obtuvo: " + condType,
                            "ERROR SEMANTICO")
            );
        }

        // Analizar cuerpo del while
        symbolTable.enterScope("while_body");
        for (StmtNode stmt : node.getBody()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Object visitUnaryOp(UnaryOpNode node) {
        String operandType = (String) node.getOperand().accept(this);

        if (node.getOperator() == UnaryOpNode.Operator.NOT) {
            if (!"bool".equals(operandType)) {
                errorHandler.addError(
                        new CompilerError(-1, -1,
                                "El operador NOT requiere un operador booleano, pero se obtuvo: " + operandType,
                                "ERROR SEMANTICO")
                );
                return "error";
            }
            return "bool";
        }

        return "error";
    }


    // ========== VISITANTES PARA EXPRESIONES ==========

    @Override
    public Object visitBinaryOp(BinaryOpNode node) {
        String leftType = (String) node.getLeft().accept(this);
        String rightType = (String) node.getRight().accept(this);

        if (!TypeChecker.areTypesCompatible(leftType, rightType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "Error de tipo en operación binaria: " + leftType + " y " + rightType,
                            "ERROR SEMANTICO")
            );
            return "error";
        }

        switch (node.getOperator()) {
            case PLUS: case MINUS: case TIMES: case DIVIDE:
                if (!TypeChecker.isArithmeticType(leftType)) {
                    errorHandler.addError(
                            new CompilerError(-1, -1,
                                    "Las operaciones aritméticas requieren tipos numéricos, pero se obtuvo: " + leftType,
                                    "ERROR SEMANTICO")
                    );
                    return "error";
                }
                return "int";

            case LT: case GT:
                if (!TypeChecker.isArithmeticType(leftType)) {
                    errorHandler.addError(
                            new CompilerError(-1, -1,
                                    "Las operaciones de comparación requieren tipos numéricos, pero se obtuvo: " + leftType,
                                    "ERROR SEMANTICO")
                    );
                    return "error";
                }
                return "bool";

            case EQ:
                // EQ funciona para cualquier tipo compatible
                return "bool";

            case AND: case OR:
                if (!"bool".equals(leftType)) {
                    errorHandler.addError(
                            new CompilerError(-1, -1,
                                    "Las operaciones lógicas requieren tipos booleanos, pero se obtuvo: " + leftType,
                                    "ERROR SEMANTICO")
                    );
                    return "error";
                }
                return "bool";

            default:
                return "error";
        }
    }

    @Override
    public Object visitNumber(NumberNode node) {
        return "int";
    }

    @Override
    public Object visitBoolean(BooleanNode node) {
        return "bool";
    }

    @Override
    public Object visitVariable(VariableNode node) {
        String varName = node.getName();
        SymbolEntry entry = symbolTable.lookup(varName);

        if (entry == null) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "La variable '" + varName + "' no fue declarada",
                            "ERROR SEMANTICO")
            );
            return "error";
        }

        if (!entry.isInitialized()) {
            errorHandler.addWarning(
                    new CompilerError(-1, -1,
                            "La variable '" + varName + "' se utiliza antes de ser inicializada",
                            "ADVERTENCIA")
            );
        }

        return entry.getType();
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Verifica si una lista de sentencias contiene un return statement
     */
    private boolean hasReturnStatement(List<StmtNode> statements) {
        for (StmtNode stmt : statements) {
            if (stmt instanceof ReturnStmtNode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Imprime un resumen del análisis semántico
     */
    public void printSummary() {
        /*System.out.println("\n" + "=".repeat(50));
        System.out.println("RESUMEN DEL ANALISIS SEMANTICO");
        System.out.println("=".repeat(50));*/

        //symbolTable.printSymbolTable();
        symbolTable.printStatistics();

        errorHandler.printSummary();
    }

    /**
     * Reinicia el analizador para un nuevo análisis
     */
    public void reset() {
        symbolTable.clear();
        errorHandler.reset();
        currentFunctionReturnType = null;
    }
}
