package newhorizon.expand.units.ai;

import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.gen.Flyingc;
import mindustry.gen.Teamc;

import static mindustry.Vars.state;

//TODO to be done
public class InterceptorAI extends FlyingAI{
	//	@Override
	//	public void updateMovement(){
	//		unloadPayloads();
	//
	//
	//		if(command() == UnitCommand.rally){
	//			moveTo(targetFlag(unit.x, unit.y, BlockFlag.rally, false), 60f);
	//		}
	//
	//		if(target == null && timer.get(timerTarget2, 20f)){
	//			target = Units.bestTarget(unit.team, unit.x, unit.y, 1200f, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), e -> unit.type.targetGround, (u, x1, y1) -> -u.speed());
	//		}
	//
	//		if(target != null && unit.hasWeapons() && command() == UnitCommand.attack){
	//			if(!unit.type.circleTarget){
	//				moveTo(target, unit.type.range * 0.75f);
	//				unit.lookAt(target);
	//			}else{
	//				attack(unit.type.range * 0.8f);
	//			}
	//		}
	//
	//		if(target == null && command() == UnitCommand.attack && state.rules.waves && unit.team == state.rules.defaultTeam){
	//			moveTo(getClosestSpawner(), Math.max(state.rules.dropZoneRadius + 120f, unit.type.range * 0.8f));
	//		}
	//
	//	}
	
	@Override
	public void updateMovement(){
		unloadPayloads();
		
		if(target != null && unit.hasWeapons()){
			if(unit.type.circleTarget){
				circleAttack(240f);
			}else{
				moveTo(target, unit.type.range * 0.8f);
				unit.lookAt(target);
			}
		}
		
		if(target == null && state.rules.waves && unit.team == state.rules.defaultTeam){
			moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 230f);
		}
	}
	
	@Override
	public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
		Teamc result = findMainTarget(x, y, range, air, false);
		
		//if the main target is in range, use it, otherwise target whatever is closest
		return checkTarget(result, x, y, range) ? target(x, y, range, air, false) : result;
	}
	
//	@Override
//	public boolean checkTarget(TeamHealthc target, float x, float y, float range){
//		if(target instanceof Unit){
//			return ((Unit) target).isFlying() && super.checkTarget(target, x, y, range);
//		}
//	}
	
	@Override
	public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground){
		return Units.closestEnemy(unit.team, x, y, range * 10, Flyingc::isFlying);
	}
//	protected boolean needRepair = false;

	
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
	
//	@Override
//	public TeamHealthc target(float x, float y, float range, boolean air, boolean ground){
//		TeamHealthc t = Units.bestTarget(unit.team, x, y, range, u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround), e -> unit.type.targetGround, (u, x1, y1) ->
//			-u.maxHealth()
//		);
//		return t == null ? target : t;
//	}
//
//	@Override
//	public TeamHealthc findTarget(float x, float y, float range, boolean air, boolean ground){
//		return target(x, y, range, air, ground);
//	}
//
//	@Override
//	public TeamHealthc findMainTarget(float x, float y, float range, boolean air, boolean ground){
//		return target(x, y, range, air, ground);
//	}
}
