package newhorizon.expand.block.production.drill;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.content.NHItems;

import static arc.graphics.g2d.Lines.circleVertices;
import static mindustry.type.ItemStack.with;
import static newhorizon.util.func.NHFunc.rand;

public class ImplosionDrill extends AdaptDrill{
    public ImplosionDrill() {
        super("implosion-mining-facility");
        requirements(Category.production, with(NHItems.multipleSteel, 60, NHItems.seniorProcessor, 50, NHItems.irayrondPanel, 25, NHItems.presstanium, 50, NHItems.zeta, 100));
        mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});
        size = 4;

        health = 1560;

        mineSpeed = 10f;
        mineCount = 15;
        mineTier = 100;

        itemCapacity = 120;

        maxBoost = 2f;

        powerConsBase = 480f;


        updateEffect = new Effect(30f, e -> {
            Rand rand = rand(e.id);
            Draw.color(e.color, Color.white, e.fout() * 0.66f);
            Draw.alpha(0.55f * e.fout() + 0.5f);
            Angles.randLenVectors(e.id, 4, 4f + e.finpow() * 17f, (x, y) -> {
                Fill.poly(e.x + x, e.y + y, 3, e.fout() * rand.random(2.5f, 4), rand.random(360));
            });
        });

    }

    public class implosionDrillBuild extends AdaptDrillBuild{
        public float blastTimer;
        public float blastReload = 400f;
        public Rand rand = new Rand();
        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            if (warmup > 0f){drawMining();}
            Draw.z(Layer.blockOver - 4f);
            Draw.rect(topRegion, x, y);
            drawTeamTop();
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (Vars.headless)return;
            if (dominantItem == null) return;
            blastTimer += edelta() * Mathf.clamp((float) dominantItems/maxOreTileReq) * boostScl();
            if (blastTimer >= blastReload) blastTimer = 0;
        }

        @Override
        public void drawMining() {
            rand.setSeed(id);

            float base = (Time.time / 25);
            Tmp.c1.set(dominantItem.color).lerp(Color.white, 0.2f).a(warmup);
            Draw.color(Tmp.c1);
            Lines.stroke(1.2f);
            for(int i = 0; i < 32; i++){
                rand.setSeed(id + hashCode() + i);
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f);
                float len = 13.5f * Interp.pow2.apply(fout);
                Lines.lineAngle(
                    x + Angles.trnsx(angle, len),
                    y + Angles.trnsy(angle, len),
                    angle, 6 * fin
                );
            }

            Tmp.c1.set(team.color).lerp(Color.white, 0.4f).a(warmup/1.1f);
            Draw.color(Tmp.c1);
            Fill.circle(x, y, 3 + Mathf.sinDeg(Time.time * 1.2f));
            Lines.stroke(1.3f);
            Lines.circle(x, y, 6 + Mathf.sinDeg(Time.time * 1.2f));
            Fill.light(x, y, circleVertices(15f), 15f, Color.clear, Tmp.c1);

            Draw.color();
        }


        public float animationProgress(float value){
            if (value <= 0 || value >= 1) return 0;
            return value;
        }
    }
}
