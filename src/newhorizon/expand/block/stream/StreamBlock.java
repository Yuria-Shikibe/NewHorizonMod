package newhorizon.expand.block.stream;

import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.expand.type.ParticleStream;

import static mindustry.Vars.tilesize;

public class StreamBlock extends Block {
    public static final float heatPerStreamPerSecond = 6f;
    public static final float streamCapacity = 6f;

    public int[] streamLength = {3, -1, -1, -1};
    public float[] streamCap = {0.5f, -1, -1, -1};

    public StreamBlock(String name) {
        super(name);

        update = true;
        solid = true;
        rotate = true;
        drawArrow = false;
        hasLiquids = false;
        outputsLiquid = false;
        noUpdateDisabled = true;
        group = BlockGroup.liquids;
        envEnabled = Env.any;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        for (int i = 0; i < 4; i++) {
            if (streamLength[i] > 0) {
                Drawf.dashLine(Pal.placing,
                        x * tilesize + Geometry.d4[rotation + i].x * (tilesize / 2f + 2),
                        y * tilesize + Geometry.d4[rotation + i].y * (tilesize / 2f + 2),
                        x * tilesize + Geometry.d4[rotation + i].x * (streamLength[i] + 1) * tilesize,
                        y * tilesize + Geometry.d4[rotation + i].y * (streamLength[i] + 1) * tilesize);
            }
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("liquid");
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class StreamBuild extends Building {
        public StreamBeam[] streams;
        public ParticleStream currentStream;
        public float streamAmount;
        public float streamCapacity = StreamBlock.streamCapacity;

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return false;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
        }

        public boolean acceptStream(StreamBeam stream) {
            return true;
        }

        public boolean canAcceptStream(StreamBeam stream) {
            return stream != null && stream.currentStream != null
                    && (currentStream == null || currentStream == stream.currentStream)
                    && streamAmount < streamCapacity - 0.0001f;
        }

        public void handleStream(StreamBeam stream, float amount) {
            if (!canAcceptStream(stream)) return;

            if (currentStream == null) currentStream = stream.currentStream;
            streamAmount = Math.min(streamCapacity, streamAmount + amount);
        }

        public void clearStreamIfEmpty() {
            if (streamAmount <= 0.0001f) {
                streamAmount = 0f;
                currentStream = null;
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(currentStream == null ? -1 : currentStream.id);
            write.f(streamAmount);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            currentStream = null;
            streamAmount = 0f;
            if (revision < 1) return;

            int id = read.s();
            streamAmount = read.f();
            for (ParticleStream stream : ParticleStream.all) {
                if (stream.id == id) {
                    currentStream = stream;
                    break;
                }
            }
            clearStreamIfEmpty();
        }

        @Override
        public void created() {
            super.created();

            streams = new StreamBeam[streamLength.length];
            for (int i = 0; i < 4; i++) {
                if (streamLength[i] > 0) {
                    streams[i] = new StreamBeam(this);
                    streams[i].beamLength = streamLength[i];
                    streams[i].amountCap = streamCap[i];
                    streams[i].rotationOffset = i;
                }
            }
        }

        @Override
        public void updateTile() {
            clearStreamIfEmpty();
            for (StreamBeam stream : streams) {
                if (stream != null) stream.update();
            }
            clearStreamIfEmpty();
        }

        @Override
        public void draw() {
            super.draw();
            for (StreamBeam stream : streams) {
                if (stream != null) stream.draw();
            }
        }
    }
}
