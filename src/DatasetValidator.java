import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Validador de Integridad y Formato de Datasets.
 * 
 * Permite certificar que un archivo cumple al 100% con la especificación:
 * 1. Encabezado con N y n válidos.
 * 2. Exactamente N líneas de datos posteriores.
 * 3. Exactamente n valores de tipo double por línea delimitados por espacio.
 * 4. Procesamiento en streaming sin cargar el archivo a memoria (O(1) RAM).
 * 
 * Responsable: Integrante 2
 */
public class DatasetValidator {

    private static final int BUFFER_SIZE = 65536; // 64 KB de buffer

    public static void main(String[] args) {
        if (args.length < 1) {
            mostrarUso();
            return;
        }

        String rutaArchivo = args[0];
        validarDataset(rutaArchivo);
    }

    /**
     * Valida la estructura del archivo en disco de forma secuencial.
     */
    public static boolean validarDataset(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists() || !archivo.isFile()) {
            System.err.println("❌ ERROR: El archivo no existe o no es un archivo regular: " + rutaArchivo);
            return false;
        }

        System.out.println("==================================================");
        System.out.println("          VALIDADOR DE DATASETS (Java)            ");
        System.out.println("==================================================");
        System.out.println("Archivo a inspeccionar   : " + archivo.getAbsolutePath());
        System.out.printf("Tamaño del archivo       : %.2f MB (%,d bytes)%n", 
                          archivo.length() / (1024.0 * 1024.0), archivo.length());
        System.out.println("--------------------------------------------------");

        long tiempoInicio = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo), BUFFER_SIZE)) {
            // 1. Leer y validar encabezado
            String encabezado = reader.readLine();
            if (encabezado == null) {
                System.err.println("❌ ERROR [Línea 1]: El archivo está completamente vacío.");
                return false;
            }

            String[] partesEncabezado = encabezado.trim().split("\\s+");
            if (partesEncabezado.length != 2) {
                System.err.println("❌ ERROR [Línea 1]: El encabezado debe contener exactamente 2 valores (N y n).");
                System.err.println("   Contenido recibido: '" + encabezado + "'");
                return false;
            }

            int N_esperado;
            int n_esperado;
            try {
                N_esperado = Integer.parseInt(partesEncabezado[0]);
                n_esperado = Integer.parseInt(partesEncabezado[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ ERROR [Línea 1]: Los valores de N y n en el encabezado deben ser enteros válidos.");
                return false;
            }

            if (N_esperado < 2) {
                System.err.println("❌ ERROR [Línea 1]: N debe ser mayor o igual a 2 (recibido: " + N_esperado + ").");
                return false;
            }
            if (n_esperado < 1) {
                System.err.println("❌ ERROR [Línea 1]: n debe ser mayor o igual a 1 (recibido: " + n_esperado + ").");
                return false;
            }

            System.out.printf("Encabezado detectado     : N = %,d puntos, n = %,d dimensiones%n", N_esperado, n_esperado);
            System.out.println("Iniciando validación en streaming...");

            // 2. Validar cada línea de puntos
            int lineasPuntos = 0;
            String linea;
            long numeroLinea = 1; // 1 fue el encabezado

            while ((linea = reader.readLine()) != null) {
                numeroLinea++;

                // Verificar si hay líneas en blanco intermedias
                if (linea.trim().isEmpty()) {
                    System.err.printf("❌ ERROR [Línea %d]: Línea vacía no permitida.%n", numeroLinea);
                    return false;
                }

                String[] tokens = linea.trim().split("\\s+");
                if (tokens.length != n_esperado) {
                    System.err.printf("❌ ERROR [Línea %d]: Dimensionalidad incorrecta. Esperadas: %d, encontradas: %d.%n",
                                      numeroLinea, n_esperado, tokens.length);
                    System.err.println("   Fragmento: " + truncar(linea, 60));
                    return false;
                }

                // Validar que cada token sea un double parseable
                for (int k = 0; k < tokens.length; k++) {
                    try {
                        Double.parseDouble(tokens[k]);
                    } catch (NumberFormatException e) {
                        System.err.printf("❌ ERROR [Línea %d, Dimensión %d]: Valor numérico no válido: '%s'%n",
                                          numeroLinea, (k + 1), tokens[k]);
                        return false;
                    }
                }

                lineasPuntos++;

                // Notificación visual para datasets grandes
                if (lineasPuntos % 500000 == 0) {
                    System.out.printf("  Verificados: %,d / %,d puntos...%n", lineasPuntos, N_esperado);
                }
            }

            // 3. Validar cantidad total de puntos
            if (lineasPuntos != N_esperado) {
                System.err.printf("❌ ERROR: Discrepancia en cantidad de puntos.%n");
                System.err.printf("   Encabezado declaraba N = %,d puntos, pero el archivo contiene %,d líneas de datos.%n",
                                  N_esperado, lineasPuntos);
                return false;
            }

            long tiempoFin = System.currentTimeMillis();
            long duracionMs = tiempoFin - tiempoInicio;

            System.out.println("--------------------------------------------------");
            System.out.println("✅ VALIDACIÓN EXITOSA: El dataset es 100% íntegro y válido.");
            System.out.printf("Puntos verificados       : %,d%n", lineasPuntos);
            System.out.printf("Dimensiones verificadas  : %,d%n", n_esperado);
            System.out.printf("Tiempo de validación     : %.3f s (%d ms)%n", (duracionMs / 1000.0), duracionMs);
            System.out.println("==================================================");
            return true;

        } catch (IOException e) {
            System.err.println("❌ ERROR de I/O al leer el archivo: " + e.getMessage());
            return false;
        }
    }

    private static String truncar(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }

    private static void mostrarUso() {
        System.out.println("Uso:");
        System.out.println("  java DatasetValidator <ruta_archivo>");
        System.out.println();
        System.out.println("Ejemplo:");
        System.out.println("  java DatasetValidator datasets/dataset_1000_3.txt");
    }
}
