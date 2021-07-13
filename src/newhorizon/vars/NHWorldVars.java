package newhorizon.vars;

import arc.struct.Seq;
import newhorizon.block.defence.GravityTrap;
import newhorizon.block.special.CommandableBlock;
import newhorizon.block.special.UpgradeBlock;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.interfaces.ServerInitc;

public class NHWorldVars{
	public transient boolean serverLoaded = true;
	public transient boolean worldLoaded = false;
	public transient boolean load = false;
	
	public static final Seq<ServerInitc> serverLoad = new Seq<>();
	public static final Seq<BeforeLoadc> advancedLoad = new Seq<>();
	
	public transient final Seq<UpgradeBlock.UpgradeBlockBuild> upgraderGroup = new Seq<>();
	public transient final Seq<GravityTrap.GravityTrapBuild> gravityTraps = new Seq<>();
	public transient final Seq<CommandableBlock.CommandableBlockBuild> commandables = new Seq<>();
	
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
