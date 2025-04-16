package newhorizon.content.blocks;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.*;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.inner.ModulePayload;

public class ModuleBlock {
    public static Block
            processorT1, processorT2, processorT3, processorT4, processorT5,
            armorT1, armorT2, armorT3, armorT4, armorT5,
            coreT1, coreT2, coreT3, coreT4, coreT5,
            speedModule1, speedModule2, speedModule3,
            efficiencyModule1, efficiencyModule2, efficiencyModule3,
            productivityModule1, productivityModule2, productivityModule3;

    public static ObjectMap<Block, ModuleCost> moduleCosts = new ObjectMap<>();
    public static ObjectMap<UnitType, UnitCost> unitCosts = new ObjectMap<>();

    public static Seq<ModulePayload> modules = new Seq<>();

    public static void load(){
        processorT1 = new ModulePayload("processor-t1");
        processorT2 = new ModulePayload("processor-t2");
        processorT3 = new ModulePayload("processor-t3");
        processorT4 = new ModulePayload("processor-t4");
        processorT5 = new ModulePayload("processor-t5");

        armorT1 = new ModulePayload("armor-t1");
        armorT2 = new ModulePayload("armor-t2");
        armorT3 = new ModulePayload("armor-t3");
        armorT4 = new ModulePayload("armor-t4");
        armorT5 = new ModulePayload("armor-t5");

        coreT1 = new ModulePayload("core-t1");
        coreT2 = new ModulePayload("core-t2");
        coreT3 = new ModulePayload("core-t3");
        coreT4 = new ModulePayload("core-t4");
        coreT5 = new ModulePayload("core-t5");

        speedModule1 = new ModulePayload("speed-module-1");
        speedModule2 = new ModulePayload("speed-module-2");
        speedModule3 = new ModulePayload("speed-module-3");

        efficiencyModule1 = new ModulePayload("efficiency-module-1");
        efficiencyModule2 = new ModulePayload("efficiency-module-2");
        efficiencyModule3 = new ModulePayload("efficiency-module-3");

        productivityModule1 = new ModulePayload("productivity-module-1");
        productivityModule2 = new ModulePayload("productivity-module-2");
        productivityModule3 = new ModulePayload("productivity-module-3");

        moduleCosts.put(processorT1, new ModuleCost(){{
            itemReq = ItemStack.list(Items.silicon, 25, Items.titanium, 20);
            craftTime = 60 * 4f;
        }});
        moduleCosts.put(processorT2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25, NHItems.presstanium, 20);
            craftTime = 60 * 5f;
        }});
        moduleCosts.put(processorT3, new ModuleCost(){{
            itemReq = ItemStack.list(Items.surgeAlloy, 40);
            liquidReq = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            payloadReq = PayloadStack.list(processorT1, 2);
            craftTime = 60 * 8f;
        }});
        moduleCosts.put(processorT4, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.seniorProcessor, 50);
            liquidReq = LiquidStack.list(NHLiquids.irdryonFluid, 6 / 60f);
            payloadReq = PayloadStack.list(processorT2, 2);
            craftTime = 60 * 10f;
        }});
        moduleCosts.put(processorT5, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.ancimembrane, 150);
            payloadReq = PayloadStack.list(processorT3, 4, processorT4, 2);
            craftTime = 60 * 20f;
            outputMultiplier = 2;
        }});

        moduleCosts.put(armorT1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.presstanium, 60, Items.tungsten, 40);
            craftTime = 60 * 20f;
            outputMultiplier = 2;
        }});
        moduleCosts.put(armorT2, new ModuleCost(){{
            itemReq = ItemStack.list(Items.carbide, 120);
            payloadReq = PayloadStack.list(armorT1, 2);
            craftTime = 60 * 20f;
            outputMultiplier = 4;
        }});
        moduleCosts.put(armorT3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.multipleSteel, 80, NHItems.metalOxhydrigen, 120);
            liquidReq = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            craftTime = 60 * 30f;
            outputMultiplier = 3;
        }});
        moduleCosts.put(armorT4, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.setonAlloy, 200);
            payloadReq = PayloadStack.list(armorT3, 3);
            craftTime = 60 * 30f;
            outputMultiplier = 6;
        }});
        moduleCosts.put(armorT5, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.upgradeSort, 300);
            payloadReq = PayloadStack.list(armorT3, 4, armorT4, 8);
            craftTime = 60 * 40f;
            outputMultiplier = 6;
        }});

        moduleCosts.put(coreT1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.zeta, 90);
            payloadReq = PayloadStack.list(armorT1, 2);
            craftTime = 60 * 20f;
        }});
        moduleCosts.put(coreT2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.fusionEnergy, 120);
            payloadReq = PayloadStack.list(armorT1, 2);
            craftTime = 60 * 30f;
        }});
        moduleCosts.put(coreT3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.thermoCoreNegative, 200);
            payloadReq = PayloadStack.list(armorT3, 4);
            craftTime = 60 * 40f;
            outputMultiplier = 2;
        }});
        moduleCosts.put(coreT4, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.thermoCorePositive, 200);
            payloadReq = PayloadStack.list(armorT3, 4);
            craftTime = 60 * 40f;
            outputMultiplier = 2;
        }});
        moduleCosts.put(coreT5, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.darkEnergy, 400);
            payloadReq = PayloadStack.list(armorT5, 9);
            craftTime = 60 * 60f;
            outputMultiplier = 5;
        }});

        moduleCosts.put(speedModule1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.zeta, 40);
            payloadReq = PayloadStack.list(processorT1, 2);
        }});
        moduleCosts.put(speedModule2, new ModuleCost(){{
            itemReq = ItemStack.list(Items.surgeAlloy, 80);
            payloadReq = PayloadStack.list(speedModule1, 2, processorT2, 2);
        }});
        moduleCosts.put(speedModule3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.thermoCorePositive, 100);
            payloadReq = PayloadStack.list(speedModule2, 2, processorT3, 2, processorT4, 2);
        }});

        moduleCosts.put(efficiencyModule1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.metalOxhydrigen, 40);
            payloadReq = PayloadStack.list(processorT1, 2);
        }});
        moduleCosts.put(efficiencyModule2, new ModuleCost(){{
            itemReq = ItemStack.list(Items.phaseFabric, 80);
            payloadReq = PayloadStack.list(efficiencyModule1, 2, processorT2, 2);
        }});
        moduleCosts.put(efficiencyModule3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.thermoCoreNegative, 100);
            payloadReq = PayloadStack.list(efficiencyModule2, 2, processorT3, 2, processorT4, 2);
        }});

        moduleCosts.put(productivityModule1, new ModuleCost(){{
            itemReq = ItemStack.list(Items.carbide, 40);
            payloadReq = PayloadStack.list(processorT1, 2);
        }});
        moduleCosts.put(productivityModule2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.multipleSteel, 80);
            payloadReq = PayloadStack.list(productivityModule1, 2, processorT2, 2);
        }});
        moduleCosts.put(productivityModule3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.irayrondPanel, 100);
            payloadReq = PayloadStack.list(productivityModule2, 2, processorT3, 2, processorT4, 2);
        }});

        unitCosts.put(UnitTypes.poly, unitCost(60 * 10f, processorT1, 2, armorT1, 1));

    }

    public static UnitCost unitCost(float time, Object... objects){
        UnitCost uc = new UnitCost();
        uc.payloadSeq = PayloadStack.list(objects);
        uc.craftTime = time;
        return uc;
    }
    public static class UnitCost{
        public Seq<PayloadStack> payloadSeq = new Seq<>();
        public float craftTime = 60 * 10f;
    }

    public static class ModuleCost{
        public Seq<ItemStack> itemReq = new Seq<>();
        public Seq<LiquidStack> liquidReq = new Seq<>();
        public Seq<PayloadStack> payloadReq = new Seq<>();
        public float craftTime = 240f;
        public int outputMultiplier = 1;
    }
}
