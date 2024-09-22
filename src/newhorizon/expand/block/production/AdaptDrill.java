package newhorizon.expand.block.production;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.EnumSet;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import mindustry.world.meta.*;
import newhorizon.content.NHColor;
import newhorizon.util.func.NHFunc;

import static arc.Core.settings;
import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static newhorizon.util.func.NHFunc.globalEffectRand;
import static newhorizon.util.func.NHFunc.rand;

public class AdaptDrill extends Block {
    //output speed in items/sec
    public float mineSpeed = 5;
    //output count once
    public int mineCount = 2;

    public Seq<Item> mineOres = new Seq<>();

    //return variables for countOre
    protected final int maxOreTileReq = 10;
    protected @Nullable Item returnItem;
    protected int returnCount;
    protected final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();
    protected final Seq<Item> itemArray = new Seq<>();

    public TextureRegion baseRegion, topRegion, oreRegion;

    public float updateEffectChance = 0.02f;
    public Effect updateEffect = new Effect(30f, e -> {
        Rand rand = rand(e.id);
        Draw.color(e.color, Color.white, e.fout() * 0.66f);
        Draw.alpha(0.55f * e.fout() + 0.5f);
        Angles.randLenVectors(e.id, 2, 4f + e.finpow() * 17f, (x, y) -> {
            Fill.square(e.x + x, e.y + y, e.fout() * rand.random(2.5f, 4));
        });
    });;

