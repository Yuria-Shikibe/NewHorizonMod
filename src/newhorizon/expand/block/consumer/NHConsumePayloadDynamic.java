package newhorizon.expand.block.consumer;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.type.PayloadStack;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.ConsumePayloadDynamic;
import mindustry.world.meta.StatValues;

public class NHConsumePayloadDynamic extends ConsumePayloadDynamic {
    public <T extends Building> NHConsumePayloadDynamic(Func<T, Seq<PayloadStack>> payloads) {
        super(payloads);
    }

    @Override
    public void build(Building build, Table table){}
}
