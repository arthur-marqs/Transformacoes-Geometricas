package cg.ui;

import cg.app.AppContext;
import cg.terminal.CommandInterpreter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** Terminal: mostra as contas (matrizes, vértices, tabela do ponto médio) e aceita comandos digitados. */
public final class TerminalPanel extends JPanel {
    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private final JLabel prompt = new JLabel(" > ");
    private final List<String> history = new ArrayList<>();
    private int histPos;

    public TerminalPanel(AppContext ctx) {
        super(new BorderLayout());
        CommandInterpreter interpreter = new CommandInterpreter(ctx);
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        area.setEditable(false); area.setFont(mono); area.setLineWrap(false);
        input.setFont(mono); prompt.setFont(mono);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(prompt, BorderLayout.WEST); bottom.add(input, BorderLayout.CENTER);
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setBorder(BorderFactory.createTitledBorder("Terminal (contas das transformações)"));

        ctx.console.onLine(s -> { area.append(s + "\n"); area.setCaretPosition(area.getDocument().getLength()); });
        ctx.console.onClear(() -> area.setText(""));
        ctx.theme.addListener(() -> applyColors(ctx));
        applyColors(ctx);

        input.addActionListener(e -> {
            String s = input.getText();
            if (!s.trim().isEmpty()) { history.add(s); histPos = history.size(); }
            input.setText("");
            interpreter.execute(s);
        });
        input.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP && histPos > 0) input.setText(history.get(--histPos));
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) input.setText(histPos < history.size() - 1 ? history.get(++histPos) : "");
            }
        });
        ctx.console.println("Digite 'ajuda' para ver os comandos.");
    }

    /** O terminal segue as cores do tema (fundo e cor da forma). */
    private void applyColors(AppContext ctx) {
        Color bg = new Color(ctx.theme.background()), fg = new Color(ctx.theme.shape());
        area.setBackground(bg); area.setForeground(fg); area.setCaretColor(fg);
        input.setBackground(bg); input.setForeground(fg); input.setCaretColor(fg);
        prompt.setOpaque(true); prompt.setBackground(bg); prompt.setForeground(fg);
    }

    public void focusInput() { input.requestFocusInWindow(); }
}
