package newhorizon.util.func;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Intc2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHFx;
import newhorizon.expand.entities.Spawner;

import static mindustry.Vars.*;
import static mindustry.core.World.toTile;

public class NHFunc{
    private static long seedR = 0;
    public static long getSeed(){
        return seedR++;
    }
    
    private static final float MAX_TELEPORT_DST_NET = tilesize / 2f;
    private static Tile tileParma;
    private static Floor floorParma;
    private static final Seq<Tile> tiles = new Seq<>();
    private static final IntSeq buildingIDSeq = new IntSeq();
    private static final int maxCompute = 32;
    
    private static final Vec2
            vec21 = new Vec2(),
            vec22 = new Vec2(),
            vec23 = new Vec2();
    
    private static Tile furthest;
    private static Building tmpBuilding;
    private static Unit tmpUnit;
    private static final Rect rect = new Rect();
    private static final Rect hitrect = new Rect();
    private static final Vec2 tr = new Vec2(), seg1 = new Vec2(), seg2 = new Vec2();
    private static final Seq<Unit> units = new Seq<>();
    private static final IntSet collidedBlocks = new IntSet();
    private static final IntFloatMap damages = new IntFloatMap();

    //just for effect. never modify this.
    public static final Rand globalEffectRand = new Rand(0);
    public static final Rand rand = new Rand(0);
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
    private static final Vec2 point1 = new Vec2(), point2 = new Vec2(), point3 = new Vec2(), point4 = new Vec2();
    private static final Rect r1 = new Rect(), r2 = new Rect();
    
    public static void extinguish(Team team, float x, float y, float range, float intensity){
        indexer.eachBlock(team, x, y, range, b -> true, b -> Fires.extinguish(b.tile, intensity));
    }
    
    public static void extinguish(Teamc teamc, float range, float intensity){
        indexer.eachBlock(teamc.team(), teamc.x(), teamc.y(), range, b -> true, b -> Fires.extinguish(b.tile, intensity));
    }
    

    public static Position collideBuild(Team team, float x1, float y1, float x2, float y2, Boolf<Building> boolf){
        tmpBuilding = null;
    
        boolean found = World.raycast(World.toTile(x1), World.toTile(y1), World.toTile(x2), World.toTile(y2),
                (x, y) -> (tmpBuilding = world.build(x, y)) != null && tmpBuilding.team != team && boolf.get(tmpBuilding));
    
        return found ? tmpBuilding : vec21.set(x2, y2);
    }
    
    public static Position collideBuildOnLength(Team team, float x1, float y1, float length, float ang, Boolf<Building> boolf){
        vec22.trns(ang, length).add(x1, y1);
        return collideBuild(team, x1, y1, vec22.x, vec22.y, boolf);
    }
    
    
    public static float findLaserLength(Bullet b, float angle, float length){
        Tmp.v1.trnsExact(angle, length);
    
        tileParma = null;
        
        boolean found = World.raycast(b.tileX(), b.tileY(), World.toTile(b.x + Tmp.v1.x), World.toTile(b.y + Tmp.v1.y),
                (x, y) -> (tileParma = world.tile(x, y)) != null && tileParma.team() != b.team && tileParma.block().absorbLasers);
        
        return found && tileParma != null ? Math.max(6f, b.dst(tileParma.worldx(), tileParma.worldy())) : length;
    }
    
