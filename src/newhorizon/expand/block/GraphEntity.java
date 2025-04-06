package newhorizon.expand.block;

import arc.struct.Seq;
import mindustry.gen.Buildingc;

public interface GraphEntity extends Buildingc {
    GraphBuildings graph();

    Seq<GraphEntity> proximityEntities();
}
