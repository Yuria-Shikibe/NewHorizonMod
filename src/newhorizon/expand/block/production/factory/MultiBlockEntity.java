package newhorizon.expand.block.production.factory;

import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import newhorizon.expand.block.inner.LinkBlock.LinkBuild;

public interface MultiBlockEntity extends Buildingc {
    void updateLinkProximity();

    //void setLinkBuild();

    //void removeLinkBuild();
}
