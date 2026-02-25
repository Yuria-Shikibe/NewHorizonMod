package newhorizon.expand.block.flood;

import arc.Core;
import arc.util.Strings;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHLiquids;

public class FloodCore extends CoreBlock implements FloodBlock{
    public float[] packedData = {
            10f / 60f, // use 10 every second
            5f / 60f, // 100 seconds to heal, scale by stat multiplier
            0.95f,
            1000f,
            10f
    };

    public FloodCore(String name) {
        super(name);
    }

    @Override
    public float[] packedData() {
        return packedData;
    }

    @Override
    public void setBars() {
        super.setBars();

        removeBar("liquid");

        addBar("healing-speed", (FloodFluidBlock.FloodFluidBuilding entity) -> new Bar(
                () -> Core.bundle.format("nh.bar.plo-naq-healing-speed", Strings.autoFixed(entity.getHealingSpeed(entity) * 60f, 2)),
                () -> NHLiquids.ploNaq.color,
                () -> entity.liquids.get(NHLiquids.ploNaq) / liquidCapacity
        ));

        addBar("damage-reduction", (FloodFluidBlock.FloodFluidBuilding entity) -> new Bar(
                () -> Core.bundle.format("nh.bar.cho-vat-damage-reduction", Strings.autoFixed(entity.getDamageReduction(entity) * 100f, 2)),
                () -> NHLiquids.choVat.color,
                () -> entity.liquids.get(NHLiquids.choVat) / liquidCapacity
        ));

        addBar("stat-multiplier", (FloodFluidBlock.FloodFluidBuilding entity) -> new Bar(
                () -> Core.bundle.format("nh.bar.kar-ion-stat-multiplier", Strings.autoFixed((entity.getStatMultiplier(entity) - 1) * 100, 2)),
                () -> NHLiquids.karIon.color,
                () -> entity.liquids.get(NHLiquids.karIon) / liquidCapacity
        ));
    }

    public class FloodCoreBuild extends CoreBuild implements FloodBuilding{
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
