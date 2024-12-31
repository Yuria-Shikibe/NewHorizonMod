package newhorizon.expand.block.production.factory;

import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Log;
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
        //ordered map, target-source pair
        public OrderedMap<Building, Building> linkProximityMap;
        public int dumpIndex = 0;

        @Override
        public void created() {
            super.created();
            linkProximityMap = new OrderedMap<>();
            linkEntities = setLinkBuild(this, tile, team, size, rotation);
        }

        public void removeLinkEntity(){

        }

        public void handleLinkEntity(){

        }

        //most times its not a good idea to change the methods


        @Override
        public boolean dump(Item todump) {
            if (!block.hasItems || items.total() == 0 || linkProximityMap.size == 0 || (todump != null && !items.has(todump))) return false;
            for (int i = 0; i < linkProximityMap.size; i++) {
                Building target = linkProximityMap.orderedKeys().get((i + dumpIndex) % linkProximityMap.size);
                Building source = linkProximityMap.get(target);

                if (todump == null) {
                    for (int ii = 0; ii < content.items().size; ii++) {
                        if (!items.has(ii)) continue;
                        Item item = content.items().get(ii);
                        if (target.acceptItem(source, item) && canDump(target, item)) {
                            target.handleItem(source, item);
                            items.remove(item, 1);
                            incrementDumpIndex(linkProximityMap.size);
                            return true;
                        }
                    }
                } else {
                    if (target.acceptItem(source, todump) && canDump(target, todump)) {
                        target.handleItem(source, todump);
                        items.remove(todump, 1);
                        incrementDumpIndex(linkProximityMap.size);
                        return true;
                    }
                }
                incrementDumpIndex(linkProximityMap.size);
            }
            return false;
        }

        @Override
        public void offload(Item item) {
            produced(item, 1);
            for (int i = 0; i < linkProximityMap.size; i++) {
                incrementDumpIndex(linkProximityMap.size);
                Building target = linkProximityMap.orderedKeys().get(dumpIndex);
                Building source = linkProximityMap.get(target);
                if (target.acceptItem(source, item) && canDump(target, item)) {
                    target.handleItem(source, item);
                    return;
                }
            }
            handleItem(this, item);
        }

        public void incrementDumpIndex(int prox) {
            dumpIndex = ((dumpIndex + 1) % prox);
        }

        @Override
        public void updateLinkProximity(){
            if (linkEntities != null) {
                linkProximityMap.clear();
                //add link entity's proximity
                for (Building link : linkEntities){
                    for (Building linkProx : link.proximity){
                        if (linkProx != this && !linkEntities.contains(linkProx)){
                            linkProximityMap.put(linkProx, link);
                        }
                    }
                }

                //add self entity's proximity
                Seq<Building> linkProximity = new Seq<>();
                for (Building prox : proximity){
                    if (!linkEntities.contains(prox)){
                        linkProximityMap.put(prox, this);
                    }
                }
            }
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
