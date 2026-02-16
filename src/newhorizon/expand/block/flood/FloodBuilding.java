package newhorizon.expand.block.flood;

import arc.math.Mathf;
import mindustry.gen.Building;
import newhorizon.content.NHLiquids;

public interface FloodBuilding{
    FloodBlock getFloodBlock();

    default void dumpLiquid(Building building){
        building.dumpLiquid(NHLiquids.ploNaq, 2.25f);
        building.dumpLiquid(NHLiquids.choVat, 2.5f);
        building.dumpLiquid(NHLiquids.karIon, 2f);
    }

    default float scaledDefense(Building building){
        if (building.liquids == null) return 0f;
        return Mathf.sqrt(building.liquids.get(NHLiquids.choVat) / building.block.liquidCapacity);
    }

    default float getDamageReduction(Building building) {
        if (building.liquids == null) return 0f;
        return scaledDefense(building) * getFloodBlock().damageReduction();
    }

    default float handleDamage(Building building, float amount) {
        if (building.liquids != null) building.liquids.remove(NHLiquids.choVat, amount / getFloodBlock().damageAbsorption());
        return amount * (1 - getDamageReduction(building));
    }

    default void applyHealing(Building building){
        if (building.damaged() && building.liquids != null && building.liquids.get(NHLiquids.ploNaq) > 0) {
            building.heal(getFloodBlock().healSpeed());
            building.liquids.remove(NHLiquids.ploNaq, getFloodBlock().healConsumption());
        }
    }
}
