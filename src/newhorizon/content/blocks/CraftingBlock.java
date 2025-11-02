package newhorizon.content.blocks;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
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
            stampingFacility, processorPrinter, condenseFacility, crucibleFoundry, crystallizer, zetaFactory, zetaDissociator,
            surgeRefactor, fabricSynthesizer, processorEncoder, irdryonMixer, hugeplastaniumFactory, multipleSteelFactory,
            irayrondFactory, setonFactory, upgradeSortFactory, ancimembraneConcentrator;

    public static Block factory0, factory1, factory2, factory3, factory4, factory5, factory6;

    public static void load() {
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
                    NHItems.presstanium, 50,
                    NHItems.juniorProcessor, 20,
                    Items.silicon, 100
            ));
            health = 300;
            size = 3;
            rotate = false;
            itemCapacity = 20;
            liquidCapacity = 100;
            consumePower(200 / 60f);

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
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 50, NHItems.juniorProcessor, 30, Items.tungsten, 20));

            size = 2;
            health = 900;
            armor = 4;
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

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

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

        factory0 = new RecipeGenericCrafter("factory-0"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.tungsten, 80,
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 40
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1, 0, 2, 1, 1, 2, 1, 0, -1, 1, 1, -1, 1);

            size = 2;
            health = 750;
            armor = 5;
            itemCapacity = 30;
            rotate = false;
            drawer = new DrawDefault();

            consumePower(300f / 60f);
        }};
        
        factory1 = new RecipeGenericCrafter("factory-1"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 10,
                    NHItems.juniorProcessor, 80,
                    NHItems.carbide, 40,
                    NHItems.metalOxhydrigen, 80
            ));
            addLink(2, 0, 1, 2, 1, 1, -1, 0, 1, -1, 1, 1, 0, 2, 1, 1, 2, 1, 0, -1, 1, 1, -1, 1, -1, 2, 1, 2, -1, 1);
            canMirror = true;
            rotations = new int[]{1, 0, 1, 0, 3, 2, 3, 2};

            size = 2;
            health = 1050;
            armor = 7;
            itemCapacity = 30;
            drawer = new DrawRegionRotatedDiagonal("-rot");

            consumePower(360f / 60f);
        }};
        factory2 = new RecipeGenericCrafter("factory-2"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.juniorProcessor, 60,
                    NHItems.metalOxhydrigen, 120,
                    NHItems.phaseFabric, 80,
                    NHItems.surgeAlloy, 80
            ));
            addLink(2, -1, 1, 2, 0, 1, 2, 1, 1, -2, -1, 1, -2, 0, 1, -2, 1, 1);

            size = 3;
            health = 1350;
            armor = 9;
            itemCapacity = 30;
            drawer = new DrawRegionFlip();

            consumePower(450f / 60f);
        }};
        factory3 = new RecipeGenericCrafter("factory-3"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.zeta, 200,
                    NHItems.presstanium, 100,
                    NHItems.multipleSteel, 60,
                    NHItems.seniorProcessor, 40
            ));
            size = 4;
            health = 1650;
            armor = 11;
            itemCapacity = 30;
            rotate = false;
            drawer = new DrawRegionFlip();

            consumePower(600f / 60f);
        }};
        factory4 = new RecipeGenericCrafter("factory-4"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.juniorProcessor, 100,
                    NHItems.carbide, 60,
                    NHItems.irayrondPanel, 60,
                    NHItems.setonAlloy, 80
            ));
            addLink(-2, -2, 1, -2, -1, 1, -2, 0, 1, -1, -2, 1, 0, -2, 1, 2, 2, 1, 2, 1, 1, 2, 0, 1, 1, 2, 1, 0, 2, 1);
            canMirror = true;
            rotations = new int[]{1, 0, 1, 0, 3, 2, 3, 2};

            size = 3;
            health = 1950;
            armor = 13;
            itemCapacity = 30;
            drawer = new DrawRegionRotatedDiagonal("-rot");

            consumePower(720f / 60f);
        }};
        factory5 = new RecipeGenericCrafter("factory-5"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.silicon, 150,
                    NHItems.surgeAlloy, 60,
                    NHItems.nodexPlate, 30,
                    NHItems.ancimembrane, 20
            ));

            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);
            size = 4;
            health = 2250;
            armor = 15;
            itemCapacity = 30;
            drawer = new DrawRegionFlip();

            consumePower(840f / 60f);
        }};
        factory6 = new RecipeGenericCrafter("factory-6"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.multipleSteel, 150,
                    NHItems.seniorProcessor, 100,
                    NHItems.nodexPlate, 55,
                    NHItems.darkEnergy, 40
            ));
            addLink(-2, 1, 1, -2, 2, 1, -2, 3, 1, -1, 3, 1, 0, 3, 1, 1, -2, 1, 2, -2, 1, 3, -2, 1, 3, -1, 1, 3, 0, 1);
            canMirror = true;
            rotations = new int[]{1, 0, 1, 0, 3, 2, 3, 2};

            size = 4;
            health = 2550;
            armor = 17;
            itemCapacity = 30;
            drawer = new DrawRegionRotatedDiagonal("-rot");

            consumePower(1200f / 60f);
        }};
    }
}
