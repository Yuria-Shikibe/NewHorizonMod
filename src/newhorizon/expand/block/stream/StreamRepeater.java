package newhorizon.expand.block.stream;

import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.inner.LinkBlock;

public class StreamRepeater extends Block {
    public StreamRepeater(String name) {
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


    public class StreamRepeaterBuild extends Building implements StreamBeamBuild {
        public StreamBeam stream;

        @Override
        public void created() {
            super.created();
            stream = new StreamBeam(this);
            stream.beamLength = 13;
        }

        @Override
        public void update() {
            stream.update();
        }

        @Override
        public void draw() {
            super.draw();
            stream.draw();
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return stream.getRotation() == rotation;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (liquid instanceof NHLiquids.Stream) {
                return source instanceof StreamBeamBuild ||
                        (source instanceof LinkBlock.LinkBuild entity && entity.linkBuild instanceof StreamBeamBuild);
            }
            return false;
        }
    }
}
