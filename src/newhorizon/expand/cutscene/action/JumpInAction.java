package newhorizon.expand.cutscene.action;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.UnitType;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;
import newhorizon.expand.entities.Spawner;

public class JumpInAction extends Action {
    public UnitType unitType;
    public Team team;
    public float worldX = 0, worldY = 0, angle = 0, delay = 5, inaccuracyRadius = 0;
    public JumpInAction(float duration) {
        super(0);
    }

    public JumpInAction(String[] tokens) {
        super(0);
        unitType = ActionControl.parseUnitType(tokens[0]);
        team = ActionControl.parseTeam(tokens[1]);
        worldX = Float.parseFloat(tokens[2]);
        worldY = Float.parseFloat(tokens[3]);
        angle = Float.parseFloat(tokens[4]);
        delay = Float.parseFloat(tokens[5]) * 60;
        inaccuracyRadius = Float.parseFloat(tokens[6]);
    }

    public JumpInAction(String[] tokens, Building source) {
        super(0);
        unitType = ActionControl.parseUnitType(tokens[0]);
        team = ActionControl.parseTeam(tokens[1]);
        worldX = ActionControl.parseFloat(tokens[2], source);
        worldY = ActionControl.parseFloat(tokens[3], source);
        angle = ActionControl.parseFloat(tokens[4], source);
        delay = ActionControl.parseFloat(tokens[5], source) * 60;
        inaccuracyRadius = ActionControl.parseFloat(tokens[6], source);
    }

    @Override
    public void end() {
        Spawner spawner = new Spawner();
        Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracyRadius));
        spawner.init(unitType, team, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), angle, delay);
        spawner.add();
    }
}
