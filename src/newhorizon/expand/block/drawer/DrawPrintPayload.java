package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.math.Interp;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.draw.DrawDefault;
import newhorizon.expand.block.production.factory.PayloadCrafter;

public class DrawPrintPayload extends DrawDefault {
    @Override
    public void draw(Building build) {
        if (build instanceof PayloadCrafter.PayloadCrafterBuild pc){
            if (pc.recipe == null) return;
            Draw.draw(Layer.blockOver, () -> Drawf.construct(build.x, build.y, pc.recipe.fullIcon, build.team.color, 0, build.progress(), Interp.pow2Out.apply(build.progress()), build.totalProgress() * 2));
        }
    }
}
