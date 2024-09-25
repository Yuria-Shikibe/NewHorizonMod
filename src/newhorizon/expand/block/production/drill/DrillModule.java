package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
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
import mindustry.world.meta.Stat;
import newhorizon.content.NHStats;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;

public class DrillModule extends Block {
    public TextureRegion topFullRegions;
    public TextureRegion[] topRotRegions;
    public Seq<Item[]> convertList = new Seq<>();
    public float boostSpeed = 0f;
    public float boostFinalMul = 0f;
    public float powerMul = 0f;
    public float powerExtra = 0f;
    public boolean coreSend = false;
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
        topFullRegions = Core.atlas.find(name + "-top-full");
        topRotRegions = SpriteUtil.splitRegionArray(topFullRegions, 80, 80);
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
            String cvt = Fonts.getUnicodeStr(convert[0].name) + convert[0].localizedName + " -> " + Fonts.getUnicodeStr(convert[1].name) + convert[1].localizedName + (i == convertList.size - 1?"": "\n");
            builder.append(cvt);
        }
        return builder.toString();
    }


    @Override
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRotRegions[plan.rotation], plan.drawx(), plan.drawy());
        drawPlanConfig(plan, list);
    }

    public class DrillModuleBuild extends Building{

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            Draw.z(Layer.blockOver);
            drawTeamTop();
            Draw.rect(topRotRegions[rotation], x, y);
        }

        public boolean canApply(AdaptDrill.AdaptDrillBuild drill){
            for (int i = 0; i < size; i++){
                Point2 p = Edges.getEdges(size)[rotation * size + i];
                Building t = world.build(tileX() + p.x, tileY() + p.y);
                if (t != drill){
                    return false;
                }
            }
            return true;
        }
        public void apply(AdaptDrill.AdaptDrillBuild drill){
            drill.boostMul += boostSpeed;
            drill.boostFinalMul += boostFinalMul;
            drill.powerConsMul += powerMul;
            drill.powerConsExtra += powerExtra;
            for (Item[] convert: convertList){
                if (drill.dominantItem == convert[0]){
                    drill.convertItem = convert[1];
                }
            }
            if (coreSend){
                drill.coreSend = true;
            }
        }
    }
}