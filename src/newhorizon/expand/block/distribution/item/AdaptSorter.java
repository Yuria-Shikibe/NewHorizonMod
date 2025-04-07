package newhorizon.expand.block.distribution.item;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Sorter;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class AdaptSorter extends Sorter {
    public TextureRegion itemRegion;
    public AdaptSorter(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;
    }

    @Override
    public void load() {
        super.load();
        itemRegion = Core.atlas.find(NewHorizon.name("logistics-item"));
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

    public class AdaptSorterBuild extends SorterBuild {
        @Override
        public void draw(){
            Draw.rect(region, x, y);

            if(sortItem != null){
                Draw.color(sortItem.color);
                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
        }
    }
}
