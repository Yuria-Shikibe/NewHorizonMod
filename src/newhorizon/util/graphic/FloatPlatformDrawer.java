package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import newhorizon.NewHorizon;

import static mindustry.Vars.world;

public class FloatPlatformDrawer {

    protected static final Point2[] orthogonalPos = {
        new Point2(0, 1),
        new Point2(1, 0),
        new Point2(0, -1),
        new Point2(-1, 0),
    };

    protected static final Point2[][] diagonalPos = {
        new Point2[]{ new Point2(1, 0), new Point2(0, 1), new Point2(1, 1)},
        new Point2[]{ new Point2(1, 0), new Point2(0, -1), new Point2(1, -1)},
        new Point2[]{ new Point2(-1, 0), new Point2(0, -1), new Point2(-1, -1)},
        new Point2[]{ new Point2(-1, 0), new Point2(0, 1), new Point2(-1, 1)},
    };

    public static TextureRegion center;
    public static TextureRegion[] edgeA;
    public static TextureRegion[] edgeB;
    public static TextureRegion[] corner;

    public static void load(){
        edgeA = new TextureRegion[4];
        edgeB = new TextureRegion[4];
        corner = new TextureRegion[4];

        center = Core.atlas.find(NewHorizon.name("platform-center"));
        for(int i = 0; i < 4; i++){
            edgeA[i] = Core.atlas.find(NewHorizon.name("platform-A-" + i));
            edgeB[i] = Core.atlas.find(NewHorizon.name("platform-B-" + i));
            corner[i] = Core.atlas.find(NewHorizon.name("platform-corner-" + i));
        }
    }

    public static void drawPlatform(Building b, boolean AB, boolean[] edge, boolean[] drawCorner){
        Draw.z(Layer.blockUnder);
        Draw.rect(center, b.x, b.y);
        if (AB){
            for(int i = 0; i < 4; i++){
                if(edge[i]){
                    Draw.rect(edgeA[i], b.x, b.y);
                }
            }
        }else {
            for(int i = 0; i < 4; i++){
                if(edge[i]){
                    Draw.rect(edgeB[i], b.x, b.y);
                }
            }
        }

        for(int i = 0; i < 4; i++){
            if (drawCorner[i]){
                Draw.rect(corner[i], b.x, b.y);
            }
        }
    }

    public static boolean getEdgeAB(Building b){
        return (b.tile.x + b.tile.y) % 2 == 0;
    }

    public static boolean[] getEdge(Building b){
        boolean[] edge = new boolean[4];
        for(int i = 0; i < 4; i++){
            Point2 p = orthogonalPos[i];
            edge[i] = world.tile(b.tile.x + p.x, b.tile.y + p.y).build == null;
        }
        return edge;
    }

    public static boolean[] getCorner(Building b){
        boolean[] corner = new boolean[4];
        for(int i = 0; i < 4; i++){
            Point2[] pa = diagonalPos[i];
            Building b0 = world.tile(b.tile.x + pa[2].x, b.tile.y + pa[2].y).build;
            Building b1 = world.tile(b.tile.x + pa[0].x, b.tile.y + pa[0].y).build;
            Building b2 = world.tile(b.tile.x + pa[1].x, b.tile.y + pa[1].y).build;

            corner[i] = (b1 == null && b2 == null);
        }
        return corner;
    }
}
