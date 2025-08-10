package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

import static mindustry.Vars.tilesize;

public class DrawRegionFlip extends DrawBlock {
    public TextureRegion region;
    public String suffix = "";
    public float layer = -1;
    public float x = 0, y = 0;

    public DrawRegionFlip(String suffix) {
        this.suffix = suffix;
    }

    public DrawRegionFlip() {}

    @Override
    public void draw(Building build) {
        float z = Draw.z();
        if (layer > 0) Draw.z(layer);
        Tmp.v1.set(x, y).rotate(build.rotdeg() % 180).add(build);
        drawRegion(Tmp.v1.x, Tmp.v1.y, build.rotation);
        Draw.z(z);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        drawRegion(plan.drawx(), plan.drawy(), plan.rotation);
    }

    public void drawRegion(float x, float y, int rotation) {
        if (rotation % 2 == 0){
            Draw.rect(region, x, y);
        }else {
            Draw.rect(region, x, y, (float) region.width / tilesize * 2f, (float) -region.height / tilesize * 2f, 90);
        }
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{region};
    }

    @Override
    public void load(Block block) {
        region = Core.atlas.find(block.name + suffix);
    }
}
