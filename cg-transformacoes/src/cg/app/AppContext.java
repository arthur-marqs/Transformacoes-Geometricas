package cg.app;

import cg.geometry.Shape2D;
import cg.geometry.ShapeFactory;
import cg.render.Theme;
import cg.render.View;
import cg.scene.Scene;
import cg.terminal.Console;
import cg.terminal.Report;
import cg.tools.ToolRegistry;
import cg.transform.TransformService;

import java.util.ArrayList;
import java.util.List;

/** Objetos compartilhados por toda a aplicação (injetados nos painéis e ferramentas). */
public final class AppContext {
    public final Theme theme = new Theme();
    public final View view = new View();
    public final Scene scene = new Scene();
    public final Console console = new Console();
    public final TransformService transforms = new TransformService(scene, console);
    public final ToolRegistry tools = new ToolRegistry();
    public DisplayControl display;

    // estado exibido na barra de status
    public int cursorX, cursorY, cursorSX, cursorSY;
    public boolean cursorValid;
    public String toolInfo = "";
    public int displayW, displayH;
    public double deviceScale = 1;

    private Runnable repaint = () -> {};
    private final List<Runnable> statusListeners = new ArrayList<>();
    private long irregularSeed = 7;

    public AppContext() {
        scene.addListener(this::redraw);
        theme.addListener(this::redraw);
    }

    public void setRepaint(Runnable r) { repaint = r; }
    public void redraw() { repaint.run(); }
    public void addStatusListener(Runnable r) { statusListeners.add(r); }
    public void statusChanged() { for (Runnable r : statusListeners) r.run(); }

    /** Gera um polígono regular/irregular centrado na origem, substituindo o gerado anteriormente. */
    public void generatePolygon(boolean regular, int n, double radius) {
        Shape2D s = regular ? ShapeFactory.regular(n, radius) : ShapeFactory.irregular(n, radius, irregularSeed++);
        scene.replaceGenerated(s);
        Report.shape(console, "Polígono gerado na origem: " + s.name + " (raio " + (int) radius + ")", s);
    }

    public void clearAll() {
        scene.clear();
        console.println("Plano limpo.");
    }
}
