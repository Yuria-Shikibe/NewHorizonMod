package newhorizon.expand.vars;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.OrderedSet;
import mindustry.Vars;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.special.CommandableBlock;

public class NHWorldVars{
	public transient boolean serverLoaded = true;
	public transient boolean worldLoaded = false;
	public transient boolean load = false;
	
	public transient final QuadTree<GravityTrap.TrapField> gravityTraps;
	public transient final OrderedSet<CommandableBlock.CommandableBlockBuild> commandables = new OrderedSet<>();
	
	public transient int ix, iy;
	public transient int commandPos = -1;
	
	public NHWorldVars(){
		gravityTraps = new QuadTree<>(Vars.world.getQuadBounds(new Rect()));
	}
	
	public void clear(){
		commandables.clear();
		gravityTraps.clear();
		
		ix = iy = 0;
		commandPos = -1;
	}
	
	public void afterLoad(){
		gravityTraps.bounds.set(Vars.world.getQuadBounds(new Rect()));
	}
}
