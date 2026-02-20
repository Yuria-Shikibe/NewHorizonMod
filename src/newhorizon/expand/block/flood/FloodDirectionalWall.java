package newhorizon.expand.block.flood;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.NHLiquids;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.tilesize;

public class FloodDirectionalWall extends Wall implements FloodBlock{
    public TextureRegion[] rotRegions;
    public TextureRegion[] innerRegions;
    public TextureRegion[] outerRegions;


    public FloodDirectionalWall(String name) {
        super(name);
        size = 1;
        insulated = true;
        absorbLasers = true;
        placeableLiquid = true;
        crushDamageMultiplier = 1f;
        teamPassable = true;

        rotate = true;
    }

    @Override
    public void load() {
        super.load();
        rotRegions = SpriteUtil.loadIndexedRegions(name + "-rot", 4);
        innerRegions = SpriteUtil.splitRegionArray(name + "-inner", 32, 32, 0, new int[]{1, 0, 2, 3});
        outerRegions = SpriteUtil.splitRegionArray(name + "-outer", 32, 32, 0, new int[]{1, 0, 2, 3});
    }

    @Override
    public float[] packedData() {
        return new float[0];
    }

    public class FloodDirectionalWallBuild extends WallBuild implements FloodBuilding{
        @Override
        public void draw() {
            super.draw();
            Lines.stroke(2f);
            Draw.z(Layer.max);
            Draw.color(NHLiquids.ploNaq.color);
            Lines.lineAngle(x - 3, y + 2.5f, 0, liquids.get(NHLiquids.ploNaq) / liquidCapacity * 6);
            Draw.color(NHLiquids.choVat.color);
            Lines.lineAngle(x - 3, y, 0, liquids.get(NHLiquids.choVat) / liquidCapacity * 6);
            Draw.color(NHLiquids.karIon.color);
            Lines.lineAngle(x - 3, y - 2.5f, 0, liquids.get(NHLiquids.karIon) / liquidCapacity * 6);
            Draw.color();
            Draw.z(Layer.block);
        }

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
            removeLiquidOnDamage(this, amount);
            return super.handleDamage(amount);
        }

        @Override
        public FloodBlock getFloodBlock() {
            return (FloodBlock) this.block;
        }
    }
}
