package cg.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lista de ferramentas de mouse e qual está ativa. */
public final class ToolRegistry {
    private final List<Tool> tools = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private Tool active;

    public void register(Tool t) { tools.add(t); if (active == null) active = t; }
    public List<Tool> all() { return Collections.unmodifiableList(tools); }
    public Tool active() { return active; }

    public void setActive(Tool t) {
        if (t == active) return;
        if (active != null) active.cancel();
        active = t;
        for (Runnable r : listeners) r.run();
    }

    public void addListener(Runnable r) { listeners.add(r); }
}
