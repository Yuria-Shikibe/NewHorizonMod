package newhorizon.expand.block.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
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
    public TextureRegion[] edgeRegion, headRegion ,arrowRegions;
    public AdaptConveyor conveyor;

    private final Vec2 tmp0 = new Vec2(), tmp1 = new Vec2(), tmp2 = new Vec2(), tmp3 = new Vec2();

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
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1, false);
        headRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-head0"), 8, 36);
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

        //used to control the bridge
        public float bridgeAng;
        public float bridgeDst;
        public int bridgeSeg;

        //used to control the rotation center of the corner
        public Vec2 bridgeRotCenter = new Vec2();
        public float bridgeRotDst;
        public float bridgeRotAng;
        public float bridgeRotClip;

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

                bridgeAng = MathUtil.angle(this, other);
                bridgeDst = MathUtil.dst(this, other) - tilesize;
                bridgeSeg = Mathf.round(bridgeDst / tilesize);

                bridgeRotAng = (rotdeg() + 180 + bridgeAng)/2f;
                bridgeRotClip = Angles.angleDist(bridgeAng, bridgeRotAng);
                if (Angles.within(bridgeRotClip, 90, 1f)){
                    bridgeRotCenter.set(this);
                    bridgeRotAng = rotdeg();
                    bridgeRotDst = 4f;
                }else {
                    float dst = tilesize/2f / Mathf.cosDeg(bridgeRotClip);
                    bridgeRotCenter.trns(bridgeRotAng, dst).add(this);
                    bridgeRotDst = dst;
                }
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
            if (other != null){
                drawCorner();
                drawBridge();
            }
            /*
            if (other != null){
                drawBridge();
                if (status == SEND_STATUS){
                    //Draw.rect(headRegion[0], x, y, rotdeg());
                    Draw.rect(arrowRegions[index], x, y, rotdeg());
                }
                if (status == RECEIVE_STATUS){
                    //Draw.rect(headRegion[1], x, y, rotdeg());
                    Draw.rect(arrowRegions[index + 16], x, y, rotdeg());
                }
            }else {
                //Draw.rect(headRegion[0], x, y, rotdeg());
                Draw.rect(arrowRegions[index], x, y, rotdeg());
            }

             */
            float z = Draw.z();
            //Draw.z(z + 0.0001f);
            //Draw.rect(conveyor.edgeRegions[0], x, y, rotdeg());

            if(stackItem() != null){
                Draw.z(Layer.block + 0.11f);
                Tmp.v1.set(Geometry.d4x(rotation + 2) * tilesize / 2f, Geometry.d4y(rotation + 2) * tilesize / 2f)
                    .lerp(Geometry.d4x(rotation) * tilesize / 2f, Geometry.d4y(rotation) * tilesize / 2f, progress / framePeriod());
                Draw.rect(stackItem().fullIcon, x + Tmp.v1.x, y + Tmp.v1.y, itemSize, itemSize);
                DrawUtil.drawText(stackCount() + "", x + Tmp.v1.x, y + Tmp.v1.y, 1f);
            }
            Draw.z(Layer.block + 0.2f);
            //Lines.stroke(1f);
            //Lines.lineAngle(x, y, rotdeg() + 180, 20);
            //Lines.lineAngle(x, y, bridgeAng, 20);
            //Lines.lineAngle(x, y, bridgeRotAng, bridgeRotDst);
        }

        public void drawCorner(){
            float len = bridgeRotDst * Mathf.sinDeg(bridgeRotClip);
            float ang = (90 - bridgeRotClip)/2f;
            float start = bridgeRotAng + 180 - ang * 2;
            for (int i = 0; i < 4; i++){
                tmp0.trns(start + i * ang, len - 4.5f).add(bridgeRotCenter);
                tmp1.trns(start + i * ang, len + 4.5f).add(bridgeRotCenter);
                tmp2.trns(start + (i + 1) * ang, len + 4.5f).add(bridgeRotCenter);
                tmp3.trns(start + (i + 1) * ang, len - 4.5f).add(bridgeRotCenter);
                Fill.quad(
                    headRegion[i],
                    tmp0.x, tmp0.y,
                    tmp1.x, tmp1.y,
                    tmp2.x, tmp2.y,
                    tmp3.x, tmp3.y
                );
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

            for (int i = 0; i < bridgeSeg; i++){
                float segDst = dstLen * i + dstLen;
                int idx = i % 2;
                Tmp.v1.trns(bridgeAng, segDst).add(this);
                Draw.z(Layer.block + 0.1f);
                Draw.rect(edgeRegion[idx], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, bridgeAng);
                Draw.rect(arrowRegions[index + 32], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, bridgeAng);
            }
            for (int i = 0; i < bridgeItems.size; i++){
                ItemStacker stack = bridgeItems.get(i);
                float segDst = ((stack.progress / framePeriod()) / bridgeSeg) * bridgeDst;
                Tmp.v1.trns(bridgeAng, segDst).add(this);
                Draw.z(Layer.block + 0.11f);
                Draw.rect(stack.itemStack.item.fullIcon, Tmp.v1.x, Tmp.v1.y, itemSize, itemSize);
                DrawUtil.drawText(stack.itemStack.amount + "", Tmp.v1.x, Tmp.v1.y, 1f);
            }
            //Draw.z(Layer.block + 0.09f);
            //Draw.color(Pal.shadow);
            //Tmp.v1.trns(bridgeAng, 4f).add(this);
            //Lines.stroke(tilesize);
            //Lines.lineAngle(Tmp.v1.x - tilesize/4f, Tmp.v1.y - tilesize/4f, bridgeAng, bridgeDst - tilesize);
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
