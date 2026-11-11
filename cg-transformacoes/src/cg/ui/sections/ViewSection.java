package cg.ui.sections;

import cg.app.AppContext;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;

/** Zoom, pixels separados e escolha da resolução do display (em pixels reais). */
public final class ViewSection implements ToolSection {
    private static final class Res {
        final String label; final int w, h;   // w = 0 -> automático
        Res(String label, int w, int h) { this.label = label; this.w = w; this.h = h; }
        @Override public String toString() { return label; }
    }

    private static final Res[] PRESETS = {
        new Res("Automática — ocupa toda a área (recomendada)", 0, 0),
        new Res("640 × 480", 640, 480),
        new Res("800 × 600", 800, 600),
        new Res("1024 × 600", 1024, 600),
        new Res("1024 × 768", 1024, 768),
        new Res("1152 × 648", 1152, 648),
        new Res("1280 × 720 (HD)", 1280, 720),
        new Res("1366 × 768", 1366, 768),
        new Res("1600 × 900", 1600, 900),
        new Res("1920 × 1080 (Full HD)", 1920, 1080),
    };

    private final AppContext ctx;
    public ViewSection(AppContext ctx) { this.ctx = ctx; }

    @Override public String title() { return "Vista, zoom e resolução"; }

    @Override public JComponent build() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // ----- zoom -----
        JPanel z = new JPanel(new GridLayout(1, 4, 4, 4));
        z.setBorder(BorderFactory.createTitledBorder("Zoom (roda do mouse ou + / -)"));
        JButton minus = new JButton("−"), plus = new JButton("+"), one = new JButton("1×"), home = new JButton("Origem");
        minus.addActionListener(e -> zoom(-1));
        plus.addActionListener(e -> zoom(1));
        one.addActionListener(e -> { ctx.view.zoomCenter(1); refresh(); });
        home.addActionListener(e -> { ctx.view.centerOrigin(); refresh(); });
        z.add(minus); z.add(plus); z.add(one); z.add(home);
        JCheckBox gap = new JCheckBox("Separar pixels com zoom ≥ 6 (ver a grade)", ctx.view.pixelGap);
        gap.addActionListener(e -> { ctx.view.pixelGap = gap.isSelected(); ctx.redraw(); });
        p.add(left(z)); p.add(left(gap));

        // ----- resolução -----
        JPanel r = new JPanel();
        r.setLayout(new BoxLayout(r, BoxLayout.Y_AXIS));
        r.setBorder(BorderFactory.createTitledBorder("Resolução do display (pixels reais)"));
        JComboBox<Res> combo = new JComboBox<>(PRESETS);
        JLabel info = new JLabel();
        combo.addActionListener(e -> { apply((Res) combo.getSelectedItem(), info); });
        JSpinner cw = new JSpinner(new SpinnerNumberModel(1280, 200, 7680, 10));
        JSpinner ch = new JSpinner(new SpinnerNumberModel(720, 200, 4320, 10));
        JButton custom = new JButton("Usar");
        custom.addActionListener(e -> apply(new Res("personalizada", (Integer) cw.getValue(), (Integer) ch.getValue()), info));
        JPanel cust = new JPanel(new GridLayout(1, 4, 4, 4));
        cust.add(cw); cust.add(ch); cust.add(custom); cust.add(new JLabel("L × A"));
        r.add(left(combo)); r.add(javax.swing.Box.createVerticalStrut(4));
        r.add(left(cust)); r.add(javax.swing.Box.createVerticalStrut(4)); r.add(left(info));
        p.add(javax.swing.Box.createVerticalStrut(6)); p.add(left(r));
        info.setText(screenText());
        return p;
    }

    private void zoom(int dir) { ctx.view.zoomCenter(ctx.view.nextZoom(dir)); refresh(); }
    private void refresh() { ctx.statusChanged(); ctx.redraw(); }

    private void apply(Res res, JLabel info) {
        if (res.w == 0) { ctx.display.setAuto(); info.setText(screenText()); return; }
        ctx.display.setFixed(res.w, res.h);
        info.setText(fitText(res.w, res.h));
    }

    /** Tela do notebook: pixels lógicos x escala do sistema = pixels reais. */
    private String screenText() {
        double s = scale();
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        java.awt.Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        return String.format("<html><body style='width:270px'>Tela: %d×%d pixels reais (escala do SO %d%%).<br>Área útil: %d×%d.</body></html>",
                Math.round(scr.width * s), Math.round(scr.height * s), Math.round(s * 100),
                Math.round(usable.width * s), Math.round(usable.height * s));
    }

    /** Diz se display + painel lateral cabem na área útil (senão aparecem barras de rolagem). */
    private String fitText(int w, int h) {
        double s = scale();
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        long needW = Math.round(w + (ctx.display.sidebarWidth() + 24) * s);
        long needH = Math.round(h + 110 * s);
        long haveW = Math.round(usable.width * s), haveH = Math.round(usable.height * s);
        boolean fits = needW <= haveW && needH <= haveH;
        return String.format("<html><body style='width:270px'>%s %d×%d + painel = %d×%d (área útil %d×%d)%s</body></html>",
                fits ? "✔ Cabe:" : "✖ Não cabe:", w, h, needW, needH, haveW, haveH, fits ? "" : " — haverá rolagem.");
    }

    private double scale() { return ctx.deviceScale > 0 ? ctx.deviceScale : 1; }

    private static JComponent left(JComponent c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); return c; }
}
