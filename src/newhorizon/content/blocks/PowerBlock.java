package newhorizon.content.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.entities.Effect;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHColor;
import newhorizon.expand.block.drawer.*;
import newhorizon.expand.block.power.GravityWallSubstation;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.draw.DrawLiquidAnimatedOffset;
import newhorizon.expand.draw.DrawLiquidSmelt;
import newhorizon.expand.draw.DrawPistonsOffset;
import newhorizon.expand.draw.DrawRegionOffset;
import newhorizon.util.graphic.EffectWrapper;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class PowerBlock {
    public static Block
            nitrogenDissociator,
            crystalDecompositionThermalGenerator, psiGenerator, hydroFuelCell, zetaGenerator, anodeFusionReactor, cathodeFusionReactor, thermoReactor,
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

        //wip
        crystalDecompositionThermalGenerator = new RecipeGenericCrafter("crystal-decomposition-thermal-generator") {{
            requirements(Category.power, ItemStack.with(
                     NHItems.hardLight, 10,
                     NHItems.silicar, 30,
                     NHItems.silicon, 15
            ));
            addLink(2, 0, 1, 2, 1, 1);

            size = 2;
            health = 300;
            armor = 2f;
            itemCapacity = 20;
            liquidCapacity = 30;

            powerProduction = 480 / 60f;
            outputsPower = true;

            drawer = new DrawMulti(
                new DrawRegionRotated() {{
                        oneSprite = true;
                        suffix = "-base";
                        x = 4;
                        layer = Layer.block -1f ;
                 }},
                new DrawRegionRotated() {{
                        suffix = "-rot";
                        x = 4;
                 }},
                new DrawRegionOffset("-glow", 4f, 0f,true,0.1f),
                new DrawLiquidSmelt(){{
                    x = -1f;
                    y = 0f;
                    fixedAlpha = 0.68F;
                    flameRad = 1f;
                    circleSpace = 1.8f;
                    circleStroke = 1F;
                    colorLerp = 0.08f;
                    particles = 4;
                    particleLen = 1.0F;
                }},
                new DrawLiquidAnimatedOffset(){{
                //    suffix = "-liquid";
                    alpha = 1f;
                    offsetX = 2f;
                    offsetY = 0f;
                //    followRotation = true;
                    glow = false;
                //    baseBubbleChance= 0.03f;
                //    bubbleEffect = Fx.bubble;
                }},
                new DrawPistonsOffset(){{
                    sides = 1;
                    sinMag = 0f;
                    sinScl = 0f;
                    lenOffset = -1f;
                    angleOffset = 0f;
                    offsetX = 5f;
                    offsetY = 0f;
                    suffix = "-piston-t";
                    }},
                new DrawPistonsOffset(){{
                    sides = 1;         // 只在一侧有活塞
                    sinMag = 2.5f;       // 活塞往返幅度
                    sinScl = 6f;       // 速度
                    lenOffset = -1f;
                    angleOffset = 0f;  // 0度方向
                    offsetX = 5.1f;      // 整体向右偏移
                    offsetY = 0f;      // 不偏移
                    suffix = "-piston"; // 贴图后缀，与 block.name 拼接
                }},
                new DrawPistonsOffset(){{
                    sides = 1;
                    sinMag = 2.5f;
                    sinScl = 6f;
                    lenOffset = -1f;
                    angleOffset = 0f;
                    sinOffset = Mathf.PI / 2f;
                    offsetX = 5.1f;
                    offsetY = 0f;
                    suffix = "-piston1";
                }}
            );
        }};

        nitrogenDissociator = new RecipeGenericCrafter("nitrogen-dissociator"){{
            requirements(Category.power, ItemStack.with(
                    NHItems.presstanium, 50,
                    NHItems.juniorProcessor, 20,
                    Items.silicon, 100
            ));
            health = 300;
            size = 3;
            rotate = false;
            itemCapacity = 30;
            liquidCapacity = 100;
            outputsPower = true;
            powerProduction = 300 / 60f;
            ignoreLiquidFullness = true;

            drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.nitrogen, 4.1f),
                new DrawDefault(),
                new DrawParticles(){{
                    color = Color.valueOf("d4f0ff");
                    alpha = 0.6f;
                    particleSize = 4f;
                    particles = 10;
                    particleRad = 12f;
                    particleLife = 140f;
                }}
            );
        }};

        psiGenerator = new ThermalGenerator("psi-generator") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 45, NHItems.presstanium, 60));
            size = 2;
            health = 320;
            floating = true;
            powerProduction = 300f / 60f;

            lightColor = NHColor.darkEnrColor;

            attribute = NHBlocks.quantum;

            drawer = new DrawMulti(
                new DrawDefault(),
                new DrawGlowRegion() {{
                    color = NHColor.darkEnrColor;
                }}
            );
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
