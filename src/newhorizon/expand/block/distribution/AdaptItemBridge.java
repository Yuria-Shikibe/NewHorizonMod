package newhorizon.expand.block.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Queue;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.TargetPriority;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.meta.BlockGroup;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.util.func.MathUtil;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;
import static newhorizon.NewHorizon.DEBUGGING;

public class AdaptItemBridge extends AdaptBlock {
    public static final float
        LAYER_CORNER = Layer.block + 0.15f,
        LAYER_SHADOW = Layer.block + 0.1f,
        LAYER_BRIDGE = Layer.block + 0.2f,
        LAYER_ARROW = Layer.block + 0.3f,
        LAYER_ITEM = Layer.block + 0.4f,
        SHADOW_OFFSET = 2.5f;

    public float itemPerSecond = 3f;
    public int maxLength;
    public TextureRegion[] edgeRegion,arrowRegions;
    public TextureRegion[][] cornerRegion;
    public AdaptedConveyor conveyor;


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
        itemCapacity = 8;
        underBullets = true;
        drawTeamOverlay = false;
        priority = TargetPriority.transport;

        //config(Point2.class, (AdaptItemBridgeBuild tile, Point2 pos) -> tile.otherPos = Point2.pack(pos.x + tile.tileX(), pos.y + tile.tileY()));
        //config(Integer.class, (AdaptItemBridgeBuild tile, Integer status) -> tile.status = status);
    }

    @Override
    public void load() {
        super.load();
        edgeRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 32, 32, 1);
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1);
        cornerRegion = spiltQuad(SpriteUtil.splitRegionArray(Core.atlas.find(name + "-corner"), 38, 34));
    }

    public TextureRegion[][] spiltQuad(TextureRegion[] corner){
        TextureRegion[][] out = new TextureRegion[corner.length][3];
        for (int i = 0; i < corner.length; i++){
            TextureRegion region = corner[i];
            int x = region.getX();
            int y = region.getY();

            out[i][0] = new TextureRegion(region.texture, x + 1, y + 1, 8, 32);
            out[i][1] = new TextureRegion(region.texture, x + 11, y + 1, 16, 32);
            out[i][2] = new TextureRegion(region.texture, x + 29, y + 1, 8, 32);
        }
        return out;
    }

    public float framePeriod(){
        return 60f / itemPerSecond;
    }

    public class AdaptItemBridgeBuild extends AdaptBuilding{
        public Vec2
            tmp0 = new Vec2(), tmp1 = new Vec2(), tmp2 = new Vec2(), tmp3 = new Vec2(),
            tmp4 = new Vec2(), tmp5 = new Vec2(), tmp6 = new Vec2(), tmp7 = new Vec2();

        public float progress;
        //status flag. -1 for disabled. 0 for receive, 1 for send, 2 for transfer
        public static final int
            STATUS_DISABLED = 0,
            STATUS_SEND = 1,
            STATUS_RECEIVE = 2,
            STATUS_TRANSFER = 3;

        //status for bridge
        public int status = STATUS_DISABLED;

        //packed pos for other build
        public int senderPos = -1;
        public int receiverPos = -1;
        public AdaptItemBridgeBuild sender;
        public AdaptItemBridgeBuild receiver;

        //used to control the bridge
        //last input angle. rotDeg() if none.

        public float bridgeAng;
        //bridge length.
        public float bridgeDst;
        //bridge segments as conveyors.
        public int bridgeSeg;

        public float cornerInAng;
        public float cornerOutAng;

        //used to control the rotation center of the corner
        //the rotation center between bridges.
        public Vec2 bridgeRotCenter = new Vec2();
        public float bridgeRotDst;
        public float bridgeRotAng;
        public float bridgeRotClip;

        public Queue<ItemStacker> bridgeItems;


        @Override
        public void created() {
            super.created();
            bridgeItems = new Queue<>();
            cornerInAng = cornerOutAng = rotdeg();
            reconnectBridge(null, null);
        }


        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
        }


        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other != this && other instanceof AdaptItemBridgeBuild){
                AdaptItemBridgeBuild build = (AdaptItemBridgeBuild)other;

                if (build.sender != null) build.sender.receiver = null;
                if (receiver != null) receiver.sender = null;

                if (receiver != null) {
                    receiver.reconnectBridge(null, receiver.receiver);
                }
                if (build.sender != null) {
                    build.sender.reconnectBridge(build.sender.sender, null);
                }

                //calculate bridge
                bridgeAng = MathUtil.angle(this, build);
                bridgeDst = MathUtil.dst(this, build) - tilesize;
                bridgeSeg = Mathf.round(bridgeDst / tilesize);

                //calculate corner for both.
                cornerOutAng = build.cornerInAng = MathUtil.angle(this, build);

                //calculate rot center for both.
                bridgeRotAng = MathUtil.angleBisector(cornerInAng + 180, cornerOutAng);
                bridgeRotClip = Angles.angleDist(cornerOutAng, bridgeRotAng);

                build.bridgeRotAng = MathUtil.angleBisector(build.cornerInAng + 180, build.cornerOutAng);
                build.bridgeRotClip = Angles.angleDist(build.cornerOutAng, build.bridgeRotAng);

                Log.info(build.bridgeRotAng);

                if (Angles.within(bridgeRotClip, 90, 1f)){
                    bridgeRotCenter.set(this);
                    bridgeRotAng = cornerOutAng;
                    bridgeRotDst = 4f;

                    build.bridgeRotCenter.set(build);
                    build.bridgeRotAng = build.cornerOutAng;
                    build.bridgeRotDst = 4f;
                }else {
                    float dst1 = tilesize/2f / Mathf.cosDeg(bridgeRotClip);
                    bridgeRotCenter.trns(bridgeRotAng, dst1).add(this);
                    bridgeRotDst = dst1;

                    float dst2 = tilesize/2f / Math.abs(Mathf.cosDeg(build.bridgeRotClip));
                    build.bridgeRotCenter.trns(build.bridgeRotAng, dst2).add(build);
                    build.bridgeRotDst = dst2;
                }

                reconnectBridge(sender, build);
                build.reconnectBridge(this, build.receiver);

                return true;
            }
            return true;
        }

        public void reconnectBridge(AdaptItemBridgeBuild sender, AdaptItemBridgeBuild receiver){

            this.sender = sender;
            senderPos = sender == null? -1: sender.pos();
            this.receiver = receiver;
            receiverPos = receiver == null? -1: receiver.pos();
            updateBridgeStatus();
            updateCorner();
        }

        public void updateBridgeStatus(){
            if (sender == null && receiver == null) status = STATUS_DISABLED;
            if (sender == null && receiver != null) status = STATUS_SEND;
            if (sender != null && receiver == null) status = STATUS_RECEIVE;
            if (sender != null && receiver != null) status = STATUS_TRANSFER;
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

            if (status == STATUS_SEND || status == STATUS_TRANSFER){
                if (bridgeSeg > 0){
                    if(progress >= framePeriod() && bridgeCanInsert()){
                        bridgeQueueItem(stackItem(), stackCount());
                        progress %= framePeriod();
                        items.remove(stackItem(), stackCount());
                    }
                    updateBridge();
                }else {
                    int max = stackCount();
                    if(progress >= framePeriod() && moveForwardStack(receiver) == max){
                        progress %= framePeriod();
                    }
                }
            }

            if (status == STATUS_RECEIVE){
                int max = stackCount();
                if(progress >= framePeriod() && moveForwardStack(front()) == max){
                    progress %= framePeriod();
                }
            }
        }

        public int moveForwardStack(Building other) {
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
            if (first.progress > bridgeSeg * framePeriod() && receiver.acceptBridge(first.itemStack.item)){
                receiver.handleBridge(first.itemStack.item, first.itemStack.amount);
                bridgeItems.removeFirst();
            }
        }

        public void bridgeQueueItem(Item item, int count){
            bridgeItems.addLast(new ItemStacker(item, count));
        }

        public boolean bridgeCanInsert(){
            if (bridgeSeg > 0 && bridgeItems.isEmpty()) return true;
            return bridgeItems.last().progress > framePeriod() && bridgeItems.size < bridgeSeg;
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
            return status != STATUS_DISABLED;
        }

        @Override
        public void remove() {
            if (status != STATUS_DISABLED){
                if (sender != null){
                    sender.reconnectBridge(sender.sender, null);
                }
                if (receiver != null){
                    receiver.reconnectBridge(null, receiver.receiver);
                }
            }

            super.remove();
        }

        @Override
        public void draw() {
            if (conveyor == null) return;
            drawCorner();
            drawBridge();
        }

        public void updateCorner(){
            if (Angles.within(bridgeRotClip, 90, 1f) || status == STATUS_DISABLED){
                tmp0.set(-tilesize/2f, tilesize/2f).rotate(cornerInAng).add(this);
                tmp1.set(-tilesize/2f, -tilesize/2f).rotate(cornerInAng).add(this);
                tmp2.set(-tilesize/4f, tilesize/2f).rotate(cornerInAng).add(this);
                tmp3.set(-tilesize/4f, -tilesize/2f).rotate(cornerInAng).add(this);
                tmp4.set(tilesize/4f, tilesize/2f).rotate(cornerInAng).add(this);
                tmp5.set(tilesize/4f, -tilesize/2f).rotate(cornerInAng).add(this);
                tmp6.set(tilesize/2f, tilesize/2f).rotate(cornerInAng).add(this);
                tmp7.set(tilesize/2f, -tilesize/2f).rotate(cornerInAng).add(this);

                return;
            }

            float dst = bridgeRotDst * Mathf.sinDeg(bridgeRotClip);
            float ang = (90 - bridgeRotClip)/2f;
            float len1 = dst - tilesize/2f, len2 = dst + tilesize/2f;
            float dst1 = len1 / Mathf.cosDeg(ang), dst2 = len2 / Mathf.cosDeg(ang);

            Tmp.v1.trns(cornerInAng + 180, tilesize/2f);
            Tmp.v2.trns(cornerOutAng, tilesize/2f);

            float ang1 = Tmp.v1.angle();
            float ang2 = Tmp.v2.angle();

            boolean realInvert = MathUtil.angelDistance(ang1, ang2) > 180f;
            if (realInvert){
                tmp0.trns(bridgeRotAng + 180f - ang * 2, len1).add(bridgeRotCenter);
                tmp1.trns(bridgeRotAng + 180f - ang * 2, len2).add(bridgeRotCenter);
                tmp2.trns(bridgeRotAng + 180f - ang, dst1).add(bridgeRotCenter);
                tmp3.trns(bridgeRotAng + 180f - ang, dst2).add(bridgeRotCenter);
                tmp4.trns(bridgeRotAng + 180f + ang, dst1).add(bridgeRotCenter);
                tmp5.trns(bridgeRotAng + 180f + ang, dst2).add(bridgeRotCenter);
                tmp6.trns(bridgeRotAng + 180f + ang * 2, len1).add(bridgeRotCenter);
                tmp7.trns(bridgeRotAng + 180f + ang * 2, len2).add(bridgeRotCenter);
            }else {
                tmp0.trns(bridgeRotAng + 180f + ang * 2, len1).add(bridgeRotCenter);
                tmp1.trns(bridgeRotAng + 180f + ang * 2, len2).add(bridgeRotCenter);
                tmp2.trns(bridgeRotAng + 180f + ang, dst1).add(bridgeRotCenter);
                tmp3.trns(bridgeRotAng + 180f + ang, dst2).add(bridgeRotCenter);
                tmp4.trns(bridgeRotAng + 180f - ang, dst1).add(bridgeRotCenter);
                tmp5.trns(bridgeRotAng + 180f - ang, dst2).add(bridgeRotCenter);
                tmp6.trns(bridgeRotAng + 180f - ang * 2, len1).add(bridgeRotCenter);
                tmp7.trns(bridgeRotAng + 180f - ang * 2, len2).add(bridgeRotCenter);
            }

            if (receiver != null){
                Log.info(MathUtil.dst(Tmp.v1.set(tmp0).lerp(tmp1, 0.5f), Tmp.v2.set(this)));
                Log.info(MathUtil.dst(Tmp.v1.set(tmp6).lerp(tmp7, 0.5f), Tmp.v2.set(this)));
                Log.info(MathUtil.dst(this, receiver));
                Log.info(bridgeDst);
            }
        }

        public void drawBridge(){
            if (status == STATUS_DISABLED) return;
            if (receiver == null) return;

            int index = (int)((((Time.time) % conveyor.framePeriod()) / conveyor.framePeriod()) * 16);
            float dstLen = bridgeDst / bridgeSeg;

            for (int i = 0; i < bridgeSeg; i++){
                float segDst = dstLen * i + tilesize/2f + dstLen/2f;
                int idx = i % 2;
                Tmp.v1.trns(bridgeAng, segDst).add(this);
                Draw.z(LAYER_BRIDGE);
                Draw.rect(edgeRegion[idx], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, bridgeAng);
                Draw.z(LAYER_ARROW);
                Draw.rect(arrowRegions[index], Tmp.v1.x, Tmp.v1.y, dstLen, tilesize, bridgeAng);
            }

            Tmp.v1.trns(bridgeAng, tilesize / 2f + bridgeDst / 2f).add(this);
            Draw.z(LAYER_SHADOW);
            Draw.color(Pal.shadow);
            Fill.rect(Tmp.v1.x - SHADOW_OFFSET, Tmp.v1.y - SHADOW_OFFSET, bridgeDst, tilesize, bridgeAng);
            Draw.color();

            for (int i = 0; i < bridgeItems.size; i++){
                ItemStacker stack = bridgeItems.get(i);
                float segDst = ((stack.progress / framePeriod()) / bridgeSeg) * bridgeDst + tilesize/2f;
                Tmp.v1.trns(bridgeAng, segDst).add(this);
                Draw.z(LAYER_ITEM);
                Draw.rect(stack.itemStack.item.fullIcon, Tmp.v1.x, Tmp.v1.y, itemSize, itemSize);
                DrawFunc.drawText(stack.itemStack.amount + "", Tmp.v1.x, Tmp.v1.y, 1f);
            }
        }

        public void drawCorner(){
            int index = (int)((((Time.time) % conveyor.framePeriod()) / conveyor.framePeriod()) * 16);
            if (status == STATUS_DISABLED || status == STATUS_SEND){
                quad(cornerRegion[index][0], tmp0, tmp1, tmp3, tmp2);
                quad(cornerRegion[index][1], tmp2, tmp3, tmp5, tmp4);
                quad(cornerRegion[index][2], tmp4, tmp5, tmp7, tmp6);

                Draw.z(LAYER_SHADOW);
                Draw.color(Pal.shadow);
                Fill.quad(
                    tmp2.x, tmp2.y,
                    tmp3.x, tmp3.y,
                    tmp5.x - SHADOW_OFFSET, tmp5.y - SHADOW_OFFSET,
                    tmp4.x - SHADOW_OFFSET, tmp4.y - SHADOW_OFFSET);
                Fill.quad(
                    tmp4.x - SHADOW_OFFSET, tmp4.y - SHADOW_OFFSET,
                    tmp5.x - SHADOW_OFFSET, tmp5.y - SHADOW_OFFSET,
                    tmp7.x - SHADOW_OFFSET, tmp7.y - SHADOW_OFFSET,
                    tmp6.x - SHADOW_OFFSET, tmp6.y - SHADOW_OFFSET);

                if (status == STATUS_DISABLED){
                    Fill.quad(
                        tmp6.x, tmp6.y,
                        tmp7.x, tmp7.y,
                        tmp7.x - SHADOW_OFFSET, tmp7.y - SHADOW_OFFSET,
                        tmp6.x - SHADOW_OFFSET, tmp6.y - SHADOW_OFFSET);
                }
                Draw.color();
            }
            if (status == STATUS_RECEIVE){
                quad(cornerRegion[index + 16][0], tmp0, tmp1, tmp3, tmp2);
                quad(cornerRegion[index + 16][1], tmp2, tmp3, tmp5, tmp4);
                quad(cornerRegion[index + 16][2], tmp4, tmp5, tmp7, tmp6);

                Draw.z(LAYER_SHADOW);
                Draw.color(Pal.shadow);
                Fill.quad(
                    tmp0.x - SHADOW_OFFSET, tmp0.y - SHADOW_OFFSET,
                    tmp1.x - SHADOW_OFFSET, tmp1.y - SHADOW_OFFSET,
                    tmp3.x - SHADOW_OFFSET, tmp3.y - SHADOW_OFFSET,
                    tmp2.x - SHADOW_OFFSET, tmp2.y - SHADOW_OFFSET);
                Fill.quad(
                    tmp2.x - SHADOW_OFFSET, tmp2.y - SHADOW_OFFSET,
                    tmp3.x - SHADOW_OFFSET, tmp3.y - SHADOW_OFFSET,
                    tmp5.x, tmp5.y,
                    tmp4.x, tmp4.y);
                Draw.color();
            }
            if (status == STATUS_TRANSFER){
                quad(cornerRegion[index + 32][0], tmp0, tmp1, tmp3, tmp2);
                quad(cornerRegion[index + 32][1], tmp2, tmp3, tmp5, tmp4);
                quad(cornerRegion[index + 32][2], tmp4, tmp5, tmp7, tmp6);

                Draw.z(LAYER_SHADOW);
                Draw.color(Pal.shadow);
                quadShadow(tmp0, tmp1, tmp3, tmp2);
                quadShadow(tmp2, tmp3, tmp5, tmp4);
                quadShadow(tmp4, tmp5, tmp7, tmp6);
                Draw.color();
            }

            if(stackItem() != null){
                Draw.z(Layer.blockUnder - 0.2f);
                Tmp.v1.trns(cornerInAng + 180, tilesize/2f).add(this);
                Tmp.v2.trns(cornerOutAng, tilesize/2f).add(this);
                Tmp.v3.set(Tmp.v1).lerp(Tmp.v2, cornerFrac());
                Draw.z(LAYER_ITEM);
                Draw.rect(stackItem().fullIcon, Tmp.v3.x, Tmp.v3.y, itemSize, itemSize);
                DrawFunc.drawText(stackCount() + "", Tmp.v3.x, Tmp.v3.y, 1f);
            }
        }

        public float cornerFrac(){
            return progress / framePeriod();
        }

        public void quad(TextureRegion region, Vec2 v0, Vec2 v1, Vec2 v2, Vec2 v3){
            Draw.z(LAYER_CORNER);
            Fill.quad(region, v0.x, v0.y, v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
        }

        public void quadShadow(Vec2 v0, Vec2 v1, Vec2 v2, Vec2 v3){
            Fill.quad(
                v0.x - SHADOW_OFFSET, v0.y - SHADOW_OFFSET, v1.x - SHADOW_OFFSET, v1.y - SHADOW_OFFSET,
                v2.x - SHADOW_OFFSET, v2.y - SHADOW_OFFSET, v3.x - SHADOW_OFFSET, v3.y - SHADOW_OFFSET);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();


            if (!DEBUGGING) return;
            DrawFunc.drawText("status: " + status, x, y - 2);
            DrawFunc.drawText("inAngle: " + cornerInAng, x, y - 6);
            DrawFunc.drawText("outAngle: " + cornerOutAng, x, y - 10);
            DrawFunc.drawText("rotAngle: " + bridgeRotAng, x, y - 14);

            Draw.color(Pal.accent, Pal.heal, 0f);
            Fill.circle(tmp0.x, tmp0.y, 0.5f);
            Fill.circle(tmp1.x, tmp1.y, 0.5f);
            Draw.color(Pal.accent, Pal.heal, 1/3f);
            Fill.circle(tmp2.x, tmp2.y, 0.5f);
            Fill.circle(tmp3.x, tmp3.y, 0.5f);
            Draw.color(Pal.accent, Pal.heal, 2/3f);
            Fill.circle(tmp4.x, tmp4.y, 0.5f);
            Fill.circle(tmp5.x, tmp5.y, 0.5f);
            Draw.color(Pal.accent, Pal.heal, 1f);
            Fill.circle(tmp6.x, tmp6.y, 0.5f);
            Fill.circle(tmp7.x, tmp7.y, 0.5f);

            if (receiver != null){
                Draw.color(Pal.techBlue);
                Fill.circle(receiver.x, receiver.y, 2);
                Fill.circle(receiver.bridgeRotCenter.x, receiver.bridgeRotCenter.y, 2);
                Draw.color(Pal.remove);
                Lines.lineAngle(receiver.x, receiver.y, receiver.cornerInAng + 180, 8);
                Draw.color(Pal.techBlue);
                Lines.lineAngle(receiver.x, receiver.y, receiver.cornerOutAng, 8);
            }
            if (sender != null){
                Draw.color(Pal.remove);
                Fill.circle(sender.x, sender.y, 2);
                Fill.circle(sender.bridgeRotCenter.x, sender.bridgeRotCenter.y, 2);
                Draw.color(Pal.remove);
                Lines.lineAngle(sender.x, sender.y, sender.cornerInAng + 180, 8);
                Draw.color(Pal.techBlue);
                Lines.lineAngle(sender.x, sender.y, sender.cornerOutAng, 8);
            }
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
