package semantic.symboltable;

/**
 * Tabla de símbolos principal que maneja múltiples scopes anidados.
 * Proporciona operaciones para declarar, buscar y gestionar símbolos
 * a través de diferentes niveles de scope.
 */
public class SymbolTable {
    private Scope globalScope;
    private Scope currentScope;
    private int scopeCounter;

    public SymbolTable() {
        this.globalScope = new Scope("global", null);
        this.currentScope = globalScope;
        this.scopeCounter = 0;
    }

    /**
     * Entra a un nuevo scope con el nombre especificado
     */
    public void enterScope(String scopeName) {
        String fullName = scopeName + "_" + (++scopeCounter);
        currentScope = new Scope(fullName, currentScope);
    }

    /**
     * Sale del scope actual y retorna al scope padre
     * @return true si se salió exitosamente, false si ya está en el scope global
     */
    public boolean exitScope() {
        if (currentScope.getParent() == null) {
            return false;
        }
        currentScope = currentScope.getParent();
        return true;
    }

    /**
     * Declara una variable en el scope actual sin valor inicial
     */
    public boolean declare(String name, String type) {
        return currentScope.declare(name, type, -1, -1);
    }

    /**
     * Declara una variable en el scope actual con información de línea y columna
     */
    public boolean declare(String name, String type, int line, int column) {
        return currentScope.declare(name, type, line, column);
    }

    /**
     * Declara una variable en el scope actual con valor inicial
     */
    public boolean declare(String name, String type, Object value, int line, int column) {
        return currentScope.declare(name, type, value, line, column);
    }

    /**
     * Busca un símbolo en el scope actual y en los scopes padre (búsqueda jerárquica)
     */
    public SymbolEntry lookup(String name) {
        Scope scope = currentScope;
        while (scope != null) {
            SymbolEntry entry = scope.lookup(name);
            if (entry != null) {
                return entry;
            }
            scope = scope.getParent();
        }
        return null;
    }

    /**
     * Busca un símbolo solo en el scope actual (búsqueda local)
     */
    public SymbolEntry lookupLocal(String name) {
        return currentScope.lookup(name);
    }

    /**
     * Verifica si un símbolo existe (búsqueda jerárquica)
     */
    public boolean exists(String name) {
        return lookup(name) != null;
    }

    /**
     * Verifica si un símbolo existe en el scope actual (búsqueda local)
     */
    public boolean existsLocal(String name) {
        return lookupLocal(name) != null;
    }

    /**
     * Asigna un valor a una variable existente
     * @return true si se asignó exitosamente, false si la variable no existe
     */
    public boolean assign(String name, Object value) {
        SymbolEntry entry = lookup(name);
        if (entry != null) {
            entry.setValue(value);
            return true;
        }
        return false;
    }

    /**
     * Obtiene el valor de una variable
     * @return el valor de la variable, o null si no existe
     */
    public Object getValue(String name) {
        SymbolEntry entry = lookup(name);
        return entry != null ? entry.getValue() : null;
    }

    /**
     * Obtiene el tipo de una variable
     * @return el tipo de la variable, o null si no existe
     */
    public String getType(String name) {
        SymbolEntry entry = lookup(name);
        return entry != null ? entry.getType() : null;
    }

    /**
     * Verifica si una variable está inicializada
     */
    public boolean isInitialized(String name) {
        SymbolEntry entry = lookup(name);
        return entry != null && entry.isInitialized();
    }

    // Getters para información del scope
    public Scope getCurrentScope() {
        return currentScope;
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    public int getCurrentScopeLevel() {
        return currentScope.getLevel();
    }

    /**
     * Imprime la tabla de símbolos completa de forma jerárquica
     */
    public void printSymbolTable() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SYMBOL TABLE");
        System.out.println("=".repeat(50));
        printScope(globalScope, 0);
        System.out.println("=".repeat(50));
    }

    private void printScope(Scope scope, int indent) {
        String indentation = "  ".repeat(indent);
        System.out.println(indentation + "├─ " + scope);

        for (SymbolEntry entry : scope.getSymbols()) {
            System.out.println(indentation + "│  └─ " + entry);
        }

        for (Scope child : scope.getChildren()) {
            printScope(child, indent + 1);
        }
    }

    /**
     * Imprime estadísticas de la tabla de símbolos
     */
    public void printStatistics() {
        int totalSymbols = countSymbols(globalScope);
        int totalScopes = countScopes(globalScope);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("SYMBOL TABLE STATISTICS");
        System.out.println("=".repeat(50));
        System.out.println("Total scopes:        " + totalScopes);
        System.out.println("Total symbols:       " + totalSymbols);
        System.out.println("Current scope:       " + currentScope.getScopeName());
        System.out.println("Current scope level: " + currentScope.getLevel());
        System.out.println("=".repeat(50));
    }

    private int countSymbols(Scope scope) {
        int count = scope.getSymbolCount();
        for (Scope child : scope.getChildren()) {
            count += countSymbols(child);
        }
        return count;
    }

    private int countScopes(Scope scope) {
        int count = 1;
        for (Scope child : scope.getChildren()) {
            count += countScopes(child);
        }
        return count;
    }

    /**
     * Limpia completamente la tabla de símbolos
     */
    public void clear() {
        this.globalScope = new Scope("global", null);
        this.currentScope = globalScope;
        this.scopeCounter = 0;
    }
}