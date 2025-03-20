package newhorizon.expand.block.drawer;

import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.Liquid;
import mindustry.world.draw.DrawLiquidRegion;

public class DrawLiquidRegionRotated extends DrawLiquidRegion {

    public DrawLiquidRegionRotated(Liquid drawLiquid) {
        super(drawLiquid);
    }

    public DrawLiquidRegionRotated() {
    }

    @Override
    public void draw(Building build) {
        Liquid drawn = drawLiquid != null ? drawLiquid : build.liquids.current();
        Drawf.liquid(liquid, build.x, build.y,
                build.liquids.get(drawn) / build.block.liquidCapacity * alpha,
                drawn.color, build.rotdeg()
        );
    }
}
