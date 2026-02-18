package newhorizon.expand.block.flood;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Strings;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.defence.AdaptWall;

import static mindustry.Vars.tilesize;

public class FloodFluidBlock extends AdaptWall implements FloodBlock{
    public FloodFluidBlock(String name) {
        super(name);
        update = true;
        solid = true;
        hasLiquids = true;
        group = BlockGroup.liquids;
        outputsLiquid = true;
        envEnabled |= Env.space | Env.underwater;
        maxShareStep = 1.75f;

        liquidCapacity = 50f;
    }

    @Override
    public float damageReduction() {
        return 0.999f;
    }

    @Override
    public void setBars() {
        super.setBars();

        removeBar("liquid");

        addLiquidBar(NHLiquids.ploNaq);


        addBar("damage-reduction", (FloodFluidBuilding entity) -> new Bar(
                () -> Strings.format("Damage Reduction: @%", Strings.autoFixed(entity.getDamageReduction(entity) * 100f, 2)),
                () -> NHLiquids.choVat.color,
                () -> entity.liquids.get(NHLiquids.choVat) / liquidCapacity
        ));

        addLiquidBar(NHLiquids.karIon);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class FloodFluidBuilding extends AdaptWallBuild implements FloodBuilding{

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
        public float getDamageReduction() {
            return getDamageReduction(this);
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
