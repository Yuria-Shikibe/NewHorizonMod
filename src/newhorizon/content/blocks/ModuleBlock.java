package newhorizon.content.blocks;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.content.NHUnitTypes;
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

    public static void load() {
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

        moduleCosts.put(processorT1, new ModuleCost() {{
            itemReq = ItemStack.list(Items.silicon, 10, Items.titanium, 10);
            craftTime = 60 * 5f;
        }});
        moduleCosts.put(processorT2, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 10, NHItems.presstanium, 10);
            craftTime = 60 * 5f;
        }});
        moduleCosts.put(processorT3, new ModuleCost() {{
            itemReq = ItemStack.list(Items.surgeAlloy, 20);
            liquidReq = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            payloadReq = PayloadStack.list(processorT1, 2);
            craftTime = 60 * 10f;
        }});
        moduleCosts.put(processorT4, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.seniorProcessor, 20);
            liquidReq = LiquidStack.list(NHLiquids.irdryonFluid, 6 / 60f);
            payloadReq = PayloadStack.list(processorT2, 2);
            craftTime = 60 * 10f;
        }});
        moduleCosts.put(processorT5, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.ancimembrane, 30);
            payloadReq = PayloadStack.list(processorT3, 4, processorT4, 2);
            craftTime = 60 * 20f;
            outputMultiplier = 2;
        }});

        moduleCosts.put(armorT1, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.presstanium, 20, Items.tungsten, 20);
            craftTime = 60 * 20f;
            outputMultiplier = 2;
        }});
        moduleCosts.put(armorT2, new ModuleCost() {{
            itemReq = ItemStack.list(Items.carbide, 40);
            payloadReq = PayloadStack.list(armorT1, 2);
            craftTime = 60 * 20f;
            outputMultiplier = 4;
        }});
        moduleCosts.put(armorT3, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.multipleSteel, 30, NHItems.metalOxhydrigen, 45);
            liquidReq = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            craftTime = 60 * 30f;
            outputMultiplier = 3;
        }});
        moduleCosts.put(armorT4, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.setonAlloy, 90);
            payloadReq = PayloadStack.list(armorT3, 3);
            craftTime = 60 * 30f;
            outputMultiplier = 6;
        }});
        moduleCosts.put(armorT5, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.upgradeSort, 120);
            payloadReq = PayloadStack.list(armorT3, 4, armorT4, 8);
            craftTime = 60 * 40f;
            outputMultiplier = 6;
        }});

        moduleCosts.put(coreT1, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.zeta, 20);
            payloadReq = PayloadStack.list(armorT1, 2);
            craftTime = 60 * 20f;
        }});
        moduleCosts.put(coreT2, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.fusionEnergy, 30);
            payloadReq = PayloadStack.list(armorT1, 2);
            craftTime = 60 * 30f;
        }});
        moduleCosts.put(coreT3, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.thermoCoreNegative, 40);
            payloadReq = PayloadStack.list(armorT3, 4);
            craftTime = 60 * 40f;
            outputMultiplier = 2;
        }});
        moduleCosts.put(coreT4, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.thermoCorePositive, 40);
            payloadReq = PayloadStack.list(armorT3, 4);
            craftTime = 60 * 40f;
            outputMultiplier = 2;
        }});
        moduleCosts.put(coreT5, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.darkEnergy, 120);
            payloadReq = PayloadStack.list(armorT5, 4);
            craftTime = 60 * 60f;
            outputMultiplier = 8;
        }});

        moduleCosts.put(speedModule1, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.zeta, 40);
            payloadReq = PayloadStack.list(processorT1, 2);
            craftTime = 60 * 10f;
        }});
        moduleCosts.put(speedModule2, new ModuleCost() {{
            itemReq = ItemStack.list(Items.surgeAlloy, 80);
            payloadReq = PayloadStack.list(speedModule1, 2, processorT2, 2);
            craftTime = 60 * 20f;
        }});
        moduleCosts.put(speedModule3, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.thermoCorePositive, 100);
            payloadReq = PayloadStack.list(speedModule2, 2, processorT3, 2, processorT4, 2);
            craftTime = 60 * 40f;
        }});

        moduleCosts.put(efficiencyModule1, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.metalOxhydrigen, 40);
            payloadReq = PayloadStack.list(processorT1, 2);
            craftTime = 60 * 10f;
        }});
        moduleCosts.put(efficiencyModule2, new ModuleCost() {{
            itemReq = ItemStack.list(Items.phaseFabric, 80);
            payloadReq = PayloadStack.list(efficiencyModule1, 2, processorT2, 2);
            craftTime = 60 * 20f;
        }});
        moduleCosts.put(efficiencyModule3, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.thermoCoreNegative, 100);
            payloadReq = PayloadStack.list(efficiencyModule2, 2, processorT3, 2, processorT4, 2);
            craftTime = 60 * 40f;
        }});

        moduleCosts.put(productivityModule1, new ModuleCost() {{
            itemReq = ItemStack.list(Items.carbide, 40);
            payloadReq = PayloadStack.list(processorT1, 2);
            craftTime = 60 * 10f;
        }});
        moduleCosts.put(productivityModule2, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.multipleSteel, 80);
            payloadReq = PayloadStack.list(productivityModule1, 2, processorT2, 2);
            craftTime = 60 * 20f;
        }});
        moduleCosts.put(productivityModule3, new ModuleCost() {{
            itemReq = ItemStack.list(NHItems.irayrondPanel, 100);
            payloadReq = PayloadStack.list(productivityModule2, 2, processorT3, 2, processorT4, 2);
            craftTime = 60 * 40f;
        }});

        unitCosts.put(UnitTypes.poly, unitCost(60 * 10f, processorT1, 1, armorT1, 1));
        unitCosts.put(UnitTypes.cleroi, unitCost(60 * 15f, processorT1, 3, armorT1, 3));
        unitCosts.put(UnitTypes.quasar, unitCost(60 * 20f, processorT1, 2, armorT1, 3, coreT1, 1));
        unitCosts.put(UnitTypes.zenith, unitCost(60 * 20f, processorT1, 3, armorT1, 2, coreT1, 1));
        unitCosts.put(UnitTypes.cyerce, unitCost(60 * 20f, processorT1, 3, armorT1, 3, coreT1, 1));
        unitCosts.put(NHUnitTypes.ghost, unitCost(60 * 30f, processorT2, 2, armorT2, 3, coreT1, 2, productivityModule1, 1));
        unitCosts.put(NHUnitTypes.warper, unitCost(60 * 30f, processorT2, 3, armorT2, 2, coreT1, 2, speedModule1, 1));
        unitCosts.put(NHUnitTypes.aliotiat, unitCost(60 * 30f, processorT2, 3, armorT2, 3, coreT1, 2, efficiencyModule1, 1));
        unitCosts.put(NHUnitTypes.rhino, unitCost(60 * 45f, processorT2, 5, armorT1, 3, coreT2, 2, speedModule1, 2));
        unitCosts.put(NHUnitTypes.gather, unitCost(60 * 45f, processorT2, 3, armorT1, 5, coreT2, 2, efficiencyModule1, 2));

        unitCosts.put(NHUnitTypes.naxos, unitCost(60 * 45f, processorT3, 6, armorT2, 4, coreT2, 3, speedModule2, 2));
        unitCosts.put(NHUnitTypes.tarlidor, unitCost(60 * 45f, processorT3, 5, armorT2, 5, coreT2, 3, efficiencyModule2, 2));
        unitCosts.put(NHUnitTypes.zarkov, unitCost(60 * 45f, processorT3, 4, armorT2, 6, coreT2, 3, productivityModule2, 2));
        unitCosts.put(UnitTypes.eclipse, unitCost(60 * 60f, processorT3, 6, armorT3, 10, coreT2, 5, productivityModule2, 3));
        unitCosts.put(UnitTypes.disrupt, unitCost(60 * 60f, processorT3, 10, armorT3, 6, coreT2, 5, productivityModule2, 3));
        unitCosts.put(UnitTypes.corvus, unitCost(60 * 75f, processorT4, 20, armorT3, 10, coreT2, 10, speedModule2, 3));
        unitCosts.put(UnitTypes.navanax, unitCost(60 * 75f, processorT4, 15, armorT3, 15, coreT2, 10, efficiencyModule2, 3));
        unitCosts.put(UnitTypes.collaris, unitCost(60 * 75f, processorT4, 10, armorT3, 20, coreT2, 10, productivityModule2, 3));

        unitCosts.put(NHUnitTypes.destruction, unitCost(60 * 90f, processorT4, 15, armorT3, 20, coreT3, 10, speedModule3, 3));
        unitCosts.put(NHUnitTypes.longinus, unitCost(60 * 90f, processorT4, 20, armorT3, 15, coreT4, 10, efficiencyModule3, 3));
        unitCosts.put(NHUnitTypes.annihilation, unitCost(60 * 120f, processorT4, 20, armorT4, 30, coreT3, 15, speedModule3, 4));
        unitCosts.put(NHUnitTypes.saviour, unitCost(60 * 120f, processorT4, 25, armorT4, 25, coreT3, 10, coreT4, 10, efficiencyModule3, 4));
        unitCosts.put(NHUnitTypes.declining, unitCost(60 * 120f, processorT4, 30, armorT4, 20, coreT4, 15, productivityModule3, 6));
        unitCosts.put(NHUnitTypes.anvil, unitCost(60 * 150f, processorT5, 40, armorT5, 30, coreT3, 25, speedModule3, 8, productivityModule3, 5));
        unitCosts.put(NHUnitTypes.hurricane, unitCost(60 * 150f, processorT5, 35, armorT5, 35, coreT3, 15, coreT4, 15, efficiencyModule3, 12));
        unitCosts.put(NHUnitTypes.sin, unitCost(60 * 150f, processorT5, 30, armorT5, 40, coreT4, 25, efficiencyModule3, 8, productivityModule3, 5));
        unitCosts.put(NHUnitTypes.collapser, unitCost(60 * 180f, processorT5, 50, armorT5, 75, coreT5, 25, productivityModule3, 15, speedModule3, 8, efficiencyModule3, 8));

    }

    public static UnitCost unitCost(float time, Object... objects) {
        UnitCost uc = new UnitCost();
        uc.payloadSeq = PayloadStack.list(objects);
        uc.craftTime = time;
        return uc;
    }

    public static class UnitCost {
        public Seq<PayloadStack> payloadSeq = new Seq<>();
        public float craftTime = 60 * 10f;
    }

    public static class ModuleCost {
        public Seq<ItemStack> itemReq = new Seq<>();
        public Seq<LiquidStack> liquidReq = new Seq<>();
        public Seq<PayloadStack> payloadReq = new Seq<>();
        public float craftTime = 240f;
        public int outputMultiplier = 1;
    }
}
