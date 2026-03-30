package newhorizon.expand.block.stream;

import mindustry.gen.Building;
import mindustry.world.Block;

public class StreamBlock extends Block {
    public int[] streamLength = {3, -1, -1, -1};
    public float[] streamCap = {0.5f, -1, -1, -1};

    public StreamBlock(String name) {
        super(name);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class StreamBuild extends Building {
        public StreamBeam[] streams;

        public boolean acceptStream(StreamBeam stream) {
            return true;
        }

        public void handleStream(StreamBeam stream) {}

        @Override
        public void created() {
            super.created();

            streams = new StreamBeam[streamLength.length];
            for (int i = 0; i < streamLength.length; i++) {
                if (streams[i].beamLength > 0) {
                    streams[i] = new StreamBeam(this);
                    streams[i].beamLength = streamLength[i];
                    streams[i].amountCap = streamCap[i];
                }
            }
        }

        @Override
        public void updateTile() {
            for (StreamBeam stream : streams) {
                if (stream != null && streams.length > 0) stream.update();
            }
        }
    }
}
