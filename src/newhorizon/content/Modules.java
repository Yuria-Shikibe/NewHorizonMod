package newhorizon.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.ctype.UnlockableContent;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;

public class Modules {
    public static Block
            processorT1,
            armorT1,
            coreT1;

    public static ObjectMap<Block, ModuleCost> moduleCosts = new ObjectMap<>();
    public static ObjectMap<UnitType, PayloadSeq> unitCosts = new ObjectMap<>();

    public static void load(){
        processorT1 = new Wall("processor-t1");
        armorT1 = new Wall("armor-t1");
        coreT1 = new Wall("core-t1");

        moduleCosts.put(processorT1, moduleCost(Items.silicon, 25, Items.titanium, 20));
        moduleCosts.put(armorT1, moduleCost(Items.tungsten, 30, NHItems.presstanium, 20));
        moduleCosts.put(coreT1, moduleCost(NHItems.presstanium, 20, NHItems.zeta, 40));

        unitCosts.put(UnitTypes.poly, unitCost(processorT1, 2, armorT1, 1));
    }

    public static PayloadSeq unitCost(Object... objects){
        PayloadSeq payloadSeq = new PayloadSeq();
        PayloadStack[] payloadStacks = PayloadStack.with(objects);
        for(PayloadStack ps : payloadStacks){
            payloadSeq.add(ps.item, ps.amount);
        }
        return payloadSeq;
    }

    public static ModuleCost moduleCost(Object... objects){
        int len = objects.length / 2;
        ModuleCost cost = new ModuleCost();
        for(int i = 0; i < len; i++){
            Object content = objects[2 * i];
            Object amount = objects[2 * i + 1];
            if (content instanceof Item && amount instanceof Integer){
                cost.addItemReq(new ItemStack((Item) content, (Integer) amount));
            }
            if (content instanceof Liquid && amount instanceof Float){
                cost.addLiquidReq(new LiquidStack((Liquid) content, (Float) amount));
            }
            if (content instanceof UnlockableContent && amount instanceof Integer){
                cost.addPayloadReq(new PayloadStack((UnlockableContent) content, (Integer) amount));
            }
        }
        return cost;
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
