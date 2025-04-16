package newhorizon.expand.block.payload;

import arc.graphics.g2d.Draw;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.PayloadSeq;
import mindustry.world.blocks.payloads.Payload;
import newhorizon.expand.block.consumer.NHConsumeShowStat;

import static newhorizon.NHVars.worldData;

public class ModuleDeposit extends ModuleVoid{
    public ModuleDeposit(String name) {
        super(name);
        enableDrawStatus = false;
        consume(new NHConsumeShowStat(e -> null, e -> null, e -> null, b -> worldData.teamPayloadData.getPayload(b.team)));
    }

    public class ModuleDepositBuild extends ModuleVoidBuild{
        @Override
        public void handlePayload(Building source, Payload payload) {
            super.handlePayload(source, payload);
            worldData.teamPayloadData.getPayload(team).add(payload.content(), 1);
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.reset();
        }
    }
}
