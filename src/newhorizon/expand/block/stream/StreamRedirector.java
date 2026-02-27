package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

import static mindustry.Vars.tilesize;

public class StreamRedirector extends StreamRepeater {
    public StreamRedirector(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawPotentialLinks(x, y);
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
        Drawf.dashLine(Pal.placing,
                x * tilesize + Geometry.d4[rotation].x * (tilesize / 2f + 2),
                y * tilesize + Geometry.d4[rotation].y * (tilesize / 2f + 2),
                x * tilesize + Geometry.d4[rotation].x * 4 * tilesize,
                y * tilesize + Geometry.d4[rotation].y * 4 * tilesize);
    }

    public class StreamRedirectorBuild extends StreamRepeaterBuild {
        @Override
        public void created() {
            super.created();
            stream.beamLength = 3;
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() + 2) % 4 != rotation;
        }
    }
}
