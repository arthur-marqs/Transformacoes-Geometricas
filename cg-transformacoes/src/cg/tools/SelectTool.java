package cg.tools;

import cg.app.AppContext;
import cg.geometry.Shape2D;

import java.awt.Cursor;

/** Clique perto de uma forma para torná-la a forma ativa (alvo das transformações). */
public final class SelectTool extends Tool {
    public SelectTool(AppContext ctx) { super(ctx); }

    @Override public String name() { return "Selecionar"; }
    @Override public String hint() { return "Clique sobre uma forma para torná-la ativa (vértices brancos). Delete remove a ativa."; }
    @Override public int cursorType() { return Cursor.HAND_CURSOR; }

    @Override public void pressed(ToolEvent e) {
        if (e.button != 1) return;
        double tol = Math.max(2.0, 8.0 / ctx.view.zoom);        // ~8 pixels reais de tolerância
        Shape2D best = null; double bd = tol;
        for (Shape2D s : ctx.scene.shapes()) {
            double d = s.distanceTo(e.cellX, e.cellY);
            if (d <= bd) { bd = d; best = s; }
        }
        if (best != null) { ctx.scene.setActive(best); ctx.console.println("Forma ativa: " + best.name); }
    }
}
