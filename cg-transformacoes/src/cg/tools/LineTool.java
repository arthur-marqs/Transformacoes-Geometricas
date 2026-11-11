package cg.tools;

import cg.app.AppContext;
import cg.geometry.Shape2D;
import cg.render.Renderer;
import cg.terminal.Report;

import java.awt.event.KeyEvent;

/** Reta pelo ponto médio: arraste (ou clique, mova e clique de novo). A pré-visualização acompanha o mouse. */
public final class LineTool extends Tool {
    private int x0, y0, cx, cy;
    private boolean active;

    public LineTool(AppContext ctx) { super(ctx); }

    @Override public String name() { return "Reta (ponto médio)"; }
    @Override public String hint() { return "Arraste de P1 até P2, ou clique em P1 e depois em P2. Esc cancela."; }

    @Override public void pressed(ToolEvent e) {
        if (e.button == 3) { cancel(); return; }
        if (e.button != 1) return;
        if (!active) { active = true; x0 = cx = e.cellX; y0 = cy = e.cellY; update(); ctx.redraw(); }
    }

    @Override public void dragged(ToolEvent e) { moved(e); }

    @Override public void moved(ToolEvent e) {
        if (!active) return;
        cx = e.cellX; cy = e.cellY; update(); ctx.redraw();
    }

    @Override public void released(ToolEvent e) {
        if (!active || e.button != 1) return;
        if (e.cellX == x0 && e.cellY == y0) return;       // soltou no mesmo lugar: modo "clique, clique"
        cx = e.cellX; cy = e.cellY;
        commit();
    }

    @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { cancel(); ctx.redraw(); } }

    @Override public void cancel() { active = false; ctx.toolInfo = ""; ctx.statusChanged(); }

    @Override public void drawPreview(Renderer r) {
        if (!active) return;
        r.line(x0, y0, cx, cy, r.theme().preview());
        r.cell(x0, y0, r.theme().vertex());
    }

    private void update() {
        int dx = cx - x0, dy = cy - y0;
        ctx.toolInfo = String.format("P1(%d,%d)  P2(%d,%d)  Δ=(%d,%d)  L=%.2f", x0, y0, cx, cy, dx, dy, Math.hypot(dx, dy));
        ctx.statusChanged();
    }

    private void commit() {
        Shape2D s = new Shape2D("Reta " + ctx.scene.nextIndex("Reta"), false,
                new double[] { x0, cx }, new double[] { y0, cy });
        ctx.scene.add(s);
        Report.midpoint(ctx.console, x0, y0, cx, cy);
        active = false; ctx.toolInfo = ""; ctx.statusChanged();
    }
}
