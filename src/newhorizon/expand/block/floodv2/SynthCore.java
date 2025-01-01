package newhorizon.expand.block.floodv2;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.blocks.FloodContentBlock;
import newhorizon.util.graphic.DrawFunc;

import java.util.Arrays;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class SynthCore extends Wall {
    public int expansionRadius = 12;
    public Rand rand = new Rand();
    public boolean ignoreRotate = true;
    public int tier = 1;

    public SynthCore(String name) {
        super(name);
        update = true;
        rotate = true;

        quickRotate = false;
    }

    @Override
    public void init() {
        super.init();
    }

    public class SynthCoreBuild extends WallBuild{
        public int[] expandDst = new int[]{-1, -1, -1, -1};

        public Seq<Tile> tmpTiles = new Seq<>();
        public Seq<SynthCoreBuild> tmpBuildings = new Seq<>();
        public Seq<SynthCoreBuild> connectedBuildings = new Seq<>();

        public float interval = 5f;
        public float timer;

        @Override
        public void created() {
            super.created();
            rand.setSeed(pos());

            expandDst[0] = (int) (rand.random(1f, 2f) * expansionRadius);
            expandDst[1] = (int) (rand.random(1f, 2f) * expansionRadius);
            expandDst[2] = (int) (rand.random(1f, 2f) * expansionRadius);
            expandDst[3] = (int) (rand.random(1f, 2f) * expansionRadius);

            if (!ignoreRotate){
                expandDst[(rotation + 2) % 4] = -1;
            }
        }

        @Override
        public void draw() {
            super.draw();
            DrawFunc.drawText(Arrays.toString(expandDst), x, y);
            for (int i = 0; i < 4; i++){
                if (expandDst[i] == -1) continue;
                Draw.color(Pal.remove);
                Draw.z(Layer.blockOver);

                Fill.square(
                    x + Geometry.d4x(i) * expandDst[i] * tilesize,
                    y + Geometry.d4y(i) * expandDst[i] * tilesize,
                    2f
                );
            }
        }

        //expand logic:
        @Override
        public void updateTile() {
            super.updateTile();
            if (timer <= interval){
                timer += Time.delta;
            }else {
                expandTile();
                timer %= interval;
            }
        }

        public void expandTile(){
            int rot = Mathf.mod(ignoreRotate? Mathf.random(3): rotation + Mathf.randomSign(), 4);
            Tile t = world.tile(tileX() + Geometry.d4x(rot) * (size/2 + 1), tileY() + Geometry.d4y(rot) * (size/2 + 1));
            if (t != null){
                if (t.build == null){
                    t.setBlock(FloodContentBlock.testWall, team, rot);
                }else {
                    if (t.build instanceof SynthConduit.SynthVesselBuild){
                        SynthConduit.SynthVesselBuild build = (SynthConduit.SynthVesselBuild) t.build;
                        build.handleExpandCommand(this);
                    }
                }
            }

        }

        @Override
        public void remove() {
            super.remove();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
        }
    }
}
