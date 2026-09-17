import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

/**
 * Orquestador principal: ejecuta SerialProcessor y ParallelProcessor sobre
 * el mismo dataset, valida que produzcan resultados idénticos, mide tiempos
 * y guarda la comparación en resultados/resultados.csv.
 *
 * Responsable: Todo el equipo
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java Main <ruta_dataset> [ruta_csv_salida]");
            return;
        }

        String rutaDataset = args[0];
        String rutaCsv = args.length >= 2 ? args[1] : "resultados/resultados.csv";

        int N;
        int n;
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaDataset))) {
            String[] encabezado = reader.readLine().trim().split("\\s+");
            N = Integer.parseInt(encabezado[0]);
            n = Integer.parseInt(encabezado[1]);
        }

        System.out.println("Dataset: " + rutaDataset + " (N=" + N + ", n=" + n + ")");

        System.out.println("Ejecutando SerialProcessor...");
        Resultado resultadoSerial = new SerialProcessor().procesar(rutaDataset, N, n);

        System.out.println("Ejecutando ParallelProcessor...");
        Resultado resultadoParalelo = new ParallelProcessor().procesar(rutaDataset, N, n);

        boolean coincide = resultadoSerial.minimo == resultadoParalelo.minimo
                && resultadoSerial.maximo == resultadoParalelo.maximo;

        double mejora = resultadoSerial.tiempoMs == 0
                ? 0
                : (resultadoSerial.tiempoMs - resultadoParalelo.tiempoMs) * 100.0 / resultadoSerial.tiempoMs;

        System.out.println("--------------------------------------------------");
        System.out.println("Serial   -> mínimo: " + resultadoSerial.minimo
                + " | máximo: " + resultadoSerial.maximo
                + " | tiempo: " + resultadoSerial.tiempoMs + " ms");
        System.out.println("Paralelo -> mínimo: " + resultadoParalelo.minimo
                + " | máximo: " + resultadoParalelo.maximo
                + " | tiempo: " + resultadoParalelo.tiempoMs + " ms");
        System.out.println("Resultados idénticos: " + (coincide ? "SÍ" : "NO"));

        if (!coincide) {
            System.err.println("ERROR: el resultado paralelo no coincide con el serial. "
                    + "Revisar la implementación antes de comparar rendimiento.");
            return;
        }

        System.out.printf("Mejora de rendimiento: %.2f%%%n", mejora);

        guardarResultado(rutaCsv, N, n, resultadoSerial, resultadoParalelo, mejora);
        System.out.println("Resultado guardado en: " + rutaCsv);
    }

    private static void guardarResultado(String rutaCsv, int N, int n,
                                          Resultado serial, Resultado paralelo, double mejora) throws IOException {
        File archivoCsv = new File(rutaCsv);
        boolean existe = archivoCsv.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoCsv, true))) {
            if (!existe) {
                writer.println("N,n,tiempoSerialMs,tiempoParaleloMs,minimo,maximo,mejoraPorcentaje");
            }
            writer.printf("%d,%d,%d,%d,%s,%s,%.2f%n",
                    N, n, serial.tiempoMs, paralelo.tiempoMs, serial.minimo, serial.maximo, mejora);
        }
    }
}
