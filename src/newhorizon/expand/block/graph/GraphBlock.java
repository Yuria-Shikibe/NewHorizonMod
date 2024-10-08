package newhorizon.expand.block.graph;

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
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Edges;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHItems;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.util.graphic.DrawUtil;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class GraphBlock extends AdaptBlock {
    public TextureRegion[] edgeRegions, baseRegions, arrowRegions;
    public float itemPerSecond = 6f;
    public GraphBlock(String name) {
        super(name);

        rotate = true;
        update = true;
        group = BlockGroup.transportation;
        hasItems = true;
        //actually the capacity doesnt really counts so much
        itemCapacity = 100;
        conveyorPlacement = true;
        underBullets = true;
        drawTeamOverlay = false;
        priority = TargetPriority.transport;

        ambientSound = Sounds.conveyor;
        ambientSoundVolume = 0.004f;

        requirements(Category.distribution, with(NHItems.zeta, 10));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, itemPerSecond, StatUnit.itemsSecond);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress", (GraphBlockBuild b) -> new Bar(() -> b.recDir + "", () -> Pal.techBlue, () -> 1f));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);

    }

    //frame period in a circle. eg 5 ips -> 12 ticks a circle
    public float framePeriod(){
        return 60f / itemPerSecond;
    }

    @Override
    public void load() {
        super.load();
        edgeRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 36, 36, 1, false);
        baseRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-base"), 32, 32, 3, false);
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1, false);
    }

    public class GraphBlockBuild extends AdaptBuilding{
        //conveyed item data
        //progress, 0 - framePeriod().
        public float progress, extraProgress;

        public boolean canLoad;
        public boolean canSend;
        public Item stackItem;
        public int stackCount;
        public @Nullable Building next;
        public @Nullable GraphBlockBuild nextConveyor;

        //draw part var
        public int recDir = 0, recConvDir;
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

        @Override
        public void updateTile() {
            super.updateTile();
            //check for empty item and apply new items
            //reset anything for empty item then skip update.
            //else set current stack items
            if (!items.any()){
                stackItem = null;
                stackCount = 0;
                progress = 0;
                canLoad = true;
                canSend = false;

                return;
            }else{
                stackItem = items.first();
                stackCount = items.get(stackItem);

                canLoad = false;
            }

            //transport progress, 0 -> framePeriod.
            //storage extra progress for next conveyor. extra progress reset to 0 in next tick.
            //when progress up to framePeriod, ready to send to next conveyor
            if (progress >= framePeriod()){
                extraProgress = progress - framePeriod();
                progress = progress % framePeriod();
                canSend = true;
            }

            if (!canSend && stackItem != null){
                progress += edelta();
            }

            //send item to next build. also add extra progress to next conveyor.
            if (canSend && next != null && next.acceptItem(this, stackItem) && timer(timerDump, dumpTime)){
                //for something with fake item modules such as item void
                if (next.items == null){
                    while (moveForward(stackItem) && stackCount > 0){
                        items.remove(stackItem, 1);
                        stackCount -= 1;
                    }
                    if (stackCount == 0){
                        stackItem = null;
                        progress = 0;
                        canLoad = true;
                        canSend = false;

                        recConvDir = -1;
                    }
                }else {
                    int maxAccepted = next.getMaximumAccepted(stackItem) - next.items.get(stackItem);
                    //for dir reason so this need to leave extra 1 for handleItem()
                    int maxSend = Math.min(maxAccepted, stackCount) - 1;
                    //handle maxSend + 1
                    next.handleItem(this, stackItem);
                    next.items.add(stackItem, maxSend);
                    //remove maxSend + 1
                    items.remove(stackItem, maxSend + 1);
                    stackCount -= (maxSend + 1);
                    if (maxSend + 1 >= stackCount){
                        stackItem = null;
                        stackCount = 0;
                        progress = 0;

                        recConvDir = -1;

                        canLoad = true;
                        canSend = false;
                    }
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return (stackItem == null || stackItem == item)
                //&& canLoad
                && items.get(item) < itemCapacity
                && !(source.block.rotate && next == source) && Edges.getFacingEdge(source.tile, tile) != null && Math.abs(Edges.getFacingEdge(source.tile, tile).relativeTo(tile.x, tile.y) - rotation) != 2;
        }


        @Override
        public void handleItem(Building source, Item item){
            stackItem = item;

            recDir = relativeToEdge(source.tile);
            if (source instanceof GraphBlockBuild){
                recConvDir = recDir;
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
            if (next instanceof GraphBlockBuild){
                nextConveyor = (GraphBlockBuild) front();
            }

            if (right() instanceof GraphBlockBuild && (right().rotation + 3) % 4 == rotation){dIdx += 1;dInput += 1;}
            if (back() instanceof GraphBlockBuild && back().rotation == rotation){dIdx += 1 << 1;dInput += 1 << 1;}
            if (left() instanceof GraphBlockBuild && (left().rotation + 1) % 4 == rotation){dIdx += 1 << 2;dInput += 1 << 2;}

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
        public void onProximityAdded() {
            super.onProximityAdded();
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
        }

        @Override
        public void draw() {
            int index = (int)((((Time.time) % 10f) / 10f) * 16);
            int pulse = (int) ((Time.time/4f) % 4f);
            if (pulse == 3) pulse = 1;

            Tmp.c1.set(Pal.lightishGray).lerp(team.color, 0.25f);
            Draw.blend(Blending.additive);
            Draw.color(Tmp.c1);
            Draw.rect(baseRegions[dIdx + pulse * 4], x, y, rotdeg() + dRot);
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

            int r = recConvDir == -1? recDir: recConvDir;

            if(stackItem != null){
                Draw.z(z + 0.1f);
                Tmp.v1.set(Geometry.d4x(r) * tilesize / 2f, Geometry.d4y(r) * tilesize / 2f)
                    .lerp(Geometry.d4x(rotation) * tilesize / 2f, Geometry.d4y(rotation) * tilesize / 2f,
                        Mathf.clamp(progress / framePeriod()));
                Draw.rect(stackItem.fullIcon, x + Tmp.v1.x, y + Tmp.v1.y, itemSize, itemSize);
                DrawUtil.drawText(stackCount + "", x + Tmp.v1.x, y + Tmp.v1.y, 1f);
            }

            Draw.reset();

        }

        @Override
        public void drawTeam() {
            super.drawTeam();
        }

        @Override
        public void drawSelect() {

        }
    }
}
