package newhorizon.expand.block.payload;

import arc.struct.Seq;
import mindustry.type.ItemStack;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.Constructor;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumePayloadDynamic;
import newhorizon.content.Modules;

public class PayloadFactory extends Constructor {
    public PayloadFactory(String name) {
        super(name);


        for (Consume cons: consumers){
            if (cons instanceof ConsumeItemDynamic){
                removeConsumer(cons);
            }
        }

        consume(new ConsumePayloadDynamic(PayloadFactoryBuild::getPayloadReq));

        consume(new ConsumeItemDynamic((BlockProducerBuild e) -> {
            Block block = e.recipe();
            if(block != null && Modules.moduleCosts.get(block) != null){
                return Modules.moduleCosts.get(block).itemReq.copy().toArray(ItemStack.class);
            }else{
                return ItemStack.empty;
            }
        }));
    }

    public class PayloadFactoryBuild extends ConstructorBuild {
        public PayloadSeq payloads = new PayloadSeq();

        public Seq<PayloadStack> getPayloadReq() {
            if (recipe == null) return new Seq<>();
            Seq<PayloadStack> stacks = Modules.moduleCosts.get(recipe()).payloadReq;
            return stacks == null ? new Seq<>() : stacks;
        }

        @Override
        public PayloadSeq getPayloads() {
            return payloads;
        }
    }
}
