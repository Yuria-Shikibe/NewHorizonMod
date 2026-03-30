package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.inner.LinkBlock;

import static mindustry.Vars.tilesize;

public class StreamRepeater extends StreamBlock {

    public StreamRepeater(String name) {
        super(name);

        streamLength = new int[]{7, -1, -1, -1};
        streamCap = new float[]{-1f, -1, -1, -1};
    }

    public class StreamRepeaterBuild extends StreamBuild {
        @Override
        public boolean acceptStream(StreamBeam stream) {
            return stream.getRotation() == rotation;
        }
    }
}
