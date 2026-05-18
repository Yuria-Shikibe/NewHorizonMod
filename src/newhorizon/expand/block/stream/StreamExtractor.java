package newhorizon.expand.block.stream;

import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHLiquids;
import newhorizon.content.NHStats;

public class StreamExtractor extends StreamRepeater {

    public StreamExtractor(String name) {
        super(name);

        streamLength = new int[]{5, -1, -1, -1};
        streamCap = new float[]{0.5f, -1, -1, -1};
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.streamCap, streamCap[0] * Time.toSeconds, StatUnit.perSecond);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public class StreamExtractorBuild extends StreamRepeaterBuild {

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return liquid instanceof NHLiquids.Stream;
        }
    }
}
