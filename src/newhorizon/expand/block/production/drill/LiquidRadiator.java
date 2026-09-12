package newhorizon.expand.block.production.drill;

import arc.func.Boolf;
import arc.math.Mathf;
import mindustry.content.Liquids;
import mindustry.type.Liquid;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import newhorizon.content.NHStatValues;

public class LiquidRadiator extends DrillModule {
    public float liquidUse = 0.05f;
    public float waterEfficiencyBoost = 0.6f;
    public float maxEfficiencyBoost = 2f;
    public float chemicalHeatCapacity = 2f;
    public float chemicalPowerIncrease = 1.5f;
    public float maxPowerIncrease = 4f;
    public Boolf<Liquid> liquidFilter = liquid -> liquid.flammability <= 0.4f
            && liquid.explosiveness <= 0.4f
            && liquid.temperature <= 0.5f;

    public final ConsumeLiquidFilter liquidConsumer;

    public LiquidRadiator(String name) {
        super(name);

        hasLiquids = true;
        liquidConsumer = consume(new ConsumeLiquidFilter(this::acceptsLiquid, liquidUse));
        buildType = LiquidRadiatorBuild::new;
    }

    @Override
    public void init() {
        liquidConsumer.amount = liquidUse;
        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.input);
        stats.add(Stat.input, NHStatValues.liquidRadiatorLiquids(
                liquidUse,
                this::acceptsLiquid,
                this::efficiencyBoost,
                this::powerMultiplier
        ));
    }

    public boolean acceptsLiquid(Liquid liquid) {
        return liquid != null && liquidFilter.get(liquid);
    }

    public float efficiencyBoost(Liquid liquid) {
        float waterHeatCapacity = Liquids.water.heatCapacity;
        if (waterHeatCapacity <= 0f) return 0f;
        return Mathf.clamp(liquid.heatCapacity / waterHeatCapacity * waterEfficiencyBoost, 0f, maxEfficiencyBoost);
    }

    public float powerMultiplier(Liquid liquid) {
        float waterHeatCapacity = Liquids.water.heatCapacity;
        float heatCapacityRange = chemicalHeatCapacity - waterHeatCapacity;
        if (heatCapacityRange <= 0f) return 1f;

        float increase = (liquid.heatCapacity - waterHeatCapacity) / heatCapacityRange * chemicalPowerIncrease;
        return 1f + Mathf.clamp(increase, 0f, maxPowerIncrease);
    }

    public class LiquidRadiatorBuild extends DrillModuleBuild {
        @Override
        public void updateDrill(AdaptDrill.AdaptDrillBuild drill) {
            Liquid liquid = liquidConsumer.getConsumed(this);
            if (liquid == null || efficiency <= 0f) return;

            float activity = Mathf.clamp(efficiency);
            drill.moduleBoost += efficiencyBoost(liquid) * activity;
            drill.modulePowerMultiplier *= Mathf.lerp(1f, powerMultiplier(liquid), activity);
        }
    }
}
