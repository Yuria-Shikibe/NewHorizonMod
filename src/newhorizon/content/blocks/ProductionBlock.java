package newhorizon.content.blocks;

import arc.Core;
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
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.production.Drill;
import mindustry.world.draw.*;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.*;
import newhorizon.expand.block.drawer.DrawRotator;
import newhorizon.expand.block.production.drill.AdaptDrill;
import newhorizon.expand.block.production.drill.DrillModule;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Lines.circleVertices;
import static mindustry.type.ItemStack.with;
import static newhorizon.util.func.NHFunc.rand;

public class ProductionBlock {
    public static Block solidificationShaper, sandCracker, tungstenReconstructor, titaniumReconstructor, liquidConvertor, xenExtractor, xenIterator;
    public static Drill opticalMediumDrill;
    public static AdaptDrill resonanceMiningFacility, beamMiningFacility, implosionMiningFacility;
    public static DrillModule speedModule, speedModuleMk2, refineModule, convertorModule, deliveryModule;

    public static void load() {
        //wip
        solidificationShaper = new RecipeGenericCrafter("solidification-shaper") {{
            requirements(Category.production, ItemStack.with(NHItems.hardLight, 10));
            health = 300;
            size = 2;
            craftTime = 120f;
            itemCapacity = 30;
            craftEffect = Fx.smeltsmoke;
            outputsPower = true;
            rotate = false;
            powerProduction = 0.1f;

            outputItem = new ItemStack(NHItems.hardLight, 2);
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.darkEnergy.color));

        //    buildType = () -> new RecipeGenericCrafterBuild() {
        //        @Override
        //        public void updateTile() {
        //            super.updateTile();
        //            if (power != null) {
        //                float sunlightFactor = Mathf.clamp(
        //                        (1f - Vars.state.rules.ambientLight.a) * Vars.state.rules.solarMultiplier,
        //                        0.4f, 4f
        //                );

        //                powerProduction = sunlightFactor;
        //            }
        //    }
        //    };

        }};

        sandCracker = new RecipeGenericCrafter("sand-cracker") {{
            requirements(Category.production, ItemStack.with(
                    NHItems.silicon, 40,
                    NHItems.graphite, 40
            ));
            size = 2;
            health = 300;
            armor = 2;
            itemCapacity = 30;
            rotate = false;

            drawer = new DrawMulti(new DrawRegion("-base"), new DrawRotator(), new DrawRegion("-top"));
            craftEffect = NHFx.hugeSmokeGray;
            updateEffect = new Effect(80f, e -> {
                Fx.rand.setSeed(e.id);
                Draw.color(Color.lightGray, Color.gray, e.fin());
                Angles.randLenVectors(e.id, 4, 2.0F + 12.0F * e.fin(Interp.pow3Out), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * Fx.rand.random(1, 2.5f));
                });
            }).layer(Layer.blockOver + 1);

            consumePower(5f);
        }};
        tungstenReconstructor = new RecipeGenericCrafter("tungsten-reconstructor") {{
            requirements(Category.production, ItemStack.with(
                    NHItems.silicon, 40,
                    NHItems.graphite, 40
            ));     
            size = 2;
            craftTime = 60f;
            itemCapacity = 30;
            liquidCapacity = 30f;

            rotate = false;

            craftEffect = updateEffect = NHFx.square(NHColor.thurmixRed, 60, 6, 16, 3);

            consumePower(300f / 60f);

            drawer = new DrawMulti(new DrawDefault());
        }};
        titaniumReconstructor = new RecipeGenericCrafter("titanium-reconstructor") {{
            requirements(Category.production, ItemStack.with(
                    NHItems.silicon, 40,
                    NHItems.graphite, 40
            ));
            
            size = 2;
            craftTime = 60f;
            itemCapacity = 30;
            liquidCapacity = 30f;

            rotate = false;

            craftEffect = updateEffect = NHFx.square(NHColor.xenGamma, 60, 6, 16, 3);

            consumePower(300f / 60f);

            drawer = new DrawMulti(new DrawDefault());
        }};
        liquidConvertor = new RecipeGenericCrafter("liquid-convertor") {{
            requirements(Category.production, ItemStack.with(
                    NHItems.silicon, 40,
                    NHItems.graphite, 40
            ));
            size = 2;
            health = 300;
            armor = 2;
            itemCapacity = 30;
            liquidCapacity = 90f;
            rotate = false;

            drawer = new DrawMulti(new DrawRegion("-base"), new DrawCrucibleFlame() {{
                midColor = flameColor = Pal.accent;
                flameRad /= 1.585f;
                particleRad /= 1.5f;
            }}, new DrawRegion("-top"));
            craftEffect = updateEffect = NHFx.square(Pal.accent, 60, 6, 16, 3);
            consumePower(5f);
        }};
        xenExtractor = new ThermalGenerator("xen-extractor") {{
            requirements(Category.production, with(NHItems.titanium, 40, NHItems.silicon, 40));
            attribute = NHContent.quantum;
            group = BlockGroup.liquids;
            displayEfficiencyScale = 1f / 9f;
            minEfficiency = 9f - 0.0001f;
            powerProduction = 800.0001f / 60f / 9f;
            displayEfficiency = false;
            effectChance = 0.2f;
            generateEffect = new OptionalMultiEffect(
                    NHFx.square(NHColor.lightSkyFront, 60, 6, 32, 3),
                    new Effect(40f, 80f, e -> {
                        Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin() * 0.8f);
                        Lines.stroke(2f * e.fout());
                        Lines.spikes(e.x, e.y, 12 * e.finpow(), 1.5f * e.fout() + 4 * e.fslope(), 4, 45);
                    })
            );
            effectChance = 0.04f;
            size = 3;
            ambientSound = Sounds.hum;
            ambientSoundVolume = 0.06f;

            squareSprite = false;

            drawer = new DrawMulti(
                    new DrawRegion("-base"),
                    new DrawLiquidTile(NHLiquids.xenFluid, 2f),
                    new DrawRegion("-top")
            );

            hasLiquids = true;
            outputLiquid = new LiquidStack(NHLiquids.xenFluid, 30f / 60f / 9f);
            liquidCapacity = 300f;
            health = 1200;
            armor = 8;
        }};
        xenIterator = new RecipeGenericCrafter("xen-iterator"){{
            requirements(Category.production, ItemStack.with(
                    NHItems.metalOxhydrigen, 40,
                    NHItems.juniorProcessor, 80,
                    NHItems.zeta, 100
            ));
            size = 3;
            health = 150 * 9;
            armor = 10f;
            itemCapacity = 30;
            rotate = false;

            liquidCapacity = 300f;
            //consumePower(5f);
        }};

        opticalMediumDrill = new Drill("optical-medium-drill"){{
            requirements(Category.production, with(NHItems.silicon, 20, NHItems.hardLight, 20));
            drillTime = 360;
            size = 3;
            tier = 3;
            itemCapacity = 20;
            liquidCapacity = 20f;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;
            
            consumeLiquid(NHLiquids.quantumLiquid, 0.08f).boost();
            liquidBoostIntensity = 1.5f;
        }};
        resonanceMiningFacility = new AdaptDrill("resonance-mining-facility") {{
            requirements(Category.production, with(Items.titanium, 80, Items.silicon, 120, Items.tungsten, 40));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});

            health = 900;
            armor = 6f;

            mineSpeed = 6f;
            mineCount = 15;
            mineTier = 5;

            powerConsBase = 120f;

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

            updateEffectChance = 0.1f;

            drawer = b -> {
                float rad = 9.2f + Mathf.absin(8, 1);
                float base = (Time.time / 30f);
                Tmp.c1.set(b.dominantItem.color).lerp(Color.white, 0.2f).a(b.warmup);
                Draw.color(Tmp.c1);
                Lines.stroke(1.2f);
                for (int i = 0; i < 32; i++) {
                    rand.setSeed(b.id + i);
                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                    float angle = rand.random(360f);
                    float len = 12.5f * Interp.pow2.apply(fout);
                    Lines.lineAngle(
                            b.x + Angles.trnsx(angle, len),
                            b.y + Angles.trnsy(angle, len),
                            angle, 6 * fin
                    );
                }

                Tmp.c1.set(Pal.techBlue).lerp(Color.white, 0.2f).a(b.warmup / 1.1f);
                Draw.color(Tmp.c1);
                Lines.stroke(1.32f);
                Lines.circle(b.x, b.y, rad);

                Draw.reset();
            };
        }};
        beamMiningFacility = new AdaptDrill("beam-mining-facility") {{
            requirements(Category.production, with(NHItems.metalOxhydrigen, 60, Items.tungsten, 90, Items.surgeAlloy, 80, Items.phaseFabric, 60, NHItems.zeta, 60));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});

            health = 1200;
            armor = 8f;

            mineSpeed = 10f;
            mineCount = 20;
            mineTier = 5;

            powerConsBase = 180f;
            itemCapacity = 75;

            maxModules = 4;

            updateEffect = new Effect(30f, e -> {
                Rand rand = rand(e.id);
                Draw.color(e.color, Color.white, e.fout() * 0.66f);
                Draw.alpha(0.55f * e.fout() + 0.5f);
                Angles.randLenVectors(e.id, 2, 4f + e.finpow() * 17f, (x, y) -> {
                    Fill.square(e.x + x, e.y + y, e.fout() * rand.random(2.5f, 4), 45);
                });
            });
            updateEffectChance = 0.06f;

            drawer = b -> {
                float shooterOffset = 12f;
                float shooterExtendOffset = 1.8f;
                float shooterMoveRange = 5.2f;
                float shootY = 1.55f;
                float moveScale = 60f;
                float moveScaleRand = 20f;
                float laserScl = 0.2f;
                Color laserColor = Color.valueOf("f58349");
                float laserAlpha = 0.75f;
                float laserAlphaSine = 0.2f;
                int particles = 25;
                float particleLife = 40f, particleRad = 9.75f, particleLen = 4f;

                float timeDrilled = Time.time / 2.5f;
                float
                        moveX = Mathf.sin(timeDrilled, moveScale + Mathf.randomSeed(b.id, -moveScaleRand, moveScaleRand), shooterMoveRange) + b.x,
                        moveY = Mathf.sin(timeDrilled + Mathf.randomSeed(b.id >> 1, moveScale), moveScale + Mathf.randomSeed(b.id >> 2, -moveScaleRand, moveScaleRand), shooterMoveRange) + b.y;

                float stroke = laserScl * b.warmup;
                Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));
                Draw.alpha(laserAlpha + Mathf.absin(8f, laserAlphaSine));
                Draw.blend(Blending.additive);
                Drawf.laser(Core.atlas.find("minelaser"), Core.atlas.find("minelaser-end"), b.x + (-shooterOffset + b.warmup * shooterExtendOffset + shootY), moveY, b.x - (-shooterOffset + b.warmup * shooterExtendOffset + shootY), moveY, stroke);
                Drawf.laser(Core.atlas.find("minelaser"), Core.atlas.find("minelaser-end"), moveX, b.y + (-shooterOffset + b.warmup * shooterExtendOffset + shootY), moveX, b.y - (-shooterOffset + b.warmup * shooterExtendOffset + shootY), stroke);

                Draw.color(b.dominantItem.color);

                float sine = 1f + Mathf.sin(6f, 0.1f);

                Lines.stroke(stroke / laserScl / 2f);
                Lines.circle(moveX, moveY, stroke * 12f * sine);
                Fill.circle(moveX, moveY, stroke * 8f * sine);

                rand.setSeed(id);
                float base = (Time.time / particleLife);
                for (int i = 0; i < particles; i++) {
                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                    float angle = rand.random(360f);
                    float len = Mathf.randomSeed(rand.nextLong(), particleRad * 0.8f, particleRad * 1.1f) * Interp.pow2Out.apply(fin);
                    Lines.lineAngle(moveX + Angles.trnsx(angle, len), moveY + Angles.trnsy(angle, len), angle, particleLen * fout * stroke / laserScl);
                }

                Draw.blend();
                Draw.reset();
            };
        }};
        implosionMiningFacility = new AdaptDrill("implosion-mining-facility") {{
            requirements(Category.production, with(NHItems.multipleSteel, 60, NHItems.setonAlloy, 80, NHItems.irayrondPanel, 60, NHItems.zeta, 150));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});
            size = 4;

            health = 1500;
            armor = 10f;

            mineSpeed = 12f;
            mineCount = 30;
            mineTier = 100;

            itemCapacity = 120;

            maxModules = 8;

            powerConsBase = 480f;

            updateEffectChance = 0.04f;

            updateEffect = new Effect(30f, e -> {
                Rand rand = rand(e.id);
                Draw.color(e.color, Color.white, e.fout() * 0.66f);
                Draw.alpha(0.55f * e.fout() + 0.5f);
                Angles.randLenVectors(e.id, 4, 4f + e.finpow() * 17f, (x, y) -> {
                    Fill.poly(e.x + x, e.y + y, 3, e.fout() * rand.random(2.5f, 4), rand.random(360));
                });
            });

            drawer = b -> {
                rand.setSeed(b.id);

                float base = (Time.time / 25);
                Tmp.c1.set(b.dominantItem.color).lerp(Color.white, 0.2f).a(b.warmup);
                Draw.color(Tmp.c1);
                Lines.stroke(1.2f);
                for (int i = 0; i < 32; i++) {
                    rand.setSeed(id + hashCode() + i);
                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                    float angle = rand.random(360f);
                    float len = 13.5f * Interp.pow2.apply(fout);
                    Lines.lineAngle(
                            b.x + Angles.trnsx(angle, len),
                            b.y + Angles.trnsy(angle, len),
                            angle, 6 * fin
                    );
                }

                Tmp.c1.set(b.team.color).lerp(Color.white, 0.4f).a(b.warmup / 1.1f);
                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y, 3 + Mathf.sinDeg(Time.time * 1.2f));
                Lines.stroke(1.3f);
                Lines.circle(b.x, b.y, 6 + Mathf.sinDeg(Time.time * 1.2f));
                Fill.light(b.x, b.y, circleVertices(15f), 15f, Color.clear, Tmp.c1);

                Draw.color();
            };
        }};

        speedModule = new DrillModule("speed-module") {{
            requirements(Category.production, with(NHItems.juniorProcessor, 30, NHItems.presstanium, 25, NHItems.metalOxhydrigen, 20));
            health = 600;
            armor = 4;
            size = 2;
            boostSpeed = 1f;
            powerMul = 0.4f;
            powerExtra = 80f;

            drawer = module -> {
                for (int i = 0; i < 3; i++) {
                    float scl = (Mathf.sinDeg(-Time.time * 3 + 120 * i) * 1.2f + (Mathf.sinDeg(-Time.time * 3 + 120 * i + 120)) * 0.6f) * module.smoothWarmup;
                    Draw.alpha(scl);
                    Draw.rect(name + "-arrow-" + i, module.x, module.y, module.rotdeg());
                }
            };
        }};
        speedModuleMk2 = new DrillModule("speed-module-mk2") {{
            requirements(Category.production, with(NHItems.seniorProcessor, 30, Items.phaseFabric, 25, NHItems.zeta, 40));
            health = 900;
            armor = 6;
            size = 2;
            boostSpeed = 2f;
            powerMul = 0.8f;
            powerExtra = 150f;

            drawer = module -> {
                for (int i = 0; i < 3; i++) {
                    float scl = (Mathf.sinDeg(-Time.time * 3 + 120 * i) * 1.2f + (Mathf.sinDeg(-Time.time * 3 + 120 * i + 120)) * 0.6f) * module.smoothWarmup;
                    Draw.alpha(scl);
                    Draw.rect(name + "-arrow-" + i, module.x, module.y, module.rotdeg());
                }
            };
        }};
        refineModule = new DrillModule("refine-module") {{
            requirements(Category.production, with(Items.titanium, 35, Items.tungsten, 40));
            health = 600;
            armor = 4;
            size = 2;
            boostFinalMul = -0.25f;
            powerMul = 1f;
            powerExtra = 180f;
            convertList.add(
                    new Item[]{Items.sand, Items.silicon},
                    new Item[]{Items.coal, Items.graphite},
                    new Item[]{Items.beryllium, Items.oxide},
                    new Item[]{Items.thorium, NHItems.zeta}
            );
            convertMul.put(Items.sand, -0.6f);
            convertMul.put(Items.coal, -0.4f);
            convertMul.put(Items.beryllium, -0.25f);
            convertMul.put(Items.thorium, -0.5f);


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
                for (int i = 0; i < particles; i++) {
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
        convertorModule = new DrillModule("convertor-module") {{
            requirements(Category.production, with(Items.carbide, 25, NHItems.juniorProcessor, 30, NHItems.presstanium, 20));
            health = 600;
            armor = 4;
            size = 2;
            convertList.add(
                    new Item[]{Items.titanium, Items.tungsten},
                    new Item[]{Items.copper, Items.tungsten},
                    new Item[]{Items.lead, Items.tungsten}
            );
            convertList.add(
                    new Item[]{Items.tungsten, Items.titanium},
                    new Item[]{Items.beryllium, Items.titanium}
            );
            convertMul.put(Items.titanium, -0.33f);
            convertMul.put(Items.copper, -0.6f);
            convertMul.put(Items.lead, -0.6f);
            convertMul.put(Items.tungsten, 0.5f);
            convertMul.put(Items.beryllium, 0f);


            Color flameColor = NHColor.darkEnrFront, midColor = NHColor.darkEnr;
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
                for (int i = 0; i < particles; i++) {
                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                    float angle = rand.random(360f) - (Time.time / rotateScl) % 360f;
                    float len = particleRad * particleInterp.apply(fout);
                    Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                    Fill.circle(
                            module.x - Angles.trnsx(angle, len),
                            module.y - Angles.trnsy(angle, len),
                            particleSize * fin * module.smoothWarmup
                    );
                }

                Draw.blend();
                Draw.reset();
            };
        }};
        deliveryModule = new DrillModule("delivery-module") {{
            requirements(Category.production, with(NHItems.irayrondPanel, 25, NHItems.seniorProcessor, 50, NHItems.multipleSteel, 50, NHItems.setonAlloy, 10));
            health = 900;
            armor = 6;
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
