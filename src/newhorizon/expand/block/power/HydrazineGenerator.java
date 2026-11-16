package newhorizon.expand.block.power;

import arc.Core;
import arc.math.geom.Geometry;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHParticleStreams;
import newhorizon.content.NHStatValues;
import newhorizon.content.NHStats;
import newhorizon.expand.block.stream.StreamBeam;
import newhorizon.expand.block.stream.StreamBlock;
import newhorizon.expand.block.stream.StreamExtractor;
import newhorizon.expand.type.ParticleStream;
import newhorizon.expand.type.ParticleStreamStack;

import static mindustry.Vars.world;

public class HydrazineGenerator extends ConsumeGenerator {
    public ParticleStreamStack outputStream;
    public static final float streamStorage = StreamBlock.streamCapacity;

    public HydrazineGenerator(String name) {
        super(name);
        buildType = () -> new HydrazineGeneratorBuild();
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("particle-stream", (HydrazineGeneratorBuild build) -> new Bar(
                () -> Core.bundle.format("bar.new-horizon-particle-stream",
                        Strings.fixed(build.streamAmount, 1), Strings.fixed(build.streamStorage, 1)),
                () -> outputStream == null || outputStream.stream == null ? Pal.accent : outputStream.stream.color,
                () -> build.streamAmount / build.streamStorage
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        if (outputStream != null) {
            stats.add(mindustry.world.meta.Stat.output, NHStatValues.particleStream(outputStream.stream, outputStream.amount));
        }
    }

    public class HydrazineGeneratorBuild extends ConsumeGeneratorBuild {
        public StreamBeam[] streamInputs;
        public ParticleStream currentStream;
        public float streamAmount;
        public float streamStorage = HydrazineGenerator.streamStorage;

        @Override
        public void created() {
            super.created();
            streamInputs = new StreamBeam[4];
            for (int direction = 0; direction < streamInputs.length; direction++) {
                streamInputs[direction] = new StreamBeam(this);
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();

            float outputAmount = outputStream == null ? 0f : outputStream.amount;
            if (outputStream != null && outputStream.stream != null
                    && (currentStream == null || currentStream == outputStream.stream)) {
                streamAmount = Math.min(streamStorage, streamAmount + outputAmount * productionEfficiency * delta());
                if (streamAmount > 0.0001f) currentStream = outputStream.stream;
            }

            float remaining = outputAmount * delta();
            int radius = size / 2;
            for (int direction = 0; direction < 4 && remaining > 0.0001f; direction++) {
                int sideDirection = (direction + 1) % 4;
                StreamBeam input = streamInputs[direction];
                input.rotationOffset = (direction - rotation + 4) % 4;
                input.currentStream = currentStream;

                for (int offset = -radius; offset <= radius && remaining > 0.0001f; offset++) {
                    Building neighbor = world.build(
                            tileX() + Geometry.d4x(direction) * (radius + 1) + Geometry.d4x(sideDirection) * offset,
                            tileY() + Geometry.d4y(direction) * (radius + 1) + Geometry.d4y(sideDirection) * offset
                    );
                    if (!(neighbor instanceof StreamExtractor.StreamExtractorBuild extractor)
                            || !extractor.acceptStream(input) || !extractor.canAcceptStream(input)) continue;

                    float amount = Math.min(remaining, extractor.streamCapacity - extractor.streamAmount);
                    float previousAmount = extractor.streamAmount;
                    extractor.handleStream(input, amount);
                    float transferred = extractor.streamAmount - previousAmount;
                    streamAmount -= transferred;
                    remaining -= transferred;
                }
            }
            clearStreamIfEmpty();
        }

        @Override
        public byte version() {
            return 2;
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
            if (revision >= 2) {
                int id = read.s();
                streamAmount = read.f();
                currentStream = null;
                for (ParticleStream stream : NHParticleStreams.all) {
                    if (stream.id == id) {
                        currentStream = stream;
                        break;
                    }
                }
                clearStreamIfEmpty();
            }
        }

        public void clearStreamIfEmpty() {
            if (streamAmount <= 0.0001f) {
                streamAmount = 0f;
                currentStream = null;
            }
        }

    }
}
