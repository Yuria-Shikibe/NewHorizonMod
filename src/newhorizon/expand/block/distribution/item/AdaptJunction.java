package newhorizon.expand.block.distribution.item;

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

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (state.rules.mode() == Gamemode.sandbox) return true;

        Seq<GravityTrapField> fields = new Seq<>();
        NHGroups.gravityTraps.intersect(tile.worldx(), tile.worldy(), tilesize, tilesize, fields);
        if (fields.isEmpty()) return false;
        for (GravityTrapField field : fields) {
            if (field.team() != team) return false;
        }
        return true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        NHVars.renderer.drawGravityTrap();
    }
}