    public static void collideLine(Bullet hitter, Team team, Effect effect, float x, float y, float angle, float length, boolean large, boolean laser){
        if(laser) length = findLaserLength(hitter, angle, length);
    
        collidedBlocks.clear();
        tr.trnsExact(angle, length);
    
        Intc2 collider = (cx, cy) -> {
            Building tile = world.build(cx, cy);
            boolean collide = tile != null && collidedBlocks.add(tile.pos());
        
            if(hitter.damage > 0){
                float health = !collide ? 0 : tile.health;
            
                if(collide && tile.team != team && tile.collide(hitter)){
                    tile.collision(hitter);
                    hitter.type.hit(hitter, tile.x, tile.y);
                }
            
                //try to heal the tile
                if(collide && hitter.type.testCollision(hitter, tile)){
                    hitter.type.hitTile(hitter, tile, cx * tilesize, cy * tilesize, health, false);
                }
            }
        };
    
        if(hitter.type.collidesGround){
            seg1.set(x, y);
            seg2.set(seg1).add(tr);
            World.raycastEachWorld(x, y, seg2.x, seg2.y, (cx, cy) -> {
                collider.get(cx, cy);
            
                for(Point2 p : Geometry.d4){
                    Tile other = world.tile(p.x + cx, p.y + cy);
                    if(other != null && (large || Intersector.intersectSegmentRectangle(seg1, seg2, other.getBounds(Tmp.r1)))){
                        collider.get(cx + p.x, cy + p.y);
                    }
                }
                return false;
            });
        }
    
        rect.setPosition(x, y).setSize(tr.x, tr.y);
        float x2 = tr.x + x, y2 = tr.y + y;
    
        if(rect.width < 0){
            rect.x += rect.width;
            rect.width *= -1;
        }
    
        if(rect.height < 0){
            rect.y += rect.height;
            rect.height *= -1;
        }
    
        float expand = 3f;
    
        rect.y -= expand;
        rect.x -= expand;
        rect.width += expand * 2;
        rect.height += expand * 2;
    
        Cons<Unit> cons = e -> {
            e.hitbox(hitrect);
        
            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitrect.grow(expand * 2));
        
            if(vec != null && hitter.damage > 0){
                effect.at(vec.x, vec.y);
                e.collision(hitter, vec.x, vec.y);
                hitter.collision(e, vec.x, vec.y);
            }
        };
    
        units.clear();
    
