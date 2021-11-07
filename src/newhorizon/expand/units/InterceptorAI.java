package newhorizon.expand.units;

import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.entities.units.UnitCommand;
import mindustry.gen.Teamc;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.state;

public class InterceptorAI extends FlyingAI{
	protected boolean needRepair = false;
	@Override
	public void updateMovement(){
		unloadPayloads();
		
		
		if(command() == UnitCommand.rally){
			moveTo(targetFlag(unit.x, unit.y, BlockFlag.rally, false), 60f);
		}
		
		if(target == null && timer.get(timerTarget2, 20f)){
			target = Units.bestTarget(unit.team, unit.x, unit.y, 1200f, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), e -> unit.type.targetGround, (u, x1, y1) -> -u.speed());
		}
		
		if(target != null && unit.hasWeapons() && command() == UnitCommand.attack){
			if(!unit.type.circleTarget){
				moveTo(target, unit.type.range * 0.75f);
				unit.lookAt(target);
			}else{
				attack(unit.type.range * 0.8f);
			}
		}
		
		if(target == null && command() == UnitCommand.attack && state.rules.waves && unit.team == state.rules.defaultTeam){
			moveTo(getClosestSpawner(), Math.max(state.rules.dropZoneRadius + 120f, unit.type.range * 0.8f));
		}
		
	}
	
//	protected void attack(float circleLength){
//		vec.set(target).sub(unit);
//		float ang = unit.angleTo(target);
//		float len = vec.len();
//		if(len < circleLength / 2f){
//			vec.trns(ang - 180 + 90 * (1f - len / circleLength), unit.speed());
//		}else if(len > circleLength){
//			vec.trns((float)(ang + Math.asin(circleLength / len)), unit.speed()).rotateRad(Mathf.sin(20f, 0.75f));
//		}else vec.trns(ang - 90, Math.min(unit.speed(), unit.type.rotateSpeed * unit.speedMultiplier() * len));
//		//
//		//		float diff = Angles.angleDist(ang, unit.rotation());
//		//
//		//		if(diff > 70f && vec.len() < circleLength){
//		//			vec.setAngle(unit.vel().angle());
//		//		}else{
//		//			vec.setAngle(Angles.moveToward(unit.vel().angle(), vec.angle(), 6f));
//		//		}
//		//
//		//		vec.setLength(unit.speed());
//
////		unit.rotation = Angles.moveToward(unit.rotation, ang, unit.type.rotateSpeed * Time.delta * unit.speedMultiplier());
//		unit.moveAt(vec);
//		unit.lookAt(ang);
//		unit.lookAt(ang);
//	}
	
	@Override
	public Teamc target(float x, float y, float range, boolean air, boolean ground){
		Teamc t = Units.bestTarget(unit.team, x, y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), e -> unit.type.targetGround, (u, x1, y1) ->
			-u.maxHealth()
		);
		return t == null ? target : t;
	}
	
	@Override
	public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
		return target(x, y, range, air, ground);
	}
	
	@Override
	public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground){
		return target(x, y, range, air, ground);
	}
}
