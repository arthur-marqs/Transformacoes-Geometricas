package cg;

import cg.app.AppContext;
import cg.geometry.Matrix3;
import cg.geometry.Shape2D;
import cg.geometry.ShapeFactory;
import cg.raster.MidpointLine;
import cg.render.Framebuffer;
import cg.render.Renderer;
import cg.terminal.CommandInterpreter;
import cg.transform.TransformOp;
import cg.transform.Transforms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Testes sem interface:  java -cp out:testout cg.SelfTest */
public final class SelfTest {
    private static int ok, fail;

    private static void check(String name, boolean cond) {
        if (cond) ok++; else { fail++; System.out.println("FALHOU: " + name); }
    }
    private static boolean near(double a, double b) { return Math.abs(a - b) < 1e-9; }

    private static List<int[]> pixels(int x0, int y0, int x1, int y1) {
        List<int[]> l = new ArrayList<>();
        MidpointLine.draw(x0, y0, x1, y1, (x, y) -> l.add(new int[] { x, y }));
        return l;
    }
    private static Set<Long> set(List<int[]> l) {
        Set<Long> s = new HashSet<>();
        for (int[] p : l) s.add(((long) p[0] << 32) ^ (p[1] & 0xffffffffL));
        return s;
    }

    public static void main(String[] args) {
        // ---------- G3, slide 28: (20,10) -> (30,18) ----------
        int[][] exp = { {20,10},{21,11},{22,12},{23,12},{24,13},{25,14},{26,15},{27,16},{28,16},{29,17},{30,18} };
        List<int[]> got = pixels(20, 10, 30, 18);
        boolean same = got.size() == exp.length;
        for (int i = 0; same && i < exp.length; i++) same = got.get(i)[0] == exp[i][0] && got.get(i)[1] == exp[i][1];
        check("slide 28: pixels", same);
        List<int[]> log = new ArrayList<>();
        MidpointLine.draw(20, 10, 30, 18, (x, y) -> {}, log);
        int[] dExp = { 6, 2, -2, 14, 10, 6, 2, -2, 14, 10 };
        boolean dOk = log.size() == dExp.length;
        for (int i = 0; dOk && i < dExp.length; i++) dOk = log.get(i)[0] == dExp[i];
        check("slide 28: tabela de d (6 2 -2 14 10 6 2 -2 14 10)", dOk);
        int[] pr = MidpointLine.params(20, 10, 30, 18);
        check("slide 28: incE=16 incNE=-4 d0=6", pr[3] == 16 && pr[4] == -4 && pr[2] == 6);

        // ---------- G3: todos os octantes ----------
        Random rnd = new Random(42);
        boolean all = true;
        for (int n = 0; n < 4000 && all; n++) {
            int x0 = rnd.nextInt(201) - 100, y0 = rnd.nextInt(201) - 100, x1 = rnd.nextInt(201) - 100, y1 = rnd.nextInt(201) - 100;
            List<int[]> a = pixels(x0, y0, x1, y1), b = pixels(x1, y1, x0, y0);
            int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
            boolean good = a.size() == steps + 1 && set(a).equals(set(b));                     // contagem e simetria
            boolean hasA = false, hasB = false;
            for (int[] p : a) { hasA |= p[0] == x0 && p[1] == y0; hasB |= p[0] == x1 && p[1] == y1; }
            good &= hasA && hasB;                                                               // extremos
            for (int i = 1; good && i < a.size(); i++)                                          // sem buracos
                good = Math.abs(a.get(i)[0] - a.get(i - 1)[0]) <= 1 && Math.abs(a.get(i)[1] - a.get(i - 1)[1]) <= 1;
            double dx = x1 - x0, dy = y1 - y0, len = Math.hypot(dx, dy);
            for (int[] p : a) if (len > 0) good &= Math.abs((p[0] - x0) * dy - (p[1] - y0) * dx) / len <= 0.71 + 1e-9;  // erro < 1 px
            if (!good) { all = false; System.out.println("  reta ruim: " + x0 + "," + y0 + " -> " + x1 + "," + y1); }
        }
        check("ponto médio: 4000 retas aleatórias (extremos, simetria, sem buracos, erro)", all);
        check("ponto: (5,5)->(5,5)", pixels(5, 5, 5, 5).size() == 1);
        check("vertical", pixels(3, -2, 3, 6).size() == 9);

        // ---------- G5: matrizes ----------
        double[] p;
        p = Transforms.scale(2, 2, 2, 1).total().apply(2, 1);
        check("slide 25: escala 2 ponto fixo (2,1): (2,1)->(2,1)", near(p[0], 2) && near(p[1], 1));
        p = Transforms.scale(2, 2, 2, 1).total().apply(4, 1);
        check("slide 25: (4,1)->(6,1)", near(p[0], 6) && near(p[1], 1));
        p = Transforms.rotate(90, 0, 0).total().apply(1, 0);
        check("rotação 90: (1,0)->(0,1)", near(p[0], 0) && near(p[1], 1));
        p = Transforms.rotate(90, 1, 1).total().apply(2, 1);
        check("rotação 90 em torno de (1,1): (2,1)->(1,2)", near(p[0], 1) && near(p[1], 2));
        p = Transforms.translate(3, -4).total().apply(1, 1);
        check("translação", near(p[0], 4) && near(p[1], -3));
        p = Transforms.reflect(Transforms.Reflection.EIXO_X).total().apply(2, 3);
        check("reflexão eixo x", near(p[0], 2) && near(p[1], -3));
        p = Transforms.reflect(Transforms.Reflection.EIXO_Y).total().apply(2, 3);
        check("reflexão eixo y", near(p[0], -2) && near(p[1], 3));
        p = Transforms.reflect(Transforms.Reflection.ORIGEM).total().apply(2, 3);
        check("reflexão origem", near(p[0], -2) && near(p[1], -3));
        p = Transforms.reflect(Transforms.Reflection.RETA_Y_X).total().apply(2, 3);
        check("reflexão y=x", near(p[0], 3) && near(p[1], 2));
        p = Transforms.shear(true, 0.5, 0, 0).total().apply(2, 4);
        check("cisalhamento x: (2,4)->(4,4)", near(p[0], 4) && near(p[1], 4));
        p = Transforms.shear(false, 2, 0, 0).total().apply(3, 1);
        check("cisalhamento y: (3,1)->(3,7)", near(p[0], 3) && near(p[1], 7));
        Matrix3 rr = Transforms.rotate(37, 0, 0).total().mul(Transforms.rotate(-37, 0, 0).total());
        check("R(θ)·R(-θ) = I", near(rr.m[0][0], 1) && near(rr.m[1][1], 1) && near(rr.m[0][1], 0) && near(rr.m[1][0], 0));
        Matrix3 tr = Transforms.translate(5, 0).total(), ro = Transforms.rotate(90, 0, 0).total();
        double[] a1 = ro.mul(tr).apply(1, 0), a2 = tr.mul(ro).apply(1, 0);
        check("não-comutatividade: R·T ≠ T·R", !(near(a1[0], a2[0]) && near(a1[1], a2[1])));
        TransformOp wv = Transforms.windowViewport(new double[] { -10, -5, 10, 5 }, new double[] { 100, 50, 300, 150 });
        double[] c0 = wv.total().apply(-10, -5), c1 = wv.total().apply(10, 5), cm = wv.total().apply(0, 0);
        check("janela->viewport: cantos e centro", near(c0[0], 100) && near(c0[1], 50) && near(c1[0], 300) && near(c1[1], 150) && near(cm[0], 200) && near(cm[1], 100));

        // ---------- polígono na origem ----------
        for (int n = 3; n <= 12; n++) {
            double[] c = ShapeFactory.regular(n, 100).centroid();
            check("regular n=" + n + " centróide na origem", near(c[0] + 1, 1) && near(c[1] + 1, 1));
        }
        double[] ci = ShapeFactory.irregular(9, 100, 3).centroid();
        check("irregular centróide na origem", Math.abs(ci[0]) < 1e-9 && Math.abs(ci[1]) < 1e-9);

        // ---------- interpretador + serviço (sem janela) ----------
        AppContext ctx = new AppContext();
        List<String> out = new ArrayList<>();
        ctx.console.onLine(out::add);
        CommandInterpreter cli = new CommandInterpreter(ctx);
        cli.execute("regular 4 100");
        Shape2D sq = ctx.scene.active();
        check("regular 4 100: 4 vértices", sq != null && sq.size() == 4);
        cli.execute("rotacionar 45");
        check("rotacionar 45: vértice 0 do quadrado (135°) vai para 180°: (-100, 0)",
                Math.abs(sq.x(0) + 100) < 1e-9 && Math.abs(sq.y(0)) < 1e-9);
        cli.execute("desfazer");
        check("desfazer volta ao original", !sq.isModified());
        cli.execute("transladar 10 20");
        double[] g = sq.centroid();
        check("transladar 10 20: centróide (10,20)", near(g[0], 10) && near(g[1], 20));
        cli.execute("centralizar");
        g = sq.centroid();
        check("centralizar: centróide (0,0)", Math.abs(g[0]) < 1e-9 && Math.abs(g[1]) < 1e-9);
        cli.execute("fila add escalar 2");
        cli.execute("fila add rotacionar 90");
        cli.execute("fila aplicar");
        check("fila composta aplicada", sq.isModified() && ctx.transforms.queue().isEmpty());
        cli.execute("resetar");
        check("resetar", !sq.isModified());
        cli.execute("linha 20 10 30 18");
        boolean table = false;
        for (String s : out) table |= s.contains("incE = 2dy = 16") && s.contains("incNE = 2(dy-dx) = -4");
        check("terminal imprime d0/incE/incNE da reta", table);
        cli.execute("rotacionar");
        check("erro de uso é tratado (sem exceção)", out.get(out.size() - 1).startsWith("! "));

        // ---------- display em pixels reais: zoom 1 => 1 célula = 1 pixel físico ----------
        Framebuffer fb = new Framebuffer();
        fb.resize(400, 300);
        ctx.scene.clear();
        ctx.view.zoom = 1;
        ctx.view.panX = ctx.view.panY = 0;
        Renderer rd = new Renderer(ctx.view, ctx.theme);
        Shape2D ln = new Shape2D("t", false, new double[] { 0, 50 }, new double[] { 0, 30 });
        ctx.scene.add(ln);
        rd.render(fb, ctx.scene, null, false, 0, 0);
        int ox = ctx.view.originX(), oy = ctx.view.originY();
        boolean pix = true; int plotted = 0;
        for (int[] q : pixels(0, 0, 50, 30)) {
            int sx = ox + q[0], sy = oy - q[1] - 1;
            int c = fb.get(sx, sy);
            if (!(c == ctx.theme.shape() || c == ctx.theme.vertex())) pix = false;
            plotted++;
        }
        check("zoom 1: cada pixel da reta está no pixel real esperado (" + plotted + ")", pix);
        check("fundo preto fora da reta", fb.get(10, 10) == ctx.theme.background());
        check("eixo vermelho na linha central", fb.get(ctx.view.centerX(0), 5) == ctx.theme.axes());
        ctx.view.zoom = 4;
        int cx = ctx.view.cellX(ctx.view.screenX(7) + 3), cy = ctx.view.cellY(ctx.view.screenY(-3) + 2);
        check("zoom 4: célula <-> pixel (ida e volta)", cx == 7 && cy == -3);
        ctx.view.zoomAt(123, 77, 8);
        int wx = ctx.view.cellX(123), wy = ctx.view.cellY(77);
        ctx.view.zoomAt(123, 77, 16);
        check("zoom no cursor mantém a célula sob o mouse", Math.abs(ctx.view.cellX(123) - wx) <= 1 && Math.abs(ctx.view.cellY(77) - wy) <= 1);

        System.out.println(ok + " ok, " + fail + " falhas");
        System.exit(fail == 0 ? 0 : 1);
    }
}
