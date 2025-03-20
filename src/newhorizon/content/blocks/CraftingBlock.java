package newhorizon.content.blocks;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawArcSmelt;
import mindustry.world.draw.DrawLiquidRegion;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
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
    public static Block stampingFacility, processorPrinter, crucibleFoundry, fluxPhaser, hyperZetaFactory, glassQuantifier;

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
                    new DrawRegionCenterSymmetry(){{
                        suffix = "-rot";
                    }},
                    new DrawFlameRotated(NHItems.fusionEnergy.color){{
                        suffix = "-flame-0";
                        flameX = -8;
                    }},
                    new DrawFlameRotated(NHItems.fusionEnergy.color){{
                        suffix = "-flame-1";
                        flameX = 8;
                    }}
            );
        }};
        crucibleFoundry = new RecipeGenericCrafter("crucible-foundry"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 30, NHItems.seniorProcessor, 50, Items.tungsten, 40));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 120f;
            consumePower(300 / 60f);

            addInput(ItemStack.with(Items.tungsten, 2), LiquidStack.with(NHLiquids.xenAlpha, 6 / 60f));
            addInput(ItemStack.with(Items.tungsten, 2, Items.graphite, 3), LiquidStack.with(Liquids.ozone, 6 / 60f));
            addInput(ItemStack.with(Items.tungsten, 2, Items.pyratite, 1), LiquidStack.empty);

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
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();
        glassQuantifier = new GlassQuantifier();
    }
}
