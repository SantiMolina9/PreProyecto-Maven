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

Este es un **compilador** desarrollado en Java que implementa un compilador completo con las tres fases fundamentales: análisis léxico, análisis sintáctico y análisis semántico. El compilador genera código assembler x86-64 a partir de un lenguaje simple de alto nivel.

Como extensión para la materia **Análisis Estático de Programas**, se implementa el pipeline de análisis sobre el CFG:

- Construcción del **Grafo de Flujo de Control (CFG)** desde el AST, sin nodos JOIN artificiales.
- Cómputo de **Post-Dominadores (PDOM)** mediante el algoritmo iterativo de punto fijo.
- Construcción del **Árbol de Post-Dominadores (PDT)** mediante el algoritmo BuildDtree.
- Construcción del **Grafo de Dependencias de Control (CDG)** mediante el algoritmo de Ferrante et al.
- Análisis de **Definiciones Alcanzantes (Reaching Definitions)** con el algoritmo iterativo forward-may.
- Construcción del **Grafo de Dependencias de Datos (DDG)** a partir de los pares definición-uso.
- **Program Slicing**: cómputo del backward slice a partir del PDG (CDG + DDG) dado un criterio de slice.
- Exportación de todos los grafos al formato DOT de Graphviz.

### Características principales

- **Análisis Léxico**: Tokenización usando JFlex
- **Análisis Sintáctico**: Parsing usando CUP generando un AST
- **Análisis Semántico**: Validación de tipos, scopes y variables
- **Generación de Código**: Código assembler x86-64
- **CFG**: Sin nodos JOIN artificiales; el merge de ramas es implícito en el nodo siguiente
- **PDOM / PDT**: Post-dominadores y árbol de post-dominadores
- **CDG**: Dependencias de control (Ferrante et al., 1987)
- **Reaching Definitions**: Análisis de flujo de datos (GEN/KILL/IN/OUT) hasta punto fijo
- **DDG**: Pares definición-uso derivados de las definiciones alcanzantes
- **Program Slicing**: Backward slice por BFS sobre el PDG (CDG + DDG inverso)
- **Visualización**: DOT (Graphviz) para todos los grafos, con slice resaltado
- **Manejo de Errores**: Sistema completo con errores léxicos, sintácticos, semánticos y de tipos

### Ejemplo de código analizable

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

# Ejecutar con archivo de prueba (Windows) — sin slicing
mvn exec:java "-Dexec.args=src/main/resources/cfg/test_cfg.txt"

# Ejecutar con slicing (especificando el nodo criterio por su ID)
mvn exec:java "-Dexec.args=src/main/resources/cfg/test_cfg.txt 6"

# Ejecutar (Linux/Mac)
mvn exec:java -Dexec.args="src/main/resources/cfg/test_cfg.txt 6"
```

Archivos DOT generados (en la misma carpeta que el `.txt` de entrada):

```bash
# Visualizar el CFG+PDOM
dot -Tpng src/main/resources/cfg/dot/test_cfg.dot -o cfg.png

# Visualizar el PDT
dot -Tpng src/main/resources/cfg/dot/test_cfg_pdt.dot -o pdt.png

# Visualizar el CDG
dot -Tpng src/main/resources/cfg/dot/test_cfg_cdg.dot -o cdg.png

# Visualizar el DDG
dot -Tpng src/main/resources/cfg/dot/test_cfg_ddg.dot -o ddg.png

# Visualizar el slice (CFG completo con nodos resaltados)
dot -Tpng src/main/resources/cfg/dot/test_cfg_slice.dot -o slice.png

# Visualizar el CFG reducido al slice
dot -Tpng src/main/resources/cfg/dot/test_cfg_sliced_cfg.dot -o sliced_cfg.png
```

O pegar el contenido DOT en: https://dreampuf.github.io/GraphvizOnline/

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
```

La generación del parser y el lexer están automatizados en el `pom.xml`.

#### 3. Ejecutar el pipeline

```bash
# Sin slicing (Windows)
mvn exec:java "-Dexec.args=src/main/resources/cfg/test_cfg.txt"

# Con slicing — indicar el ID del nodo criterio (Linux/Mac)
mvn exec:java -Dexec.args="src/main/resources/cfg/test_cfg.txt <nodeId>"
```

Si se omite `<nodeId>`, la Fase 9 imprime la lista de nodos disponibles y no genera archivos de slice.

