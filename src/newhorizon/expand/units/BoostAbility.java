package newhorizon.expand.units;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;

public class BoostAbility extends Ability{
	public static final int maxSize = 8;
	
	public float velocityMultiple = 3f;
	public float warmupTime = 120f;
	public int trailLength = 8;
	
	public float angleCone = 5f;
//	public float accelUp = 0.045f;
//	public float accelDown = 0.07f;
	
	
	public BoostAbility(){
		this(3);
	}
	
	public BoostAbility(float velocityMultiple, float angleCone){
		this.velocityMultiple = velocityMultiple;
		this.angleCone = angleCone;
	}
	
	public BoostAbility(float velocityMultiple){
		this.velocityMultiple = velocityMultiple;
	}
	
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
	
	public float warmup(float angle){
		float f = 0;
		for(float i : seq){
			if(Angles.within(angle, i, angleCone))f++;
		}
		return f / seq.size;
	}
	
	public boolean allSame(float angle){
		if(seq.size < maxSize - 1)return false;
		for(float f : seq){
			if(!Angles.within(angle, f, angleCone)){
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
		
		
		if(same){
			unit.speedMultiplier(velocityMultiple);
		}
		
		if(seq.size > maxSize)seq.remove(0);
		if(timer.get(12f) && speed > 0.1f)seq.add(angle);
		
		if(Vars.headless)return;
		Tmp.v1.trns(unit.rotation, -unit.type.engineOffset + unit.type.engineSize / 2);
		trail.update(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, same ? 1 : 0);
		
	}
	
	@Override
	public void draw(Unit unit){
		float z = Draw.z();
		Draw.z(unit.type.lowAltitude ? Layer.flyingUnitLow - 0.001f : Layer.flyingUnit - 0.001f);
		trail.draw(unit.team.color, unit.type.engineSize / 1.5f);
		Draw.z(z);
		
		int particles = (int)unit.type.hitSize;
		float particleLife = 40f, particleRad = unit.type.hitSize, particleStroke = 1.1f, particleLen = unit.hitSize / 8f;
		Rand rand = new Rand();
		float base = (Time.time / particleLife);
		rand.setSeed(unit.id);
		
		float warmup = warmup(unit.rotation) * Mathf.clamp(unit.vel.len() / unit.speed());
		Tmp.v1.trns(unit.rotation, unit.type.engineOffset * 1.25f);
		
		Draw.blend(Blending.additive);
		
		Draw.color(Color.white, warmup * 0.6f);
		
		for(int i = 0; i < particles; i++){
			float fin = (rand.random(1f) + base) % 1f * warmup, fout = 1f - fin;
			float angle = unit.rotation + rand.range(60f) - 180;
			float len = particleRad * Interp.pow2Out.apply(fin);
			Lines.lineAngle(unit.x + Angles.trnsx(angle, len) + Tmp.v1.x, unit.y + Angles.trnsy(angle, len) + Tmp.v1.y, angle, particleLen * fout * warmup);
		}
		
		Draw.blend();
	}
}
