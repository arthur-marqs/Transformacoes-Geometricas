package cg.transform;

import cg.geometry.Matrix3;
import cg.geometry.Shape2D;
import cg.scene.Scene;
import cg.terminal.Console;

import java.util.ArrayList;
import java.util.List;

import static cg.geometry.Matrix3.fmt;

/**
 * Aplica transformações à forma ativa e escreve no terminal TODAS as contas (matrizes e vértice a vértice).
 * Painel e terminal usam esta mesma classe, então sempre mostram os mesmos resultados.
 */
public final class TransformService {
    private final Scene scene;
    private final Console console;
    private final List<TransformOp> queue = new ArrayList<>();
    private final List<Runnable> queueListeners = new ArrayList<>();
    public boolean verbose = true;   // false: só "Pi -> Pi'" em vez da conta completa

    public TransformService(Scene scene, Console console) { this.scene = scene; this.console = console; }

    // ---------- aplicação ----------

    public boolean apply(TransformOp op) {
        Shape2D s = scene.active();
        if (s == null) { console.println("! Não há forma ativa. Gere um polígono, desenhe ou selecione uma forma."); return false; }
        Matrix3 m = op.total();
        console.println("");
        console.println("=== " + op.title + "  em  " + s.name + " ===");
        printMatrices(op, m);

        double[] ox = s.xs(), oy = s.ys();
        double[] nx = new double[ox.length], ny = new double[ox.length];
        console.println(verbose ? "Vértices  (P' = M . [x y 1]^T):" : "Vértices:");
        for (int i = 0; i < ox.length; i++) {
            double[] r = m.apply(ox[i], oy[i]);
            nx[i] = r[0]; ny[i] = r[1];
            printVertex(i, ox[i], oy[i], m, r);
        }
        double[][] oldGuides = { scene.windowGuide(), scene.viewportGuide() };
        s.set(nx, ny);
        if (op.windowRect != null) scene.setGuides(op.windowRect, op.viewportRect);
        scene.pushUndo(() -> { s.set(ox, oy); scene.setGuides(oldGuides[0], oldGuides[1]); });
        double[] g = s.centroid();
        console.println("centróide agora = (" + fmt(g[0]) + ", " + fmt(g[1]) + ")");
        scene.fire();
        return true;
    }

    private void printMatrices(TransformOp op, Matrix3 total) {
        List<TransformOp.Step> st = op.steps();
        for (TransformOp.Step step : st) {
            String[] r = step.matrix.rows();
            console.println(String.format("  %-14s %s", step.label + " =", r[0]));
            console.println(String.format("  %-14s %s", "", r[1]));
            console.println(String.format("  %-14s %s", "", r[2]));
        }
        if (st.size() > 1) {
            StringBuilder f = new StringBuilder("M = ");
            for (int i = st.size() - 1; i >= 0; i--) f.append(st.get(i).label).append(i > 0 ? " . " : "");
            console.println("  " + f);
            String[] r = total.rows();
            console.println("  " + String.format("%-14s %s", "M =", r[0]));
            console.println("  " + String.format("%-14s %s", "", r[1]));
            console.println("  " + String.format("%-14s %s", "", r[2]));
        }
    }

    private void printVertex(int i, double x, double y, Matrix3 m, double[] r) {
        if (!verbose) {
            console.println("  P" + i + " (" + fmt(x) + ", " + fmt(y) + ") -> (" + fmt(r[0]) + ", " + fmt(r[1]) + ")");
            return;
        }
        double[][] a = m.m;
        console.println("  P" + i + " = (" + fmt(x) + ", " + fmt(y) + ")");
        console.println("     x' = " + fmt(a[0][0]) + "*" + fmt(x) + " + " + fmt(a[0][1]) + "*" + fmt(y) + " + " + fmt(a[0][2]) + " = " + fmt(r[0]));
        console.println("     y' = " + fmt(a[1][0]) + "*" + fmt(x) + " + " + fmt(a[1][1]) + "*" + fmt(y) + " + " + fmt(a[1][2]) + " = " + fmt(r[1]));
    }

    // ---------- fila de composição ----------

    public List<TransformOp> queue() { return queue; }
    public void addQueueListener(Runnable r) { queueListeners.add(r); }
    private void queueChanged() { for (Runnable r : queueListeners) r.run(); }

    public void enqueue(TransformOp op) { queue.add(op); console.println("+ fila[" + queue.size() + "]: " + op.title); queueChanged(); }
    public void clearQueue() { queue.clear(); queueChanged(); }

    /** Compõe a fila na ordem de inserção (primeiro item é aplicado primeiro) e aplica de uma vez. */
    public boolean applyQueue() {
        if (queue.isEmpty()) { console.println("! A fila está vazia."); return false; }
        TransformOp all = new TransformOp("Composição de " + queue.size() + " transformações");
        for (TransformOp op : queue) all.addAll(op);
        TransformOp last = queue.get(queue.size() - 1);
        all.windowRect = last.windowRect; all.viewportRect = last.viewportRect;
        boolean ok = apply(all);
        if (ok) clearQueue();
        return ok;
    }

    // ---------- utilitários ----------

    public void undo() {
        console.println(scene.undo() ? "Desfeito." : "! Nada para desfazer.");
    }

    public void reset() {
        Shape2D s = scene.active();
        if (s == null) { console.println("! Não há forma ativa."); return; }
        double[] ox = s.xs(), oy = s.ys();
        s.reset();
        scene.pushUndo(() -> s.set(ox, oy));
        scene.fire();
        console.println(s.name + " voltou ao estado original.");
    }

    /** Leva o centróide da forma ativa para (0,0) (uma translação, registrada com as contas). */
    public void centerOnOrigin() {
        Shape2D s = scene.active();
        if (s == null) { console.println("! Não há forma ativa."); return; }
        double[] g = s.centroid();
        apply(Transforms.translate(-g[0], -g[1]));
    }
}
