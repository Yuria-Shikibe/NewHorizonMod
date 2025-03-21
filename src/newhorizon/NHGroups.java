package newhorizon;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.entities.EntityGroup;
import mindustry.game.Team;
import newhorizon.expand.block.commandable.CommandableBlock;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.expand.entities.WorldEvent;

import static mindustry.Vars.world;

public class NHGroups{
	public static final EntityGroup<WorldEvent> events = new EntityGroup<>(WorldEvent.class, false, true, (u, i) -> {});
	public static final ObjectSet<RemoteCoreStorage.RemoteCoreStorageBuild>[] placedRemoteCore = new ObjectSet[Team.all.length];
	public static QuadTree<GravityTrapField> gravityTraps = new QuadTree<>(world.getQuadBounds(new Rect()));
	public static final Seq<GravityTrapField> gravityTrapsDraw = new Seq<>();
	public static final Seq<CommandableBlock.CommandableBlockBuild> commandableBuilds = new Seq<>();


	static{
		for(int i = 0; i < Team.all.length; i++){
			NHGroups.placedRemoteCore[i] = new ObjectSet<>(i < 6 ? 20 : 1);
		}
	}
	
	public static void worldInit() {
		gravityTraps = new QuadTree<>(world.getQuadBounds(new Rect()));
		gravityTrapsDraw.each(g -> gravityTraps.insert(g));

	}
	
	public static void clear(){
		events.clear();
		gravityTraps.clear();
		gravityTrapsDraw.clear();
		commandableBuilds.clear();
		RemoteCoreStorage.clear();
	}

	public static void worldReset(){
	}

	public static void update(){}


	public static void draw(){
	}
}
