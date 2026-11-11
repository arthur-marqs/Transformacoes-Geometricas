package cg.transform;

import cg.geometry.Matrix3;

import static cg.geometry.Matrix3.fmt;

/**
 * Fábrica de todas as transformações da aula G5. Para adicionar uma nova transformação:
 * crie um método aqui que devolva um {@link TransformOp} - painel e terminal só precisam chamá-lo.
 */
public final class Transforms {
    private Transforms() {}

    public enum Reflection {
        EIXO_X("eixo x"), EIXO_Y("eixo y"), ORIGEM("origem"), RETA_Y_X("reta y = x");
        public final String label;
        Reflection(String l) { label = l; }
        @Override public String toString() { return label; }
    }

    /** Slide 6: x' = x + tx ; y' = y + ty. */
    public static TransformOp translate(double tx, double ty) {
        return new TransformOp("Translação (" + fmt(tx) + ", " + fmt(ty) + ")")
                .add("T(" + fmt(tx) + "," + fmt(ty) + ")", Matrix3.translation(tx, ty));
    }

    /** Slide 7 (origem) e 22-25 (ponto fixo): T(p) · S · T(-p). */
    public static TransformOp scale(double sx, double sy, double px, double py) {
        return pivot("Escala (" + fmt(sx) + ", " + fmt(sy) + ")", "S(" + fmt(sx) + "," + fmt(sy) + ")",
                Matrix3.scaling(sx, sy), px, py);
    }

    /** Slides 8-11 (origem) e 26-27 (ponto fixo). Ângulo em graus, anti-horário. */
    public static TransformOp rotate(double deg, double px, double py) {
        return pivot("Rotação de " + fmt(deg) + "°", "R(" + fmt(deg) + "°)", Matrix3.rotation(deg), px, py);
    }

    /** Slide 20. Em x: x' = x + k*y. Em y: y' = y + k*x. */
    public static TransformOp shear(boolean alongX, double k, double px, double py) {
        String ax = alongX ? "x" : "y";
        return pivot("Cisalhamento em " + ax + " (k=" + fmt(k) + ")", "Sh" + ax + "(" + fmt(k) + ")",
                alongX ? Matrix3.shearX(k) : Matrix3.shearY(k), px, py);
    }

    /** Slides 18-19. */
    public static TransformOp reflect(Reflection r) {
        Matrix3 m;
        switch (r) {
            case EIXO_X:  m = Matrix3.of(1, 0, 0,  0, -1, 0,  0, 0, 1); break;   // (x, -y)
            case EIXO_Y:  m = Matrix3.of(-1, 0, 0, 0, 1, 0,  0, 0, 1); break;    // (-x, y)
            case ORIGEM:  m = Matrix3.of(-1, 0, 0, 0, -1, 0, 0, 0, 1); break;    // (-x, -y)
            default:      m = Matrix3.of(0, 1, 0,  1, 0, 0,  0, 0, 1); break;    // (y, x)
        }
        return new TransformOp("Reflexão em torno de " + r.label).add("Ref(" + r.label + ")", m);
    }

    /**
     * Slides 29-33 (janela -> viewport): translada a janela para a origem, escala pela razão
     * viewport/janela e translada para o canto da viewport.  w, v = {xmin, ymin, xmax, ymax}.
     */
    public static TransformOp windowViewport(double[] w, double[] v) {
        double ww = w[2] - w[0], wh = w[3] - w[1];
        if (ww == 0 || wh == 0) throw new IllegalArgumentException("A janela precisa ter largura e altura diferentes de zero.");
        double sx = (v[2] - v[0]) / ww, sy = (v[3] - v[1]) / wh;
        TransformOp op = new TransformOp("Janela -> Viewport")
                .add("T(-xwmin,-ywmin)", Matrix3.translation(-w[0], -w[1]))
                .add("S(" + fmt(sx) + "," + fmt(sy) + ")", Matrix3.scaling(sx, sy))
                .add("T(xvmin,yvmin)", Matrix3.translation(v[0], v[1]));
        op.windowRect = w.clone(); op.viewportRect = v.clone();
        return op;
    }

    /** Composição com ponto fixo: se o pivô é (0,0) devolve só o passo; senão T(p) · core · T(-p). */
    private static TransformOp pivot(String title, String label, Matrix3 core, double px, double py) {
        if (px == 0 && py == 0) return new TransformOp(title).add(label, core);
        return new TransformOp(title + " com ponto fixo (" + fmt(px) + ", " + fmt(py) + ")")
                .add("T(" + fmt(-px) + "," + fmt(-py) + ")", Matrix3.translation(-px, -py))
                .add(label, core)
                .add("T(" + fmt(px) + "," + fmt(py) + ")", Matrix3.translation(px, py));
    }
}
