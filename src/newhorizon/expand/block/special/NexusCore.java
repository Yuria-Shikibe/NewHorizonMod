package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.Category;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class NexusCore extends CoreBlock {
    public final Seq<Trail> trails = Seq.with(new Trail(30), new Trail(40), new Trail(50), new Trail(60), new Trail(70), new Trail(80), new Trail(90));
    public final Interp interp = Interp.pow2Out;
    public float coreDelay = -1;
    public static Rand rand = new Rand();

    public TextureRegion base;
    public NexusCore() {
        super("nexus-core");

        requirements(Category.effect, with(NHItems.zeta, 1500, NHItems.presstanium, 1000, NHItems.juniorProcessor, 1000, NHItems.metalOxhydrigen, 1800, NHItems.multipleSteel, 600));

        alwaysUnlocked = true;

        unitType = NHUnitTypes.liv;
        health = 30000;
        itemCapacity = 10000;
        size = 5;
        armor = 20f;
        incinerateNonBuildable = false;
        buildCostMultiplier = 2f;
        requiresCoreZone = true;

        unitCapModifier = 10;

        drawTeamOverlay = false;
    }

    @Override
    public void load() {
        super.load();
        base = Core.atlas.find(name + "-base");
    }

    public void drawLanding(CoreBuild build, float x, float y){
        if (coreDelay == -1){
            coreDelay = renderer.getLandTime();
            Time.run(coreDelay, () -> {
                coreDelay = -1;
                NHFx.smoothColorRect(build.team.color, 92f, 165f).at(build);
                new Effect(150, e -> {
                    color(build.team.color);
                    stroke(2f * e.fout() + 2f);
                    Lines.square(e.x, e.y, e.finpow() * 62f);

                    e.scaled(60, f -> {
                        rand.setSeed(f.id);
                        stroke(3f * f.fout());
                        randLenVectors(f.id, 12, 2f + 148f * f.finpow(), (fx, fy) -> {
                            Lines.square(f.x + fx, f.y + fy, f.foutpow() * rand.random(12f, 20f));
                        });
                    });
                }).at(build);
                trails.clear().add(Seq.with(new Trail(30), new Trail(40), new Trail(50), new Trail(60), new Trail(70), new Trail(80), new Trail(90)));
            });
        }
        float fout = renderer.getLandTime() / coreLandDuration;
        float fin = 1f - fout;
        for (int i = 0; i < trails.size; i++){
            rand.setSeed(build.id + i);
            float ang = rand.random(360f) + 360 * rand.random(2f, 6f) * interp.apply(fout);
            float dst = rand.random(40, 220) * interp.apply(fout);
            Tmp.v1.trns(ang, dst);
            trails.get(i).update(x + Tmp.v1.x, y + Tmp.v1.y);
            float offset = rand.random(0.65f, 1.25f);
            Tmp.c1.set(build.team.color).mul(offset);
            Fx.trailFade.at(x, y, 2.5f, Tmp.c1, trails.get(i).copy());
        }

    }

    @Override
    public void drawShadow(Tile tile) {
        super.drawShadow(tile);

    }

    public class NexusCoreBuild extends CoreBuild{
        public void updateLandParticles(){}

        @Override
        public void draw(){
            if(!(renderer.getLandTime() > 0)){
                Draw.rect(base, x, y);
                drawTeamTop();
            }
        }
    }
}
