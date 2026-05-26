package cfg;

import ast.nodes.program.*;
import ast.nodes.statement.*;
import ast.nodes.expression.*;
import cfg.CFGNode.CFGEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Construye el Control Flow Graph (CFG) a partir del AST.
 *
 * Reglas de flujo:
 *   S1; S2                -> secuencial: S1 -> S2
 *   if E then S1 else S2  -> E -True-> S1, E -False-> S2; ambas ramas conectan al siguiente nodo
 *   while E { S }         -> E -True-> S -> E (back edge), E -False-> siguiente nodo
 *   id = expr             -> nodo simple
 *   return expr           -> nodo que conecta directamente al EXIT
 *
 * No se crean nodos JOIN artificiales: el punto de merge de un if-else es simplemente
 * el primer nodo que sigue al if-else (tiene dos predecesores naturales).
 * Esto evita nodos estructurales sin semantica que contaminan el CDG, DDG y los slices.
 */
public class CFGBuilder {

    private int nodeCounter = 0;
    private CFGNode entryNode;
    private CFGNode exitNode;
    private List<CFGNode> allNodes = new ArrayList<>();

    // ========== Getters ==========

    public CFGNode getEntryNode() { return entryNode; }
    public CFGNode getExitNode()  { return exitNode; }
    public List<CFGNode> getAllNodes() { return allNodes; }

    // ========== Construccion del CFG ==========

    public void build(ProgramNode program) {
        entryNode = createNode("ENTRY", CFGNode.NodeType.ENTRY);
        exitNode  = createNode("EXIT",  CFGNode.NodeType.EXIT);

        FunctionDefNode func = program.getMainFunction();
        CFGFragment frag = processStmtList(func.getStatements());

        if (frag.entry != null) {
            addEdge(entryNode, frag.entry, "");
        } else {
            addEdge(entryNode, exitNode, "");
        }

        // Conectar salidas normales pendientes al EXIT
        for (CFGFragment.PendingExit pe : frag.exits) {
            addEdge(pe.node, exitNode, pe.label);
        }
    }

    // ========== Fragmento de CFG ==========

    /**
     * Representa el resultado de procesar una sentencia o bloque:
     *   entry: nodo por donde entra el flujo
     *   exits: pares (nodo, etiqueta) de aristas que aun no tienen destino
     *
     * Las exits pendientes se resuelven al encontrar el siguiente fragmento
     * en la secuencia, o al conectar con EXIT al final.
     */
    private static class CFGFragment {
        final CFGNode entry;
        final List<PendingExit> exits;

        static class PendingExit {
            final CFGNode node;
            final String label;
            PendingExit(CFGNode node, String label) {
                this.node  = node;
                this.label = label;
            }
        }

        /** Fragmento de un nodo simple (entry == exit, sin label). */
        CFGFragment(CFGNode single) {
            this.entry = single;
            this.exits = new ArrayList<>();
            this.exits.add(new PendingExit(single, ""));
        }

        CFGFragment(CFGNode entry, List<PendingExit> exits) {
            this.entry = entry;
            this.exits = exits;
        }
    }

    // ========== Metodos auxiliares ==========

    private CFGNode createNode(String label, CFGNode.NodeType type) {
        CFGNode node = new CFGNode(nodeCounter++, label, type);
        allNodes.add(node);
        return node;
    }

    private void addEdge(CFGNode from, CFGNode to, String label) {
        CFGEdge edge = new CFGEdge(from, to, label);
        from.addSuccessor(edge);
        to.addPredecessor(edge);
    }

    // ========== Procesamiento de sentencias ==========

    /**
     * Procesa una lista de sentencias en secuencia.
     * Las exits de cada sentencia se conectan al entry de la siguiente.
     */
    private CFGFragment processStmtList(List<StmtNode> stmts) {
        if (stmts == null || stmts.isEmpty()) {
            return new CFGFragment(createNode("skip", CFGNode.NodeType.STATEMENT));
        }

        CFGFragment first = null;
        CFGFragment prev  = null;

        for (StmtNode stmt : stmts) {
            CFGFragment curr = processStmt(stmt);

            if (first == null) first = curr;

            if (prev != null) {
                for (CFGFragment.PendingExit pe : prev.exits) {
                    addEdge(pe.node, curr.entry, pe.label);
                }
            }

            prev = curr;
        }

        return new CFGFragment(first.entry, prev.exits);
    }

