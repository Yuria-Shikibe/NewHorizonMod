package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.util.Time;
import arc.util.Tmp;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;

public class CameraControlAction extends Action {
    public float worldX, worldY;

    @Override
    public String actionName() {
        return "camera_control";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        worldX = ParseUtil.getNextFloat(tokens) * tilesize;
        worldY = ParseUtil.getNextFloat(tokens) * tilesize;
    }

    @Override
    public void act() {
        if (headless) return;

        control.input.logicCutscene = true;

        Tmp.v1.set(Core.camera.position).lerpDelta(worldX, worldY, progress());
        control.input.logicCamSpeed = 1000f;
        control.input.logicCamPan = Tmp.v1;
    }
}
