package cg.terminal;

import cg.app.AppContext;
import cg.geometry.Shape2D;
import cg.transform.TransformOp;
import cg.transform.Transforms;
import cg.transform.Transforms.Reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interpreta os comandos digitados no terminal. Cada comando chama os mesmos serviços usados pelos botões
 * (Transforms, TransformService, AppContext), então painel e terminal nunca divergem.
 * Para criar um comando novo, adicione um "case" em {@link #execute}.
 */
public final class CommandInterpreter {
    private final AppContext ctx;
    private final Console out;

    public CommandInterpreter(AppContext ctx) { this.ctx = ctx; this.out = ctx.console; }

    public void execute(String line) {
        line = line.trim();
        if (line.isEmpty()) return;
        out.println("> " + line);
        String[] t = line.replace(',', '.').split("\\s+");
        String cmd = t[0].toLowerCase(Locale.ROOT);
        try {
            TransformOp op = parseOp(t, 0);
            if (op != null) { ctx.transforms.apply(op); return; }
            switch (cmd) {
                case "ajuda": case "help": case "?": help(); break;
                case "fila": case "queue": queue(t); break;
                case "linha": case "line": line(t); break;
                case "regular": ctx.generatePolygon(true, (int) num(t, 1, 5), num(t, 2, 100)); break;
                case "irregular": ctx.generatePolygon(false, (int) num(t, 1, 7), num(t, 2, 100)); break;
                case "centralizar": case "center": ctx.transforms.centerOnOrigin(); break;
                case "desfazer": case "undo": ctx.transforms.undo(); break;
                case "resetar": case "reset": ctx.transforms.reset(); break;
                case "info": info(); break;
                case "zoom": ctx.view.zoomCenter((int) num(t, 1, 1)); ctx.redraw(); ctx.statusChanged(); break;
                case "detalhe": ctx.transforms.verbose = !(t.length > 1 && t[1].equalsIgnoreCase("off")); out.println("Detalhe das contas: " + (ctx.transforms.verbose ? "completo" : "resumido")); break;
                case "apagar": ctx.clearAll(); break;
                case "limpar": case "clear": out.clear(); break;
                default: out.println("! Comando desconhecido: '" + t[0] + "'. Digite 'ajuda'.");
            }
        } catch (IllegalArgumentException ex) {
            out.println("! " + ex.getMessage());
        }
    }

    /** Converte "rotacionar 30 2 1" etc. numa transformação; devolve null se t[i] não for uma transformação. */
    private TransformOp parseOp(String[] t, int i) {
        String c = t[i].toLowerCase(Locale.ROOT);
        double[] a;
        switch (c) {
            case "transladar": case "translate": case "mover":
                a = nums(t, i + 1, 2, 2, "transladar tx ty");
                return Transforms.translate(a[0], a[1]);
            case "escalar": case "scale":
                a = nums(t, i + 1, 1, 4, "escalar s | sx sy | s px py | sx sy px py");
                if (a.length == 1) return Transforms.scale(a[0], a[0], 0, 0);
                if (a.length == 2) return Transforms.scale(a[0], a[1], 0, 0);
                if (a.length == 3) return Transforms.scale(a[0], a[0], a[1], a[2]);
                return Transforms.scale(a[0], a[1], a[2], a[3]);
            case "rotacionar": case "rotate": case "rotar":
                a = nums(t, i + 1, 1, 3, "rotacionar graus [px py]");
                return Transforms.rotate(a[0], a.length > 2 ? a[1] : 0, a.length > 2 ? a[2] : 0);
            case "cisalhar": case "shear":
                if (t.length <= i + 2) throw new IllegalArgumentException("uso: cisalhar x|y k [px py]");
                boolean alongX = t[i + 1].equalsIgnoreCase("x");
                if (!alongX && !t[i + 1].equalsIgnoreCase("y")) throw new IllegalArgumentException("eixo deve ser x ou y");
                a = nums(t, i + 2, 1, 3, "cisalhar x|y k [px py]");
                return Transforms.shear(alongX, a[0], a.length > 2 ? a[1] : 0, a.length > 2 ? a[2] : 0);
            case "refletir": case "reflect":
                if (t.length <= i + 1) throw new IllegalArgumentException("uso: refletir x | y | origem | y=x");
                return Transforms.reflect(parseReflection(t[i + 1]));
            case "janela": case "viewport":
                a = nums(t, i + 1, 8, 8, "janela wxmin wymin wxmax wymax vxmin vymin vxmax vymax");
                return Transforms.windowViewport(new double[] { a[0], a[1], a[2], a[3] }, new double[] { a[4], a[5], a[6], a[7] });
            default:
                return null;
        }
    }

    private static Reflection parseReflection(String s) {
        switch (s.toLowerCase(Locale.ROOT)) {
            case "x": case "eixox": return Reflection.EIXO_X;
            case "y": case "eixoy": return Reflection.EIXO_Y;
            case "origem": case "o": case "origin": return Reflection.ORIGEM;
            case "y=x": case "diagonal": case "d": return Reflection.RETA_Y_X;
            default: throw new IllegalArgumentException("reflexão desconhecida: " + s + " (use x, y, origem ou y=x)");
        }
    }

    private void queue(String[] t) {
        String sub = t.length > 1 ? t[1].toLowerCase(Locale.ROOT) : "ver";
        switch (sub) {
            case "add": case "adicionar": {
                TransformOp op = t.length > 2 ? parseOp(t, 2) : null;
                if (op == null) throw new IllegalArgumentException("uso: fila add <transformação...>");
                ctx.transforms.enqueue(op);
                break;
            }
            case "aplicar": case "apply": ctx.transforms.applyQueue(); break;
            case "limpar": case "clear": ctx.transforms.clearQueue(); out.println("Fila limpa."); break;
            default:
                List<TransformOp> q = ctx.transforms.queue();
                if (q.isEmpty()) out.println("Fila vazia.");
                for (int i = 0; i < q.size(); i++) out.println("  " + (i + 1) + ". " + q.get(i).title);
        }
    }

    private void line(String[] t) {
        double[] a = nums(t, 1, 4, 4, "linha x1 y1 x2 y2");
        int x0 = (int) Math.round(a[0]), y0 = (int) Math.round(a[1]), x1 = (int) Math.round(a[2]), y1 = (int) Math.round(a[3]);
        Shape2D s = new Shape2D("Reta " + ctx.scene.nextIndex("Reta"), false, new double[] { x0, x1 }, new double[] { y0, y1 });
        ctx.scene.add(s);
        Report.midpoint(out, x0, y0, x1, y1);
    }

    private void info() {
        Shape2D s = ctx.scene.active();
        out.println("Formas no plano: " + ctx.scene.shapes().size());
        if (s == null) { out.println("Nenhuma forma ativa."); return; }
        Report.shape(out, "Forma ativa: " + s.name + (s.isModified() ? " (transformada)" : " (original)"), s);
    }

    private void help() {
        String[] h = {
            "Comandos (separe os números por espaço; ponto ou vírgula decimal):",
            "  transladar tx ty",
            "  escalar s | sx sy | s px py | sx sy px py     (px py = ponto fixo)",
            "  rotacionar graus [px py]                        (anti-horário)",
            "  refletir x | y | origem | y=x",
            "  cisalhar x|y k [px py]",
            "  janela wxmin wymin wxmax wymax vxmin vymin vxmax vymax",
            "  fila add <transformação> | fila aplicar | fila limpar | fila   (composição)",
            "  linha x1 y1 x2 y2        desenha e mostra a tabela do ponto médio",
            "  regular n [raio]  |  irregular n [raio]   polígono na origem",
            "  centralizar | desfazer | resetar | info | apagar",
            "  zoom n | detalhe on|off | limpar (terminal) | ajuda",
            "Mouse: roda = zoom no cursor; botão do meio ou ferramenta 'Mover vista' = arrastar; setas = mover."
        };
        for (String s : h) out.println(s);
    }

    // ---------- números ----------

    private static double num(String[] t, int i, double def) {
        if (i >= t.length) return def;
        return parse(t[i]);
    }

    private static double parse(String s) {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("número inválido: '" + s + "'"); }
    }

    /** Lê de t[from..] entre min e max números (ignora palavras de ligação "em", "ponto", "@"). */
    private static double[] nums(String[] t, int from, int min, int max, String uso) {
        List<Double> v = new ArrayList<>();
        for (int i = from; i < t.length; i++) {
            String w = t[i].toLowerCase(Locale.ROOT);
            if (w.equals("em") || w.equals("at") || w.equals("@") || w.equals("ponto")) continue;
            v.add(parse(t[i]));
        }
        if (v.size() < min || v.size() > max) throw new IllegalArgumentException("uso: " + uso);
        double[] r = new double[v.size()];
        for (int i = 0; i < r.length; i++) r[i] = v.get(i);
        return r;
    }
}
