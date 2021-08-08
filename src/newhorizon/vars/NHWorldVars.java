package newhorizon.vars;

import arc.struct.OrderedSet;
import newhorizon.block.defence.GravityTrap;
import newhorizon.block.special.CommandableBlock;
import newhorizon.block.special.UpgradeBlock;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.interfaces.ServerInitc;

public class NHWorldVars{
	public transient boolean serverLoaded = true;
	public transient boolean worldLoaded = false;
	public transient boolean load = false;
	
	public static final OrderedSet<ServerInitc> serverLoad = new OrderedSet<>();
	public static final OrderedSet<BeforeLoadc> advancedLoad = new OrderedSet<>();
	
	public transient final OrderedSet<UpgradeBlock.UpgradeBlockBuild> upgraderGroup = new OrderedSet<>();
	public transient final OrderedSet<GravityTrap.GravityTrapBuild> gravityTraps = new OrderedSet<>();
	public transient final OrderedSet<CommandableBlock.CommandableBlockBuild> commandables = new OrderedSet<>();
	
	public transient int ix, iy;
	public transient int commandPos = -1;
	
	
	public void clear(){
		
		upgraderGroup.clear();
		commandables.clear();
		gravityTraps.clear();
		
		ix = iy = 0;
		commandPos = -1;
	}
	
	public void clearLast(){
		advancedLoad.clear();
		serverLoad.clear();
	}
	
}
