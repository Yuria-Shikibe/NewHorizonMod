package newhorizon.expand.block.distribution;

import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import newhorizon.expand.block.NHBlock;
import newhorizon.expand.block.NHBuilding;

public class AdaptConveyor extends NHBlock {
    public AdaptConveyor(String name) {
        super(name);
    }

    public class AdaptedConveyorBuild extends NHBuilding {
        public ItemStack item;
    }
}
