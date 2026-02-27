package newhorizon.expand.block.flood;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHContent;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.defence.AdaptWall;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.tilesize;

public class FloodFluidBlock extends AdaptWall implements FloodBlock{
    //[0] - healConsumption  - Plo-Naq - liquid unit consumed every tick for healing
    //[1] - healSpeed        - Plo-Naq - healing amount every tick, in percent
    //[2] - damageReduction  - Cho-Var - scaled damage reduction. damageReduction ^ 2 linear to fullness
    //[3] - damageAbsorption - Cho-Var - damage / this value = real removed amount
    //[4] - statMultiplier   - Kar-Ion - certain multiplier to some stats. linear
    public float[] packedData = {
            10f / 60f, // use 10 every second
            5f / 60f, // 100 seconds to heal, scale by stat multiplier
            0.95f,
            1000f,
            10f
    };

    public TextureRegion[] innerAtlasRegions;

    public FloodFluidBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasLiquids = true;
        group = BlockGroup.liquids;
        outputsLiquid = true;
        envEnabled |= Env.space | Env.underwater;
        maxShareStep = 3f;

        liquidCapacity = 50f;
        drawTeamOverlay = false;
    }

    @Override
    public void load() {
        super.load();
        innerAtlasRegions = SpriteUtil.splitRegionArray(name + "-inner-tiled", 32, 32, 0, SpriteUtil.ATLAS_INDEX_4_4_VANILLA);
    }

    @Override
    public float[] packedData() {
        return packedData;
    }

    @Override
    public void setBars() {
        super.setBars();

        removeBar("liquid");

        addBar("healing-speed", (FloodFluidBuilding entity) -> new Bar(
                () -> Core.bundle.format("nh.bar.plo-naq-healing-speed", Strings.autoFixed(entity.getHealingSpeed(entity) * 60f, 2)),
                () -> NHLiquids.ploNaq.color,
                () -> entity.liquids.get(NHLiquids.ploNaq) / liquidCapacity
        ));

        addBar("damage-reduction", (FloodFluidBuilding entity) -> new Bar(
                () -> Core.bundle.format("nh.bar.cho-vat-damage-reduction", Strings.autoFixed(entity.getDamageReduction(entity) * 100f, 2)),
                () -> NHLiquids.choVat.color,
                () -> entity.liquids.get(NHLiquids.choVat) / liquidCapacity
        ));

        addBar("stat-multiplier", (FloodFluidBuilding entity) -> new Bar(
                () -> Core.bundle.format("nh.bar.kar-ion-stat-multiplier", Strings.autoFixed((entity.getStatMultiplier(entity) - 1) * 100, 2)),
                () -> NHLiquids.karIon.color,
                () -> entity.liquids.get(NHLiquids.karIon) / liquidCapacity
        ));
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class FloodFluidBuilding extends AdaptWallBuild implements FloodBuilding{
        public int drawInnerIndex = 0;

        public void updateDrawRegion() {
            super.updateDrawRegion();
            drawInnerIndex = 0;
            for(int i = 0; i < 4; i++){
                Tile other1 = tile.nearby(Geometry.d4[i]);
                Tile other2 = tile.nearby(Tmp.p1.set(Geometry.d4[i]).add(Geometry.d4[i]));
                if(checkAutotileSame(other1) && checkAutotileSame(other2)) {
                    drawInnerIndex |= (1 << i);
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (drawIndex == 13) Draw.rect(innerAtlasRegions[drawInnerIndex], x, y);
            //drawDebug(this);

            Draw.z(NHContent.HEX_SHIELD_LAYER);
            Draw.color(NHLiquids.choVat.color);
            Draw.alpha((liquids.get(NHLiquids.choVat) / liquidCapacity));
            Fill.square(x, y, tilesize / 2f);
            Draw.color();
        }

        @Override
        public void drawSelect() {}

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
            return handleReducedDamage(this, amount);
        }

        @Override
        public FloodBlock getFloodBlock() {
            return (FloodBlock) this.block;
        }
    }
}
