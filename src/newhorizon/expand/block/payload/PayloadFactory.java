package newhorizon.expand.block.payload;

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
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.input.Placement;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.Constructor;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumePayloadDynamic;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.expand.block.consumer.NHConsumeItemDynamic;
import newhorizon.expand.block.consumer.NHConsumeLiquidDynamic;
import newhorizon.expand.block.consumer.NHConsumePayloadDynamic;
import newhorizon.expand.block.consumer.NHConsumeShowStat;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.production.factory.MultiBlock;
import newhorizon.expand.block.production.factory.MultiBlockEntity;

import static mindustry.Vars.*;
import static mindustry.Vars.state;

public class PayloadFactory extends Constructor implements MultiBlock {
    public Seq<Point2> linkPos = new Seq<>();
    public IntSeq linkSize = new IntSeq();

    public boolean canMirror = true;
    public int[] rotations = {0, 1, 2, 3, 0, 1, 2, 3};

    public Seq<Point2> acceptPos = new Seq<>();
    public Seq<Point2> targetPos = new Seq<>();

    public TextureRegion rotRegion;

    public PayloadFactory(String name) {
        super(name);

        acceptsPayload = true;
        regionRotated1 = -1;
        liquidCapacity = 200;

        for (Consume cons: consumers){
            if (cons instanceof ConsumeItemDynamic){
                removeConsumer(cons);
            }
        }
        consume(new NHConsumeItemDynamic((PayloadFactoryBuild e) -> {
            if(e.recipeCost() != null) return e.recipeCost().itemReq.copy().toArray(ItemStack.class);
            return ItemStack.empty;
        }));
        consume(new NHConsumeLiquidDynamic((PayloadFactoryBuild e) -> {
            if(e.recipeCost() != null) return e.recipeCost().liquidReq.copy().toArray(LiquidStack.class);
            return LiquidStack.empty;
        }));
        consume(new NHConsumePayloadDynamic((PayloadFactoryBuild e) -> {
            if(e.recipeCost() != null) return e.recipeCost().payloadReq;
            return PayloadStack.list();
        }));
        consume(new NHConsumeShowStat(
                (PayloadFactoryBuild e) -> {
                    if(e.recipeCost() != null) return e.recipeCost().itemReq.copy().toArray(ItemStack.class);
                    return ItemStack.empty;
                },
                (PayloadFactoryBuild e) -> {
                    if(e.recipeCost() != null) return e.recipeCost().liquidReq.copy().toArray(LiquidStack.class);
                    return LiquidStack.empty;
                },
                (PayloadFactoryBuild e) -> {
                    if(e.recipeCost() != null) return e.recipeCost().payloadReq.copy().toArray(PayloadStack.class);
                    return PayloadStack.with();
                }
        ));
    }

