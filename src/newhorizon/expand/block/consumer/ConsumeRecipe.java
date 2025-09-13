package newhorizon.expand.block.consumer;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.ui.ReqImage;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.StatValues;
import newhorizon.expand.type.Recipe;

public class ConsumeRecipe extends Consume {
    public final @Nullable Func<Building, Recipe> recipe;
    public final Func<Building, Recipe> display;

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeRecipe(Func<T, Recipe> recipe, Func<T, Recipe> display) {
        this.recipe = (Func<Building, Recipe>) recipe;
        this.display = (Func<Building, Recipe>) display;
    }

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeRecipe(Func<T, Recipe> recipe) {
        this.recipe = (Func<Building, Recipe>) recipe;
        this.display = (Func<Building, Recipe>) recipe;
    }

    @Override
    public void apply(Block block) {
        block.hasItems = true;
        block.hasLiquids = true;

        block.acceptsItems = true;
        block.acceptsPayload = true;
    }

    @Override
    public void update(Building build) {
        if (recipe.get(build) == null) return;
        for (LiquidStack stack : recipe.get(build).inputLiquid) {
            build.liquids.remove(stack.liquid, stack.amount * build.edelta() * multiplier.get(build));
        }
    }

    @Override
    public void trigger(Building build) {
        if (recipe.get(build) == null) return;
        for (ItemStack stack : recipe.get(build).inputItem) {
            build.items.remove(stack.item, Math.round(stack.amount * multiplier.get(build)));
        }
        for(PayloadStack stack : recipe.get(build).inputPayload) {
            build.getPayloads().remove(stack.item, Math.round(stack.amount * multiplier.get(build)));
        }
    }

    @Override
    public float efficiency(Building build) {
        float ed = build.edelta() * build.efficiencyScale();
        if (ed <= 0.00000001f) return 0f;
        if (recipe.get(build) == null) return 0f;
        float min = 1f;
        for (PayloadStack stack : recipe.get(build).inputPayload){
            if(!build.getPayloads().contains(stack.item, Math.round(stack.amount * multiplier.get(build)))){
                min = 0f;
                break;
            }
        }
        for (ItemStack stack : recipe.get(build).inputItem){
            if(!build.items.has(stack.item, Math.round(stack.amount * multiplier.get(build)))){
                min = 0f;
                break;
            }
        }
        for (LiquidStack stack : recipe.get(build).inputLiquid) {
            min = Math.min(build.liquids.get(stack.liquid) / (stack.amount * ed * multiplier.get(build)), min);
        }
        return min;
    }

    @Override
    public void build(Building build, Table table) {
        if (display.get(build) == null) return;
        table.update(() -> {
            table.clear();
            table.left();

            ItemStack[] currentItem = display.get(build).inputItem.toArray(ItemStack.class);
            LiquidStack[] currentLiquid = display.get(build).inputLiquid.toArray(LiquidStack.class);
            PayloadStack[] currentPayload = display.get(build).inputPayload.toArray(PayloadStack.class);
            table.table(cont -> {
                int i = 0;
                if (currentItem != null) {
                    for (ItemStack stack : currentItem) {
                        cont.add(new ReqImage(StatValues.stack(stack.item, Math.round(stack.amount * multiplier.get(build))),
                                () -> build.items != null && build.items.has(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8).left();
                        if (++i % 4 == 0) cont.row();
                    }
                }

                if (currentLiquid != null) {
                    for (LiquidStack stack : currentLiquid) {
                        cont.add(new ReqImage(stack.liquid.uiIcon,
                                () -> build.liquids != null && build.liquids.get(stack.liquid) > 0)).size(Vars.iconMed).padRight(8);
                        if (++i % 4 == 0) cont.row();
                    }
                }

                if (currentPayload != null) {
                    for (PayloadStack stack : currentPayload) {
                        cont.add(new ReqImage(StatValues.stack(stack.item, Math.round(stack.amount * multiplier.get(build))),
                                () -> build.getPayloads() != null && build.getPayloads().contains(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8);
                        if (++i % 4 == 0) cont.row();
                    }
                }
            });
        });
    }
}
