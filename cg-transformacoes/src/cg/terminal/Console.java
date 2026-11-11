package cg.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Saída de texto do sistema. Qualquer classe escreve aqui; o TerminalPanel apenas "escuta". */
public final class Console {
    private final List<Consumer<String>> out = new ArrayList<>();
    private final List<Runnable> clearListeners = new ArrayList<>();

    public void println(String s) { for (Consumer<String> c : out) c.accept(s); }
    public void clear() { for (Runnable r : clearListeners) r.run(); }
    public void onLine(Consumer<String> c) { out.add(c); }
    public void onClear(Runnable r) { clearListeners.add(r); }
}
