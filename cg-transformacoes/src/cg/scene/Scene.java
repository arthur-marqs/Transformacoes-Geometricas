package cg.scene;

import cg.geometry.Shape2D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Modelo do que está no plano: formas, forma ativa (alvo das transformações), guias e pilha de desfazer. */
public final class Scene {
    private final List<Shape2D> shapes = new ArrayList<>();
    private Shape2D active;
    private final Deque<Runnable> undo = new ArrayDeque<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private double[] windowGuide, viewportGuide;

    public void addListener(Runnable r) { listeners.add(r); }
    public void fire() { for (Runnable r : listeners) r.run(); }

    public List<Shape2D> shapes() { return Collections.unmodifiableList(shapes); }
    public Shape2D active() { return active; }
    public void setActive(Shape2D s) { active = s; fire(); }

    /** Adiciona e torna ativa. Desfazer remove. */
    public void add(Shape2D s) {
        Shape2D prev = active;
        shapes.add(s); active = s;
        undo.push(() -> { shapes.remove(s); active = shapes.contains(prev) ? prev : null; });
        fire();
    }

    /** Gerador de polígono: substitui os polígonos gerados antes (nome começa por Regular/Irregular). */
    public void replaceGenerated(Shape2D s) {
        List<Shape2D> oldShapes = new ArrayList<>(shapes);
        Shape2D oldActive = active;
        shapes.removeIf(o -> o.name.startsWith("Regular") || o.name.startsWith("Irregular"));
        shapes.add(s); active = s;
        undo.push(() -> { shapes.clear(); shapes.addAll(oldShapes); active = oldActive; });
        fire();
    }

    public void removeActive() {
        if (active == null) return;
        Shape2D s = active;
        int idx = shapes.indexOf(s);
        shapes.remove(s);
        active = shapes.isEmpty() ? null : shapes.get(shapes.size() - 1);
        undo.push(() -> { shapes.add(Math.min(idx, shapes.size()), s); active = s; });
        fire();
    }

    public void clear() {
        shapes.clear(); active = null; windowGuide = viewportGuide = null; undo.clear();
        fire();
    }

    public void pushUndo(Runnable r) { undo.push(r); }

    public boolean undo() {
        if (undo.isEmpty()) return false;
        undo.pop().run();
        fire();
        return true;
    }

    public double[] windowGuide() { return windowGuide; }
    public double[] viewportGuide() { return viewportGuide; }
    public void setGuides(double[] w, double[] v) { windowGuide = w; viewportGuide = v; }

    public int nextIndex(String base) {
        int n = 1;
        for (Shape2D s : shapes) if (s.name.startsWith(base)) n++;
        return n;
    }
}
