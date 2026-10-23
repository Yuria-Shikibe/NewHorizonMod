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
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.world.blocks.heat.HeatBlock;
import newhorizon.content.NHParticleStreams;

import static mindustry.Vars.player;

public class StreamReceiver extends StreamBlock {
    private static final int streamMode = 4;

    public TextureRegion heatRegion;

    public StreamReceiver(String name) {
        super(name);

        streamLength = new int[]{-1, -1, -1, -1};
        streamCap = new float[]{-1, -1, -1, -1};
        saveConfig = true;
        config(Integer.class, (StreamReceiverBuild build, Integer mode) -> build.setHeatOutput(mode));
    }

    @Override
    public void load() {
        super.load();
        heatRegion = Core.atlas.find(name + "-heat");
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (StreamReceiverBuild build) -> new Bar(
                () -> Core.bundle.format("bar.heatamount", (int)(build.heat + 0.001f)),
                () -> Pal.lightOrange,
                () -> build.heat / heatPerStreamPerSecond
        ));
    }

    public class StreamReceiverBuild extends StreamBuild implements HeatBlock {
        public boolean heatMode;
        public int streamDirection;
        public float heat;
        public float receivedAmount;

        @Override
        public void created() {
            super.created();
            streamDirection = rotation;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void updateTile() {
            if (heatMode) {
                heat = enabled && Time.delta > 0f
                        ? receivedAmount * Time.toSeconds / Time.delta * heatPerStreamPerSecond
                        : 0f;
                receivedAmount = 0f;
                currentStream = null;
                streamAmount = 0f;
                for (StreamBeam stream : streams) {
                    if (stream != null) stream.amountCap = 0f;
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
            boolean entersFromNonOutputSide = (stream.getRotation() + 2) % 4 != streamDirection;
            if (!heatMode) return entersFromNonOutputSide;
            return enabled && entersFromNonOutputSide && stream.currentStream == NHParticleStreams.particle;
        }

        @Override
        public void handleStream(StreamBeam stream, float amount) {
            if (heatMode) {
                if (enabled && stream.currentStream == NHParticleStreams.particle) receivedAmount += amount;
            } else {
                super.handleStream(stream, amount);
            }
        }

        @Override
        public void draw() {
            if (!heatMode) {
                super.draw();
                return;
            }

            Draw.rect(heatRegion, x, y, rotation * 90f);
            for (StreamBeam stream : streams) {
                if (stream != null) stream.draw();
            }
        }

        @Override
        public void tapped() {
            super.tapped();
            Fx.placeBlock.at(this, size);
            Sounds.click.at(this);
            configure(heatMode ? streamMode : streamDirection);
        }

        @Override
        public Graphics.Cursor getCursor() {
            return interactable(player.team()) ? Graphics.Cursor.SystemCursor.hand : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public float heat() {
            return heatMode ? heat : 0f;
        }

        @Override
        public float heatFrac() {
            return heat() / heatPerStreamPerSecond;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.heat) return heat();
            return super.sense(sensor);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button("@nh.stream.toggle-heat", Icon.refresh, () -> configure(heatMode ? streamMode : streamDirection))
                    .size(220f, 48f);
        }

        @Override
        public Object config() {
            return heatMode ? streamDirection : streamMode;
        }

        private void setHeatOutput(int mode) {
            if (mode == streamMode || mode < 0 || mode >= 4) {
                heatMode = false;
                heat = 0f;
                receivedAmount = 0f;
                currentStream = null;
                streamAmount = 0f;
                rotation = streamDirection;
                onProximityUpdate();
                return;
            }

            heatMode = true;
            heat = 0f;
            receivedAmount = 0f;
            currentStream = null;
            streamAmount = 0f;
            rotation = streamDirection;
            onProximityUpdate();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(heatMode);
            write.i(streamDirection);
            write.i(-1);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                heatMode = read.bool();
                streamDirection = read.i();
                read.i();
                rotation = streamDirection;
            } else {
                streamDirection = rotation;
            }
        }
    }
}
