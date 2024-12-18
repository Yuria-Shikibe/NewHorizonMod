package newhorizon.expand.block.synth;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Geometry;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.blocks.FloodContentBlock;

import static mindustry.Vars.world;

public class SynthConduit extends Wall {
    public float transportInterval = 20f;
    public int level = 1;

    public SynthConduit(String name) {
        super(name);
        rotate = true;
        size = 1;

        quickRotate = false;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("dst", entity -> new Bar(
            () -> "dst: " + ((SynthVesselBuild)entity).distance,
            () -> Pal.accent,
            () -> 1f
        ));
    }

    public class SynthVesselBuild extends WallBuild{
        //distance from core, when distance = 0, don't expand
        public int distance;

        @Override
        public void draw() {
            super.draw();

        }

        public void handleExpandCommand(int lastDst){
            distance = lastDst - 1;
            if (distance > 0){
                executeExpandCommand();
            }else {
                for (int x = -1; x < 1; x++){
                    for (int y = -1; y < 1; y++){
                        if (world.tile(tileX() + x, tileY() + y) != null && world.build(tileX() + x, tileY() + y) != null){
                            if (!(world.build(tileX() + x, tileY() + y) instanceof SynthVesselBuild)){
                                return;
                            }
                        }
                    }
                }
                tile.setBlock(FloodContentBlock.testRot, team, rotation);
            }
        }

        public void executeExpandCommand(){
            Tile tile = world.tile(tileX() + Geometry.d4x(rotation), tileY() + Geometry.d4y(rotation));
            if (tile != null){
                Building b = tile.build;
                if (b == null){
                    tile.setBlock(block, team, rotation);
                }else if ((b instanceof SynthVesselBuild)){
                    ((SynthVesselBuild)b).handleExpandCommand(distance);
                }
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(distance);
        }

        @Override
        public void read(Reads read) {
            super.read(read);
            distance = read.i();
        }
    }
}
