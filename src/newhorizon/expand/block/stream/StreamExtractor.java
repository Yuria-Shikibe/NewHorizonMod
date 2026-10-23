package newhorizon.expand.block.stream;

import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHParticleStreams;
import newhorizon.content.NHStats;

import static mindustry.Vars.player;

public class StreamExtractor extends StreamRepeater {
    public TextureRegion heatRegion;

    public StreamExtractor(String name) {
        super(name);

        streamLength = new int[]{5, -1, -1, -1};
        streamCap = new float[]{0.5f, -1, -1, -1};
        saveConfig = true;
        config(Boolean.class, (StreamExtractorBuild build, Boolean heatMode) -> build.setHeatMode(heatMode));
    }

    @Override
    public void load() {
        super.load();
        heatRegion = Core.atlas.find(name + "-heat");
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.streamCap, streamCap[0] * Time.toSeconds, StatUnit.perSecond);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (StreamExtractorBuild build) -> new Bar(
                () -> Core.bundle.format("bar.heatamount", (int)(build.heat + 0.001f)),
                () -> Pal.lightOrange,
                () -> build.heat / heatPerStreamPerSecond
        ));
    }

    public class StreamExtractorBuild extends StreamRepeaterBuild implements HeatConsumer {
        public boolean heatMode;
        public float heat;
        public float[] sideHeat = new float[4];

        public void setHeatMode(boolean enabled) {
            heatMode = enabled;
            heat = 0f;
            currentStream = null;
            streamAmount = 0f;
            onProximityUpdate();
        }

        @Override
        public void updateTile() {
            if (heatMode) {
                heat = enabled ? calculateHeat(sideHeat) : 0f;
                currentStream = heat > 0f ? NHParticleStreams.particle : null;
                streamAmount = heat > 0f ? streamCapacity : 0f;
                for (StreamBeam stream : streams) {
                    if (stream != null) stream.amountCap = heat / heatPerStreamPerSecond / Time.toSeconds;
                }
            } else {
                heat = 0f;
                for (StreamBeam stream : streams) {
                    if (stream != null) stream.amountCap = streamCap[stream.rotationOffset];
                }
            }
            super.updateTile();
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return !heatMode && stream.getRotation() == rotation;
        }

        @Override
        public void tapped() {
            super.tapped();
            Fx.placeBlock.at(this, size);
            Sounds.click.at(this);
            configure(!heatMode);
        }

        @Override
        public Graphics.Cursor getCursor() {
            return interactable(player.team()) ? Graphics.Cursor.SystemCursor.hand : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public void draw() {
            if (!heatMode) {
                super.draw();
                return;
            }

            Draw.rect(heatRegion, x, y, rotdeg());
            for (StreamBeam stream : streams) {
                if (stream != null) stream.draw();
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button("@nh.stream.toggle-heat", Icon.refresh, () -> configure(!heatMode))
                    .size(220f, 48f);
        }

        @Override
        public Object config() {
            return heatMode;
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            return heatMode ? heatPerStreamPerSecond : 0f;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(heatMode);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) heatMode = read.bool();
        }
    }
}
