package newhorizon.content.blocks;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.Seq;
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
import newhorizon.expand.block.production.factory.PayloadCrafter;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block
        //vanilla adapt build
        sandCracker, oilRefiner,
        convertorTungsten, convertorTitanium, xenRefinery, zetaCrafter,
        stampingFacility, processorPrinter, crucibleFoundry, crucibleCaster, crystallizer, zetaDissociator, surgeRefactor,
        fabricSynthesizer, processorEncoder, irdryonMixer,hugeplastaniumFactory,multipleFoundry, processorCompactor, irayrondFactory, setonFactory,
        multipleSteelFactory, upgradeSortFactory, ancimembraneConcentrator,

        electronicAssemblyMk1, electronicAssemblyMk2, electronicAssemblyMk3,
        mechanicAssemblyMk1, mechanicAssemblyMk2, mechanicAssemblyMk3
    ;

    public static void load(){
        sandCracker = new RecipeGenericCrafter("sand-cracker"){{
            size = 2;
            requirements(Category.crafting, ItemStack.with(NHItems.presstanium, 40, NHItems.juniorProcessor, 40));
            health = 320;
            craftTime = 60f;
            itemCapacity = 20;
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

            addInput(ItemStack.with(Items.scrap, 4), LiquidStack.empty);
            addInput(ItemStack.with(Items.copper, 10), LiquidStack.empty);
            addInput(ItemStack.with(Items.lead, 10), LiquidStack.empty);
            addInput(ItemStack.with(Items.beryllium, 6), LiquidStack.empty);
            addInput(ItemStack.with(Items.titanium, 5), LiquidStack.empty);
            addInput(ItemStack.with(Items.thorium, 4), LiquidStack.empty);
            addInput(ItemStack.with(Items.tungsten, 5), LiquidStack.empty);

            outputItem = new ItemStack(Items.sand, 12);
        }};
        oilRefiner = new GenericCrafter("oil-refiner"){{

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
        convertorTungsten = new RecipeGenericCrafter("convertor-tungsten"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.titanium, 45, Items.graphite, 30));

            size = 2;
            craftTime = 30f;
            itemCapacity = 20;

            rotate = false;

            addInput(ItemStack.with(Items.titanium, 3), LiquidStack.empty);
            addInput(ItemStack.with(Items.copper, 5), LiquidStack.empty);
            addInput(ItemStack.with(Items.lead, 5), LiquidStack.empty);

            craftEffect = updateEffect = NHFx.square(NHColor.thurmixRed, 60, 6, 16, 3);

            consumePower(90f / 60f);
            outputItems = with(Items.tungsten, 2);

            drawer = new DrawMulti(new DrawDefault());
        }};
        convertorTitanium = new RecipeGenericCrafter("convertor-titanium"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.tungsten, 45, Items.graphite, 60));

            size = 2;
            craftTime = 30f;
            itemCapacity = 20;

            rotate = false;

            addInput(ItemStack.with(Items.tungsten, 2), LiquidStack.empty);
            addInput(ItemStack.with(Items.beryllium, 3), LiquidStack.empty);

            craftEffect = updateEffect = NHFx.square(NHColor.xenGamma, 60, 6, 16, 3);

            consumePower(90f / 60f);
            outputItems = with(Items.titanium, 3);

            drawer = new DrawMulti(new DrawDefault());
        }};
        xenRefinery = new RecipeGenericCrafter("xen-refinery"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 30, NHItems.juniorProcessor, 45, Items.carbide, 30));

            size = 2;
            craftTime = 60f;
            liquidCapacity = 40f;
            itemCapacity = 20;

            rotate = false;

            addInput(ItemStack.empty, LiquidStack.with(Liquids.oil, 15 / 60f));
            addInput(ItemStack.empty, LiquidStack.with(Liquids.arkycite, 20 / 60f));

            consumePower(120f / 60f);
            outputLiquids = LiquidStack.with(NHLiquids.xenFluid, 12 / 60f);

            drawer = new DrawMulti(
                    new DrawRegion(){{
                        suffix = "-base";
                    }},
                    new DrawLiquidTile(){{
                        drawLiquid = NHLiquids.xenFluid;
                    }},
                    new DrawRegion(){{
                        suffix = "-top";
                    }}
            );
        }};
        zetaCrafter = new RecipeGenericCrafter("zeta-crafter"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 30, NHItems.juniorProcessor, 45));

            size = 2;
            craftTime = 60f;
            liquidCapacity = 12f;
            itemCapacity = 30;

            rotate = false;

            addInput(with(Items.thorium, 5), LiquidStack.with(Liquids.water, 6 / 60f));

            consumePower(120f / 60f);
            outputItem = new ItemStack(NHItems.zeta, 3);

            drawer = new DrawDefault();
        }};
        stampingFacility = new RecipeGenericCrafter("stamping-facility"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.titanium, 45, Items.silicon, 60));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            craftTime = 40f;
            itemCapacity = 20;

            addInput(ItemStack.with(Items.titanium, 2), LiquidStack.with(NHLiquids.quantumLiquid, 6 / 60f));
            addInput(ItemStack.with(Items.titanium, 2, Items.graphite, 1), LiquidStack.empty);

            consumePower(180f / 60f);
            outputItems = with(NHItems.presstanium, 2);

            drawer = new DrawMulti(
                    new DrawRegionRotated(){{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawArcSmelt(){{
                        midColor = flameColor = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
                        flameRad /= 1.585f;
                        particleStroke /= 1.35f;
                        particleLen /= 1.25f;
                    }},
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        processorPrinter = new RecipeGenericCrafter("processor-printer"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.titanium, 30, Items.silicon, 45, Items.tungsten, 30));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            craftTime = 40f;
            itemCapacity = 20;

            addInput(ItemStack.with(Items.silicon, 2), LiquidStack.with(NHLiquids.quantumLiquid, 4 / 60f));
            addInput(ItemStack.with(Items.silicon, 2, Items.copper, 3), LiquidStack.empty);
            addInput(ItemStack.with(Items.silicon, 2, Items.beryllium, 3), LiquidStack.empty);

            consumePower(240f / 60f);
            outputItems = with(NHItems.juniorProcessor, 2);

            drawer = new DrawMulti(
                    new DrawRegionRotated(){{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawGlowRegionRotated(){{
                        suffix = "-glow-rot";
                    }},
                    new DrawParticleFlow(){{
                       startX = -14f;
                       startY = 0;
                       endX = 14f;
                       endY = 0;
                       ignoreRot2_3 = true;
                       particleLife = 75;
                       particles = 15;
                    }},
                    new DrawParticleFlow(){{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 15;
                    }},
                    new DrawParticleFlow(){{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 60;
                        particles = 15;
                    }},
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        crucibleFoundry = new RecipeGenericCrafter("crucible-foundry"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30, NHItems.juniorProcessor, 50, Items.tungsten, 40, NHItems.zeta, 40));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 60f;
            consumePower(300 / 60f);

            addInput(ItemStack.with(Items.tungsten, 2), LiquidStack.with(NHLiquids.xenFluid, 6 / 60f));
            addInput(ItemStack.with(Items.tungsten, 2, Items.pyratite, 1), LiquidStack.empty);
            addInput(ItemStack.with(Items.tungsten, 2, Items.graphite, 3), LiquidStack.with(Liquids.ozone, 6 / 60f));

            outputItems = with(Items.carbide, 2);

            itemCapacity = 20;
            liquidCapacity = 12f;

            drawer = new DrawMulti(
                    new DrawRegionRotated(){{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawCrucibleFlameRotated(){{
                        flameX = -6.25f;
                    }},
                    new DrawLiquidRegionRotated(){{
                        suffix = "-liquid-xen";
                        drawLiquid = NHLiquids.xenFluid;
                        alpha = 0.2f;
                    }},
                    new DrawArcSmeltRotated(){{
                        flameX = 11.25f;
                    }},
                    new DrawRegionRotated(){{
                        suffix = "-rot";
                    }},
                    new DrawRegionRotated(){{
                        suffix = "-top";
                    }}
            );
        }};
        crucibleCaster = new RecipeGenericCrafter("crucible-caster"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30, NHItems.seniorProcessor, 50, Items.carbide, 40, Items.phaseFabric, 60));

            rotate = false;
            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/,
                    -1, 2, 1, 0, 2, 1, 1, 2, 1,
                    -1, -2, 1, 0, -2, 1, 1, -2, 1
            );

            craftTime = 60f;
            consumePower(600 / 60f);

            addInput(ItemStack.with(Items.tungsten, 3), LiquidStack.with(NHLiquids.irdryonFluid, 4 / 60f));
            addInput(ItemStack.with(Items.tungsten, 3, Items.blastCompound, 3), LiquidStack.empty);
            addInput(ItemStack.with(Items.tungsten, 3), LiquidStack.with(Liquids.cyanogen, 3 / 60f));

            outputItems = with(Items.carbide, 8);

            itemCapacity = 30;

            drawer = new DrawDefault();
        }};
        crystallizer = new RecipeGenericCrafter("crystallizer"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 60, NHItems.juniorProcessor, 45, Items.tungsten, 30));

            size = 2;

            canMirror = true;
            rotations = new int[]{1, 0, 3, 2, 3, 2, 1, 0};

            addLink(2, 0, 1,  /**/ 2, 1, 1, /**/
                    0, 2, 1, /**/1, 2, 1 /**/);

            craftTime = 90f;
            consumePower(480 / 60f);
            addInput(ItemStack.empty, LiquidStack.with(NHLiquids.xenFluid, 4 / 60f, NHLiquids.quantumLiquid, 4.5 / 60f));
            addInput(ItemStack.with(Items.lead, 3), LiquidStack.with(Liquids.water, 9 / 60f));
            addInput(ItemStack.with(Items.metaglass, 1), LiquidStack.with(Liquids.water, 6 / 60f));
            addInput(ItemStack.with(Items.oxide, 1), LiquidStack.with(Liquids.water, 9 / 60f));

            outputItems = with(NHItems.metalOxhydrigen, 3);

            itemCapacity = 30;
            liquidCapacity = 20f;
            health = 1600;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionRotated(){{
                        suffix = "-rot";
                        x = 4;
                        y = 4;
                    }},
                    new DrawLiquidRegionRotated(){{
                        suffix = "-liquid";
                        drawLiquid = Liquids.water;
                        x = 4;
                        y = 4;
                    }},
                    new DrawLiquidRegionRotated(){{
                        suffix = "-liquid";
                        drawLiquid = NHLiquids.quantumLiquid;
                        x = 4;
                        y = 4;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 1;
                        flameY = 1;
                        flameColor = NHLiquids.quantumLiquid.color;
                        flameRadius *= 0.8f;
                        flameRadiusIn *= 0.8f;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 8;
                        flameY = 0;
                        flameColor = NHLiquids.quantumLiquid.color;
                        flameRadius *= 0.5f;
                        flameRadiusIn *= 0.5f;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 0;
                        flameY = 8;
                        flameColor = NHLiquids.quantumLiquid.color;
                        flameRadius *= 0.5f;
                        flameRadiusIn *= 0.5f;
                    }},
                    new DrawRegionRotated(){{
                        oneSprite = true;
                        suffix = "-edge";
                        x = 4;
                        y = 4;
                    }}
            );
        }};
        zetaDissociator = new RecipeGenericCrafter("zeta-dissociator"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30, NHItems.juniorProcessor, 40, Items.carbide, 60, NHItems.metalOxhydrigen, 45, NHItems.zeta, 60));

            quickRotate = true;
            invertFlip = true;
            canMirror = true;
            rotations = new int[]{0, 3, 2, 1, 2, 1, 0, 3};

            size = 3;

            craftTime = 60f;
            itemCapacity = 30;
            liquidCapacity = 30f;
            consumePower(450 / 60f);

            addInput(ItemStack.with(NHItems.zeta, 4), LiquidStack.with(NHLiquids.quantumLiquid, 4 / 60f));
            addInput(ItemStack.with(NHItems.zeta, 4), LiquidStack.with(Liquids.cryofluid, 6 / 60f));
            addInput(ItemStack.with(NHItems.zeta, 4), LiquidStack.with(Liquids.cyanogen, 3 / 60f));

            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 4.25 / 60f, NHLiquids.zetaFluidNegative, 4.25 / 60f);
            liquidOutputDirections = new int[]{1, 3};

            drawer = new DrawMulti(
                    new DrawRegion(){{
                        suffix = "-base";
                    }},
                    new DrawRegionRotated(){{
                        suffix = "-top-rot";
                    }}
            );
        }};
        surgeRefactor = new RecipeGenericCrafter("surge-refactor"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 60, NHItems.juniorProcessor, 90, Items.tungsten, 60, NHItems.metalOxhydrigen, 45));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 60f;
            consumePower(480 / 60f);
            addInput(ItemStack.with(Items.titanium, 3), LiquidStack.with(NHLiquids.zetaFluidPositive, 4 / 60f));

            outputItems = with(Items.surgeAlloy, 2);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidNegative, 3 / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;
            health = 1600;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        fabricSynthesizer = new RecipeGenericCrafter("fabric-synthesizer"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 90, NHItems.juniorProcessor, 60, Items.tungsten, 60, NHItems.metalOxhydrigen, 45));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 60f;
            consumePower(480 / 60f);
            addInput(ItemStack.with(Items.silicon, 3), LiquidStack.with(NHLiquids.zetaFluidNegative, 4 / 60f));

            outputItems = with(Items.phaseFabric, 2);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 3f / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;
            health = 1600;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        multipleSteelFactory = new RecipeGenericCrafter("multiple-steel-factory") {{
            requirements(Category.crafting,
                    with(NHItems.presstanium, 120, NHItems.juniorProcessor, 65, NHItems.metalOxhydrigen, 80, Items.surgeAlloy, 60));
            lightColor = NHItems.multipleSteel.color;
            updateEffect = EffectWrapper.wrap(Fx.smeltsmoke, lightColor);
            craftEffect = EffectWrapper.wrap(NHFx.square45_6_45, lightColor);
            outputItem = new ItemStack(NHItems.multipleSteel, 3);
            craftTime = 60f;
            itemCapacity = 20;
            health = 600;
            size = 3;
            hasPower = hasItems = true;
            drawer = new DrawDefault();
            addInput(ItemStack.with(Items.tungsten, 3, NHItems.presstanium, 3, NHItems.metalOxhydrigen, 4), LiquidStack.empty);
            addInput(ItemStack.with(Items.tungsten, 3, NHItems.presstanium, 2, Items.metaglass, 2), LiquidStack.empty);
            addInput(ItemStack.with(Items.tungsten, 2, NHItems.presstanium, 3, Items.oxide, 3), LiquidStack.empty);

            consumePower(3f);

            rotate = false;
            drawer = new DrawRegion(){{buildingRotate = false;}};
        }};
        processorEncoder = new RecipeGenericCrafter("processor-encoder"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 90, Items.phaseFabric, 90, Items.carbide, 120, NHItems.zeta, 80));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            craftTime = 120f;
            itemCapacity = 20;

            addInput(ItemStack.with(Items.surgeAlloy, 2, NHItems.juniorProcessor, 4), LiquidStack.empty);

            consumePower(240f / 60f);
            outputItems = with(NHItems.seniorProcessor, 2);

            drawer = new DrawMulti(
                    new DrawRegionRotated(){{
                        oneSprite = true;
                        suffix = "-base";
                    }},
                    new DrawGlowRegionRotated(){{
                        suffix = "-glow-rot";
                    }},
                    new DrawParticleFlow(){{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 75;
                        particles = 15;
                    }},
                    new DrawParticleFlow(){{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 90;
                        particles = 15;
                    }},
                    new DrawParticleFlow(){{
                        startX = -14f;
                        startY = 0;
                        endX = 14f;
                        endY = 0;
                        ignoreRot2_3 = true;
                        particleLife = 60;
                        particles = 15;
                    }},
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        irdryonMixer = new RecipeGenericCrafter("irdryon-mixer"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 90, Items.phaseFabric, 60, NHItems.metalOxhydrigen, 120, NHItems.zeta, 80));

            size = 3;

            craftTime = 60f;
            liquidCapacity = 30f;
            itemCapacity = 30;
            consumePower(300 / 60f);

            addInput(ItemStack.with(Items.phaseFabric, 1), LiquidStack.with(NHLiquids.xenFluid, 6 / 60f));

            outputLiquids = LiquidStack.with(NHLiquids.irdryonFluid, 8 / 60f);

            drawer = new DrawMulti(
                    new DrawRegion(){{
                        buildingRotate = false;
                    }}
            );
        }};
        multipleFoundry = new RecipeGenericCrafter("multiple-foundry"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 100, Items.phaseFabric, 80, NHItems.seniorProcessor, 60, NHItems.zeta, 80, NHItems.irayrondPanel, 80));

            size = 4;
            rotate = false;

            craftTime = 120f;
            liquidCapacity = 24f;
            ignoreLiquidFullness = true;
            itemCapacity = 40;
            consumePower(900 / 60f);

            addInput(ItemStack.with(Items.titanium, 12), LiquidStack.with(NHLiquids.zetaFluidNegative, 8 / 60f, NHLiquids.irdryonFluid, 12 / 60f));
            addInput(ItemStack.with(Items.titanium, 6, NHItems.fusionEnergy, 2), LiquidStack.with(NHLiquids.zetaFluidNegative, 8 / 60f));

            outputItems = with(Items.surgeAlloy, 8, NHItems.presstanium, 15);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 6 / 60f);

            drawer = new DrawDefault();
        }};
        processorCompactor = new RecipeGenericCrafter("processor-compactor"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.surgeAlloy, 100, Items.phaseFabric, 80, NHItems.seniorProcessor, 60, NHItems.zeta, 80, NHItems.irayrondPanel, 80));

            size = 4;
            rotate = false;

            craftTime = 150f;
            liquidCapacity = 20f;
            ignoreLiquidFullness = true;
            itemCapacity = 40;
            consumePower(900 / 60f);

            addInput(ItemStack.with(Items.silicon, 15, NHItems.metalOxhydrigen, 10), LiquidStack.with(NHLiquids.zetaFluidPositive, 8 / 60f));
            addInput(ItemStack.with(Items.silicon, 10, Items.plastanium, 10), LiquidStack.with(NHLiquids.zetaFluidPositive, 8 / 60f));

            outputItems = with(NHItems.juniorProcessor, 20, NHItems.seniorProcessor, 10);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidNegative, 6 / 60f);

            drawer = new DrawDefault();
        }};
        irayrondFactory = new RecipeGenericCrafter("irayrond-factory"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 90, NHItems.seniorProcessor, 60, Items.phaseFabric, 120, NHItems.metalOxhydrigen, 45, NHItems.multipleSteel, 85));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 120f;
            consumePower(480 / 60f);
            addInput(ItemStack.with(Items.surgeAlloy, 2, Items.carbide, 4), LiquidStack.with(NHLiquids.zetaFluidNegative, 4 / 60f));
            addInput(ItemStack.with(Items.surgeAlloy, 2, Items.plastanium, 3), LiquidStack.with(NHLiquids.zetaFluidNegative, 4 / 60f));

            outputItems = with(NHItems.irayrondPanel, 4);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 3f / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        hugeplastaniumFactory = new RecipeGenericCrafter("plastanium-crafter"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 90, NHItems.juniorProcessor, 120, Items.surgeAlloy, 80, NHItems.metalOxhydrigen, 40, NHItems.multipleSteel, 60));

            size = 3;
            rotate = false;

            craftTime = 90f;
            consumePower(640 / 60f);
            addInput(ItemStack.with(NHItems.metalOxhydrigen, 6), LiquidStack.with(NHLiquids.zetaFluidPositive, 1 / 60f));
            addInput(ItemStack.with(Items.tungsten, 6), LiquidStack.with(Liquids.cyanogen, 3 / 60f));
            addInput(ItemStack.with(NHItems.multipleSteel, 3), LiquidStack.with(Liquids.oil, 30 / 60f));


            outputItems = with(Items.plastanium, 9);
            outputLiquids = null;
            ignoreLiquidFullness = true;

            itemCapacity = 30;
            liquidCapacity = 60f;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawDefault();
        }};
        setonFactory = new RecipeGenericCrafter("seton-factory"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.juniorProcessor, 90, NHItems.zeta, 150, Items.surgeAlloy, 90, NHItems.metalOxhydrigen, 80, NHItems.multipleSteel, 65));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 120f;
            consumePower(480 / 60f);
            addInput(ItemStack.with(Items.carbide, 4), LiquidStack.with(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f));
            addInput(ItemStack.with(Items.plastanium, 4), LiquidStack.with(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f));
            addInput(ItemStack.with(Items.metaglass, 2, NHItems.presstanium, 2), LiquidStack.with(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f));

            outputItems = with(NHItems.setonAlloy, 2);
            outputLiquids = LiquidStack.with(NHLiquids.zetaFluidNegative, 3f / 60f);
            ignoreLiquidFullness = true;

            itemCapacity = 30;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                new DrawRegionCenterSymmetry(){{
                    suffix = "-rot";
                }}
            );
        }};
        ancimembraneConcentrator = new GenericCrafter("ancimembrane-concentrator"){{
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
                    new DrawCrucibleFlame(){
                        {
                            flameColor = NHColor.ancient;
                            midColor = Color.valueOf("2e2f34");
                            circleStroke = 1.05f;
                            circleSpace = 2.65f;
                        }

                        @Override
                        public void draw(Building build){
                            if(build.warmup() > 0f && flameColor.a > 0.001f){
                                Lines.stroke(circleStroke * build.warmup());

                                float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
                                float a = alpha * build.warmup();

                                Draw.blend(Blending.additive);
                                Draw.color(flameColor, a);

                                float base = (Time.time / particleLife);
                                rand.setSeed(build.id);
                                for(int i = 0; i < particles; i++){
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
                    new DrawGlowRegion(){{
                        color = NHColor.ancient;
                        layer = -1;
                        glowIntensity = 1.1f;
                        alpha = 1.1f;
                    }},
                    new DrawRotator(1f, "-top"){
                        @Override
                        public void draw(Building build){
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
        upgradeSortFactory = new GenericCrafter("upgradeSort-factory"){{
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
            outputItem = new ItemStack(NHItems.upgradeSort, 2);
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

        electronicAssemblyMk1 = new PayloadCrafter("electronic-assembly-mk1"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.juniorProcessor, 80, NHItems.presstanium, 60, Items.tungsten, 80));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            itemCapacity = 50;
            liquidCapacity = 50;
            payloadCapacity = 8;

            consumePower(180f / 60f);

            filter = Seq.with(ModuleBlock.processorT1, ModuleBlock.processorT2, ModuleBlock.speedModule1, ModuleBlock.productivityModule1, ModuleBlock.efficiencyModule1);

            drawer = new DrawMulti(
                new DrawRegionCenterSymmetry(){{
                    suffix = "-rot";
                }}
            );
        }};
        electronicAssemblyMk2 = new PayloadCrafter("electronic-assembly-mk2"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.phaseFabric, 120, Items.surgeAlloy, 80, Items.carbide, 60, NHItems.metalOxhydrigen, 120));

            size = 4;

            itemCapacity = 150;
            liquidCapacity = 150;
            payloadCapacity = 12;

            consumePower(180f / 60f);

            filter = Seq.with(ModuleBlock.processorT3, ModuleBlock.processorT4, ModuleBlock.speedModule2, ModuleBlock.productivityModule2, ModuleBlock.efficiencyModule2);

            drawer = new DrawMulti(
                    new DrawRegion(){{
                        suffix = "-base";
                    }}
            );
        }};
        electronicAssemblyMk3 = new PayloadCrafter("electronic-assembly-mk3"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.irayrondPanel, 250, NHItems.setonAlloy, 200, NHItems.seniorProcessor, 300));

            size = 4;

            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            itemCapacity = 300;
            liquidCapacity = 300;
            payloadCapacity = 20;

            consumePower(180f / 60f);

            filter = Seq.with(ModuleBlock.processorT5, ModuleBlock.speedModule3, ModuleBlock.productivityModule3, ModuleBlock.efficiencyModule3);

            drawer = new DrawMulti(
                new DrawRegionCenterSymmetry(){{
                    suffix = "-rot";
                }}
            );
        }};
        mechanicAssemblyMk1 = new PayloadCrafter("mechanic-assembly-mk1"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.juniorProcessor, 60, NHItems.presstanium, 80, Items.tungsten, 80));

            size = 3;

            consumePower(180f / 60f);

            itemCapacity = 50;
            liquidCapacity = 50;
            payloadCapacity = 8;

            filter = Seq.with(ModuleBlock.armorT1, ModuleBlock.armorT2, ModuleBlock.coreT1, ModuleBlock.coreT2);

            updateEffect = new Effect(25f, e-> {
                Draw.color(Pal.techBlue, Color.white, e.fout() * 0.6f);
                randLenVectors(e.id, 5, 12f * e.finpow(), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 2f * e.fout(), 45);
                    Drawf.light(e.x + x, e.y + y, e.fout() * 12f, e.color, 0.7f);
                });
            });

            craftEffect = new Effect(25f, e-> {
                Draw.color(Pal.techBlue, Color.white, e.fout() * 0.6f);
                randLenVectors(e.id, 12, 26f * e.finpow(), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 2f * e.fout());
                    Drawf.light(e.x + x, e.y + y, e.fout() * 12f, e.color, 0.7f);
                });
            });

            drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawPrintPayload(),
                new DrawScanLine(),
                new DrawRegion("-base"),
                new DrawGlowRegion("-light"){{color = Pal.techBlue;}}
            );
        }};
        mechanicAssemblyMk2 = new PayloadCrafter("mechanic-assembly-mk2"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.phaseFabric, 80, Items.surgeAlloy, 120, Items.carbide, 100, NHItems.metalOxhydrigen, 80));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            itemCapacity = 150;
            liquidCapacity = 150;
            payloadCapacity = 12;

            consumePower(180f / 60f);

            filter = Seq.with(ModuleBlock.armorT3, ModuleBlock.coreT3, ModuleBlock.coreT4);

            drawer = new DrawMulti(
                new DrawRegionCenterSymmetry(){{
                    suffix = "-rot";
                }}
            );
        }};
        mechanicAssemblyMk3 = new PayloadCrafter("mechanic-assembly-mk3"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.irayrondPanel, 200, NHItems.setonAlloy, 250, NHItems.seniorProcessor, 300));

            size = 5;

            itemCapacity = 300;
            liquidCapacity = 300;
            payloadCapacity = 20;

            consumePower(180f / 60f);

            filter = Seq.with(ModuleBlock.armorT4, ModuleBlock.armorT5, ModuleBlock.coreT5);

            drawer = new DrawMulti(
                new DrawRegion(){{
                    suffix = "-base";
                }}
            );
        }};
    }
}
