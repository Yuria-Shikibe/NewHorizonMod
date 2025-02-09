package newhorizon.expand.cutscene.components;

import arc.Core;
import newhorizon.util.struct.TimeQueue;

import static newhorizon.NHVars.cutsceneUI;

public class ActionBus extends TimeQueue<Action> {
    public boolean skipping = false;

    public void skip() {
        skipping = true;

        //when skipped, trigger all remaining action.
        if (current != null) {
            current.skip();
        }

        while (!queue.isEmpty()) {
            Action action = queue.removeLast();
            action.skip();
        }

        skipping = false;

        current = null;
        queue.clear();

        Core.app.post(() -> cutsceneUI.controlOverride = false);
    }
}
