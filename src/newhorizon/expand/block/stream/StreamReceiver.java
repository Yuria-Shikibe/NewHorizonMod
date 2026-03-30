package newhorizon.expand.block.stream;

import mindustry.gen.Building;
import mindustry.type.Liquid;
import newhorizon.content.NHLiquids;

public class StreamReceiver extends StreamBlock {
    public StreamReceiver(String name) {
        super(name);

        streamLength = new int[]{-1, -1, -1, -1};
        streamCap = new float[]{-1, -1, -1, -1};

        outputsLiquid = true;
    }

    public class StreamReceiverBuild extends StreamBuild {

        @Override
        public void updateTile() {
            dumpLiquid(liquids.current(), 2f, rotation);
        }
        
        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() + 2) % 4 != rotation;
        }
    }
}
