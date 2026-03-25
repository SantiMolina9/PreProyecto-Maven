package cfg;

import ast.nodes.program.*;
import ast.nodes.statement.*;
import ast.nodes.expression.*;
import cfg.CFGNode.CFGEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * Construye el Control Flow Graph (CFG) a partir del AST.
 *
 * Sigue las reglas de flujo para programas estructurados:
 *   - S1; S2          -> secuencial: S1 -> S2
 *   - if E then S1 else S2 -> branch: E -True-> S1, E -False-> S2, ambos -> join
 *   - while E S1      -> loop: E -True-> S1 -> back to E, E -False-> exit
 *   - id = expr       -> nodo simple
 *   - return expr     -> nodo que conecta al EXIT
 */
public class CFGBuilder {

    private int nodeCounter = 0;
    private CFGNode entryNode;
    private CFGNode exitNode;
    private List<CFGNode> allNodes = new ArrayList<>();

    // ========== Getters ==========

    public CFGNode getEntryNode() { return entryNode; }
    public CFGNode getExitNode() { return exitNode; }
    public List<CFGNode> getAllNodes() { return allNodes; }

    // ========== Construccion del CFG ==========

    /**
     * Construye el CFG completo a partir de un ProgramNode.
     * Crea nodos ENTRY y EXIT, y conecta el cuerpo de la funcion.
     */
    public void build(ProgramNode program) {
        // Crear nodos especiales de entrada y salida
        entryNode = createNode("ENTRY", CFGNode.NodeType.ENTRY);
        exitNode = createNode("EXIT", CFGNode.NodeType.EXIT);

        // Obtener la funcion principal y sus sentencias
        FunctionDefNode func = program.getMainFunction();
        List<StmtNode> stmts = func.getStatements();

        // Procesar la lista de sentencias
        CFGNode[] pair = processStmtList(stmts);

        // Conectar ENTRY -> primer statement
        if (pair[0] != null) {
            addEdge(entryNode, pair[0], "");
        } else {
            // Programa vacio: ENTRY -> EXIT
            addEdge(entryNode, exitNode, "");
        }

        // Conectar ultimo statement -> EXIT (si tiene flujo normal de salida)
        if (pair[1] != null) {
            addEdge(pair[1], exitNode, "");
        }
    }

    // ========== Metodos auxiliares ==========

    /**
     * Crea un nuevo nodo CFG con un ID unico.
     */
    private CFGNode createNode(String label, CFGNode.NodeType type) {
        CFGNode node = new CFGNode(nodeCounter++, label, type);
        allNodes.add(node);
        return node;
    }

    /**
     * Agrega una arista entre dos nodos.
     */
    private void addEdge(CFGNode from, CFGNode to, String label) {
        CFGEdge edge = new CFGEdge(from, to, label);
        from.addSuccessor(edge);
        to.addPredecessor(edge);
    }

    // ========== Procesamiento de sentencias ==========
    // Cada metodo retorna CFGNode[]{entryNode, exitNode}
    // - entry: por donde entra el flujo de control
    // - exit: por donde sale el flujo normal (null si no hay, ej: return)

    /**
     * Procesa una lista de sentencias (secuencia S1; S2; ... Sn).
     * Conecta las sentencias en cadena.
     */
    private CFGNode[] processStmtList(List<StmtNode> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            // Lista vacia: crear un nodo "skip"
            CFGNode empty = createNode("skip", CFGNode.NodeType.STATEMENT);
            return new CFGNode[]{empty, empty};
        }

        CFGNode firstEntry = null;
        CFGNode lastExit = null;

        for (StmtNode stmt : stmts) {
            CFGNode[] pair = processStmt(stmt);

            // El primer entry es el entry de la primera sentencia
            if (firstEntry == null) {
                firstEntry = pair[0];
            }

            // Conectar la salida de la sentencia anterior con la entrada de esta
            if (lastExit != null && pair[0] != null) {
                addEdge(lastExit, pair[0], "");
            }

            // Actualizar la ultima salida
            lastExit = pair[1];
        }

