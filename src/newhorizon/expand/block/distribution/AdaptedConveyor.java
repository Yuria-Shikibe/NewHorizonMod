package newhorizon.expand.block.distribution;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.content.NHItems;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class AdaptedConveyor extends AdaptBlock implements Autotiler {
    public static final float
        LAYER_CORNER = Layer.block + 0.15f,
        LAYER_SHADOW = Layer.block + 0.1f,
        LAYER_BRIDGE = Layer.block + 0.2f,
        LAYER_ARROW = Layer.block + 0.3f,
        LAYER_ITEM = Layer.block + 0.4f,
        SHADOW_OFFSET = 2.5f;

    public TextureRegion[] edgeRegions, baseRegions, arrowRegions;
    public boolean drawPulse = false;
    public float itemPerSecond = 3f;
    public AdaptedConveyor(String name) {
        super(name);

        rotate = true;
        update = true;
        group = BlockGroup.transportation;
        hasItems = true;
        //actually the capacity doesnt really counts so much
        itemCapacity = 10;
        conveyorPlacement = true;
        underBullets = true;
        drawTeamOverlay = false;
        priority = TargetPriority.transport;

        ambientSound = Sounds.conveyor;
        ambientSoundVolume = 0.004f;

        requirements(Category.distribution, with(NHItems.presstanium, 1));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, itemPerSecond * itemCapacity, StatUnit.itemsSecond);
    }

    @Override
    public void setBars() {
        super.setBars();
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
    }

    //frame period in a circle. eg 5 ips -> 12 ticks a circle
    public float framePeriod(){
        return 60f / itemPerSecond;
    }
    public int pulseFrame(){return (int) ((Time.time/4f) % 4f);}


    @Override
    public void load() {
        super.load();
        edgeRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 36, 36, 1);
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1);

        if (drawPulse){
            baseRegions = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("track-rail-pulse")), 32, 32, 3);
        }else {
            baseRegions = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("track-rail-base")), 32, 32, 0);
        }
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (otherblock.outputsItems() || (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock.hasItems))
            && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
    }

    public boolean blends(Building self, Building other){
        if (other == null) return false;
        return blends(self.tile(), self.rotation, other.tileX(), other.tileY(), other.rotation, other.block);
    }

    public class AdaptConveyorBuild extends AdaptBuilding implements StackTransport{
        public float progress, cooldown;

        public @Nullable Building next;

        //draw part var
        public int recDir = -1;
        public int dIdx;
        public int dRot;
        public byte dInput;
        public boolean drawPattern;

        public float getProgress(){
            return progress;
        }
        @Override
        public void created() {
            super.created();
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

            if (cooldown > 0f){
                cooldown -= edelta();
            }


            if(progress >= framePeriod() && cooldown <= 0){
                int max = stackCount();
                int moveCount = moveForwardStack();
                if (moveCount == max){
                    progress %= framePeriod();
                    recDir = -1;
                }
                cooldown += ((float) moveCount /itemCapacity) * framePeriod();
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

        @Override
        public boolean acceptItem(Building source, Item item) {
            return (stackItem() == null || stackItem() == item)
                //&& canLoad
                && items.get(item) < itemCapacity
                && !(source.block.rotate && next == source) && Edges.getFacingEdge(source.tile, tile) != null && Math.abs(Edges.getFacingEdge(source.tile, tile).relativeTo(tile.x, tile.y) - rotation) != 2;
        }


        @Override
        public void handleItem(Building source, Item item){
            if (recDir == -1){
                recDir = relativeToEdge(source.tile);
            }

            items.add(item, 1);
            noSleep();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            dIdx = 0;
            dRot = 0;
            dInput = 0;
            drawPattern = false;
            next = front();

            if (blends(this, right())){dIdx += 1;dInput += 1;}
            if (blends(this, back())){dIdx += 1 << 1;dInput += 1 << 1;}
            if (blends(this, left())){dIdx += 1 << 2;dInput += 1 << 2;}

            if (rotation % 2 == 0 && tileX() % 2 == 0) drawPattern = true;
            if (rotation % 2 == 1 && tileY() % 2 == 0) drawPattern = true;

            remapIdxRot();
        }

        public void remapIdxRot(){
            if (dIdx == 2) {dIdx = 0;}
            if (dIdx == 3) {dIdx = 2;}
            if (dIdx == 4) {dIdx = 1; dRot = 90;}
            if (dIdx == 5) {dIdx = 2; dRot = 90;}
            if (dIdx == 6) {dIdx = 2; dRot = 180;}
            if (dIdx == 7) {dIdx = 3;}

            if (dInput == 0) dInput += 1 << 1;
        }

        @Override
        public void draw() {

            if (drawPulse){
                drawPulse();
            }else {
                drawNormal();
            }

            int r = recDir == -1? rotation + 2: recDir;
            float prog = Mathf.clamp(progress / framePeriod());
            if(stackItem() != null){
                Draw.z(LAYER_ITEM);
                Tmp.v1.set(Geometry.d4x(r) * tilesize / 2f, Geometry.d4y(r) * tilesize / 2f)
                    .lerp(Geometry.d4x(rotation) * tilesize / 2f, Geometry.d4y(rotation) * tilesize / 2f, prog);
                Draw.rect(stackItem().fullIcon, x + Tmp.v1.x, y + Tmp.v1.y, itemSize, itemSize);
                DrawFunc.drawText(stackCount() + "", x + Tmp.v1.x, y + Tmp.v1.y, 1f);
            }
            Draw.reset();

        }

        private void drawPulse(){
            int index = (int)((((Time.time) % framePeriod()) / framePeriod()) * 16);
            int pulse = (int) ((Time.time/4f) % 4f);
            if (pulse == 3) pulse = 1;
            Tmp.c1.set(Pal.lightishGray).lerp(team.color, 0.25f);
            Draw.blend(Blending.additive);
            Draw.color(Tmp.c1);
            Draw.z(Layer.blockUnder - 0.1f);
            Draw.rect(baseRegions[dIdx + pulse * 4], x, y, rotdeg() + dRot);
            Draw.z(Layer.blockUnder - 0.01f);
            Draw.color(team.color);
            Draw.rect(arrowRegions[index], x, y, rotdeg());
            if ((dInput & 1 << 2) == 4) Draw.rect(arrowRegions[index + 16], x, y, rotdeg() - 90);
            if ((dInput & 1 << 1) == 2) Draw.rect(arrowRegions[index + 16], x, y, rotdeg());
            if ((dInput & 1) == 1) Draw.rect(arrowRegions[index + 16], x, y, rotdeg() + 90);
            Draw.blend();
            float z = Draw.z();
            Draw.color(team.color);
            Draw.z(z + 0.0001f);
            Draw.rect(edgeRegions[dIdx], x, y, rotdeg() + dRot);
            if (drawPattern){
                Draw.z(z + 0.0002f);
                Draw.rect(edgeRegions[dIdx + 4], x, y, rotdeg() + dRot);
            }
            Draw.z(z);
            Draw.color();
        }

        private void drawNormal(){
            int index = (int)((((Time.time) % framePeriod()) / framePeriod()) * 16);
            Draw.z(Layer.blockUnder - 0.1f);
            Draw.rect(baseRegions[0], x, y, rotdeg() + dRot);
            Draw.rect(arrowRegions[index], x, y, rotdeg());
            if ((dInput & 1 << 2) == 4) Draw.rect(arrowRegions[index + 16], x, y, rotdeg() - 90);
            if ((dInput & 1 << 1) == 2) Draw.rect(arrowRegions[index + 16], x, y, rotdeg());
            if ((dInput & 1) == 1) Draw.rect(arrowRegions[index + 16], x, y, rotdeg() + 90);
            float z = Draw.z();
            Draw.z(z + 0.0001f);
            Draw.rect(edgeRegions[dIdx], x, y, rotdeg() + dRot);
            if (drawPattern){
                Draw.z(z + 0.0002f);
                Draw.rect(edgeRegions[dIdx + 4], x, y, rotdeg() + dRot);
            }
            Draw.z(z);
        }

        @Override
        public void drawTeam() {
            super.drawTeam();
        }


    }
}
