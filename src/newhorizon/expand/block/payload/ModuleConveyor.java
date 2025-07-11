package newhorizon.expand.block.payload;

import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.PayloadConveyor;
import mindustry.world.meta.Stat;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.expand.block.production.factory.AdaptCrafter;

public class ModuleConveyor extends PayloadConveyor {
    public ModuleConveyor(String name) {
        super(name);
        size = 1;
        moveTime = 30;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.payloadCapacity);
    }

    public class AdaptPayloadConveyorBuild extends PayloadConveyorBuild {
        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return super.acceptPayload(source, payload) && payload.content() instanceof ModulePayload;
        }

        @Override
        protected boolean blends(int direction){
            if(direction == rotation){
                return !blocked || next != null;
            }

            Building accept = nearby(Geometry.d4(direction).x, Geometry.d4(direction).y);
            if (accept instanceof AdaptCrafter.AdaptCrafterBuild) return true;
            if (accept instanceof LinkBlock.LinkBuild) return true;
            return PayloadBlock.blends(this, direction);
        }
    }
}
