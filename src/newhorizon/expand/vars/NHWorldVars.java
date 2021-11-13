package newhorizon.expand.vars;

import arc.struct.OrderedSet;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.special.CommandableBlock;
import newhorizon.expand.interfaces.BeforeLoadc;
import newhorizon.expand.interfaces.ServerInitc;

public class NHWorldVars{
	public transient boolean serverLoaded = true;
	public transient boolean worldLoaded = false;
	public transient boolean load = false;
	
	public static final OrderedSet<ServerInitc> serverLoad = new OrderedSet<>();
	public static final OrderedSet<BeforeLoadc> advancedLoad = new OrderedSet<>();
	
	public transient final OrderedSet<GravityTrap.GravityTrapBuild> gravityTraps = new OrderedSet<>();
	public transient final OrderedSet<CommandableBlock.CommandableBlockBuild> commandables = new OrderedSet<>();
	
	public transient int ix, iy;
	public transient int commandPos = -1;
	
	
	public void clear(){
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
