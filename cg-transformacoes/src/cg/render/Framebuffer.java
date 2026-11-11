package cg.render;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Memória de vídeo simulada: um vetor int[] com UM elemento por PIXEL REAL do dispositivo.
 * Todo desenho do sistema acaba em {@link #set}. O BufferedImage apenas compartilha esse vetor
 * para o Swing poder copiá-lo para a tela 1:1 (sem escala).
 */
public final class Framebuffer {
    public int w = 1, h = 1;
    private BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    private int[] px = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

    public void resize(int newW, int newH) {
        newW = Math.max(1, newW); newH = Math.max(1, newH);
        if (newW == w && newH == h) return;
        w = newW; h = newH;
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        px = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public void clear(int rgb) { Arrays.fill(px, rgb); }

    public void set(int x, int y, int rgb) {
        if (x < 0 || y < 0 || x >= w || y >= h) return;
        px[y * w + x] = rgb;
    }

    /** Preenche um retângulo de pixels reais, recortado aos limites do framebuffer. */
    public void fillRect(int x, int y, int rw, int rh, int rgb) {
        int x0 = Math.max(0, x), y0 = Math.max(0, y);
        int x1 = Math.min(w, x + rw), y1 = Math.min(h, y + rh);
        for (int yy = y0; yy < y1; yy++) {
            int base = yy * w;
            Arrays.fill(px, base + x0, base + Math.max(x0, x1), rgb);
        }
    }

    public int get(int x, int y) { return (x < 0 || y < 0 || x >= w || y >= h) ? 0 : px[y * w + x]; }

    public BufferedImage image() { return image; }
}
