package newhorizon.expand.block.consumer;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.type.PayloadStack;
import mindustry.world.consumers.ConsumePayloadDynamic;

public class NHConsumePayloadDynamic extends ConsumePayloadDynamic {
    public <T extends Building> NHConsumePayloadDynamic(Func<T, Seq<PayloadStack>> payloads) {
        super(payloads);
    }

    @Override
    public void build(Building build, Table table) {}
}
