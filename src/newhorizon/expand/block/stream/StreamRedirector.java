package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

import static mindustry.Vars.tilesize;

public class StreamRedirector extends StreamRepeater {
    public StreamRedirector(String name) {
        super(name);

        streamLength = new int[]{3, -1, -1, -1};
        streamCap = new float[]{-1, -1, -1, -1};
    }

    public class StreamRedirectorBuild extends StreamRepeaterBuild {
        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() + 2) % 4 != rotation;
        }
    }
}
