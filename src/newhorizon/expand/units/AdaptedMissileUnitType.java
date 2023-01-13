package newhorizon.expand.units;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Sounds;
import mindustry.gen.TimedKillc;
import mindustry.gen.Unit;
import mindustry.graphics.Trail;
import mindustry.type.unit.MissileUnitType;

public class AdaptedMissileUnitType extends MissileUnitType{
	public float trailAppearDelay = 0f;
	
	public AdaptedMissileUnitType(String name){
		super(name);
		constructor = AdaptedTimedKillUnit::new;
		deathSound = Sounds.explosion;
	}
	
	@Override
	public void drawEngines(Unit unit){}
	
	@Override
	public void drawTrail(Unit unit){
		if(((TimedKillc)unit).time() > trailAppearDelay){
			if(unit.trail == null)unit.trail = new Trail(trailLength);
			
			Trail trail = unit.trail;
			Color color = trailColor == null ? unit.team.color : trailColor;
			float width = (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f) * (useEngineElevation ? unit.elevation : 1f)) * trailScl;
			trail.draw(color, width);
			trail.drawCap(color, width);
		}
	}
}
