package cg.geometry;

import java.util.Arrays;
import java.util.Random;

/** Gera polígonos já centrados na origem do plano cartesiano. */
public final class ShapeFactory {
    private ShapeFactory() {}

    public static final String REGULAR = "Regular";
    public static final String IRREGULAR = "Irregular";

    /** Polígono regular de n lados inscrito numa circunferência de raio r centrada em (0,0). */
    public static Shape2D regular(int n, double r) {
        n = Math.max(3, n);
        double[] x = new double[n], y = new double[n];
        // ponta para cima (n ímpar) ou lado plano em cima (n par): fica simétrico em relação ao eixo y
        double start = 90 + (n % 2 == 0 ? 180.0 / n : 0);
        for (int k = 0; k < n; k++) {
            double a = Math.toRadians(start + 360.0 * k / n);
            x[k] = clean(r * Math.cos(a)); y[k] = clean(r * Math.sin(a));
        }
        return new Shape2D(REGULAR + " n=" + n, true, x, y);
    }

    /**
     * Polígono irregular simples (sem auto-interseção): ângulos e raios perturbados, ordenados em torno do centro.
     * Depois o centróide da área é levado exatamente para (0,0).
     */
    public static Shape2D irregular(int n, double r, long seed) {
        n = Math.max(3, n);
        Random rnd = new Random(seed);
        double[] ang = new double[n];
        for (int k = 0; k < n; k++) ang[k] = 360.0 * (k + (rnd.nextDouble() - 0.5) * 0.7) / n;
        Arrays.sort(ang);
        double[] x = new double[n], y = new double[n];
        for (int k = 0; k < n; k++) {
            double rad = r * (0.45 + 0.65 * rnd.nextDouble());
            x[k] = Math.round(rad * Math.cos(Math.toRadians(ang[k] + 90)));
            y[k] = Math.round(rad * Math.sin(Math.toRadians(ang[k] + 90)));
        }
        Shape2D tmp = new Shape2D(IRREGULAR + " n=" + n, true, x, y);
        double[] c = tmp.centroid();
        for (int k = 0; k < n; k++) { x[k] -= c[0]; y[k] -= c[1]; }
        return new Shape2D(IRREGULAR + " n=" + n, true, x, y);
    }

    private static double clean(double v) { return Math.abs(v) < 1e-9 ? 0 : v; }
}
