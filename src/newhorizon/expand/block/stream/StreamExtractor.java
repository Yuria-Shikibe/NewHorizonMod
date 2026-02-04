package newhorizon.expand.block.stream;

import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import newhorizon.content.NHLiquids;

import static mindustry.Vars.tilesize;

public class StreamExtractor extends StreamRepeater {

    public StreamExtractor(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawPotentialLinks(x, y);
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
        Drawf.dashLine(Pal.placing,
                x * tilesize + Geometry.d4[rotation].x * (tilesize / 2f + 2),
                y * tilesize + Geometry.d4[rotation].y * (tilesize / 2f + 2),
                x * tilesize + Geometry.d4[rotation].x * 6 * tilesize,
                y * tilesize + Geometry.d4[rotation].y * 6 * tilesize);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class StreamExtractorBuild extends StreamRepeaterBuild{

        @Override
        public void created() {
            super.created();
            efficiency = 1f;

            stream = new StreamBeam(this);
            stream.amountCap = 0.5f;
            stream.beamLength = 5;
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return liquid instanceof NHLiquids.Stream;
        }
    }
}
