package newhorizon.expand.block.production;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;

public class DrillModule extends Block {
    public TextureRegion topFullRegions;
    public TextureRegion[] topRotRegions;
    public Seq<Item[]> convertList = new Seq<>();
    public float boostSpeed = 0f;
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
            drill.boostMultiplier += boostSpeed;
            for (Item[] convert: convertList){
                if (drill.dominantItem == convert[0]){
                    drill.dominantItem = convert[1];
                }
            }
            if (coreSend){
                drill.coreSend = coreSend;
            }
        }
    }
}
