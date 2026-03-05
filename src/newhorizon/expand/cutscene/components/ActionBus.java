package newhorizon.expand.cutscene.components;

import arc.Core;
import newhorizon.util.struct.TimeQueue;

import static newhorizon.NHVars.cutsceneUI;

/**
 * A queue for executing cutscene actions sequentially.
 * Extends TimeQueue with skip functionality for cutscenes.
 */
public class ActionBus extends TimeQueue<Action> {
    /** Whether the bus is currently skipping */
    public boolean skipping = false;

    /**
     * Skip all remaining actions in the bus.
     * Triggers skip() on all actions and clears the queue.
     */
    public void skip() {
        skipping = true;

        // Skip current action
        if (current != null) {
            current.skip();
        }

        // Skip all pending actions
        while (!queue.isEmpty()) {
            Action action = queue.removeLast();
            action.skip();
        }

        skipping = false;

        // Clear the queue
        current = null;
        queue.clear();

        // Reset UI control override
        Core.app.post(() -> cutsceneUI.controlOverride = false);
    }

    /**
     * Skip only the current action and continue with the next.
     */
    public void skipCurrent() {
        if (current != null) {
            current.skip();
            super.skipCurrent();
        }
    }

    /**
     * Pause the action bus execution.
     */
    @Override
    public void pause() {
        super.pause();
    }

    /**
     * Resume the action bus execution.
     */
    @Override
    public void resume() {
        super.resume();
    }
}
