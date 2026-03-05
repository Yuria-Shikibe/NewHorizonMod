package newhorizon.expand.cutscene.action;

import arc.util.Strings;
import arc.util.Time;
import newhorizon.expand.cutscene.components.Action;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class WaitAction extends Action {
    //wait a few seconds.
    public WaitAction(float second) {
        super(second * Time.toSeconds);
    }

    public WaitAction(String[] args) {
        super(Float.parseFloat(args[0]) * Time.toSeconds);
    }

    @Override
    public String phaseToString() {
        return "wait" + " " + Strings.autoFixed(duration / Time.toSeconds, 1);
    }
}
