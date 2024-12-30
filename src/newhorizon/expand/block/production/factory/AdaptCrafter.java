package newhorizon.expand.block.production.factory;

import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;

public class AdaptCrafter extends GenericCrafter implements MultiBlock{
    public AdaptCrafter(String name) {
        super(name);

        rotate = true;
        rotateDraw = true;
        quickRotate = false;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return super.canPlaceOn(tile, team, rotation) && checkLink(tile, team, size, rotation);
    }

    public class AdaptCrafterBuild extends GenericCrafterBuild{
        public Seq<Building> linkEntities;
        public Seq<Building> linkProximity;

        @Override
        public void created() {
            super.created();
            linkProximity = new Seq<>();
            linkEntities = setLinkBuild(this, tile, team, size, rotation);
        }

        public void removeLinkEntity(){

        }

        public void handleLinkEntity(){

        }

        public void updateLinkProximity(){

        }

        @Override
        public void remove() {
            removeLink(tile, size, rotation);
            super.remove();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
        }
    }
}
