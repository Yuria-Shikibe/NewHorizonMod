package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.draw.DrawRegion;
import newhorizon.NewHorizon;

public class DrawBaseRegion extends DrawRegion {
    public DrawBaseRegion(String suffix) {
        super(suffix);
    }

    @Override
    public void load(Block block){
        region = Core.atlas.find(NewHorizon.name("bottom" + suffix));
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        if(!drawPlan) return;
        Tmp.v1.set(x, y).rotate(plan.rotation * 90).add(plan.drawx(), plan.drawy());
        Draw.rect(region, Tmp.v1.x, Tmp.v1.y, plan.rotation * 90);
    }

    @Override
    public void draw(Building build){
        float z = Draw.z();
        if(layer > 0) Draw.z(layer);
        if(color != null) Draw.color(color);
        Tmp.v1.set(x, y).rotate(build.rotdeg()).add(build.x, build.y);
        Draw.rect(region, Tmp.v1.x, Tmp.v1.y, build.rotdeg());
        if(color != null) Draw.color();
        Draw.z(z);
    }
}
