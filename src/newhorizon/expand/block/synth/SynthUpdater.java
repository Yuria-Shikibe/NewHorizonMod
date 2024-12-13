package newhorizon.expand.block.synth;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ByteSeq;
import arc.struct.Seq;
import newhorizon.expand.block.flood.FloodBuildingEntity;

import static mindustry.Vars.world;

public class SynthUpdater {
    public static final float UPDATE_INTERVAL = 15; //update 4fps should be enough
    public float updateTimer;

    //used to control the max altitude for tiles.
    public volatile ByteSeq altitude;
    public Seq<FloodBuildingEntity> allCores;
    public QuadTree<FloodBuildingEntity> coreBuildings;

    public AltitudeUpdater altitudeUpdater;

    /** init all stuff when a new map is loaded*/
    public void init(){
        allCores = new Seq<>();
        altitude = new ByteSeq(world.width() * world.height());
        coreBuildings = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
    }

    public int getMaxAltitude(int x, int y){
        return altitude.get(x + y * world.width());
    }

    public static class AltitudeUpdater extends Thread{
        @Override
        public void run() {
            super.run();
        }
    }
}
