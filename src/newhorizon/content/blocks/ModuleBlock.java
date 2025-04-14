package newhorizon.content.blocks;

import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.expand.block.payload.PayloadFactory;

public class ModuleBlock {
    public static Block
            processorT1, processorT2, processorT3, processorT4, processorT5,
            armorT1,
            coreT1,
            speedModule1, speedModule2, speedModule3,
            efficiencyModule1, efficiencyModule2, efficiencyModule3,
            productivityModule1, productivityModule2, productivityModule3;

    public static ObjectMap<Block, ModuleCost> moduleCosts = new ObjectMap<>();
    public static ObjectMap<UnitType, PayloadSeq> unitCosts = new ObjectMap<>();

    public static Block moduleWorkshop;

    public static void load(){
        processorT1 = new ModulePayload("processor-t1");
        processorT2 = new ModulePayload("processor-t2");
        processorT3 = new ModulePayload("processor-t3");
        processorT4 = new ModulePayload("processor-t4");

        armorT1 = new Wall("armor-t1");
        coreT1 = new Wall("core-t1");

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
        }});
        moduleCosts.put(processorT2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(processorT1, 1);
        }});
        moduleCosts.put(processorT3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.seniorProcessor, 25);
            liquidReq = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            payloadReq = PayloadStack.list(processorT2, 1);
        }});
        moduleCosts.put(processorT4, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.ancimembrane, 25);
            liquidReq = LiquidStack.list(NHLiquids.irdryonFluid, 6 / 60f);
            payloadReq = PayloadStack.list(processorT3, 1);
        }});

        moduleCosts.put(speedModule1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(processorT1, 2);
        }});
        moduleCosts.put(speedModule2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(speedModule1, 2);
        }});
        moduleCosts.put(speedModule3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(speedModule2, 2);
        }});

        moduleCosts.put(efficiencyModule1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(processorT1, 2);
        }});
        moduleCosts.put(efficiencyModule2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(efficiencyModule1, 2);
        }});
        moduleCosts.put(efficiencyModule3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(efficiencyModule2, 2);
        }});

        moduleCosts.put(productivityModule1, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(processorT1, 2);
        }});
        moduleCosts.put(productivityModule2, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(productivityModule1, 2);
        }});
        moduleCosts.put(productivityModule3, new ModuleCost(){{
            itemReq = ItemStack.list(NHItems.juniorProcessor, 25);
            payloadReq = PayloadStack.list(productivityModule2, 2);
        }});



        unitCosts.put(UnitTypes.poly, unitCost(processorT1, 2, armorT1, 1));

        moduleWorkshop = new PayloadFactory("cpu-factory"){{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with());

            size = 2;
            addLink(-1, 0, 1, -1, 1, 1, 0, -1, 1, 1, -1, 1, 0, 2, 1, 1, 2, 1, 2, 0, 1, 2, 1, 1);
            acceptPos = Seq.with(new Point2(-2, 0), new Point2(-2, 1));
            targetPos = Seq.with(new Point2(3, 0), new Point2(3, 1));

            filter = Seq.with(processorT1, processorT2, processorT3, processorT4, speedModule1, speedModule2, speedModule3, efficiencyModule1, efficiencyModule2, efficiencyModule3, productivityModule1, productivityModule2, productivityModule3);
        }};
    }

    public static PayloadSeq unitCost(Object... objects){
        PayloadSeq payloadSeq = new PayloadSeq();
        PayloadStack[] payloadStacks = PayloadStack.with(objects);
        for(PayloadStack ps : payloadStacks){
            payloadSeq.add(ps.item, ps.amount);
        }
        return payloadSeq;
    }

    public static class ModuleCost{
        public Seq<ItemStack> itemReq = new Seq<>();
        public Seq<LiquidStack> liquidReq = new Seq<>();
        public Seq<PayloadStack> payloadReq = new Seq<>();

        public void addItemReq(ItemStack item){
            itemReq.add(item);
        }

        public void addLiquidReq(LiquidStack liquid){
            liquidReq.add(liquid);
        }

        public void addPayloadReq(PayloadStack payload){
            payloadReq.add(payload);
        }
    }
}
