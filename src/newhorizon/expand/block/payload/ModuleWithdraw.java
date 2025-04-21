package newhorizon.expand.block.payload;

import arc.math.Mathf;
import mindustry.type.PayloadSeq;
import mindustry.world.blocks.payloads.BuildPayload;
import newhorizon.expand.block.consumer.NHConsumeShowStat;

import static newhorizon.NHVars.worldData;

public class ModuleWithdraw extends ModuleSource{
    public ModuleWithdraw(String name) {
        super(name);
        enableDrawStatus = false;
        consume(new NHConsumeShowStat(e -> null, e -> null, e -> null, b -> worldData.teamPayloadData.getPayload(b.team)));
    }

    public class ModuleWithdrawBuild extends ModuleSourceBuild{
        @Override
        public void updateTile() {
            if(payload != null){
                payload.update(null, this);
            }
            if(payload == null){
                PayloadSeq storage = worldData.teamPayloadData.getPayload(team);
                if(configBlock != null && storage.get(configBlock) > 0){
                    payload = new BuildPayload(configBlock, team);
                    storage.remove(configBlock, 1);
                }
                payVector.setZero();
                payRotation = rotdeg();
            }
            scl = Mathf.lerpDelta(scl, 1f, 0.1f);
            moveOutPayload();
        }
    }
}
