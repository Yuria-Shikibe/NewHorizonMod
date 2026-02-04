package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

import static mindustry.Vars.tilesize;

public class StreamSplitter extends StreamRepeater{
    public StreamSplitter(String name) {
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

        Drawf.dashLine(Pal.placing,
                x * tilesize + Geometry.d4[(rotation + 1) % 4].x * (tilesize / 2f + 2),
                y * tilesize + Geometry.d4[(rotation + 1) % 4].y * (tilesize / 2f + 2),
                x * tilesize + Geometry.d4[(rotation + 1) % 4].x * 6 * tilesize,
                y * tilesize + Geometry.d4[(rotation + 1) % 4].y * 6 * tilesize);

        Drawf.dashLine(Pal.placing,
                x * tilesize + Geometry.d4[(rotation + 3) % 4].x * (tilesize / 2f + 2),
                y * tilesize + Geometry.d4[(rotation + 3) % 4].y * (tilesize / 2f + 2),
                x * tilesize + Geometry.d4[(rotation + 3) % 4].x * 6 * tilesize,
                y * tilesize + Geometry.d4[(rotation + 3) % 4].y * 6 * tilesize);
    }


    public class StreamSplitterBuild extends StreamRepeaterBuild{
        public StreamBeam streamForward;
        public int lastDirection;

        @Override
        public void created() {
            super.created();
            efficiency = 1f;
            stream.beamLength = 3;
            stream.amountCap = 0.1f;

            streamForward = new StreamBeam(this);
        }

        @Override
        public void update() {
            stream.update();

            streamForward.rotationOffset = lastDirection;
            streamForward.update();
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() % 2) != (rotation % 2);
        }

        @Override
        public void handleStream(StreamBeam stream) {
            lastDirection = (stream.getRotation() - rotation);
        }

        @Override
        public void draw() {
            super.draw();
            streamForward.draw();
        }
    }
}
