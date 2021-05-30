package newhorizon.vars;

import arc.math.Rand;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.world.Tile;

public class NHVars{
	public static NHWorldVars world = new NHWorldVars();
	public static NHCtrlVars ctrl = new NHCtrlVars();
	
	public static Rand rand = new Rand();
	public static Tile tmpTile;
	public static final Seq<Team> allTeamSeq = new Seq<Team>(Team.all.length).addAll(Team.all);
	
	public static void reset(){
		ctrl = new NHCtrlVars();
		world = new NHWorldVars();
	}
	
	public static void resetCtrl(){
		ctrl = new NHCtrlVars();
	}
}
