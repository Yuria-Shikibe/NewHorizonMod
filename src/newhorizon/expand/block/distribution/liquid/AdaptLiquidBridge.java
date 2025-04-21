package newhorizon.expand.block.distribution.liquid;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.expand.block.distribution.item.logistics.AdaptItemBridge;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class AdaptLiquidBridge extends AdaptItemBridge {
    public AdaptLiquidBridge(String name) {
        super(name);

        hasItems = false;
        hasLiquids = true;
        outputsLiquid = true;
        canOverdrive = false;
        group = BlockGroup.liquids;
        envEnabled = Env.any;
    }
    
    public class AdaptLiquidBridgeBuild extends AdaptItemBridgeBuild {
        @Override
        public void updateTransport(Building other){
            if(warmup >= 0.25f){
                moved |= moveLiquid(other, liquids.current()) > 0.05f;
            }
        }

        @Override
        public void doDump(){
            dumpLiquid(liquids.current(), 1f);
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.z(Layer.power + 0.1f);
            Draw.rect(topRegion, x, y);

            Draw.z(Layer.power);

            Tile other = world.tile(link);
            if(!linkValid(tile, other)) return;
            if(Mathf.zero(Renderer.bridgeOpacity)) return;

            Lines.stroke(bridgeWidth);
            Lines.line(bridgeRegion, x, y, other.worldx(), other.worldy(), false);

            float dst = Mathf.dst(x, y, other.worldx(), other.worldy()) - tilesize/4f;
            float ang = Angles.angle(x, y, other.worldx(), other.worldy());
            int seg = Mathf.round(dst / tilesize);

            if (seg == 0) return;
            for (int i = 0; i < seg; i++) {
                Tmp.v1.trns(ang, (dst/seg) * i + tilesize/8f).add(this);
                Draw.alpha(Mathf.absin(i - time / arrowTimeScl, arrowPeriod, 1f) * warmup * Renderer.bridgeOpacity);
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, ang);
            }
            Draw.color();
            Draw.reset();
        }
    }
}
