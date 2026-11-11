package cg.terminal;

import cg.geometry.Matrix3;
import cg.geometry.Shape2D;
import cg.raster.MidpointLine;

import java.util.ArrayList;
import java.util.List;

import static cg.geometry.Matrix3.fmt;

/** Formatação de texto para o terminal (contas do ponto médio e listagem de formas). */
public final class Report {
    private Report() {}

    public static void shape(Console c, String header, Shape2D s) {
        c.println(header);
        for (int i = 0; i < s.size(); i++) c.println("  P" + i + " = (" + fmt(s.x(i)) + ", " + fmt(s.y(i)) + ")");
        double[] g = s.centroid();
        c.println("  centróide = (" + fmt(g[0]) + ", " + fmt(g[1]) + ")");
    }

    /** Contas do algoritmo do ponto médio para a reta, no formato do exemplo do slide 28 (G3). */
    public static void midpoint(Console c, int x0, int y0, int x1, int y1) {
        List<int[]> log = new ArrayList<>();
        MidpointLine.draw(x0, y0, x1, y1, (x, y) -> {}, log);
        int[] p = MidpointLine.params(x0, y0, x1, y1);
        c.println("Ponto médio: (" + x0 + "," + y0 + ") -> (" + x1 + "," + y1 + ")");
        c.println("  dx = " + (x1 - x0) + ", dy = " + (y1 - y0)
                + (Math.abs(y1 - y0) > Math.abs(x1 - x0) ? "   (|m|>1: eixos trocados por simetria)" : ""));
        c.println("  d0 = 2dy - dx = " + p[2] + "   incE = 2dy = " + p[3] + "   incNE = 2(dy-dx) = " + p[4]);
        if (log.size() <= 40) {
            c.println("     d     x     y     escolha");
            for (int[] r : log) c.println(String.format("  %4d  %4d  %4d     %s", r[0], r[1], r[2], r[0] > 0 ? "NE" : "E"));
        } else {
            c.println("  (" + log.size() + " passos; tabela omitida - reta longa)");
        }
    }

    public static String[] matrixLines(Matrix3 m) { return m.rows(); }
}
