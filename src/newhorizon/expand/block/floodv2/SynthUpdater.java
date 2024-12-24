package newhorizon.expand.block.floodv2;

import arc.math.Rand;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Shieldc;

import static mindustry.Vars.world;

public class SynthUpdater {
    public static final float UPDATE_INTERVAL = 15; //update 4fps should be enough
    public float updateTimer;

    //used to control the max altitude for tiles.
    public QuadTree<SynthCore.SynthCoreBuild> tree;
    public Seq<Building> nodes = new Seq<>();
    public Seq<Shieldc> bridge = new Seq<>();


    public Rand rand = new Rand();

    //when new game about to load, worldReset everything.
    public void worldReset(){
        tree = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
    }

    //when blocks are added, set things.
    public void worldInit(){

    }
}
