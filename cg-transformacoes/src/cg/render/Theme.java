package cg.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Cores configuráveis (0xRRGGBB). Padrão pedido: fundo preto, pixels verdes, eixos vermelhos. */
public final class Theme {
    public static final String[] NAMES = {
        "Fundo", "Forma (pixels)", "Eixos cartesianos", "Original (fantasma)",
        "Pré-visualização", "Vértices da forma ativa", "Cursor", "Guias janela/viewport"
    };

    private final int[] c = new int[NAMES.length];
    private final List<Runnable> listeners = new ArrayList<>();

    public Theme() { apply(PRESETS.get(0)); }

    public int background() { return c[0]; }
    public int shape()      { return c[1]; }
    public int axes()       { return c[2]; }
    public int ghost()      { return c[3]; }
    public int preview()    { return c[4]; }
    public int vertex()     { return c[5]; }
    public int cursor()     { return c[6]; }
    public int guide()      { return c[7]; }

    public int get(int i) { return c[i]; }
    public void set(int i, int rgb) { c[i] = rgb & 0xFFFFFF; changed(); }
    public void apply(Preset p) { System.arraycopy(p.colors, 0, c, 0, c.length); changed(); }

    public void addListener(Runnable r) { listeners.add(r); }
    public void changed() { for (Runnable r : listeners) r.run(); }

    public static final class Preset {
        public final String name; final int[] colors;
        Preset(String name, int... colors) { this.name = name; this.colors = colors; }
        @Override public String toString() { return name; }
    }

    //                                  fundo     forma     eixos     fantasma  preview   vértice   cursor    guia
    public static final List<Preset> PRESETS = Arrays.asList(
        new Preset("Clássico (preto / verde / vermelho)",
                   0x000000, 0x00FF00, 0xFF0000, 0x1E6B1E, 0xFFFF00, 0xFFFFFF, 0x808080, 0x00FFFF),
        new Preset("Projetor (fundo branco)",
                   0xFFFFFF, 0x006400, 0xC80000, 0xA8D0A8, 0xE08000, 0x000000, 0x808080, 0x0090A0),
        new Preset("Daltônico (ciano / laranja)",
                   0x000000, 0x00E5FF, 0xFF9100, 0x005566, 0xFFFF00, 0xFFFFFF, 0x808080, 0xFF00FF)
    );
}
