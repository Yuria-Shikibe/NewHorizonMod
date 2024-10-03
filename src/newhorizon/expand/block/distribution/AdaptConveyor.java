package newhorizon.expand.block.distribution;

import mindustry.type.ItemStack;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;

public class AdaptConveyor extends AdaptBlock {
    public AdaptConveyor(String name) {
        super(name);
    }

    public class AdaptedConveyorBuild extends AdaptBuilding {
        public ItemStack item;
    }
}
