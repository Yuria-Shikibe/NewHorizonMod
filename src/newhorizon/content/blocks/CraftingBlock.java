package newhorizon.content.blocks;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.DrawArcSmelt;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHColor;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.drawer.DrawRegionCenterSymmetry;
import newhorizon.expand.block.drawer.DrawRegionRotated;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.block.production.factory.content.FluxPhaser;
import newhorizon.expand.block.production.factory.content.GlassQuantifier;
import newhorizon.expand.block.production.factory.content.HyperZetaFactory;

import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block stampingFacility, fluxPhaser, hyperZetaFactory, glassQuantifier;

    public static void load(){
        stampingFacility = new RecipeGenericCrafter("stamping-facility"){{
            requirements(Category.crafting, BuildVisibility.shown,
                    ItemStack.with(Items.titanium, 45, Items.silicon, 60));

            size = 2;

            addLink(2, 0, 1,  /**/ 2, 1, 1,/**/
                    -1, 0, 1, /**/-1, 1, 1 /**/);

            craftTime = 120f;

            addInput(ItemStack.with(Items.titanium, 2), LiquidStack.with(NHLiquids.quantumEntity, 9 / 60f));
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
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();
        glassQuantifier = new GlassQuantifier();
    }
}
