package cg.ui;

import cg.app.AppContext;
import cg.app.DisplayControl;
import cg.render.Framebuffer;
import cg.render.Renderer;
import cg.tools.Tool;
import cg.tools.ToolEvent;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

/**
 * O DISPLAY. Um Framebuffer com 1 elemento por PIXEL REAL do dispositivo é desenhado por cima do painel
 * sem nenhuma escala (compensa o fator de DPI do Windows: 125%, 150%...). Cada pixel gerado pelos algoritmos
 * é um pixel físico da tela quando o zoom é 1.
 *
 * Eventos de mouse (movimento, clique, arrasto, roda) são traduzidos para células do mundo e entregues à
 * ferramenta ativa; as coordenadas ao vivo vão para a barra de status.
 */
public final class CanvasPanel extends JPanel implements Scrollable, DisplayControl {
    private final AppContext ctx;
    private final Framebuffer fb = new Framebuffer();
    private final Renderer renderer;
    private boolean fixed;
    private int fixedW = 1280, fixedH = 720;
    private double sx = 1, sy = 1;               // pixels reais por pixel lógico do Swing
    private int panLastX, panLastY;
    private boolean midPan;
    private java.util.function.IntSupplier sidebarWidth = () -> 0;

    public CanvasPanel(AppContext ctx) {
        this.ctx = ctx;
        this.renderer = new Renderer(ctx.view, ctx.theme);
        ctx.setRepaint(this::repaint);
        ctx.display = this;
        setFocusable(true);
        setBackground(Color.DARK_GRAY);

        MouseAdapter m = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isMiddleMouseButton(e)) { midPan = true; panLastX = dev(e.getX(), sx); panLastY = dev(e.getY(), sy); return; }
                track(e); ctx.tools.active().pressed(event(e));
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (midPan && SwingUtilities.isMiddleMouseButton(e)) { midPan = false; return; }
                track(e); ctx.tools.active().released(event(e));
            }
            @Override public void mouseDragged(MouseEvent e) {
                track(e);
                if (midPan) {
                    int x = dev(e.getX(), sx), y = dev(e.getY(), sy);
                    ctx.view.panX += x - panLastX; ctx.view.panY += y - panLastY; panLastX = x; panLastY = y;
                    repaint(); return;
                }
                ctx.tools.active().dragged(event(e)); repaint();
            }
            @Override public void mouseMoved(MouseEvent e) { track(e); ctx.tools.active().moved(event(e)); repaint(); }
            @Override public void mouseExited(MouseEvent e) { ctx.cursorValid = false; ctx.statusChanged(); repaint(); }
            @Override public void mouseEntered(MouseEvent e) { requestFocusInWindow(); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                int dir = e.getWheelRotation() < 0 ? 1 : -1;
                ctx.view.zoomAt(dev(e.getX(), sx), dev(e.getY(), sy), ctx.view.nextZoom(dir));
                track(e); ctx.statusChanged(); repaint();
            }
        };
        addMouseListener(m); addMouseMotionListener(m); addMouseWheelListener(m);

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int step = 24;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:  ctx.view.panX += step; break;
                    case KeyEvent.VK_RIGHT: ctx.view.panX -= step; break;
                    case KeyEvent.VK_UP:    ctx.view.panY += step; break;
                    case KeyEvent.VK_DOWN:  ctx.view.panY -= step; break;
                    case KeyEvent.VK_PLUS: case KeyEvent.VK_ADD: case KeyEvent.VK_EQUALS: ctx.view.zoomCenter(ctx.view.nextZoom(1)); break;
                    case KeyEvent.VK_MINUS: case KeyEvent.VK_SUBTRACT: ctx.view.zoomCenter(ctx.view.nextZoom(-1)); break;
                    case KeyEvent.VK_0: case KeyEvent.VK_HOME: ctx.view.centerOrigin(); break;
                    case KeyEvent.VK_DELETE: ctx.scene.removeActive(); break;
                    default: ctx.tools.active().keyPressed(e);
                }
                ctx.statusChanged(); repaint();
            }
        });
    }

    public void setSidebarWidthSupplier(java.util.function.IntSupplier s) { sidebarWidth = s; }

    // ---------- DisplayControl ----------
    @Override public void setAuto() { fixed = false; ctx.view.centerOrigin(); revalidate(); repaint(); }
    @Override public void setFixed(int w, int h) { fixed = true; fixedW = w; fixedH = h; ctx.view.centerOrigin(); revalidate(); repaint(); }
    @Override public boolean isAuto() { return !fixed; }
    @Override public int sidebarWidth() { return sidebarWidth.getAsInt(); }

    // ---------- conversão de eventos ----------
    private static int dev(int logical, double scale) { return (int) Math.round(logical * scale); }

    private void track(MouseEvent e) {
        int x = dev(e.getX(), sx), y = dev(e.getY(), sy);
        ctx.cursorSX = x; ctx.cursorSY = y;
        ctx.cursorX = ctx.view.cellX(x); ctx.cursorY = ctx.view.cellY(y);
        ctx.cursorValid = true;
        ctx.statusChanged();
    }

    private ToolEvent event(MouseEvent e) {
        int x = dev(e.getX(), sx), y = dev(e.getY(), sy);
        int b = SwingUtilities.isLeftMouseButton(e) ? 1 : SwingUtilities.isRightMouseButton(e) ? 3 : e.getButton();
        return new ToolEvent(ctx.view.cellX(x), ctx.view.cellY(y), x, y, b, e.getClickCount());
    }

    // ---------- pintura ----------
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            AffineTransform t = g2.getTransform();
            double nsx = t.getScaleX() > 0 ? t.getScaleX() : 1, nsy = t.getScaleY() > 0 ? t.getScaleY() : 1;
            boolean scaleChanged = nsx != sx || nsy != sy;
            sx = nsx; sy = nsy; ctx.deviceScale = sx;

            int w = fixed ? fixedW : (int) Math.round(getWidth() * sx);
            int h = fixed ? fixedH : (int) Math.round(getHeight() * sy);
            boolean sizeChanged = w != ctx.displayW || h != ctx.displayH;
            fb.resize(w, h);

            Tool tool = ctx.tools.active();
            renderer.render(fb, ctx.scene, tool::drawPreview, ctx.cursorValid, ctx.cursorX, ctx.cursorY);

            g2.scale(1.0 / sx, 1.0 / sy);               // 1 pixel do framebuffer = 1 pixel físico
            g2.drawImage(fb.image(), 0, 0, null);

            ctx.displayW = fb.w; ctx.displayH = fb.h;
            if (sizeChanged || scaleChanged) { ctx.statusChanged(); if (fixed) revalidate(); }
        } finally { g2.dispose(); }
        setCursor(Cursor.getPredefinedCursor(ctx.tools.active().cursorType()));
    }

    // ---------- Scrollable: no modo automático o display acompanha a janela; no fixo mantém o tamanho ----------
    @Override public Dimension getPreferredSize() {
        if (!fixed) return new Dimension(640, 480);
        return new Dimension((int) Math.ceil(fixedW / sx), (int) Math.ceil(fixedH / sy));
    }
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 32; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 256; }
    @Override public boolean getScrollableTracksViewportWidth() { return !fixed; }
    @Override public boolean getScrollableTracksViewportHeight() { return !fixed; }
}
