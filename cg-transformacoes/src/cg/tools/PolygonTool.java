package cg.tools;

import cg.app.AppContext;
import cg.geometry.Shape2D;
import cg.render.Renderer;
import cg.terminal.Report;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** Polígono livre (regular ou irregular): clique nos vértices; duplo clique, botão direito ou Enter fecha. */
public final class PolygonTool extends Tool {
    private final List<int[]> pts = new ArrayList<>();
    private int cx, cy;

    public PolygonTool(AppContext ctx) { super(ctx); }

    @Override public String name() { return "Polígono"; }
    @Override public String hint() { return "Clique nos vértices. Duplo clique / botão direito / Enter fecha. Backspace apaga o último; Esc cancela."; }

    @Override public void pressed(ToolEvent e) {
        if (e.button == 3) { close(); return; }
        if (e.button != 1) return;
        int[] last = pts.isEmpty() ? null : pts.get(pts.size() - 1);
        if (last == null || last[0] != e.cellX || last[1] != e.cellY) pts.add(new int[] { e.cellX, e.cellY });
        if (e.clickCount >= 2) { close(); return; }
        cx = e.cellX; cy = e.cellY; update(); ctx.redraw();
    }

    @Override public void moved(ToolEvent e) { cx = e.cellX; cy = e.cellY; if (!pts.isEmpty()) { update(); ctx.redraw(); } }
    @Override public void dragged(ToolEvent e) { moved(e); }

    @Override public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER: close(); break;
            case KeyEvent.VK_ESCAPE: cancel(); ctx.redraw(); break;
            case KeyEvent.VK_BACK_SPACE: if (!pts.isEmpty()) pts.remove(pts.size() - 1); update(); ctx.redraw(); break;
            default: break;
        }
    }

    @Override public void cancel() { pts.clear(); ctx.toolInfo = ""; ctx.statusChanged(); }

    @Override public void drawPreview(Renderer r) {
        if (pts.isEmpty()) return;
        int pv = r.theme().preview();
        for (int i = 0; i + 1 < pts.size(); i++) r.line(pts.get(i)[0], pts.get(i)[1], pts.get(i + 1)[0], pts.get(i + 1)[1], pv);
        int[] last = pts.get(pts.size() - 1), first = pts.get(0);
        r.line(last[0], last[1], cx, cy, pv);
        if (pts.size() >= 2) r.line(cx, cy, first[0], first[1], r.theme().ghost());   // aresta de fechamento prevista
        for (int[] p : pts) r.cell(p[0], p[1], r.theme().vertex());
    }

    private void update() {
        if (pts.isEmpty()) { ctx.toolInfo = ""; ctx.statusChanged(); return; }
        int[] l = pts.get(pts.size() - 1);
        ctx.toolInfo = String.format("%d vértice(s)  último(%d,%d)  Δ=(%d,%d)  L=%.2f",
                pts.size(), l[0], l[1], cx - l[0], cy - l[1], Math.hypot(cx - l[0], cy - l[1]));
        ctx.statusChanged();
    }

    private void close() {
        if (pts.size() < 3) { if (pts.size() > 0) ctx.console.println("! Um polígono precisa de pelo menos 3 vértices."); cancel(); ctx.redraw(); return; }
        double[] x = new double[pts.size()], y = new double[pts.size()];
        for (int i = 0; i < x.length; i++) { x[i] = pts.get(i)[0]; y[i] = pts.get(i)[1]; }
        Shape2D s = new Shape2D("Polígono " + ctx.scene.nextIndex("Polígono"), true, x, y);
        ctx.scene.add(s);
        Report.shape(ctx.console, "Polígono desenhado: " + s.name + " (" + x.length + " vértices)", s);
        cancel();
    }
}
