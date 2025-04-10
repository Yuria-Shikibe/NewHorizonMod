package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.PayloadConveyor;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.payload.AdaptPayloadConveyor;

public class PayloadBlock {
    public static Block payloadRail;

    public static void load(){
        payloadRail = new AdaptPayloadConveyor("payload-rail"){{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 10));
        }};
    }
}
