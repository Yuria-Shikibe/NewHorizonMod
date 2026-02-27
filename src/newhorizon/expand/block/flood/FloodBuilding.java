package newhorizon.expand.block.flood;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import newhorizon.content.NHLiquids;

public interface FloodBuilding{
    FloodBlock getFloodBlock();

    default void drawDebug(Building building) {
        float x = building.getX(), y = building.getY();
        var liquids = building.liquids;
        var liquidCapacity = building.block.liquidCapacity;

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

    default void dumpLiquid(Building building) {
        building.dumpLiquid(NHLiquids.ploNaq, 12f);
        building.dumpLiquid(NHLiquids.choVat, 8f);
        building.dumpLiquid(NHLiquids.karIon, 4f);
    }

    default float scaledHealing(Building building) {
        if (building.liquids == null) return 0f;
        return Mathf.sqr(building.liquids.get(NHLiquids.ploNaq) / building.block.liquidCapacity);
    }

    default float scaledDefense(Building building) {
        if (building.liquids == null) return 0f;
        return Mathf.sqrt(building.liquids.get(NHLiquids.choVat) / building.block.liquidCapacity);
    }

    default float scaledMultiplier(Building building) {
        if (building.liquids == null) return 0f;
        return building.liquids.get(NHLiquids.karIon) / building.block.liquidCapacity;
    }

    default float getHealingSpeed(Building building) {
        if (building.liquids == null) return 0f;
        return scaledHealing(building) * getStatMultiplier(building) * getFloodBlock().healSpeed();
    }

    default float getDamageReduction(Building building) {
        if (building.liquids == null) return 0f;
        return scaledDefense(building) * getFloodBlock().damageReduction();
    }

    default float getStatMultiplier(Building building) {
        if (building.liquids == null) return 1f;
        return scaledMultiplier(building) * (getFloodBlock().statMultiplier() - 1) + 1f;
    }

    default void removeLiquidOnDamage(Building building, float amount) {
        if (building.liquids != null)
            building.liquids.remove(NHLiquids.choVat, amount / getFloodBlock().damageAbsorption());
    }

    default float handleReducedDamage(Building building, float amount) {
        return (1 - getDamageReduction(building)) * amount;
    }

    default void applyHealing(Building building) {
        if (building.damaged() && building.liquids != null && building.liquids.get(NHLiquids.ploNaq) > 0) {
            building.heal(getHealingSpeed(building) * building.maxHealth() * Time.delta);
            building.liquids.remove(NHLiquids.ploNaq, getFloodBlock().healConsumption() * Time.delta);
        }
    }
}
