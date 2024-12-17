package newhorizon.expand.block.synth;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.NHFx;
import newhorizon.content.blocks.FloodContentBlock;
import newhorizon.util.graphic.DrawFunc;

import java.util.Arrays;

import static mindustry.Vars.*;

public class SynthCore extends Wall {
    public int expansionRadius = 18;
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
        //front, left, right, back. when ignoreRotate, back always = -1;
        public int[] expandDst = new int[]{-1, -1, -1, -1};

        public Seq<Tile> tmpTiles = new Seq<>();
        public Seq<SynthCoreBuild> tmpBuildings = new Seq<>();
        public Seq<SynthCoreBuild> connectedBuildings = new Seq<>();

        public float interval = 30f;
        public float timer;

        @Override
        public void created() {
            super.created();
            rand.setSeed(pos());
            if (ignoreRotate){
                expandDst[0] = (int) (rand.random(1f, 2f) * expansionRadius);
                expandDst[1] = (int) (rand.random(1f, 2f) * expansionRadius);
                expandDst[2] = (int) (rand.random(1f, 2f) * expansionRadius);
                expandDst[3] = (int) (rand.random(1f, 2f) * expansionRadius);
            }else {
                expandDst[0] = (int) (rand.random(1f, 2f) * expansionRadius);
                expandDst[1] = (int) (rand.random(1f, 2f) * expansionRadius);
                expandDst[2] = (int) (rand.random(1f, 2f) * expansionRadius);
                expandDst[3] = -1;
            }
        }

        @Override
        public void draw() {
            super.draw();
            DrawFunc.drawText(Arrays.toString(expandDst), x, y);
            Draw.color(Pal.techBlue);
            Draw.alpha(0.8f);
            Draw.z(Layer.blockOver);
            Fill.square(
                x + Geometry.d4x(rotation) * expandDst[0] * tilesize,
                y + Geometry.d4y(rotation) * expandDst[0] * tilesize,
                2f);
            Fill.square(
                x + Geometry.d4x(rotation + 1) * expandDst[1] * tilesize,
                y + Geometry.d4y(rotation + 1) * expandDst[1] * tilesize,
                2f);
            Fill.square(
                x + Geometry.d4x(rotation - 1) * expandDst[2] * tilesize,
                y + Geometry.d4y(rotation - 1) * expandDst[2] * tilesize,
                2f);
            if (!ignoreRotate) return;
            Fill.square(
                x + Geometry.d4x(rotation + 2) * expandDst[3] * tilesize,
                y + Geometry.d4y(rotation + 2) * expandDst[3] * tilesize,
                2f);

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
            int rot = ignoreRotate? Mathf.random(3): rotation + Mathf.randomSign();
            float angle = rot * 90;

            tmpTiles.clear();
            for (int i = 0; i < expansionRadius; i++){
                tmpTiles.add(world.tile(tileX() + Geometry.d4x(rot) * expandDst[1]));
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
