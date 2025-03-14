package newhorizon.expand.cutscene.action;

import mindustry.gen.Building;
import newhorizon.expand.block.cutscene.CutsceneTrigger;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.*;

public class TriggerActivateAction extends Action {
    public int x, y;
    public boolean relative;
    public Building source;
    public TriggerActivateAction(int x, int y, boolean relative, Building source) {
        super(0);
        this.x = x;
        this.y = y;
        this.relative = relative;
        this.source = source;
    }

    public TriggerActivateAction(String[] args, Building source) {
        super(0);
        this.x = Integer.parseInt(args[0]);
        this.y = Integer.parseInt(args[1]);
        this.relative = Boolean.parseBoolean(args[2]);
        this.source = source;
    }

    @Override
    public void end() {
        if (headless) return;
        if (source != null){
            int px = relative? source.tileX() + x: x;
            int py = relative? source.tileY() + y: y;
            Building building = world.build(px, py);
            if (building instanceof CutsceneTrigger ct){
                ct.activate();
            }else {
                ui.announce("Failed to activate building in <" + px + ", " + py + ">");
            }
        }
    }

    @Override
    public void skip() {
        if (headless) return;
        end();
    }
}