### Archivos generados

A partir de un archivo `nombre.txt`, el pipeline produce:

| Archivo | Contenido |
|---|---|
Los archivos `.dot` se guardan en `<carpeta-del-txt>/dot/`:

| Archivo | Contenido |
|---|---|
| `dot/nombre.dot` | CFG con conjuntos PDOM anotados + tabla resumen |
| `dot/nombre_pdt.dot` | Árbol de Post-Dominadores |
| `dot/nombre_cdg.dot` | Grafo de Dependencias de Control |
| `dot/nombre_ddg.dot` | Grafo de Dependencias de Datos |
| `dot/nombre_slice.dot` | CFG completo con el slice resaltado |
| `dot/nombre_sliced_cfg.dot` | CFG reducido a los nodos del slice |

Los archivos de slice solo se generan si se pasa un `<nodeId>` como segundo argumento.

### Estructura del proyecto

```
compiler/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   ├── CompilerMain.java                       # Punto de entrada (9 fases)
        │   ├── ast/                                    # Árbol sintáctico abstracto
        │   │   ├── ASTNode.java
        │   │   ├── nodes/
        │   │   │   ├── expression/                     # BinaryOpNode, NumberNode, VariableNode...
        │   │   │   ├── statement/                      # AssignmentNode, IfStmtNode, WhileStmtNode...
        │   │   │   └── program/                        # ProgramNode, FunctionDefNode, ParamNode
        │   │   ├── visitor/
        │   │   │   └── ASTVisitor.java
        │   │   └── utils/
        │   │       └── ASTUtils.java
        │   ├── cfg/                                    # Análisis estático
        │   │   ├── CFGBuilder.java                     # Construcción del CFG (sin nodos JOIN)
        │   │   ├── CFGNode.java                        # Nodo y arista del CFG
        │   │   ├── DOTExporter.java                    # Exportador a formato Graphviz
        │   │   ├── PostDominatorComputer.java           # Cómputo de Post-Dominadores (PDOM)
        │   │   ├── PostDominatorTreeBuilder.java        # Árbol de Post-Dominadores (PDT)
        │   │   ├── CDGBuilder.java                     # Grafo de Dependencias de Control (CDG)
        │   │   ├── Definition.java                     # Par (nodo, variable) para reaching defs
        │   │   ├── VariableExtractor.java              # Extrae variables usadas de expresiones AST
        │   │   ├── ReachingDefinitionsComputer.java    # Análisis GEN/KILL/IN/OUT (punto fijo)
        │   │   ├── DDGBuilder.java                     # Grafo de Dependencias de Datos (DDG)
        │   │   └── ProgramSlicer.java                  # Backward program slicing sobre el PDG
        │   ├── cup/
        │   │   └── parser.cup
        │   └── jflex/
        │       └── lexer.flex
        └── resources/
            ├── cfg/                                    # Tests del análisis CFG/PDG
            │   ├── test_cfg.txt                        # Programa simple con if/else
            │   ├── test_cfg_while.txt                  # Programa con bucle while
            │   ├── test_cfg_completo.txt               # Programa con if anidado en while
            │   └── dot/                                # Archivos .dot generados por el pipeline
            │       ├── test_cfg.dot
            │       ├── test_cfg_pdt.dot
            │       ├── test_cfg_cdg.dot
            │       ├── test_cfg_ddg.dot
            │       ├── test_cfg_slice.dot              # (solo si se especifica criterio)
            │       └── test_cfg_sliced_cfg.dot         # (solo si se especifica criterio)
            ├── test_bueno.txt / .asm
            ├── test_completo_exitoso.txt / .asm
            ├── test_error_*.txt                        # Tests de errores semánticos
            └── ...                                     # Resto de tests del compilador
```

---

## Arquitectura

### Pipeline completo (9 fases)

