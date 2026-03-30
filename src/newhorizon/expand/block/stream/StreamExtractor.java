package newhorizon.expand.block.stream;

import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import newhorizon.content.NHLiquids;

import static mindustry.Vars.tilesize;

public class StreamExtractor extends StreamRepeater {

    public StreamExtractor(String name) {
        super(name);

        streamLength = new int[]{5, -1, -1, -1};
        streamCap = new float[]{0.5f, -1, -1, -1};
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class StreamExtractorBuild extends StreamRepeaterBuild{

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
