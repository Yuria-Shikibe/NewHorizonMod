package newhorizon.content.blocks;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
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

import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;
import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Color stampingArc, processorBlue;

    public static Block
            silicarCrusher, stampingPresser, processorManufactory, manufactory,
            processorPrinter, condenseFacility, crucibleFoundry, crystallizer, zetaFactory, zetaDissociator,

            castingFoundry, stampingFacility, heavyStampingFacility, plasticator, metalOxhydrigenSynthesizer,
            metalOxhydrigenRestructuror, photocatalystFactoty, fusionEnergySealingFactory,
            plasmaActivator,ultracooler,subcooler, particleActivator,heavyRollingMill,

            surgeRefactor, fabricSynthesizer, processorEncoder, irdryonMixer, hugePlastaniumFactory, multipleSteelFactory,
            irayrondFactory, setonFactory, upgradeSortFactory, ancimembraneConcentrator;

    public static void load() {
        loadColors();
        silicarCrusher = new MultiBlockCrafter("silicar-crusher") {{
            requirements(Category.crafting, with(
                    NHItems.hardLight, 50,
                    NHItems.silicar, 45
            ));

            size = 2;
            itemCapacity = 20;
            scaledHealth = 100f;

            addLink(2, 0, 1, 2, 1, 1);
            addOutputItemDirection(0, -1, NHItems.silicon);
            addOutputItemDirection(1, -1, NHItems.silicon);
            addOutputItemDirection(2, -1, NHItems.silicon);

            addOutputItemDirection(0, 2, NHItems.graphite);
            addOutputItemDirection(1, 2, NHItems.graphite);
            addOutputItemDirection(2, 2, NHItems.graphite);


            craftTime = 60f;
            outputItems = with(NHItems.graphite, 1, NHItems.silicon, 1);

            consumeItems(with(NHItems.silicar, 2));

            drawer = new DrawMulti(
                    new DrawBaseRegion("-2x3") {{
                        x = 4f;
                        buildingRotate = true;
                    }},
                    new DrawRotation() {{
                        xOffset = 4f;
                    }}
            );

            craftEffect = NHFx.hugeSmokeGray;
            updateEffect = new Effect(80f, e -> {
                Fx.rand.setSeed(e.id);
                Draw.color(Color.lightGray, Color.gray, e.fin());
                Angles.randLenVectors(e.id, 4, 2.0F + 12.0F * e.fin(Interp.pow3Out), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * Fx.rand.random(1, 2.5f));
                });
            }).layer(Layer.blockOver + 1);

            enableRotate();
        }};
        processorManufactory = new GenericCrafter("processor-manufactory") {{
            requirements(Category.crafting, with(
                    NHItems.hardLight, 60,
                    NHItems.graphite, 45,
                    NHItems.silicon, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;

            craftTime = 120f;
            outputItems = with(NHItems.juniorProcessor, 2);

            consumeItems(with(NHItems.silicon, 3));
            consumePower(0.5f);

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
      /*  stampingPresser = new MultiBlockCrafter("stamping-presser") {{
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
        }};*/

        processorPrinter = new MultiBlockCrafter("processor-printer") {{
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

            craftTime = 120f;
            consumePower(180f / 60f);
            consumeLiquids(LiquidStack.with(NHLiquids.quantumLiquid,6/60f));
            consumeItems(with(NHItems.silicon,2));
            outputItems = with(NHItems.juniorProcessor, 2);

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

        crystallizer = new MultiBlockCrafter("crystallizer") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 10f;

            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 1);

            craftTime = 120f;
            outputItems = with(NHItems.presstanium, 1);

            consumeLiquid(NHLiquids.ammonia, 0.1f);
            consumePower(2.5f);

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

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
                        drawType = DrawRotation.DRAW_NORMAL;
                        xOffset = yOffset = 8.25f;
                        layer = Layer.block + 1f;
                    }},
                    new DrawLiquidRegionRotated() {{
                        suffix = "-liquid";
                        drawLiquid = NHLiquids.ammonia;
                        x = 4;
                        y = 4;
                    }}
            );

            enableRotate();
        }};

        castingFoundry = new MultiBlockCrafter("casting-foundry") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 3;
            scaledHealth = 150f;
            itemCapacity = 20;

            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, -1, 2, 2, 1, 1, 3, 1, 1, -1, 2, 2, 1, 2, 1, 1, 3, 1);

            craftTime = 120f;
            outputItems = with(NHItems.presstanium, 1);

            consumeLiquid(NHLiquids.ammonia, 0.1f);
            consumePower(2.5f);

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

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
                    }}
            );

            enableRotate();
        }};
        stampingFacility = new GenericCrafter("stamping-facility") {{
            requirements(Category.crafting, with(
                    NHItems.titanium, 45,
                    NHItems.silicon, 60
            ));
            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            drawer = new DrawMulti(
                    new DrawDefault(),
                    new DrawArcSmelt() {{
                        midColor = flameColor = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
                        flameRad /= 1.585f;
                        particleStroke /= 1.35f;
                        particleLen /= 1.25f;
                    }}
            );
            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            craftTime = 120f;
            consumeItems(with(NHItems.titanium, 2, NHItems.graphite, 1));
            outputItems = with(NHItems.presstanium, 2);
            consumePower(180f / 60f);
        }};

        heavyStampingFacility = new MultiBlockCrafter("heavy-stamping-facility") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 10f;

            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                    }},
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

            craftTime = 120f;
            consumeItems(with(NHItems.titanium, 2, NHItems.graphite, 1));
            consumeLiquid(NHLiquids.neutron, 6 / 60f);
            consumePower(240f / 60f);
            outputItems = with(NHItems.presstanium, 5);

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            enableRotate();
        }};
        plasticator = new GenericCrafter("plasticator") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));
            hasItems = true;
            liquidCapacity = 60f;
            craftTime = 120f;

            size = 3;
            health = 640;
            hasPower = hasLiquids = true;
            craftEffect = Fx.formsmoke;
            updateEffect = Fx.plasticburn;
            drawer = new DrawMulti(new DrawDefault(), new DrawFade());

            consumeLiquids(LiquidStack.with(NHLiquids.photon, 6 / 60f, NHLiquids.xenFluid, 9 / 60f));
            consumePower(4f);
            consumeItem(Items.titanium, 3);
            outputItem = new ItemStack(Items.plastanium, 5);
        }};

        metalOxhydrigenSynthesizer = new MultiBlockCrafter("metal-oxhydrigen-synthesizer") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 20f;

            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(1, 2, 1, 2, 0, 1, 2, 1, 1, 2, 2, 1);

            craftTime = 120f;
            consumeLiquid(NHLiquids.ammonia, 12 / 60f);
            consumePower(240f / 60f);
            outputItems = with(NHItems.metalOxhydrigen, 1);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

        metalOxhydrigenRestructuror = new MultiBlockCrafter("metal-oxhydrigen-restructuror") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 30f;

            hasLiquids = true;
            canMirror = true;
            rotateDraw=false;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1, 2, 1, 1, 0, 2, 1, 1, 2, 1
                    , -1, 0, 1, -1, 1, 1, 0, -1, 1, 1, -1, 1);

            craftTime = 120f;
            consumeLiquids(LiquidStack.with(NHLiquids.ammonia, 18 / 60f, NHLiquids.proton, 6 / 60f));
            consumePower(240f / 60f);
            outputItems = with(NHItems.metalOxhydrigen, 3);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

        photocatalystFactoty = new MultiBlockCrafter("photocatalyst-factoty") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 30f;

            hasLiquids = true;
            canMirror = true;

            addLink(0, 2, 1, 2, 0, 1, 2, 1, 1, 2, 2, 1);

            craftTime = 60f;
            consumeLiquids(LiquidStack.with(NHLiquids.ammonia, 12 / 60f, NHLiquids.photon, 3 / 60f));
            consumePower(60 / 60f);
            outputLiquids = LiquidStack.with(NHLiquids.hydrazine, 6 / 60f);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

        fusionEnergySealingFactory = new GenericCrafter("fusion-energy-sealing-factory") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));
            hasItems = true;
            liquidCapacity = 60f;
            craftTime = 120f;

            size = 3;
            health = 640;
            hasPower = hasLiquids = true;
            craftEffect = updateEffect = NHFx.square(NHItems.fusionEnergy.color, 60, 6, 16, 3);

            consumeLiquids(LiquidStack.with(NHLiquids.cryofluid, 6 / 60f, NHLiquids.xenFluid, 12 / 60f));
            consumePower(2f);
            outputItem = new ItemStack(NHItems.fusionEnergy, 1);
        }};

        plasmaActivator = new GenericCrafter("plasma-activator") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 20f;

            hasLiquids = true;

            craftTime = 120f;
            consumePower(240f / 60f);
            consumeItems(with(NHItems.graphite, 2, NHItems.thorium, 1));
            outputLiquids = LiquidStack.with(NHLiquids.xenFluid, 15 / 60f);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

        }};

        subcooler = new GenericCrafter("subcooler") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 20f;

            hasLiquids = true;

            craftTime = 120f;
            consumePower(240f / 60f);
            consumeItems(with(NHItems.titanium, 2));
            consumeLiquids(LiquidStack.with(NHLiquids.water, 6 / 60f));
            outputLiquids = LiquidStack.with(NHLiquids.cryofluid, 18 / 60f);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

        }};

        ultracooler= new MultiBlockCrafter("ultracooler") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 2;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 20f;

            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1, 2, 1, 1, 3, 0, 1, 3, 1, 1);

            craftTime = 60f;
            consumePower(240f / 60f);
            consumeLiquids( LiquidStack.with(NHLiquids.xenFluid, 6 / 60f,NHLiquids.hydrazine,12/60f));

            outputLiquids=LiquidStack.with(NHLiquids.cryofluid, 30 / 60f);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

        particleActivator = new GenericCrafter("particle-activator") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.titanium, 45,
                    NHItems.silicon, 60
            ));

            size = 2;
            health = 600;
            armor = 4;
            itemCapacity = 20;
            liquidCapacity = 30f;

            craftTime = 120f;
            consumePower(60 / 60f);
            consumeItems(with(NHItems.graphite, 3));
            outputLiquids = LiquidStack.with(NHLiquids.xenFluid, 6 / 60f);
        }};

        heavyRollingMill= new MultiBlockCrafter("heavy-rolling-mill") {{
            requirements(Category.crafting, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 45
            ));

            size = 3;
            scaledHealth = 100f;
            itemCapacity = 20;
            liquidCapacity = 20f;

            hasLiquids = true;
            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2,0,1, 2,1,1 ,2,2,1,  0,2,1, 1,2,1,
                    -2,0,1, -2,-1,1, -2,-2,1, -1,-2,1, 0,-2,1);

            craftTime = 120f;
            consumePower(240f / 60f);
            consumeItems(with(NHItems.surgeAlloy, 2, NHItems.carbide, 2));
            consumeLiquids(LiquidStack.with(NHLiquids.cryofluid, 6 / 60f));

            outputItem = new ItemStack(NHItems.multipleSteel, 5);

            craftEffect = updateEffect = NHFx.square(Pal.techBlue, 60, 6, 16, 3);

            enableRotate();
        }};

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
        zetaFactory = new RecipeGenericCrafter("zeta-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    NHItems.hardLight, 50,
                    NHItems.tungsten, 100,
                    NHItems.carbide, 50,
                    NHItems.metalOxhydrigen, 50
            ));

            size = 2;
            health = 900;
            armor = 6;
            itemCapacity = 30;
            liquidCapacity = 30f;

            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1);

            craftTime = 60f;
            consumePower(300f / 60f);

            craftEffect = Fx.formsmoke;
            updateEffect = NHFx.trailToGray;

            drawer = new DrawMulti(
                    new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawLiquidRegion() {{
                        suffix = "-top";
                        drawLiquid = NHLiquids.zetaFluidPositive;
                    }},
                    new DrawRegionFlip() {{
                        suffix = "-rot";
                    }},
                    new DrawFlame() {{
                        flameColor = NHLiquids.zetaFluidPositive.color;
                        flameRadius = 3f;
                        flameRadiusIn = 1.9f;
                        flameRadiusScl = 5f;
                        flameRadiusMag = 2f;
                        flameRadiusInMag = 1f;
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
        setonFactory = new RecipeGenericCrafter("seton-factory") {{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.juniorProcessor, 90, NHItems.zeta, 150, Items.surgeAlloy, 90, NHItems.metalOxhydrigen, 80, NHItems.multipleSteel, 65));

            size = 3;

            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            craftTime = 120f;
            itemCapacity = 30;
            health = 1800;
            armor = 12;
            consumePower(480 / 60f);

            outputItems = with(NHItems.setonAlloy, 2);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidNegative, 3f / 60f);
            ignoreLiquidFullness = true;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

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
        }};
        upgradeSortFactory = new RecipeGenericCrafter("nodex-factory") {{
            requirements(Category.crafting,
                    ItemStack.with(NHItems.setonAlloy, 160, NHItems.seniorProcessor, 80,
                            NHItems.presstanium, 150, NHItems.irayrondPanel, 90));

            size = 3;
            rotate = false;
            health = 2100;
            armor = 14;
            itemCapacity = 40;
            hasPower = hasItems = true;

            drawer = new DrawPrinter(NHItems.nodexPlate) {{
                printColor = NHColor.darkEnrColor;
                lightColor = Color.valueOf("#E1BAFF");
                moveLength = 4.2f;
                time = 25f;
            }};
            clipSize = size * tilesize * 2f;

            craftEffect = new Effect(25f, e -> {
                Draw.color(NHColor.darkEnrColor);
                Angles.randLenVectors(e.id, 4, 24 * e.fout() * e.fout(), (x, y) -> {
                    Lines.stroke(e.fout() * 1.7f);
                    Lines.square(e.x + x, e.y + y, 2f + e.fout() * 6f);
                });
            });
            updateEffect = NHStatusEffects.quantization.effect;

            consumePower(1600 / 60f);
        }};
        ancimembraneConcentrator = new RecipeGenericCrafter("ancimembrane-concentrator") {{
            size = 3;
            rotate = false;

            lightRadius /= 2f;

            requirements(Category.crafting,
                    ItemStack.with(NHItems.seniorProcessor, 120, NHItems.multipleSteel, 90, NHItems.zeta, 45, NHItems.setonAlloy, 60));

            craftTime = 120f;

            health = 2100;
            armor = 14;
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

            itemCapacity = 40;
            liquidCapacity = 40f;
            consumePower(1600 / 60f);
        }};

         */
    }

    public static void loadColors() {
        stampingArc = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
        processorBlue = Color.valueOf("cee5ed");
    }
}
