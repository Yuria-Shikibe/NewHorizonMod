package newhorizon.expand.block.inner;

import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.blocks.ModuleBlock;

public class ModulePayload extends Block {
    public ModulePayload(String name) {
        super(name);
        solid = true;
        destructible = true;
        rebuildable = false;

        placeablePlayer = false;
        hideDatabase = true;

        buildVisibility = BuildVisibility.shown;
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
    
    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return false;
    }
}
