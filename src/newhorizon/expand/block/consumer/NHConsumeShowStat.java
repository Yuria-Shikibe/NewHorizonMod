package newhorizon.expand.block.consumer;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.Consume;
import newhorizon.util.ui.display.ItemImage;
import newhorizon.util.ui.display.LiquidDisplay;


//This is just for show stats for annoying ConsumeDynamics
public class NHConsumeShowStat extends Consume {
    public final Func<Building, ItemStack[]> items;
    public final Func<Building, LiquidStack[]> liquids;


    @SuppressWarnings("unchecked")
    public <T extends Building> NHConsumeShowStat(Func<T, ItemStack[]> items, Func<T, LiquidStack[]> liquids) {
        this.items = (Func<Building, ItemStack[]>) items;
        this.liquids = (Func<Building, LiquidStack[]>) liquids;
    }

    @Override
    public void build(Building build, Table table) {
        /*
        if (items != null && items.get(build) != null) {

        }

        if (liquids != null && liquids.get(build) != null) {
            LiquidStack[][] currentLiquid = {liquids.get(build)};

            table.table(cont -> {
                table.update(() -> {
                    if (currentLiquid[0] != liquids.get(build)) {
                        rebuild(build, cont);
                        currentLiquid[0] = liquids.get(build);
                    }
                });

                rebuild(build, cont);
            });
        }

         */

        ItemStack[][] currentItem = {items.get(build)};
        LiquidStack[][] currentLiquid = {liquids.get(build)};

        table.table(cont -> {
            table.update(() -> {
                if (currentItem[0] != items.get(build)) {
                    rebuild(build, cont);
                    currentItem[0] = items.get(build);
                }
                if (currentLiquid[0] != liquids.get(build)) {
                    rebuild(build, cont);
                    currentLiquid[0] = liquids.get(build);
                }
            });

            rebuild(build, cont);
        });
    }

    private void rebuild(Building build, Table table) {
        int i = 0;
        table.clear();

        if (items != null && items.get(build) != null) {
            for (ItemStack stack : items.get(build)) {
                table.add(new ReqImage(new ItemImage(stack.item.uiIcon, Math.round(stack.amount * multiplier.get(build))),
                    () -> build.items != null && build.items.has(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8).left();
                if (++i % 4 == 0) table.row();
            }
        }

        if (liquids != null && liquids.get(build) != null) {
            for (LiquidStack stack : liquids.get(build)) {
                table.add(new ReqImage(new LiquidDisplay(stack.liquid, 0f, false),
                    () -> build.liquids != null && build.liquids.get(stack.liquid) > 0)).size(Vars.iconMed).padRight(8);
                if (++i % 4 == 0) table.row();
            }
        }
    }
}
