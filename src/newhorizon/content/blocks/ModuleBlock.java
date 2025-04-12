package newhorizon.content.blocks;

import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.ctype.UnlockableContent;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.payload.PayloadFactory;

public class ModuleBlock {
    public static Block
            processorT1, processorT2, processorT3, processorT4, processorT5,
            armorT1,
            coreT1;

    public static ObjectMap<Block, ModuleCost> moduleCosts = new ObjectMap<>();
    public static ObjectMap<UnitType, PayloadSeq> unitCosts = new ObjectMap<>();

    public static Block moduleWorkshop;

    public static void load(){
        processorT1 = new Wall("processor-t1"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with());
        }};
        processorT2 = new Wall("processor-t2"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with());
        }};
        processorT3 = new Wall("processor-t3"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with());
        }};
        processorT4 = new Wall("processor-t4"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with());
        }};
        armorT1 = new Wall("armor-t1");
        coreT1 = new Wall("core-t1");

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

        unitCosts.put(UnitTypes.poly, unitCost(processorT1, 2, armorT1, 1));

        moduleWorkshop = new PayloadFactory("module-workshop"){{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with());

            size = 4;
            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1, 3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);
            acceptPos = Seq.with(new Point2(-3, 2), new Point2(-3, 1), new Point2(-3, 0), new Point2(-3, -1));
            targetPos = Seq.with(new Point2(4, 2), new Point2(4, 1), new Point2(4, 0), new Point2(4, -1));

            filter = Seq.with(processorT1, processorT2, processorT3, processorT4);
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
