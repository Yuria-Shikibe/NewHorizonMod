package newhorizon.units;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.EventType;
import mindustry.gen.Trailc;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Trail;

public class UnitBoostEntity extends UnitEntity implements Trailc {
    public Interval timer = new Interval(4);
    public Trail trail = new Trail(30);
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
        
        Events.on(EventType.WorldLoadEvent.class, e -> {
            trail = new Trail(25);
            trail.clear();
        });
        
        if(timer.get(0, 4 / type().speed)) {
            trail.length = type.trailLength;
            float scale = elevation;
            float offset = type.engineOffset / 2.0F + type.engineOffset / 2.0F * scale;
            Tmp.v1.trns(rotation - 180, offset).add(x, y);

            trail.update(Tmp.v1.x, Tmp.v1.y);
        }
    }
}
