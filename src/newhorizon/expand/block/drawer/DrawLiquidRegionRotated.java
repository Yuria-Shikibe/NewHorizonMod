package newhorizon.expand.block.drawer;

import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.Liquid;
import mindustry.world.draw.DrawLiquidRegion;

public class DrawLiquidRegionRotated extends DrawLiquidRegion {
    public float x, y;

    public DrawLiquidRegionRotated(Liquid drawLiquid) {
        super(drawLiquid);
    }

    public DrawLiquidRegionRotated() {
    }

    @Override
    public void draw(Building build) {
        Liquid drawn = drawLiquid != null ? drawLiquid : build.liquids.current();
        Tmp.v1.set(x, y).rotate(build.rotdeg()).add(build);

        Drawf.liquid(liquid, Tmp.v1.x, Tmp.v1.y,
                build.liquids.get(drawn) / build.block.liquidCapacity * alpha,
                drawn.color, build.rotdeg()
        );
    }
}