```
Código Fuente (.txt)
        │
        ▼
┌─────────────────┐
│  Fase 1: Lexer  │  JFlex → Tokens
│     + Parser    │  CUP   → AST (ProgramNode)
└────────┬────────┘
         │ AST
         ▼
┌─────────────────┐
│  Fase 2:        │
│   CFGBuilder    │  AST → Lista de CFGNodes (sin nodos JOIN)
└────────┬────────┘
         │ CFGNodes
         ▼
┌──────────────────────────┐
│  Fase 3:                 │
│  PostDominatorComputer   │  CFG → PDOM(n) para cada nodo
└────────┬─────────────────┘
         │ Map<CFGNode, Set<CFGNode>>
         ▼
┌──────────────────────────┐
│  Fase 4:                 │
│ PostDominatorTreeBuilder │  PDOM → Árbol PDT
└────────┬─────────────────┘
         │ PDT
         ▼
┌─────────────────┐
│  Fase 5:        │
│  DOTExporter    │  CFG+PDOM → .dot  /  PDT → _pdt.dot
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Fase 6:        │
│   CDGBuilder    │  CFG + PDT → CDG (Ferrante et al.)
│  DOTExporter    │  CDG → _cdg.dot
└────────┬────────┘
         │ CDG
         ▼
┌──────────────────────────────┐
│  Fase 7:                     │
│  ReachingDefinitionsComputer │  CFG → GEN/KILL/IN/OUT (punto fijo)
└────────┬─────────────────────┘
         │ IN-sets
         ▼
┌─────────────────┐
│  Fase 8:        │
│   DDGBuilder    │  IN-sets + usedVars → pares Def-Uso → DDG
│  DOTExporter    │  DDG → _ddg.dot
└────────┬────────┘
         │ DDG
         ▼
┌─────────────────┐
│  Fase 9:        │
│  ProgramSlicer  │  CDG + DDG + criterio → slice (BFS hacia atrás)
│  DOTExporter    │  slice → _slice.dot / _sliced_cfg.dot
└─────────────────┘
```

### Ruta de grafos generados

Muestra cómo cada análisis alimenta al siguiente y qué archivo `.dot` produce cada uno.

```
 Código fuente (.txt)
         │
         ▼
 ┌───────────────────────────────────┐
 │              CFG                  │──────────────────► test_cfg.dot
 │    (Control Flow Graph)           │                    (CFG + PDOM anotado)
 └──────────────┬────────────────────┘
                │
        ┌───────┴───────────────────────────┐
        │                                   │
        ▼                                   ▼
 ┌──────────────────┐              ┌──────────────────────────┐
 │       PDOM       │              │    Reaching Definitions   │
 │  Post-Dominadores│              │   (GEN / KILL / IN / OUT) │
 └────────┬─────────┘              └─────────────┬────────────┘
          │                                      │
          ▼                                      ▼
 ┌──────────────────┐                   ┌────────────────┐
 │       PDT        │──────────────────►│      DDG       │──► test_cfg_ddg.dot
 │ (Post-Dom. Tree) │  test_cfg_pdt.dot │ (Data Dep. Graph)   (dependencias de datos)
 └────────┬─────────┘                   └────────┬───────┘
          │                                      │
          ▼                                      │
 ┌──────────────────┐                            │
 │       CDG        │──────────────────────────► │
 │(Control Dep.Graph│  test_cfg_cdg.dot          │
 └────────┬─────────┘  (dependencias de control) │
          │                                      │
          └──────────────────┬───────────────────┘
                             │
                        PDG = CDG + DDG
                             │
                    + criterio: nodo ID
                             │
                             ▼
                  ┌────────────────────┐
                  │   Program Slice    │──► test_cfg_slice.dot
                  │  (backward BFS    │     (CFG completo, slice resaltado)
                  │   sobre el PDG)   │
                  └────────────────────┘──► test_cfg_sliced_cfg.dot
                                           (CFG reducido al slice)
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

Punto de entrada del pipeline. Orquesta las nueve fases.

**Uso:**
```bash
# Sin slicing
mvn exec:java "-Dexec.args=src/main/resources/cfg/test_cfg.txt"

# Con slicing (nodeId = ID del nodo criterio)
mvn exec:java "-Dexec.args=src/main/resources/cfg/test_cfg.txt <nodeId>"
```

Si se omite el segundo argumento, la Fase 9 imprime los nodos disponibles y no genera archivos de slice. El path por defecto (sin argumentos) es `src/main/resources/cfg/test_cfg.txt`.

---

### 2. CFGBuilder

Construye el CFG a partir del AST. Usa internamente un modelo de **fragmentos con salidas pendientes** (`CFGFragment`) en lugar del par `{entrada, salida}` clásico, lo que permite eliminar los nodos JOIN artificiales.

#### Por qué no hay nodos JOIN

En una representación con JOIN:
```
condNode --True--> S1 ---> JOIN ---> return z
condNode --False-> S2 ---> JOIN
```

El nodo JOIN no tiene semántica computacional: no define ni usa variables, no tiene condición. Su presencia contamina el CDG (puede aparecer como control-dependiente de la condición), el PDT y los slices.

En la representación actual, ambas ramas conectan **directamente** al siguiente nodo de la secuencia:
```
condNode --True--> S1 ---> return z
condNode --False-> S2 ---> return z
```

`return z` tiene dos predecesores naturales — ese es el merge implícito.

#### CFGFragment (interno)

```
CFGFragment {
    entry:  CFGNode          — nodo de entrada del fragmento
    exits:  List<PendingExit>  — aristas pendientes de conectar
}

