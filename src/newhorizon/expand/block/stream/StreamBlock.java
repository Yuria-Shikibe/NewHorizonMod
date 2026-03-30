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

import static mindustry.Vars.tilesize;

public class StreamBlock extends Block {
    public int[] streamLength = {3, -1, -1, -1};
    public float[] streamCap = {0.5f, -1, -1, -1};

    public StreamBlock(String name) {
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
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        for (int i = 0; i < streamLength.length; i++) {
            Drawf.dashLine(Pal.placing,
                    x * tilesize + Geometry.d4[rotation + i].x * (tilesize / 2f + 2),
                    y * tilesize + Geometry.d4[rotation + i].y * (tilesize / 2f + 2),
                    x * tilesize + Geometry.d4[rotation + i].x * (streamLength[i] + 1) * tilesize,
                    y * tilesize + Geometry.d4[rotation + i].y * (streamLength[i] + 1) * tilesize);
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class StreamBuild extends Building {
        public StreamBeam[] streams;

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return liquid instanceof NHLiquids.Stream && source instanceof StreamBuild;
        }

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
                    streams[i].rotationOffset = i;
                }
            }
        }

        @Override
        public void updateTile() {
            for (StreamBeam stream : streams) {
                if (stream != null && streams.length > 0) stream.update();
            }
        }

        @Override
        public void draw() {
            for (StreamBeam stream : streams) {
                if (stream != null && streams.length > 0) stream.draw();
            }
        }
    }
}
