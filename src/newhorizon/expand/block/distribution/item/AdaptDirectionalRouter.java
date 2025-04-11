package newhorizon.expand.block.distribution.item;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.DuctRouter;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class AdaptDirectionalRouter extends DuctRouter {
    public TextureRegion baseRegion, itemRegion;

    public AdaptDirectionalRouter(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;
    }

    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(NewHorizon.name("logistics-base"));
        itemRegion = Core.atlas.find(NewHorizon.name("logistics-item"));
        topRegion = Core.atlas.find(name + "-overlay");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
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

    public class AdaptDirectionalRouterBuild extends DuctRouterBuild{
        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            if(sortItem != null){
                Draw.color(sortItem.color);
                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
            Draw.rect(topRegion, x, y, rotdeg());
        }
    }
}
