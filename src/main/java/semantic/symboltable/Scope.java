package semantic.symboltable;

import java.util.*;

/**
 * Representa un scope (ámbito) individual en la tabla de símbolos.
 * Cada scope puede contener símbolos y tener un scope padre y scopes hijos.
 */
public class Scope {
    private String scopeName;
    private Map<String, SymbolEntry> symbols;
    private Scope parent;
    private List<Scope> children;
    private int level;

    public Scope(String name, Scope parent) {
        this.scopeName = name;
        this.symbols = new LinkedHashMap<>(); // Mantiene orden de inserción
        this.parent = parent;
        this.children = new ArrayList<>();
        this.level = parent != null ? parent.level + 1 : 0;

        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void addChild(Scope child) {
        children.add(child);
    }

    /**
     * Declara un símbolo en este scope
     * @return true si se declaró exitosamente, false si ya existía
     */
    public boolean declare(String name, String type, int line, int column) {
        if (symbols.containsKey(name)) {
            return false;
        }
        SymbolEntry entry = new SymbolEntry(name, type, line, column);
        entry.setScope(scopeName);
        symbols.put(name, entry);
        return true;
    }

    /**
     * Declara un símbolo con valor inicial
     */
    public boolean declare(String name, String type, Object value, int line, int column) {
        if (symbols.containsKey(name)) {
            return false;
        }
        SymbolEntry entry = new SymbolEntry(name, type, value, line, column);
        entry.setScope(scopeName);
        symbols.put(name, entry);
        return true;
    }

    /**
     * Busca un símbolo solo en este scope
     */
    public SymbolEntry lookup(String name) {
        return symbols.get(name);
    }

    /**
     * Verifica si existe un símbolo en este scope
     */
    public boolean contains(String name) {
        return symbols.containsKey(name);
    }

    /**
     * Obtiene todos los nombres de símbolos en este scope
     */
    public Set<String> getSymbolNames() {
        return new LinkedHashSet<>(symbols.keySet());
    }

    /**
     * Obtiene todas las entradas de símbolos
     */
    public Collection<SymbolEntry> getSymbols() {
        return symbols.values();
    }

    /**
     * Obtiene el número de símbolos en este scope
     */
    public int getSymbolCount() {
        return symbols.size();
    }

    // Getters
    public String getScopeName() {
        return scopeName;
    }

    public Scope getParent() {
        return parent;
    }

    public List<Scope> getChildren() {
        return new ArrayList<>(children);
    }

    public int getLevel() {
        return level;
    }

    public boolean isGlobal() {
        return parent == null;
    }

    @Override
    public String toString() {
        return String.format("Scope{name='%s', level=%d, symbols=%d, children=%d}",
                scopeName, level, symbols.size(), children.size());
    }
}