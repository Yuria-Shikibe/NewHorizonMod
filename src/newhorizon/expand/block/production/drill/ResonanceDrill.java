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
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.content.NHItems;

import static mindustry.type.ItemStack.with;
import static newhorizon.util.func.NHFunc.rand;

public class ResonanceDrill extends AdaptDrill {
    public ResonanceDrill() {
        super("resonance-mining-facility");
        requirements(Category.production, with(Items.copper, 60, Items.lead, 45, Items.titanium, 40, Items.graphite, 20, Items.silicon, 40));
        mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});

        health = 960;

        mineSpeed = 5f;
        mineCount = 3;
        mineTier = 5;

        powerConsBase = 150f;

        itemCapacity = 45;
        maxBoost = 0.5f;

        updateEffect = new Effect(30f, e -> {
            Rand rand = rand(e.id);
            Draw.color(e.color, Color.white, e.fout() * 0.66f);
            Draw.alpha(0.55f * e.fout() + 0.5f);
            Angles.randLenVectors(e.id, 2, 4f + e.finpow() * 17f, (x, y) -> {
                Fill.square(e.x + x, e.y + y, e.fout() * rand.random(2.5f, 4));
            });
        });
    }

    public class ResonanceDrillBuild extends AdaptDrillBuild{
        public Rand rand = new Rand();
        public void drawMining(){
            float rad = 9.2f + Mathf.absin(8, 1);
            float base = (Time.time / 30f);
            Tmp.c1.set(dominantItem.color).lerp(Color.white, 0.2f).a(warmup);
            Draw.color(Tmp.c1);
            Lines.stroke(1.2f);
            for(int i = 0; i < 32; i++){
                rand.setSeed(id + hashCode() + i);
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f);
                float len = 12.5f * Interp.pow2.apply(fout);
                Lines.lineAngle(
                    x + Angles.trnsx(angle, len),
                    y + Angles.trnsy(angle, len),
                    angle, 6 * fin
                );
            }


            Tmp.c1.set(Pal.techBlue).lerp(Color.white, 0.2f).a(warmup/1.1f);
            Draw.color(Tmp.c1);
            Lines.stroke(1.32f);
            Lines.circle(x, y, rad);

            Draw.reset();
        }
    }
}
