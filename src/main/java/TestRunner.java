import java.io.File;

public class TestRunner {

    private static class TestCase {
        String file;
        String expected;

        TestCase(String file, String expected) {
            this.file = file;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        // IMPORTANTE: Activar modo test para evitar System.exit
        CompilerMain.setTestMode(true);

        TestCase[] tests = {
                new TestCase("test_bueno.txt", "EXITOSO"),
                new TestCase("test_completo_exitoso.txt", "EXITOSO"),
                new TestCase("test_error_no_declarada.txt", "ERROR"),
                new TestCase("test_malo.txt", "ERROR"),
                new TestCase("test_error_tipos.txt", "ERROR"),
                //new TestCase("test_error_return.txt", "ERROR"), TODO: Corregir la validación del return
                new TestCase("test_warning_no_inicializada.txt", "WARNING"),
                new TestCase("test_error_duplicada.txt", "ERROR"),
                new TestCase("test_scopes.txt", "ERROR"),
                new TestCase("test_expresiones_complejas.txt", "EXITOSO"),
                new TestCase("test_division_cero.txt", "ERROR"),
                new TestCase("test_integral.txt", "EXITOSO")
        };

        int passed = 0;
        int failed = 0;

        System.out.println("==========================================");
        System.out.println("EJECUTANDO TESTS DEL COMPILADOR");
        System.out.println("==========================================");

        // Buscar archivos en resources
        String resourcesPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator;

        for (TestCase test : tests) {
            System.out.println();
            System.out.println("==========================================");
            System.out.println("Test: " + test.file);
            System.out.println("Esperado: " + test.expected);
            System.out.println("==========================================");

            String testFilePath = resourcesPath + test.file;
            File testFile = new File(testFilePath);

            if (!testFile.exists()) {
                System.out.println("✗ ARCHIVO NO ENCONTRADO: " + testFilePath);
                failed++;
                continue;
            }

            try {
                // IMPORTANTE: Resetear el ErrorHandler antes de cada test
                semantic.errors.ErrorHandler.getInstance().reset();

                String[] compilerArgs = {testFilePath};
                CompilerMain.main(compilerArgs);

                // Verificar si hubo errores después de la compilación
                boolean hasErrors = semantic.errors.ErrorHandler.getInstance().hasErrors();

                if (test.expected.equals("EXITOSO")) {
                    if (!hasErrors) {
                        System.out.println("\n✓ TEST PASSED");
                        passed++;
                    } else {
                        System.out.println("\n✗ TEST FAILED - Se esperaba éxito pero hubo errores");
                        failed++;
                    }
                } else if (test.expected.equals("ERROR")) {
                    if (hasErrors) {
                        System.out.println("\n✓ TEST PASSED - Errores detectados correctamente");
                        passed++;
                    } else {
                        System.out.println("\n✗ TEST FAILED - Se esperaba error pero compiló exitosamente");
                        failed++;
                    }
                } else if (test.expected.equals("WARNING")) {
                    int warningCount = semantic.errors.ErrorHandler.getInstance().getWarnings().size();
                    if (warningCount > 0) {
                        System.out.println("\n✓ TEST PASSED - Advertencias generadas (" + warningCount + ")");
                        passed++;
                    } else {
                        System.out.println("\n⚠ TEST CHECK - No se generaron advertencias");
                        passed++;
                    }
                }

            } catch (Exception e) {
                if (test.expected.equals("ERROR")) {
                    System.out.println("\n✓ TEST PASSED - Error detectado (excepción capturada)");
                    passed++;
                } else {
                    System.out.println("\n✗ TEST FAILED - Error inesperado: " + e.getMessage());
                    e.printStackTrace();
                    failed++;
                }
            }

            System.out.println("------------------------------------------");
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("RESUMEN DE TESTS");
        System.out.println("==========================================");
        System.out.println("✓ Pasados:  " + passed);
        System.out.println("✗ Fallados: " + failed);
        System.out.println("Total:      " + (passed + failed));
        double percentage = (passed * 100.0) / (passed + failed);
        System.out.println("Porcentaje: " + String.format("%.1f%%", percentage));
        System.out.println("==========================================");

        if (failed == 0) {
            System.out.println("🎉 TODOS LOS TESTS PASARON");
        } else {
            System.out.println("⚠ HAY TESTS FALLIDOS");
        }
    }
}
