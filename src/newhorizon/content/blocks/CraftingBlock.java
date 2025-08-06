package newhorizon.content.blocks;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.*;
import newhorizon.expand.block.drawer.*;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;

import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block
            sandCracker, oilRefiner, convertorTungsten, convertorTitanium, xenRefinery, zetaCrafter,
            stampingFacility, processorPrinter, crucibleFoundry, crystallizer, zetaDissociator, surgeRefactor,
            fabricSynthesizer, processorEncoder, irdryonMixer, irayrondFactory, setonFactory,
            multipleSteelFactory, upgradeSortFactory, ancimembraneConcentrator;

    public static void load() {
        sandCracker = new RecipeGenericCrafter("sand-cracker") {{
            size = 2;
            requirements(Category.crafting, ItemStack.with(NHItems.presstanium, 40, NHItems.juniorProcessor, 40));
            health = 320;
            craftTime = 60f;
            itemCapacity = 60;
            hasPower = hasItems = true;
            craftEffect = NHFx.hugeSmokeGray;
            updateEffect = new Effect(80f, e -> {
                Fx.rand.setSeed(e.id);
                Draw.color(Color.lightGray, Color.gray, e.fin());
                Angles.randLenVectors(e.id, 4, 2.0F + 12.0F * e.fin(Interp.pow3Out), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * Fx.rand.random(1, 2.5f));
                });
            }).layer(Layer.blockOver + 1);

            rotate = false;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawFrames(), new DrawArcSmelt(), new DrawDefault());
            consumePower(5f);

            addInput(Items.scrap, 4, LiquidStack.empty);
            addInput(Items.copper, 10, LiquidStack.empty);
            addInput(Items.lead, 10, LiquidStack.empty);
            addInput(Items.beryllium, 6, LiquidStack.empty);
            addInput(Items.titanium, 5, LiquidStack.empty);
            addInput(Items.thorium, 4, LiquidStack.empty);
            addInput(Items.tungsten, 5, LiquidStack.empty);

            outputItem = new ItemStack(Items.sand, 12);
        }};
        oilRefiner = new GenericCrafter("oil-refiner") {{

            size = 2;
            requirements(Category.production, ItemStack.with(Items.metaglass, 30, NHItems.juniorProcessor, 20, Items.copper, 60, NHItems.metalOxhydrigen, 45));
            health = 200;
            craftTime = 60f;
            liquidCapacity = 60f;
            itemCapacity = 20;
            hasPower = hasLiquids = hasItems = true;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.oil), new DrawDefault());
            consumePower(5f);
            consumeItems(new ItemStack(Items.sand, 3));
            outputLiquid = new LiquidStack(Liquids.oil, 15f / 60f);
        }};
        convertorTungsten = new RecipeGenericCrafter("convertor-tungsten") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.titanium, 45, Items.graphite, 30));

            size = 2;
            craftTime = 30f;
            itemCapacity = 12;

            rotate = false;

            addInput(Items.copper, 5, LiquidStack.empty);
            addInput(Items.lead, 5, LiquidStack.empty);
            addInput(Items.titanium, 3, LiquidStack.empty);

            craftEffect = updateEffect = NHFx.square(NHColor.thurmixRed, 60, 6, 16, 3);

            consumePower(90f / 60f);
            outputItems = with(Items.tungsten, 2);

            drawer = new DrawMulti(new DrawDefault());
        }};
        convertorTitanium = new RecipeGenericCrafter("convertor-titanium") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.tungsten, 45, Items.graphite, 60));

            size = 2;
            craftTime = 30f;
            itemCapacity = 12;

            rotate = false;

            addInput(Items.beryllium, 3, LiquidStack.empty);
            addInput(Items.tungsten, 2, LiquidStack.empty);

            craftEffect = updateEffect = NHFx.square(NHColor.xenGamma, 60, 6, 16, 3);

            consumePower(90f / 60f);
            outputItems = with(Items.titanium, 3);

            drawer = new DrawMulti(new DrawDefault());
        }};
        xenRefinery = new RecipeGenericCrafter("xen-refinery") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 30, NHItems.juniorProcessor, 45, Items.carbide, 30));

            size = 2;
            craftTime = 60f;
            liquidCapacity = 40f;
            itemCapacity = 20;

            rotate = false;

            addInput(Liquids.oil, 15 / 60f);
            addInput(Liquids.arkycite, 20 / 60f);

            consumePower(120f / 60f);
            outputLiquids = LiquidStack.with(NHLiquids.xenFluid, 12 / 60f);

            drawer = new DrawMulti(
                    new DrawRegion() {{
                        suffix = "-base";
                    }},
                    new DrawLiquidTile() {{
                        drawLiquid = NHLiquids.xenFluid;
                    }},
                    new DrawRegion() {{
                        suffix = "-top";
                    }}
            );
        }};
        zetaCrafter = new RecipeGenericCrafter("zeta-crafter") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            craftTime = 60f;
            liquidCapacity = 12f;
            itemCapacity = 30;

            rotate = false;

            consumePower(120f / 60f);
            outputItem = new ItemStack(NHItems.zeta, 3);

            drawer = new DrawDefault();
        }};

        stampingFacility = new RecipeGenericCrafter("stamping-facility") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.titanium, 45,
                    NHItems.silicon, 60
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            health = 800;
            itemCapacity = 20;
            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawArcSmelt() {{
                        midColor = flameColor = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
                        flameRad /= 1.585f;
                        particleStroke /= 1.35f;
                        particleLen /= 1.25f;
                    }},
                    new DrawRegionCenterSymmetry("-rot")
            );
            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            consumePower(180f / 60f);
        }};
        processorPrinter = new RecipeGenericCrafter("processor-printer") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.titanium, 30,
                    NHItems.silicon, 45,
                    NHItems.tungsten, 30
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            health = 800;
            itemCapacity = 20;

            consumePower(240f / 60f);
            outputItems = with(NHItems.juniorProcessor, 3);

            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawScanLine() {{
                        scanLength = 24f;
                        scanAngle = 90f;
                        scanScl = 6f;
                        strokeRange = 6f;
                        colorFrom = Pal.techBlue;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        scanScl = 12f;
                        strokeRange = 12f;
                        colorFrom = Pal.techBlue;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        strokePlusScl = 2f;
                        scanScl = 12f;
                        strokeRange = 12f;
                        totalProgressMultiplier = 1.2f;
                        colorFrom = Pal.techBlue;
                    }},
                    new DrawGlowRegion() {{
                        suffix = "-glow";
                        rotate = true;
                        color = Pal.techBlue;
                    }},
                    new DrawRegionCenterSymmetry() {{
                        suffix = "-rot";
                    }}
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);
        }};
        crucibleFoundry = new RecipeGenericCrafter("crucible-foundry") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30,
                    NHItems.juniorProcessor, 50,
                    NHItems.tungsten, 40,
                    NHItems.zeta, 40
            ));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            health = 1500;
            itemCapacity = 20;
            liquidCapacity = 15f;

            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry("-rot"),
                    new DrawFlameRotated() {{
                        suffix = "-flame";
                    }}
            );

            updateEffect = craftEffect = new Effect(90, e -> randLenVectors(e.id, e.fin(), 10, 20f, (x, y, fin, fout) -> {
                color(Color.gray);
                alpha((0.5f - Math.abs(fin - 0.5f)) * 2f);
                Fill.circle(e.x + x, e.y + y, 0.5f + fout * 4f);
            }));
            consumePower(300 / 60f);
        }};
        crystallizer = new RecipeGenericCrafter("crystallizer") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 60, NHItems.juniorProcessor, 45, Items.tungsten, 30));

            size = 2;
            health = 800;

            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 1);

            craftTime = 120f;
            consumePower(480 / 60f);

            outputItems = with(NHItems.metalOxhydrigen, 4);

            itemCapacity = 30;
            liquidCapacity = 20f;
            health = 1600;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        suffix = "-rot";
                        x = 4;
                        y = 4;
                    }},
                    new DrawLiquidRegionRotated() {{
                        suffix = "-liquid";
                        drawLiquid = Liquids.water;
                        x = 4;
                        y = 4;
                    }},
                    new DrawLiquidRegionRotated() {{
                        suffix = "-liquid";
                        drawLiquid = NHLiquids.quantumLiquid;
                        x = 4;
                        y = 4;
                    }},
                    new DrawFlameRotated() {{
                        drawFlame = false;
                        flameX = 1;
                        flameY = 1;
                        flameColor = NHLiquids.quantumLiquid.color;
                        flameRadius *= 0.8f;
                        flameRadiusIn *= 0.8f;
                    }},
                    new DrawFlameRotated() {{
                        drawFlame = false;
                        flameX = 8;
                        flameY = 0;
                        flameColor = NHLiquids.quantumLiquid.color;
                        flameRadius *= 0.5f;
                        flameRadiusIn *= 0.5f;
                    }},
                    new DrawFlameRotated() {{
                        drawFlame = false;
                        flameX = 0;
                        flameY = 8;
                        flameColor = NHLiquids.quantumLiquid.color;
                        flameRadius *= 0.5f;
                        flameRadiusIn *= 0.5f;
                    }},
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-edge";
                        x = 4;
                        y = 4;
                    }}
            );
        }};
        zetaDissociator = new GenericCrafter("zeta-dissociator") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30,
                    NHItems.juniorProcessor, 40,
                    NHItems.carbide, 60,
                    NHItems.metalOxhydrigen, 45,
                    NHItems.zeta, 60
            ));

            quickRotate = true;
            invertFlip = true;

            size = 3;
            health = 900;

            itemCapacity = 30;
            liquidCapacity = 30f;

            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 4.5 / 60f, NHLiquids.zetaFluidNegative, 4.5 / 60f);
            liquidOutputDirections = new int[]{1, 3};

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawRegion("-base"),
                    new DrawRegionRotated() {{suffix = "-top-rot";}},
                    new DrawGlowRegion("-glow") {{color = NHItems.zeta.color;}}
            );
            craftEffect = updateEffect = NHFx.square(NHItems.zeta.color, 60, 6, 16, 3);

            consumePower(450 / 60f);
            consumeItems(ItemStack.with(NHItems.zeta, 4));
        }};
        surgeRefactor = new RecipeGenericCrafter("surge-refactor") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 90,
                    NHItems.tungsten, 60,
                    NHItems.metalOxhydrigen, 45
            ));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            health = 1500;

            ignoreLiquidFullness = true;

            itemCapacity = 30;


            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry() {{suffix = "-rot";}},
                    new DrawGlowRegion() {{
                        rotate = true;
                        suffix = "-glow";
                        color = NHItems.surgeAlloy.color;
                    }}
            );
            craftEffect = updateEffect = NHFx.polyCloud(Pal.accent, 60, 3, 16, 6);
            consumePower(480 / 60f);
        }};
        fabricSynthesizer = new RecipeGenericCrafter("fabric-synthesizer") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 90,
                    NHItems.juniorProcessor, 60,
                    NHItems.tungsten, 60,
                    NHItems.metalOxhydrigen, 45
            ));

            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            craftTime = 60f;

            outputItems = with(Items.phaseFabric, 2);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 3f / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;
            health = 1600;

            craftEffect = updateEffect = NHFx.polyCloud(Pal.accent, 60, 3, 16, 6);

            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawGlowRegion() {{
                        rotate = true;
                        suffix = "-glow";
                        color = NHItems.phaseFabric.color;
                    }},
                    new DrawScanLine() {{
                        scanLength = 24f;
                        scanAngle = 90f;
                        scanScl = 6f;
                        strokeRange = 8f;
                        colorFrom = NHItems.phaseFabric.color;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        scanScl = 12f;
                        strokeRange = 16f;
                        colorFrom = NHItems.phaseFabric.color;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        strokePlusScl = 2f;
                        scanScl = 12f;
                        strokeRange = 16f;
                        totalProgressMultiplier = 1.2f;
                        colorFrom = NHItems.phaseFabric.color;
                    }},
                    new DrawRegionCenterSymmetry() {{suffix = "-rot";}}
            );

            consumePower(480 / 60f);
        }};
        multipleSteelFactory = new RecipeGenericCrafter("multiple-steel-factory") {{
            requirements(Category.crafting,
                    with(NHItems.presstanium, 120, NHItems.juniorProcessor, 65, NHItems.metalOxhydrigen, 80, Items.surgeAlloy, 60));
            lightColor = NHItems.multipleSteel.color;
            updateEffect = EffectWrapper.wrap(Fx.smeltsmoke, lightColor);
            craftEffect = EffectWrapper.wrap(NHFx.square45_6_45, lightColor);
            outputItem = new ItemStack(NHItems.multipleSteel, 3);
            craftTime = 60f;
            itemCapacity = 30;
            liquidCapacity = 100;
            health = 600;
            size = 3;
            hasPower = hasItems =hasLiquids =true;
            drawer = new DrawDefault();

            consumePower(3f);

            rotate = false;
            drawer = new DrawRegion() {{
                buildingRotate = false;
            }};
        }};
        processorEncoder = new RecipeGenericCrafter("processor-encoder") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 90, Items.phaseFabric, 90, Items.carbide, 120, NHItems.zeta, 80));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            craftTime = 120f;
            itemCapacity = 20;

            consumePower(240f / 60f);
            outputItems = with(NHItems.seniorProcessor, 2);

            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 75;
                        particles = 15;
                    }},
                    new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 15;
                    }},
                    new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 60;
                        particles = 15;
                    }},
                    new DrawRegionCenterSymmetry() {{
                        suffix = "-rot";
                    }}
            );
        }};
        irdryonMixer = new RecipeGenericCrafter("irdryon-mixer") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 90, Items.phaseFabric, 60, NHItems.metalOxhydrigen, 120, NHItems.zeta, 80));

            size = 3;

            craftTime = 60f;
            liquidCapacity = 30f;
            itemCapacity = 30;
            consumePower(300 / 60f);

            addInput(Items.phaseFabric, 1, NHLiquids.xenFluid, 6 / 60f);

            outputLiquids = LiquidStack.with(NHLiquids.irdryonFluid, 8 / 60f);

            drawer = new DrawMulti(
                    new DrawRegion() {{
                        buildingRotate = false;
                    }}
            );
        }};
        irayrondFactory = new RecipeGenericCrafter("irayrond-factory") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 90, NHItems.seniorProcessor, 60, Items.phaseFabric, 120, NHItems.metalOxhydrigen, 45, NHItems.multipleSteel, 85));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 120f;
            consumePower(480 / 60f);

            outputItems = with(NHItems.irayrondPanel, 4);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 3f / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry() {{
                        suffix = "-rot";
                    }}
            );
        }};
        setonFactory = new RecipeGenericCrafter("seton-factory") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.juniorProcessor, 90, NHItems.zeta, 150, Items.surgeAlloy, 90, NHItems.metalOxhydrigen, 80, NHItems.multipleSteel, 65));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 120f;
            consumePower(480 / 60f);

            outputItems = with(NHItems.setonAlloy, 2);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidNegative, 3f / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry() {{
                        suffix = "-rot";
                    }}
            );
        }};
        ancimembraneConcentrator = new GenericCrafter("ancimembrane-concentrator") {{
            size = 3;

            lightRadius /= 2f;

            requirements(Category.crafting,
                    ItemStack.with(NHItems.seniorProcessor, 120, NHItems.multipleSteel, 90, NHItems.zeta, 45, NHItems.setonAlloy, 60));

            craftTime = 120f;

            health = 2200;
            armor = 12;
            craftEffect = NHFx.crossBlast(NHColor.ancient, 45f, 45f);
            craftEffect.lifetime *= 1.5f;
            updateEffect = NHFx.squareRand(NHColor.ancient, 5f, 15f);
            hasPower = hasItems = hasLiquids = true;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.quantumLiquid), new DrawRegion("-bottom-2"),
                    new DrawCrucibleFlame() {
                        {
                            flameColor = NHColor.ancient;
                            midColor = Color.valueOf("2e2f34");
                            circleStroke = 1.05f;
                            circleSpace = 2.65f;
                        }

                        @Override
                        public void draw(Building build) {
                            if (build.warmup() > 0f && flameColor.a > 0.001f) {
                                Lines.stroke(circleStroke * build.warmup());

                                float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
                                float a = alpha * build.warmup();

                                Draw.blend(Blending.additive);
                                Draw.color(flameColor, a);

                                float base = (Time.time / particleLife);
                                rand.setSeed(build.id);
                                for (int i = 0; i < particles; i++) {
                                    float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                                    float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                                    float len = particleRad * particleInterp.apply(fout);
                                    Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                                    Fill.square(
                                            build.x + Angles.trnsx(angle, len),
                                            build.y + Angles.trnsy(angle, len),
                                            particleSize * fin * build.warmup(), 45
                                    );
                                }

                                Draw.blend();

                                Draw.color(midColor, build.warmup());
                                Lines.square(build.x, build.y, (flameRad + circleSpace + si) * build.warmup(), 45);

                                Draw.reset();
                            }
                        }
                    },
                    new DrawDefault(),
                    new DrawGlowRegion() {{
                        color = NHColor.ancient;
                        layer = -1;
                        glowIntensity = 1.1f;
                        alpha = 1.1f;
                    }},
                    new DrawRotator(1f, "-top") {
                        @Override
                        public void draw(Building build) {
                            Drawf.spinSprite(rotator, build.x + x, build.y + y, DrawFunc.rotator_90(DrawFunc.cycle(build.totalProgress() * rotateSpeed, 0, craftTime), 0.15f));
                        }
                    }
            );

            itemCapacity = 60;
            liquidCapacity = 60f;

            consumePower(12);
            consumeItems(with(NHItems.irayrondPanel, 6));
            consumeLiquid(NHLiquids.irdryonFluid, 8 / 60f);
            outputItems = with(NHItems.ancimembrane, 3);
        }};
        upgradeSortFactory = new GenericCrafter("upgradeSort-factory") {{
            requirements(Category.crafting,
                    with(NHItems.setonAlloy, 160, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, NHItems.irayrondPanel, 90));
            updateEffect = NHStatusEffects.quantization.effect;
            craftEffect = new Effect(25f, e -> {
                Draw.color(NHColor.darkEnrColor);
                Angles.randLenVectors(e.id, 4, 24 * e.fout() * e.fout(), (x, y) -> {
                    Lines.stroke(e.fout() * 1.7f);
                    Lines.square(e.x + x, e.y + y, 2f + e.fout() * 6f);
                });
            });
            outputItem = new ItemStack(NHItems.nodexPlate, 2);
            craftTime = 120f;
            itemCapacity = 20;
            size = 3;
            hasPower = hasItems = true;
            drawer = new DrawPrinter(outputItem.item) {{
                printColor = NHColor.darkEnrColor;
                lightColor = Color.valueOf("#E1BAFF");
                moveLength = 4.2f;
                time = 25f;
            }};
            clipSize = size * tilesize * 2f;
            consumeItems(new ItemStack(NHItems.setonAlloy, 4), new ItemStack(NHItems.seniorProcessor, 4));
            consumePower(10f);
        }};
    }
}
