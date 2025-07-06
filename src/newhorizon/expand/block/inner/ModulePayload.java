package newhorizon.expand.block.inner;

import mindustry.ctype.ContentType;
import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import newhorizon.content.blocks.ModuleBlock;

public class ModulePayload extends Block {
    public ModulePayload(String name) {
        super(name);
        solid = true;
        destructible = true;
        rebuildable = false;

        placeablePlayer = false;
        //hideDatabase = true;

        buildVisibility = BuildVisibility.shown;
    }

    @Override
    public void init() {
        super.init();
        ModuleBlock.modules.add(this);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.health);
        stats.remove(Stat.size);
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
