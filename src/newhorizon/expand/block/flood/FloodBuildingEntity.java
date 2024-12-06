package newhorizon.expand.block.flood;

import mindustry.gen.Buildingc;

public interface FloodBuildingEntity extends Buildingc {

    FloodGraph graph();

    void setGraph(FloodGraph graph);

    void createGraph();

    void updateGraph();

    void removeGraph();
}
