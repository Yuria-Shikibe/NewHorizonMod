package newhorizon.expand.block.production.factory;

import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;

public interface MultiBlockEntity extends Buildingc {
    void updateLinkProximity();

    void invalidateEntity();

    Seq<Building> linkEntities();
}
