package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.content.NHStats;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;
import static newhorizon.util.graphic.SpriteUtil.*;

public class AdaptWall extends Wall {
    private final Seq<Building> toDamage = new Seq<>();
    private final Queue<Building> queue = new Queue<>();
    public TextureRegion[] atlasRegion;
    public float damageReduction = 0.1f;
    public float maxShareStep = 2;

    public static final Point2[] checkPos = {
            new Point2( 0,  1),
            new Point2( 1,  0),
            new Point2( 0, -1),
            new Point2(-1,  0),

            new Point2( 1,  1),
            new Point2( 1, -1),
            new Point2(-1, -1),
            new Point2(-1,  1),

            new Point2( 0,  2),
            new Point2( 2,  0),
            new Point2( 0, -2),
            new Point2(-2,  0),
    };


    public AdaptWall(String name) {
        super(name);
        size = 1;
        insulated = true;
        absorbLasers = true;
        placeableLiquid = true;
        crushDamageMultiplier = 1f;
        clipSize = tilesize * 2 + 2;
        teamPassable = true;
    }

    @Override
    public void load() {
        super.load();
        atlasRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-tiled"), 32, 32);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.damageReduction, damageReduction * 100, StatUnit.percent);
    }

    public class AdaptWallBuild extends Building {
        public Seq<AdaptWallBuild> connectedWalls = new Seq<>();
        public int drawIndex = 0;

        public void updateDrawRegion() {
            drawIndex = 0;
            for(int i = 0; i < 8; i++){
                Tile other = tile.nearby(Geometry.d8[i]);
                if(checkAutotileSame(other)){
                    drawIndex |= (1 << i);
                }
            }
            drawIndex = TileBitmask.values[drawIndex];
        }

        public void findLinkWalls() {
            toDamage.clear();
            queue.clear();

            queue.addLast(this);
            while (queue.size > 0) {
                Building wall = queue.removeFirst();
                toDamage.addUnique(wall);
                for (Building next : wall.proximity) {
                    if (linkValid(next) && !toDamage.contains(next)) {
                        toDamage.add(next);
                        queue.addLast(next);
                    }
                }
            }
        }

        public boolean linkValid(Building build) {
            return checkAutotileSame(build) && Mathf.dstm(tileX(), tileY(), build.tileX(), build.tileY()) <= maxShareStep;
        }

        public boolean checkAutotileSame(Tile other){
            return other != null && checkAutotileSame(other.build);
        }

        public boolean checkAutotileInnerSame(Tile other){
            return other != null && checkAutotileInnerSame(other.build);
        }

        public boolean checkAutotileSame(Building build) {
            return build != null && build.block == this.block;
        }

        public boolean checkAutotileInnerSame(Building build){
            return build instanceof AdaptWallBuild wall && build.block == this.block && wall.drawIndex == 13;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            findLinkWalls();
            for (Building wall : toDamage) {
                Draw.color(team.color);
                Draw.alpha(0.5f);
                Fill.square(wall.x, wall.y, 2);
            }
            Draw.reset();
        }

        public void updateProximityWall() {
            connectedWalls.clear();

            for (Point2 point : checkPos) {
                Building other = world.build(tile.x + point.x, tile.y + point.y);
                if (other == null || other.team != team) continue;
                if (other instanceof AdaptWallBuild wall) {
                    wall.updateDrawRegion();
                    if (checkAutotileSame(other)) {
                        connectedWalls.add((AdaptWallBuild) other);
                    }
                }
            }

            updateDrawRegion();
        }

        public void drawTeam() {
            Draw.color(team.color);
            Draw.alpha(0.25f);
            Draw.z(Layer.blockUnder);
            Fill.square(x, y, 5f);
            Draw.color();
        }

        @Override
        public boolean checkSolid() {
            return false;
        }

        @Override
        public float handleDamage(float amount) {
            findLinkWalls();
            float shareDamage = (amount / toDamage.size) * (1 - damageReduction);
            for (Building b : toDamage) {
                damageShared(b, shareDamage);
            }
            return shareDamage;
        }

        public void damageShared(Building building, float damage) {
            if (!building.dead()) {
                float dm = state.rules.blockHealth(team);
                damage = Mathf.zero(dm)? building.health + 1: damage / dm;
                if (!net.client()) building.health -= damage;
                building.healthChanged();
                if (building.health <= 0) Call.buildDestroyed(building);
                NHFx.shareDamage.at(building.x, building.y, building.block.size * tilesize / 2f,
                        team.color, Mathf.clamp(damage / (block.health * 0.1f)));
            }
        }

        @Override
        public void draw() {
            Draw.z(Layer.block + 1f);
            Draw.rect(atlasRegion[drawIndex], x, y);
        }

        public void updateProximity() {
            super.updateProximity();

            updateProximityWall();
            for (AdaptWallBuild other : connectedWalls) {
                other.updateProximityWall();
            }
        }

        @Override
        public void onRemoved() {
            for (AdaptWallBuild other : connectedWalls) {
                other.updateProximityWall();
            }
            super.onRemoved();
        }
    }
}