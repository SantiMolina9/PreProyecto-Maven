package semantic.symboltable;

/**
 * Entrada en la tabla de símbolos que representa un símbolo individual.
 * Contiene información sobre nombre, tipo, valor, estado de inicialización
 * y ubicación en el código fuente.
 */
public class SymbolEntry {
    private String name;
    private String type;
    private Object value;
    private boolean isInitialized;
    private int declarationLine;
    private int declarationColumn;
    private String scope;

    public SymbolEntry(String name, String type, int line, int column) {
        this.name = name;
        this.type = type;
        this.value = null;
        this.isInitialized = false;
        this.declarationLine = line;
        this.declarationColumn = column;
    }

    public SymbolEntry(String name, String type, Object value, int line, int column) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.isInitialized = (value != null);
        this.declarationLine = line;
        this.declarationColumn = column;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public int getDeclarationLine() {
        return declarationLine;
    }

    public int getDeclarationColumn() {
        return declarationColumn;
    }

    public String getScope() {
        return scope;
    }

    // Setters
    public void setValue(Object value) {
        this.value = value;
        this.isInitialized = true;
    }

    public void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s', type='%s', value=%s, initialized=%b, line=%d, col=%d}",
                name, type, value, isInitialized, declarationLine, declarationColumn);
    }

    /**
     * Formato resumido para debugging
     */
    public String toShortString() {
        return String.format("%s:%s=%s", name, type, isInitialized ? value : "uninitialized");
    }
}