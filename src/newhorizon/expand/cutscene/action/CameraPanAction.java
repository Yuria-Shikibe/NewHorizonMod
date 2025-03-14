package newhorizon.expand.cutscene.action;

import arc.Core;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;

import static mindustry.Vars.headless;

public class CameraPanAction extends Action {
    public Vec2 target;
    public Vec2 begin;
    public CameraPanAction(float duration, float x, float y) {
        super(duration * Time.toSeconds);
        target = new Vec2(x, y);
    }

    public CameraPanAction(String[] args) {
        super(Float.parseFloat(args[0]) * Time.toSeconds);
        target = new Vec2(Float.parseFloat(args[1]), Float.parseFloat(args[2]));
    }

    public CameraPanAction(String[] tokens, Building source) {
        super(ActionControl.parseFloat(tokens[0], source) * Time.toSeconds);
        target = new Vec2(ActionControl.parseFloat(tokens[1], source), ActionControl.parseFloat(tokens[2], source));
    }

    @Override
    public void begin() {
        if (headless) return;
        begin = Core.camera.position;
    }

    @Override
    public void act() {
        if (headless) return;
        Tmp.v1.set(target.x - begin.x, target.y - begin.y);
        Core.camera.position.set(begin).add(Tmp.v1, progress());
    }
}
