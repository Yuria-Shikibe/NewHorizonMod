package newhorizon.expand.cutscene.components;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.game.EventType;

import static newhorizon.NHVars.cutsceneUI;

/**
 * Controls the execution of cutscene action buses.
 * Manages main bus queue, sub buses, and waiting periods between cutscenes.
 */
public class CutsceneControl {
    /** Whether currently waiting between cutscenes */
    public boolean waiting = false;
    /** Time spacing between cutscenes in ticks */
    public float waitSpacing = 60f;
    /** Current wait timer */
    public float waitTimer = 0f;

    /** Currently executing main action bus */
    public ActionBus mainBus;
    /** Queue of main action buses waiting to execute */
    public Queue<ActionBus> waitingBuses = new Queue<>();
    /** Sub action buses that run in parallel */
    public Seq<ActionBus> subBuses = new Seq<>();

    public CutsceneControl() {
        Events.on(EventType.WorldLoadEvent.class, event -> clear());
    }

    /**
     * Update all cutscene buses. Should be called every frame.
     */
    public void update() {
        updateMainBus();
        updateWaiting();
        startNextMainBus();
        updateSubBuses();
        cutsceneUI.update();
    }

    /**
     * Update the main action bus.
     */
    private void updateMainBus() {
        if (mainBus == null) return;

        mainBus.update();
        if (mainBus.complete()) {
            mainBus = null;
            waiting = true;
            cutsceneUI.reset();
        }
    }

    /**
     * Update waiting timer between cutscenes.
     */
    private void updateWaiting() {
        if (!waiting) return;

        waitTimer += Time.delta;
        if (waitTimer >= waitSpacing) {
            waitTimer = 0f;
            waiting = false;
        }
    }

    /**
     * Start the next main bus from waiting queue.
     */
    private void startNextMainBus() {
        if (mainBus == null && !waiting && !waitingBuses.isEmpty()) {
            mainBus = waitingBuses.removeLast();
        }
    }

    /**
     * Update all sub action buses and remove completed ones.
     */
    private void updateSubBuses() {
        for (int i = subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = subBuses.get(i);
            bus.update();
            if (bus.complete()) {
                subBuses.remove(i);
            }
        }
    }

    /**
     * Skip all cutscene buses (main, waiting, and sub).
     */
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

    /**
     * Pause all cutscene buses.
     */
    public void pauseAll() {
        if (mainBus != null) {
            mainBus.pause();
        }
        waitingBuses.each(ActionBus::pause);
        subBuses.each(ActionBus::pause);
    }

    /**
     * Resume all cutscene buses.
     */
    public void resumeAll() {
        if (mainBus != null) {
            mainBus.resume();
        }
        waitingBuses.each(ActionBus::resume);
        subBuses.each(ActionBus::resume);
    }

    /**
     * Clear all cutscene buses and reset state.
     */
    public void clear() {
        waiting = false;
        waitTimer = 0f;
        mainBus = null;
        waitingBuses.clear();
        subBuses.clear();
    }

    /**
     * Add a main action bus to the queue.
     * If no main bus is running, starts immediately; otherwise queues it.
     */
    public void addMainActionBus(ActionBus bus) {
        if (bus == null) return;

        if (mainBus == null) {
            mainBus = bus;
        } else {
            waitingBuses.add(bus);
        }
    }

    /**
     * Add a sub action bus that runs in parallel.
     */
    public void addSubActionBus(ActionBus bus) {
        if (bus != null) {
            subBuses.add(bus);
        }
    }

    /**
     * Check if any cutscene is currently playing.
     */
    public boolean isPlaying() {
        return (mainBus != null && !mainBus.complete()) || !subBuses.isEmpty();
    }

    /**
     * Check if the main cutscene is playing.
     */
    public boolean isMainPlaying() {
        return mainBus != null && !mainBus.complete();
    }

    /**
     * Get the total number of pending main buses.
     */
    public int pendingMainBuses() {
        return waitingBuses.size + (mainBus != null ? 1 : 0);
    }

    /**
     * Get the number of active sub buses.
     */
    public int activeSubBuses() {
        return subBuses.size;
    }
}
