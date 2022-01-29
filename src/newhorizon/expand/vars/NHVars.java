package newhorizon.expand.vars;

import arc.math.Rand;
import mindustry.world.Tile;

public class NHVars{
	public static NHWorldVars world = new NHWorldVars();
	public static NHCtrlVars ctrl = new NHCtrlVars();
	
	public static Rand rand = new Rand();
	public static Tile tmpTile;
	
	public static void reset(){
		ctrl = new NHCtrlVars();
		world.clear();
	}
	
	public static void resetCtrl(){
		ctrl = new NHCtrlVars();
	}
}
