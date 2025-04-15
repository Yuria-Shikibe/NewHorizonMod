package newhorizon.expand.block.inner;

import mindustry.world.Block;
import newhorizon.content.blocks.ModuleBlock;

public class ModulePayload extends Block {
    public ModulePayload(String name) {
        super(name);
        solid = true;
        destructible = true;
    }

    @Override
    public void init() {
        super.init();
        ModuleBlock.modules.add(this);
    }

    @Override
    public boolean canBeBuilt() {
        return false;
    }
}
