package newhorizon.expand.block.production.factory;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;
import newhorizon.util.graphic.DrawFunc;

import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.Vars.content;

public class AdaptCrafter extends GenericCrafter implements MultiBlock{
    public Seq<Point2> linkPos = new Seq<>();
    public IntSeq linkSize = new IntSeq();

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

    @Override
    public void placeBegan(Tile tile, Block previous) {
        createPlaceholder(tile, size);
    }

    @Override
    public Seq<Point2> getLinkBlockPos() {
        return linkPos;
    }

    @Override
    public IntSeq getLinkBlockSize() {
        return linkSize;
    }

    public class AdaptCrafterBuild extends GenericCrafterBuild implements MultiBlockEntity{

        public boolean linkCreated = false;
        public Seq<Building> linkEntities;
        //ordered seq, target-source pair
        public Seq<Building[]> linkProximityMap;
        public int dumpIndex = 0;

        @Override
        public void created() {
            super.created();
            linkProximityMap = new Seq<>();
        }

        @Override
        public void updateTile() {
            if (!linkCreated){
                linkEntities = setLinkBuild(this, tile, team, size, rotation);
                linkCreated = true;
                updateLinkProximity();
            }
            super.updateTile();
        }

        @Override
        public boolean dump(Item todump) {
            if (!block.hasItems || items.total() == 0 || linkProximityMap.size == 0 || (todump != null && !items.has(todump))) return false;
            int dump = dumpIndex;
            for (int i = 0; i < linkProximityMap.size; i++) {
                int idx = (i + dump) % linkProximityMap.size;
                Building[] pair = linkProximityMap.get(idx);
                Building target = pair[0];
                Building source = pair[1];

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
            int dump = dumpIndex;
            for (int i = 0; i < linkProximityMap.size; i++) {
                incrementDumpIndex(linkProximityMap.size);
                int idx = (i + dump) % linkProximityMap.size;
                Building[] pair = linkProximityMap.get(idx);
                Building target = pair[0];
                Building source = pair[1];
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
                            if (checkValidPair(linkProx, link)){
                                linkProximityMap.add(new Building[]{linkProx, link});
                            }
                        }
                    }
                }

                //add self entity's proximity
                for (Building prox : proximity){
                    if (!linkEntities.contains(prox)){
                        if (checkValidPair(prox, this)) {
                            linkProximityMap.add(new Building[]{prox, this});
                        }
                    }
                }
            }
        }

        public boolean checkValidPair(Building target, Building source){
            for (Building[] pair : linkProximityMap){
                Building pairTarget = pair[0];
                Building pairSource = pair[1];

                if (target == pairTarget){
                    if (target.relativeTo(pairSource) == target.relativeTo(source)){
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateLinkProximity();
        }

        @Override
        public void remove() {
            removeLink(tile, size, rotation);
            createPlaceholder(tile, size);
            super.remove();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            AtomicInteger i = new AtomicInteger(0);

            linkProximityMap.each(pair -> {
                if (dumpIndex == i.get()){
                    Draw.color(Pal.techBlue);
                }else {
                    Draw.color(Pal.remove);
                }

                Draw.alpha(0.5f);
                Draw.z(Layer.block + 3f);

                Building target = pair[0];
                Building source = pair[1];

                Lines.line(target.x, target.y, source.x, source.y);
                Fill.square(target.x, target.y, target.hitSize()/2f - 2f);

                DrawFunc.drawText(i + "", target.x, target.y);
                i.getAndIncrement();
            });
            DrawFunc.drawText(linkProximityMap.size + "", x, y);
        }
    }
}
