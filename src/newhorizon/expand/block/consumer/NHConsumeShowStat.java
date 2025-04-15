package newhorizon.expand.block.consumer;

import arc.func.Func;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.StatValues;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.util.ui.display.ItemImage;
import newhorizon.util.ui.display.LiquidDisplay;


//This is just for show stats for annoying ConsumeDynamics
public class NHConsumeShowStat extends Consume {
    public final Func<Building, ItemStack[]> items;
    public final Func<Building, LiquidStack[]> liquids;
    public final Func<Building, PayloadStack[]> payloads;
    public final Func<Building, PayloadSeq> payloadInventory;

    @SuppressWarnings("unchecked")
    public <T extends Building> NHConsumeShowStat(Func<T, ItemStack[]> items, Func<T, LiquidStack[]> liquids, Func<T, PayloadStack[]> payloads, Func<T, PayloadSeq> payloadInventory) {
        this.items = items == null? e -> new ItemStack[]{}: (Func<Building, ItemStack[]>) items;
        this.liquids = liquids == null? e -> new LiquidStack[]{}: (Func<Building, LiquidStack[]>) liquids;
        this.payloads = payloads == null? e -> new PayloadStack[]{}: (Func<Building, PayloadStack[]>) payloads;
        this.payloadInventory = payloadInventory == null? e -> new PayloadSeq(): (Func<Building, PayloadSeq>) payloadInventory;
    }


    @SuppressWarnings("unchecked")
    public <T extends Building> NHConsumeShowStat(Func<T, ItemStack[]> items, Func<T, LiquidStack[]> liquids, Func<T, PayloadStack[]> payloads) {
        this.items = items == null? e -> new ItemStack[]{}: (Func<Building, ItemStack[]>) items;
        this.liquids = liquids == null? e -> new LiquidStack[]{}: (Func<Building, LiquidStack[]>) liquids;
        this.payloads = payloads == null? e -> new PayloadStack[]{}: (Func<Building, PayloadStack[]>) payloads;
        this.payloadInventory = e -> new PayloadSeq();
    }

    @Override
    public void build(Building build, Table table) {
        ItemStack[][] currentItem = {items.get(build)};
        LiquidStack[][] currentLiquid = {liquids.get(build)};
        PayloadStack[][] currentPayload = {payloads.get(build)};

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
                if(currentPayload[0] != payloads.get(build)){
                    rebuild(build, cont);
                    currentPayload[0] = payloads.get(build);
                }
            });

            rebuild(build, cont);
        });
    }

    private void rebuild(Building build, Table table) {
        table.clear();
        int j = 0;
        if (payloadInventory.get(build) != null && !payloadInventory.get(build).isEmpty()) {
            for (ModulePayload module : ModuleBlock.modules) {
                if (payloadInventory.get(build).contains(module)) {
                    table.add(new ReqImage(StatValues.stack(module, payloadInventory.get(build).get(module)),
                            () -> true)).padRight(8).left();
                    if (++j % 4 == 0) table.row();
                }
            }
        }
        table.row();

        int i = 0;

        if (items.get(build) != null) {
            for (ItemStack stack : items.get(build)) {
                table.add(new ReqImage(StatValues.stack(stack.item, Math.round(stack.amount * multiplier.get(build))),
                    () -> build.items != null && build.items.has(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8).left();
                if (++i % 4 == 0) table.row();
            }
        }

        if (liquids.get(build) != null) {
            for (LiquidStack stack : liquids.get(build)) {
                table.add(new ReqImage(stack.liquid.uiIcon,
                    () -> build.liquids != null && build.liquids.get(stack.liquid) > 0)).size(Vars.iconMed).padRight(8);
                if (++i % 4 == 0) table.row();
            }
        }

        if (payloads.get(build) != null) {
            for(PayloadStack stack : payloads.get(build)){
                table.add(new ReqImage(StatValues.stack(stack.item, Math.round(stack.amount * multiplier.get(build))),
                        () -> build.getPayloads() != null && build.getPayloads().contains(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8);
                if(++i % 4 == 0) table.row();
            }
        }
    }
}
