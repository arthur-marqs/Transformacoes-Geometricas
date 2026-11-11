package cg.render;

/**
 * Relação entre o MUNDO (células inteiras, y para cima, origem no centro) e a TELA (pixels reais, y para baixo).
 *
 * Com zoom z, cada célula (wx,wy) do mundo vira um bloco z x z de pixels reais:
 *   x da tela:  [originX + wx*z , +z)        y da tela: [originY - (wy+1)*z , +z)
 * Com zoom 1, uma célula é exatamente 1 pixel real da tela.
 */
public final class View {
    public static final int[] ZOOMS = { 1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24, 32, 48, 64 };
    public static final int MAX_ZOOM = 64;

    public int zoom = 2;
    public int panX, panY;          // deslocamento da origem em relação ao centro do framebuffer (pixels reais)
    public boolean pixelGap = true; // com zoom >= 6, deixa 1 px de separação entre células (mostra a "grade de pixels")
    private int fbW = 1, fbH = 1;

    public void setSize(int w, int h) { fbW = w; fbH = h; }

    public int originX() { return fbW / 2 + panX; }
    public int originY() { return fbH / 2 + panY; }

    /** Canto superior esquerdo (em pixels reais) do bloco da célula. */
    public int screenX(int wx) { return originX() + wx * zoom; }
    public int screenY(int wy) { return originY() - (wy + 1) * zoom; }

    /** Pixel real do centro da célula: onde passam as retas dos eixos. */
    public int centerX(int wx) { return originX() + wx * zoom + zoom / 2; }
    public int centerY(int wy) { return originY() - wy * zoom - (zoom + 1) / 2; }

    /** Célula do mundo sob o pixel real (sx,sy). */
    public int cellX(int sx) { return Math.floorDiv(sx - originX(), zoom); }
    public int cellY(int sy) { return Math.floorDiv(originY() - sy - 1, zoom); }

    /** Desenha uma célula (bloco zoom x zoom) no framebuffer. */
    public void plot(Framebuffer fb, int wx, int wy, int rgb) {
        int x = screenX(wx), y = screenY(wy);
        if (x >= fb.w || y >= fb.h || x + zoom <= 0 || y + zoom <= 0) return;
        int size = (pixelGap && zoom >= 6) ? zoom - 1 : zoom;
        if (size == 1) fb.set(x, y, rgb); else fb.fillRect(x, y, size, size, rgb);
    }

    /** Zoom mantendo fixa a célula sob (sx,sy) - usado pela roda do mouse. */
    public void zoomAt(int sx, int sy, int newZoom) {
        newZoom = Math.max(1, Math.min(MAX_ZOOM, newZoom));
        double wx = (sx - originX()) / (double) zoom;
        double wy = (originY() - sy) / (double) zoom;
        zoom = newZoom;
        panX = (int) Math.round(sx - wx * zoom - fbW / 2.0);
        panY = (int) Math.round(sy + wy * zoom - fbH / 2.0);
    }

    public void zoomCenter(int newZoom) { zoomAt(fbW / 2, fbH / 2, newZoom); }

    public int nextZoom(int dir) {
        if (dir > 0) { for (int z : ZOOMS) if (z > zoom) return z; return MAX_ZOOM; }
        for (int i = ZOOMS.length - 1; i >= 0; i--) if (ZOOMS[i] < zoom) return ZOOMS[i];
        return 1;
    }

    public void centerOrigin() { panX = 0; panY = 0; }
}
