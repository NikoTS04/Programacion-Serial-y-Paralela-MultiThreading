import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

/**
 * Generador de Datasets Sintéticos Multidimensionales.
 * 
 * Cumple con las restricciones:
 * - Cero librerías externas.
 * - Sin uso de ArrayList ni colecciones dinámicas.
 * - Consumo de memoria constante O(1): streaming directo a disco mediante buffers.
 * - Soporte para configuración de N, n, rango de valores y semilla pseudoaleatoria.
 * 
 * Responsable: Integrante 2
 */
public class DatasetGenerator {

    private static final int BUFFER_SIZE = 65536; // 64 KB de buffer para I/O eficiente

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args.length < 3) {
            mostrarUso();
            return;
        }

        try {
            int N = Integer.parseInt(args[0]);
            int n = Integer.parseInt(args[1]);
            String rutaSalida = args[2];
            double minVal = 0.0;
            double maxVal = 100.0;
            Long seed = null;

            if (args.length >= 4) {
                minVal = Double.parseDouble(args[3]);
            }
            if (args.length >= 5) {
                maxVal = Double.parseDouble(args[4]);
            }
            if (args.length >= 6) {
                seed = Long.parseLong(args[5]);
            }

            if (N < 2) {
                System.err.println("Error: N debe ser mayor o igual a 2.");
                return;
            }
            if (n < 1) {
                System.err.println("Error: n (dimensiones) debe ser mayor o igual a 1.");
                return;
            }
            if (minVal >= maxVal) {
                System.err.println("Error: minVal debe ser estrictamente menor que maxVal.");
                return;
            }

            generarDataset(N, n, rutaSalida, minVal, maxVal, seed);

        } catch (NumberFormatException e) {
            System.err.println("Error de formato numérico en los argumentos: " + e.getMessage());
            mostrarUso();
        } catch (IOException e) {
            System.err.println("Error de I/O al generar el dataset: " + e.getMessage());
        }
    }

    /**
     * Genera el archivo del dataset escribiendo secuencialmente cada punto en disco.
     */
    public static void generarDataset(int N, int n, String rutaSalida, double minVal, double maxVal, Long seed) throws IOException {
        File archivo = new File(rutaSalida);
        File carpetaPadre = archivo.getParentFile();
        if (carpetaPadre != null && !carpetaPadre.exists()) {
            carpetaPadre.mkdirs();
        }

        System.out.println("==================================================");
        System.out.println("          GENERADOR DE DATASETS (Java)            ");
        System.out.println("==================================================");
        System.out.printf("Cantidad de puntos (N)   : %,d%n", N);
        System.out.printf("Dimensiones por punto (n): %,d%n", n);
        System.out.printf("Rango de valores         : [%.2f, %.2f]%n", minVal, maxVal);
        System.out.println("Archivo de salida        : " + archivo.getAbsolutePath());
        System.out.println("Semilla (seed)           : " + (seed != null ? seed : "Aleatoria"));
        System.out.println("Estrategia I/O           : Streaming con buffer de 64 KB");
        System.out.println("--------------------------------------------------");

        Random random = (seed != null) ? new Random(seed) : new Random();
        double rango = maxVal - minVal;

        long tiempoInicio = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo), BUFFER_SIZE)) {
            // 1. Escribir encabezado con N y n
            writer.write(N + " " + n);
            writer.newLine();

            // 2. Reutilizar StringBuilder para formatear cada línea sin crear millones de objetos
            StringBuilder sb = new StringBuilder(Math.max(64, n * 12));

            for (int i = 0; i < N; i++) {
                sb.setLength(0);

                for (int k = 0; k < n; k++) {
                    double valor = minVal + (random.nextDouble() * rango);
                    // Redondear a 4 decimales para balance entre precisión y tamaño en disco
                    double valorRedondeado = Math.round(valor * 10000.0) / 10000.0;
                    sb.append(valorRedondeado);

                    if (k < n - 1) {
                        sb.append(' ');
                    }
                }

                writer.write(sb.toString());
                writer.newLine();

                // Notificación visual de progreso para datasets masivos (cada 500,000 puntos)
                if ((i + 1) % 500000 == 0) {
                    System.out.printf("  Progreso: %,d / %,d puntos generados...%n", (i + 1), N);
                }
            }

            writer.flush();
        }

        long tiempoFin = System.currentTimeMillis();
        long duracionMs = tiempoFin - tiempoInicio;
        double segundos = duracionMs / 1000.0;
        long bytesTotales = archivo.length();
        double megabytes = bytesTotales / (1024.0 * 1024.0);

        System.out.println("--------------------------------------------------");
        System.out.println("Generación completada con éxito.");
        System.out.printf("Tiempo total             : %.3f s (%d ms)%n", segundos, duracionMs);
        System.out.printf("Tamaño del archivo       : %.2f MB (%,d bytes)%n", megabytes, bytesTotales);
        if (segundos > 0) {
            System.out.printf("Velocidad de generación  : %,.0f puntos/segundo%n", (N / segundos));
        }
        System.out.println("==================================================");
    }

    private static void mostrarUso() {
        System.out.println("Uso:");
        System.out.println("  java DatasetGenerator <N> <n> <ruta_salida> [minVal] [maxVal] [seed]");
        System.out.println();
        System.out.println("Parámetros:");
        System.out.println("  N            : Cantidad total de puntos (entero >= 2)");
        System.out.println("  n            : Dimensionalidad de los puntos (entero >= 1)");
        System.out.println("  ruta_salida  : Ruta del archivo resultante (ej. datasets/dataset_1000_3.txt)");
        System.out.println("  minVal       : (Opcional) Valor mínimo permitido (por defecto: 0.0)");
        System.out.println("  maxVal       : (Opcional) Valor máximo permitido (por defecto: 100.0)");
        System.out.println("  seed         : (Opcional) Semilla para reproducibilidad científica");
        System.out.println();
        System.out.println("Ejemplo:");
        System.out.println("  java DatasetGenerator 1000 3 datasets/dataset_1000_3.txt 0.0 100.0 42");
    }
}
