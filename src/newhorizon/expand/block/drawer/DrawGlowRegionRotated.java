package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import newhorizon.util.func.MathUtil;

public class DrawGlowRegionRotated extends DrawRegionCenterSymmetry {

    @Override
    public void draw(Building build) {
        if (build.warmup() > 0) {
            Tmp.v1.set(x, y).rotate(build.rotdeg() % 180).add(build);

            Draw.color(Pal.techBlue);
            Draw.alpha(MathUtil.timeValue(0.75f, 0.9f, 0.8f) * build.warmup());
            Draw.rect(region[build.rotation % 2], Tmp.v1.x, Tmp.v1.y);
            Draw.reset();
        }
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{};
    }
}
