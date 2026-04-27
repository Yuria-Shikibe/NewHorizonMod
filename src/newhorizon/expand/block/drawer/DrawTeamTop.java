package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawRegion;

import static mindustry.Vars.player;

public class DrawTeamTop extends DrawBlock {
    @Override
    public void draw(Building build) {
        build.drawTeamTop();
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        if(plan.worldContext && player != null && block.teamRegion != null && block.teamRegion.found()){
            if(block.teamRegions[player.team().id] == block.teamRegion) Draw.color(player.team().color);
            Draw.rect(block.teamRegions[player.team().id], plan.drawx(), plan.drawy());
            Draw.color();
        }
    }
}
