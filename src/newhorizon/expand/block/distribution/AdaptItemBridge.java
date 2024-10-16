package newhorizon.expand.block.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.TargetPriority;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Edges;
import mindustry.world.meta.BlockGroup;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.util.MathUtil;
import newhorizon.util.graphic.DrawUtil;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;

public class AdaptItemBridge extends AdaptBlock {
    public float itemPerSecond = 3f;
    public int maxLength;
    public TextureRegion[] edgeRegion;
    public TextureRegion headRegion;
    public AdaptConveyor conveyor;

    public AdaptItemBridge(String name) {
        super(name);


        solid = false;
        unloadable = false;
        noUpdateDisabled = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;


        rotate = true;
        update = true;
        group = BlockGroup.transportation;
        hasItems = true;
        //actually the capacity doesnt really counts so much
        itemCapacity = 8;
        underBullets = true;
        drawTeamOverlay = false;
        priority = TargetPriority.transport;



        config(Point2.class, (AdaptItemBridgeBuild tile, Point2 pos) -> tile.otherPos = Point2.pack(pos.x + tile.tileX(), pos.y + tile.tileY()));
        config(Integer.class, (AdaptItemBridgeBuild tile, Integer status) -> tile.status = status);
    }

    @Override
    public void load() {
        super.load();
        edgeRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 32, 32, 1);
        headRegion = Core.atlas.find(name + "-head");
    }

    public float framePeriod(){
        return 60f / itemPerSecond;
    }

    public class AdaptItemBridgeBuild extends AdaptBuilding{
        public float progress;
        //status flag. -1 for disabled. 0 for receive, 1 for send
        public static final int DISABLED_STATUS = -1;
        public static final int RECEIVE_STATUS = 0;
        public static final int SEND_STATUS = 1;

        public float bridgeDst;
        public int bridgeSeg;

        public Queue<ItemStacker> bridgeItems;

        //packed pos for other build
        public int otherPos = -1;
        public AdaptItemBridgeBuild other;

        public int status = DISABLED_STATUS;

        @Override
        public void created() {
            super.created();
            bridgeItems = new Queue<>();
        }


        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
        }


        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other != this && other instanceof AdaptItemBridgeBuild){
                AdaptItemBridgeBuild build = (AdaptItemBridgeBuild)other;
                setReceiver(build);
                build.setSender(this);

                Tmp.v1.trns(rotdeg(), tilesize/2f).add(this);
                Tmp.v2.trns(other.rotdeg(), -tilesize/2f).add(other);

                bridgeDst = MathUtil.dst(Tmp.v1, Tmp.v2);
                bridgeSeg = Mathf.round(bridgeDst / tilesize);

                return true;
            }
            return true;
        }

        public Item stackItem(){
            return items.first();
        }

        public int stackCount(){
            if (items.empty())return 0;
            return items.get(stackItem());
        }

        @Override
        public void updateTile() {
            if (stackItem() != null && progress < framePeriod()){
                progress += edelta();
            }

            if (status == SEND_STATUS){
                if(progress >= framePeriod() && bridgeCanInsert()){
                    bridgeQueueItem(stackItem(), stackCount());
                    progress %= framePeriod();
                    items.remove(stackItem(), stackCount());
                }
                updateBridge();
            }

            if (status == RECEIVE_STATUS){
                int max = stackCount();
                if(progress >= framePeriod() && moveForwardStack() == max){
                    progress %= framePeriod();
                }
            }
        }

        public int moveForwardStack() {
            Building other = front();
            int max = stackCount();
            if (stackItem() != null && other != null && other.team == team) {
                if (other.acceptItem(this, stackItem())){
                    if (other instanceof AdaptItemBridge.AdaptItemBridgeBuild){
                        AdaptItemBridge.AdaptItemBridgeBuild bridge = ((AdaptItemBridge.AdaptItemBridgeBuild)other);
                        items.remove(stackItem(), bridge.handleStacker(this, stackItem(), stackCount()));
                    }else if (other.items == null){
                        while (stackCount() > 0){
                            other.handleItem(this, stackItem());
                            items.remove(stackItem(), 1);
                        }
                    }else {
                        int maxAccepted = other.getMaximumAccepted(stackItem()) - other.items.get(stackItem());
                        int maxSend = Math.min(maxAccepted, stackCount());
                        other.items.add(stackItem(), maxSend - 1);
                        other.handleItem(this, stackItem());
                        items.remove(stackItem(), maxSend);
                    }
                }
            }
            return max - stackCount();
        }

        public void updateBridge(){
            if (bridgeItems.isEmpty())return;

            for (int i = 0; i < bridgeItems.size; i++){
                float maxProgressLimit = (bridgeSeg - i) * framePeriod();
                float progress = bridgeItems.get(i).progress;
                if (progress < maxProgressLimit){
                    bridgeItems.get(i).addProgress(edelta());
                }
            }

            ItemStacker first = bridgeItems.first();
            if (first.progress > bridgeSeg * framePeriod() && other.acceptBridge(first.itemStack.item)){
                other.handleBridge(first.itemStack.item, first.itemStack.amount);
                bridgeItems.removeFirst();
            }
        }

        public void bridgeQueueItem(Item item, int count){
            bridgeItems.addLast(new ItemStacker(item, count));
        }

        public void bridgeDequeueItem(){
            if (other == null) return;
            bridgeItems.removeFirst();
        }

        public boolean bridgeCanDequeue(){
            if (other == null) return false;
            return other.acceptItem(this, bridgeItems.first().itemStack.item);
        }

        public boolean bridgeCanInsert(){
            if (bridgeSeg > 0 && bridgeItems.isEmpty()) return true;
            return bridgeItems.last().progress > framePeriod();
        }

        public boolean acceptBridge(Item item){
            return items.empty();
        }

        public void handleBridge(Item item, int count) {
            progress = 0f;
            items.add(item, count);
        }
        @Override
        public boolean acceptItem(Building source, Item item) {
            //check if connected and input&output pos
            if (!connected()) return false;
            if (source != back()) return false;

            //check build status

            return (stackItem() == null || stackItem() == item) && items.get(item) < itemCapacity;

        }

        @Override
        public void handleItem(Building source, Item item) {
            super.handleItem(source, item);
        }

        public int handleStacker(Building source, Item item, int count){
            int max = count;

            int maxAccepted = getMaximumAccepted(item) - items.get(item);
            int maxSend = Math.min(maxAccepted, count);
            items.add(item, maxSend);
            count -= maxSend;

            return max - count;
        }

        public boolean connected(){
            return otherPos != -1 && status != -1;
        }

        public void resetBuild(){
            if (other != null) {
                other.otherPos = other.status = DISABLED_STATUS;
                other.other = null;
            }
            otherPos = status = DISABLED_STATUS;
            other = null;
        }


        public void setReceiver(AdaptItemBridgeBuild receiver){
            resetBuild();
            receiver.resetBuild();

            setOther(receiver);
            receiver.setOther(this);

            receiver.status = RECEIVE_STATUS;
            status = SEND_STATUS;
        }

        public void setSender(AdaptItemBridgeBuild sender){
            resetBuild();
            sender.resetBuild();

            setOther(sender);
            sender.setOther(this);

            sender.status = SEND_STATUS;
            status = RECEIVE_STATUS;
        }

        public void setOther(AdaptItemBridgeBuild build){
            otherPos = build.pos();
            other = build;
        }

        @Override
        public void remove() {
            if (!(status == DISABLED_STATUS)){
                other.resetBuild();
                resetBuild();
            }

            super.remove();
        }

        @Override
        public void draw() {
            if (conveyor == null) return;
            int index = (int)((((Time.time) % conveyor.framePeriod()) / conveyor.framePeriod()) * 16);
            Draw.z(Layer.blockUnder - 0.1f);
            Draw.rect(conveyor.baseRegions[0], x, y, rotdeg());
            Draw.rect(conveyor.arrowRegions[index + 16], x, y, rotdeg());
            Draw.rect(conveyor.arrowRegions[index], x, y, rotdeg());
            float z = Draw.z();
            Draw.z(z + 0.0001f);
            Draw.rect(conveyor.edgeRegions[0], x, y, rotdeg());

            if(stackItem() != null){
                Draw.z(Layer.block + 0.08f);
                Tmp.v1.set(Geometry.d4x(rotation + 2) * tilesize / 2f, Geometry.d4y(rotation + 2) * tilesize / 2f)
                    .lerp(Geometry.d4x(rotation) * tilesize / 2f, Geometry.d4y(rotation) * tilesize / 2f, progress / framePeriod());
                Draw.rect(stackItem().fullIcon, x + Tmp.v1.x, y + Tmp.v1.y, itemSize, itemSize);
                DrawUtil.drawText(stackCount() + "", x + Tmp.v1.x, y + Tmp.v1.y, 1f);
            }

            if (other != null){
                drawBridge();
            }else {
                Draw.rect(headRegion, x, y, 90);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
        }

        public void drawBridge(){
            if (other == null) return;
            if (status != SEND_STATUS) return;

            int index = (int)((((Time.time) % conveyor.framePeriod()) / conveyor.framePeriod()) * 16);
            float dstLen = bridgeDst / bridgeSeg;

            Tmp.v1.trns(rotdeg(), tilesize/2f).add(this);
            Tmp.v2.trns(other.rotdeg(), -tilesize/2f).add(other);
            float angle = MathUtil.angle(Tmp.v1, Tmp.v2);
            for (int i = 0; i < bridgeSeg; i++){
                float segDst = dstLen * i + dstLen/2f;
                int idx = i % 2;
                Tmp.v1.trns(angle, segDst).add(this);
                Tmp.v2.trns(rotdeg(), tilesize/2f);
                Tmp.v1.add(Tmp.v2);
                Draw.z(Layer.block + 0.1f);
                Draw.rect(edgeRegion[idx], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, angle);
                Draw.rect(conveyor.arrowRegions[index], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, angle);
                Draw.rect(conveyor.arrowRegions[index + 16], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, angle);
            }
            for (int i = 0; i < bridgeItems.size; i++){
                ItemStacker stack = bridgeItems.get(i);
                float segDst = ((stack.progress / framePeriod()) / bridgeSeg) * bridgeDst;
                Tmp.v1.trns(angle, segDst).add(this);
                Tmp.v2.trns(rotdeg(), tilesize/2f);
                Tmp.v1.add(Tmp.v2);
                Draw.z(Layer.block + 0.11f);
                Draw.rect(stack.itemStack.item.fullIcon, Tmp.v1.x, Tmp.v1.y, itemSize, itemSize);
                DrawUtil.drawText(stack.itemStack.amount + "", Tmp.v1.x, Tmp.v1.y, 1f);
            }
            Draw.z(Layer.block + 0.09f);
            Draw.color(Pal.shadow);
            Tmp.v1.trns(angle, 4f).add(this);
            Lines.stroke(tilesize);
            Lines.lineAngle(Tmp.v1.x - tilesize/4f, Tmp.v1.y - tilesize/4f, angle, bridgeDst - tilesize);
            Draw.color();
        }
    }

    public class ItemStacker{
        public ItemStack itemStack;
        public float progress;

        public ItemStacker(Item item, int count){
            itemStack = new ItemStack(item, count);
            progress = 0;
        }

        public void addProgress(float prog){
            progress += prog;
        }
    }
}
