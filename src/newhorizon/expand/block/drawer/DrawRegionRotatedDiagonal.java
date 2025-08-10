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

public class DrawRegionRotatedDiagonal extends DrawBlock {
    public TextureRegion[] region;
    public String suffix = "";
    public float layer = -1;
    public float x = 0, y = 0;


    public DrawRegionRotatedDiagonal(String suffix) {
        this.suffix = suffix;
    }

    public DrawRegionRotatedDiagonal() {}

    @Override
    public void draw(Building build) {
        float z = Draw.z();
        if (layer > 0) Draw.z(layer);
        Tmp.v1.set(x, y).rotate(build.rotdeg() % 180).add(build);
        Draw.rect(region[build.rotation % 2], Tmp.v1.x, Tmp.v1.y);
        Draw.z(z);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region[plan.rotation % 2], plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{region[0]};
    }

    @Override
    public void load(Block block) {
        region = new TextureRegion[2];
        for (int i = 0; i < 2; i++) {
            region[i] = Core.atlas.find(block.name + suffix + "-" + i);
        }
    }
}
