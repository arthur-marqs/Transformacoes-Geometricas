package cg.tools;

/** Evento de mouse já traduzido: célula do mundo sob o cursor + pixel real da tela. */
public final class ToolEvent {
    public final int cellX, cellY;       // coordenadas do mundo (inteiras)
    public final int screenX, screenY;   // pixel real do dispositivo
    public final int button;             // 1 = esquerdo, 2 = meio, 3 = direito
    public final int clickCount;

    public ToolEvent(int cellX, int cellY, int screenX, int screenY, int button, int clickCount) {
        this.cellX = cellX; this.cellY = cellY; this.screenX = screenX; this.screenY = screenY;
        this.button = button; this.clickCount = clickCount;
    }
}
