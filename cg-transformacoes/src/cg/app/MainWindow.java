package cg.app;

import cg.tools.LineTool;
import cg.tools.PanTool;
import cg.tools.PolygonTool;
import cg.tools.SelectTool;
import cg.ui.CanvasPanel;
import cg.ui.Sidebar;
import cg.ui.StatusBar;
import cg.ui.TerminalPanel;
import cg.ui.sections.ColorSection;
import cg.ui.sections.DrawSection;
import cg.ui.sections.ToolSection;
import cg.ui.sections.TransformSection;
import cg.ui.sections.ViewSection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta a janela: painel lateral (sanfona de ferramentas + terminal) | display em pixels reais; status embaixo.
 * É aqui que se REGISTRAM ferramentas de mouse e seções do painel.
 */
public final class MainWindow extends JFrame {
    private final AppContext ctx = new AppContext();

    /** Contexto da aplicação (usado pelos testes automatizados de interface). */
    public AppContext context() { return ctx; }

    public MainWindow() {
        super("Computação Gráfica — Transformações 2D e Ponto Médio");

        // 1) ferramentas de mouse (cada uma ganha um botão no painel "Desenho")
        ctx.tools.register(new SelectTool(ctx));
        ctx.tools.register(new LineTool(ctx));
        ctx.tools.register(new PolygonTool(ctx));
        ctx.tools.register(new PanTool(ctx));
        ctx.tools.setActive(ctx.tools.all().get(1));        // começa na Reta

        // 2) seções do painel lateral (ferramenta nova = nova classe ToolSection aqui)
        List<ToolSection> sections = new ArrayList<>();
        sections.add(new DrawSection(ctx));
        sections.add(new TransformSection(ctx));
        sections.add(new ViewSection(ctx));
        sections.add(new ColorSection(ctx));

        CanvasPanel canvas = new CanvasPanel(ctx);
        Sidebar sidebar = new Sidebar(sections);
        TerminalPanel terminal = new TerminalPanel(ctx);

        JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sidebar, terminal);
        left.setResizeWeight(0.55);
        left.setOneTouchExpandable(true);
        left.setBorder(null);
        left.setPreferredSize(new Dimension(380, 700));
        left.setMinimumSize(new Dimension(300, 200));

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0x303030));
        scroll.getVerticalScrollBar().setUnitIncrement(32);

        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, scroll);
        main.setResizeWeight(0);
        main.setBorder(null);
        main.setContinuousLayout(true);
        canvas.setSidebarWidthSupplier(() -> left.getWidth());

        JPanel root = new JPanel(new BorderLayout());
        root.add(main, BorderLayout.CENTER);
        root.add(new StatusBar(ctx), BorderLayout.SOUTH);
        setContentPane(root);

        // polígono inicial: pentágono regular centrado na origem
        ctx.generatePolygon(true, 5, 100);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 520));
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
    }
}
