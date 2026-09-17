import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Procesador serial: recorre todo el dataset en un solo hilo (main thread)
 * y calcula la distancia mínima y máxima entre todos los pares de puntos.
 *
 * Sirve como línea base de tiempo para comparar contra ParallelProcessor.
 *
 * Responsable: Integrante 3
 */
public class SerialProcessor {

    private static final int BUFFER_SIZE = 65536; // 64 KB de buffer

    /**
     * Procesa el dataset completo en serie y devuelve el resultado global.
     * Misma firma que ParallelProcessor.procesar(...) para que Main.java
     * los invoque de forma intercambiable.
     */
    public Resultado procesar(String rutaDataset, int N, int n) {
        long tiempoInicio = System.currentTimeMillis();

        double minimo = Double.POSITIVE_INFINITY;
        double maximo = Double.NEGATIVE_INFINITY;

        try (BufferedReader readerExterno = new BufferedReader(new FileReader(rutaDataset), BUFFER_SIZE)) {
            readerExterno.readLine(); // descarta encabezado "N n"

            for (int i = 0; i < N; i++) {
                String lineaI = readerExterno.readLine();
                double[] puntoI = parsearPunto(lineaI, n);

                try (BufferedReader readerInterno = new BufferedReader(new FileReader(rutaDataset), BUFFER_SIZE)) {
                    readerInterno.readLine(); // descarta encabezado
                    for (int k = 0; k <= i; k++) {
                        readerInterno.readLine(); // avanza hasta dejar atrás el punto i
                    }

                    String lineaJ;
                    while ((lineaJ = readerInterno.readLine()) != null) {
                        double[] puntoJ = parsearPunto(lineaJ, n);
                        double distancia = DistanceCalculator.calcular(puntoI, puntoJ);
                        if (distancia < minimo) {
                            minimo = distancia;
                        }
                        if (distancia > maximo) {
                            maximo = distancia;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error procesando dataset en SerialProcessor", e);
        }

        long tiempoFin = System.currentTimeMillis();

        Resultado resultado = new Resultado();
        resultado.minimo = minimo;
        resultado.maximo = maximo;
        resultado.tiempoMs = tiempoFin - tiempoInicio;
        return resultado;
    }

    private double[] parsearPunto(String linea, int n) {
        String[] tokens = linea.trim().split("\\s+");
        double[] punto = new double[n];
        for (int k = 0; k < n; k++) {
            punto[k] = Double.parseDouble(tokens[k]);
        }
        return punto;
    }

    /**
     * Punto de entrada para probar SerialProcessor de forma aislada.
     *
     * Uso: java SerialProcessor <ruta_dataset>
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java SerialProcessor <ruta_dataset>");
            return;
        }

        String rutaDataset = args[0];
        int N;
        int n;
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaDataset))) {
            String[] encabezado = reader.readLine().trim().split("\\s+");
            N = Integer.parseInt(encabezado[0]);
            n = Integer.parseInt(encabezado[1]);
        }

        Resultado resultado = new SerialProcessor().procesar(rutaDataset, N, n);

        System.out.println("Dataset      : " + rutaDataset + " (N=" + N + ", n=" + n + ")");
        System.out.println("Distancia mín: " + resultado.minimo);
        System.out.println("Distancia máx: " + resultado.maximo);
        System.out.println("Tiempo (ms)  : " + resultado.tiempoMs);
    }
}
