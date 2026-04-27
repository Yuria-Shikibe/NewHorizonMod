package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import mindustry.gen.Building;
import mindustry.world.blocks.production.Drill;
import mindustry.world.draw.DrawBlock;

public class DrawDrillOreTop extends DrawBlock {
    @Override
    public void draw(Building build) {
        if (build instanceof Drill.DrillBuild drill) {
            if(drill.dominantItem != null){
                Draw.color(drill.dominantItem.color);
                Draw.rect(((Drill)drill.block).itemRegion, drill.x, drill.y);
                Draw.color();
            }
        }
    }
}
