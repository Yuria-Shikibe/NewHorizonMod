package newhorizon.expand.units;

import mindustry.ai.types.FlyingAI;
import mindustry.entities.units.UnitCommand;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.state;

public class SniperAI extends FlyingAI{
	protected static final float APPROACHING_DST = 64f;
	
	@Override
	public void updateWeapons(){
		super.updateWeapons();
	}
	
	@Override
	public void updateMovement(){
		unloadPayloads();
		
		if(target != null && unit.hasWeapons() && command() == UnitCommand.attack){
			if(!unit.type.circleTarget){
				if(unit.within(target, unit.type.maxRange - APPROACHING_DST)){
					moveTo(target, unit.type.maxRange - APPROACHING_DST / 2);
					unit.lookAt(target);
					unit.lookAt(target); //Always Look At target in range
				}else{
					moveTo(target, unit.type.maxRange - APPROACHING_DST / 2);
					unit.lookAt(target);
				}
			}else{
				attack(unit.type.range * 0.75f);
			}
		}
		
		if(target == null && command() == UnitCommand.attack && state.rules.waves && unit.team == state.rules.defaultTeam){
			moveTo(getClosestSpawner(), Math.max(state.rules.dropZoneRadius + 120f, unit.type.range - APPROACHING_DST));
		}
		
		if(command() == UnitCommand.rally){
			moveTo(targetFlag(unit.x, unit.y, BlockFlag.rally, false), 60f);
		}
	}
}
