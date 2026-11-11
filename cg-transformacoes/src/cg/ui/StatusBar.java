package cg.ui;

import cg.app.AppContext;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.Insets;

/** Barra inferior: coordenadas ao vivo (mundo e pixel de tela), zoom, resolução do display e info da ferramenta. */
public final class StatusBar extends JPanel {
    private final AppContext ctx;
    private final JLabel coords = new JLabel(), zoom = new JLabel(), res = new JLabel(), info = new JLabel();

    public StatusBar(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        Font f = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        for (JLabel l : new JLabel[] { coords, zoom, res, info }) l.setFont(f);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 22); c.anchor = GridBagConstraints.WEST;
        coords.setPreferredSize(new java.awt.Dimension(330, 16));
        add(coords, c); add(zoom, c); add(res, c);
        c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 0, 0, 0);
        add(info, c);
        ctx.addStatusListener(this::refresh);
        refresh();
    }

    private void refresh() {
        coords.setText(ctx.cursorValid
                ? String.format("Cursor: mundo (%d, %d)  tela (%d, %d)", ctx.cursorX, ctx.cursorY, ctx.cursorSX, ctx.cursorSY)
                : "Cursor: —");
        zoom.setText("Zoom " + ctx.view.zoom + "×");
        res.setText(String.format("Display %d×%d px reais%s", ctx.displayW, ctx.displayH,
                ctx.deviceScale > 1.001 ? String.format(" (escala do SO %d%%)", Math.round(ctx.deviceScale * 100)) : ""));
        info.setText(ctx.toolInfo);
    }
}