        return new CFGNode[]{firstEntry, lastExit};
    }

    /**
     * Procesa una sentencia individual, delegando segun su tipo.
     */
    private CFGNode[] processStmt(StmtNode stmt) {
        if (stmt instanceof AssignmentNode) {
            return processAssignment((AssignmentNode) stmt);
        } else if (stmt instanceof ReturnStmtNode) {
            return processReturn((ReturnStmtNode) stmt);
        } else if (stmt instanceof IfStmtNode) {
            return processIf((IfStmtNode) stmt);
        } else if (stmt instanceof WhileStmtNode) {
            return processWhile((WhileStmtNode) stmt);
        }

        // Fallback para cualquier otro tipo de sentencia
        CFGNode node = createNode(stmt.toString(), CFGNode.NodeType.STATEMENT);
        return new CFGNode[]{node, node};
    }

    /**
     * Procesa una asignacion: id = expr
     * Un solo nodo, entry = exit.
     */
    private CFGNode[] processAssignment(AssignmentNode node) {
        String label = node.getVariableName() + " = " + node.getExpression().toString();
        CFGNode n = createNode(label, CFGNode.NodeType.STATEMENT);
        return new CFGNode[]{n, n};
    }

    /**
     * Procesa un return: return expr
     * El nodo conecta directamente al EXIT. No tiene flujo normal de salida.
     */
    private CFGNode[] processReturn(ReturnStmtNode node) {
        String label;
        if (node.hasExpression()) {
            label = "return " + node.getExpression().toString();
        } else {
            label = "return";
        }
        CFGNode n = createNode(label, CFGNode.NodeType.STATEMENT);
        // El return conecta directamente al EXIT
        addEdge(n, exitNode, "");
        // null como exit indica que no hay flujo normal despues del return
        return new CFGNode[]{n, null};
    }

    /**
     * Procesa un if-else: if (E) { S1 } else { S2 }
     *
     *     condNode (E)
     *    /         \
     *  True       False
     *  /             \
     * S1             S2
     *  \             /
     *   -> joinNode <-
     */
    private CFGNode[] processIf(IfStmtNode node) {
        // Nodo de condicion (branch node)
        CFGNode condNode = createNode(node.getCondition().toString(),
                CFGNode.NodeType.CONDITION);

        // Procesar rama THEN
        CFGNode[] thenPair = processStmtList(node.getThenBranch());
        addEdge(condNode, thenPair[0], "True");

        // Procesar rama ELSE (si existe)
        if (node.hasElseBranch()) {
            CFGNode[] elsePair = processStmtList(node.getElseBranch());
            addEdge(condNode, elsePair[0], "False");

            // Punto de union (join node)
            CFGNode joinNode = createNode("", CFGNode.NodeType.JOIN);

            // Conectar ambas ramas al join (si tienen flujo normal)
            if (thenPair[1] != null) {
                addEdge(thenPair[1], joinNode, "");
            }
            if (elsePair[1] != null) {
                addEdge(elsePair[1], joinNode, "");
            }

            return new CFGNode[]{condNode, joinNode};
        } else {
            // If sin else: la rama False va directo al join
            CFGNode joinNode = createNode("", CFGNode.NodeType.JOIN);
            addEdge(condNode, joinNode, "False");

            if (thenPair[1] != null) {
                addEdge(thenPair[1], joinNode, "");
            }

            return new CFGNode[]{condNode, joinNode};
        }
    }

    /**
     * Procesa un while: while (E) { S1 }
     *
     *      condNode (E) <---+
     *      /         \      |
     *    True       False   |
     *    /             \    |
     *   S1          afterWhile
     *    |
     *    +--- back to condNode
     */
    private CFGNode[] processWhile(WhileStmtNode node) {
        // Nodo de condicion (se evalua repetidamente)
        CFGNode condNode = createNode(node.getCondition().toString(),
                CFGNode.NodeType.CONDITION);

        // Procesar cuerpo del while
        CFGNode[] bodyPair = processStmtList(node.getBody());

        // Condicion True -> cuerpo
        addEdge(condNode, bodyPair[0], "True");

        // Back edge: cuerpo -> condicion (el loop)
        if (bodyPair[1] != null) {
            addEdge(bodyPair[1], condNode, "");
        }

        // Condicion False -> salida del while
        CFGNode afterWhile = createNode("", CFGNode.NodeType.JOIN);
        addEdge(condNode, afterWhile, "False");

        return new CFGNode[]{condNode, afterWhile};
    }

    // ========== Impresion del CFG ==========

    /**
     * Imprime una representacion textual del CFG.
     */
    public void printCFG() {
        System.out.println("\n--- Control Flow Graph ---");
        System.out.println("Nodos: " + allNodes.size());
        System.out.println();

        for (CFGNode node : allNodes) {
            System.out.println(node);
            for (CFGEdge edge : node.getSuccessors()) {
                System.out.println("  " + edge);
            }
        }

        System.out.println("--- Fin CFG ---\n");
    }
}
