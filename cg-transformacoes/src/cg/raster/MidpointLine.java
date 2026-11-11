package cg.raster;

import java.util.List;

/**
 * Algoritmo do Ponto Médio (Bresenham) para retas - aula G3, slides 16-28.
 *
 * Variáveis (slide 25, já multiplicadas por 2 para ficarem inteiras):
 *   d0    = 2*dy - dx        (valor inicial da variável de decisão)
 *   incE  = 2*dy             (se d <= 0 escolhe E:  só avança o eixo maior)
 *   incNE = 2*(dy - dx)      (se d >  0 escolhe NE: avança os dois eixos)
 *
 * O slide trata só o 1º octante (0 <= m <= 1). Os demais octantes são obtidos por simetria:
 *   - |m| > 1  -> o eixo maior passa a ser y (troca dx <-> dy);
 *   - m < 0    -> o passo em y é -1 em vez de +1.
 * Os extremos são ordenados (x crescente) para a mesma reta gerar os mesmos pixels
 * independentemente da ordem em que os pontos foram clicados.
 */
public final class MidpointLine {
    private MidpointLine() {}

    /** Desenha a reta (x0,y0)-(x1,y1). Se {@code log} != null, grava {d, x, y} de cada passo (como na tabela do slide 28). */
    public static void draw(int x0, int y0, int x1, int y1, PixelSink sink, List<int[]> log) {
        if (x0 > x1 || (x0 == x1 && y0 > y1)) { int t = x0; x0 = x1; x1 = t; t = y0; y0 = y1; y1 = t; }
        int dx = x1 - x0;
        int dy = Math.abs(y1 - y0);
        int stepY = (y1 >= y0) ? 1 : -1;
        boolean steep = dy > dx;                 // |m| > 1: eixo maior é y
        int major = steep ? dy : dx;             // "dx" do slide, após a simetria
        int minor = steep ? dx : dy;             // "dy" do slide, após a simetria

        int d = 2 * minor - major;
        int incE = 2 * minor;
        int incNE = 2 * (minor - major);

        int x = x0, y = y0;
        sink.plot(x, y);
        for (int i = 0; i < major; i++) {
            boolean ne = d > 0;                  // d <= 0 -> E ; d > 0 -> NE
            int dUsed = d;
            d += ne ? incNE : incE;
            if (steep) { y += stepY; if (ne) x += 1; }
            else       { x += 1;     if (ne) y += stepY; }
            sink.plot(x, y);
            if (log != null) log.add(new int[] { dUsed, x, y });
        }
    }

    public static void draw(int x0, int y0, int x1, int y1, PixelSink sink) {
        draw(x0, y0, x1, y1, sink, null);
    }

    /** Parâmetros iniciais {dx, dy, d0, incE, incNE} após a redução ao 1º octante (para exibir no terminal). */
    public static int[] params(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        boolean steep = dy > dx;
        int major = steep ? dy : dx, minor = steep ? dx : dy;
        return new int[] { major, minor, 2 * minor - major, 2 * minor, 2 * (minor - major) };
    }
}