    private CFGFragment processStmt(StmtNode stmt) {
        if (stmt instanceof AssignmentNode)  return processAssignment((AssignmentNode) stmt);
        if (stmt instanceof ReturnStmtNode)  return processReturn((ReturnStmtNode) stmt);
        if (stmt instanceof IfStmtNode)      return processIf((IfStmtNode) stmt);
        if (stmt instanceof WhileStmtNode)   return processWhile((WhileStmtNode) stmt);

        CFGNode node = createNode(stmt.toString(), CFGNode.NodeType.STATEMENT);
        return new CFGFragment(node);
    }

    private CFGFragment processAssignment(AssignmentNode node) {
        String label = node.getVariableName() + " = " + node.getExpression().toString();
        CFGNode n = createNode(label, CFGNode.NodeType.STATEMENT);
        n.setDefinedVar(node.getVariableName());
        n.setUsedVars(VariableExtractor.extract(node.getExpression()));
        return new CFGFragment(n);
    }

    private CFGFragment processReturn(ReturnStmtNode node) {
        String label = node.hasExpression()
                ? "return " + node.getExpression().toString()
                : "return";
        CFGNode n = createNode(label, CFGNode.NodeType.STATEMENT);
        if (node.hasExpression()) {
            n.setUsedVars(VariableExtractor.extract(node.getExpression()));
        }
        addEdge(n, exitNode, "");
        // Sin exits pendientes: el return sale directamente al EXIT
        return new CFGFragment(n, Collections.emptyList());
    }

    /**
     * if (E) { S1 } else { S2 }
     *
     *      condNode
     *      /      \
     *   True     False
     *    /          \
     *   S1          S2
     *    \          /
     *   (exits pendientes, se resuelven en el proximo nodo de la secuencia)
     *
     * if (E) { S1 }  (sin else)
     *
     *      condNode
     *      /      \
     *   True    False (exit pendiente del propio condNode)
     *    /
     *   S1
     *    \
     *   (exit pendiente)
     */
    private CFGFragment processIf(IfStmtNode node) {
        CFGNode condNode = createNode(node.getCondition().toString(), CFGNode.NodeType.CONDITION);
        condNode.setUsedVars(VariableExtractor.extract(node.getCondition()));

        CFGFragment thenFrag = processStmtList(node.getThenBranch());
        addEdge(condNode, thenFrag.entry, "True");

        List<CFGFragment.PendingExit> exits = new ArrayList<>();

        if (node.hasElseBranch()) {
            CFGFragment elseFrag = processStmtList(node.getElseBranch());
            addEdge(condNode, elseFrag.entry, "False");
            exits.addAll(thenFrag.exits);
            exits.addAll(elseFrag.exits);
        } else {
            // Sin else: la rama False sale directamente del nodo condicion
            exits.add(new CFGFragment.PendingExit(condNode, "False"));
            exits.addAll(thenFrag.exits);
        }

        return new CFGFragment(condNode, exits);
    }

    /**
     * while (E) { S }
     *
     *      condNode <---+
     *      /      \     |
     *   True    False   |
     *    /               \
     *   S ---------------+ (back edge)
     *
     * Exit pendiente: condNode con etiqueta "False"
     */
    private CFGFragment processWhile(WhileStmtNode node) {
        CFGNode condNode = createNode(node.getCondition().toString(), CFGNode.NodeType.CONDITION);
        condNode.setUsedVars(VariableExtractor.extract(node.getCondition()));

        CFGFragment bodyFrag = processStmtList(node.getBody());
        addEdge(condNode, bodyFrag.entry, "True");

        // Back edges: salidas del cuerpo regresan a la condicion
        for (CFGFragment.PendingExit pe : bodyFrag.exits) {
            addEdge(pe.node, condNode, pe.label);
        }

        List<CFGFragment.PendingExit> exits = new ArrayList<>();
        exits.add(new CFGFragment.PendingExit(condNode, "False"));
        return new CFGFragment(condNode, exits);
    }

    // ========== Impresion del CFG ==========

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
