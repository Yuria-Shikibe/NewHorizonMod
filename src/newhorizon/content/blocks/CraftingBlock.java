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
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
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
import newhorizon.expand.block.production.factory.MultiBlockCrafter;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;

import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Color stampingArc, processorBlue;

    public static Block
            silicarCrusher, processorManuFactory, stampingFacility, heavyStampingFacility,
            processorPrinter,
            subCooler, hyperCooler, metalOxhydrigenRestructuror, photocatalystFactory, plasticator, crystallizer,
            phaseRestructuror, fabricSynthesizer, alloySmelter, surgeSynthesizer,
            particleActivator, plasmaActivator, fusionCoreEnergyFactory, thoriumTransmuter,
            rectificatior, phaseRectificatior,
            castingFoundry, crucibleFoundry, zetaFactory, multipleRollingMill, mixedRollingMill, heavyRollingMill, xenSeparator, processorEtchingFacility,
            irdryonFluidFactory, irdryonPhaseAscender, denseFactory, tandemFactory, processorCompactor,
            positivePhaseDecayer, negativePhaseDecayer,
            irayrondFactory, largeIrayrondFactory, nodexFactory, darkEnergyTrap, ancimembraneConcentrator, hadronCompositeBuilder, hyperProcessor;

    public static void load() {
        loadColors();
        silicarCrusher = new MultiBlockCrafter("silicar-crusher") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.silicar, 20
            ));

            size = 2;
            itemCapacity = 20;
            scaledHealth = 100f;
            craftTime = 60f;

            outputItems = ItemStack.with(NHItems.graphite, 1, NHItems.silicon, 1);
            consumeItems(ItemStack.with(NHItems.silicar, 2));

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawPistons() {{
                        sinScl = 8f;
                        sinMag = 2f;
                        sinOffset = 0;
                        lenOffset = -1f;
                    }},
                    new DrawRegion("-top")
            );

            craftEffect = NHFx.hugeSmokeGray;
            updateEffect = new Effect(80f, e -> {
                Fx.rand.setSeed(e.id);
                Draw.color(Color.lightGray, Color.gray, e.fin());
                Angles.randLenVectors(e.id, 4, 2.0F + 12.0F * e.fin(Interp.pow3Out), (x, y) -> 
                    Fill.circle(e.x + x, e.y + y, e.fout() * Fx.rand.random(1, 2.5f))
                );
            }).layer(Layer.blockOver + 1);
        }};

        processorManuFactory = new GenericCrafter("processor-manufactory") {{
            requirements(Category.crafting, with(
                    NHItems.graphite, 20,
                    NHItems.silicon, 20,
                    NHItems.hardLight, 10
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(0.5f);
            consumeItems(with(NHItems.silicon, 2));
            outputItems = with(NHItems.juniorProcessor, 1);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawRegion("-top"),
                    new DrawFlame() {
                        {
                            flameRadius *= 0.75f;
                            flameRadiusIn *= 0.75f;
                            flameRadiusScl *= 1.25f;
                            flameColor = processorBlue;
                        }

                        @Override
                        public void load(Block block) {
                            super.load(block);
                            top = Core.atlas.find(block.name + "-flame");
                        }
                    },
                    new DrawGlowRegion() {{
                        alpha = 0.5f;
                        suffix = "-glow";
                        color = processorBlue;
                    }}
            );

            craftEffect = NHFx.square(processorBlue, 60, 6, 16, 3);
            updateEffect = NHFx.square(processorBlue, 60, 2, 12, 3);
        }};

        processorPrinter = new MultiBlockCrafter("processor-printer") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.metalOxhydrigen, 30,
                    NHItems.presstanium, 20,
                    NHItems.juniorProcessor, 60,
                    NHItems.hardLight, 80
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(180f / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.quantumLiquid, 6 / 60f));
            consumeItems(with(NHItems.silicon, 2));
            outputItems = with(NHItems.juniorProcessor, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x4"),
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
                        speedMultiplier = 1.2f;
                        colorFrom = Pal.techBlue;
                    }},
                    new DrawGlowRegion() {{
                        suffix = "-glow";
                        rotate = true;
                        color = Pal.techBlue;
                    }},
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }}
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

        rectificatior = new GenericCrafter("rectificatior") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.graphite, 25,
                    NHItems.silicon, 30,
                    NHItems.titanium, 15,
                    NHItems.hardLight, 20
            ));

            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(60 / 60f);
            consumeItem(NHItems.silicon, 1);
            outputLiquids = LiquidStack.with(NHLiquids.photon, 3 / 60f);

        }};


        phaseRectificatior = new GenericCrafter("phase-rectificatior") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 3;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 100;
            craftTime = 120f;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.phaseFabric, 1));
            outputLiquids = LiquidStack.with(NHLiquids.photon, 12 / 60f);


            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawParticles() {{
                        particles = 15;
                        particleRad = 10f;
                        particleSize = 5f;
                        color = NHLiquids.photon.color.cpy();
                    }},
                    new DrawArcSmelt() {{
                        flameRad = 1.7f;
                        circleSpace = 3f;
                    }},
                    new DrawRegion(),
                    new DrawGlowRegion() {{
                        suffix = "-glow";
                        rotate = true;
                        color = NHLiquids.photon.color.cpy();
                    }}
            );

            ambientSound = Sounds.loopElectricHum;
            ambientSoundVolume = 0.08f;

            craftEffect = updateEffect = NHFx.squareLine(NHLiquids.photon.color.cpy(), 30, 3, 16, 4);

        }};

        stampingFacility = new GenericCrafter("stamping-facility") {{
            requirements(Category.crafting, with(
                    NHItems.titanium, 30,
                    NHItems.silicon, 20,
                    NHItems.hardLight, 15
            ));
            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(180f / 60f);
            consumeItems(with(NHItems.titanium, 2, NHItems.graphite, 1));
            outputItems = with(NHItems.presstanium, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawDefault(),
                    new DrawArcSmelt() {{
                        midColor = flameColor = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
                        flameRad = 0.3f;
                        circleSpace = 1.5f;
                        particleStroke = 1f;
                        particleRad = 4.5f;
                        particleLen = 1f;
                    }}
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 2);
        }};

        heavyStampingFacility = new MultiBlockCrafter("heavy-stamping-facility") {{
            requirements(Category.crafting, with(
                    NHItems.metalOxhydrigen, 40,
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 20,
                    NHItems.hardLight, 80
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.titanium, 2, NHItems.graphite, 1));
            consumeLiquid(NHLiquids.neutron, 6 / 60f);
            outputItems = with(NHItems.presstanium, 5);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x4"),
                    new DrawArcSmelt() {{
                        midColor = stampingArc;
                        flameColor = stampingArc;

                        flameRad /= 1.8f;
                        particleStroke /= 1.5f;
                        particleLen /= 1.5f;
                    }},
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }});

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            enableRotate();
        }};

        subCooler = new MultiBlockCrafter("sub-cooler") {{
            requirements(Category.crafting, with(
                    NHItems.titanium, 40,
                    NHItems.silicon, 40,
                    NHItems.hardLight, 60
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(1f);
            consumeItems(with(NHItems.titanium, 2));
            consumeLiquid(NHLiquids.water, 6 / 60f);
            outputLiquid = new LiquidStack(NHLiquids.cryofluid, 18 / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawLiquidTile(NHLiquids.water, 2f),
                    new DrawLiquidTile(NHLiquids.cryofluid, 2f),
                    new DrawRegion("-top"),
                    new DrawGlowRegion() {{
                        alpha = 0.5f;
                        suffix = "-glow";
                        color = Pal.techBlue;
                    }}
            );

            craftEffect = NHFx.square(processorBlue, 60, 6, 16, 3);
            updateEffect = NHFx.square(processorBlue, 60, 2, 12, 3);
        }};

        hyperCooler = new MultiBlockCrafter("hyper-cooler") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.tungsten, 60,
                    NHItems.presstanium, 50,
                    NHItems.silicon, 60
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            hasLiquids = true;
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 60f;


            consumePower(180f / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.xenFluid, 6 / 60f, NHLiquids.hydrazine, 12 / 60f));
            outputLiquid = new LiquidStack(NHLiquids.cryofluid, 30 / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x4"),
                    new DrawLiquidRegionRotated(NHLiquids.xenFluid) {{
                        suffix = "-liquid-xen";
                    }},
                    new DrawLiquidRegionRotated(NHLiquids.hydrazine) {{
                        suffix = "-liquid-hydrazine";
                    }},
                    new DrawLiquidRegionRotated(NHLiquids.cryofluid) {{
                        suffix = "-liquid-cryofluid";
                    }},
                    new DrawRotation() {{
                        suffix = "-top-rot";
                        drawType = DRAW_CENTRAL_SYMMETRY;
                    }}
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

        crystallizer = new MultiBlockCrafter("crystallizer") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 15,
                    NHItems.juniorProcessor, 25,
                    NHItems.silicon, 20,
                    NHItems.hardLight, 80
            ));
            addLink(2, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 1);

            size = 2;
            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(2.5f);
            consumeLiquid(NHLiquids.ammonia, 12 / 60f);
            outputItems = with(NHItems.metalOxhydrigen, 1);

            drawer = new DrawMulti(
                    new DrawRotation() {{
                        suffix = "-inner";
                        drawType = DrawRotation.DRAW_OBLIQUE;
                    }},
                    new DrawRotation() {{
                        suffix = "-outer";
                        drawType = DrawRotation.DRAW_Y_MIRROR;
                        xOffset = 12f;
                    }},
                    new DrawRotation() {{
                        suffix = "-outer";
                        drawType = DrawRotation.DRAW_Y_MIRROR;
                        xOffset = 12f;
                        rotOffset = 1;
                    }},
                    new DrawRotation() {{
                        suffix = "-edge";
                        drawType = DrawRotation.DRAW_ROTATED;
                        xOffset = yOffset = 8.25f;
                        layer = Layer.block + 1f;
                    }},
                    new DrawLiquidRegionRotated() {{
                        suffix = "-liquid";
                        drawLiquid = NHLiquids.ammonia;
                    }},
                    new DrawFlameRotated() {{
                        drawFlame = false;
                        flameColor = NHLiquids.ammonia.color.cpy();
                    }}
            );

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            enableRotate();
        }};

        crucibleFoundry = new MultiBlockCrafter("crucible-foundry") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.zeta, 125,
                    NHItems.tungsten, 200,
                    NHItems.carbide, 150,
                    NHItems.irayrondPanel, 75
            ));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            health = 900;
            armor = 6;
            itemCapacity = 20;
            liquidCapacity = 15f;
            craftTime = 60f;

            consumePower(300 / 60f);
            consumeItems(with(NHItems.graphite, 3, NHItems.tungsten, 1));
            outputItem = new ItemStack(NHItems.carbide, 1);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x5"),
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }},
                    new DrawFlameRotated() {{
                        suffix = "-flame";
                    }}
            );

            updateEffect = craftEffect = new Effect(90, e -> randLenVectors(e.id, e.fin(), 10, 20f, (x, y, fin, fout) -> {
                color(Color.gray);
                alpha((0.5f - Math.abs(fin - 0.5f)) * 2f);
                Fill.circle(e.x + x, e.y + y, 0.5f + fout * 4f);
            }));

            enableRotate();
        }};

        castingFoundry = new MultiBlockCrafter("casting-foundry") {{
            requirements(Category.crafting, with(
                    NHItems.multipleSteel, 90,
                    NHItems.plastanium, 60,
                    NHItems.metalOxhydrigen, 50,
                    NHItems.tungsten, 80
            ));
            addLink(2, -1, 2, 2, 1, 1, 3, 1, 1, -1, 2, 2, 1, 2, 1, 1, 3, 1);

            size = 3;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};
            scaledHealth = 150f;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(2.5f);
            consumeItems(with(NHItems.graphite, 3, NHItems.tungsten, 2));
            consumeLiquid(NHLiquids.hydrazine, 0.2f);
            outputItems = with(NHItems.carbide, 2);

            drawer = new DrawMulti(
                    new DrawRotation() {{
                        suffix = "-inner";
                        drawType = DrawRotation.DRAW_OBLIQUE;
                    }},
                    new DrawRotation() {{
                        suffix = "-outer";
                        drawType = DrawRotation.DRAW_Y_MIRROR;
                        xOffset = 20f;
                    }},
                    new DrawRotation() {{
                        suffix = "-outer";
                        drawType = DrawRotation.DRAW_Y_MIRROR;
                        xOffset = 20f;
                        rotOffset = 1;
                    }},
                    new DrawFlameRotated(Pal.slagOrange.cpy().lerp(Color.white, 0.2f)) {{
                        suffix = "-top";
                    }}
            );

            craftEffect = new MultiEffect(
                    Fx.smeltsmoke,
                    new RadialEffectRotated(Fx.surgeCruciSmoke, 5, 59 / 4f, 0f),
                    new RadialEffectRotated(Fx.surgeCruciSmoke, 5, 0, 59 / 4f)
            );
            updateEffect = Fx.smeltsmoke;

            enableRotate();
        }};

        zetaFactory = new MultiBlockCrafter("zeta-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.hardLight, 50,
                    NHItems.tungsten, 100,
                    NHItems.carbide, 50,
                    NHItems.metalOxhydrigen, 50
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            hasLiquids = true;
            health = 900;
            armor = 6;
            itemCapacity = 30;
            craftTime = 60f;

            consumePower(300f / 60f);
            consumeItems(with(NHItems.fissileMatter, 1, NHItems.fusionEnergy, 1));
            outputItems = with(NHItems.zeta, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x4"),
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }},
                    new DrawFlameRotated() {{
                        suffix = "-top";
                        flameColor = NHLiquids.zetaFluidPositive.color;
                    }}
            );

            craftEffect = Fx.formsmoke;
            updateEffect = NHFx.trailToGray;

            enableRotate();
        }};

        plasticator = new GenericCrafter("plasticator") {{
            requirements(Category.crafting, with(
                    NHItems.silicar, 70,
                    NHItems.juniorProcessor, 45,
                    NHItems.metalOxhydrigen, 60,
                    NHItems.hardLight, 100
            ));

            hasItems = true;
            size = 3;
            health = 640;
            craftTime = 120f;

            consumePower(4f);
            consumeItem(NHItems.titanium, 3);
            consumeLiquids(LiquidStack.with(NHLiquids.photon, 6 / 60f, NHLiquids.xenFluid, 9 / 60f));
            outputItem = new ItemStack(NHItems.plastanium, 5);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(NHLiquids.xenFluid) {{
                        alpha = 0.7f;
                    }},
                    new DrawCircles() {{
                        amount = 10;
                        color = NHLiquids.xenFluid.color.cpy();
                    }},
                    new DrawBubbles(NHLiquids.xenFluid.color.cpy().lerp(Pal.coalBlack, 0.1f)) {{
                        sides = 10;
                        recurrence = 3f;
                        spread = 6;
                        radius = 1.5f;
                        amount = 12;
                    }},
                    new DrawDefault(),
                    new DrawFade()
            );

            craftEffect = Fx.formsmoke;
            updateEffect = Fx.plasticburn;
        }};

        metalOxhydrigenRestructuror = new MultiBlockCrafter("metal-oxhydrigen-restructuror") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 65,
                    NHItems.plastanium, 50,
                    NHItems.silicon, 150,
                    NHItems.metalOxhydrigen, 90
            ));
            addLink(2, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 1, -1, 0, 1, -1, 1, 1, 0, -1, 1, 1, -1, 1);

            size = 2;
            hasLiquids = true;
            canMirror = true;
            rotateDraw = false;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(240f / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.ammonia, 18 / 60f, NHLiquids.proton, 6 / 60f));
            outputItems = with(NHItems.metalOxhydrigen, 3);


            Color drawColor = NHLiquids.ammonia.color.cpy().lerp(Pal.techBlue, 0.2f);
            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-bottom";
                    }},
                    /*new DrawCrucibleFlameRotated() {{
                        flameColor = midColor = drawColor;
                        flameRad = 2f;
                        circleSpace = 5f;
                        flameRadiusScl = 10f;
                    }},*/
                    new DrawArcSmeltRotated() {{
                        flameColor = midColor = drawColor;
                        flameRad = 2f;
                        circleSpace = 5f;
                        flameRadiusScl = 10f;
                    }},
                  /*  new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 75;
                        particles = 8;
                        color = drawColor;
                    }},
                    new DrawParticleFlow() {{
                        startX = 14f;
                        startY = 0;
                        endX = -14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 8;
                        color = drawColor;
                    }},
                    new DrawParticleFlow() {{
                        startX = 0;
                        startY = -14f;
                        endX = 0;
                        endY = 14f;
                        ignoreRot2_3 = true;
                        particleLife = 75;
                        particles = 8;
                        color = drawColor;
                    }},
                    new DrawParticleFlow() {{
                        startX = 0;
                        startY = 14f;
                        endX = 0;
                        endY = -14f;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 8;
                        color = drawColor;
                    }},*/
                    new DrawRegion("-top"));

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);
        }};

        photocatalystFactory = new MultiBlockCrafter("photocatalyst-factory") {{
            requirements(Category.crafting, with(
                    NHItems.tungsten, 50,
                    NHItems.juniorProcessor, 30,
                    NHItems.silicon, 80,
                    NHItems.metalOxhydrigen, 30
            ));

            size = 3;
            hasLiquids = true;
            canMirror = true;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 30f;
            craftTime = 60f;

            consumePower(60 / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.ammonia, 12 / 60f, NHLiquids.photon, 3 / 60f));
            outputLiquids = LiquidStack.with(NHLiquids.hydrazine, 6 / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidRegionRotated(NHLiquids.ammonia) {{
                        suffix = "-liquid-ammonia";
                    }},
                    new DrawLiquidRegionRotated(NHLiquids.photon) {{
                        suffix = "-liquid-photon";
                    }},
                    new DrawLiquidRegionRotated(NHLiquids.hydrazine) {{
                        suffix = "-liquid-hydrazine";
                    }},
                    new DrawRegion()
            );

            craftEffect = updateEffect = NHFx.square(NHLiquids.photon.color, 60, 6, 16, 3);
        }};

        fusionCoreEnergyFactory = new GenericCrafter("fusion-core-energy-factory") {{
            requirements(Category.crafting, with(
                    NHItems.thorium, 135,
                    NHItems.presstanium, 100,
                    NHItems.plastanium, 60,
                    NHItems.juniorProcessor, 100
            ));

            size = 3;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(6f);
            consumeLiquids(LiquidStack.with(NHLiquids.xenFluid, 12 / 60f, NHLiquids.cryofluid, 6 / 60f));
            outputItem = new ItemStack(NHItems.fusionEnergy, 1);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(),
                    new DrawDefault(),
                    new DrawGlowRegion() {{
                        color = NHItems.zeta.color;
                    }}
            );

            craftEffect = Fx.smeltsmoke;
            updateEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, NHItems.fusionEnergy.color.cpy().a(0.53f));
        }};

        thoriumTransmuter = new GenericCrafter("thorium-transmuter") {{
            requirements(Category.crafting, with(
                    NHItems.tungsten, 120,
                    NHItems.thorium, 125,
                    NHItems.multipleSteel, 75,
                    NHItems.juniorProcessor, 60
            ));

            size = 4;
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(1f);
            consumeItems(with(NHItems.thorium, 3));
            consumeLiquids(LiquidStack.with(NHLiquids.proton, 4 / 60f, NHLiquids.neutron, 4 / 60f));
            outputItems = with(NHItems.fissileMatter, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-4x4"),
                    new DrawRegion()
            );
        }};

        particleActivator = new GenericCrafter("particle-activator") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.titanium, 30,
                    NHItems.graphite, 20,
                    NHItems.silicon, 20,
                    NHItems.hardLight, 40
            ));

            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(60 / 60f);
            consumeItems(with(NHItems.graphite, 3));
            outputLiquids = LiquidStack.with(NHLiquids.xenFluid, 6 / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawLiquidTile(NHLiquids.xenFluid) {{
                        alpha = 0.5f;
                    }},
                    new DrawArcSmelt(),
                    new DrawParticles() {{
                        color = NHLiquids.xenFluid.color.cpy().lerp(Pal.techBlue, 0.3f).lerp(Pal.coalBlack, 0.05f);
                        particles = 10;
                        particleSize = 2;
                    }},
                    new DrawDefault()
            );
        }};

        plasmaActivator = new GenericCrafter("plasma-activator") {{
            requirements(Category.crafting, with(
                    NHItems.plastanium, 50,
                    NHItems.multipleSteel, 80,
                    NHItems.carbide, 80
            ));

            size = 3;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 20f;
            craftTime = 120f;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.graphite, 2, NHItems.thorium, 1));
            outputLiquids = LiquidStack.with(NHLiquids.xenFluid, 15 / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(NHLiquids.xenFluid, 0.2f) {{
                        alpha = 0.7f;
                    }},
                    new DrawDefault()
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);
        }};

        multipleRollingMill = new GenericCrafter("multiple-rolling-mill") {{
            requirements(Category.crafting, with(
                    NHItems.metalOxhydrigen, 60,
                    NHItems.presstanium, 40,
                    NHItems.titanium, 80
            ));

            size = 3;
            itemCapacity = 30;
            craftTime = 120f;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.presstanium, 2, NHItems.metalOxhydrigen, 1));
            outputItem = new ItemStack(NHItems.multipleSteel, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawRegion()
            );
        }};

        mixedRollingMill = new GenericCrafter("mixed-rolling-mill") {{
            requirements(Category.crafting, with(
                    NHItems.multipleSteel, 90,
                    NHItems.plastanium, 75,
                    NHItems.metalOxhydrigen, 80,
                    NHItems.silicon, 150
            ));

            size = 3;
            itemCapacity = 30;
            craftTime = 240f;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.graphite, 5, NHItems.tungsten, 3));
            outputItem = new ItemStack(NHItems.multipleSteel, 4);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawCrucibleFlame() {{
                        flameColor = Pal.techBlue.cpy().lerp(Color.white, 0.2f);
                        midColor = Pal.techBlue.cpy().lerp(Color.white, 0.4f);
                    }},
                    new DrawScanLine() {{
                        colorFrom = Pal.techBlue.cpy().lerp(Color.white, 0.2f);
                        scanAngle = 45f;
                        lineStroke = 0.78f;
                        scanLength = 12f;
                        speedMultiplier = 0.8f;
                    }},
                    new DrawScanLine() {{
                        colorFrom = Pal.techBlue.cpy().lerp(Color.white, 0.2f);
                        scanAngle = 45f;
                        lineStroke = 0.78f;
                        scanLength = 12f;
                        speedMultiplier = 1.21f;
                    }},
                    new DrawScanLine() {{
                        colorFrom = Pal.techBlue.cpy().lerp(Color.white, 0.2f);
                        scanAngle = 135f;
                        lineStroke = 0.78f;
                        scanLength = 12f;
                        startOffset = colorLerpScl * Mathf.pi;
                        speedMultiplier = 0.67f;
                    }},
                    new DrawScanLine() {{
                        colorFrom = Pal.techBlue.cpy().lerp(Color.white, 0.2f);
                        scanAngle = 135f;
                        lineStroke = 0.78f;
                        scanLength = 12f;
                        startOffset = colorLerpScl * Mathf.pi;
                        speedMultiplier = 1.53f;
                    }},
                    new DrawRegion() {{
                        suffix = "-top";
                    }}
            );
        }};

        heavyRollingMill = new MultiBlockCrafter("heavy-rolling-mill") {{
            requirements(Category.crafting, with(
                    NHItems.irayrondPanel, 40,
                    NHItems.surgeAlloy, 60,
                    NHItems.multipleSteel, 150,
                    NHItems.presstanium, 175
            ));
            addLink(2, 0, 1, 2, 1, 1, 2, 2, 1, 0, 2, 1, 1, 2, 1, -2, 0, 1, -2, -1, 1, -2, -2, 1, -1, -2, 1, 0, -2, 1);

            size = 3;
            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};
            scaledHealth = 100f;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.surgeAlloy, 2, NHItems.carbide, 2));
            consumeLiquids(LiquidStack.with(NHLiquids.cryofluid, 6 / 60f));
            outputItem = new ItemStack(NHItems.multipleSteel, 5);

            drawer = new DrawMulti(
                    new DrawRegionRotated("-rot"),
                    new DrawFlameRotated() {{
                        suffix = "-top";
                        flameColor = Pal.techBlue;
                    }}
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 3, 16, 3);

            enableRotate();
        }};

        phaseRestructuror = new GenericCrafter("phase-restructuror") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.thorium, 70,
                    NHItems.juniorProcessor, 60,
                    NHItems.metalOxhydrigen, 80
            ));

            size = 3;
            health = 800;
            armor = 4;
            itemCapacity = 20;
            craftTime = 60f;

            outputItems = with(NHItems.phaseFabric, 1);
            consumePower(60 / 60f);
            consumeItems(with(NHItems.silicon, 3, NHItems.thorium, 1));

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawSpikes(){{
                        color = Color.valueOf("ffd59e");
                        stroke = 1.3f;
                        layers = 2;
                        amount = 10;
                        rotateSpeed = 0.5f;
                        layerSpeed = -0.9f;
                    }},
                    new DrawMultiWeave(){{
                        glowColor = new Color(1f, 0.4f, 0.4f, 0.8f);
                    }},
                    new DrawScanLine() {{
                        scanScl = 10f;
                        colorFrom = Pal.accent;
                        colorTo = Color.white;
                        alpha = 0.67f;
                        lineStroke = 0.786f;
                    }},
                    new DrawScanLine() {{
                        scanAngle = 180f;
                        scanScl = 10f;
                        colorFrom = Pal.accent;
                        colorTo = Color.white;
                        alpha = 0.67f;
                        lineStroke = 0.786f;
                    }},
                    new DrawDefault(),
                    new DrawGlowRegion("-vents"){{
                        color = new Color(1f, 0.4f, 0.3f, 1f);
                    }}
            );

            ambientSound = Sounds.loopTech;
            ambientSoundVolume = 0.04f;
        }};

        fabricSynthesizer = new MultiBlockCrafter("fabric-synthesizer") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.thorium, 175,
                    NHItems.juniorProcessor, 125,
                    NHItems.phaseFabric, 150,
                    NHItems.irayrondPanel, 75
            ));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            itemCapacity = 30;
            health = 1200;
            armor = 8;
            craftTime = 60f;

            consumePower(480 / 60f);
            consumeItems(with(NHItems.silicon, 2, NHItems.phaseFabric, 5));
            consumeLiquids(LiquidStack.with(NHLiquids.photon, 6 / 60f));
            outputItems = with(NHItems.phaseFabric, 6);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x5"),
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
                        speedMultiplier = 1.2f;
                        colorFrom = NHItems.phaseFabric.color;
                    }},
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }}
            );

            craftEffect = updateEffect = NHFx.polyCloud(Pal.accent, 60, 3, 16, 6);

            enableRotate();
        }};

        alloySmelter = new GenericCrafter("alloy-smelter") {{
            requirements(Category.crafting, with(
                    NHItems.multipleSteel, 100,
                    NHItems.plastanium, 90,
                    NHItems.tungsten, 80
            ));

            size = 3;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(4f);
            consumeItems(with(NHItems.titanium, 2, NHItems.silicon, 2));
            consumeLiquids(LiquidStack.with(NHLiquids.xenFluid, 12 / 60f));
            outputItem = new ItemStack(NHItems.surgeAlloy, 1);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(),
                    new DrawDefault()
            );

            craftEffect = Fx.smeltsmoke;
        }};

        surgeSynthesizer = new MultiBlockCrafter("surge-synthesizer") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.zeta, 150,
                    NHItems.surgeAlloy, 145,
                    NHItems.irayrondPanel, 125,
                    NHItems.tungsten, 225
            ));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            health = 1200;
            armor = 8;
            itemCapacity = 30;
            craftTime = 180f;

            consumePower(480 / 60f);
            consumeItems(with(NHItems.titanium, 3, NHItems.silicon, 4, NHItems.zeta, 2));
            consumeLiquids(LiquidStack.with(NHLiquids.xenFluid, 12 / 60f));
            outputItem = new ItemStack(NHItems.surgeAlloy, 3);

            drawer = new DrawMulti(
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }},
                    new DrawGlowRegion() {{
                        rotate = true;
                        suffix = "-glow";
                        color = NHItems.surgeAlloy.color;
                    }}
            );

            craftEffect = updateEffect = NHFx.polyCloud(Pal.accent, 60, 3, 16, 6);

            enableRotate();
        }};

        xenSeparator = new GenericCrafter("xen-separator") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.multipleSteel, 30,
                    NHItems.metalOxhydrigen, 70,
                    NHItems.hardLight, 100
            ));

            size = 3;
            rotate = true;
            invertFlip = true;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(60 / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.xenFluid, 6 / 60f));
            outputLiquids = LiquidStack.with(NHLiquids.neutron, 3 / 60f, NHLiquids.proton, 3 / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(NHLiquids.xenFluid, 2f),
                    new DrawRegion(),
                    new DrawLiquidOutputs()
            );

            regionRotated1 = 3;
            liquidOutputDirections = new int[]{1, 3};
        }};

        processorEtchingFacility = new MultiBlockCrafter("processor-etching-facility") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                            NHItems.surgeAlloy, 125,
                            NHItems.phaseFabric, 110,
                            NHItems.juniorProcessor, 250,
                            NHItems.zeta, 125,
                            NHItems.carbide, 90
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            itemCapacity = 20;
            health = 1500;
            armor = 10;
            craftTime = 60;

            consumePower(240f / 60f);
            consumeItems(with(NHItems.surgeAlloy, 2, NHItems.juniorProcessor, 1));
            consumeLiquids(LiquidStack.with(NHLiquids.quantumLiquid, 12 / 60f));
            outputItems = with(NHItems.seniorProcessor, 1);

            Color c = NHItems.seniorProcessor.color.cpy().lerp(Pal.slagOrange, 0.3f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x4"),
                    new DrawScanLine() {{
                        scanLength = 24f;
                        scanAngle = 90f;
                        scanScl = 6f;
                        strokeRange = 6f;
                        colorFrom = c;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        scanScl = 12f;
                        strokeRange = 12f;
                        colorFrom = c;
                    }},
                    new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 75;
                        particles = 15;
                        color = c;
                    }},
                    new DrawParticleFlow() {{
                        startX = 14f;
                        startY = 0;
                        endX = -14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 15;
                        color = c;
                    }},
                    new DrawCrucibleFlameRotated() {{
                        flameX = 28 / 4f;
                        particles = 10;
                    }},
                    new DrawCrucibleFlameRotated() {{
                        flameX = -28 / 4f;
                        particles = 10;
                    }},
                    new DrawRegionFlip("-rot")
            );

            updateEffect = craftEffect = new MultiEffect(
                    NHFx.square(NHItems.seniorProcessor.color, 60, 5, 30, 5)
            );

            enableRotate();
        }};


        irdryonFluidFactory = new GenericCrafter("irdryon-fluid-factory") {{
            requirements(Category.crafting, with(
                        NHItems.seniorProcessor, 30,
                        NHItems.surgeAlloy, 75,
                        NHItems.irayrondPanel, 35,
                        NHItems.metalOxhydrigen, 100
            ));

            size = 2;
            itemCapacity = 20;
            craftTime = 60;

            consumePower(4f);
            consumeLiquids(LiquidStack.with(NHLiquids.cryofluid, 6 / 60f, NHLiquids.hydrazine, 12 / 60f));
            outputLiquid = new LiquidStack(NHLiquids.irdryonFluid, 6f / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawLiquidTile(NHLiquids.irdryonFluid),
                    new DrawDefault(),
                    new DrawPistons() {{
                        angleOffset = 45;
                        sinMag = 4f;
                        sinScl = 5f;
                        sides = 4;
                        sideOffset = 0;
                    }},
                    new DrawRegion("-top")
            );

            craftEffect = Fx.smeltsmoke;
        }};

        irdryonPhaseAscender = new GenericCrafter("irdryon-phase-ascender") {{

            requirements(Category.crafting, with(
                        NHItems.setonAlloy, 20,
                        NHItems.phaseFabric, 75,
                        NHItems.multipleSteel, 80,
                        NHItems.seniorProcessor, 25
            ));

            size = 2;
            itemCapacity = 20;
            craftTime = 60;


            consumePower(4f);
            consumeItems(new ItemStack(NHItems.phaseFabric, 2));
            consumeLiquids(LiquidStack.with(NHLiquids.xenFluid, 6 / 60f));
            outputLiquid = new LiquidStack(NHLiquids.irdryonFluid, 12f / 60f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawLiquidTile(NHLiquids.irdryonFluid),
                    new DrawRegion("-top"),
                    new DrawRotator(){{
                        rotateSpeed = -4;
                    }}
            );

            craftEffect = Fx.smeltsmoke;
        }};

        irayrondFactory = new GenericCrafter("irayrond-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.multipleSteel, 120,
                    NHItems.surgeAlloy, 75,
                    NHItems.tungsten, 90,
                    NHItems.presstanium, 45
            ));

            size = 4;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 60f;

            consumePower(60 / 60f);
            consumeItems(ItemStack.with(NHItems.presstanium, 5, NHItems.surgeAlloy, 1));
            outputItems = with(NHItems.irayrondPanel, 2);

            drawer = new DrawMulti(
                    new DrawDefault()
            );
        }};

        largeIrayrondFactory = new MultiBlockCrafter("large-irayrond-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.presstanium, 200,
                    NHItems.irayrondPanel, 135,
                    NHItems.surgeAlloy, 140,
                    NHItems.multipleSteel, 150
            ));
            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            size = 4;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            craftTime = 120f;

            consumePower(60 / 60f);
            consumeItems(ItemStack.with(NHItems.surgeAlloy, 2, NHItems.multipleSteel, 5));
            consumeLiquid(NHLiquids.irdryonFluid, 12 / 60f);
            outputItems = with(NHItems.irayrondPanel, 6);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-4x6"),
                    new DrawRegionFlip("-rot")
            );

            enableRotate();
        }};

        denseFactory = new GenericCrafter("dense-factory") {{
            requirements(Category.crafting, with(
                    NHItems.irayrondPanel, 75,
                    NHItems.carbide, 80,
                    NHItems.plastanium, 120,
                    NHItems.metalOxhydrigen, 120
            ));

            size = 3;
            hasLiquids = true;
            health = 500;
            itemCapacity = 24;
            craftTime = 60;

            consumePower(12f);
            consumeItems(ItemStack.with(NHItems.plastanium, 2, NHItems.tungsten, 5));
            consumeLiquid(NHLiquids.irdryonFluid, 6 / 60f);
            outputItem = new ItemStack(NHItems.setonAlloy, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawPlasma(),
                    new DrawLiquidRegionRotated(NHLiquids.irdryonFluid) {{
                        suffix = "-liquid-irdryon-fluid";
                    }},
                    new DrawDefault()
            );

            craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 4f + e.finpow() * 14f, (x, y) -> {
                Draw.color(NHLiquids.irdryonFluid.color);
                Fill.square(e.x + x, e.y + y, e.fout() * 3f);
            }));
        }};

        tandemFactory = new MultiBlockCrafter("tandem-factory") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(
                            NHItems.multipleSteel, 120,
                            NHItems.setonAlloy, 80,
                            NHItems.irayrondPanel, 120,
                            NHItems.seniorProcessor, 50,
                            NHItems.zeta, 150
                    ));

            size = 3;

            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            craftTime = 120f;
            itemCapacity = 30;
            health = 1800;
            armor = 12;
            consumePower(480 / 60f);

            consumeItems(with(NHItems.thorium, 5, NHItems.zeta, 2, NHItems.carbide, 4));
            outputItems = with(NHItems.setonAlloy, 4);

            drawer = new DrawMulti(
                    new DrawRegionFlip("-base"),
                    new DrawRegion("-piston-1") {
                        @Override
                        public void draw(Building build) {
                            Tmp.v1.setAngle(90f + build.rotdeg()).setLength(Mathf.absin(build.totalProgress(), 12f, 3));
                            Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, region.width / 4f, region.height / 4f);
                        }
                    },
                    new DrawRegion("-piston-1") {
                        @Override
                        public void draw(Building build) {
                            Tmp.v1.setAngle(270f + build.rotdeg()).setLength(Mathf.absin(build.totalProgress(), 12f, 3));
                            Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, region.width / 4f, -region.height / 4f);
                        }
                    },
                    new DrawRegion("-piston-2") {
                        @Override
                        public void draw(Building build) {
                            Tmp.v1.setAngle(135f + build.rotdeg()).setLength(Mathf.absin(build.totalProgress(), 12f, 3));
                            Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, region.width / 4f, region.height / 4f);
                        }
                    },
                    new DrawRegion("-piston-2") {
                        @Override
                        public void draw(Building build) {
                            Tmp.v1.setAngle(45f + build.rotdeg()).setLength(Mathf.absin(build.totalProgress(), 12f, 3));
                            Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, -region.width / 4f, region.height / 4f);
                        }
                    },
                    new DrawRegion("-piston-2") {
                        @Override
                        public void draw(Building build) {
                            Tmp.v1.setAngle(225f + build.rotdeg()).setLength(Mathf.absin(build.totalProgress(), 12f, 3));
                            Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, region.width / 4f, -region.height / 4f);
                        }
                    },
                    new DrawRegion("-piston-2") {
                        @Override
                        public void draw(Building build) {
                            Tmp.v1.setAngle(315f + build.rotdeg()).setLength(Mathf.absin(build.totalProgress(), 12f, 3));
                            Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, -region.width / 4f, -region.height / 4f);
                        }
                    },
                    new DrawRegionFlip("-top")
            );

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            enableRotate();
        }};


        processorCompactor = new GenericCrafter("processor-compactor"){{
            requirements(Category.crafting, with(
                    NHItems.setonAlloy, 150,
                    NHItems.irayrondPanel, 100,
                    NHItems.seniorProcessor, 80,
                    NHItems.presstanium, 120
            ));

            size = 4;
            health = 1500;
            armor = 4;
            itemCapacity = 50;
            craftTime = 120;

            consumePower(25);
            consumeItems(with(NHItems.silicon, 8, NHItems.surgeAlloy, 4));
            consumeLiquid(NHLiquids.irdryonFluid, 12 / 60f);
            outputItems = ItemStack.with(NHItems.juniorProcessor,16, NHItems.seniorProcessor, 8);

            Color senior = Pal.ammo.cpy().lerp(Color.red, 0.63f).lerp(Color.white, 0.2f);
            Color junior = Pal.bulletYellowBack;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawScanLine(){{
                        colorFrom = junior;
                        scanLength = 73 / 4f;
                        scanScl = 15f;
                        scanAngle = 90;
                        lineStroke -= 0.15f;
                        speedMultiplier = 1.25f;
                        startOffset = Mathf.random() * 5f;
                    }},
                    new DrawScanLine(){{
                        colorFrom = junior;
                        scanLength = 73 / 4f;
                        scanScl = 15f;
                        scanAngle = 0;
                        speedMultiplier = 1.55f;
                        startOffset = Mathf.random() * 5f;
                    }},
                    new DrawScanLine(){{
                        colorFrom = senior;
                        scanLength = 73 / 4f;
                        scanScl = 15f;
                        scanAngle = 90;
                        speedMultiplier = 1.35f;
                        startOffset = Mathf.random() * 5f;
                    }},
                    new DrawScanLine(){{
                        colorFrom = senior;
                        scanLength = 73 / 4f;
                        scanScl = 8f;
                        scanAngle = 0;
                        lineStroke -= 0.15f;
                        speedMultiplier = 1.65f;
                        startOffset = Mathf.random() * 5f;
                    }},
                    new DrawRegion("-mid"),
                    new DrawLiquidTile(NHLiquids.irdryonFluid, 54 / 4f),
                    new DrawDefault(),
                    new DrawGlowRegion("-glow1"){{
                        color = junior;
                    }},
                    new DrawGlowRegion("-glow2"){{
                        color = junior;
                    }},
                    new DrawGlowRegion("-glow3"){{
                        color = junior;
                    }}
            );

            updateEffectChance = 0.07f;
            updateEffect = new Effect(30f, e -> {
                Rand rand = NHFunc.rand(e.id);
                Draw.color(senior, Color.white, e.fout() * 0.66f);
                Draw.alpha(0.55f * e.fout() + 0.5f);
                Angles.randLenVectors(e.id, 2, 4f + e.finpow() * 16f, (x, y) -> Fill.square(e.x + x, e.y + y, e.fout() * rand.random(2, 4)));

                Draw.color(junior, Color.white, e.fout() * 0.66f);
                Draw.alpha(0.55f * e.fout() + 0.5f);
                Angles.randLenVectors(e.id + 12, 4, 4f + e.finpow() * 16f, (x, y) -> Fill.square(e.x + x, e.y + y, e.fout() * rand.random(1, 3)));
            });
        }};

        positivePhaseDecayer = new MultiBlockCrafter("positive-phase-decayer") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.carbide, 150,
                    NHItems.multipleSteel, 200,
                    NHItems.surgeAlloy, 175,
                    NHItems.seniorProcessor, 100,
                    NHItems.plastanium, 225
            ));
            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            size = 4;
            hasLiquids = true;
            health = 3000;
            armor = 10f;
            itemCapacity = 45;
            craftTime = 60;

            consumeItems(ItemStack.with(NHItems.fusionEnergy, 2, NHItems.zeta, 1));
            consumeLiquid(NHLiquids.proton, 3 / 60f);
            outputItems = with(NHItems.thermoCorePositive, 3);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-4x6"),
                    new DrawLiquidRegionRotated(NHLiquids.proton) {{
                        suffix = "-liquid-proton";
                    }},
                    new DrawRegionFlip("-rot")
            );

            lightColor = NHItems.thermoCorePositive.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);

            enableRotate();
        }};

        negativePhaseDecayer = new MultiBlockCrafter("negative-phase-decayer") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.carbide, 150,
                    NHItems.multipleSteel, 200,
                    NHItems.surgeAlloy, 175,
                    NHItems.seniorProcessor, 100,
                    NHItems.plastanium, 225
            ));
            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            size = 4;
            hasLiquids = true;
            health = 3000;
            armor = 10f;
            itemCapacity = 45;
            liquidCapacity = 45;

            craftTime = 60;

            consumeItems(ItemStack.with(NHItems.fissileMatter, 2, NHItems.zeta, 1));
            consumeLiquid(NHLiquids.neutron, 3 / 60f);
            outputItems = with(NHItems.thermoCoreNegative, 3);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-4x6"),
                    new DrawLiquidRegionRotated(NHLiquids.neutron) {{
                        suffix = "-liquid-neutron";
                    }},
                    new DrawRegionFlip("-rot")
            );

            lightColor = NHItems.thermoCoreNegative.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);

            enableRotate();
        }};

        nodexFactory = new GenericCrafter("nodex-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.setonAlloy, 150,
                    NHItems.irayrondPanel, 100,
                    NHItems.seniorProcessor, 80,
                    NHItems.presstanium, 120)
            );

            size = 3;
            health = 2100;
            armor = 14;
            itemCapacity = 40;
            craftTime = 60f;

            consumePower(1600 / 60f);
            consumeItems(ItemStack.with(NHItems.seniorProcessor, 1, NHItems.setonAlloy, 2));
            outputItems = with(NHItems.nodexPlate, 2);

            drawer = new DrawPrinter(NHItems.nodexPlate) {{
                printColor = NHColor.darkEnrColor;
                lightColor = Color.valueOf("#E1BAFF");
                moveLength = 4.2f;
                time = 25f;
            }};

            craftEffect = new Effect(25f, e -> {
                Draw.color(NHColor.darkEnrColor);
                Angles.randLenVectors(e.id, 4, 24 * e.fout() * e.fout(), (x, y) -> {
                    Lines.stroke(e.fout() * 1.7f);
                    Lines.square(e.x + x, e.y + y, 2f + e.fout() * 6f);
                });
            });
            updateEffect = NHStatusEffects.quantization.effect;