    public AdaptDrill(String name) {
        super(name);
        update = true;
        solid = true;

        drawCracks = false;

        hasItems = true;
        hasLiquids = false;
        itemCapacity = 40;

        canOverdrive = false;

        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;

        group = BlockGroup.drills;
        flags = EnumSet.of(BlockFlag.drill);
    }

    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(name + "-bottom");
        topRegion = Core.atlas.find(name + "-top");
        oreRegion = Core.atlas.find(name + "-ore");
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("drillspeed", (AdaptDrillBuild e) ->
            new Bar(() -> e.getMineSpeed() + "/s (" + (e.boostMultiplier >= 0? "+": "")+ (int)((e.boostMultiplier - 1) * 100) + "%)", () -> Pal.ammo, () -> e.warmup));
    }

    public float mineInterval(){
        return (60f / mineSpeed) * mineCount;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.drillSpeed, mineSpeed, StatUnit.itemsSecond);
        stats.add(Stat.drillTier, StatValues.items(item -> mineOres.contains(item)));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(isMultiblock()){
            for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
                if(canMine(other)){
                    return true;
                }
            }
            return false;
        }else{
            return canMine(tile);
        }
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        if(!plan.worldContext) return;
        Tile tile = plan.tile();
        if(tile == null) return;

        countOre(tile);
        if(returnItem == null) return;

        Draw.color(returnItem.color);
        Draw.rect(oreRegion, plan.drawx(), plan.drawy());
        Draw.color();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Tile tile = world.tile(x, y);
        if(tile == null) return;

        countOre(tile);

        if(returnItem != null){
            String oreCountText = (returnCount < maxOreTileReq? "[sky](": "[heal](") + returnCount + "/" +  maxOreTileReq + ")[] " +mineSpeed * Mathf.clamp((float) returnCount /maxOreTileReq) + "/s";
            float width = drawPlaceText(oreCountText, x, y, valid);
            float dx = x * tilesize + offset - width/2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5, s = iconSmall / 4f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(returnItem.fullIcon, dx, dy - 1, s, s);
            Draw.reset();
            Draw.rect(returnItem.fullIcon, dx, dy, s, s);
        }else {
            Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && !mineOres.contains(t.drop()));
            Item item = to == null ? null : to.drop();
            if(item != null){
                drawPlaceText(Core.bundle.get("bar.drilltierreq"), x, y, valid);
            }else {
                drawPlaceText("No Ores", x, y, valid);
            }
        }
    }

    protected void countOre(Tile tile) {
        returnItem = null;
        returnCount = 0;

        oreCount.clear();
        itemArray.clear();

        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            if(canMine(other)){
                oreCount.increment(getDrop(other), 0, 1);
            }
        }

        for(Item item : oreCount.keys()){
            itemArray.add(item);
        }

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if(type != 0) return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(itemArray.size == 0){
            return;
        }

        returnItem = itemArray.peek();
        returnCount = Math.min(oreCount.get(itemArray.peek(), 0), maxOreTileReq);
    }

    protected boolean canMine(Tile tile){
        if(tile == null || tile.block().isStatic()) return false;
        Item drops = tile.drop();
        return drops != null && mineOres.contains(drops);
    }

    protected Item getDrop(Tile tile){
        return tile.drop();
    }

    public class AdaptDrillBuild extends Building{
        public float progress;

        //only for visual
        public float warmup;

        public int dominantItems;
        public Item dominantItem;

        public boolean coreSend = false;
        public float boostMultiplier = 1f;
        public Seq<DrillModule.DrillModuleBuild> modules = new Seq<>();

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            countOre(tile);
            dominantItem = returnItem;
            dominantItems = returnCount;

            updateDrillModule();
        }

        @Override
        public void updateTile() {
            tryDump();
            updateProgress();
            updateOutput();
            updateEffect();
        }

        private void updateOutput(){
            if (progress > mineInterval()){
                int outCount = (int) (progress / mineInterval()) * mineCount;
                for (int i = 0; i < outCount; i++){
                    if (dominantItem != null){
                        if (coreSend && core() != null && core().acceptItem(this, dominantItem)){
                            core().handleItem(this, dominantItem);
                        }else {
                            offload(dominantItem);
                        }
                    }
                }
                progress %= mineInterval();
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            if(dominantItem != null){
                float dx = x - size * tilesize/2f, dy = y + size * tilesize/2f, s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(dominantItem.fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(dominantItem.fullIcon, dx, dy, s, s);
            }

            Drawf.selected(this, Pal.accent);
            for (DrillModule.DrillModuleBuild module: modules){
                Drawf.selected(module, Pal.accent);
            }
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            if (efficiency > 0.001){
                if (items.total() < itemCapacity && dominantItem != null){
                    warmup = Mathf.lerp(warmup, efficiency, 0.003f);
                }else {
                    warmup = Mathf.lerp(warmup, 0, 0.01f);
                }
                drawT1();
            }
            Draw.z(Layer.blockOver - 4f);
            Draw.rect(topRegion, x, y);
            if(dominantItem != null){
                Draw.color(dominantItem.color);
                Draw.rect(oreRegion, x, y);
                Draw.color();
            }
        }
        private void drawT1(){
            float rad = 9.2f + Mathf.absin(8, 1);
            float base = (Time.time / 70f);
            Tmp.c1.set(NHColor.thurmixRed).a(warmup/1.1f);
            //Draw.z(Layer.effect);
            Draw.color(Tmp.c1);
            Lines.stroke(2f);
            for(int i = 0; i < 32; i++){
                Mathf.rand.setSeed(id + hashCode() + i);
                float fin = (Mathf.rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = Mathf.rand.random(360f) + ((Time.time * 2.2f) % 360f);
                float len = 12.5f * Interp.pow2.apply(fout);
                Lines.lineAngle(
                    x + Angles.trnsx(angle, len),
                    y + Angles.trnsy(angle, len),
                    angle, 6 * fin
                );
            }


            Tmp.c1.set(NHColor.thurmixRed).a(warmup/1.3f);
            Draw.color(Tmp.c1);
            Lines.stroke(2f);
            Lines.circle(x, y, rad);

            Draw.reset();
        }

        private void tryDump(){
            if(timer(timerDump, dumpTime)){
                if (dominantItem != null){
                    if (coreSend && items.has(dominantItem) && core() != null && core().acceptItem(this, dominantItem)){
                        items.remove(dominantItem, 1);
                        core().handleItem(this, dominantItem);
                    }else {
                        dump(items.has(dominantItem) ? dominantItem : null);
                    }
                }
            }
        }

        private void updateEffect(){
            if (!headless){
                if (warmup > 0.8f && efficiency > 0 && dominantItem != null && globalEffectRand.chance(updateEffectChance * boostMultiplier)){
                    updateEffect.at(x + globalEffectRand.range(size * 3.6f), y + globalEffectRand.range(size * 3.6f), dominantItem.color);
                }
            }
        }

        private void resetModule(){
            boostMultiplier = 1f;
            coreSend = false;
            modules.clear();
        }

        public void updateDrillModule(){
            resetModule();
            for (Building building: proximity){
                if (building instanceof DrillModule.DrillModuleBuild) {
                    DrillModule.DrillModuleBuild module = (DrillModule.DrillModuleBuild) building;
                    if (module.canApply(this)){
                        modules.add(module);
                        module.apply(this);
                    }
                }
            }
        }

        private void updateProgress(){
            if (items.total() < itemCapacity){
                progress += edelta() * Mathf.clamp((float) dominantItems/maxOreTileReq) * boostMultiplier;
            }
        }

        private float getMineSpeed(){
            return Mathf.clamp((float) dominantItems/maxOreTileReq) * boostMultiplier * mineSpeed;
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.firstItem) return dominantItem;
            return super.senseObject(sensor);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
        }
    }
}
