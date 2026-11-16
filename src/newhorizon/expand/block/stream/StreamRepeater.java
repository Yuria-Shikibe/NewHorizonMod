package newhorizon.expand.block.stream;

import newhorizon.content.NHStats;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.input.Placement;

public class StreamRepeater extends StreamBlock {

    public StreamRepeater(String name) {
        super(name);

        streamLength = new int[]{7, -1, -1, -1};
        streamCap = new float[]{-1f, -1, -1, -1};
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        int range = streamLength[0] + 1;
        Placement.calculateNodes(points, this, rotation,
                (point, other) -> Mathf.dst(point.x, point.y, other.x, other.y) <= range);
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
