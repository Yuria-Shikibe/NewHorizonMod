package newhorizon.content.register;

import arc.func.Cons;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.content.blocks.CraftingBlock;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.type.Recipe;

public class RecipeRegister {
    /*
    to va:你把下面这对配方，120，80，60这种需求的都改到20左右
    贵点输入的15物品，10物品，便宜输入的30物品这种逻辑

    工厂顺序参见CraftingBlocks.java
    物品你对照ModuleBlocks.java找
    纯脏活累活，va你干活吧，干完活了帮我吧这段注释删了

    线路工具:10硅 10钨
    动力模块:2线路工具 20羟金 20重混合钢
    仿生处理器:2线路工具 20高清钢 20芯片
    记忆调整器:2动力模块 40相织布 40量子芯片
    电阻阵列:2仿生处理器 2记忆调整器 120自塑合金

    晶体二极管:10芯片 10重混合钢
    质子电容器:2晶体二极管 20碳化物 20芯片
    强子缓冲器:2晶体二极管 20巨浪合金 20量子芯片
    快子发射器:2质子电容器 40铱板 40量子芯片
    中子膜层:2强子缓冲器 2快子发射器 120幽膜

    装甲铸件:20重混合钢 20钨
    热能探测器:2装甲铸件 40碳化物
    高斯接收器:2装甲铸件 40铱板
    回声消除器:2热能探测器 80致密合金
    脉冲变异器:2高斯接收器 2回声消除器 120自塑合金

    能量电池:20硅 20zeta
    裂变电池:2能量电池 40巨浪合金
    充能补偿器:2能量电池 40相织布
    聚变反应堆:2裂变电池 40聚变能
    多相推进剂:2充能补偿器 2聚变反应堆 60零位能 60反零能

    超凝胶体:12量子体 20钨
    冷却单元:2超凝胶体 12xen 40羟金
    信号环流器:2超凝胶体 8正zeta流体 8负zeta流体
    粒子调整器:2冷却单元 4化工液 80致密合金
    量子导体:2信号环流器 2粒子调整器 120幽膜
     */
    public static void load(){
        //va你按这个来就好
        input(CraftingBlock.electronicFacilityBasic, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 15, Items.tungsten, 15));
        input(CraftingBlock.electronicFacilityBasic, recipe -> recipe.inputItem = ItemStack.list(Items.graphite, 15, Items.metaglass, 15));
        input(CraftingBlock.electronicFacilityBasic, recipe -> recipe.inputItem = ItemStack.list(Items.beryllium, 15, Items.oxide, 15));
        output(CraftingBlock.electronicFacilityBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.wiringKit, 2);
        });

        input(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.presstanium, 20, NHItems.metalOxhydrigen, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });  
        input(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.thorium, 20, Items.plastanium, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        input(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.thorium, 20, Items.carbide, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });  
        output(CraftingBlock.electronicFacilityRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.powerUnit, 2);
        });

        input(CraftingBlock.electronicFacilityUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 20, NHItems.multipleSteel, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        input(CraftingBlock.electronicFacilityUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 15, Items.phaseFabric, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        output(CraftingBlock.electronicFacilityUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.bionicsProcessor, 2);
        });

        input(CraftingBlock.electronicFacilityEpic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 20, NHItems.seniorProcessor, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
        });
        output(CraftingBlock.electronicFacilityEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.memoryRecalibrator, 2);
        });

        input(CraftingBlock.electronicFacilityLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 2, ModuleBlock.memoryRecalibrator, 2);
        });
        output(CraftingBlock.electronicFacilityLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.neutronMembrane, 2);
        });

      
        input(CraftingBlock.particleProcessorBasic, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 15, NHItems.juniorProcessor, 15));
        output(CraftingBlock.particleProcessorBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.crystalDiode, 2);
        });

        input(CraftingBlock.particleProcessorRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 15, NHItems.juniorProcessor, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
        });
        output(CraftingBlock.particleProcessorRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.protonCapacitor, 2);
        });

        input(CraftingBlock.particleProcessorUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 15, NHItems.seniorProcessor, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
        });
        output(CraftingBlock.particleProcessorUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.hadronBuffers, 2);
        });

        input(CraftingBlock.particleProcessorEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 15, NHItems.irayrondPanel, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.protonCapacitor, 2);
        });
        output(CraftingBlock.particleProcessorEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.tachyonEmitter, 2);
        });

        input(CraftingBlock.particleProcessorLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.hadronBuffers, 2, ModuleBlock.tachyonEmitter, 2);
        });
        output(CraftingBlock.particleProcessorLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.neutronMembrane, 2);
        });


        input(CraftingBlock.foundryBasic, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 20, NHItems.presstanium, 10));
        output(CraftingBlock.foundryBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.armorCast, 2);
        });

        input(CraftingBlock.foundryRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 30);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
        });
        output(CraftingBlock.foundryRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.heatDetector, 2);
        });

        input(CraftingBlock.foundryUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 30);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
        });
        output(CraftingBlock.foundryUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.gaussReceptor, 2);
        });

        input(CraftingBlock.foundryEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.heatDetector, 2);
        });
        output(CraftingBlock.foundryEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.echoCanceller, 2);
        });

        input(CraftingBlock.foundryLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 2, ModuleBlock.echoCanceller, 2);
        });
        output(CraftingBlock.foundryLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.pulseMutator, 2);
        });


        input(CraftingBlock.powerBasic, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 20, NHItems.zeta, 10));
        input(CraftingBlock.powerBasic, recipe -> recipe.inputItem = ItemStack.list(Items.titanium, 20, Items.thorium, 20));
        input(CraftingBlock.powerBasic, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 20, Items.thorium, 20));
        output(CraftingBlock.powerBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.powerCell, 2);
        });

        input(CraftingBlock.powerRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 20);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });
        output(CraftingBlock.powerRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.fissionCell, 2);
        });

        input(CraftingBlock.powerUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 20);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });
        output(CraftingBlock.powerUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.chargeCompensator, 2);
        });

        input(CraftingBlock.powerEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.fusionEnergy, 20);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.fissionCell, 2);
        });
        output(CraftingBlock.powerEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.fusionReactor, 2);
        });

        input(CraftingBlock.powerLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 15, NHItems.thermoCoreNegative, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.chargeCompensator, 2, ModuleBlock.fusionReactor, 2);
        });
        output(CraftingBlock.powerLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.multiphasePropellant, 2);
        });


        input(CraftingBlock.componentBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 20);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 12 / 60f);
        });
        input(CraftingBlock.componentBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 20);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 12 / 60f);
        });  
        output(CraftingBlock.componentBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.supraGel, 2);
        });

        input(CraftingBlock.componentRare, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 30);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
        });
        output(CraftingBlock.componentRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.coolingUnit, 2);
        });

        input(CraftingBlock.componentUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel， 20);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
        });
        output(CraftingBlock.componentUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.signalCirculator, 2);
        });

        input(CraftingBlock.componentEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 20);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 2);
        });
        output(CraftingBlock.componentEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.particleModulator, 2);
        });

        input(CraftingBlock.componentLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 10);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 2, ModuleBlock.particleModulator, 2);
        });
        output(CraftingBlock.componentLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.quantumConductor, 2);
        });
}

    @SafeVarargs
    public static void registerBlockRecipe(Block block, Cons<AdaptCrafter> output, Cons<Recipe>...recipe){
        output(block, output);
        for(Cons<Recipe> r : recipe){
            input(block, r);
        }
    }

    public static void input(Block block, Cons<Recipe> recipe) {
        if (block instanceof RecipeGenericCrafter crafter) {
            Recipe r = new Recipe();
            recipe.get(r);
            crafter.recipes.add(r);
        }
    }

    public static void output(Block block, Cons<AdaptCrafter> output) {
        if (block instanceof AdaptCrafter crafter) {
            output.get(crafter);
        }
    }
}
