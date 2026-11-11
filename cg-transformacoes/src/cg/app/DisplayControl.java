package cg.app;

/** O que o painel lateral pode pedir ao display (implementado pelo CanvasPanel/MainWindow). */
public interface DisplayControl {
    /** Resolução automática: o framebuffer ocupa toda a área disponível (1 pixel do framebuffer = 1 pixel real). */
    void setAuto();
    /** Resolução fixa em pixels reais (w x h); se não couber na janela, aparece rolagem. */
    void setFixed(int w, int h);
    boolean isAuto();
    /** Largura atual do painel lateral em pixels lógicos (para calcular se uma resolução "cabe"). */
    int sidebarWidth();
}
