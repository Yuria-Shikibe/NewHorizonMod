package newhorizon.expand.block.graph;

import mindustry.type.Category;
import newhorizon.content.NHItems;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;

import static mindustry.type.ItemStack.with;

public class GraphBlock extends AdaptBlock {
    public GraphBlock(String name) {
        super(name);

        update = true;
        isGraphEntity = true;

        requirements(Category.defense, with(NHItems.zeta, 10));
    }

    public class GraphBlockBuild extends AdaptBuilding{

    }
}