    @Override
    public void load() {
        super.load();
        rotRegion = Core.atlas.find(name + "-rot");
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.size);
        stats.add(Stat.size, "@x@", getMaxSize(size, 0).x, getMaxSize(size, 0).y);
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
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> {
            if (rotation % 2 == 0) {
                return Math.abs(point.x - other.x) <= getMaxSize(size, rotation).x;
            }else{
                return Math.abs(point.y - other.y) <= getMaxSize(size, rotation).y;
            }
        });
    }

    @Override
    public Seq<Point2> linkBlockPos() {
        return linkPos;
    }

    @Override
    public IntSeq linkBlockSize() {
        return linkSize;
    }

    @Override
    public Block mirrorBlock() {
        return this;
    }

    @Override
    public boolean isMirror() {
        return false;
    }

    public class PayloadFactoryBuild extends ConstructorBuild implements MultiBlockEntity {
        public boolean linkCreated = false;
        public Seq<Building> linkEntities;
        //ordered seq, target-source pair
        public Seq<Building[]> linkProximityMap;
        public int dumpIndex = 0;
        public int dumpPayloadIndex = 0;
        public Tile teamPos, statusPos;

        public PayloadSeq payloads = new PayloadSeq();

        public ModuleBlock.ModuleCost recipeCost(){
            if(recipe() != null && ModuleBlock.moduleCosts.get(recipe()) != null) return ModuleBlock.moduleCosts.get(recipe());
            return null;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);

            var recipe = recipe();
            if(recipe != null){
                Drawf.shadow(x, y, recipe.size * tilesize * 1.5f, progress / recipe.buildTime);
                Draw.draw(Layer.blockBuilding, () -> {
                    Draw.color(Pal.accent);

                    for(TextureRegion region : recipe.getGeneratedIcons()){
                        Shaders.blockbuild.region = region;
                        Shaders.blockbuild.time = time;
                        Shaders.blockbuild.progress = progress / recipe.buildTime;

                        Draw.rect(region, x, y, recipe.rotate ? rotdeg() : 0);
                        Draw.flush();
                    }

                    Draw.color();
                });
                Draw.z(Layer.blockBuilding + 1);
                Draw.color(Pal.accent, heat);

                Lines.lineAngleCenter(x + Mathf.sin(time, 10f, Vars.tilesize / 2f * recipe.size + 1f), y, 90, recipe.size * Vars.tilesize + 1f);

                Draw.reset();
            }

            drawPayload();

            Draw.z(Layer.blockBuilding + 1.1f);
            Draw.rect(topRegion, x, y);
            Draw.rect(rotRegion, x, y, rotdeg());
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return recipeCost() != null
                    && recipeCost().itemReq.contains(stack -> stack.item == item)
                    && items.get(item) < recipeCost().itemReq.find(stack -> stack.item == item).amount * 2;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return recipeCost() != null
                    && recipeCost().liquidReq.contains(stack -> stack.liquid == liquid)
                    && liquids.get(liquid) < liquidCapacity;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            boolean accept = false;
            for (Point2 p: acceptPos){
                Point2 realPos = calculateRotatedPosition(p, size, 1, rotation);
                Building accepter = world.build(tileX() + realPos.x, tileY() + realPos.y);
                if (source == accepter) {
                    accept = true;
                    break;
                }
            }

            return accept && recipeCost() != null
                    && recipeCost().payloadReq.contains(stack -> stack.item == payload.content())
                    && payloads.get(payload.content()) < recipeCost().payloadReq.find(stack -> stack.item == payload.content()).amount * 2;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            yeetPayload(payload);
        }

        public void yeetPayload(Payload payload){
            payloads.add(payload.content(), 1);
            float rot = payload.angleTo(this);
            Fx.payloadDeposit.at(payload.x(), payload.y(), rot, new UnitAssembler.YeetData(new Vec2(x, y), payload.content()));
        }

        @Override
        public PayloadSeq getPayloads() {
            return payloads;
        }

        public void moveOutPayload(){
            if(payload == null) return;

            updatePayload();

            Vec2 dest = Tmp.v1.trns(rotdeg(), size * tilesize/2f);

            payRotation = Angles.moveToward(payRotation, rotdeg(), payloadRotateSpeed * delta());
            payVector.approach(dest, payloadSpeed * delta());

            Building[] pair = validFrontAccepter();
            Building target = pair == null ? null : pair[0];
            boolean canDump = target == null || !target.tile.solid();
            boolean canMove = target != null && (target.block.outputsPayload || target.block.acceptsPayload);

            if(canDump && !canMove){
                pushOutput(payload, 1f - (payVector.dst(dest) / (size * tilesize / 2f)));
            }

            if(payVector.within(dest, 0.001f)){
                payVector.clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);

                if(canMove){
                    if(movePayload(payload)){
                        payload = null;
                    }
                }else if(canDump){
                    dumpPayload();
                }
            }
        }

        @Override
        public boolean movePayload(Payload todump) {
            if (validFrontAccepter() == null) return false;
            Building target = validFrontAccepter()[0];
            Building source = validFrontAccepter()[1];

            if (target != null && target.team == this.team && target.acceptPayload(source, todump)) {
                target.handlePayload(this, todump);
                incrementDumpPayloadIndex();
                return true;
            } else {
                return false;
            }
        }

        //return a target-source pair
        public Building[] validFrontAccepter(){
            int dump = dumpPayloadIndex;
            for (int i = 0; i < targetPos.size; i++){
                int idx = (i + dump) % targetPos.size;
                Point2 p = targetPos.get(idx);

                Point2 realPos = calculateRotatedPosition(p, size, 1, rotation);
                Building target = world.build(tileX() + realPos.x, tileY() + realPos.y);
                Building[] tsPair = linkProximityMap.find(pair -> pair[0] == target);
                if (tsPair == null) continue;
                Building source = tsPair[1];

                if (target != null && source != null && (target.block.outputsPayload || target.block.acceptsPayload)){
                    return new Building[]{target, source};
                }
            }
            return null;
        }

        //not necessary?
        @Override
        public boolean dumpPayload(Payload todump) {
            if (this.proximity.size != 0) {
                int dump = dumpIndex;
                for (int i = 0; i < linkProximityMap.size; ++i) {
                    int idx = (i + dump) % linkProximityMap.size;
                    Building[] pair = linkProximityMap.get(idx);
                    Building target = pair[0];
                    Building source = pair[1];
                    if (target.acceptPayload(source, todump)) {
                        target.handlePayload(source, todump);
                        incrementDumpIndex(linkProximityMap.size);
                        return true;
                    }
                    incrementDumpIndex(linkProximityMap.size);
                }

            }
            return false;
        }

        //todo duplicated code, this is awful
        @Override
        public void created() {
            super.created();
            linkProximityMap = new Seq<>();
        }

        @Override
        public void updateTile() {
            if(isPayload()) return;

            if (!linkCreated){
                linkEntities = setLinkBuild(this, block, tile, team, size, rotation);
                linkCreated = true;
                updateLinkProximity();
            }

            //uh so period check to avoid invalid link entity
            if (timer(0, 300)){
                boolean linkValid = true;
                for (Tile t: getLinkTiles(tile, size, rotation)){
                    if (!(t.build instanceof LinkBlock.LinkBuild lb && lb.linkBuild == this && lb.isValid())){
                        linkValid = false;
                        break;
                    }
                }
                if (!linkValid){
                    linkEntities.each(Building::kill);
                    kill();
                }
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
        public void dumpLiquid(Liquid liquid, float scaling, int outputDir) {
            int dump = this.cdump;
            if (liquids.get(liquid) <= 0.0001f) return;
            if (!net.client() && state.isCampaign() && team == state.rules.defaultTeam) liquid.unlock();
            for (int i = 0; i < linkProximityMap.size; i++) {
                incrementDumpIndex(linkProximityMap.size);
                int idx = (i + dump) % linkProximityMap.size;
                Building[] pair = linkProximityMap.get(idx);
                Building target = pair[0];
                Building source = pair[1];
                if (outputDir != -1 && (outputDir + rotation) % 4 != relativeTo(target)) continue;
                target = target.getLiquidDestination(source, liquid);
                if (target != null && target.block.hasLiquids && canDumpLiquid(target, liquid) && target.liquids != null) {
                    float ofract = target.liquids.get(liquid) / target.block.liquidCapacity;
                    float fract = liquids.get(liquid) / block.liquidCapacity;
                    if (ofract < fract) transferLiquid(target, (fract - ofract) * block.liquidCapacity / scaling, liquid);
                }
            }
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

        public void incrementDumpPayloadIndex() {
            dumpPayloadIndex = ((dumpPayloadIndex + 1) % targetPos.size);
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
        public void onRemoved() {
            createPlaceholder(tile, size);
        }

        @Override
        public boolean canPickup() {
            return false;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (validFrontAccepter() != null){
                Drawf.selected(validFrontAccepter()[0], Pal.accent);
                Drawf.selected(validFrontAccepter()[1], Pal.regen);
            }

            for (Point2 p: acceptPos){
                Point2 realPos = calculateRotatedPosition(p, size, 1, rotation);
                Tile receive = world.tile(tileX() + realPos.x, tileY() + realPos.y);
                if (receive != null){
                    Drawf.selected(tileX() + realPos.x, tileY() + realPos.y, Blocks.copperWall, Pal.remove);
                }
            }
        }

        @Override
        public void drawTeam() {
            teamPos = world.tile(tileX() + teamOverlayPos(size, rotation).x, tileY() + teamOverlayPos(size, rotation).y);
            if (teamPos != null){
                Draw.color(team.color);
                Draw.rect("block-border", teamPos.worldx(), teamPos.worldy());
                Draw.color();
            }
        }

        @Override
        public void drawStatus() {
            statusPos = world.tile(tileX() + statusOverlayPos(size, rotation).x, tileY() + statusOverlayPos(size, rotation).y);
            if (block.enableDrawStatus && block.consumers.length > 0) {
                float multiplier = block.size > 1 ? 1 : 0.64F;
                Draw.z(Layer.power + 1);
                Draw.color(Pal.gray);
                Fill.square(statusPos.worldx(), statusPos.worldy(), 2.5F * multiplier, 45);
                Draw.color(status().color);
                Fill.square(statusPos.worldx(), statusPos.worldy(), 1.5F * multiplier, 45);
                Draw.color();
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            payloads.write(write);
        }

        @Override
        public void read(Reads read) {
            super.read(read);
            payloads = new PayloadSeq();
            payloads.read(read);
        }
    }
}
