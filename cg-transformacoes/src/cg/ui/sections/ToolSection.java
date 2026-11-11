package cg.ui.sections;

import javax.swing.JComponent;

/**
 * Uma seção expansível do painel lateral. Para adicionar uma ferramenta futura ao painel:
 * crie uma classe que implemente esta interface e adicione-a à lista em MainWindow.
 */
public interface ToolSection {
    String title();
    JComponent build();
    default boolean initiallyOpen() { return false; }
}