//            clipSize = size * tilesize * 2f;
        }};
        ancimembraneConcentrator = new GenericCrafter("ancimembrane-concentrator") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.setonAlloy, 125,
                    NHItems.irayrondPanel, 80,
                    NHItems.seniorProcessor, 60,
                    NHItems.zeta, 90)
            );

            size = 3;
            health = 2100;
            armor = 14;
            itemCapacity = 40;
            craftTime = 60f;

            consumePower(1600 / 60f);
            consumeItems(ItemStack.with(NHItems.multipleSteel, 2, NHItems.irayrondPanel, 3));
            consumeLiquid(NHLiquids.quantumLiquid, 12 / 60f);
            outputItems = with(NHItems.ancimembrane, 2);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(NHLiquids.quantumLiquid),
                    new DrawRegion("-bottom-2"),
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

            craftEffect = NHFx.crossBlast(NHColor.ancient, 45f, 45f);
            craftEffect.lifetime *= 1.5f;
            updateEffect = NHFx.squareRand(NHColor.ancient, 5f, 15f);
            lightRadius /= 2f;
        }};

        darkEnergyTrap = new MultiBlockCrafter("dark-energy-trap"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.nodexPlate, 90,
                    NHItems.presstanium, 150,
                    NHItems.multipleSteel, 125,
                    NHItems.seniorProcessor, 80));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1, -1, 2, 1, 0, 2, 1, 1, 2, 1, -1, -2, 1, 0, -2, 1, 1, -2, 1);

            size = 3;
            health = 600;
            itemCapacity = 15;
            craftTime = 120f;

            consumePower(600 / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.neutron, 6 / 60f, NHLiquids.proton, 6 / 60f, NHLiquids.antiMatter, 3 / 60f));
            outputItems = with(NHItems.darkEnergy, 2);

            drawer = new DrawDefault();
        }};

        hadronCompositeBuilder = new MultiBlockCrafter("hadron-composite-builder") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.nodexPlate, 150,
                    NHItems.surgeAlloy, 175,
                    NHItems.multipleSteel, 175,
                    NHItems.seniorProcessor, 120,
                    NHItems.ancimembrane, 75
            ));
            addLink(-1, 2, 2, 1, 2, 1, 1, 3, 1, -1, -3, 2, 1, -2, 1, 1, -3, 1, -3, -1, 2, -2, 1, 1, -3, 1, 1, 2, -1, 2, 2, 1, 1, 3, 1, 1);

            size = 3;
            hasLiquids = true;
            itemCapacity = 30;
            craftTime = 120;

            consumePower(1600 / 60f);
            consumeItems(ItemStack.with(NHItems.nodexPlate, 3, NHItems.zeta, 4));
            consumeLiquid(NHLiquids.neutron, 12 / 60f);
            outputItems = with(NHItems.hadronicomp, 2);

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(NHLiquids.neutron),
                    new DrawRegion()
            );

            clipSize = size * tilesize * 2f;
        }};

        hyperProcessor = new MultiBlockCrafter("hyper-processor") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.nodexPlate, 150,
                    NHItems.surgeAlloy, 175,
                    NHItems.multipleSteel, 175,
                    NHItems.seniorProcessor, 120,
                    NHItems.ancimembrane, 75
            ));
            addLink( -3, 2, 1, -3, 3, 1, -2, 3, 1, 3, 2, 1, 3, 3, 1, 2, 3, 1, -3, -2, 1, -3, -3, 1, -2, -3, 1, 3, -2, 1, 3, -3, 1, 2, -3, 1);

            size = 5;
            hasLiquids = true;
            itemCapacity = 30;
            craftTime = 120;

            consumePower(1600 / 60f);
            consumeItems(ItemStack.with(NHItems.ancimembrane, 3));
            consumeLiquids(LiquidStack.with(NHLiquids.proton, 12 / 60f, NHLiquids.irdryonFluid, 12 / 60f));
            outputItems = with(NHItems.hyperProcessor, 2);
        }};

                /*
        stampingPresser = new MultiBlockCrafter("stamping-presser") {{
            requirements(Category.crafting, with(
                    NHItems.hardLight, 60,
                    NHItems.titanium, 45,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;

            craftTime = 120f;
            outputItems = with(NHItems.presstanium, 1);

            consumeItems(with(NHItems.titanium, 2, NHItems.graphite, 1));
            consumePower(0.5f);

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x2"),
                    new DrawArcSmelt() {{
                        midColor = stampingArc;
                        flameColor = stampingArc;

                        flameRad /= 1.8f;
                        particleStroke /= 1.5f;
                        particleLen /= 1.5f;
                    }},
                    new DrawRegion("-top")
            );

            craftEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);
            updateEffect = NHFx.square(Pal.techBlue, 60, 2, 12, 3);
        }};
        */

        /*
        sheetPresser = new GenericCrafter("sheet-presser"){{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.titanium, 45,
                    NHItems.silicon, 60
            ));

            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
        }};

        manufactory = new GenericCrafter("manufactory"){{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.titanium, 45,
                    NHItems.silicon, 60
            ));

            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
        }};
        stampingFacility = new RecipeGenericCrafter("stamping-facility") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.titanium, 45,
                    NHItems.silicon, 60
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            size = 2;
            health = 600;
            armor = 4;
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
                    new DrawRegionFlip("-rot")
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
            health = 600;
            armor = 4;
            itemCapacity = 20;

            consumePower(180f / 60f);

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
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }}
            );

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);
        }};
        condenseFacility = new RecipeGenericCrafter("condense-facility"){{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.hardLight, 25,
                    NHItems.titanium, 100,
                    NHItems.presstanium, 50,
                    NHItems.juniorProcessor, 25
            ));
            health = 600;
            armor = 4f;
            size = 3;
            rotate = false;
            itemCapacity = 20;
            liquidCapacity = 100;
            consumePower(120 / 60f);

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(NHLiquids.quantumLiquid),
                    new DrawLiquidTile(Liquids.water),
                    new DrawLiquidTile(Liquids.cryofluid),
                    new DrawDefault()
           );
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
            health = 900;
            armor = 6;
            itemCapacity = 20;
            liquidCapacity = 15f;

            drawer = new DrawMulti(
                    new DrawRegionFlip("-rot"),
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
            health = 900;
            armor = 6;

            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 1);

            craftTime = 120f;
            consumePower(300 / 60f);

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
            rotate = true;

            size = 3;
            health = 900;
            armor = 6;

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

            consumePower(480 / 60f);
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
            health = 1200;
            armor = 8;

            ignoreLiquidFullness = true;

            itemCapacity = 30;


            drawer = new DrawMulti(
                    new DrawRegionFlip() {{suffix = "-rot";}},
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
            health = 1200;
            armor = 8;

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
                    new DrawRegionFlip() {{suffix = "-rot";}}
            );

            consumePower(480 / 60f);
        }};
        multipleSteelFactory = new RecipeGenericCrafter("multiple-steel-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.presstanium, 120,
                    NHItems.juniorProcessor, 65,
                    NHItems.metalOxhydrigen, 80,
                    NHItems.surgeAlloy, 60
            ));

            size = 3;
            rotate = false;
            health = 1500;
            armor = 10;
            itemCapacity = 30;


            drawer = new DrawMulti(
                    new DrawRegion("-base"),
                    new DrawCrucibleFlame(){{
                        midColor = flameColor = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
                    }},
                    new DrawRegion("-mid"),
                    new DrawRotator(0.75f, "-rotator-1") {
                        @Override
                        public void draw(Building build) {
                            Drawf.spinSprite(rotator, build.x + x, build.y + y, DrawFunc.rotator_90(DrawFunc.cycle(build.totalProgress() * rotateSpeed, 0, craftTime), 0.15f));
                        }
                    },
                    new DrawRotator(1.5f, "-rotator-2") {
                        @Override
                        public void draw(Building build) {
                            Drawf.spinSprite(rotator, build.x + x, build.y + y, -DrawFunc.rotator_90(DrawFunc.cycle(build.totalProgress() * rotateSpeed, 0, craftTime), 0.15f));
                        }
                    }
            );

            lightColor = NHItems.multipleSteel.color;
            updateEffect = EffectWrapper.wrap(Fx.smeltsmoke, lightColor);
            craftEffect = EffectWrapper.wrap(NHFx.square45_6_45, lightColor);

            consumePower(240f / 60f);
        }};
        processorEncoder = new RecipeGenericCrafter("processor-encoder") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 90, Items.phaseFabric, 90, Items.carbide, 120, NHItems.zeta, 80));

            size = 2;

            addLink(2, 0, 1,   2, 1, 1,
                    -1, 0, 1, -1, 1, 1 );

            craftTime = 120f;
            itemCapacity = 20;
            health = 1500;
            armor = 10;

            consumePower(240f / 60f);
            outputItems = with(NHItems.seniorProcessor, 2);

            Color drawerColor = Pal.accent.cpy().lerp(Color.white, 0.4f);
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
                        color = drawerColor;
                    }},
                    new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 15;
                        color = drawerColor;
                    }},
                    new DrawParticleFlow() {{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 60;
                        particles = 15;
                        color = drawerColor;
                    }},
                    new DrawScanLine() {{
                        scanLength = 24f;
                        scanAngle = 90f;
                        scanScl = 6f;
                        strokeRange = 6f;
                        colorFrom = drawerColor;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        scanScl = 12f;
                        strokeRange = 12f;
                        colorFrom = drawerColor;
                    }},
                    new DrawScanLine() {{
                        scanLength = 12f;
                        strokePlusScl = 2f;
                        scanScl = 12f;
                        strokeRange = 12f;
                        totalProgressMultiplier = 1.2f;
                        colorFrom = drawerColor;
                    }},
                    new DrawGlowRegion() {{
                        suffix = "-glow";
                        rotate = true;
                        color = drawerColor;
                    }},
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }}
            );
        }};
        irdryonMixer = new RecipeGenericCrafter("irdryon-mixer") {{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.surgeAlloy, 90,
                    NHItems.phaseFabric, 60,
                    NHItems.metalOxhydrigen, 120,
                    NHItems.zeta, 80
            ));

            size = 3;
            liquidCapacity = 30f;
            itemCapacity = 30;
            health = 1500;
            armor = 10;

            drawer = new DrawMulti(
                    new DrawRegion("-base"),
                    new DrawCrucibleFlame(){{
                        particles = 45;
                        particleRad = 11f;
                    }},
                    new DrawLiquidTile(NHLiquids.irdryonFluid),
                    new DrawRegion("-mid"),
                    new DrawRotator(true){{rotateSpeed = 2;}}
            );

            updateEffectChance = 0.075f;
            updateEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, NHItems.fusionEnergy.color.cpy().a(0.53f));

            consumePower(300 / 60f);
        }};
        hugeplastaniumFactory = new RecipeGenericCrafter("plastanium-crafter"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 90, NHItems.juniorProcessor, 120, Items.surgeAlloy, 80, NHItems.metalOxhydrigen, 40, NHItems.multipleSteel, 60));

            size = 3;
            health = 1500;
            armor = 10;
            rotate = false;

            craftTime = 90f;
            consumePower(640 / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;
            liquidCapacity = 60f;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawDefault();
        }};
        irayrondFactory = new RecipeGenericCrafter("irayrond-factory"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 90,
                    NHItems.seniorProcessor, 60,
                    NHItems.phaseFabric, 120,
                    NHItems.metalOxhydrigen, 45,
                    NHItems.multipleSteel, 85
            ));

            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            health = 1800;
            armor = 12;

            ignoreLiquidFullness = true;

            itemCapacity = 30;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionFlip("-rot"),
                    new DrawGlowRegion(){{
                        color = NHColor.lightSky;
                        rotate = true;
                        alpha = 1.1f;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 8;
                        flameColor = NHColor.lightSky;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameColor = NHColor.lightSky;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = -8;
                        flameColor = NHColor.lightSky;
                    }}
            );

            consumePower(480 / 60f);
        }};
         */
    }

    public static void loadColors() {
        stampingArc = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
        processorBlue = Color.valueOf("cee5ed");
    }
}
