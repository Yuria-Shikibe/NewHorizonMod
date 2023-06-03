package newhorizon.expand.units;

import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.Seq;
import mindustry.ai.types.FlyingAI;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.world.meta.BlockFlag;

public class ProbeAI extends FlyingAI{
	public Seq<Building> scannedBuilds = new Seq<>();
	public IntSet scanned = new IntSet();
	
	@Override
	public void updateMovement(){
		super.updateMovement();
	}
	
	@Override
	public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
		Teamc result = findMainTarget(x, y, range, air, ground);
		
		//if the main target is in range, use it, otherwise target whatever is closest
		return checkTarget(result, x, y, range) ? target(x, y, range, air, ground) : result;
	}
	
	@Override
	public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground){
		Teamc core = targetFlag(x, y, BlockFlag.core, true);
		
		if(core != null && Mathf.within(x, y, core.getX(), core.getY(), range)){
			return core;
		}
		
		for(BlockFlag flag : unit.type.targetFlags){
			if(flag == null){
				Teamc result = target(x, y, range, air, ground);
				if(result != null) return result;
			}else if(ground){
				Teamc result = targetFlag(x, y, flag, true);
				if(result != null) return result;
			}
		}
		
		return core;
	}
}
