package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.Block;
import mindustry.world.draw.DrawRegion;

public class DrawScanRegion extends DrawRegion {
    public float sinMag = 4f, sinScl = 6f, sinOffset = 50f, sideOffset = 0f, lenOffset = 0f, angleOffset = 0f;

    @Override
    public void draw(Building build){
        float z = Draw.z();
        float offset = Mathf.absin(build.totalProgress() + sinOffset + sideOffset, sinScl, sinMag);
        Tmp.v1.trns(angleOffset, offset + lenOffset).rotate(build.rotdeg());
        if(layer > 0) Draw.z(layer);
        Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, build.totalProgress() * rotateSpeed + rotation + (buildingRotate ? build.rotdeg() : 0));
        Draw.z(z);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        if(!drawPlan) return;
        Draw.rect(region, plan.drawx()+ x, plan.drawy() + y, (buildingRotate ? plan.rotation * 90f : 0 + rotation));
    }
}
