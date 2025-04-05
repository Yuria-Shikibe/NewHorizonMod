package newhorizon.content.blocks;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.*;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.block.special.JumpGate;

public class PowerBlock {
    public static Block zetaGenerator;

    public static void load(){
        zetaGenerator = new RecipeGenericCrafter("zeta-generator"){{
            requirements(Category.power, ItemStack.with(NHItems.metalOxhydrigen, 120,NHItems.juniorProcessor, 80,Items.plastanium, 80,NHItems.zeta,100,Items.copper, 150,Items.metaglass, 60));

            size = 3;

            rotate = false;

            loopSound = Sounds.electricHum;
            loopSoundVolume = 0.24F;
            itemCapacity = 30;
            liquidCapacity = 30;

            powerProduction = 50f;

            addInput(ItemStack.with(NHItems.zeta, 2, Items.carbide, 1), LiquidStack.with(NHLiquids.quantumLiquid, 6 / 60f));
            addInput(ItemStack.with(NHItems.zeta, 2, Items.carbide, 1), LiquidStack.with(Liquids.cryofluid, 6 / 60f));
            addInput(ItemStack.with(NHItems.zeta, 2, Items.carbide, 1), LiquidStack.with(Liquids.nitrogen, 6 / 60f));

            outputItem = new ItemStack(NHItems.fusionEnergy, 1);

            outputsPower = true;
            hasLiquids = hasItems = hasPower = true;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(NHLiquids.quantumLiquid),
                    new DrawLiquidTile(Liquids.cryofluid),
                    new DrawLiquidTile(Liquids.nitrogen),
                    new DrawDefault(),
                    new DrawGlowRegion(){{
                        color = NHItems.zeta.color;
                    }}
            );

            consumePower(0f);
            lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
            updateEffect = craftEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
        }};
    }
}
