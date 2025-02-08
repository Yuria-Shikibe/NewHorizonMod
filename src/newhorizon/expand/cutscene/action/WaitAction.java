package newhorizon.expand.cutscene.action;

import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import newhorizon.expand.cutscene.components.Action;

public class WaitAction extends Action {
    //wait a few seconds.
    public WaitAction(float second) {
        super(second * Time.toSeconds);
    }

    public WaitAction(String[] args) {
        super(Float.parseFloat(args[0]));
    }

    @Override
    public String phaseToString() {
        return "wait_action" + " " + Strings.autoFixed(maxTimer / Time.toSeconds, 1);
    }
}
