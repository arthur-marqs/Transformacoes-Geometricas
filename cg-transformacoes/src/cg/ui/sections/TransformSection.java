package cg.ui.sections;

import cg.app.AppContext;
import cg.transform.TransformOp;
import cg.transform.Transforms;
import cg.transform.Transforms.Reflection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;

/** Todas as transformações da aula G5 sobre a forma ativa, mais fila de composição. As contas saem no terminal. */
public final class TransformSection implements ToolSection {
    private static final String[] NAMES = { "Translação", "Escala", "Rotação", "Reflexão", "Cisalhamento", "Janela → Viewport" };

    private final AppContext ctx;
    private JSpinner tx, ty, sx, sy, sPx, sPy, ang, rPx, rPy, k, shPx, shPy;
    private final JSpinner[] win = new JSpinner[4], vp = new JSpinner[4];
    private JComboBox<Reflection> refl;
    private JComboBox<String> shAxis;
    private JComboBox<String> op;

    public TransformSection(AppContext ctx) { this.ctx = ctx; }

    @Override public String title() { return "Transformações 2D (G5)"; }

    @Override public JComponent build() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        op = new JComboBox<>(NAMES);
        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);
        tx = num(50); ty = num(30);
        cardPanel.add(form("Deslocamento", new String[] { "tx", "ty" }, new JComponent[] { tx, ty }), NAMES[0]);
        sx = num(2); sy = num(2); sPx = num(0); sPy = num(0);
        cardPanel.add(form("Fatores e ponto fixo", new String[] { "sx", "sy", "ponto fixo x", "ponto fixo y" },
                new JComponent[] { sx, sy, sPx, sPy }), NAMES[1]);
        ang = num(30); rPx = num(0); rPy = num(0);
        cardPanel.add(form("Ângulo (anti-horário) e ponto fixo", new String[] { "graus", "ponto fixo x", "ponto fixo y" },
                new JComponent[] { ang, rPx, rPy }), NAMES[2]);
        refl = new JComboBox<>(Reflection.values());
        cardPanel.add(form("Reflexão", new String[] { "em torno de" }, new JComponent[] { refl }), NAMES[3]);
        shAxis = new JComboBox<>(new String[] { "x  (x' = x + k·y)", "y  (y' = y + k·x)" });
        k = num(0.5); shPx = num(0); shPy = num(0);
        cardPanel.add(form("Cisalhamento", new String[] { "eixo", "k", "ponto fixo x", "ponto fixo y" },
                new JComponent[] { shAxis, k, shPx, shPy }), NAMES[4]);
        cardPanel.add(windowForm(), NAMES[5]);
        op.addActionListener(e -> cards.show(cardPanel, (String) op.getSelectedItem()));

        JButton apply = new JButton("Aplicar"), add = new JButton("+ Fila");
        apply.addActionListener(e -> run(false));
        add.addActionListener(e -> run(true));
        JPanel row = new JPanel(new GridLayout(1, 2, 4, 4));
        row.add(apply); row.add(add);

        DefaultListModel<String> qm = new DefaultListModel<>();
        JList<String> qlist = new JList<>(qm);
        qlist.setVisibleRowCount(4);
        ctx.transforms.addQueueListener(() -> {
            qm.clear();
            int i = 1;
            for (TransformOp o : ctx.transforms.queue()) qm.addElement((i++) + ". " + o.title);
        });
        JButton qApply = new JButton("Aplicar fila (composta)"), qClear = new JButton("Limpar fila");
        qApply.addActionListener(e -> ctx.transforms.applyQueue());
        qClear.addActionListener(e -> ctx.transforms.clearQueue());
        JPanel qrow = new JPanel(new GridLayout(1, 2, 4, 4));
        qrow.add(qApply); qrow.add(qClear);
        JPanel queue = new JPanel();
        queue.setLayout(new BoxLayout(queue, BoxLayout.Y_AXIS));
        queue.setBorder(BorderFactory.createTitledBorder("Fila: M = Mn · … · M2 · M1"));
        queue.add(left(new JScrollPane(qlist))); queue.add(left(qrow));

        JButton undo = new JButton("Desfazer"), reset = new JButton("Original"), center = new JButton("Centralizar");
        undo.addActionListener(e -> ctx.transforms.undo());
        reset.addActionListener(e -> ctx.transforms.reset());
        center.addActionListener(e -> ctx.transforms.centerOnOrigin());
        JPanel urow = new JPanel(new GridLayout(1, 3, 4, 4));
        urow.add(undo); urow.add(reset); urow.add(center);

        p.add(left(new JLabel("Atua na forma ativa (vértices brancos):")));
        p.add(left(op)); p.add(javax.swing.Box.createVerticalStrut(4));
        p.add(left(cardPanel)); p.add(javax.swing.Box.createVerticalStrut(4));
        p.add(left(row)); p.add(javax.swing.Box.createVerticalStrut(6));
        p.add(left(queue)); p.add(javax.swing.Box.createVerticalStrut(6));
        p.add(left(urow));
        return p;
    }

    private void run(boolean enqueue) {
        try {
            TransformOp o = buildOp();
            if (enqueue) ctx.transforms.enqueue(o); else ctx.transforms.apply(o);
        } catch (IllegalArgumentException ex) {
            ctx.console.println("! " + ex.getMessage());
        }
    }

    private TransformOp buildOp() {
        switch (op.getSelectedIndex()) {
            case 0: return Transforms.translate(v(tx), v(ty));
            case 1: return Transforms.scale(v(sx), v(sy), v(sPx), v(sPy));
            case 2: return Transforms.rotate(v(ang), v(rPx), v(rPy));
            case 3: return Transforms.reflect((Reflection) refl.getSelectedItem());
            case 4: return Transforms.shear(shAxis.getSelectedIndex() == 0, v(k), v(shPx), v(shPy));
            default:
                return Transforms.windowViewport(
                        new double[] { v(win[0]), v(win[1]), v(win[2]), v(win[3]) },
                        new double[] { v(vp[0]), v(vp[1]), v(vp[2]), v(vp[3]) });
        }
    }

    private JComponent windowForm() {
        JPanel f = new JPanel(new GridLayout(0, 4, 4, 4));
        f.setBorder(BorderFactory.createTitledBorder("Janela (mundo) → Viewport"));
        double[] w = { -150, -150, 150, 150 }, v = { 100, 50, 300, 200 };
        String[] n = { "xmin", "ymin", "xmax", "ymax" };
        f.add(new JLabel("Janela")); f.add(new JLabel("")); f.add(new JLabel("")); f.add(new JLabel(""));
        for (int i = 0; i < 4; i++) { f.add(new JLabel("w." + n[i])); f.add(win[i] = num(w[i])); }
        f.add(new JLabel("Viewport")); f.add(new JLabel("")); f.add(new JLabel("")); f.add(new JLabel(""));
        for (int i = 0; i < 4; i++) { f.add(new JLabel("v." + n[i])); f.add(vp[i] = num(v[i])); }
        return f;
    }

    /** Formulário de pares rótulo/campo. */
    private static JComponent form(String title, String[] labels, JComponent[] fields) {
        JPanel f = new JPanel(new GridLayout(0, 2, 4, 4));
        f.setBorder(BorderFactory.createTitledBorder(title));
        for (int i = 0; i < labels.length; i++) { f.add(new JLabel(labels[i])); f.add(fields[i]); }
        JPanel wrap = new JPanel(new java.awt.BorderLayout());   // evita esticar as linhas na vertical
        wrap.add(f, java.awt.BorderLayout.NORTH);
        return wrap;
    }

    private static JSpinner num(double v) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v, -100000.0, 100000.0, 1.0));
        ((JSpinner.NumberEditor) s.getEditor()).getTextField().setColumns(5);
        return s;
    }

    private static double v(JSpinner s) { return ((Number) s.getValue()).doubleValue(); }

    private static JComponent left(JComponent c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); return c; }
}
