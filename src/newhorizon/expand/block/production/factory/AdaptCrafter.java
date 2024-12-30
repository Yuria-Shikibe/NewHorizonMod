package newhorizon.expand.block.production.factory;

import arc.math.geom.Point2;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;
import newhorizon.content.blocks.InnerBlock;

import static mindustry.Vars.world;

public class AdaptCrafter extends GenericCrafter implements MultiBlock{
    public AdaptCrafter(String name) {
        super(name);

        rotate = true;
        rotateDraw = true;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return super.canPlaceOn(tile, team, rotation) && checkLink(tile, team, size, rotation);
    }

    public class AdaptCrafterBuild extends GenericCrafterBuild implements MultiBlock{
        @Override
        public void created() {
            super.created();
            setLinkBuild(this, tile, team, size, rotation);
        }
    }
}
