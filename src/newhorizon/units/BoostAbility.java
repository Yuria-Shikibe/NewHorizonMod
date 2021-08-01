package newhorizon.units;

import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Tmp;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import newhorizon.content.NHStatusEffects;

public class BoostAbility extends Ability{
	public static final int maxSize = 8;
	
	public float velocityMultiple = 3f;
	public float warmupTime = 120f;
	public int trailLength = 8;
	
	public float angleCone = 0.003f;
//	public float accelUp = 0.045f;
//	public float accelDown = 0.07f;
	
	protected Trail trail = new Trail(trailLength);
	protected Seq<Float> seq = new Seq<>();
	protected Interval timer = new Interval();
	
	@Override
	public BoostAbility copy(){
		BoostAbility out = (BoostAbility)super.copy();
		out.trail = new Trail(trailLength);
		out.seq = new Seq<>(maxSize);
		out.timer = new Interval();
		return out;
	}
	
	public boolean allSame(float angle){
		if(seq.size < maxSize - 1)return false;
		for(float f : seq){
			if(!Angles.within(angle, f, 5f)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void update(Unit unit){
		float angle = unit.vel.angle();
		float speed =unit.vel.len();
		boolean same = allSame(angle);
		
		if(seq.size > maxSize)seq.remove(0);
		if(timer.get(12f) && speed > 0.1f)seq.add(angle);
		
		Tmp.v1.trns(unit.rotation, -unit.type.engineOffset);
		trail.update(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, same ? 1 : 0);
		
		if(same){
			unit.apply(NHStatusEffects.accel_3, 20f);
		}
	}
	
	@Override
	public void draw(Unit unit){
		float z = Draw.z();
		Draw.z(unit.type.lowAltitude ? Layer.flyingUnitLow - 0.001f : Layer.flyingUnit - 0.001f);
		trail.draw(unit.team.color, unit.type.engineSize / 2f);
		Draw.z(z);
	}
}
