package newhorizon.expand.block.consumer;

import arc.func.Func;
import mindustry.gen.Building;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;

public class NHConsumeLiquidDynamic extends Consume {
    public final Func<Building, LiquidStack[]> liquids;

    @SuppressWarnings("unchecked")
    public <T extends Building> NHConsumeLiquidDynamic(Func<T, LiquidStack[]> liquids) {
        this.liquids = (Func<Building, LiquidStack[]>) liquids;
    }

    @Override
    public void apply(Block block) {
        if (liquids != null) {
            block.hasLiquids = true;
        }
    }
    /*

    @Override
    public void build(Building build, Table table) {
        if (liquids != null && liquids.get(build) != null) {
            LiquidStack[][] current = {liquids.get(build)};

            table.table(cont -> {
                table.update(() -> {
                    if (current[0] != liquids.get(build)) {
                        rebuild(build, cont);
                        current[0] = liquids.get(build);
                    }
                });

                rebuild(build, cont);
            });
        }
    }

    private void rebuild(Building build, Table table) {
        if (liquids != null && liquids.get(build) != null) {
            table.clear();
            int i = 0;

            for (LiquidStack stack : liquids.get(build)) {
                table.add(new ReqImage(new LiquidDisplay(stack.liquid, 0f, false),
                    () -> build.liquids != null && build.liquids.get(stack.liquid) > 0)).size(Vars.iconMed).padRight(8);
                if (++i % 4 == 0) table.row();
            }
        }
    }

     */

    @Override
    public void update(Building build) {
        if (liquids != null && liquids.get(build) != null) {
            float mult = multiplier.get(build);
            for (LiquidStack stack : liquids.get(build)) {
                build.liquids.remove(stack.liquid, stack.amount * build.edelta() * mult);
            }
        }
    }

    @Override
    public float efficiency(Building build) {
        if (liquids != null && liquids.get(build) != null) {
            float ed = build.edelta();
            if (ed <= 0.00000001f) return 0f;
            float min = 1f;
            for (LiquidStack stack : liquids.get(build)) {
                min = Math.min(build.liquids.get(stack.liquid) / (stack.amount * ed * multiplier.get(build)), min);
            }
            return min;
        } else {
            return 1f;
        }
    }
}
