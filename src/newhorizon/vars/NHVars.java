package newhorizon.vars;

import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.world.Tile;

public class NHVars{
	public static Tile tmpTile;
	public static final Seq<Team> allTeamSeq = new Seq<Team>(Team.all.length).addAll(Team.all);
}
