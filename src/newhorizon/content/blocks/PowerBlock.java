package newhorizon.content.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.graphics.Pal;
import mindustry.entities.Effect;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.drawer.DrawRegionFlip;
import newhorizon.expand.block.power.GravityWallSubstation;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.util.graphic.EffectWrapper;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class PowerBlock {
    public static Block
            atmosphericConcentrator,
            hydroFuelCell, zetaGenerator, anodeFusionReactor, cathodeFusionReactor, thermoReactor,
            armorBattery, armorBatteryLarge, armorBatteryHuge,
            gravityTrapMidantha, gravityTrapSerpulo, gravityTrapErekir, gravityTrapSmall, gravityTrap;

    public static void load() {
        gravityTrapMidantha = new GravityWallSubstation("gravity-node-midantha") {{
            requirements(Category.power, BuildVisibility.shown, with(Items.silicon, 5));

            size = 1;
            health = 400;
            laserRange = 8;
            maxNodes = 10;
            gravityRange = laserRange * tilesize * 1.5f;
        }};
        
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

        armorBattery = new Battery("armor-battery") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 10,
                    NHItems.juniorProcessor, 5
            ));
            size = 1;
            health = 600;
            armor = 20;
            consumePowerBuffered(10000f);
        }};

        armorBatteryLarge = new Battery("armor-battery-large") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 40,
                    NHItems.juniorProcessor, 10,
                    NHItems.zeta, 50
            ));
            size = 3;
            health = 2000;
            armor = 30;
            consumePowerBuffered(100000f);
        }};

        armorBatteryHuge = new Battery("armor-battery-huge") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                    NHItems.multipleSteel, 60,
                    NHItems.seniorProcessor, 40,
                    NHItems.zeta, 200
            ));
            size = 5;
            health = 5000;
            armor = 50;
            consumePowerBuffered(1000000f);
        }};

        atmosphericConcentrator = new RecipeGenericCrafter("atmospheric-concentrator"){{
            requirements(Category.power, ItemStack.with(
                    NHItems.presstanium, 50,
                    NHItems.juniorProcessor, 20,
                    Items.silicon, 100,
            ));
            health = 300;
            size = 3;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.nitrogen, 4.1f), new DrawDefault(), new DrawHeatInput(),
            new DrawParticles(){{
                color = Color.valueOf("d4f0ff");
                alpha = 0.6f;
                particleSize = 4f;
                particles = 10;
                particleRad = 12f;
                particleLife = 140f;
            }});

            itemCapacity = 20;
            liquidCapacity = 60;
            outputsPower = true;
            powerProduction = 300 / 60f;
        }};

        hydroFuelCell = new ConsumeGenerator("hydro-fuel-cell") {{
            size = 2;
            requirements(Category.power, ItemStack.with(NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 45, NHItems.presstanium, 60));

            lightColor = Pal.techBlue;
            consumeEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, Liquids.hydrogen.color.cpy().lerp(Liquids.nitrogen.color, 0.4f).a(0.56f));
            generateEffect = new Effect(45f, e -> {
                Draw.color(lightColor, Color.white, e.fin() * 0.66f);
                Lines.stroke(e.fout() * 1.375f);
                Lines.spikes(e.x, e.y, 0.45f + 5 * e.finpow(), 5.5f * e.fout(), 4, 45);
            });
            //			//NHTechTree.add(Blocks.thoriumReactor,this);
            powerProduction = 1800f / 60f;
            health = 320;
            itemCapacity = 40;
            liquidCapacity = 30;
            itemDuration = 240f;
            consumeItem(NHItems.metalOxhydrigen, 4);
            consumeLiquid(Liquids.nitrogen, 2 / 60f);

            squareSprite = false;
            hasLiquids = hasItems = true;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.nitrogen, 2f), new DrawDefault(), new DrawGlowRegion() {{
                color = Liquids.hydrogen.color;
            }});
        }};
        
        zetaGenerator = new RecipeGenericCrafter("zeta-generator") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.metalOxhydrigen, 120,
                    NHItems.juniorProcessor, 80,
                    NHItems.zeta, 100,
                    NHItems.carbide, 150
            ));

            size = 3;
            health = 150 * 9;
            armor = 10f;
            itemCapacity = 30;
            liquidCapacity = 30;
            rotate = false;

            powerProduction = 50f;
            outputsPower = true;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(NHLiquids.xenFluid),
                    new DrawDefault(),
                    new DrawGlowRegion() {{
                        color = NHItems.zeta.color;
                    }}
            );

            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};

        anodeFusionReactor = new RecipeGenericCrafter("anode-fusion-reactor") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.seniorProcessor, 300,
                    NHItems.phaseFabric, 300,
                    NHItems.surgeAlloy, 450,
                    NHItems.carbide, 600,
                    NHItems.multipleSteel, 240
            ));
            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            size = 4;
            health = 3000;
            armor = 10f;
            itemCapacity = 45;
            liquidCapacity = 45;
            outputsPower = true;
            powerProduction = 12000 / 60f;

            drawer = new DrawRegionFlip("-rot");

            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};

        cathodeFusionReactor = new RecipeGenericCrafter("cathode-fusion-reactor") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.seniorProcessor, 300,
                    NHItems.phaseFabric, 300,
                    NHItems.surgeAlloy, 450,
                    NHItems.carbide, 600,
                    NHItems.multipleSteel, 240
            ));
            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            size = 4;
            health = 3000;
            armor = 10f;
            itemCapacity = 45;
            liquidCapacity = 45;
            outputsPower = true;
            powerProduction = 12000 / 60f;

            drawer = new DrawRegionFlip("-rot");

            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};

        thermoReactor = new RecipeGenericCrafter("thermo-reactor") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.phaseFabric, 300,
                    NHItems.surgeAlloy, 450,
                    NHItems.carbide, 600,
                    NHItems.multipleSteel, 240
            ));
            addLink(-1, 3, 2, 1, 3, 1, 1, 4, 1, 3, 0, 2, 3, -1, 1, 4, -1, 1, -1, -4, 2, 1, -4, 1, 1, -3, 1, -4, -1, 2, -4, 1, 1, -3, 1, 1);

            size = 5;
            health = 10000;
            rotate = false;
            armor = 20f;
            itemCapacity = 45;
            liquidCapacity = 45;
            powerProduction = 90000 / 60f;
            outputsPower = true;

            drawer = new DrawDefault();

            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};
    }
}
