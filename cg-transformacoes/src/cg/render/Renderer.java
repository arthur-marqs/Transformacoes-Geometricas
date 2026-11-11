package cg.render;

import cg.geometry.Shape2D;
import cg.raster.MidpointLine;
import cg.scene.Scene;

/**
 * Monta um quadro completo no framebuffer: fundo, eixos, forma original (fantasma), formas, guias,
 * pré-visualização da ferramenta e cursor. TODA reta passa pelo algoritmo do ponto médio.
 * As ferramentas desenham suas pré-visualizações usando {@link #line} e {@link #cell}.
 */
public final class Renderer {
    public interface Overlay { void draw(Renderer r); }

    private static final int[] TICKS = { 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000 };

    private final View view;
    private final Theme theme;
    private Framebuffer fb;

    public Renderer(View view, Theme theme) { this.view = view; this.theme = theme; }

    public void render(Framebuffer fb, Scene scene, Overlay overlay, boolean showCursor, int cx, int cy) {
        this.fb = fb;
        view.setSize(fb.w, fb.h);
        fb.clear(theme.background());
        drawAxes();

        Shape2D act = scene.active();
        if (act != null && act.isModified()) drawPolyline(act.originalX(), act.originalY(), act.closed, theme.ghost());
        for (Shape2D s : scene.shapes()) drawPolyline(s.xs(), s.ys(), s.closed, theme.shape());
        if (act != null) for (int i = 0; i < act.size(); i++) cell(round(act.x(i)), round(act.y(i)), theme.vertex());

        drawRect(scene.windowGuide(), theme.guide());
        drawRect(scene.viewportGuide(), theme.guide());
        if (overlay != null) overlay.draw(this);
        if (showCursor) drawCursor(cx, cy);
    }

    // ---------- primitivas expostas às ferramentas ----------

    public void cell(int wx, int wy, int rgb) { view.plot(fb, wx, wy, rgb); }

    public void line(int x0, int y0, int x1, int y1, int rgb) {
        MidpointLine.draw(x0, y0, x1, y1, (x, y) -> view.plot(fb, x, y, rgb));
    }

    public void drawPolyline(double[] x, double[] y, boolean closed, int rgb) {
        int n = x.length;
        if (n == 1) { cell(round(x[0]), round(y[0]), rgb); return; }
        int last = closed ? n : n - 1;
        for (int i = 0; i < last; i++) {
            int j = (i + 1) % n;
            line(round(x[i]), round(y[i]), round(x[j]), round(y[j]), rgb);
        }
    }

    public Theme theme() { return theme; }

    // ---------- partes do quadro ----------

    private void drawRect(double[] r, int rgb) {
        if (r == null) return;
        drawPolyline(new double[] { r[0], r[2], r[2], r[0] }, new double[] { r[1], r[1], r[3], r[3] }, true, rgb);
    }

    private void drawCursor(int cx, int cy) {
        int z = view.zoom, x = view.screenX(cx), y = view.screenY(cy), c = theme.cursor();
        if (z < 4) { fb.fillRect(x, y, z, z, c); return; }
        fb.fillRect(x, y, z, 1, c); fb.fillRect(x, y + z - 1, z, 1, c);
        fb.fillRect(x, y, 1, z, c); fb.fillRect(x + z - 1, y, 1, z, c);
    }

    /** Eixos x e y como retas de 1 pixel passando pelo centro das células (0,y) e (x,0), com marcas e números. */
    private void drawAxes() {
        int ax = theme.axes();
        int yAxis = view.centerY(0), xAxis = view.centerX(0);
        fb.fillRect(0, yAxis, fb.w, 1, ax);      // eixo x (horizontal)
        fb.fillRect(xAxis, 0, 1, fb.h, ax);      // eixo y (vertical)

        int step = TICKS[TICKS.length - 1];
        for (int t : TICKS) if (t * view.zoom >= 70) { step = t; break; }
        int scale = fb.w >= 1600 ? 2 : 1;
        int minX = view.cellX(0), maxX = view.cellX(fb.w - 1), minY = view.cellY(fb.h - 1), maxY = view.cellY(0);

        for (int t = Math.floorDiv(minX, step) * step; t <= maxX; t += step) {
            if (t == 0) continue;
            int sx = view.centerX(t);
            fb.fillRect(sx, yAxis + 1, 1, 4, ax);
            String s = String.valueOf(t);
            PixelFont.draw(fb, s, sx - PixelFont.width(s, scale) / 2, yAxis + 7, scale, ax);
        }
        for (int t = Math.floorDiv(minY, step) * step; t <= maxY; t += step) {
            if (t == 0) continue;
            int sy = view.centerY(t);
            fb.fillRect(xAxis - 4, sy, 4, 1, ax);
            String s = String.valueOf(t);
            PixelFont.draw(fb, s, xAxis - 7 - PixelFont.width(s, scale), sy - PixelFont.height(scale) / 2, scale, ax);
        }
        PixelFont.draw(fb, "0", xAxis - 7 - PixelFont.width("0", scale), yAxis + 7, scale, ax);
    }

    private static int round(double v) { return (int) Math.round(v); }
}
