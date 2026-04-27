package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.func.Cons;
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
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHStats;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class DrillModule extends Block {
    public float boostSpeed = 0f;
    public DrawBlock drawer = new DrawDefault();

    public DrillModule(String name) {
        super(name);
        size = 2;
        solid = true;
        destructible = true;
        rotate = true;
        enableDrawStatus = false;
        group = BlockGroup.drills;
        flags = EnumSet.of(BlockFlag.drill);
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }

    public class DrillModuleBuild extends Building {
        public @Nullable AdaptDrill.AdaptDrillBuild drillBuild;
        public float totalProgress;
        public float warmup;

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void updateTile() {
            warmup = Mathf.approachDelta(warmup, efficiency, 0.02f);
            totalProgress += warmup * edelta();
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        public boolean canApply(AdaptDrill.AdaptDrillBuild drill) {
            for (int i = 0; i < size; i++) {
                Point2 p = Edges.getEdges(size)[rotation * size + i];
                Building t = world.build(tileX() + p.x, tileY() + p.y);
                if (t != drill) {
                    return false;
                }
            }
            return drill.modules.size < drill.maxModules();
        }

        public void apply(AdaptDrill.AdaptDrillBuild drill) {

        }

        public void updateDrill(AdaptDrill.AdaptDrillBuild drill) {

        }

        @Override
        public void write(Writes write) {
            write.f(warmup);
            write.f(totalProgress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            warmup = read.f();
            totalProgress = read.f();
        }
    }
}
