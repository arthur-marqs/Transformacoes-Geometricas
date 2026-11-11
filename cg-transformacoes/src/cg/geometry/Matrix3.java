package cg.geometry;

import java.util.Locale;

/**
 * Matriz 3x3 em coordenadas homogêneas (aula G5, slides 12-17). Convenção do slide: P' = M · P,
 * com P = [x y 1]^T (vetor coluna). Composição: aplicar A e depois B  =>  M = B · A.
 */
public final class Matrix3 {
    public final double[][] m = new double[3][3];

    public static Matrix3 identity() {
        Matrix3 r = new Matrix3();
        for (int i = 0; i < 3; i++) r.m[i][i] = 1;
        return r;
    }

    public static Matrix3 translation(double tx, double ty) {
        Matrix3 r = identity(); r.m[0][2] = tx; r.m[1][2] = ty; return r;
    }

    public static Matrix3 scaling(double sx, double sy) {
        Matrix3 r = identity(); r.m[0][0] = sx; r.m[1][1] = sy; return r;
    }

    /** Rotação anti-horária (eixo y para cima) em torno da origem. */
    public static Matrix3 rotation(double degrees) {
        double a = Math.toRadians(degrees), c = Math.cos(a), s = Math.sin(a);
        c = clean(c); s = clean(s);
        Matrix3 r = identity();
        r.m[0][0] = c; r.m[0][1] = -s;
        r.m[1][0] = s; r.m[1][1] = c;
        return r;
    }

    /** Cisalhamento em x: x' = x + k*y. */
    public static Matrix3 shearX(double k) { Matrix3 r = identity(); r.m[0][1] = k; return r; }

    /** Cisalhamento em y: y' = y + k*x. */
    public static Matrix3 shearY(double k) { Matrix3 r = identity(); r.m[1][0] = k; return r; }

    public static Matrix3 of(double... v) {
        Matrix3 r = new Matrix3();
        for (int i = 0; i < 9; i++) r.m[i / 3][i % 3] = v[i];
        return r;
    }

    /** this · o  (aplica "o" primeiro, depois "this"). */
    public Matrix3 mul(Matrix3 o) {
        Matrix3 r = new Matrix3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                double s = 0;
                for (int k = 0; k < 3; k++) s += m[i][k] * o.m[k][j];
                r.m[i][j] = s;
            }
        return r;
    }

    /** Aplica a matriz ao ponto (x,y,1) e homogeneíza (divide por W) - slides 14-15. */
    public double[] apply(double x, double y) {
        double nx = m[0][0] * x + m[0][1] * y + m[0][2];
        double ny = m[1][0] * x + m[1][1] * y + m[1][2];
        double w  = m[2][0] * x + m[2][1] * y + m[2][2];
        if (w != 0 && w != 1) { nx /= w; ny /= w; }
        return new double[] { nx, ny };
    }

    /** Linhas "| a b c |" alinhadas, para o terminal. */
    public String[] rows() {
        String[][] cell = new String[3][3];
        int width = 1;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) { cell[i][j] = fmt(m[i][j]); width = Math.max(width, cell[i][j].length()); }
        String[] out = new String[3];
        for (int i = 0; i < 3; i++) {
            StringBuilder sb = new StringBuilder("| ");
            for (int j = 0; j < 3; j++) { sb.append(pad(cell[i][j], width)); sb.append(j < 2 ? "  " : " |"); }
            out[i] = sb.toString();
        }
        return out;
    }

    private static String pad(String s, int w) { StringBuilder b = new StringBuilder(); for (int i = s.length(); i < w; i++) b.append(' '); return b.append(s).toString(); }

    private static double clean(double v) { return Math.abs(v) < 1e-12 ? 0 : v; }

    /** Número "limpo": inteiro quando for (a menos de erro de ponto flutuante), senão até 4 casas. */
    public static String fmt(double v) {
        if (Math.abs(v) < 1e-9) return "0";
        double r = Math.rint(v);
        if (Math.abs(v - r) < 1e-9) return String.valueOf((long) r);
        String s = String.format(Locale.US, "%.4f", v);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s.equals("-0") ? "0" : s;
    }
}
