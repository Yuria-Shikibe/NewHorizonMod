package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHLiquids;
import newhorizon.content.NHStats;
import newhorizon.expand.block.inner.LinkBlock;

import static mindustry.Vars.tilesize;

public class StreamRepeater extends StreamBlock {

    public StreamRepeater(String name) {
        super(name);

        streamLength = new int[]{7, -1, -1, -1};
        streamCap = new float[]{-1f, -1, -1, -1};
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.streamLength, streamLength[0]);
    }

    public class StreamRepeaterBuild extends StreamBuild {
        @Override
        public boolean acceptStream(StreamBeam stream) {
            return stream.getRotation() == rotation;
        }
    }
}
