package newhorizon.vars;

import arc.math.Rand;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.world.Tile;

public class NHVars{
	public static Rand rand = new Rand();
	public static Tile tmpTile;
	public static final Seq<Team> allTeamSeq = new Seq<Team>(Team.all.length).addAll(Team.all);
}
