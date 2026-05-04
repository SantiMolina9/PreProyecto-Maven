## Universidad Nacional de Villa Mercedes - Escuela de Ingeniería y Ciencias Ambientales
# Proyecto de Compiladores + Análisis Estático de Programas
### Alumnos:
- Molina, Santiago Manuel
- Quiroga Stek, Esteban Eduardo
- Sanabria Quattrocchio, Pablo Emiliano
- Videla Del Castillo, Florencia Fatima

# Documentación del Compilador

## 📋 Tabla de Contenidos
1. [Introducción](#introducción)
2. [Instalación y Configuración](#instalación-y-configuración)
3. [Arquitectura](#arquitectura)
4. [Componentes](#componentes)
5. [Archivos de prueba](#archivos-de-prueba)

---

## Introducción

### ¿Qué es este proyecto?

Este es un **compilador educativo** desarrollado en Java que implementa un compilador completo con las tres fases fundamentales: análisis léxico, análisis sintáctico y análisis semántico. El compilador genera código assembler x86-64 a partir de un lenguaje simple de alto nivel.

Como extensión para la materia **Análisis Estático de Programas**, se agrega:
- La construcción del **Grafo de Flujo de Control (CFG)** desde el AST.
- El cómputo de **Post-Dominadores (PDOM)** mediante el algoritmo iterativo de punto fijo.
- La construcción del **Árbol de Post-Dominadores (PDT)** mediante el algoritmo BuildDtree.
- La construcción del **Grafo de Dependencias de Control (CDG)** mediante el algoritmo de Ferrante et al.
- La exportación de todos los grafos al formato DOT de Graphviz.

### Características principales

- **Análisis Léxico**: Tokenización usando JFlex
- **Análisis Sintáctico**: Parsing usando CUP (Java Cup) generando un Árbol Sintáctico Abstracto (AST)
- **Análisis Semántico**: Validación de tipos, scopes y variables
- **Generación de Código**: Producción de código assembler x86-64
- **CFG**: Construcción del Grafo de Flujo de Control a partir del AST
- **Post-Dominadores**: Cómputo del conjunto PDOM para cada nodo del CFG
- **PDT**: Construcción del Árbol de Post-Dominadores a partir de los conjuntos PDOM
- **CDG**: Construcción del Grafo de Dependencias de Control a partir del PDT
- **Visualización**: Exportación del CFG+PDOM, el PDT y el CDG a formato DOT (Graphviz)
- **Manejo de Errores**: Sistema completo con errores léxicos, sintácticos, semánticos y de tipos
- **Suite de Tests**: Casos de prueba cubriendo diferentes escenarios

### Ejemplo de código compilable

```c
integer f(){
    x = 3 ;
    if (y) {
        z = x + 1 ;
    }
    else {
        y = x + z ;
    }
    return z ;
}
```

### Requisitos previos

- Java 11 o superior
- Maven 3.6+
- Git
- Graphviz (opcional, para visualizar los grafos generados)

### Inicio rápido

```bash
# Clonar el repositorio
git clone <url-repositorio>
cd compiler

# Compilar el proyecto
mvn clean compile

# Ejecutar con archivo de prueba (Windows)
mvn exec:java "-Dexec.args=src/main/resources/test_cfg.txt"

# Ejecutar con archivo de prueba (Linux/Mac)
mvn exec:java -Dexec.args="src/main/resources/test_cfg.txt"

# Visualizar el CFG+PDOM generado
dot -Tpng src/main/resources/test_cfg.dot -o cfg.png

# Visualizar el PDT generado
dot -Tpng src/main/resources/test_cfg_pdt.dot -o pdt.png

# Visualizar el CDG generado
dot -Tpng src/main/resources/test_cfg_cdg.dot -o cdg.png
```

---

## Instalación y Configuración

### Instalación paso a paso

#### 1. Preparar el ambiente

```bash
java -version
javac -version
mvn --version
```

#### 2. Clonar y compilar el proyecto

```bash
git clone <url-repositorio> compiler
cd compiler
mvn clean compile

# Ejecutar (Windows)
mvn exec:java "-Dexec.args=src/main/resources/{nombre_archivo}.txt"

# Ejecutar (Linux/Mac)
mvn exec:java -Dexec.args="src/main/resources/{nombre_archivo}.txt"
```

La generación del parser y el lexer están automatizados en el `pom.xml`.

Se generan tres archivos DOT por cada programa analizado:

| Archivo generado | Contenido |
|---|---|
| `nombre.dot` | CFG con conjuntos PDOM anotados |
| `nombre_pdt.dot` | Árbol de Post-Dominadores |
| `nombre_cdg.dot` | Grafo de Dependencias de Control |

### Estructura del proyecto

```
compiler/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   ├── CompilerMain.java                  # Punto de entrada
        │   ├── ast/                               # Árbol sintáctico abstracto
        │   │   ├── ASTNode.java
        │   │   ├── nodes/
        │   │   │   ├── expression/                # BinaryOpNode, NumberNode, VariableNode...
        │   │   │   ├── statement/                 # AssignmentNode, IfStmtNode, WhileStmtNode...
        │   │   │   └── program/                   # ProgramNode, FunctionDefNode, ParamNode
        │   │   ├── visitor/
        │   │   │   └── ASTVisitor.java
        │   │   └── utils/
        │   │       └── ASTUtils.java
        │   ├── cfg/                               # Análisis de flujo de control
        │   │   ├── CFGBuilder.java                # Construcción del CFG desde el AST
        │   │   ├── CFGNode.java                   # Nodo y arista del CFG
        │   │   ├── DOTExporter.java               # Exportador a formato Graphviz
        │   │   ├── PostDominatorComputer.java      # Cómputo de Post-Dominadores (PDOM)
        │   │   ├── PostDominatorTreeBuilder.java  # Árbol de Post-Dominadores (PDT)
        │   │   └── CDGBuilder.java                # Grafo de Dependencias de Control (CDG)
        │   ├── cup/
        │   │   └── parser.cup
        │   └── jflex/
        │       └── lexer.flex
        └── resources/                             # Archivos de prueba y salidas .dot / .asm
```

---

## Arquitectura

### Pipeline completo

```
Código Fuente (.txt)
        │
        ▼
┌─────────────────┐
│      Lexer      │  JFlex → Tokens
└────────┬────────┘
         │ Tokens
         ▼
┌─────────────────┐
│    MiParser     │  CUP → AST (ProgramNode)
└────────┬────────┘
         │ AST
         ▼
┌─────────────────┐
│   CFGBuilder    │  AST → Lista de CFGNodes
└────────┬────────┘
         │ Lista de CFGNodes
         ▼
┌──────────────────────────┐
│  PostDominatorComputer   │  CFG → PDOM(n) para cada nodo
└────────┬─────────────────┘
         │ Map<CFGNode, Set<CFGNode>>
         ▼
┌──────────────────────────┐
│ PostDominatorTreeBuilder │  PDOM → Árbol PDT
└────────┬─────────────────┘
         │ PDT (padre → hijos)
         ▼
┌─────────────────┐
│   CDGBuilder    │  CFG + PDT → CDG (Ferrante et al.)
└────────┬────────┘
         │ Map<CFGNode, List<CDGEdge>>
         ▼
┌─────────────────┐
│  DOTExporter    │  CFG+PDOM → archivo .dot
│                 │  PDT      → archivo _pdt.dot
│                 │  CDG      → archivo _cdg.dot
└─────────────────┘
```

### Gramática del lenguaje (parser.cup)

```
program    → integer id ( ) { stmt_list }
stmt_list  → stmt+
stmt       → id = expr ;
           | return expr ;
           | if ( expr ) { stmt_list } else { stmt_list }
           | while ( expr ) { stmt_list }
expr       → value + value | value
value      → id | number
```

---

## Componentes

### 1. CompilerMain

Punto de entrada del pipeline. Orquesta las seis fases: parseo, construcción del CFG, cómputo de post-dominadores, construcción del PDT, construcción del CDG y exportación DOT.

```java
public static void main(String[] args)
```

**Fase 1 — Análisis Léxico y Sintáctico:**
```java
ProgramNode ast = parseFile(inputFile);
```

**Fase 2 — Construcción del CFG:**
```java
CFGBuilder cfgBuilder = new CFGBuilder();
cfgBuilder.build(ast);
cfgBuilder.printCFG();
```

**Fase 3 — Cómputo de Post-Dominadores:**
```java
PostDominatorComputer pdomComputer = new PostDominatorComputer(
        cfgBuilder.getAllNodes(), cfgBuilder.getExitNode());
pdomComputer.compute();
pdomComputer.printPdom();
```

**Fase 4 — Árbol de Post-Dominadores:**
```java
PostDominatorTreeBuilder pdtBuilder = new PostDominatorTreeBuilder(
        cfgBuilder.getAllNodes(), cfgBuilder.getExitNode(), pdomComputer.getAllPdom());
pdtBuilder.build();
pdtBuilder.printTree();
```

**Fase 5 — Exportación DOT (CFG+PDOM y PDT):**
```java
// CFG con PDOM anotado
String dot = DOTExporter.exportWithPdom(cfgBuilder.getAllNodes(), pdomComputer.getAllPdom());
// Árbol de Post-Dominadores
String pdtDot = DOTExporter.exportPDT(pdtBuilder);
```

**Fase 6 — Control Dependence Graph:**
```java
CDGBuilder cdgBuilder = new CDGBuilder(cfgBuilder.getAllNodes(), pdtBuilder);
cdgBuilder.build();
cdgBuilder.printCDG();
String cdgDot = DOTExporter.exportCDG(cfgBuilder.getAllNodes(), cdgBuilder);
```

Al finalizar imprime los comandos Graphviz para visualizar los grafos:
```
dot -Tpng test_cfg.dot -o cfg.png
dot -Tpng test_cfg_pdt.dot -o pdt.png
dot -Tpng test_cfg_cdg.dot -o cdg.png
```

---

### 2. CFGBuilder

Construye el Grafo de Flujo de Control a partir del AST. El patrón de construcción es recursivo: cada método retorna un par `{nodoEntrada, nodoSalida}` que representa la subred del CFG para esa sentencia.

#### Método principal

```java
public void build(ProgramNode program)
```

Crea los nodos especiales `ENTRY` y `EXIT`, procesa la lista de sentencias del cuerpo de la función y los conecta.

#### Métodos de acceso

```java
public CFGNode       getEntryNode()  // nodo ENTRY
public CFGNode       getExitNode()   // nodo EXIT
public List<CFGNode> getAllNodes()   // todos los nodos del CFG
public void          printCFG()     // imprime representación textual por stdout
```

#### Métodos de construcción (privados)

Cada método retorna `CFGNode[] { entryNode, exitNode }`:

```java
private CFGNode[] processStmtList(List<StmtNode> stmts)
```
Procesa una secuencia de sentencias conectándolas en cadena. Si la lista está vacía, crea un nodo `skip`.

```java
private CFGNode[] processStmt(StmtNode stmt)
```
Delega al método específico según el tipo concreto de sentencia. Para tipos no reconocidos, crea un nodo genérico.

```java
private CFGNode[] processAssignment(AssignmentNode node)
```
Crea un único nodo `STATEMENT` con etiqueta `"variableName = expresión"`. El nodo es a la vez entrada y salida del subgrafo.

```java
private CFGNode[] processReturn(ReturnStmtNode node)
```
Crea un nodo `STATEMENT` con etiqueta `"return expresión"` y lo conecta directamente al nodo `EXIT`. Retorna `null` como nodo de salida para indicar que no hay flujo normal de continuación.

```java
private CFGNode[] processIf(IfStmtNode node)
```
Construye la estructura de ramificación del `if/else`:

```
     condNode (CONDITION)
    /                    \
 True                  False
  /                        \
thenBranch             elseBranch (o directo a joinNode si no hay else)
    \                      /
     ──→  joinNode (JOIN) ←──
```

```java
private CFGNode[] processWhile(WhileStmtNode node)
```
Construye el bucle con back-edge:

```
     condNode (CONDITION) ←──────────────┐
    /                    \               │
 True                  False             │
  /                        \             │
bodyStatements          afterWhile       │
    │                  (JOIN, salida)    │
    └────────────────────────────────────┘ (back edge)
```

---

### 3. CFGNode

Representa un nodo del CFG. Contiene su tipo, etiqueta, lista de aristas salientes (sucesores) y lista de aristas entrantes (predecesores).

#### Tipos de nodo (`CFGNode.NodeType`)

| Tipo | Forma en DOT | Descripción |
|---|---|---|
| `ENTRY` | Círculo verde | Punto de entrada del programa |
| `EXIT` | Doble círculo rojo | Punto de salida del programa |
| `STATEMENT` | Rectángulo azul | Sentencia simple: asignación o return |
| `CONDITION` | Rombo amarillo | Condición de `if` o `while` |
| `JOIN` | Punto/Elipse | Confluencia de ramas o salida de bucle |

#### Clase interna: CFGEdge

Representa una arista dirigida entre dos nodos.

```java
public static class CFGEdge {
    public CFGNode getFrom()    // nodo origen
    public CFGNode getTo()      // nodo destino
    public String  getLabel()   // "True", "False" o "" para aristas normales
}
```

---

### 4. PostDominatorComputer

Computa los Post-Dominadores (PDOM) de un CFG usando el algoritmo iterativo de punto fijo sobre el CFG reverso.

#### Algoritmo (según slides de clase)

Para computar post-dominadores se obtiene el CFG reverso invirtiendo todas las aristas y se corre el algoritmo DOM sobre él. En el CFG reverso el nodo de entrada es el `EXIT` del CFG original.

```
PDOM(exit) = {exit}
Para todo nodo x ≠ exit:  PDOM(x) = N  (todos los nodos)

Hasta que no haya cambios:
  Para todo nodo x ≠ exit:
    PDOM(x) = {x} ∪ (⋂ PDOM(s)  para todo sucesor s de x en el CFG original)
```

Al converger: el nodo `d` pertenece a `PDOM(n)` si y solo si `d` post-domina a `n`.

#### Métodos principales

```java
public void                          compute()        // ejecuta el algoritmo
public Set<CFGNode>                  getPdom(CFGNode) // PDOM de un nodo
public Map<CFGNode, Set<CFGNode>>    getAllPdom()     // mapa completo
public void                          printPdom()      // imprime tabla por stdout
```

---

### 5. PostDominatorTreeBuilder

Construye el Árbol de Post-Dominadores (PDT) a partir de los conjuntos PDOM, implementando el algoritmo **BuildDtree** (ref: *rep-analysis-soft.pdf*, Figura 11).

#### Algoritmo BuildDtree

```
Entrada: Conjunto N, raíz n0 (EXIT), D(n) = PDOM(n) para cada nodo n.
Salida:  Árbol de post-dominadores PDT.

1. n0 es la raíz del PDT (EXIT)
2. Poner n0 en la cola Q
3. Para cada nodo n: D(n) = D(n) − {n}   (remover el propio nodo)
4. Mientras Q no esté vacía:
     m = siguiente nodo en Q
     Para cada nodo n tal que D(n) no esté vacío:
       Si D(n) contiene m:
         D(n) = D(n) − {m}
         Si D(n) quedó vacío:
           Agregar n como hijo de m en PDT
           Agregar n a Q
```

El post-dominador inmediato de un nodo `n` es su padre en el árbol resultante.

#### Métodos principales

```java
public void              build()              // ejecuta el algoritmo
public CFGNode           getRoot()            // raíz del árbol (EXIT)
public List<CFGNode>     getChildren(node)    // hijos de un nodo
public CFGNode           getParent(node)      // padre (ipdom) de un nodo
public void              printTree()          // imprime el árbol por stdout
```

---

### 6. CDGBuilder

Construye el Grafo de Dependencias de Control (CDG) a partir del CFG y el PDT, implementando el algoritmo de **Ferrante, Ottenstein y Warren (1987)**.

#### ¿Qué es el CDG?

El CDG captura las **dependencias de control** entre nodos: una arista `A → Y` en el CDG significa que la ejecución de `Y` depende de cuál rama toma `A`. Dicho de otro modo, `Y` es control-dependiente de `A` si:

- Existe una rama de `A` por la que `Y` siempre se ejecuta, y
- Existe otra rama de `A` por la que `Y` puede no ejecutarse.

Los nodos sin aristas CDG entrantes son de **ejecución incondicional** (no dependen de ningún predicado).

#### Algoritmo (Ferrante et al., 1987)

```
Para cada arista (A → B) en el CFG:
  Sea L = LCA(A, B) en el PDT   // Ancestro Común Más Profundo
  Para cada nodo Y en el camino de B hasta L (sin incluir L):
    Y es control-dependiente de A  →  agregar arista CDG A → Y
  Caso especial: si L == A (bucles while):
    A también es control-dependiente de sí mismo  →  agregar self-loop A → A
```

El **LCA en el PDT** se calcula subiendo desde `B` hasta encontrar el primer nodo que sea también ancestro de `A`.

#### Ejemplo: if/else

```
integer f() {
    if (cond) {   // n1: condición
        y = 1;    // n2
    } else {
        y = -1;   // n3
    }
    z = 2;        // n4 — siempre se ejecuta
}
```

PDT (raíz = EXIT):
```
EXIT
└── z = 2  (n4)
    └── join  (n5)
        ├── cond  (n1)
        │   └── ENTRY
        ├── y = 1  (n2)
        └── y = -1 (n3)
```

CDG resultante:
```
n1 (cond) --[True]-->  n2 (y = 1)
n1 (cond) --[False]--> n3 (y = -1)
```
`n4 (z = 2)` no tiene aristas CDG entrantes → se ejecuta incondicionalmente.

#### Ejemplo: while

```
integer f() {
    while (cond) {   // n1: condición
        y = y - 1;   // n2
    }
    z = 2;           // n3
}
```

CDG resultante:
```
n1 (cond) --[True]--> n2 (y = y - 1)
n1 (cond) --[True]--> n1 (cond)      ← self-loop: la condición controla su propia re-evaluación
```
`n3 (z = 2)` es incondicional.

#### Métodos principales

```java
public void build()
```
Ejecuta el algoritmo de Ferrante sobre todas las aristas del CFG.

```java
public Map<CFGNode, List<CDGEdge>> getAllEdges()
```
Retorna el mapa `predicado → lista de aristas CDG salientes`.

```java
public List<CDGEdge> getIncomingDependencies(CFGNode node)
```
Retorna los predicados de los que `node` es control-dependiente (aristas entrantes al nodo).

```java
public void printCDG()
```
Imprime el CDG y un resumen tabulado por nodo indicando de quién depende cada uno.

#### Clase interna: CDGEdge

```java
public static class CDGEdge {
    public CFGNode getPredicate()  // nodo predicado (A)
    public CFGNode getDependent()  // nodo dependiente (Y)
    public String  getLabel()      // "True", "False" o ""
}
```

---

### 7. DOTExporter

Exporta el CFG, el PDT y el CDG al formato DOT de Graphviz. Es una clase de utilidad con métodos estáticos.

#### Métodos principales

```java
public static String export(List<CFGNode> nodes)
```
Exporta el CFG plano sin información de PDOM.

```java
public static String exportWithPdom(List<CFGNode> nodes, Map<CFGNode, Set<CFGNode>> pdomMap)
```
Exporta el CFG con los conjuntos PDOM anotados en cada nodo, más una tabla resumen de post-dominadores.

```java
public static String exportPDT(PostDominatorTreeBuilder pdt)
```
Exporta el Árbol de Post-Dominadores con la raíz en `EXIT` y aristas padre → hijo representando la relación de post-dominación inmediata.

```java
public static String exportCDG(List<CFGNode> nodes, CDGBuilder cdg)
```
Exporta el CDG manteniendo la misma paleta visual del CFG. Las aristas CDG se dibujan como **flechas punteadas** (`style=dashed`) para distinguirlas de las aristas de flujo.

#### Representación visual por tipo de nodo

| Tipo | Atributos DOT |
|---|---|
| `ENTRY` | `shape=circle`, `fillcolor=green` |
| `EXIT` | `shape=doublecircle`, `fillcolor=red` |
| `STATEMENT` | `shape=box, style="rounded,filled"`, `fillcolor=lightblue` |
| `CONDITION` | `shape=diamond, style=filled`, `fillcolor=lightyellow` |
| `JOIN` | `shape=point` (en CFG) / `shape=ellipse` (en CFG+PDOM, PDT y CDG) |

#### Representación visual de aristas

| Grafo | Estilo | Etiqueta `"True"` | Etiqueta `"False"` |
|---|---|---|---|
| CFG | Sólida | Verde oscuro | Rojo |
| CDG | Punteada (`dashed`) | Verde oscuro | Rojo |

#### Archivos generados

A partir de un archivo `nombre.txt`, el pipeline produce:

| Archivo | Contenido |
|---|---|
| `nombre.dot` | CFG con conjuntos PDOM anotados + tabla resumen |
| `nombre_pdt.dot` | Árbol de Post-Dominadores (PDT) |
| `nombre_cdg.dot` | Grafo de Dependencias de Control (CDG) |

#### Ejemplo de salida (CFG simple)

```dot
digraph CFG_PDOM {
    rankdir=TB;
    ...
    n0 [shape=circle, ..., label="ENTRY"];
    n1 [shape=doublecircle, ..., label="EXIT"];
    n2 [shape=box, ..., label="n2: x = 3\nPDOM: {n2, n3, n6, n7, n1}"];
    n3 [shape=diamond, ..., label="n3: y\nPDOM: {n3, n6, n7, n1}"];
    ...
    n0 -> n2;
    n3 -> n4 [label="True",  color=darkgreen, fontcolor=darkgreen];
    n3 -> n5 [label="False", color=red,       fontcolor=red];
    ...
}
```

---

## Archivos de prueba

| Archivo | Descripción |
|---|---|
| `test_cfg.txt` | Programa simple con `if/else` |
| `test_cfg_while.txt` | Programa con bucle `while` |
| `test_cfg_completo.txt` | Programa con `if` anidado dentro de `while` |
| `test_bueno.txt` | Programa con múltiples estructuras de control |
| `test_completo_exitoso.txt` | Programa con todas las construcciones soportadas |
| `test_integral.txt` | Programa de prueba integral |
| `test_expresiones_complejas.txt` | Programa con expresiones aritméticas y booleanas complejas |
| `test_warning_no_inicializada.txt` | Variables usadas sin inicializar |
| `test_error_tipos.txt` | Errores de tipos |
| `test_error_duplicada.txt` | Variables declaradas más de una vez |
| `test_error_no_declarada.txt` | Variables usadas sin declarar |
| `test_error_ambitos.txt` | Variables fuera de su ámbito |
| `test_error_return.txt` | Return con tipo incorrecto |
| `test_division_cero.txt` | División por cero |
| `test_malo.txt` | Programa con múltiples errores semánticos |
| `test_scopes.txt` | Prueba de scoping de variables |