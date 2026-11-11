package cg.tools;

import cg.app.AppContext;

import java.awt.Cursor;

/** Arrasta a vista (útil em touchpad, que não tem botão do meio). Setas do teclado também movem a vista. */
public final class PanTool extends Tool {
    private int lastX, lastY;

    public PanTool(AppContext ctx) { super(ctx); }

    @Override public String name() { return "Mover vista"; }
    @Override public String hint() { return "Arraste para mover o plano. (Botão do meio ou setas funcionam em qualquer ferramenta.)"; }
    @Override public int cursorType() { return Cursor.MOVE_CURSOR; }

    @Override public void pressed(ToolEvent e) { lastX = e.screenX; lastY = e.screenY; }

    @Override public void dragged(ToolEvent e) {
        ctx.view.panX += e.screenX - lastX;
        ctx.view.panY += e.screenY - lastY;
        lastX = e.screenX; lastY = e.screenY;
        ctx.redraw();
    }
}
