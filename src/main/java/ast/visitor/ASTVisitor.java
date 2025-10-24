package ast.visitor;

import ast.nodes.program.*;
import ast.nodes.statement.*;
import ast.nodes.expression.*;

/**
 * Interface para implementar el patrón Visitor sobre el AST.
 * Permite separar los algoritmos de la estructura de datos del árbol.
 */
public interface ASTVisitor {
    // Nodos de programa
    Object visitProgram(ProgramNode node);
    Object visitFunctionDef(FunctionDefNode node);
    Object visitParam(ParamNode node);

    // Nodos de sentencias
    Object visitDeclaration(DeclarationNode node);
    Object visitVarDecl(VarDeclNode node);
    Object visitAssignment(AssignmentNode node);
    Object visitReturnStmt(ReturnStmtNode node);
    Object visitExprStmt(ExprStmtNode node);

    // Nodos de expresiones
    Object visitBinaryOp(BinaryOpNode node);
    Object visitNumber(NumberNode node);
    Object visitBoolean(BooleanNode node);
    Object visitVariable(VariableNode node);
    Object visitIfStmt(IfStmtNode node);
    Object visitWhileStmt(WhileStmtNode node);
    Object visitUnaryOp(UnaryOpNode node);
}