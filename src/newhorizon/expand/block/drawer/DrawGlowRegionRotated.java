package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.draw.DrawRegion;
import newhorizon.util.func.MathUtil;

public class DrawGlowRegionRotated extends DrawRegionRotated {

    @Override
    public void draw(Building build) {
        if (build.warmup() > 0){
            Tmp.v1.set(x, y).rotate(build.rotdeg()).add(build);

            Draw.color(Pal.techBlue);
            Draw.alpha(MathUtil.timeValue(0.75f, 0.9f, 0.8f) * build.warmup());
            Draw.rect(region[build.rotation], Tmp.v1.x, Tmp.v1.y, build.rotdeg());
            Draw.reset();
        }
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {}

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[]{};
    }
}
