package newhorizon.expand.block.payload;

import mindustry.gen.Building;
import mindustry.world.blocks.payloads.Payload;

import static newhorizon.NHVars.worldData;

public class ModuleDeposit extends ModuleVoid{
    public ModuleDeposit(String name) {
        super(name);
    }

    public class ModuleDepositBuild extends ModuleVoidBuild{
        @Override
        public void handlePayload(Building source, Payload payload) {
            super.handlePayload(source, payload);
            worldData.teamPayloadData.getPayload(team).add(payload.content(), 1);
        }
    }
}
