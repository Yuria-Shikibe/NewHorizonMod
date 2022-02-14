package newhorizon.expand.entities;

import arc.math.Mathf;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.game.Team;
import mindustry.gen.Groups;
import newhorizon.expand.block.special.BeaconBlock;
import newhorizon.expand.block.special.CommandableBlock;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;

public class NHGroups{
	public static final EntityGroup<CutsceneEventEntity> event = new EntityGroup<>(CutsceneEventEntity.class, false, true);
	public static final EntityGroup<AutoEventTrigger> autoEventTrigger = new EntityGroup<>(AutoEventTrigger.class, false, true);
	
	public static final ObjectMap<Integer, ObjectSet<RemoteCoreStorage.RemoteCoreStorageBuild>> placedRemoteCore = new ObjectMap<>(Team.all.length);
	public static final QuadTree<GravityTrapField> gravityTraps = new QuadTree<>(Vars.world.getQuadBounds(new Rect()));
	public static final ObjectSet<CommandableBlock.CommandableBlockBuild> commandableBuilds = new ObjectSet<>();
	public static final ObjectSet<JumpGate.JumpGateBuild> jumpGate = new ObjectSet<>();
	public static final Seq<BeaconBlock.BeaconBuild> beacon = new Seq<>();
	
	public static void clear(){
		event.clear();
		gravityTraps.clear();
		RemoteCoreStorage.clear();
		commandableBuilds.clear();
		autoEventTrigger.clear();
		jumpGate.clear();
		beacon.clear();
	}
	
	public static void update(){
		AutoEventTrigger.timeScale = AutoEventTrigger.getScale();
		if(Vars.headless)AutoEventTrigger.timeScale *= Mathf.curve(Groups.player.size(), 1.125f, 7.5f);
	}
}
