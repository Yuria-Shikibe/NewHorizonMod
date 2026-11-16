package newhorizon.expand.block.production.factory;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.liquid.Conduit.ConduitBuild;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import newhorizon.expand.block.BasicMultiBlock;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.content.NHStats;

import static mindustry.Vars.*;

public class MultiBlockCrafter extends BasicMultiBlock {
    public @Nullable ItemStack outputItem;
    public @Nullable ItemStack[] outputItems;
    public @Nullable LiquidStack outputLiquid;
    public @Nullable LiquidStack[] outputLiquids;

    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;
    public boolean heatBoostEnabled = false;
    public float heatRequirement = 6f;
    public float heatEfficiencyIncrease;
    public float heatOutputIncrease = 1f;

    public float craftTime = 80;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;

    public DrawBlock drawer = new DrawDefault();

    public MultiBlockCrafter(String name) {
        super(name);

        update = true;
        solid = true;
        hasItems = true;
        sync = true;

        ambientSound = Sounds.loopMachine;
        ambientSoundVolume = 0.03f;

        flags = EnumSet.of(BlockFlag.factory);
    }

    @Override
    public void setStats() {
        stats.timePeriod = craftTime;
        super.setStats();

        if ((hasItems && itemCapacity > 0) || outputItems != null)
            stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        if (outputItems != null) stats.add(Stat.output, StatValues.items(craftTime, outputItems));
        if (outputLiquids != null) stats.add(Stat.output, StatValues.liquids(1f, outputLiquids));
        if (heatBoostEnabled && heatRequirement > 0f) {
            stats.add(Stat.booster, heatRequirement, StatUnit.heatUnits);
            stats.add(Stat.maxEfficiency, (1f + Math.max(heatEfficiencyIncrease, 0f)) * 100f, StatUnit.percent);
            stats.add(NHStats.maxOutputIncrease, Math.max(heatOutputIncrease, 0f) * 100f, StatUnit.percent);
        }

        stats.remove(Stat.size);
        stats.add(Stat.size, "@x@", getMaxSize(size, 0).x, getMaxSize(size, 0).y);
    }

    @Override
    public void setBars() {
        super.setBars();

        //set up liquid bars for liquid outputs
        if (outputLiquids != null && outputLiquids.length > 0) {
            //no need for dynamic liquid bar
            removeBar("liquid");

            //then display output buffer
            for (var stack : outputLiquids) {
                addLiquidBar(stack.liquid);
            }
        }

        if (heatBoostEnabled) {
            addBar("heat", (AdaptCrafterBuild build) -> new Bar(
                    () -> Core.bundle.format("bar.heatamount", (int)(build.heat + 0.001f)) + " | "
                            + Core.bundle.format("nh.bar.efficiency", (int)(build.efficiencyMultiplier() * 100f + 0.001f)) + " | "
                            + Core.bundle.format("nh.bar.output", (int)(build.outputMultiplier() * 100f + 0.001f)),
                    () -> Pal.lightOrange,
                    () -> heatRequirement <= 0f ? 0f : Mathf.clamp(build.heat / heatRequirement)
            ));
        }
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-icon", name);
    }

