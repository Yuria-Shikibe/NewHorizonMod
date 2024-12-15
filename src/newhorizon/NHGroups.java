package newhorizon;

import arc.math.Mathf;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.game.Team;
import mindustry.gen.Groups;
import newhorizon.expand.NHVars;
import newhorizon.expand.block.commandable.CommandableBlock;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.expand.block.synth.SynthUpdater;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.AutoEventTrigger;

public class NHGroups{
	public static final EntityGroup<AutoEventTrigger> autoEventTrigger = new EntityGroup<>(AutoEventTrigger.class, false, true, (u, i) -> {});
	public static final EntityGroup<WorldEvent> events = new EntityGroup<>(WorldEvent.class, false, true, (u, i) -> {});
	public static final ObjectSet<RemoteCoreStorage.RemoteCoreStorageBuild>[] placedRemoteCore = new ObjectSet[Team.all.length];
	public static QuadTree<GravityTrapField> gravityTraps = new QuadTree<>(Vars.world.getQuadBounds(new Rect()));
	public static final Seq<GravityTrapField> gravityTrapsDraw = new Seq<>();
	public static final Seq<CommandableBlock.CommandableBlockBuild> commandableBuilds = new Seq<>();

	public static SynthUpdater synth = new SynthUpdater();

	static{
		for(int i = 0; i < Team.all.length; i++){
			NHGroups.placedRemoteCore[i] = new ObjectSet<>(i < 6 ? 20 : 1);
		}
	}
	
	public static void worldInit() {
		gravityTraps = new QuadTree<>(Vars.world.getQuadBounds(new Rect()));
		gravityTrapsDraw.each(g -> gravityTraps.insert(g));

		synth.worldInit();
	}
	
	public static void clear(){
		events.clear();
		autoEventTrigger.clear();
		gravityTraps.clear();
		gravityTrapsDraw.clear();
		commandableBuilds.clear();
		RemoteCoreStorage.clear();
	}

	public static void worldReset(){
		synth.worldReset();
	}
	
	public static void update(){
		if(NHVars.worldData.eventReloadSpeed > 0){
			AutoEventTrigger.timeScale = NHVars.worldData.eventReloadSpeed;
		}else{
			AutoEventTrigger.timeScale = AutoEventTrigger.getSettingScale();
		}
		
		if(Vars.headless)AutoEventTrigger.timeScale *= Mathf.curve(Groups.player.size(), 1.125f, 7.5f);
	}

	public static void draw(){
		//allGraph.values().toArray().each(FloodGraph::draw);
	}
}
