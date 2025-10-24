package semantic.analyzer;

/**
 * Clase responsable de la verificación de tipos en el compilador.
 * Proporciona métodos para verificar compatibilidad de tipos,
 * conversiones implícitas y operaciones válidas entre tipos.
 */
public class TypeChecker {

    /**
     * Verifica si dos tipos son compatibles para asignación
     */
    public static boolean areTypesCompatible(String type1, String type2) {
        if (type1 == null || type2 == null) return false;

        // Tipos iguales son siempre compatibles
        if (type1.equals(type2)) return true;

        // Para operaciones de comparación, permitimos cualquier combinación
        // pero esto puede ser ajustado según tus necesidades
        return true;
    }


    public static int getTypeSize(String type) {
        switch (type) {
            case "int": return 4;     // 32-bit integer
            case "bool": return 1;    // 8-bit boolean
            case "void": return 0;
            default: return 4;        // Default to 4 bytes
        }
    }

    public static String getAssemblyType(String type) {
        switch (type) {
            case "int": return "dd";
            case "bool": return "db";
            default: return "dd";
        }
    }

    /**
     * Verifica si un tipo es válido para operaciones aritméticas
     */
    public static boolean isArithmeticType(String type) {
        return "int".equals(type);
    }

    /**
     * Verifica si un tipo es válido para operaciones booleanas
     */
    public static boolean isBooleanType(String type) {
        return "bool".equals(type);
    }

    /**
     * Verifica si un tipo es un tipo primitivo válido
     */
    public static boolean isPrimitiveType(String type) {
        return "int".equals(type) || "bool".equals(type) || "void".equals(type);
    }

    /**
     * Obtiene el tipo de resultado de una operación binaria aritmética
     */
    public static String getArithmeticResultType(String leftType, String rightType) {
        if (isArithmeticType(leftType) && isArithmeticType(rightType)) {
            return "int";
        }
        return "error";
    }

    /**
     * Obtiene el tipo de resultado de una operación de comparación
     */
    public static String getComparisonResultType(String leftType, String rightType) {
        if (areTypesCompatible(leftType, rightType)) {
            return "bool";
        }
        return "error";
    }

    /**
     * Obtiene el valor por defecto para un tipo dado
     */
    public static Object getDefaultValue(String type) {
        switch (type) {
            case "int":
                return 0;
            case "bool":
                return false;
            case "void":
                return null;
            default:
                return null;
        }
    }

    /**
     * Verifica si un tipo requiere inicialización
     */
    public static boolean requiresInitialization(String type) {
        // En este caso simple, ningún tipo REQUIERE inicialización,
        // pero es buena práctica inicializar
        return false;
    }

    /**
     * Obtiene una descripción legible del tipo
     */
    public static String getTypeDescription(String type) {
        switch (type) {
            case "int":
                return "integer";
            case "bool":
                return "boolean";
            case "void":
                return "void";
            case "error":
                return "error type";
            default:
                return "unknown type: " + type;
        }
    }
}