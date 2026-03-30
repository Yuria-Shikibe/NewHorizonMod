package newhorizon.expand.block.stream;

import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHLiquids;

public class StreamReceiver extends StreamBlock {
    public StreamReceiver(String name) {
        super(name);

        update = true;
        solid = true;
        rotate = true;
        hasLiquids = true;
        liquidCapacity = 6f;
        outputsLiquid = false;
        noUpdateDisabled = true;
        group = BlockGroup.liquids;
        envEnabled = Env.any;
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
    }


    public class StreamReceiverBuild extends StreamBuild {
        public StreamBeam stream;

        @Override
        public void created() {
            super.created();
            stream = new StreamBeam(this);
            stream.beamLength = 13;
        }

        @Override
        public void updateTile() {
            stream.update();
            dumpLiquid(liquids.current());
        }

        @Override
        public void draw() {
            super.draw();
            stream.draw();
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() + 2) % 4 != rotation;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return liquid instanceof NHLiquids.Stream && source instanceof StreamBuild;
        }
    }
}
