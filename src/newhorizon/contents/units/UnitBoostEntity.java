package newhorizon.contents.units;

import arc.util.Interval;
import arc.util.Tmp;
import mindustry.gen.MechUnitLegacyQuasar;
import mindustry.gen.Trailc;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Trail;

public class UnitBoostEntity extends UnitEntity implements Trailc {
    public Interval timer = new Interval(4);;
    public Trail trail = new Trail(25);
    public String toString() {
        return "UnitBoostEntity#" + this.id;
    }

    public UnitBoostEntity(){
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

        if(timer.get(0, 1f / type.speed)) {
            trail.length = type.trailLength;
            float scale = elevation;
            float offset = type.engineOffset / 2.0F + type.engineOffset / 2.0F * scale;
            Tmp.v1.trns(rotation - 180, offset).add(x, y);

            trail.update(Tmp.v1.x, Tmp.v1.y);
        }
    }
}
