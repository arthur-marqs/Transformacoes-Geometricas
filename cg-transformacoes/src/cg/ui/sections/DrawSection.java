package cg.ui.sections;

import cg.app.AppContext;
import cg.tools.Tool;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

/** Ferramentas de mouse (geradas a partir do ToolRegistry) + gerador de polígono na origem. */
public final class DrawSection implements ToolSection {
    private final AppContext ctx;
    public DrawSection(AppContext ctx) { this.ctx = ctx; }

    @Override public String title() { return "Desenho"; }
    @Override public boolean initiallyOpen() { return true; }

    @Override public JComponent build() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // botões de ferramenta: um por Tool registrada (ferramenta nova aparece sozinha)
        JPanel grid = new JPanel(new GridLayout(0, 2, 4, 4));
        ButtonGroup g = new ButtonGroup();
        Map<Tool, JToggleButton> buttons = new HashMap<>();
        JLabel hint = new JLabel();
        for (Tool t : ctx.tools.all()) {
            JToggleButton b = new JToggleButton(t.name());
            b.setFocusPainted(false);
            b.addActionListener(e -> ctx.tools.setActive(t));
            g.add(b); grid.add(b); buttons.put(t, b);
        }
        Runnable sync = () -> {
            Tool a = ctx.tools.active();
            buttons.get(a).setSelected(true);
            hint.setText("<html><body style='width:260px'>" + a.hint() + "</body></html>");
        };
        ctx.tools.addListener(sync);
        sync.run();
        p.add(left(grid)); p.add(javax.swing.Box.createVerticalStrut(4)); p.add(left(hint));

        // gerador de polígono na origem
        JPanel gen = new JPanel(new GridLayout(0, 2, 4, 4));
        gen.setBorder(BorderFactory.createTitledBorder("Polígono na origem (0,0)"));
        JSpinner n = new JSpinner(new SpinnerNumberModel(5, 3, 60, 1));
        JSpinner r = new JSpinner(new SpinnerNumberModel(100, 2, 5000, 5));
        gen.add(new JLabel("Lados (n)")); gen.add(n);
        gen.add(new JLabel("Raio")); gen.add(r);
        JButton reg = new JButton("Regular"), irr = new JButton("Irregular");
        reg.addActionListener(e -> ctx.generatePolygon(true, (Integer) n.getValue(), ((Number) r.getValue()).doubleValue()));
        irr.addActionListener(e -> ctx.generatePolygon(false, (Integer) n.getValue(), ((Number) r.getValue()).doubleValue()));
        gen.add(reg); gen.add(irr);
        JButton center = new JButton("Centralizar na origem"), clear = new JButton("Limpar plano");
        center.addActionListener(e -> ctx.transforms.centerOnOrigin());
        clear.addActionListener(e -> ctx.clearAll());
        gen.add(center); gen.add(clear);
        p.add(javax.swing.Box.createVerticalStrut(6)); p.add(left(gen));
        return p;
    }

    private static JComponent left(JComponent c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); return c; }
}
