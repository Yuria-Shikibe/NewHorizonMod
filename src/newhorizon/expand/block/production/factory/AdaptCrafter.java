package newhorizon.expand.block.production.factory;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadConveyor;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.expand.block.inner.LinkBlock;

import static mindustry.Vars.*;

public class AdaptCrafter extends GenericCrafter implements MultiBlock {
    public ObjectSet<UnlockableContent> payloadFilter = new ObjectSet<>();
    public Seq<Point2> linkPos = new Seq<>();
    public IntSeq linkSize = new IntSeq();

    public boolean canMirror = true;
    public int[] rotations = {0, 1, 2, 3, 0, 1, 2, 3};

    public float powerProduction = 0f;
    public int payloadCapacity = 10;

    public PayloadStack[] outputPayloads;

    public AdaptCrafter(String name) {
        super(name);

        hasItems = true;
        hasLiquids = true;
        hasPower = true;

        acceptsPayload = true;
        outputsPayload = true;

        rotate = true;
        rotateDraw = true;
        quickRotate = false;
        allowDiagonal = false;
    }

    @Override
    public void init() {
        super.init();
        if (powerProduction > 0f){
            consumesPower = false;
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        if (hasPower && outputsPower && powerProduction > 0f) {
            removeBar("power");
            addBar("power", (AdaptCrafterBuild entity) -> new Bar(() ->
                    Core.bundle.format("bar.poweroutput",
                            Strings.fixed(entity.getPowerProduction() * 60 * entity.timeScale(), 1)),
                    () -> Pal.powerBar,
                    () -> entity.warmup));
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.size);
        stats.add(Stat.size, "@x@", getMaxSize(size, 0).x, getMaxSize(size, 0).y);
        if (powerProduction > 0) {
            stats.remove(Stat.powerUse);
            stats.add(Stat.basePowerGeneration, powerProduction * 60f, StatUnit.powerSecond);
        }
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
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        Placement.calculateNodes(points, this, rotation, (point, other) -> {
            if (rotation % 2 == 0) {
                return Math.abs(point.x - other.x) == getMaxSize(size, rotation).x;
            } else {
                return Math.abs(point.y - other.y) == getMaxSize(size, rotation).y;
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

    @Override
    public void loadIcon() {
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-icon", name);
    }

    @Override
    public TextureRegion[] getGeneratedIcons() {
        return super.getGeneratedIcons();
    }

    @Override
    public void flipRotation(BuildPlan req, boolean x) {
        if (canMirror) {
            if (mirrorBlock() != null) {
                req.rotation = rotations[req.rotation + (x ? 0 : 4)];
            }
        } else {
            super.flipRotation(req, x);
        }
    }

    public class AdaptCrafterBuild extends GenericCrafterBuild implements MultiBlockEntity {
        public boolean linkCreated = false;
        public Seq<Building> linkEntities;
        //ordered seq, target-source pair
        public Seq<Building[]> linkProximityMap;
        public int dumpIndex = 0;

        public Tile teamPos, statusPos;
        public PayloadSeq payloads = new PayloadSeq();

        @Override
        public PayloadSeq getPayloads() {
            return payloads;
        }

        @Override
        public boolean shouldConsume() {
            if (outputItems != null) {
                for (var output : outputItems) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return powerProduction > 0;
                    }
                }
            }
            if (outputPayloads != null) {
                for (var output : outputPayloads) {
                    if (getPayloads().get(output.item) + output.amount > payloadCapacity) {
                        return powerProduction > 0;
                    }
                }
            }
            if (outputLiquids != null && !ignoreLiquidFullness) {
                boolean allFull = true;
                for (var output : outputLiquids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        //if there's still space left, it's not full for all liquids
                        allFull = false;
                    }
                }
                //if there is no space left for any liquid, it can't reproduce
                if (allFull) {
                    return false;
                }
            }
            return enabled;
        }

        @Override
        public float getPowerProduction() {
            return powerProduction * warmup * efficiency;
        }

        @Override
        public void created() {
            super.created();
            linkProximityMap = new Seq<>();

            if (instantBuild || (!state.rules.editor && state.rules.instantBuild && state.rules.infiniteResources)) {
                linkEntities = setLinkBuild(this, block, tile, team, size, rotation);
                linkCreated = true;
                updateLinkProximity();
            }
        }

        @Override
        public void updateTile() {
            if (isPayload()) return;

            if (!linkCreated) {
                linkEntities = setLinkBuild(this, block, tile, team, size, rotation);
                linkCreated = true;
                updateLinkProximity();
            }

            //uh so period check to avoid invalid link entity
            if (timer(0, 600)) {
                boolean linkValid = true;
                for (Tile t : getLinkTiles(tile, size, rotation)) {
                    if (!(t.build instanceof LinkBlock.LinkBuild lb && lb.linkBuild == this && lb.isValid())) {
                        linkValid = false;
                        break;
                    }
                }
                if (!linkValid) {
                    linkEntities.each(Building::kill);
                    kill();
                }
            }
            super.updateTile();
        }

        @Override
        public void craft() {
            super.craft();
            if(outputPayloads != null){
                for(PayloadStack output : outputPayloads){
                    payloads.add(output.item, output.amount);
                }
            }
        }

        public void incrementDumpIndex(int prox) {
            dumpIndex = ((dumpIndex + 1) % prox);
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            payloads.add(payload.content(), 1);
            Fx.payloadDeposit.at(payload.x(), payload.y(), payload.angleTo(this), new UnitAssembler.YeetData(new Vec2(x, y), payload.content()));
        }

        @Override
        public boolean dump(Item todump) {
            if (!block.hasItems || items.total() == 0 || linkProximityMap.size == 0 || (todump != null && !items.has(todump)))
                return false;
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
        public boolean dumpPayload(Payload todump) {
            int dump = dumpIndex;
            for (int i = 0; i < linkProximityMap.size; ++i) {
                int idx = (i + dump) % linkProximityMap.size;
                Building[] pair = linkProximityMap.get(idx);
                Building target = pair[0];
                Building source = pair[1];
                if (todump != null && getPayloads().get(todump.content()) > 0 && target.acceptPayload(source, todump)) {
                    target.handlePayload(this, todump);
                    getPayloads().remove(todump.content(), 1);
                    if (target instanceof PayloadConveyor.PayloadConveyorBuild) {
                        Fx.payloadDeposit.at(x, y, this.angleTo(target), new UnitAssembler.YeetData(new Vec2(target.x, target.y), todump.content()));
                    }
                    incrementDumpIndex(linkProximityMap.size);
                    return true;
                }
                incrementDumpIndex(linkProximityMap.size);
            }
            return false;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return payloadFilter.contains(payload.content()) && getPayloads().get(payload.content()) < payloadCapacity;
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
                    if (ofract < fract)
                        transferLiquid(target, (fract - ofract) * block.liquidCapacity / scaling, liquid);
                }
            }
        }

        @Override
        public void dumpOutputs() {
            boolean timer = timer(timerDump, dumpTime / timeScale);
            if(outputItems != null && timer) {
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }

            if(outputPayloads != null && timer){
                for(PayloadStack output : outputPayloads){
                    BuildPayload payload = new BuildPayload((Block) output.item, team);
                    payload.set(x, y, rotdeg());
                    dumpPayload(payload);
                }
            }

            if(outputLiquids != null){
                for(int i = 0; i < outputLiquids.length; i++){
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                    dumpLiquid(outputLiquids[i].liquid, 2f, dir);
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

        @Override
        public void updateLinkProximity() {
            if (linkEntities != null) {
                linkProximityMap.clear();
                //add link entity's proximity
                for (Building link : linkEntities) {
                    for (Building linkProx : link.proximity) {
                        if (linkProx != this && !linkEntities.contains(linkProx)) {
                            if (checkValidPair(linkProx, link)) {
                                linkProximityMap.add(new Building[]{linkProx, link});
                            }
                        }
                    }
                }

                //add self entity's proximity
                for (Building prox : proximity) {
                    if (!linkEntities.contains(prox)) {
                        if (checkValidPair(prox, this)) {
                            linkProximityMap.add(new Building[]{prox, this});
                        }
                    }
                }
            }
        }

        public boolean checkValidPair(Building target, Building source) {
            for (Building[] pair : linkProximityMap) {
                Building pairTarget = pair[0];
                Building pairSource = pair[1];

                if (target == pairTarget) {
                    if (target.relativeTo(pairSource) == target.relativeTo(source)) {
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
        public void drawTeam() {
            teamPos = world.tile(tileX() + teamOverlayPos(size, rotation).x, tileY() + teamOverlayPos(size, rotation).y);
            if (teamPos != null) {
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
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            payloads.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 2){
                payloads = new PayloadSeq();
                payloads.read(read);
            }
        }
    }
}
