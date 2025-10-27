## Universidad Nacional de Villa Mercedes - Escuela de Ingeniería y Ciencias Ambientales
# Proyecto de Compiladores
### Alumnos:
- Molina, Santiago Manuel
- Quiroga Stek, Esteban Eduardo
- Sanabria Quattrocchio, Pablo Emiliano
- Videla Del Castillo, Florencia Fatima
  
# Documentación del Compilador

## 📋 Tabla de Contenidos
1. [Introducción](##introduccion)
2. [Instalación y Configuración](#instalación-y-configuración)
3. [Arquitectura](#arquitectura)
4. [Componentes](#componentes)

---

## Introduccion

### ¿Qué es este proyecto?

Este es un **compilador educativo** desarrollado en Java que implementa un compilador completo con las tres fases fundamentales: análisis léxico, análisis sintáctico y análisis semántico. El compilador genera código assembler x86-64 a partir de un lenguaje simple de alto nivel.

### Características principales

- **Análisis Léxico**: Tokenización usando JFlex
- **Análisis Sintáctico**: Parsing usando CUP (Java Cup) generando un Árbol Sintáctico Abstracto (AST)
- **Análisis Semántico**: Validación de tipos, scopes y variables
- **Generación de Código**: Producción de código assembler x86-64
- **Manejo de Errores**: Sistema completo con errores léxicos, sintácticos, semánticos y de tipos
- **Suite de Tests**: 12 casos de prueba cobriendo diferentes escenarios

### Lenguaje soportado

El compilador soporta un lenguaje simple basado en C con:
- Tipos: `int`, `bool`, `void`
- Operadores aritméticos: `+`, `-`, `*`, `/`
- Operadores lógicos: `&&`, `||`, `!`
- Operadores de comparación: `==`, `<`, `>`
- Control de flujo: `if/else`, `while`
- Funciones: parámetros y retorno
- Variables locales y globales con inicialización

### Ejemplo de código compilable

```c
int main() {
    int x = 10;
    int y = 20;
    int resultado;
    
    if (x < y) {
        resultado = x + y;
    } else {
        resultado = x - y;
    }
    
    return resultado;
}
```

### Requisitos previos

- Java 11 o superior
- Maven 3.6+
- Git

### Inicio rápido

```bash
# Clonar el repositorio
git clone <url-repositorio>
cd compiler

# Compilar el proyecto
mvn clean compile

# Ejecutar los tests
mvn test

# Compilar un archivo fuente
java -cp target/classes CompilerMain src/main/resources/test_bueno.txt
```

---

## Instalación y Configuración

### Instalación paso a paso

#### 1. Preparar el ambiente

```bash
# Verificar versión de Java
java -version
javac -version

# Verificar Maven
mvn --version
```

#### 2. Configurar variables de entorno

**En Linux/Mac:**
```bash
export JAVA_HOME=/path/to/jdk
export PATH=$JAVA_HOME/bin:$PATH
```

**En Windows:**
```
JAVA_HOME=C:\Program Files\Java\jdk-17
PATH=%JAVA_HOME%\bin;%PATH%
```

#### 3. Clonar y preparar el proyecto

```bash
git clone <url-repositorio> compiler
cd compiler
```

#### 4. Generar archivos del parser y lexer

```bash
# Generar lexer (Lexer.java)
jflex src/main/java/jflex/lexer.flex

# Generar parser (MiParser.java y sym.java)
java -jar <ruta>/java_cup-11b.jar -parser MiParser src/main/java/cup/parser.cup
```

Nota: Si usas Maven, estos pasos pueden estar automatizados en el `pom.xml`

#### 5. Compilar el proyecto

```bash
mvn clean compile
```

#### 6. Verificar la instalación

```bash
# Ejecutar los tests de prueba
mvn test

# Si todo pasa, ejecutar ejemplo básico
java -cp target/classes CompilerMain src/main/resources/test_bueno.txt
```

### Estructura del proyecto

```
compiler/
├── pom.xml                          # Configuración Maven
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── CompilerMain.java    # Punto de entrada
│   │   │   ├── TestRunner.java      # Suite de tests
│   │   │   ├── Lexer.java           # Generado (JFlex)
│   │   │   ├── ast/                 # Árbol sintáctico
│   │   │   ├── cup/                 # Parser (CUP)
│   │   │   ├── codegen/             # Generador de código
│   │   │   ├── semantic/            # Análisis semántico
│   │   │   └── jflex/               # Definición del lexer
│   │   └── resources/               # Archivos de prueba
│   └── test/                        # Tests unitarios
└── target/                          # Compilados y JARs

```

---

## Arquitectura

### Diagrama de componentes

```
┌─────────────────────────────────────────────────────┐
│              CompilerMain (Punto de entrada)         │
└────┬────────────────────────────────────────────────┘
     │
     ├─────────────────────────────────────────────┐
     │                                             │
     ▼                                             │
┌─────────────┐    Léxico                        │
│   Lexer     │────────────────────┐             │
│ (JFlex)     │                    │             │
└─────────────┘                    │ Tokens      │
                                   ▼             │
                            ┌───────────────┐    │
                            │  MiParser     │◄──┤ Sintáctico
                            │   (CUP)       │    │
                            └───────┬───────┘    │
                                    │ AST        │
                                    ▼            │
                          ┌──────────────────┐   │
                          │ SemanticAnalyzer│◄──┤ Semántico
                          │  (Visitor)       │   │
                          └────────┬─────────┘   │
                                   │             │
                          ┌────────▼────────┐   │
                          │  SymbolTable    │   │
                          │  (Scopes)       │   │
                          └────────────────┘   │
                                   │            │
                          ┌────────▼────────┐   │
                          │CodeGenerator    │◄──┤ Codegen
                          │  (x86-64)       │   │
                          └────────┬────────┘   │
                                   │            │
                                   ▼            │
                          ┌────────────────┐   │
                          │  .asm file     │   │
                          └────────────────┘   │
                                               │
                    ┌──────────────────────────┘
                    ▼
            ┌──────────────┐
            │ ErrorHandler │
            │  (Singleton) │
            └──────────────┘
```

### Fases del compilador

#### Fase 1: Análisis Léxico (Lexer)
- **Componente**: `Lexer.java` (generado desde `lexer.flex`)
- **Responsabilidad**: Convertir el código fuente en tokens
- **Errores**: Caracteres ilegales
- **Salida**: Stream de tokens (símbolo léxico + valor)

#### Fase 2: Análisis Sintáctico (Parser)
- **Componente**: `MiParser.java` (generado desde `parser.cup`)
- **Responsabilidad**: Validar estructura gramatical y construir AST
- **Errores**: Sintaxis inválida (tokens fuera de orden)
- **Salida**: Árbol Sintáctico Abstracto (AST)

#### Fase 3: Análisis Semántico
- **Componente**: `SemanticAnalyzer.java` (implementa `ASTVisitor`)
- **Responsabilidad**: Validar tipos, scopes, declaraciones
- **Errores**: Variables no declaradas, incompatibilidad de tipos
- **Salida**: AST anotado con información de tipos

#### Fase 4: Generación de Código
- **Componente**: `CodeGenerator.java` (implementa `ASTVisitor`)
- **Responsabilidad**: Traducir AST a assembler x86-64
- **Formato de salida**: Archivo `.asm`

### Patrón de diseño

El compilador utiliza el patrón **Visitor** para recorrer el AST:

- **AST Nodes**: Representan elementos del programa (expresiones, sentencias, etc.)
- **ASTVisitor**: Interfaz que define operaciones sobre nodos
- **Implementaciones**: `SemanticAnalyzer` y `CodeGenerator` implementan operaciones específicas

### Tabla de Símbolos

La `SymbolTable` gestiona:
- **Scopes jerárquicos**: Global, funciones, bloques if/while
- **Entradas de símbolo**: Nombre, tipo, valor, inicialización, ubicación
- **Búsqueda**: Jerárquica (verifica scope actual y padres)
- **Stack frame**: Tracking de offsets para generación de código

---

## Componentes

### 1. CompilerMain

**Punto de entrada principal del compilador.**

```java
// Métodos estáticos principales
public static void main(String[] args)
public static void compileFile(String filename)
public static void compileString(String source)
public static void setTestMode(boolean mode)
```

**Uso:**
```bash
# Compilar archivo
java CompilerMain archivo.txt

# Genera: archivo.asm

# Salida esperada:
# ✓ Código assembler generado en: archivo.asm
# ✓ COMPILACION EXITOSA
```

### 2. Lexer

**Analizador léxico generado con JFlex.**

```java
Lexer lexer = new Lexer(new FileReader(filename));
Symbol token = lexer.next_token();
```

**Tokens soportados:**
- Palabras clave: `int`, `bool`, `void`, `main`, `return`, `if`, `else`, `while`, `true`, `false`
- Operadores: `+`, `-`, `*`, `/`, `&&`, `||`, `!`, `>`, `<`, `==`
- Delimitadores: `(`, `)`, `{`, `}`, `;`, `,`, `=`

### 3. MiParser

**Analizador sintáctico generado con CUP.**

```java
MiParser parser = new MiParser(lexer);
Symbol result = parser.parse();
ProgramNode ast = (ProgramNode) result.value;
```

**Gramática soportada:**
```
program → function_def
function_def → type MAIN ( param_list_opt ) { stmt_list }
stmt_list → stmt+
stmt → declaration ; | assignment ; | return_stmt ; | if_stmt | while_stmt
expr → expr BINOP expr | UNOP expr | NUMBER | ID | TRUE | FALSE | ( expr )
```

### 4. SemanticAnalyzer

**Validación semántica e información de tipos.**

```java
SemanticAnalyzer analyzer = new SemanticAnalyzer();
ast.accept(analyzer);

SymbolTable symbolTable = analyzer.getSymbolTable();
boolean hasErrors = analyzer.hasErrors();
```

**Validaciones:**
- Variables declaradas antes de usar
- Compatibilidad de tipos en asignaciones
- Tipos correctos en operadores
- Inicialización de variables
- Scopes: variables fuera de alcance

### 5. CodeGenerator

**Generador de código assembler x86-64.**

```java
CodeGenerator codegen = new CodeGenerator(symbolTable);
String asmCode = (String) ast.accept(codegen);
```

**Características:**
- Prologue/Epilogue de funciones
- Allocación dinámica de stack
- Manejo de parámetros en registros
- Generación de labels para control de flujo
- Operaciones x86-64 optimizadas

### 6. SymbolTable

**Gestión de símbolos con scopes jerárquicos.**

```java
SymbolTable table = new SymbolTable();

// Operaciones
table.declare(name, type, value, line, column);
SymbolEntry entry = table.lookup(name);
table.enterScope("scope_name");
table.exitScope();
table.assign(name, value);
```

**SymbolEntry contiene:**
- `name`: Nombre del símbolo
- `type`: Tipo (int, bool, void)
- `value`: Valor actual
- `isInitialized`: Bandera de inicialización
- `address`: Dirección en memoria (para codegen)
- `stackOffset`: Offset en stack frame

### 7. ErrorHandler

**Sistema singleton de gestión de errores.**

```java
ErrorHandler handler = ErrorHandler.getInstance();
handler.addSemanticError(line, column, message);
handler.addTypeError(line, column, message);
handler.addWarning(line, column, message);

if (handler.hasErrors()) {
    // Manejar errores
}
handler.printSummary();
```

**Tipos de errores:**
- **LexicalError**: Caracteres ilegales
- **SyntaxError**: Violación de gramática
- **SemanticError**: Variables no declaradas, scopes
- **TypeError**: Incompatibilidad de tipos

### Ejecución

```bash
# Desde clase main
java -cp target/classes CompilerMain archivo.txt

# Ver resumen de compilación
java -cp target/classes TestRunner

# Archivo de salida
# Si se compila exitosamente: archivo.asm
```

## Resumen

Este compilador es un proyecto educativo completo que implementa las 4 fases principales: análisis léxico, sintáctico, semántico y generación de código. La arquitectura modular basada en patrones de diseño permite fácil extensión y mantenimiento.
