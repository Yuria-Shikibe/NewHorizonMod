package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import arc.util.Time;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHStats;

import static mindustry.Vars.tilesize;

public class StreamSplitter extends StreamRepeater{
    public StreamSplitter(String name) {
        super(name);

        streamLength = new int[]{3, -1, -1, -1};
        streamCap = new float[]{0.1f, -1, -1, -1};
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.streamCap, streamCap[0] * Time.toSeconds, StatUnit.perSecond);
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
