package newhorizon.expand.block.consumer;

import arc.func.Func;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;

public class NHConsumeItemDynamic extends Consume {
    public final Func<Building, ItemStack[]> items;

    @SuppressWarnings("unchecked")
    public <T extends Building> NHConsumeItemDynamic(Func<T, ItemStack[]> items) {
        this.items = (Func<Building, ItemStack[]>) items;
    }

    @Override
    public void apply(Block block) {
        if (items != null) {
            block.hasItems = true;
            block.acceptsItems = true;
        }
    }

    @Override
    public void trigger(Building build) {
        if (items != null && items.get(build) != null) {
            for (ItemStack stack : items.get(build)) {
                build.items.remove(stack.item, Math.round(stack.amount * multiplier.get(build)));
            }
        }
    }

    @Override
    public float efficiency(Building build) {
        if (items != null && items.get(build) != null) {
            return build.consumeTriggerValid() || build.items.has(items.get(build), multiplier.get(build)) ? 1f : 0f;
        } else {
            return 1f;
        }
    }
}