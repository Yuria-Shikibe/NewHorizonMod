package newhorizon.expand.block.stream;

import newhorizon.content.NHStats;

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