PendingExit {
    node:  CFGNode   — nodo origen
    label: String    — etiqueta de la arista ("True", "False", "")
}
```

Cuando `processStmtList` encadena sentencias, conecta todas las `exits` del fragmento anterior al `entry` del siguiente (preservando etiquetas). Al finalizar la función, las exits pendientes se conectan a `EXIT`.

#### Métodos de acceso

```java
public CFGNode       getEntryNode()
public CFGNode       getExitNode()
public List<CFGNode> getAllNodes()
public void          printCFG()
```

#### Estructura del CFG para if/else

```
      condNode (CONDITION)
      /                  \
   True                False
    /                      \
  S1                       S2
    \                      /
     ──→ siguienteNodo ←──     ← dos predecesores, sin nodo JOIN
```

#### Estructura del CFG para while

```
      condNode (CONDITION) ←──────────┐
      /                \              │
   True              False            │
    /                   \             │
  body               siguienteNodo   │
    └────────────────────────────────┘  (back edge)
```

---

### 3. CFGNode

Representa un nodo del CFG.

#### Tipos de nodo (`CFGNode.NodeType`)

| Tipo | Forma en DOT | Descripción |
|---|---|---|
| `ENTRY` | Círculo verde | Punto de entrada del programa |
| `EXIT` | Doble círculo rojo | Punto de salida del programa |
| `STATEMENT` | Rectángulo azul | Sentencia: asignación o return |
| `CONDITION` | Rombo amarillo | Condición de `if` o `while` |
| `JOIN` | — | Tipo reservado, no se usa en la construcción actual |

#### Campos para análisis de flujo de datos

```java
private String      definedVar;  // variable definida en este nodo (null si no define ninguna)
private Set<String> usedVars;    // variables referenciadas (lado derecho de asignación o condición)
```

Estos campos son poblados por `CFGBuilder` durante la construcción y son consumidos por `ReachingDefinitionsComputer` y `DDGBuilder`.

#### Clase interna: CFGEdge

```java
public static class CFGEdge {
    public CFGNode getFrom()
    public CFGNode getTo()
    public String  getLabel()   // "True", "False" o ""
}
```

---

### 4. PostDominatorComputer

Computa los Post-Dominadores (PDOM) usando el algoritmo iterativo de punto fijo sobre el CFG reverso.

#### Algoritmo

```
PDOM(exit) = {exit}
Para todo nodo x ≠ exit:  PDOM(x) = N  (todos los nodos)

Hasta convergencia:
  Para todo nodo x ≠ exit:
    PDOM(x) = {x} ∪ (⋂ PDOM(s)  para todo sucesor s de x en el CFG)
```

#### Métodos principales

```java
public void                       compute()
public Set<CFGNode>               getPdom(CFGNode)
public Map<CFGNode, Set<CFGNode>> getAllPdom()
public void                       printPdom()
```

---

### 5. PostDominatorTreeBuilder

Construye el PDT a partir de los conjuntos PDOM, implementando el algoritmo **BuildDtree**.

#### Algoritmo BuildDtree

```
1. EXIT es la raíz del PDT
2. Poner EXIT en la cola Q; D(n) = PDOM(n) − {n} para cada nodo n
3. Mientras Q no esté vacía:
     m = siguiente en Q
     Para cada nodo n con D(n) no vacío que contenga m:
       D(n) = D(n) − {m}
       Si D(n) quedó vacío:
         n es hijo de m en el PDT → agregar n a Q
