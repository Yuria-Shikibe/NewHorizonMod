package newhorizon.expand.block.production.factory;

import arc.Core;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.type.LiquidStack;
import newhorizon.content.NHStats;

public class HeatBoostCrafter extends GenericCrafter {
    public float heatRequirement = 6f;
    public float heatEfficiencyIncrease;
    public float heatOutputIncrease = 1f;

    public HeatBoostCrafter(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (HeatBoostCrafterBuild build) -> new Bar(
                () -> Core.bundle.format("bar.heatamount", (int)(build.heat + 0.001f)) + " | "
                        + Core.bundle.format("nh.bar.efficiency", (int)(build.efficiencyMultiplier() * 100f + 0.001f)) + " | "
                        + Core.bundle.format("nh.bar.output", (int)(build.outputMultiplier() * 100f + 0.001f)),
                () -> Pal.lightOrange,
                () -> heatRequirement <= 0f ? 0f : Mathf.clamp(build.heat / heatRequirement)
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        if (heatRequirement > 0f) {
            stats.add(Stat.booster, heatRequirement, StatUnit.heatUnits);
            stats.add(Stat.maxEfficiency, (1f + Math.max(heatEfficiencyIncrease, 0f)) * 100f, StatUnit.percent);
            stats.add(NHStats.maxOutputIncrease, Math.max(heatOutputIncrease, 0f) * 100f, StatUnit.percent);
        }
    }

    public class HeatBoostCrafterBuild extends GenericCrafterBuild implements HeatConsumer {
        public float[] sideHeat = new float[4];
        public float heat;

        @Override
        public void updateTile() {
            heat = enabled ? calculateHeat(sideHeat) : 0f;
            if (efficiency > 0f) {
                progress += getProgressIncrease(craftTime) * efficiencyMultiplier();
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                if (outputLiquids != null) {
                    float inc = getProgressIncrease(1f) * efficiencyMultiplier() * outputMultiplier();
                    for (var output : outputLiquids) {
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }
                if (wasVisible && Mathf.chanceDelta(updateEffectChance)) {
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
            totalProgress += warmup * Time.delta;
            if (progress >= 1f) craft();
            dumpOutputs();
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            if (ignoreLiquidFullness) {
                return super.getProgressIncrease(baseTime);
            }

            float scaling = 1f, max = 1f;
            if (outputLiquids != null) {
                max = 0f;
                for (LiquidStack output : outputLiquids) {
                    float value = (liquidCapacity - liquids.get(output.liquid))
                            / (output.amount * efficiencyMultiplier() * outputMultiplier() * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        @Override
        public float scaleOutput(float amount) {
            return super.scaleOutput(amount) * outputMultiplier();
        }

        public float efficiencyMultiplier() {
            if (heatRequirement <= 0f) return 1f;
            return 1f + Mathf.clamp(heat / heatRequirement) * Math.max(heatEfficiencyIncrease, 0f);
        }

        public float outputMultiplier() {
            if (heatRequirement <= 0f) return 1f;
            return 1f + Mathf.clamp(heat / heatRequirement) * Math.max(heatOutputIncrease, 0f);
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            return Math.max(heatRequirement, 0f);
        }
    }
}
