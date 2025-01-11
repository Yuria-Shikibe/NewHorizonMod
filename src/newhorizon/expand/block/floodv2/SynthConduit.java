package newhorizon.expand.block.floodv2;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;

import static mindustry.Vars.world;
import static newhorizon.NHVars.rectControl;
//import static newhorizon.NHVars.rectControl;

public class SynthConduit extends Wall {
    public float transportInterval = 20f;
    public int level = 1;
    public TextureRegion[] region;

    public SynthConduit(String name) {
        super(name);
        size = 1;
    }

    @Override
    public void load() {
        super.load();
        region = new TextureRegion[2];
        region[0] = Core.atlas.find(name + "-1");
        region[1] = Core.atlas.find(name + "-2");
    }

    @Override
    public void drawShadow(Tile tile) {
        super.drawShadow(tile);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("dst", entity -> new Bar(
            () -> "dst: " + ((SynthVesselBuild)entity).lastReceiveDir,
            () -> Pal.accent,
            () -> 1f
        ));
    }

    public class SynthVesselBuild extends WallBuild{
        //distance from core, when distance = 0, don't expand
        public int lastReceiveDir;
        public int idx = 0;

        @Override
        public void draw() {
            super.draw();
            Draw.color(team.color);
            Draw.z(Layer.block + 1f);
            /*
            Draw.z(Layer.block + 1);
            if (idx % 4 == 0) {
                if (rotation % 4 == 0){
                    Draw.rect(region[0], x, y, rotdeg());
                }else {
                    Draw.rect(region[1], x, y, rotdeg() + 180);
                }
            }else {
                if (rotation % 4 == 0){
                    Draw.rect(region[1], x, y, rotdeg());
                }else {
                    Draw.rect(region[0], x, y, rotdeg() + 180);
                }
            }

             */
        }

        @Override
        public void created() {
            super.created();
            if ((tileX() + tileY()) % 2 == 0){
                idx = 0;
            }else {
                idx = 1;
            }
        }


        public void handleExpandCommand(Building last){
            lastReceiveDir = relativeTo(last);
            Tile target = null;
            boolean onRect = false;

            for (Point2 p: Geometry.getD4Points()){
                if (rectControl.getPos(tileX() + p.x, tileY() + p.y) == 0) continue;
                if (world.tile(tileX() + p.x, tileY() + p.y) == null) continue;
                if (world.build(tileX() + p.x, tileY() + p.y) != null) continue;

                target = world.tile(tileX() + p.x, tileY() + p.y);
                onRect = true;
                break;
            }

            if (!onRect){
                target = world.tile(tileX() + Geometry.d4x(rotation), tileY() + Geometry.d4y(rotation));
            }

            if (target != null){
                if (target.build == null){
                    target.setBlock(block, team, (lastReceiveDir + 2) % 4);
                }else{
                    for (Point2 p: Geometry.getD4Points()){
                        Building b = world.build(tileX() + p.x, tileY() + p.y);
                        if (b == last) continue;
                        if ((b instanceof SynthVesselBuild)){
                            ((SynthVesselBuild)b).handleExpandCommand(this);
                        }
                    }
                }
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
        }

        @Override
        public void read(Reads read) {
            super.read(read);
        }
    }
}