```

#### Métodos principales

```java
public void          build()
public CFGNode       getRoot()
public List<CFGNode> getChildren(CFGNode)
public CFGNode       getParent(CFGNode)
public void          printTree()
```

---

### 6. CDGBuilder

Construye el CDG a partir del CFG y el PDT, implementando el algoritmo de **Ferrante, Ottenstein y Warren (1987)**.

#### ¿Qué es el CDG?

Una arista `A → Y` en el CDG significa que la ejecución de `Y` depende de la rama que toma `A`. Los nodos sin aristas CDG entrantes son de **ejecución incondicional**.

#### Algoritmo

```
Para cada arista (A → B) en el CFG:
  L = LCA(A, B) en el PDT
  Para cada nodo Y en el camino de B hasta L (sin incluir L):
    Y es control-dependiente de A  →  arista CDG A → Y [etiqueta de rama]
  Si L == A (bucles while):
    A es control-dependiente de sí mismo → self-loop A → A
```

#### Métodos principales

```java
public void                            build()
public Map<CFGNode, List<CDGEdge>>     getAllEdges()
public List<CDGEdge>                   getIncomingDependencies(CFGNode)
public void                            printCDG()
```

#### Clase interna: CDGEdge

```java
public static class CDGEdge {
    public CFGNode getPredicate()
    public CFGNode getDependent()
    public String  getLabel()       // "True", "False" o ""
}
```

---

### 7. Definition y VariableExtractor

**`Definition`** representa una definición alcanzante: el par `(nodo, variable)`. Se denota `nN:var` (e.g., `n2:x` = el nodo 2 define `x`). Implementa `equals` y `hashCode` para usarse en conjuntos.

**`VariableExtractor`** recorre un `ExprNode` del AST y retorna el conjunto de variables referenciadas. Soporta `VariableNode`, `BinaryOpNode` y `UnaryOpNode`. Es usado por `CFGBuilder` para poblar `usedVars` en cada nodo.

```java
public static Set<String> extract(ExprNode expr)
```

---

### 8. ReachingDefinitionsComputer

Computa las **definiciones alcanzantes** para cada nodo del CFG mediante el algoritmo iterativo **forward-may**.

#### Algoritmo

```
GEN(n)  = { (n, v) }  si n define la variable v
KILL(n) = todas las definiciones de v en otros nodos, si n define v
IN(n)   = ⋃ OUT(pred(n))
OUT(n)  = GEN(n) ∪ (IN(n) − KILL(n))

Inicialización: OUT(n) = GEN(n), IN(n) = {}
Iterar hasta que OUT(n) no cambie para ningún nodo.
```

#### Métodos principales

```java
public void                              compute()
public Map<CFGNode, Set<Definition>>     getInSets()
public Map<CFGNode, Set<Definition>>     getOutSets()
public Map<CFGNode, Set<Definition>>     getGenSets()
public Map<CFGNode, Set<Definition>>     getKillSets()
public void                              printReachingDefs()
```

---

### 9. DDGBuilder

Construye el **Grafo de Dependencias de Datos (DDG)** a partir de los pares definición-uso.

#### Construcción

Para cada nodo `U` y cada variable `v` que `U` usa:
- Si existe una definición `D` tal que `(D, v) ∈ IN(U)` → arista DDG `D → U` etiquetada con `v`.

Un par `(D, U)` significa: la definición de `v` en `D` alcanza a `U` y `U` la usa.

#### Métodos principales

```java
public void          build()
public List<DDGEdge> getEdges()
public List<DDGEdge> getEdgesFrom(CFGNode)
public void          printDDG()
```

#### Clase interna: DDGEdge

```java
public static class DDGEdge {
    public CFGNode getFrom()      // nodo que define la variable
    public CFGNode getTo()        // nodo que usa la variable
    public String  getVariable()  // nombre de la variable
}
```

---

### 10. ProgramSlicer

Computa el **backward program slice** a partir de un nodo criterio, recorriendo el PDG (CDG + DDG) hacia atrás mediante BFS.

#### Algoritmo

```
slice = { criterion }
worklist = { criterion }

Mientras worklist no esté vacía:
  n = extraer de worklist
  Para cada arista CDG A → n:
    Si A ∉ slice: agregar A a slice y worklist
  Para cada arista DDG D → n:
    Si D ∉ slice: agregar D a slice y worklist
