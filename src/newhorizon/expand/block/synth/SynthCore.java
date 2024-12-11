package newhorizon.expand.block.synth;

import arc.math.Interp;
import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.storage.CoreBlock;

public class SynthCore extends Wall {
    public int expansionRadius = 64;
    public int maxHeight = 16;
    public Interp altitudeInterp = Interp.pow2In;
    public SynthCore(String name) {
        super(name);
    }

    public class SynthCoreBuild extends WallBuild{
        //altitude - expand candidate;
        public IntMap<Seq<Tile>> expandMap;
        public IntMap<Seq<Building>> buildingMap;

        @Override
        public void created() {
            super.created();
            expandMap = new IntMap<>();
            for (int i = 0; i < maxHeight; i++){
                expandMap.put(i + 1, new Seq<>());
            }
        }
    }
}
