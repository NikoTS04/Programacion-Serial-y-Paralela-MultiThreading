import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Hilo de procesamiento por franja de índices [iInicio, iFin).
 *
 * Para cada punto i en su franja, compara contra todo punto j > i,
 * leyendo el dataset directamente desde disco (sin cargarlo en memoria).
 * Cada hilo abre sus propios BufferedReader independientes sobre el mismo
 * archivo, por lo que no hay conflicto de lectura simultánea.
 *
 * Responsable: Integrante 4
 */
public class ThreadProcessor extends Thread {

    private static final int BUFFER_SIZE = 65536; // 64 KB de buffer

    private final String rutaDataset;
    private final int n;
    private final int iInicio;
    private final int iFin; // exclusivo

    public double minLocal = Double.POSITIVE_INFINITY;
    public double maxLocal = Double.NEGATIVE_INFINITY;

    public ThreadProcessor(String rutaDataset, int n, int iInicio, int iFin) {
        this.rutaDataset = rutaDataset;
        this.n = n;
        this.iInicio = iInicio;
        this.iFin = iFin;
    }

    @Override
    public void run() {
        try (BufferedReader readerExterno = new BufferedReader(new FileReader(rutaDataset), BUFFER_SIZE)) {
            readerExterno.readLine(); // descarta encabezado "N n"
            for (int k = 0; k < iInicio; k++) {
                readerExterno.readLine(); // avanza hasta el punto iInicio
            }

            for (int i = iInicio; i < iFin; i++) {
                String lineaI = readerExterno.readLine();
                double[] puntoI = parsearPunto(lineaI);
                compararConSiguientes(i, puntoI);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error procesando dataset en " + getName(), e);
        }
    }

    /**
     * Recorre desde el punto i+1 hasta el final del archivo, comparando
     * cada uno contra puntoI y actualizando min/max locales.
     */
    private void compararConSiguientes(int i, double[] puntoI) throws IOException {
        try (BufferedReader readerInterno = new BufferedReader(new FileReader(rutaDataset), BUFFER_SIZE)) {
            readerInterno.readLine(); // descarta encabezado
            for (int k = 0; k <= i; k++) {
                readerInterno.readLine(); // avanza hasta dejar atrás el punto i
            }

            String lineaJ;
            while ((lineaJ = readerInterno.readLine()) != null) {
                double[] puntoJ = parsearPunto(lineaJ);
                double distancia = DistanceCalculator.calcular(puntoI, puntoJ);
                if (distancia < minLocal) {
                    minLocal = distancia;
                }
                if (distancia > maxLocal) {
                    maxLocal = distancia;
                }
            }
        }
    }

    private double[] parsearPunto(String linea) {
        String[] tokens = linea.trim().split("\\s+");
        double[] punto = new double[n];
        for (int k = 0; k < n; k++) {
            punto[k] = Double.parseDouble(tokens[k]);
        }
        return punto;
    }
}
