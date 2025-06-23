package newhorizon.content.blocks;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.drawer.DrawRegionCenterSymmetry;
import newhorizon.expand.block.power.GravityWallSubstation;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class PowerBlock {
    public static Block zetaGenerator, anodeFusionReactor, cathodeFusionReactor, thermoReactor;
    public static Block gravityTrapSerpulo, gravityTrapErekir, gravityTrapSmall, gravityTrap;

    public static void load() {
        gravityTrapSerpulo = new GravityWallSubstation("gravity-node-serpulo") {{
            requirements(Category.power, BuildVisibility.shown, with(Items.copper, 10, Items.lead, 8));

            size = 1;
            health = 400;
            laserRange = 8;
            maxNodes = 10;
            gravityRange = laserRange * tilesize * 1.5f;
        }};

        gravityTrapErekir = new GravityWallSubstation("gravity-node-erekir") {{
            requirements(Category.power, BuildVisibility.shown, with(Items.beryllium, 15));

            size = 1;
            health = 400;
            laserRange = 8;
            maxNodes = 10;
            gravityRange = laserRange * tilesize * 1.5f;
            clipSize = gravityRange * 2f;
        }};

        gravityTrapSmall = new GravityWallSubstation("gravity-trap") {{
            requirements(Category.power, BuildVisibility.shown, with(Items.titanium, 10, Items.tungsten, 8));

            size = 2;
            health = 640;
            laserRange = 16;
            maxNodes = 20;
            gravityRange = laserRange * tilesize * 1.5f;
            clipSize = gravityRange * 2f;
        }};

        gravityTrap = new GravityWallSubstation("gravity-trap-heavy") {{
            requirements(Category.power, BuildVisibility.shown, with(NHItems.seniorProcessor, 15, NHItems.multipleSteel, 20));

            size = 3;
            health = 1250;
            laserRange = 40;
            maxNodes = 6;
            gravityRange = laserRange * tilesize * 1.2f;
            clipSize = gravityRange * 2f;
        }};

        zetaGenerator = new RecipeGenericCrafter("zeta-generator") {{
            requirements(Category.power, ItemStack.with(NHItems.metalOxhydrigen, 120, NHItems.juniorProcessor, 80, NHItems.zeta, 100, Items.carbide, 150));

            size = 3;

            rotate = false;

            health = 1500;
            armor = 5f;
            itemCapacity = 30;
            liquidCapacity = 30;

            powerProduction = 50f;
            craftTime = 120f;

            addInput(ItemStack.with(NHItems.zeta, 3, Items.carbide, 1), LiquidStack.with(NHLiquids.quantumLiquid, 6 / 60f));
            addInput(ItemStack.with(NHItems.zeta, 3, Items.carbide, 1), LiquidStack.with(Liquids.cryofluid, 6 / 60f));
            addInput(ItemStack.with(NHItems.zeta, 3, Items.carbide, 1), LiquidStack.with(Liquids.nitrogen, 4 / 60f));

            outputItem = new ItemStack(NHItems.fusionEnergy, 2);

            outputsPower = true;
            hasLiquids = hasItems = hasPower = true;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(NHLiquids.quantumLiquid),
                    new DrawLiquidTile(Liquids.cryofluid),
                    new DrawLiquidTile(Liquids.nitrogen),
                    new DrawDefault(),
                    new DrawGlowRegion() {{
                        color = NHItems.zeta.color;
                    }}
            );

            consumePower(0f);
            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};

        anodeFusionReactor = new RecipeGenericCrafter("anode-fusion-reactor") {{
            requirements(Category.power, ItemStack.with(NHItems.seniorProcessor, 300, Items.phaseFabric, 300, Items.surgeAlloy, 450, Items.carbide, 600, NHItems.multipleSteel, 240));

            size = 4;

            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            health = 3000;
            armor = 10f;
            itemCapacity = 45;
            liquidCapacity = 45;

            powerProduction = 12000 / 60f;
            craftTime = 120f;

            addInput(ItemStack.with(NHItems.fusionEnergy, 2, Items.surgeAlloy, 4), LiquidStack.with(NHLiquids.zetaFluidPositive, 8 / 60f));

            outputItem = new ItemStack(NHItems.thermoCorePositive, 2);

            outputsPower = true;
            hasLiquids = hasItems = hasPower = true;

            drawer = new DrawRegionCenterSymmetry() {{
                suffix = "-rot";
            }};

            consumePower(0f);
            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};

        cathodeFusionReactor = new RecipeGenericCrafter("cathode-fusion-reactor") {{
            requirements(Category.power, ItemStack.with(NHItems.seniorProcessor, 300, Items.phaseFabric, 300, Items.surgeAlloy, 450, Items.carbide, 600, NHItems.multipleSteel, 240));

            size = 4;

            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            health = 3000;
            armor = 10f;
            itemCapacity = 45;
            liquidCapacity = 45;

            powerProduction = 12000 / 60f;
            craftTime = 120f;

            addInput(ItemStack.with(NHItems.fusionEnergy, 2, Items.phaseFabric, 4), LiquidStack.with(NHLiquids.zetaFluidNegative, 8 / 60f));

            outputItem = new ItemStack(NHItems.thermoCoreNegative, 2);

            outputsPower = true;
            hasLiquids = hasItems = hasPower = true;

            drawer = new DrawRegionCenterSymmetry() {{
                suffix = "-rot";
            }};

            consumePower(0f);
            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};

        thermoReactor = new RecipeGenericCrafter("thermo-reactor") {{
            requirements(Category.power, ItemStack.with(Items.phaseFabric, 300, Items.surgeAlloy, 450, Items.carbide, 600, NHItems.multipleSteel, 240));

            size = 5;

            rotate = false;

            addLink(
                    -1, 3, 2, 1, 3, 1, 1, 4, 1,
                    3, 0, 2, 3, -1, 1, 4, -1, 1,
                    -1, -4, 2, 1, -4, 1, 1, -3, 1,
                    -4, -1, 2, -4, 1, 1, -3, 1, 1
            );

            health = 10000;
            armor = 20f;
            itemCapacity = 45;
            liquidCapacity = 45;

            powerProduction = 90000 / 60f;
            craftTime = 120f;

            addInput(ItemStack.with(NHItems.thermoCorePositive, 2, NHItems.thermoCoreNegative, 2, NHItems.upgradeSort, 4, NHItems.ancimembrane, 4), LiquidStack.empty);

            outputItem = new ItemStack(NHItems.darkEnergy, 2);

            outputsPower = true;
            hasLiquids = hasItems = hasPower = true;

            drawer = new DrawDefault();

            consumePower(0f);
            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};
    }
}
