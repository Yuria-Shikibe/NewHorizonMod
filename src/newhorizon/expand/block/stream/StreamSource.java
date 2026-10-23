package newhorizon.expand.block.stream;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.world.blocks.ItemSelection;
import newhorizon.content.NHParticleStreams;
import newhorizon.expand.type.ParticleStream;
import newhorizon.content.NHStats;

import static mindustry.Vars.tilesize;

public class StreamSource extends StreamBlock {
    public TextureRegion rotRegion;

    public StreamSource(String name) {
        super(name);
        configurable = true;
        saveConfig = true;
        noUpdateDisabled = true;
        displayFlow = false;
        clearOnDoubleTap = true;

        config(ParticleStream.class, (StreamSourceBuild tile, ParticleStream stream) -> tile.setSource(stream));
        configClear((StreamSourceBuild tile) -> tile.setSource(null));
    }

    @Override
    public void load() {
        super.load();
        rotRegion = Core.atlas.find(name + "-rot");
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.streamLength, streamLength[0]);
        stats.add(NHStats.streamCap, streamCap[0] * 60f, mindustry.world.meta.StatUnit.perSecond);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public class StreamSourceBuild extends StreamBuild {
        public ParticleStream source;

        @Override
        public byte version() {
            return 1;
        }

        public void setSource(ParticleStream stream) {
            source = stream;
            currentStream = null;
            streamAmount = 0f;
        }

        @Override
        public void updateTile() {
            if (source == null) {
                currentStream = null;
                streamAmount = 0f;
            } else {
                currentStream = source;
                streamAmount = streamCapacity;
            }

            for (StreamBeam stream : streams) {
                if (stream != null) stream.amountCap = streamCap[stream.rotationOffset];
            }
            super.updateTile();
        }

        @Override
        public void draw() {
            if (rotation == 0) Draw.rect(region, x, y, rotdeg());
            else if (rotation == 1) Draw.rect(region, x, y, tilesize, -tilesize, rotdeg());
            else if (rotation == 2) Draw.rect(rotRegion, x, y, rotdeg());
            else Draw.rect(rotRegion, x, y, tilesize, -tilesize, rotdeg());

            for (StreamBeam stream : streams) {
                if (stream != null) stream.draw();
            }
        }

        public void drawItemSelection(UnlockableContent selection) {
            if (selection != null) {
                Draw.reset();
                Draw.rect(selection.fullIcon, x - 4f, y + 4f);
            }
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return false;
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(StreamSource.this, table, NHParticleStreams.all,
                    () -> source, this::configure, selectionRows, selectionColumns);
        }

        @Override
        public Object config() {
            return source;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(source == null ? -1 : source.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int id = read.s();
            source = null;
            if (revision < 1) return;
            for (ParticleStream stream : NHParticleStreams.all) {
                if (stream.id == id) {
                    source = stream;
                    break;
                }
            }
        }
    }
}
