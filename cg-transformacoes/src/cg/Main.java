package cg;

import cg.app.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Locale;

/** Ponto de entrada. Locale fixo em US para os campos numéricos e o terminal usarem sempre "." decimal. */
public final class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            new MainWindow().setVisible(true);
        });
    }
}