    @Override
    public void init() {
        if (outputItems == null && outputItem != null) outputItems = new ItemStack[]{outputItem};
        if (outputLiquids == null && outputLiquid != null) outputLiquids = new LiquidStack[]{outputLiquid};
        if (outputLiquid == null && outputLiquids != null && outputLiquids.length > 0) outputLiquid = outputLiquids[0];

        outputsLiquid = outputLiquids != null;

        if (outputItems != null) hasItems = true;
        if (outputLiquids != null) hasLiquids = true;

        super.init();
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    @Override
    public boolean rotatedOutput(int fromX, int fromY, Tile destination) {
        if (!(destination.build instanceof ConduitBuild)) return false;

        Building crafter = world.build(fromX, fromY);
        if (crafter == null) return false;
        if (liquidOutputDirections == null) return false;
        int relative = Mathf.mod(crafter.relativeTo(destination) - crafter.rotation, 4);
        for (int direction : liquidOutputDirections) {
            if (direction == -1 || direction == relative) return false;
        }

        return true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public boolean outputsItems() {
        return outputItems != null;
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    public class AdaptCrafterBuild extends BasicMultiBuilding implements HeatConsumer {
        public float progress;
        public float totalProgress;
        public float warmup;
        public float[] sideHeat = new float[4];
        public float[] outputAccumulator;
        public float heat;

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            if (liquids != null) super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public boolean shouldConsume() {
            if (outputItems != null) {
                for (var output : outputItems) {
                    if (items.get(output.item) + Mathf.ceil(output.amount * outputMultiplier()) > itemCapacity) {
                        return false;
                    }
                }
            }
            if (outputLiquids != null && !ignoreLiquidFullness) {
                boolean allFull = true;
                for (var output : outputLiquids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        //if there's still space left, it's not full for all liquids
                        allFull = false;
                    }
                }

                //if there is no space left for any liquid, it can't reproduce
                if (allFull) {
                    return false;
                }
            }

            return enabled;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            heat = heatBoostEnabled && enabled ? calculateHeat(sideHeat) : 0f;
            if (efficiency > 0) {
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

            //limit progress increase by maximum amount of liquid it can produce
            float scaling = 1f, max = 1f;
            if (outputLiquids != null) {
                max = 0f;
                for (var s : outputLiquids) {
                    float value = (liquidCapacity - liquids.get(s.liquid))
                            / (s.amount * efficiencyMultiplier() * outputMultiplier() * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            //when dumping excess take the maximum value instead of the minimum.
            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        public float outputMultiplier() {
            if (!heatBoostEnabled || heatRequirement <= 0f) return 1f;
            return 1f + Mathf.clamp(heat / heatRequirement) * Math.max(heatOutputIncrease, 0f);
        }

        public float efficiencyMultiplier() {
            if (!heatBoostEnabled || heatRequirement <= 0f) return 1f;
            return 1f + Mathf.clamp(heat / heatRequirement) * Math.max(heatEfficiencyIncrease, 0f);
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            return heatBoostEnabled ? Math.max(MultiBlockCrafter.this.heatRequirement, 0f) : 0f;
        }

        public float warmupTarget() {
            return 1f;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        public void craft() {
            consume();

            if (outputItems != null) {
                if (outputAccumulator == null || outputAccumulator.length != outputItems.length) {
                    outputAccumulator = new float[outputItems.length];
                }
                for (int i = 0; i < outputItems.length; i++) {
                    var output = outputItems[i];
                    outputAccumulator[i] += output.amount * outputMultiplier();
                    int amount = Mathf.floor(outputAccumulator[i]);
                    outputAccumulator[i] -= amount;
                    for (int j = 0; j < amount; j++) {
                        offload(output.item);
                    }
                }
            }

            if (wasVisible) {
                craftEffect.at(x, y, rotdeg());
            }
            progress %= 1f;
        }

        public void dumpOutputs() {
            boolean timer = timer(timerDump, dumpTime / timeScale);
            if (outputItems != null && timer) {
                for (ItemStack output : outputItems) {
                    dump(output.item);
                }
            }

            if (outputLiquids != null) {
                for (int i = 0; i < outputLiquids.length; i++) {
                    LiquidStack liquid = outputLiquids[i];
                    int direction = liquidOutputDirections != null && liquidOutputDirections.length > i
                            ? liquidOutputDirections[i] : -1;
                    dumpLiquid(liquid.liquid, 2f, direction);
                }
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return progress();
            //attempt to prevent wild total liquid fluctuation, at least for crafters
            if (sensor == LAccess.totalLiquids && outputLiquid != null) return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public float progress() {
            return Mathf.clamp(progress);
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return itemCapacity;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
        }
    }
}
