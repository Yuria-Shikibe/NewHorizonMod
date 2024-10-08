package newhorizon.expand.entities.shield;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Polygon;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import newhorizon.content.NHStatusEffects;

public class shieldEntity implements Entityc {
    public Team team;
    public Vec2 normalVec;
    public Polygon shieldRect;

    public Polygon createShieldRect(Vec2 v1, Vec2 v2){
        Polygon p = new Polygon();
        Vec2 center = new Vec2((v1.x + v2.x)/2, (v1.y + v2.y)/2);
        float rotation = Angles.angle(v1.x, v1.y, v2.x, v2.y);
        float length = Mathf.dst(v1.x, v1.y, v2.x, v2.y);
        float width = 16f;
        float[] vertices = new float[] {
            -length/2, -width/2, -length/2, width/2,
            length/2, width/2, length/2, -width/2,
        };
        p.setVertices(vertices);
        p.setPosition(center.x, center.y);
        p.setRotation(rotation);
        return new Polygon();
    }

    public void setNormalVec(Vec2 normalVec){
        this.normalVec = normalVec;
    }

    @Override
    public <T extends Entityc> T self() {
        return null;
    }

    @Override
    public <T> T as() {
        return null;
    }

    @Override
    public boolean isAdded() {
        return false;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean serialize() {
        return false;
    }

    @Override
    public int classId() {
        return 0;
    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public void add() {

    }

    @Override
    public void afterRead() {

    }

    @Override
    public void id(int i) {

    }

    @Override
    public void read(Reads reads) {

    }

    @Override
    public void remove() {

    }

    @Override
    public void update() {
        Rect rect = shieldRect.getBoundingRectangle();
        Groups.bullet.intersect(rect.x, rect.y, rect.width, rect.height, bullet -> {
            if (shieldRect.contains(bullet.x, bullet.y)) {
                bullet.absorb();
            }
        });
        Units.nearbyEnemies(team, rect, unit -> {
            unit.apply(NHStatusEffects.emp3);
            unit.vel.setZero();
            unit.move(normalVec.setLength(0.5f));
        });
    }

    @Override
    public void write(Writes writes) {

    }
}
