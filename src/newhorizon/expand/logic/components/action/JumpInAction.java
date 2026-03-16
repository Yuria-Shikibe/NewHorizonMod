package newhorizon.expand.logic.components.action;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.type.UnitType;
import newhorizon.expand.entities.Spawner;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

public class JumpInAction extends Action {
    public UnitType unitType;
    public Team team;
    public float worldX = 0, worldY = 0, angle = 0, delay = 5, inaccuracyRadius = 0;

    @Override
    public String actionName() {
        return "jump_in";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        unitType = ParseUtil.getNextUnitType(tokens);
        team = ParseUtil.getNextTeam(tokens);
        worldX = ParseUtil.getNextFloat(tokens);
        worldY = ParseUtil.getNextFloat(tokens);
        angle = ParseUtil.getNextFloat(tokens);
        delay = ParseUtil.getNextFloat(tokens) * 60;
        inaccuracyRadius = ParseUtil.getNextFloat(tokens);
    }

    @Override
    public void end() {
        Spawner spawner = new Spawner();
        Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracyRadius));
        spawner.init(unitType, team, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), angle, delay);
        spawner.add();
    }
}
