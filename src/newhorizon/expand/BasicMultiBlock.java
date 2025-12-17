package newhorizon.expand;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadConveyor;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.meta.Stat;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.production.factory.MultiBlock;
import newhorizon.expand.block.production.factory.MultiBlockEntity;

import static mindustry.Vars.*;

public abstract class BasicMultiBlock extends Block implements MultiBlock {
    public Seq<Point2> linkPos = new Seq<>();
    public IntSeq linkSize = new IntSeq();
    public boolean canMirror = true;
    public int[] rotations = {0, 1, 2, 3, 0, 1, 2, 3};

    public final int checkTimer = timers++;

    public BasicMultiBlock(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.size);
        stats.add(Stat.size, "@x@", getMaxSize(size, 0).x, getMaxSize(size, 0).y);
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
    public void flipRotation(BuildPlan req, boolean x) {
        if (canMirror) {
            if (mirrorBlock() != null) {
                req.rotation = rotations[req.rotation + (x ? 0 : 4)];
            }
        } else {
            super.flipRotation(req, x);
        }
    }

    public class BasicMultiBuilding extends Building implements MultiBlockEntity {
        public boolean linkCreated = false;
        public Seq<Building> linkEntities;
        public Seq<Building[]> linkProximityMap;
        public Tile teamPos, statusPos;
        public int dumpIndex = 0;

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
            //uh so period check to avoid invalid link entity, todo need improvement
            if (timer(checkTimer, 600)) {
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

        public void incrementDumpIndex(int prox) {
            dumpIndex = ((dumpIndex + 1) % prox);
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
        public boolean canPickup() {
            return false;
        }

        @Override
        public void drawTeam() {
            teamPos = world.tile(tileX() + teamOverlayPos(size, rotation).x, tileY() + teamOverlayPos(size, rotation).y);
            if (teamPos != null) {
                Draw.z(Layer.blockOver);
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
    }
}
