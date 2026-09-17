public class DistanceCalculator {

    public static double calcular(double[] p, double[] q) {
        double suma = 0;
        for (int k = 0; k < p.length; k++) {
            double diff = p[k] - q[k];
            suma += diff * diff;
        }
        return Math.sqrt(suma);
    }
}
