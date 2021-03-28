package newhorizon.func;

import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.block.special.JumpGate;

import java.util.Random;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static mindustry.core.World.toTile;

public class Functions {
    private static Tile tileParma;
    private static Floor floorParma;
    private static final Seq<Tile> tiles = new Seq<>();
    private static final IntSeq buildingIDSeq = new IntSeq();
    private static final int maxCompute = 32;
    public static final Effect debugEffect = new Effect(120f, 300f, e -> {
        if(!(e.data instanceof Seq))return;
        Seq<Rect> data = e.data();
        Draw.color(Pal.lancerLaser);
        Draw.z(Layer.flyingUnit + 2f);
        for(Rect r : data){
            r.getCenter(Tmp.v1);
            Fill.square(Tmp.v1.x, Tmp.v1.y, tilesize / 2f);
        }
    });
    private static final Vec2 point = new Vec2();
    
    /**
     * @implNote Get all the {@link Tile} {@code tile} within a certain range at certain position.
     * @param x the abscissa of search center.
     * @param y the ordinate of search center.
     * @param range the search range.
     * @param bool {@link Boolf} {@code lambda} to determine whether the condition is true.
     * @return {@link Seq}{@code <Tile>} - which contains eligible {@link Tile} {@code tile}.
     */
    public static Seq<Tile> getAcceptableTiles(int x, int y, int range, Boolf<Tile> bool){
        Seq<Tile> tiles = new Seq<>(true, (int)(Mathf.pow(range, 2) * Mathf.pi), Tile.class);
        Geometry.circle(x, y, range, (x1, y1) -> {
            if((tileParma = world.tile(x1, y1)) != null && bool.get(tileParma)){
                tiles.add(world.tile(x1, y1));
            }
        });
        return tiles;
    }
    
    private static void clearTmp(){
        tileParma = null;
        floorParma = null;
        buildingIDSeq.clear();
        tiles.clear();
    }
    
    public static Color getColor(Color defaultColor, Team team){
        return defaultColor == null ? team.color : defaultColor;
    }
    
    //not support server
    public static void spawnUnit(UnitType type, Team team, int spawnNum, float x, float y){
        for(int spawned = 0; spawned < spawnNum; spawned++){
            Time.run(spawned * Time.delta, () -> {
                Unit unit = type.create(team);
                if(unit != null){
                    unit.set(x, y);
                    unit.add();
                }else Log.info("Unit == null");
            });
        }
    }
    
    public static float regSize(UnitType type){
        return type.hitSize / tilesize / tilesize / 3.25f;
    }
    
    public static boolean spawnUnit(Teamc starter, float x, float y, float spawnRange, float spawnReloadTime, float spawnDelay, float inComeVelocity, long seed, JumpGate.UnitSet set, Color spawnColor){
        UnitType type = set.type;
        clearTmp();
        final Seq<Vec2> vectorSeq = new Seq<>();
        final Seq<Tile> tSeq = new Seq<>(Tile.class);
        float angle, regSize = regSize(type);
        if(!type.flying){
            Random r = new Random(seed);
            tSeq.addAll(getAcceptableTiles(toTile(x), toTile(y), toTile(spawnRange),
                tile -> !tile.floor().isDeep() && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes)
            );
            for(int i = 0; i < set.callIns; i++){
                Tile[] positions = tSeq.shrink();
                if(positions.length < set.callIns)return false;
                vectorSeq.add(new Vec2().set(positions[r.nextInt(positions.length)]));
            }
        }else{
            randLenVectors(seed, set.callIns, spawnRange, (sx, sy) -> vectorSeq.add(new Vec2(sx, sy).add(x, y)));
        }
        
        angle = starter.angleTo(x, y);
        
        NHSetting.debug(() -> {
            Seq<Rect> debugSeq = new Seq<>();
            for(Tile tile : tSeq){
                debugSeq.add(tile.getBounds(new Rect()));
            }
            debugEffect.at(x, y, 0, debugSeq);
        });
        
        int i = 0;
        for (Vec2 s : vectorSeq) {
            if(!Units.canCreate(starter.team(), type))break;
            FContents.spawnUnitDrawer.create(starter, starter.team(), s.x, s.y, angle, 1f, 1f, 1f, new FContents.SpawnerData(set, spawnRange, spawnDelay, spawnColor)).lifetime(spawnReloadTime + i * spawnDelay);
            i++;
        }
        return true;
    }
}
