package newhorizon.expand.entities;

import arc.math.geom.Position;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import mindustry.game.Team;
import mindustry.gen.Teamc;
import newhorizon.NHGroups;

public class GravityTrapField implements Position, QuadTree.QuadTreeObject {
    public float scale;
    public Rect rect;
    public Team owner;

    public GravityTrapField(Teamc entity, float scale, float radius) {
        rect = new Rect();
        update(entity, scale, radius);
        add();
    }

    public GravityTrapField(Teamc entity, float radius) {
        rect = new Rect();
        update(entity, 1f, radius);
        add();
    }

    public void update(Teamc entity, float scale, float radius) {
        rect.setCentered(entity.getX(), entity.getY(), radius * 2f);
        owner = entity.team();
        this.scale = scale;
    }

    public void update(Teamc entity) {
        rect.setCenter(entity.getX(), entity.getY());
        owner = entity.team();
    }


    public boolean isActive(boolean active) {
        return scale > 0.1f;
    }

    public float getGravityTrap(){
        return scale * rect.area();
    }

    public void add() {
        NHGroups.gravityFields.insert(this);
        NHGroups.gravityFieldSeq.add(this);
    }

    public void remove() {
        NHGroups.gravityFields.remove(this);
        NHGroups.gravityFieldSeq.remove(this);
    }

    @Override
    public float getX() {
        return rect.getX();
    }

    @Override
    public float getY() {
        return rect.getY();
    }

    @Override
    public void hitbox(Rect out) {
        out.set(rect);
    }

    @Override
    public String toString() {
        return "GravityTrapField{" + "pos(" + getX() + ", " + getY() + ")}";
    }
}
