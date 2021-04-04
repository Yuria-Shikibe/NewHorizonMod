package newhorizon.vars;

import arc.math.geom.Rect;
import arc.struct.Seq;
import mindustry.game.Team;

public class NHVars{
	public static final Seq<Team> allTeamSeq = new Seq<Team>(Team.all.length).addAll(Team.all);
	public static final Rect rect = new Rect();
}
