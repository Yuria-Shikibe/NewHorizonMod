package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Iconc;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.*;
import newhorizon.expand.block.consumer.PowerConsumer;
import newhorizon.util.ui.BarExtend;

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
    //yeah i want to make whitelist only so
    protected final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();
    protected final Seq<Item> itemArray = new Seq<>();

    public TextureRegion baseRegion, topRegion, oreRegion;
    public float powerConsBase;

    public float updateEffectChance = 0.02f;
    public Effect updateEffect = Fx.none;

    public AdaptDrill(String name) {
        super(name);
        size = 4;

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

        consumePower(AdaptDrillBuild::getPowerCons);
    }

    @SuppressWarnings("unchecked")
    public <T extends Building> void consumePower(Floatf<T> usage){
        consume(new PowerConsumer((Floatf<Building>) usage));
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
        barMap.clear();
        addBar("health", e -> new BarExtend(Core.bundle.format("nh.bar.health", e.health(), health, Strings.autoFixed(e.healthf() * 100, 0)), Pal.health, e::healthf, Iconc.add + "").blink(Color.white));
        addBar("power", (AdaptDrillBuild e) -> new BarExtend(
            Core.bundle.format("nh.bar.power-detail", Strings.autoFixed(e.getPowerCons() * 60f, 0), Strings.autoFixed((e.powerConsMul), 1), e.powerConsExtra),
            Pal.powerBar,
            () -> (Mathf.zero(consPower.requestedPower(e)) && e.power.graph.getPowerProduced() + e.power.graph.getBatteryStored() > 0f) ? 1f : e.power.status,
            Iconc.power + ""));
        addBar("outputOre", (AdaptDrillBuild e) -> new BarExtend(e::getMineInfo, e::getMineColor, () -> 1f, Iconc.settings + ""));
        addBar("drillSpeed", (AdaptDrillBuild e) -> new BarExtend(
            () -> Core.bundle.format("nh.bar.drill-speed", Strings.autoFixed(e.getMineSpeed(), 2), Strings.autoFixed((e.boostMul - 1) * 100, 1), e.boostFinalMul),
            () -> Pal.ammo,
            () -> e.warmup,
            Iconc.production + ""));
    }

    public float mineInterval(){
        return (60f / mineSpeed) * mineCount;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.drillSpeed, mineSpeed, StatUnit.itemsSecond);
        stats.add(Stat.drillTier, StatValues.items(item -> mineOres.contains(item)));;
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
        public Item convertItem;

        public boolean coreSend = false;
        public float boostMul = 1f;
        public float boostFinalMul = 1f;

        //(base * multiplier) + extra
        public float powerConsMul = 1f;
        public float powerConsExtra = 0f;
        public Seq<DrillModule.DrillModuleBuild> modules = new Seq<>();

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            countOre(tile);
            dominantItem = returnItem;
            dominantItems = returnCount;

            updateDrillModule();
        }

        public Item outputItem(){
            return dominantItem != null? convertItem != null? convertItem: dominantItem: null;
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
                    if (outputItem() != null){
                        if (coreSend && core() != null && core().acceptItem(this, outputItem())){
                            core().handleItem(this, outputItem());
                        }else {
                            offload(outputItem());
                        }
                    }
                }
                progress %= mineInterval();
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            if(outputItem() != null){
                float dx = x - size * tilesize/2f, dy = y + size * tilesize/2f, s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(outputItem().fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(outputItem().fullIcon, dx, dy, s, s);
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
                if (items.total() < itemCapacity && outputItem() != null){
                    warmup = Mathf.lerp(warmup, efficiency, 0.005f);
                }else {
                    warmup = Mathf.lerp(warmup, 0, 0.01f);
                }
                drawMining();
            }
            Draw.z(Layer.blockOver - 4f);
            Draw.rect(topRegion, x, y);
            if(outputItem() != null){
                Draw.color(dominantItem.color);
                Draw.rect(oreRegion, x, y);
                Draw.color();
            }

            drawTeamTop();
        }

        public void drawMining(){}

        private void tryDump(){
            if(timer(timerDump, dumpTime)){
                if (outputItem() != null){
                    if (coreSend && items.has(outputItem()) && core() != null && core().acceptItem(this, outputItem())){
                        items.remove(outputItem(), 1);
                        core().handleItem(this, outputItem());
                    }else {
                        dump(items.has(outputItem()) ? outputItem() : null);
                    }
                }
            }
        }

        private void updateEffect(){
            if (!headless){
                if (warmup > 0.8f && efficiency > 0 && outputItem() != null && globalEffectRand.chance(updateEffectChance * boostScl())){
                    updateEffect.at(x + globalEffectRand.range(size * 3.6f), y + globalEffectRand.range(size * 3.6f), outputItem().color);
                }
            }
        }

        private void resetModule(){
            boostMul = 1f;
            boostFinalMul = 1f;
            powerConsMul = 1f;
            powerConsExtra = 0f;
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

        public String getMineInfo(){
            return outputItem() == null?
                Iconc.cancel + " No Available Resource": convertItem == null?
                Fonts.getUnicodeStr(outputItem().name) + " " + outputItem().localizedName:
                Fonts.getUnicodeStr(dominantItem.name) + " " + dominantItem.localizedName + " -> " + Fonts.getUnicodeStr(outputItem().name) + " " + outputItem().localizedName;
        }

        public Color getMineColor(){
            return outputItem() == null? Pal.darkishGray: Tmp.c1.set(outputItem().color).lerp(Color.black, 0.2f);
        }

        public String getBuildInfo(){
            return getMineSpeed() + "/s (" + (boostScl() >= 1? "+": "")+ (int)((boostScl() - 1) * 100) + "%)";
        }

        //notice in tick
        public float getPowerCons(){
            return (powerConsBase * powerConsMul + powerConsExtra) / 60f;
        }

        private void updateProgress(){
            if (items.total() < itemCapacity){
                progress += edelta() * Mathf.clamp((float) dominantItems/maxOreTileReq) * boostScl();
            }
        }

        public float boostScl(){
            return boostMul * boostFinalMul;
        }

        private float getMineSpeed(){
            return Mathf.clamp((float) dominantItems/maxOreTileReq) * boostScl() * mineSpeed;
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
