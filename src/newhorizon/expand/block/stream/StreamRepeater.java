package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.inner.LinkBlock;

import static mindustry.Vars.tilesize;

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

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashLine(Pal.placing,
                x * tilesize + Geometry.d4[rotation].x * (tilesize / 2f + 2),
                y * tilesize + Geometry.d4[rotation].y * (tilesize / 2f + 2),
                x * tilesize + Geometry.d4[rotation].x * 14 * tilesize,
                y * tilesize + Geometry.d4[rotation].y * 14 * tilesize);
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
