package codegen;

import ast.visitor.ASTVisitor;
import ast.nodes.program.*;
import ast.nodes.statement.*;
import ast.nodes.expression.*;
import semantic.symboltable.*;
import semantic.analyzer.TypeChecker;

import java.util.*;

/**
 * Generador de código assembler x86-64
 */
public class CodeGenerator implements ASTVisitor {
    private SymbolTable symbolTable;
    private StringBuilder code;
    private int labelCounter;
    private int tempCounter;
    private String currentFunction;
    private int stackFrameSize;
    private List<String> dataSection;
    private List<String> textSection;
    private Map<String, Integer> functionStackSizes;

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.code = new StringBuilder();
        this.labelCounter = 0;
        this.tempCounter = 0;
        this.currentFunction = null;
        this.stackFrameSize = 0;
        this.dataSection = new ArrayList<>();
        this.textSection = new ArrayList<>();
        this.functionStackSizes = new HashMap<>();

        initializeDataSection();
    }

    private void initializeDataSection() {
        /*dataSection.add("section .data");
        dataSection.add("    newline db 10, 0");
        dataSection.add("    int_format db \"%d\", 0");
        dataSection.add("    bool_true db \"true\", 0");
        dataSection.add("    bool_false db \"false\", 0");*/
    }

    private String generateLabel(String base) {
        return base + "_" + (labelCounter++);
    }

    private String generateTemp() {
        return "temp_" + (tempCounter++);
    }

    private void emit(String instruction) {
        textSection.add("    " + instruction);
    }

    private void emitLabel(String label) {
        textSection.add(label + ":");
    }

    private void emitComment(String comment) {
        //textSection.add("    ; " + comment);
    }

    // ========== VISITANTES PRINCIPALES ==========

    @Override
    public Object visitProgram(ProgramNode node) {
        //emitComment("Inicio del programa");

        // Generar código para la función main
        node.getMainFunction().accept(this);

        // Ensamblar las secciones finales
        buildFinalCode();

        return code.toString();
    }

    @Override
    public Object visitFunctionDef(FunctionDefNode node) {
        String functionName = node.getFunctionName();
        currentFunction = functionName;

        //emitComment("Definición de función: " + functionName);
        emitLabel(functionName);

        // Prologue
        emit("push rbp");
        emit("mov rbp, rsp");

        // Calcular tamaño del stack frame
        calculateStackFrameSize(node);
        if (stackFrameSize > 0) {
            emit("sub rsp, " + stackFrameSize);
        }

        // Procesar parámetros (asignar ubicaciones en stack)
        for (ParamNode param : node.getParameters()) {
            param.accept(this);
        }

        // Procesar cuerpo de la función
        for (StmtNode stmt : node.getStatements()) {
            stmt.accept(this);
        }

        // Si no hay return statement, agregar retorno por defecto
        if (!hasReturnStatement(node.getStatements())) {
            //emitComment("Return implícito");
            emit("mov rax, 0"); // Valor de retorno por defecto
        }

        // Epilogue
        emitLabel(functionName + "_exit");
        if (stackFrameSize > 0) {
            emit("add rsp, " + stackFrameSize);
        }
        emit("pop rbp");
        emit("ret");

        currentFunction = null;
        stackFrameSize = 0;
        symbolTable.resetStackOffset();

        return null;
    }

    @Override
    public Object visitParam(ParamNode node) {
        String paramName = node.getName();
        String paramType = node.getType();
        int size = TypeChecker.getTypeSize(paramType);

        // Los parámetros en x86-64 vienen en registros: rdi, rsi, rdx, rcx, r8, r9
        // Asignamos espacio en stack y movemos desde el registro
        int offset = symbolTable.allocateStackSpace(size);
        String address = "[rbp-" + offset + "]";

        // Actualizar symbol table con la dirección
        SymbolEntry entry = symbolTable.lookup(paramName);
        if (entry != null) {
            entry.setAddress(address);
            entry.setStackOffset(offset);
            entry.setSize(size);
        }

        // Mover parámetro desde registro a stack
        //emitComment("Guardar parámetro: " + paramName);
        switch (symbolTable.getCurrentStackOffset()) {
            case 4:  emit("mov " + address + ", edi"); break; // primer parámetro
            case 8:  emit("mov " + address + ", esi"); break; // segundo parámetro
            case 12: emit("mov " + address + ", edx"); break; // tercer parámetro
            default: emit("mov " + address + ", edi"); break;
        }

        return null;
    }

    @Override
    public Object visitDeclaration(DeclarationNode node) {
        String type = node.getType();
        int typeSize = TypeChecker.getTypeSize(type);

        for (VarDeclNode varDecl : node.getVariables()) {
            String varName = varDecl.getName();

            if (currentFunction == null) {
                // Variable global
                declareGlobalVariable(varName, type, varDecl);
            } else {
                // Variable local
                declareLocalVariable(varName, type, varDecl, typeSize);
            }
        }
        return null;
    }

    private void declareGlobalVariable(String varName, String type, VarDeclNode varDecl) {
        String asmType = TypeChecker.getAssemblyType(type);
        if (varDecl.hasInitialValue()) {
            Object value = evaluateConstantExpression(varDecl.getInitialValue());
            dataSection.add("    " + varName + " " + asmType + " " + value);
        } else {
            dataSection.add("    " + varName + " " + asmType + " 0");
        }

        // Actualizar symbol table - CORREGIDO
        if (varDecl.hasInitialValue()) {
            Object value = evaluateConstantExpression(varDecl.getInitialValue());
            symbolTable.declareWithAddress(varName, type, value, varName,
                    TypeChecker.getTypeSize(type), true, -1, -1);
        } else {
            symbolTable.declareWithAddress(varName, type, varName,
                    TypeChecker.getTypeSize(type), true);
        }
    }

    private void declareLocalVariable(String varName, String type, VarDeclNode varDecl, int typeSize) {
        int offset = symbolTable.allocateStackSpace(typeSize);
        String address = "[rbp-" + offset + "]";

        // Actualizar symbol table - CORREGIDO
        if (varDecl.hasInitialValue()) {
            Object value = evaluateConstantExpression(varDecl.getInitialValue());
            symbolTable.declareWithAddress(varName, type, value, address, typeSize, false, -1, -1);
        } else {
            symbolTable.declareWithAddress(varName, type, address, typeSize, false);
        }

        // Inicializar si tiene valor
        if (varDecl.hasInitialValue()) {
            emitComment("Inicializar variable: " + varName);
            varDecl.getInitialValue().accept(this); // Resultado en rax
            emit("mov " + address + ", eax");
        } else {
            // Inicializar a cero
            emit("mov dword " + address + ", 0");
        }
    }


    @Override
    public Object visitAssignment(AssignmentNode node) {
        //emitComment("Asignación: " + node.getVariableName());

        // Evaluar la expresión derecha
        node.getExpression().accept(this); // Resultado en eax

        // Buscar la variable izquierda
        SymbolEntry entry = symbolTable.lookup(node.getVariableName());
        if (entry != null) {
            emit("mov " + entry.getAddress() + ", eax");
        }

        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmtNode node) {
        //emitComment("Return statement");

        if (node.hasExpression()) {
            node.getExpression().accept(this); // Resultado en eax
        } else {
            emit("mov eax, 0"); // Return 0 por defecto
        }

        emit("jmp " + currentFunction + "_exit");
        return null;
    }

    @Override
    public Object visitExprStmt(ExprStmtNode node) {
        // Solo evaluar la expresión y descartar el resultado
        node.getExpression().accept(this);
        return null;
    }

    // ========== VISITANTES DE EXPRESIONES ==========
    @Override
    public Object visitIfStmt(IfStmtNode node) {
        String elseLabel = generateLabel("else");
        String endLabel = generateLabel("endif");

        // Evaluar condición
        node.getCondition().accept(this);
        emit("cmp eax, 0");

        if (node.hasElseBranch()) {
            emit("je " + elseLabel);

            // Rama then
            for (StmtNode stmt : node.getThenBranch()) {
                stmt.accept(this);
            }
            emit("jmp " + endLabel);

            // Rama else
            emitLabel(elseLabel);
            for (StmtNode stmt : node.getElseBranch()) {
                stmt.accept(this);
            }
        } else {
            emit("je " + endLabel);

            // Rama then
            for (StmtNode stmt : node.getThenBranch()) {
                stmt.accept(this);
            }
        }

        emitLabel(endLabel);
        return null;
    }

    @Override
    public Object visitUnaryOp(UnaryOpNode node) {
        node.getOperand().accept(this);

        switch (node.getOperator()) {
            case NOT:
                emit("xor eax, 1"); // Invertir el valor booleano
                break;
        }

        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmtNode node) {
        String startLabel = generateLabel("while_start");
        String endLabel = generateLabel("while_end");

        emitLabel(startLabel);

        // Evaluar condición
        node.getCondition().accept(this);
        emit("cmp eax, 0");
        emit("je " + endLabel);

        // Cuerpo del while
        for (StmtNode stmt : node.getBody()) {
            stmt.accept(this);
        }

        emit("jmp " + startLabel);
        emitLabel(endLabel);

        return null;
    }

    @Override
    public Object visitBinaryOp(BinaryOpNode node) {
        //emitComment("Operación binaria: " + node.getOperator());

        // Evaluar operando izquierdo
        node.getLeft().accept(this);
        emit("push rax"); // Guardar resultado izquierdo

        // Evaluar operando derecho
        node.getRight().accept(this);
        emit("pop rbx"); // Recuperar izquierdo en rbx

        // Aplicar operación
        switch (node.getOperator()) {
            case PLUS:
                emit("add ebx, eax");
                emit("mov eax, ebx");
                break;
            case MINUS:
                emit("sub ebx, eax");
                emit("mov eax, ebx");
                break;
            case TIMES:
                emit("imul ebx, eax");
                emit("mov eax, ebx");
                break;
            case DIVIDE:
                emit("xchg eax, ebx");
                emit("cdq"); // Extender eax a edx:eax
                emit("idiv ebx");
                break;
            case LT:
                emit("cmp ebx, eax");
                emit("setl al");
                emit("movzx eax, al");
                break;

            case GT:
                emit("cmp ebx, eax");
                emit("setg al");
                emit("movzx eax, al");
                break;

            case EQ:
                emit("cmp ebx, eax");
                emit("sete al");
                emit("movzx eax, al");
                break;

            case AND:
                emit("and eax, ebx");
                break;

            case OR:
                emit("or eax, ebx");
                break;
        }
        return null;
    }

    @Override
    public Object visitNumber(NumberNode node) {
        //emitComment("Cargar número: " + node.getValue());
        emit("mov eax, " + node.getValue());
        return null;
    }

    @Override
    public Object visitBoolean(BooleanNode node) {
        //emitComment("Cargar booleano: " + node.getValue());
        emit("mov eax, " + (node.getValue() ? "1" : "0"));
        return null;
    }

    @Override
    public Object visitVariable(VariableNode node) {
        SymbolEntry entry = symbolTable.lookup(node.getName());
        if (entry != null) {
            //emitComment("Cargar variable: " + node.getName());
            if (entry.isGlobal()) {
                emit("mov eax, [" + entry.getAddress() + "]");
            } else {
                emit("mov eax, " + entry.getAddress());
            }
        }
        return null;
    }

    @Override
    public Object visitVarDecl(VarDeclNode node) {
        // Ya manejado en visitDeclaration
        return null;
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void calculateStackFrameSize(FunctionDefNode node) {
        int totalSize = 0;

        // Espacio para parámetros
        for (ParamNode param : node.getParameters()) {
            totalSize += TypeChecker.getTypeSize(param.getType());
        }

        // Espacio para variables locales
        for (StmtNode stmt : node.getStatements()) {
            if (stmt instanceof DeclarationNode) {
                DeclarationNode decl = (DeclarationNode) stmt;
                for (VarDeclNode var : decl.getVariables()) {
                    totalSize += TypeChecker.getTypeSize(decl.getType());
                }
            }
        }

        // Alinear a 16 bytes (convención x86-64)
        stackFrameSize = (totalSize + 15) & ~15;
        functionStackSizes.put(currentFunction, stackFrameSize);
    }

    private boolean hasReturnStatement(List<StmtNode> statements) {
        for (StmtNode stmt : statements) {
            if (stmt instanceof ReturnStmtNode) {
                return true;
            }
        }
        return false;
    }

    private Object evaluateConstantExpression(ExprNode expr) {
        // Evaluación simple de constantes en tiempo de compilación
        if (expr instanceof NumberNode) {
            return ((NumberNode) expr).getValue();
        } else if (expr instanceof BooleanNode) {
            return ((BooleanNode) expr).getValue() ? 1 : 0;
        }
        return 0; // Valor por defecto
    }

    private void buildFinalCode() {
        // Ensamblar sección data
        for (String line : dataSection) {
            code.append(line).append("\n");
        }
        //Correccion de CodeGenerator   
        code.append("\nsection .text\n");
        code.append("global _start\n\n");
        code.append("_start:\n");
        code.append("    call main\n");
        code.append("    mov ebx, eax    ; exit code\n");
        code.append("    mov eax, 1      ; sys_exit\n");
        code.append("    int 0x80\n\n");

        // Ensamblar sección text
        for (String line : textSection) {
            code.append(line).append("\n");
        }
    }



    public String getGeneratedCode() {
        return code.toString();
    }
}