```

El slice resultante es el conjunto mínimo de nodos que pueden afectar el valor o la ejecución del nodo criterio.

#### Reconstrucción del CFG del slice

Para construir el CFG reducido al slice, el método `buildSlicedEdges` hace BFS hacia adelante desde cada nodo del slice: si el sucesor CFG inmediato no está en el slice, continúa explorando hasta encontrar el primer nodo del slice alcanzable (heredando la etiqueta de la arista de salida). Esto reconecta directamente los nodos del slice aunque haya instrucciones intermedias eliminadas.

#### Métodos principales

```java
public Set<CFGNode>         slice(CFGNode criterion)
public CFGNode              findById(int id)
public List<CFGNode>        getSliceOrdered(Set<CFGNode>)
public List<CFGNode.CFGEdge> buildSlicedEdges(Set<CFGNode>)
public void                 printSlice(CFGNode criterion, Set<CFGNode> slice)
```

---

### 11. DOTExporter

Exporta todos los grafos al formato DOT de Graphviz. Clase de utilidad con métodos estáticos.

#### Métodos principales

```java
public static String export(List<CFGNode> nodes)
public static String exportWithPdom(List<CFGNode> nodes, Map<CFGNode, Set<CFGNode>> pdomMap)
public static String exportPDT(PostDominatorTreeBuilder pdt)
public static String exportCDG(List<CFGNode> nodes, CDGBuilder cdg)
public static String exportDDG(List<CFGNode> nodes, DDGBuilder ddg)
public static String exportSliceHighlighted(List<CFGNode> allNodes, Set<CFGNode> slice, CFGNode criterion)
public static String exportSlicedCFG(List<CFGNode> allNodes, Set<CFGNode> slice, CFGNode criterion, ProgramSlicer slicer)
```

#### Representación visual por tipo de nodo

| Tipo | Atributos DOT |
|---|---|
| `ENTRY` | `shape=circle`, `fillcolor=green` |
| `EXIT` | `shape=doublecircle`, `fillcolor=red` |
| `STATEMENT` | `shape=box, style="rounded,filled"`, `fillcolor=lightblue` |
| `CONDITION` | `shape=diamond, style=filled`, `fillcolor=lightyellow` |

#### Convención de colores para slices

| Rol del nodo | Color |
|---|---|
| Criterio del slice | Naranja (`#FF6600`), borde grueso |
| En el slice | Dorado (`#FFD700`) |
| Fuera del slice | Gris claro (`#DDDDDD`), texto atenuado |

#### Representación visual de aristas

| Grafo | Estilo | Etiqueta `"True"` | Etiqueta `"False"` |
|---|---|---|---|
| CFG / PDT | Sólida | Verde oscuro | Rojo |
| CDG | Punteada (`dashed`) | Verde oscuro | Rojo |
| DDG | Punteada naranja (`darkorange`) | — | — |
| Slice highlighted | Sólida negra (entre nodos del slice) / gris punteada (resto) | — | — |

---

## Archivos de prueba

Los archivos de entrada para el análisis CFG/PDG se encuentran en `src/main/resources/cfg/`:

| Archivo | Descripción |
|---|---|
| `cfg/test_cfg.txt` | Programa simple con `if/else` |
| `cfg/test_cfg_while.txt` | Programa con bucle `while` |
| `cfg/test_cfg_completo.txt` | Programa con `if` anidado dentro de `while` |

Los archivos `.dot` generados por el pipeline se guardan en la misma carpeta `cfg/`.

El resto de los tests del compilador (semántica, errores, código assembler) se encuentran en `src/main/resources/`:

| Archivo | Descripción |
|---|---|
| `test_bueno.txt` | Programa con múltiples estructuras de control |
| `test_completo_exitoso.txt` | Programa con todas las construcciones soportadas |
| `test_integral.txt` | Prueba integral del compilador |
| `test_expresiones_complejas.txt` | Expresiones aritméticas y booleanas complejas |
| `test_warning_no_inicializada.txt` | Variables usadas sin inicializar |
| `test_error_tipos.txt` | Errores de tipos |
| `test_error_duplicada.txt` | Variables declaradas más de una vez |
| `test_error_no_declarada.txt` | Variables usadas sin declarar |
| `test_error_ambitos.txt` | Variables fuera de su ámbito |
| `test_error_return.txt` | Return con tipo incorrecto |
| `test_division_cero.txt` | División por cero |
| `test_malo.txt` | Programa con múltiples errores semánticos |
| `test_scopes.txt` | Prueba de scoping de variables |
