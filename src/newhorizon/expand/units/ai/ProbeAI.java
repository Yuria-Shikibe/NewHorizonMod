package newhorizon.expand.units.ai;

import arc.math.geom.Position;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.TargetPriority;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeAI extends SniperAI{
	public float nullReload = 0;
	public ProbeEntity probe;
	
	public boolean checkTarget(Teamc target, float x, float y, float range){
		return
			(!(target instanceof Building)) ||
					probe.scanned.contains((Building)target) || probe.targetTeam != target.team() ||
					Units.invalidateTarget(target, unit.team, x, y, range);
	}
	
	@Override
	public void unit(Unit unit){
		super.unit(unit);
		
		if(!(unit instanceof ProbeEntity))return;
		probe = (ProbeEntity)unit;
	}
	
	@Override
	public void updateUnit(){
		if(probe == null)return;
		
		if(probe.scanTarget != null){
			target = probe.scanTarget;
			if(!probe.scanTarget.isValid())target = null;
		}
		
		super.updateUnit();
		
		if(target == null){
			nullReload += Time.delta;
			if(nullReload > 90f){
				Position target = null;
				if(unit.team.cores().any()){
					target = unit.team.core();
				}else if(unit.team == Vars.state.rules.waveTeam && Vars.spawner.getSpawns().any()){
					target = Vars.spawner.getFirstSpawn();
				}else return;
				moveTo(target, unit.type.maxRange / 2f);
			}
		}
	}
	
	@Override
	public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
		Teamc result = findMainTarget(x, y, range, air, ground);
		
		//if the main target is in range, use it, otherwise target whatever is closest
		return checkTarget(result, x, y, range) ? target(x, y, range, air, ground) : result;
	}
	
	@Override
	public void updateTargeting(){
		super.updateTargeting();
	}
	
	@Override
	public boolean retarget(){
		return super.retarget() && !probe.scanning;
	}
	
	@Override
	public Teamc target(float x, float y, float range, boolean air, boolean ground){
		Building target = Vars.indexer.findTile(probe.targetTeam, x, y, range, u -> u.block.priority >= TargetPriority.base && !probe.scanned.contains(u));

		if (target != null){
			if(probe.scanned.contains(target))target = null;
		}

		if(target == null){
			if(probe.targetTeam.cores().any())return probe.targetTeam.core();
			else return null;
		}
		
		return target;
	}
}