        Units.nearbyEnemies(team, rect, u -> {
            if(u.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround)){
                units.add(u);
            }
        });
    
        units.sort(u -> u.dst2(hitter));
        units.each(cons);
    }
    
    public static void randFadeLightningEffect(float x, float y, float range, float lightningPieceLength, Color color, boolean in){
        randFadeLightningEffectScl(x, y, range, 0.55f, 1.1f, lightningPieceLength, color, in);
    }
    
    public static void randFadeLightningEffectScl(float x, float y, float range, float sclMin, float sclMax, float lightningPieceLength, Color color, boolean in){
        vec21.rnd(range).scl(Mathf.random(sclMin, sclMax)).add(x, y);
        (in ? NHFx.chainLightningFadeReversed : NHFx.chainLightningFade).at(x, y, lightningPieceLength, color, vec21.cpy());
    }
    
    public static Unit teleportUnitNet(Unit before, float x, float y, float angle, Player player){
        if(net.active() || headless){
            if(player != null){
                player.set(x, y);
                player.snapInterpolation();
                player.snapSync();
                player.lastUpdated = player.updateSpacing = 0;
            }
            before.set(x, y);
            before.snapInterpolation();
            before.snapSync();
            before.updateSpacing = 0;
            before.lastUpdated = 0;
        }else{
            before.set(x, y);
        }
        before.rotation = angle;
        return before;
    }
    
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
    
    public static void limitRangeWithoutNew(ItemTurret turret, float margin){
        for(ObjectMap.Entry<Item, BulletType> entry : turret.ammoTypes.entries()){
            entry.value.lifetime = (turret.range + margin) / entry.value.speed;
        }
    }
    
    //not support server
    public static void spawnSingleUnit(UnitType type, Team team, int spawnNum, float x, float y){
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
    
    /**[0]For flying, [1] for navy, [2] for ground */
    public static Seq<Boolf<Tile>> formats(){
        Seq<Boolf<Tile>> seq = new Seq<>(3);
        
        seq.add(
            tile -> world.getQuadBounds(Tmp.r1).contains(tile.getBounds(Tmp.r2)),
            tile -> tile.floor().isLiquid && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes,
            tile -> !tile.floor().isDeep() && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes
        );
        
        return seq;
    }
    
    public static Boolf<Tile> ableToSpawn(UnitType type){
        Boolf<Tile> boolf;
        
        Seq<Boolf<Tile>> boolves = formats();
        
        if(type.flying){
            boolf = boolves.get(0);
        }else if(WaterMovec.class.isAssignableFrom(type.constructor.get().getClass())){
            boolf = boolves.get(1);
        }else{
            boolf = boolves.get(2);
        }
        
        return boolf;
    }
    
    public static Seq<Tile> ableToSpawn(UnitType type, float x, float y, float range){
        Seq<Tile> tSeq = new Seq<>(Tile.class);
    
        Boolf<Tile> boolf = ableToSpawn(type);
        
        return tSeq.addAll(getAcceptableTiles(toTile(x), toTile(y), toTile(range), boolf));
    }
    
    public static boolean ableToSpawnPoints(Seq<Vec2> spawnPoints, UnitType type, float x, float y, float range, int num, long seed){
        Seq<Tile> tSeq = ableToSpawn(type, x, y, range);
    
        rand.setSeed(seed);
        for(int i = 0; i < num; i++){
            Tile[] positions = tSeq.shrink();
            if(positions.length < num)return false;
            spawnPoints.add(new Vec2().set(positions[rand.nextInt(positions.length)]));
        }
        
        return true;
    }
    
    public static boolean spawnUnit(Team team, float x, float y, float angle, float spawnRange, float spawnReloadTime, float spawnDelay, UnitType type, int spawnNum, Cons<Spawner> modifier){
        if(type == null)return false;
        clearTmp();
        Seq<Vec2> vectorSeq = new Seq<>();
    
        if(!ableToSpawnPoints(vectorSeq, type, x, y, spawnRange, spawnNum, rand.nextLong()))return false;
    
        int i = 0;
        for (Vec2 s : vectorSeq) {
            Spawner spawner = Pools.obtain(Spawner.class, Spawner::new);
            spawner.init(type, team, s, angle, spawnReloadTime + i * spawnDelay);
            modifier.get(spawner);
            if(!net.client())spawner.add();
            i++;
        }
        return true;
    }
    
    public static boolean spawnUnit(Team team, float x, float y, float angle, float spawnRange, float spawnReloadTime, float spawnDelay, UnitType type, int spawnNum){
        return spawnUnit(team, x, y, angle, spawnRange, spawnReloadTime, spawnDelay, type, spawnNum, t -> {});
    }
    
    public static boolean spawnUnit(Team team, float x, float y, float angle, float spawnRange, float spawnReloadTime, float spawnDelay, UnitType type, int spawnNum, StatusEffect statusEffect, float statusDuration){
        return spawnUnit(team, x, y, angle, spawnRange, spawnReloadTime, spawnDelay, type, spawnNum, s -> {
            s.setStatus(statusEffect, statusDuration);
        });
    }
    
    public static boolean spawnUnit(Team team, float x, float y, float angle, float spawnRange, float spawnReloadTime, float spawnDelay, UnitType type, int spawnNum, StatusEffect statusEffect, float statusDuration, double frag){
        return spawnUnit(team, x, y, angle, spawnRange, spawnReloadTime, spawnDelay, type, spawnNum, s -> {
            s.setStatus(statusEffect, statusDuration);
            s.flagToApply = frag;
        });
    }
    
    public static void spawnSingleUnit(Team team, float x, float y, float angle, float delay, UnitType type){
        Spawner spawner = Pools.obtain(Spawner.class, Spawner::new);
        spawner.init(type, team, vec21.set(x, y), angle, delay);
        if(!net.client())spawner.add();
    }
    
    public static void spawnSingleUnit(Team team, float x, float y, float angle, float delay, UnitType type, Cons<Spawner> modifier){
        Spawner spawner = Pools.obtain(Spawner.class, Spawner::new);
        spawner.init(type, team, vec21.set(x, y), angle, delay);
        modifier.get(spawner);
        if(!net.client())spawner.add();
    }

    public static <T> void shuffle(Seq<T> seq, Rand rand){
        T[] items = seq.items;
        for(int i = seq.size - 1; i >= 0; i--){
            int ii = Mathf.random(i);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }
    
    public static Rand rand(long id){
        rand.setSeed(id);
        return rand;
    }
}
