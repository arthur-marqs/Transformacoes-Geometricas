package cg.transform;

import cg.geometry.Matrix3;

import java.util.ArrayList;
import java.util.List;

/** Uma transformação descrita como lista ordenada de matrizes (passos). Ex.: escala com ponto fixo = T(p) · S · T(-p). */
public final class TransformOp {
    public static final class Step {
        public final String label; public final Matrix3 matrix;
        Step(String label, Matrix3 matrix) { this.label = label; this.matrix = matrix; }
    }

    public final String title;
    private final List<Step> steps = new ArrayList<>();
    /** Retângulos {xmin,ymin,xmax,ymax} desenhados como guias (só para janela->viewport); podem ser null. */
    public double[] windowRect, viewportRect;

    public TransformOp(String title) { this.title = title; }

    public TransformOp add(String label, Matrix3 m) { steps.add(new Step(label, m)); return this; }
    public TransformOp addAll(TransformOp other) { steps.addAll(other.steps); return this; }
    public List<Step> steps() { return steps; }

    /** Matriz resultante: passos são aplicados na ordem da lista, logo M = Mn · ... · M2 · M1. */
    public Matrix3 total() {
        Matrix3 t = Matrix3.identity();
        for (Step s : steps) t = s.matrix.mul(t);
        return t;
    }
}
