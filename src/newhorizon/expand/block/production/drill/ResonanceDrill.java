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
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.content.NHColor;

import static mindustry.type.ItemStack.with;
import static newhorizon.util.func.NHFunc.rand;

public class ResonanceDrill extends AdaptDrill {
    public ResonanceDrill() {
        super("resonance-mining-facility");
        requirements(Category.production, with(Items.copper, 40, Items.lead, 20, Items.titanium, 32));
        mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium});

        mineSpeed = 5;
        mineCount = 2;

        powerConsBase = 0f;

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
        public void drawMining(){
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
    }
}
