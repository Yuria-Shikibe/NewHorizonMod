package newhorizon.expand.block.inner;

import mindustry.world.Block;

public class ModulePayload extends Block {
    public ModulePayload(String name) {
        super(name);
        solid = true;
        destructible = true;
    }

    @Override
    public boolean canBeBuilt() {
        return false;
    }
}
