package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

import static mindustry.Vars.tilesize;

public class StreamSplitter extends StreamRepeater{
    public StreamSplitter(String name) {
        super(name);
    }

    public class StreamSplitterBuild extends StreamRepeaterBuild{
        public StreamBeam streamForward;
        public int lastDirection;

        @Override
        public void created() {
            super.created();
            streamForward = new StreamBeam(this);
        }

        @Override
        public void updateTile() {
            super.updateTile();
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
