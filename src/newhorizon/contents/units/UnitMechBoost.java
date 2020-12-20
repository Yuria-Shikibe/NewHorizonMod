package newhorizon.contents.units;

import arc.util.Tmp;
import mindustry.gen.MechUnitLegacyQuasar;
import mindustry.gen.Trailc;
import mindustry.graphics.Trail;

public class UnitMechBoost extends MechUnitLegacyQuasar implements Trailc {
    public Trail trail = new Trail(6);
    public String toString() {
        return "UnitMechBoost#" + this.id;
    }

    public UnitMechBoost(){
        super();
    }

    public Trail trail(){
        return trail;
    }

    public void trail(Trail trail){
        this.trail = trail;
    }

    @Override
    public void update(){
        super.update();

        trail.length = type.trailLength;
        float scale = elevation;
        float offset = type.engineOffset / 2.0F + type.engineOffset / 2.0F * scale;
        Tmp.v1.trns(rotation - 180, offset).add(this);

        trail.update(Tmp.v1.x, Tmp.v1.y);
    }
}
