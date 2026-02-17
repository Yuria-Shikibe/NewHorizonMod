package newhorizon.expand.block.flood;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.defence.AdaptWall;
import newhorizon.util.graphic.SpriteUtil;

import static newhorizon.util.graphic.SpriteUtil.*;

public class FloodFluidBlock extends AdaptWall implements FloodBlock{
    public FloodFluidBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasLiquids = true;
        group = BlockGroup.liquids;
        outputsLiquid = true;
        envEnabled |= Env.space | Env.underwater;
        maxShareStep = 1.5f;
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class FloodFluidBuilding extends AdaptWallBuild implements FloodBuilding{
        @Override
        public void updateTile(){
            dumpLiquid(this);
            applyHealing(this);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return NHLiquids.floodLiquid.contains(liquid);
        }

        @Override
        public float handleDamage(float amount) {
            return handleDamage(this, amount);
        }

        @Override
        public FloodBlock getFloodBlock() {
            return (FloodBlock) this.block;
        }
    }
}
