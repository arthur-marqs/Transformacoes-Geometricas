package cg.ui.sections;

import cg.app.AppContext;
import cg.render.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Cores configuráveis: presets e um seletor por elemento (fundo, forma, eixos, ...). */
public final class ColorSection implements ToolSection {
    private final AppContext ctx;
    public ColorSection(AppContext ctx) { this.ctx = ctx; }

    @Override public String title() { return "Cores"; }

    @Override public JComponent build() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JComboBox<Theme.Preset> presets = new JComboBox<>(Theme.PRESETS.toArray(new Theme.Preset[0]));
        presets.addActionListener(e -> ctx.theme.apply((Theme.Preset) presets.getSelectedItem()));
        JPanel pr = new JPanel(new GridLayout(1, 1));
        pr.setBorder(BorderFactory.createTitledBorder("Esquema pronto"));
        pr.add(presets);
        p.add(left(pr));

        JPanel grid = new JPanel(new GridLayout(0, 2, 4, 4));
        grid.setBorder(BorderFactory.createTitledBorder("Personalizar (clique na cor)"));
        List<JButton> swatches = new ArrayList<>();
        for (int i = 0; i < Theme.NAMES.length; i++) {
            final int idx = i;
            JButton b = new JButton(" ");
            b.setPreferredSize(new Dimension(60, 22));
            b.addActionListener(e -> {
                Color c = JColorChooser.showDialog(p, Theme.NAMES[idx], new Color(ctx.theme.get(idx)));
                if (c != null) ctx.theme.set(idx, c.getRGB());
            });
            swatches.add(b);
            grid.add(new JLabel(Theme.NAMES[i])); grid.add(b);
        }
        Runnable sync = () -> { for (int i = 0; i < swatches.size(); i++) swatches.get(i).setBackground(new Color(ctx.theme.get(i))); };
        ctx.theme.addListener(sync); sync.run();
        p.add(javax.swing.Box.createVerticalStrut(6)); p.add(left(grid));
        return p;
    }

    private static JComponent left(JComponent c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); return c; }
}
