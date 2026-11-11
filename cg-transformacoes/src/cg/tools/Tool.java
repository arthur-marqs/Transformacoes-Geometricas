package cg.tools;

import cg.app.AppContext;
import cg.render.Renderer;

import java.awt.Cursor;
import java.awt.event.KeyEvent;

/**
 * Ferramenta de mouse orientada a eventos. Para criar uma ferramenta nova:
 *   1) estenda esta classe e sobrescreva só os eventos de que precisa;
 *   2) registre em MainWindow (ToolRegistry.register) - o botão aparece sozinho no painel.
 */
public abstract class Tool {
    protected final AppContext ctx;
    protected Tool(AppContext ctx) { this.ctx = ctx; }

    public abstract String name();
    public abstract String hint();

    public void pressed(ToolEvent e) {}
    public void released(ToolEvent e) {}
    public void dragged(ToolEvent e) {}
    public void moved(ToolEvent e) {}
    public void keyPressed(KeyEvent e) {}
    /** Abandona uma operação em andamento (ao trocar de ferramenta ou apertar Esc). */
    public void cancel() {}
    /** Desenho temporário (rubber band) feito a cada quadro, por cima da cena. */
    public void drawPreview(Renderer r) {}
    public int cursorType() { return Cursor.CROSSHAIR_CURSOR; }
}
