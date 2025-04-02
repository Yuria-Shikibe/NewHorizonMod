package newhorizon.content.blocks;

import arc.graphics.Blending;
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
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.NewHorizon;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.drill.*;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.type.ItemStack.with;
import static newhorizon.util.func.NHFunc.rand;

public class ProductionBlock {
    public static AdaptDrill resonanceMiningFacility, beamMiningFacility, implosionMiningFacility;

    public static DrillModule speedModule, refineModule, deliveryModule;

    public static void load(){
        resonanceMiningFacility = new AdaptDrill("resonance-mining-facility"){{
            requirements(Category.production, with(Items.copper, 60, Items.lead, 45, Items.titanium, 40, Items.graphite, 20, Items.silicon, 40));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});

            health = 960;

            mineSpeed = 3f;
            mineCount = 3;
            mineTier = 5;

            powerConsBase = 150f;

            itemCapacity = 45;
            maxModules = 2;

            updateEffect = new Effect(30f, e -> {
                Rand rand = rand(e.id);
                Draw.color(e.color, Color.white, e.fout() * 0.66f);
                Draw.alpha(0.55f * e.fout() + 0.5f);
                Angles.randLenVectors(e.id, 2, 4f + e.finpow() * 17f, (x, y) -> {
                    Fill.square(e.x + x, e.y + y, e.fout() * rand.random(2.5f, 4));
                });
            });

            drawer = b -> {
                float rad = 9.2f + Mathf.absin(8, 1);
                float base = (Time.time / 30f);
                Tmp.c1.set(b.dominantItem.color).lerp(Color.white, 0.2f).a(b.warmup);
                Draw.color(Tmp.c1);
                Lines.stroke(1.2f);
                for(int i = 0; i < 32; i++){
                    rand.setSeed(id + hashCode() + i);
                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                    float angle = rand.random(360f);
                    float len = 12.5f * Interp.pow2.apply(fout);
                    Lines.lineAngle(
                            b.x + Angles.trnsx(angle, len),
                            b.y + Angles.trnsy(angle, len),
                            angle, 6 * fin
                    );
                }


                Tmp.c1.set(Pal.techBlue).lerp(Color.white, 0.2f).a(b.warmup/1.1f);
                Draw.color(Tmp.c1);
                Lines.stroke(1.32f);
                Lines.circle(b.x, b.y, rad);

                Draw.reset();
            };
        }};
        //beamMiningFacility = new BeamDrill();
        //implosionMiningFacility = new ImplosionDrill();

        speedModule = new DrillModule("speed-module"){{
            requirements(Category.production, with(NHItems.juniorProcessor, 30, NHItems.presstanium, 25, Items.phaseFabric, 10, NHItems.multipleSteel, 20));
            health = 760;
            size = 2;
            boostSpeed = 0.5f;
            powerMul = 0.4f;
            powerExtra = 80f;

            drawer = module -> {
                for (int i = 0; i < 3; i++){
                    float scl = (Mathf.sinDeg(-Time.time * 3 + 120 * i) * 1.2f + (Mathf.sinDeg(-Time.time * 3 + 120 * i + 120)) * 0.6f) * module.smoothWarmup;
                    Draw.alpha(scl);
                    Draw.rect(name + "-arrow-" + i, module.x, module.y, module.rotdeg());
                }
            };
        }};
        refineModule = new DrillModule("refine-module"){{
            requirements(Category.production, with(NHItems.juniorProcessor, 35, Items.metaglass, 20, NHItems.presstanium, 40));
            health = 720;
            size = 2;
            boostFinalMul = -0.25f;
            powerMul = 1f;
            powerExtra = 180f;
            convertList.add(new Item[]{Items.sand, Items.silicon}, new Item[]{Items.coal, Items.graphite}, new Item[]{Items.beryllium, Items.oxide});
            convertMul.put(Items.sand, -0.6f);
            convertMul.put(Items.coal, -0.4f);
            convertMul.put(Items.beryllium, -0.25f);


            Color flameColor = Color.valueOf("f58349"), midColor = Color.valueOf("f2d585");
            float flameRad = 1f, circleSpace = 2f, flameRadiusScl = 8f, flameRadiusMag = 0.6f, circleStroke = 1.5f;

            float alpha = 0.5f;
            int particles = 12;
            float particleLife = 70f, particleRad = 7f, particleSize = 3f, fadeMargin = 0.4f, rotateScl = 1.5f;
            Interp particleInterp = new Interp.PowIn(1.5f);

            drawer = module -> {
                Lines.stroke(circleStroke * module.smoothWarmup);

                float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
                float a = alpha * module.smoothWarmup;
                Draw.blend(Blending.additive);

                Draw.color(midColor, a);
                Fill.circle(module.x, module.y, flameRad + si);

                Draw.color(flameColor, a);
                Lines.circle(module.x, module.y, (flameRad + circleSpace + si) * module.smoothWarmup);

                rand.setSeed(id);
                float base = (Time.time / particleLife);
                for(int i = 0; i < particles; i++){
                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                    float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                    float len = particleRad * particleInterp.apply(fout);
                    Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                    Fill.circle(
                            module.x + Angles.trnsx(angle, len),
                            module.y + Angles.trnsy(angle, len),
                            particleSize * fin * module.smoothWarmup
                    );
                }

                Draw.blend();
                Draw.reset();
            };
        }};
        deliveryModule = new DrillModule("delivery-module"){{
            requirements(Category.production, with(NHItems.juniorProcessor, 50, NHItems.irayrondPanel, 25, NHItems.seniorProcessor, 50, NHItems.multipleSteel, 50, NHItems.setonAlloy, 10));
            health = 800;
            size = 2;
            powerMul = 1.2f;
            powerExtra = 300f;
            coreSend = true;

            drawer = module -> {
                Draw.z(Layer.effect);
                Draw.color(module.team.color, Color.white, 0.2f);
                Lines.stroke(1.2f * module.smoothWarmup);


                float ang1 = DrawFunc.rotator_90(DrawFunc.cycle(Time.time / 4f, 0, 45), 0.15f);
                float ang2 = DrawFunc.rotator_90(DrawFunc.cycle(Time.time / 3f, 0, 120), 0.15f);

                Lines.spikes(module.x, module.y, 8 + 4 * Mathf.sinDeg(Time.time * 3f + 20), 3 + Mathf.sinDeg(Time.time * 2.5f), 4, ang1 + 45);
                Lines.spikes(module.x, module.y, 7 + 3 * Mathf.sinDeg(Time.time * 3.2f), 4 + 1.2f * Mathf.sinDeg(Time.time * 2.2f), 4, ang2);

                Lines.square(module.x, module.y, 8, Time.time / 8f);
                Lines.square(module.x, module.y, 8, -Time.time / 8f);
            };
        }};
    }
}
