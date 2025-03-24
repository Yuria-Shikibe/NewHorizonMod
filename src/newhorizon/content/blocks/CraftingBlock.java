package newhorizon.content.blocks;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawArcSmelt;
import mindustry.world.draw.DrawGlowRegion;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHColor;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.drawer.*;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.block.production.factory.content.FluxPhaser;
import newhorizon.expand.block.production.factory.content.GlassQuantifier;
import newhorizon.expand.block.production.factory.content.HyperZetaFactory;

import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block stampingFacility, processorPrinter, crucibleFoundry, crystallizer, surgeRefactor, fluxPhaser, hyperZetaFactory, glassQuantifier;

    public static void load(){
        stampingFacility = new RecipeGenericCrafter("stamping-facility"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.titanium, 45, Items.silicon, 60));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            craftTime = 120f;

            addInput(ItemStack.with(Items.titanium, 2), LiquidStack.with(NHLiquids.quantumEntity, 6 / 60f));
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

            craftTime = 120f;

            addInput(ItemStack.with(Items.silicon, 2), LiquidStack.with(NHLiquids.quantumEntity, 4 / 60f));
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
                        oneSprite = true;
                        suffix = "-glow";
                    }},
                    new DrawParticleFlow(){{
                       startX = -14f;
                       startY = 0;
                       endX = 14f;
                       endY = 0;
                       ignoreRot2_3 = true;
                    }},
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }}
            );
        }};
        crucibleFoundry = new RecipeGenericCrafter("crucible-foundry"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30, NHItems.juniorProcessor, 50, Items.tungsten, 40));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 60f;
            consumePower(300 / 60f);

            addInput(ItemStack.with(Items.tungsten, 2), LiquidStack.with(NHLiquids.xenAlpha, 6 / 60f));
            addInput(ItemStack.with(Items.tungsten, 2, Items.pyratite, 1), LiquidStack.empty);
            addInput(ItemStack.with(Items.tungsten, 2, Items.graphite, 3), LiquidStack.with(Liquids.ozone, 6 / 60f));

            outputItems = with(Items.carbide, 1);

            itemCapacity = 15;

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
                        drawLiquid = NHLiquids.xenAlpha;
                        alpha = 0.2f;
                    }},
                    new DrawArcSmeltRotated(){{
                        flameX = 11.25f;
                        //flameY = 4.5f;
                    }},
                    new DrawRegionRotated(){{
                        suffix = "-rot";
                    }},
                    new DrawRegionRotated(){{
                        suffix = "-top";
                    }}
            );
        }};
        crystallizer = new RecipeGenericCrafter("crystallizer"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 60, NHItems.juniorProcessor, 45, Items.carbide, 30));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1, /**/
                    0, 2, 1, /**/1, 2, 1 /**/);

            craftTime = 60f;
            consumePower(480 / 60f);
            addInput(ItemStack.empty, LiquidStack.with(NHLiquids.xenAlpha, 4 / 60f, NHLiquids.quantumEntity, 3 / 60f));
            addInput(ItemStack.with(Items.lead, 2), LiquidStack.with(Liquids.water, 6 / 60f));
            addInput(ItemStack.with(Items.oxide, 2), LiquidStack.with(Liquids.water, 6 / 60f));

            outputItems = with(NHItems.metalOxhydrigen, 2);

            itemCapacity = 30;
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
                        drawLiquid = NHLiquids.quantumEntity;
                        x = 4;
                        y = 4;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 1;
                        flameY = 1;
                        flameColor = NHLiquids.quantumEntity.color;
                        flameRadius *= 0.8f;
                        flameRadiusIn *= 0.8f;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 8;
                        flameY = 0;
                        flameColor = NHLiquids.quantumEntity.color;
                        flameRadius *= 0.5f;
                        flameRadiusIn *= 0.5f;
                    }},
                    new DrawFlameRotated(){{
                        drawFlame = false;
                        flameX = 0;
                        flameY = 8;
                        flameColor = NHLiquids.quantumEntity.color;
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
        surgeRefactor = new RecipeGenericCrafter("surge-refactor"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(NHItems.presstanium, 90, NHItems.juniorProcessor, 60, Items.carbide, 60, NHItems.metalOxhydrigen, 45));

            size = 4;

            addLink(3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            craftTime = 60f;
            consumePower(480 / 60f);
            addInput(ItemStack.empty, LiquidStack.with(NHLiquids.xenAlpha, 4 / 60f, NHLiquids.quantumEntity, 3 / 60f));

            outputItems = with(Items.surgeAlloy, 3);

            itemCapacity = 30;
            health = 1600;

            craftEffect = Fx.smeltsmoke;
            updateEffect = Fx.smeltsmoke;

            drawer = new DrawMulti(
                    new DrawRegionRotated(){{
                        suffix = "-rot";
                        x = 4;
                    }}
            );
        }};
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();
        glassQuantifier = new GlassQuantifier();
    }
}
