import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Orquestador de la solución paralela con MultiThreading (2 hilos).
 *
 * Divide el rango de índices [0, N) en dos mitades por índice i,
 * lanza un ThreadProcessor por mitad, espera con join() y combina
 * los resultados locales en un mínimo/máximo global.
 *
 * Responsable: Integrante 4
 */
public class ParallelProcessor {

    private static final int NUM_HILOS = 2;

    /**
     * Procesa el dataset con 2 hilos y devuelve el resultado global.
     * Misma firma que SerialProcessor.procesar(...) para que Main.java
     * los invoque de forma intercambiable.
     */
    public Resultado procesar(String rutaDataset, int N, int n) {
        long tiempoInicio = System.currentTimeMillis();

        int mitad = N / 2;
        ThreadProcessor hilo1 = new ThreadProcessor(rutaDataset, n, 0, mitad);
        ThreadProcessor hilo2 = new ThreadProcessor(rutaDataset, n, mitad, N);

        hilo1.start();
        hilo2.start();

        try {
            hilo1.join();
            hilo2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ejecución paralela interrumpida", e);
        }

        long tiempoFin = System.currentTimeMillis();

        Resultado resultado = new Resultado();
        resultado.minimo = Math.min(hilo1.minLocal, hilo2.minLocal);
        resultado.maximo = Math.max(hilo1.maxLocal, hilo2.maxLocal);
        resultado.tiempoMs = tiempoFin - tiempoInicio;
        return resultado;
    }

    /**
     * Punto de entrada para probar ParallelProcessor de forma aislada,
     * sin depender de Main.java ni de SerialProcessor.
     *
     * Uso: java ParallelProcessor <ruta_dataset>
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java ParallelProcessor <ruta_dataset>");
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

        Resultado resultado = new ParallelProcessor().procesar(rutaDataset, N, n);

        System.out.println("Dataset      : " + rutaDataset + " (N=" + N + ", n=" + n + ")");
        System.out.println("Distancia mín: " + resultado.minimo);
        System.out.println("Distancia máx: " + resultado.maximo);
        System.out.println("Tiempo (ms)  : " + resultado.tiempoMs);
    }
}
