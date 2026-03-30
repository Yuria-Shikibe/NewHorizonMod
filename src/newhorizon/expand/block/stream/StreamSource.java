package newhorizon.expand.block.stream;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHLiquids;
import newhorizon.content.NHStats;

import static mindustry.Vars.*;

public class StreamSource extends StreamBlock {
    public TextureRegion rotRegion;

    public StreamSource(String name) {
        super(name);
        configurable = true;
        saveConfig = true;
        noUpdateDisabled = true;
        displayFlow = false;
        clearOnDoubleTap = true;

        config(Liquid.class, (StreamSourceBuild tile, Liquid l) -> {
            if (l instanceof NHLiquids.Stream){
                tile.source = l;
            }else {
                tile.liquids.clear();
                tile.source = null;
            }
        });
        configClear((StreamSourceBuild tile) -> tile.source = null);
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
        stats.add(NHStats.streamCap, streamCap[0] * Time.toSeconds, StatUnit.perSecond);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class StreamSourceBuild extends StreamBuild {
        public @Nullable Liquid source;

        @Override
        public void updateTile(){
            super.updateTile();

            if (source == null) liquids.clear();
            else liquids.set(source, liquidCapacity);
        }

        @Override
        public void draw() {
            if (rotation == 0) Draw.rect(region, x, y, rotdeg());
            else if (rotation == 1) Draw.rect(region, x, y, tilesize, -tilesize, rotdeg());
            else if (rotation == 2) Draw.rect(rotRegion, x, y, rotdeg());
            else Draw.rect(rotRegion, x, y, tilesize, -tilesize, rotdeg());

            for (StreamBeam stream : streams) {
                if (stream != null && streams.length > 0) stream.draw();
            }
        }

        public void drawItemSelection(UnlockableContent selection) {
            if (selection != null) {
                float dx = x - 4f;
                float dy = y + 4f;
                Draw.reset();
                Draw.rect(selection.fullIcon, dx, dy);
            }
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return false;
        }

        @Override
        public void buildConfiguration(Table table){
            ItemSelection.buildTable(StreamSource.this, table,
                    content.liquids().select(liquid -> liquid instanceof NHLiquids.Stream),
                    () -> source, this::configure, selectionRows, selectionColumns);
        }

        @Override
        public Liquid config(){
            return source;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(source == null ? -1 : source.id);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            int id = read.s();
            source = id == -1 ? null : content.liquid(id);
        }
    }
}
