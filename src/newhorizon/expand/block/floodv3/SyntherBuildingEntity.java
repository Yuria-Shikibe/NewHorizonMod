package newhorizon.expand.block.floodv3;

import mindustry.gen.Buildingc;

public interface SyntherBuildingEntity extends Buildingc {
    SyntherGraph graph();

    void setGraph(SyntherGraph graph);

    void createGraph();

    void updateGraph();

    void removeGraph();
}
