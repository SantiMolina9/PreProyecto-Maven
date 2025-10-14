package semantic;

import ast.nodes.program.*;
import ast.nodes.statement.*;
import ast.nodes.expression.*;
import ast.visitor.ASTVisitor;
import semantic.symboltable.*;
import semantic.analyzer.*;
import semantic.errors.*;

import java.util.List;

/**
 * Analizador semántico principal que implementa el patrón Visitor.
 * Coordina el análisis semántico usando componentes especializados:
 * - SymbolTable para gestión de símbolos
 * - TypeChecker para verificación de tipos
 * - ExpressionEvaluator para evaluación de expresiones
 * - ErrorHandler para gestión de errores y warnings
 */
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
        System.out.println("INICIO DEL ANALISIS SEMANTICO");
        System.out.println("=".repeat(50));

        node.getMainFunction().accept(this);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("ANALISIS SEMANTICO COMPLETADO");
        System.out.println("=".repeat(50));

        return null;
    }

    @Override
    public Object visitFunctionDef(FunctionDefNode node) {
        String functionName = node.getFunctionName();
        String returnType = node.getReturnType();

        currentFunctionReturnType = returnType;

        System.out.println("\n→ Analyzing function: " + functionName + " (" + returnType + ")");

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
                            "Function '" + functionName + "' with return type '" +
                                    returnType + "' must have a return statement",
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
                            "Parameter '" + paramName + "' is already declared in this scope",
                            "ERROR SEMANTICO")
            );
        } else {
            Object defaultValue = TypeChecker.getDefaultValue(paramType);
            symbolTable.declare(paramName, paramType, defaultValue, -1, -1);
            System.out.println("  ✓ Declared parameter: " + paramName + " : " + paramType);
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
                                "Variable '" + varName + "' is already declared in this scope",
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
                                    "Cannot assign " + initType + " to variable '" +
                                            varName + "' of type " + type,
                                    "ERROR SEMANTICO")
                    );
                }

                Object initValue = expressionEvaluator.evaluate(initExpr);
                symbolTable.declare(varName, type, initValue, -1, -1);
                System.out.println("  ✓ Declared and initialized: " + varName + " = " + initValue);
            } else {
                symbolTable.declare(varName, type, -1, -1);
                System.out.println("  ✓ Declared: " + varName + " : " + type);
                errorHandler.addWarning(
                        new CompilerError(-1, -1,
                                "Variable '" + varName + "' declared but not initialized",
                                "WARNING")
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
                            "Variable '" + varName + "' is not declared",
                            "ERROR SEMANTICO")
            );
            return null;
        }

        String exprType = (String) expr.accept(this);
        if (!TypeChecker.areTypesCompatible(entry.getType(), exprType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "Cannot assign " + exprType + " to variable '" +
                                    varName + "' of type " + entry.getType(),
                            "ERROR SEMANTICO")
            );
        }

        Object value = expressionEvaluator.evaluate(expr);
        symbolTable.assign(varName, value);
        System.out.println("  → Assignment: " + varName + " = " + value);

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
                                "Return type mismatch: expected " + currentFunctionReturnType +
                                        ", got " + exprType,
                                "ERROR SEMANTICO")
                );
            }

            Object value = expressionEvaluator.evaluate(node.getExpression());
            System.out.println("  ← Return: " + value + " (" + exprType + ")");
            return exprType;
        } else {
            if (currentFunctionReturnType != null &&
                    !currentFunctionReturnType.equals("void")) {
                errorHandler.addError(
                        new CompilerError(-1, -1,
                                "Function must return a value of type " + currentFunctionReturnType,
                                "ERROR SEMANTICO")
                );
            }
            System.out.println("  ← Return (void)");
            return "void";
        }
    }

    @Override
    public Object visitExprStmt(ExprStmtNode node) {
        return node.getExpression().accept(this);
    }

    // ========== VISITANTES PARA EXPRESIONES ==========

    @Override
    public Object visitBinaryOp(BinaryOpNode node) {
        String leftType = (String) node.getLeft().accept(this);
        String rightType = (String) node.getRight().accept(this);

        if (!TypeChecker.areTypesCompatible(leftType, rightType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "Type mismatch in binary operation: " + leftType + " and " + rightType,
                            "ERROR SEMANTICO")
            );
            return "error";
        }

        if (!TypeChecker.isArithmeticType(leftType)) {
            errorHandler.addError(
                    new CompilerError(-1, -1,
                            "Arithmetic operations are only supported for int type, got: " + leftType,
                            "ERROR SEMANTICO")
            );
            return "error";
        }

        return "int";
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
                            "Variable '" + varName + "' is not declared",
                            "ERROR SEMANTICO")
            );
            return "error";
        }

        if (!entry.isInitialized()) {
            errorHandler.addWarning(
                    new CompilerError(-1, -1,
                            "Variable '" + varName + "' is used before being initialized",
                            "WARNING")
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
        System.out.println("\n" + "=".repeat(50));
        System.out.println("RESUMEN DEL ANALISIS SEMANTICO");
        System.out.println("=".repeat(50));

        symbolTable.printSymbolTable();
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