package newhorizon.expand.logic.components;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;

import static newhorizon.NHVars.cutsceneUI;

/**
 * Controls the execution of cutscene action buses.
 * Manages main bus queue, sub buses, and waiting periods between cutscenes.
 */
public class CutsceneControl {

    public static final String CSS_ACTION_KEY_PREFIX = "[CSS_ACTION]";
    public static final String CSS_ACTION_BUS_KEY_PREFIX = "[CSS_ACTION_BUS]";

    // Whether currently waiting between cutscenes
    public boolean waiting = false;
    // Time spacing between cutscenes in ticks
    public float waitSpacing = 60f;
    // Current wait timer
    public float waitTimer = 0f;

    /** Currently executing main action bus */
    public ActionBus mainBus;
    /** Sub action buses that run in parallel */
    public Seq<ActionBus> subBuses = new Seq<>();
    /** Queue of main action buses waiting to execute */
    public Queue<ActionBus> waitingBuses = new Queue<>();

    public CutsceneControl() {
        Events.on(EventType.WorldLoadEvent.class, event -> clear());
    }

    public void update() {
        updateMainBus();
        updateWaiting();
        startNextMainBus();
        updateSubBuses();
        cutsceneUI.update();
    }

    private void updateMainBus() {
        if (mainBus == null) return;

        mainBus.update();
        if (mainBus.complete()) {
            mainBus = null;
            waiting = true;
            cutsceneUI.reset();
        }
    }

    private void updateWaiting() {
        if (!waiting) return;

        waitTimer += Time.delta;
        if (waitTimer >= waitSpacing) {
            waitTimer = 0f;
            waiting = false;
        }
    }

    private void startNextMainBus() {
        if (mainBus == null && !waiting && !waitingBuses.isEmpty()) {
            mainBus = waitingBuses.removeLast();
        }
    }

    private void updateSubBuses() {
        for (int i = subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = subBuses.get(i);
            bus.update();
            if (bus.complete()) {
                subBuses.remove(i);
            }
        }
    }

    public void skipAll() {
        if (mainBus != null) {
            mainBus.skip();
        }
        if (!waitingBuses.isEmpty()) {
            waitingBuses.each(ActionBus::skip);
        }
        if (!subBuses.isEmpty()) {
            subBuses.each(ActionBus::skip);
        }
        clear();
    }

    public void clear() {
        waiting = false;
        waitTimer = 0f;
        mainBus = null;
        waitingBuses.clear();
        subBuses.clear();
    }

    public static void saveAction(String name, String action) {
        Vars.state.rules.tags.put(CSS_ACTION_KEY_PREFIX + name, action);
    }

    public static String getAction(String name) {
        return Vars.state.rules.tags.get(CSS_ACTION_KEY_PREFIX + name, "");
    }

    public static void saveActionBus(String name, String action) {
        Vars.state.rules.tags.put(CSS_ACTION_BUS_KEY_PREFIX + name, action);
    }

    public static String getActionBus(String name) {
        return Vars.state.rules.tags.get(CSS_ACTION_BUS_KEY_PREFIX + name, "");
    }

    //Add a main action bus to the queue. If no main bus is running, starts immediately; otherwise queues it.
    public void addMainActionBus(ActionBus bus) {
        if (bus == null) return;

        if (mainBus == null) {
            mainBus = bus;
        } else {
            waitingBuses.add(bus);
        }
    }

    //Add a sub action bus that runs in parallel.
    public void addSubActionBus(ActionBus bus) {
        if (bus != null) {
            subBuses.add(bus);
        }
    }
}
