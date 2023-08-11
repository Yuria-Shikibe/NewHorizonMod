package newhorizon.expand.game;

import arc.math.Mathf;
import mindustry.Vars;
import mindustry.entities.Units.Sortf;
import mindustry.world.meta.BlockGroup;

public class NHUnitSorts{
	private static float dcr;
	
	public static final Sortf slowest = (u, x, y) -> u.speed() + Mathf.dst2(u.x, u.y, x, y) / 6400f;
	public static final Sortf fastest = (u, x, y) -> -u.speed() + Mathf.dst2(u.x, u.y, x, y) / 6400f;
	
	public static final Sortf regionalHPMaximum_Unit = (u, x, y) -> {
		dcr = 0;
		Vars.state.teams.get(u.team).tree().intersect(u.x - 68, u.y - 68, 128, 128, t -> {
			dcr -= t.health + t.shield;
		});
		
		return dcr;
	};
	
	public static final Sortf regionalHPMaximum_Building = (u, x, y) -> {
		dcr = 0;
		
		Vars.indexer.eachBlock(u, 128, bi -> !(bi.block.group == BlockGroup.walls), b -> {
			dcr -= b.health;
		});
		
		return dcr;
	};
	
	public static final Sortf regionalHPMaximum_All = (u, x, y) -> {
		dcr = 0;
		Vars.state.teams.get(u.team).tree().intersect(u.x - 68, u.y - 68, 128, 128, t -> {
			dcr -= t.health+ t.shield;
		});
		
		Vars.indexer.eachBlock(u, 128, bi -> !(bi.block.group == BlockGroup.walls), b -> dcr -= b.health);
		
		return dcr;
	};
}
