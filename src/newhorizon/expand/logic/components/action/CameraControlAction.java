package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.util.Time;
import arc.util.Tmp;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class CameraControlAction extends Action {
    public float worldX, worldY;

    @Override
    public String actionName() {
        return "camera_control";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        worldX = ParseUtil.getNextFloat(tokens);
        worldY = ParseUtil.getNextFloat(tokens);
    }

    @Override
    public void act() {
        if (headless) return;

        Tmp.v1.set(Core.camera.position).lerpDelta(worldX, worldY, progress());
        control.input.logicCamSpeed = 10f;
        control.input.logicCamPan = Tmp.v1;
    }
}
