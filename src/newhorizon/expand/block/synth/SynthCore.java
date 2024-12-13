package newhorizon.expand.block.synth;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.blocks.FloodContentBlock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.Vars.*;

public class SynthCore extends Wall {
    public float expansionRadius = 32;
    public int maxHeight = 10;
    public int[] expandDst = new int[maxHeight];

    public Interp altitudeInterp = Interp.pow2In;
    public SynthCore(String name) {
        super(name);
        update = true;
    }

    @Override
    public void init() {
        super.init();
        for (int i = 0; i < maxHeight; i++){
            expandDst[i] = (int) Math.ceil(altitudeInterp.apply((float) (maxHeight - i) / maxHeight) * expansionRadius);
        }
    }

    public class SynthCoreBuild extends WallBuild{
        public Seq<Tile> tmpTiles;
        public Seq<Building> tmpBuilds;

        @Override
        public void created() {
            super.created();
            tmpTiles = new Seq<>(true);
        }

        @Override
        public void draw() {
            super.draw();
            tmpTiles.each(t -> {
                Draw.color(Pal.accent);
                Draw.alpha(0.5f);
                Draw.z(Layer.blockOver);
                Fill.square(t.drawx(), t.drawy(), 2f);
            });
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (timer(0, 1)){
                float angle = Mathf.random(360f);
                Tmp.v1.trns(angle, expansionRadius).add(tileX(), tileY());
                tmpTiles.clear();

                //find expand line with cap
                AtomicBoolean cap = new AtomicBoolean(false);
                Bresenham2.line(tileX(), tileY(), Mathf.round(Tmp.v1.x), Mathf.round(Tmp.v1.y), (x, y) -> {
                    if (!cap.get()){
                        Tile t = world.tile(x, y);
                        if (t == null) {
                            cap.set(true);
                            return;
                        }
                        if (t.build == this) return;
                        if (t.floor().isDeep()) cap.set(true);
                        tmpTiles.add(t);
                    }
                });

                for (int i = 0; i < tmpTiles.size; i++){
                    int currentAltitude = getTileAltitude(tmpTiles.get(i));
                    int nextAltitude = (i < tmpTiles.size - 1)? getTileAltitude(tmpTiles.get(i + 1)): 0;
                    if (currentAltitude == 8) continue;
                    if (currentAltitude > nextAltitude) continue;
                    upgradeTile(tmpTiles.get(i));
                    break;
                }

            }
        }

        public int getTileAltitude(Tile tile){
            if (tile.build != null && tile.build.block instanceof SynthWall){
                return ((SynthWall) tile.build.block).altitude;
            }else {
                return 0;
            }
        }

        public void upgradeTile(Tile tile){
            tile.setBlock(FloodContentBlock.synthWalls[getTileAltitude(tile)], team);
        }
    }
}
