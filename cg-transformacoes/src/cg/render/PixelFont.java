package cg.render;

/** Fonte bitmap 3x5 (dígitos e sinais) para os rótulos dos eixos: também escrita pixel a pixel, sem drawString. */
public final class PixelFont {
    private PixelFont() {}

    private static final String CHARS = "0123456789-.";
    // 5 linhas de 3 bits por caractere (bit mais à esquerda = coluna 0)
    private static final int[][] G = {
        {7,5,5,5,7}, {2,6,2,2,7}, {7,1,7,4,7}, {7,1,7,1,7}, {5,5,7,1,1},
        {7,4,7,1,7}, {7,4,7,5,7}, {7,1,1,1,1}, {7,5,7,5,7}, {7,5,7,1,7},
        {0,0,7,0,0}, {0,0,0,0,2}
    };

    public static int width(String s, int scale)  { return s.isEmpty() ? 0 : s.length() * 4 * scale - scale; }
    public static int height(int scale)           { return 5 * scale; }

    public static void draw(Framebuffer fb, String s, int x, int y, int scale, int rgb) {
        for (int i = 0; i < s.length(); i++) {
            int g = CHARS.indexOf(s.charAt(i));
            if (g < 0) continue;
            for (int row = 0; row < 5; row++)
                for (int col = 0; col < 3; col++)
                    if ((G[g][row] & (4 >> col)) != 0)
                        fb.fillRect(x + (i * 4 + col) * scale, y + row * scale, scale, scale, rgb);
        }
    }
}
