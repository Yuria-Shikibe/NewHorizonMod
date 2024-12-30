package newhorizon.expand.block.production.factory;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;

import static mindustry.Vars.content;

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

    public class AdaptCrafterBuild extends GenericCrafterBuild implements MultiBlockEntity{
        public Seq<Building> linkEntities;
        public Seq<Seq<Building>> linkProximity;
        public int dumpIndex;

        @Override
        public void created() {
            super.created();
            //linkProximity = new Seq<>();
            linkEntities = setLinkBuild(this, tile, team, size, rotation);
        }

        public void removeLinkEntity(){

        }

        public void handleLinkEntity(){

        }

        //most times its not a good idea to change the methods

        /*
        @Override
        public boolean dump(Item todump) {
            if (!block.hasItems || items.total() == 0 || linkProximity.size == 0 || (todump != null && !items.has(todump))) return false;
            for (int i = 0; i < linkProximity.size; i++) {
                Building other = linkProximity.get((i + dumpIndex) % linkProximity.size);
                if (todump == null) {
                    for (int ii = 0; ii < content.items().size; ii++) {
                        if (!items.has(ii)) continue;
                        Item item = content.items().get(ii);
                        if (other.acceptItem(this, item) && canDump(other, item)) {
                            other.handleItem(this, item);
                            items.remove(item, 1);
                            incrementDumpIndex(linkProximity.size);
                            return true;
                        }
                    }
                } else {
                    if (other.acceptItem(this, todump) && canDump(other, todump)) {
                        other.handleItem(this, todump);
                        items.remove(todump, 1);
                        incrementDumpIndex(linkProximity.size);
                        return true;
                    }
                }
                incrementDumpIndex(linkProximity.size);
            }
            return false;
        }

        @Override
        public void offload(Item item) {
            produced(item, 1);
            int dump = dumpIndex;
            for (int i = 0; i < linkProximity.size; i++) {
                incrementDumpIndex(linkProximity.size);
                Building other = linkProximity.get((i + dump) % linkProximity.size);
                if (other.acceptItem(this, item) && canDump(other, item)) {
                    other.handleItem(this, item);
                    return;
                }
            }
            handleItem(this, item);
        }

        public void incrementDumpIndex(int prox) {
            dumpIndex = ((dumpIndex + 1) % prox);
        }

         */


        @Override
        public void updateLinkProximity(){
            /*
            linkProximity.clear();
            if (linkEntities == null || linkEntities.isEmpty()) return;
            for (Building link : linkEntities){
                for (Building linkProx : link.proximity){
                    if (linkProx != this && !linkEntities.contains(linkProx)){
                        linkProximity.addUnique(linkProx);
                    }
                }
            }

            for (Building prox : proximity){
                if (!linkEntities.contains(prox)){
                    linkProximity.addUnique(prox);
                }
            }

             */
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateLinkProximity();
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
