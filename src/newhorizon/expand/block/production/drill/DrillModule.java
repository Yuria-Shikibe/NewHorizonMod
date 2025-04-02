package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.func.Cons;
import arc.func.Cons2;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.EnumSet;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHStats;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class DrillModule extends Block {
    public TextureRegion[] topRegion;
    public TextureRegion baseRegion;

    public Seq<Item[]> convertList = new Seq<>();
    public ObjectFloatMap<Item> convertMul = new ObjectFloatMap<>();
    public float boostSpeed = 0f;
    public float boostFinalMul = 0f;
    public float powerMul = 0f;
    public float powerExtra = 0f;
    public boolean coreSend = false;

    public Cons<DrillModuleBuild> drawer = module -> {};
    public DrillModule(String name) {
        super(name);
        size = 2;

        update = false;
        solid = true;
        destructible = true;
        rotate = true;

        drawCracks = false;

        hasItems = false;
        hasLiquids = false;
        hasPower = false;

        canOverdrive = false;
        drawDisabled = false;

        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;

        group = BlockGroup.drills;
        flags = EnumSet.of(BlockFlag.drill);
    }

    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(name + "-base");
        topRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-top"), 64, 64, 1);
    }

    @Override
    public void setStats() {
        super.setStats();
        if (powerMul != 0 || powerExtra != 0) stats.add(NHStats.powerConsModifier, Core.bundle.get("nh.stat.power-cons-modifier"), Strings.autoFixed(powerMul * 100, 0), Strings.autoFixed(powerExtra, 0));
        if (boostSpeed != 0 || boostFinalMul != 0) stats.add(NHStats.minerBoosModifier, Core.bundle.get("nh.stat.miner-boost-modifier"), Strings.autoFixed(boostSpeed * 100, 0), Strings.autoFixed(boostFinalMul * 100, 0));
        if (convertList.size > 0) stats.add(NHStats.itemConvertList, getConvertList());
    }

    public String getConvertList(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < convertList.size; i++){
            Item[] convert = convertList.get(i);
            String cvt = Fonts.getUnicodeStr(convert[0].name) + convert[0].localizedName + " -> " + Fonts.getUnicodeStr(convert[1].name) + convert[1].localizedName + "(" + Strings.autoFixed((convertMul.get(convert[0], boostFinalMul)) * 100, 0) + "%)" + (i == convertList.size - 1?"": "\n");
            builder.append(cvt);
        }
        return builder.toString();
    }


    @Override
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRegion[plan.rotation + 8], plan.drawx(), plan.drawy());

        drawPlanConfig(plan, list);
    }

    public class DrillModuleBuild extends Building{
        public @Nullable AdaptDrill.AdaptDrillBuild drillBuild;
        public float smoothWarmup, targetWarmup;
        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.z(Layer.blockOver);
            drawTeamTop();
            if (drillBuild != null) {
                Draw.rect(topRegion[rotation], x, y);
                Draw.rect(topRegion[rotation + 4], Geometry.d4x(rotation) * size * tilesize + x, Geometry.d4y(rotation) * size * tilesize + y);
            }else {
                Draw.rect(topRegion[rotation + 8], x, y);
            }

            drawer.get(this);

            targetWarmup = (drillBuild != null && drillBuild.modules.contains(this))?drillBuild.warmup : 0;
            smoothWarmup = Mathf.lerp(smoothWarmup, targetWarmup, 0.02f);
        }

        public boolean canApply(AdaptDrill.AdaptDrillBuild drill){
            for (int i = 0; i < size; i++){
                Point2 p = Edges.getEdges(size)[rotation * size + i];
                Building t = world.build(tileX() + p.x, tileY() + p.y);
                if (t != drill){
                    return false;
                }
            }
            return drill.modules.size < drill.maxModules();
        }

        public void apply(AdaptDrill.AdaptDrillBuild drill){
            drill.powerConsMul += powerMul;
            drill.powerConsExtra += powerExtra;
            drill.boostMul += boostSpeed;
            for (Item[] convert: convertList){
                if (drill.dominantItem == convert[0]){
                    drill.convertItem = convert[1];
                    drill.items.set(convert[0], 0);
                    drill.boostFinalMul += convertMul.get(convert[0], boostFinalMul);
                }
            }
            if (coreSend){
                drill.coreSend = true;
            }
        }
    }
}
