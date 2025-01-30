package newhorizon.expand.block.distribution.transport;

import mindustry.gen.Building;

public interface LogisticBuild {
    boolean canSend(Building target);
    boolean canReceive(Building source);
}
