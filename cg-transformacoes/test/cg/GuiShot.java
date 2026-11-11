package cg;

import cg.app.AppContext;
import cg.app.MainWindow;
import cg.terminal.CommandInterpreter;
import cg.render.Theme;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/** Teste de interface sob Xvfb: usa o mouse de verdade (Robot) e salva capturas.  java cg.GuiShot <pasta> */
public final class GuiShot {
    static Robot robot; static String dir; static MainWindow win; static AppContext ctx;

    static void shot(String name) throws Exception {
        robot.waitForIdle(); Thread.sleep(350);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage img = robot.createScreenCapture(new Rectangle(d));
        ImageIO.write(img, "png", new File(dir, name + ".png"));
        System.out.println("captura " + name + " " + img.getWidth() + "x" + img.getHeight());
    }
    static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }

    static AbstractButton find(Container c, String text) {
        for (Component k : c.getComponents()) {
            if (k instanceof AbstractButton && ((AbstractButton) k).getText() != null && ((AbstractButton) k).getText().contains(text)) return (AbstractButton) k;
            if (k instanceof Container) { AbstractButton b = find((Container) k, text); if (b != null) return b; }
        }
        return null;
    }
    static void click(String text) throws Exception {
        AbstractButton b = find(win, text);
        if (b == null) throw new RuntimeException("botão não achado: " + text);
        edt(b::doClick); robot.waitForIdle(); Thread.sleep(200);
    }
    static void cmd(String s) throws Exception { edt(() -> new CommandInterpreter(ctx).execute(s)); Thread.sleep(150); }

    public static void main(String[] a) throws Exception {
        dir = a[0]; new File(dir).mkdirs();
        java.util.Locale.setDefault(java.util.Locale.US);
        robot = new Robot();
        SwingUtilities.invokeAndWait(() -> { win = new MainWindow(); win.setVisible(true); ctx = win.context(); });
        Thread.sleep(1500);
        shot("1_inicial");

        // reta com o mouse (arrastar): a pré-visualização acompanha e a posição aparece na barra de status
        Point o = new Point(ctx.view.originX(), ctx.view.originY());
        Point canvas = ((Component) win.getContentPane().getComponent(0)).getLocationOnScreen();
        Component cv = find2(win);
        Point cs = cv.getLocationOnScreen();
        double sc = ctx.deviceScale;
        int x0 = cs.x + (int) ((o.x - 250) / sc), y0 = cs.y + (int) ((o.y + 120) / sc);
        robot.mouseMove(x0, y0); robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        for (int i = 1; i <= 20; i++) { robot.mouseMove(x0 + i * 6, y0 - i * 4); Thread.sleep(20); }
        shot("2_arrastando");
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(200);

        // polígono com cliques
        edt(() -> ctx.tools.setActive(ctx.tools.all().get(2)));
        int[][] pts = { {120, -90}, {220, -60}, {260, 10}, {200, 70}, {110, 40} };
        for (int[] p : pts) {
            robot.mouseMove(cs.x + (int) ((o.x + p[0] * 2) / sc), cs.y + (int) ((o.y - p[1] * 2) / sc));
            Thread.sleep(60); robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
        robot.mouseMove(cs.x + (int) ((o.x + 100) / sc), cs.y + (int) ((o.y + 80) / sc));
        shot("3_poligono_em_andamento");
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);   // botão direito fecha
        Thread.sleep(200);
        edt(() -> ctx.tools.setActive(ctx.tools.all().get(0)));

        // transformações no pentágono original (selecionar: clicar perto dele não é necessário; usa comando)
        cmd("regular 5 100");
        cmd("rotacionar 30 40 20");
        cmd("escalar 1.5 1 0 0");
        click("Transformações");
        shot("4_transformado_painel");

        cmd("zoom 8");
        edt(() -> ctx.view.centerOrigin());
        shot("5_zoom8");

        click("Vista, zoom");
        click("Usar");     // resolução personalizada 1280x720
        shot("6_resolucao_1280x720");

        click("Cores");
        edt(() -> ctx.theme.apply(Theme.PRESETS.get(1)));
        shot("7_tema_projetor");
        System.exit(0);
    }

    static Component find2(Container c) {
        for (Component k : c.getComponents()) {
            if (k.getClass().getSimpleName().equals("CanvasPanel")) return k;
            if (k instanceof Container) { Component r = find2((Container) k); if (r != null) return r; }
        }
        return null;
    }
}
