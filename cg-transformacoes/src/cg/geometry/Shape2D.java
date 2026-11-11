package cg.geometry;

/**
 * Forma 2D (polígono fechado ou reta/polilinha aberta) com vértices em ponto flutuante.
 * Os vértices ficam em double para as transformações encadeadas não acumularem erro de arredondamento;
 * só na hora de rasterizar é que viram inteiros (Math.round).
 */
public final class Shape2D {
    public final String name;
    public final boolean closed;
    private double[] x, y;
    private final double[] ox, oy;   // estado original (criação), usado pelo "fantasma" e pelo reset

    public Shape2D(String name, boolean closed, double[] x, double[] y) {
        this.name = name; this.closed = closed;
        this.x = x.clone(); this.y = y.clone();
        this.ox = x.clone(); this.oy = y.clone();
    }

    public int size() { return x.length; }
    public double x(int i) { return x[i]; }
    public double y(int i) { return y[i]; }
    public double[] xs() { return x.clone(); }
    public double[] ys() { return y.clone(); }

    public void set(double[] nx, double[] ny) { x = nx.clone(); y = ny.clone(); }
    public void reset() { x = ox.clone(); y = oy.clone(); }

    public double[] originalX() { return ox; }
    public double[] originalY() { return oy; }

    public boolean isModified() {
        for (int i = 0; i < x.length; i++) if (Math.abs(x[i] - ox[i]) > 1e-9 || Math.abs(y[i] - oy[i]) > 1e-9) return true;
        return false;
    }

    /** Centróide da área (polígono fechado, fórmula do "shoelace"); média dos vértices se degenerado/aberto. */
    public double[] centroid() {
        int n = x.length;
        if (closed && n >= 3) {
            double a = 0, cx = 0, cy = 0;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                double cr = x[i] * y[j] - x[j] * y[i];
                a += cr; cx += (x[i] + x[j]) * cr; cy += (y[i] + y[j]) * cr;
            }
            if (Math.abs(a) > 1e-9) return new double[] { cx / (3 * a), cy / (3 * a) };
        }
        double sx = 0, sy = 0;
        for (int i = 0; i < n; i++) { sx += x[i]; sy += y[i]; }
        return new double[] { sx / n, sy / n };
    }

    /** Menor distância do ponto (px,py) à forma (usada pela ferramenta Selecionar). */
    public double distanceTo(double px, double py) {
        int n = x.length;
        if (n == 1) return Math.hypot(px - x[0], py - y[0]);
        double best = Double.MAX_VALUE;
        int last = closed ? n : n - 1;
        for (int i = 0; i < last; i++) {
            int j = (i + 1) % n;
            double dx = x[j] - x[i], dy = y[j] - y[i];
            double len2 = dx * dx + dy * dy;
            double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, ((px - x[i]) * dx + (py - y[i]) * dy) / len2));
            best = Math.min(best, Math.hypot(px - (x[i] + t * dx), py - (y[i] + t * dy)));
        }
        return best;
    }
}
