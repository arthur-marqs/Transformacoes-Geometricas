package cg.ui;

import cg.ui.sections.ToolSection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/** Painel lateral em "sanfona": um cabeçalho por ferramenta; clicar expande a seção e recolhe as outras. */
public final class Sidebar extends JPanel {
    private final List<JToggleButton> headers = new ArrayList<>();
    private final List<JComponent> bodies = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();

    public Sidebar(List<ToolSection> sections) {
        super(new BorderLayout());
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        for (ToolSection s : sections) {
            JToggleButton h = new JToggleButton();
            h.setHorizontalAlignment(JToggleButton.LEFT);
            h.setFocusPainted(false);
            h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            h.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel body = new JPanel(new BorderLayout());
            body.setBorder(BorderFactory.createEmptyBorder(4, 6, 8, 6));
            body.add(s.build(), BorderLayout.CENTER);
            body.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.setVisible(false);

            int idx = headers.size();
            headers.add(h); bodies.add(body); titles.add(s.title());
            h.addActionListener(e -> open(h.isSelected() ? idx : -1));
            col.add(h); col.add(body);
            col.add(Box.createVerticalStrut(2));
        }
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.add(new javax.swing.JLabel(" "));
        JScrollPane sp = new JScrollPane(col, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        int first = -1;
        for (int i = 0; i < sections.size(); i++) if (sections.get(i).initiallyOpen()) { first = i; break; }
        open(first);
    }

    private void open(int idx) {
        for (int i = 0; i < headers.size(); i++) {
            boolean on = i == idx;
            headers.get(i).setSelected(on);
            headers.get(i).setText((on ? "▼  " : "▶  ") + titles.get(i));
            bodies.get(i).setVisible(on);
        }
        revalidate(); repaint();
    }
}
