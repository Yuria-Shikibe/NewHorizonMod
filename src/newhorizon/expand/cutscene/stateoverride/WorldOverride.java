package newhorizon.expand.cutscene.stateoverride;

import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;

public class WorldOverride{
	public static FOVGiver dummyBuild;
	
	public static Pool<FOVGiver> fovGiverPool = new Pool<FOVGiver>(){
		@Override
		protected FOVGiver newObject(){
			return new FOVGiver();
		}
	};
	
	public static boolean visible(Team team, Building building){
		return Vars.fogControl.isVisible(team, building.x, building.y);
	}
	
	public static boolean visible(Building building){
		return building != null && Vars.fogControl.isVisible(Vars.state.rules.defaultTeam, building.x, building.y);
	}
	
	public static void getFov(float x, float y, Team team, float radius){
		if(Vars.state.rules.fog){
			dummyBuild = new FOVGiver();
			dummyBuild.radius = radius;
			dummyBuild.team(team);
			dummyBuild.set(x, y);
			dummyBuild.tile = new Tile(World.toTile(x), World.toTile(y));
			Vars.fogControl.forceUpdate(team, dummyBuild);
		}
	}
	
	public static Building getDummy(Team team, float x, float y){
		Building b = Building.create();
		b.set(x, y);
		b.team(team);
		b.tile = new Tile(World.toTile(x), World.toTile(y));
		
		return b;
	}
	
	private static class FOVGiver extends Building implements Pool.Poolable{
		public float radius;
		
		public FOVGiver(){}
		
		@Override
		public float fogRadius(){
			return radius;
		}
		
		/** Resets the object for reuse. Object references should be nulled and fields may be set to default values. */
		@Override
		public void reset(){
			x = y = 0;
			team = Team.derelict;
			radius = 0;
		}
	}
}
