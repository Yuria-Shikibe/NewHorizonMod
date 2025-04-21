package newhorizon.expand.block.distribution.item.logistics;

import arc.struct.Seq;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Junction;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class AdaptJunction extends Junction {
    public AdaptJunction(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;
    }
}
