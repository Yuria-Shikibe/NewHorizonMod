package newhorizon.units;

import arc.util.Log;
import arc.util.Tmp;
import mindustry.gen.Trailc;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;

public class UnitBoostEntity extends UnitEntity implements Trailc{
    public Trail trail = new Trail(10);
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
    public void add(){
        super.add();
        trail = new Trail(type.trailLength);
        Log.info("added" + this + " | " + id);
    }
    
    @Override
    public void setType(UnitType type){
        super.setType(type);
    }
    
    @Override
    public void update(){
        super.update();
        
        float scale = elevation;
        float offset = type.engineOffset / 2.0F + type.engineOffset / 2.0F * scale;
        Tmp.v1.trns(rotation - 180, offset).add(x, y);
        
        trail.update(Tmp.v1.x, Tmp.v1.y);
    }
}
