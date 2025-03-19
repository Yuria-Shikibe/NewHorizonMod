package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawRegionCenterSymmetry extends DrawBlock {
    public TextureRegion[] region;
    public String suffix = "";
    public float layer = -1;

    public DrawRegionCenterSymmetry(String suffix) {
        this.suffix = suffix;
    }

    public DrawRegionCenterSymmetry() {}

    @Override
    public void draw(Building build) {
        float z = Draw.z();
        if (layer > 0) Draw.z(layer);
        Draw.rect(region[build.rotation % 2], build.x, build.y);
        Draw.z(z);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region[plan.rotation % 2], plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return super.icons(block);
    }

    @Override
    public void load(Block block) {
        region = new TextureRegion[2];
        for (int i = 0; i < 2; i++) {
            region[i] = Core.atlas.find(block.name + suffix + "-" + i);
        }
    }
}
