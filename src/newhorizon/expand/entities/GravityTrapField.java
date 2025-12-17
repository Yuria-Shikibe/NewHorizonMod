package newhorizon.expand.entities;

import arc.math.geom.Position;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import mindustry.game.Team;
import mindustry.gen.Teamc;
import newhorizon.NHGroups;

public class GravityTrapField implements Position, QuadTree.QuadTreeObject {
    public float x, y, range;
    public boolean active;
    public Team owner;

    public GravityTrapField(Team owner, float range) {
        this.range = range;
        this.owner = owner;
        add();
    }

    public void update(Teamc entity) {
        x = entity.x();
        y = entity.y();
        owner = entity.team();
    }

    public void active(boolean active) {
        this.active = active;
    }

    public void add() {
        NHGroups.gravityTraps.insert(this);
    }

    public void remove() {
        NHGroups.gravityTraps.remove(this);
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void hitbox(Rect out) {
        out.setSize(range * 2).setCenter(x, y);
    }

    @Override
    public String toString() {
        return "GravityTrapField{" + "pos(" + x + ", " + y + ")}";
    }
}
