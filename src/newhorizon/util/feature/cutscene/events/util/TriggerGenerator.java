package newhorizon.util.feature.cutscene.events.util;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.UnitType;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHUnitTypes;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.func.OV_Pair;

public class TriggerGenerator{
	protected static Team team = Team.derelict;
	protected static CutsceneEvent event = CutsceneEvent.NULL_EVENT;
	protected static boolean directlyAdd = false;
	
	protected static Rand rand = new Rand();
	
	public static AutoEventTrigger random(int itemTypes, int maxItemAmount, int unitTypes, int maxUnitAmount){
		return random(itemTypes, maxItemAmount, unitTypes, maxUnitAmount, t -> {});
	}
	
	public static AutoEventTrigger random(int itemTypes, int maxItemAmount, int unitTypes, int maxUnitAmount, Cons<AutoEventTrigger> modifier){
		return handle(new AutoEventTrigger(team, event, t -> {
			Seq<Item> seq1 = Vars.content.items().copy().shuffle();
			t.items = new OV_Pair[itemTypes];
			for(int i = 0; i < itemTypes; i++)t.items[i] = new OV_Pair<>(seq1.pop(), rand.random(Mathf.clamp(0, Vars.state.teams.get(team).hasCore() ? Vars.state.teams.get(team).core().storageCapacity : 0, maxItemAmount)));
			
			Seq<UnitType> seq2 = Vars.content.units().copy().shuffle();
			t.units = new OV_Pair[itemTypes];
			for(int i = 0; i < itemTypes; i++)t.units[i] = new OV_Pair<>(seq2.pop(), rand.random(Mathf.clamp(0, Vars.state.teams.get(team).unitCap, maxUnitAmount)));
			
			modifier.get(t);
		}));
	}
	
	public static AutoEventTrigger Item_50SurgeAlloy(){
		return handle(new AutoEventTrigger(team, event, t -> t.items = OV_Pair.with(Items.surgeAlloy, 50)));
	}
	
	public static AutoEventTrigger Unit_8Gather(){
		return handle(new AutoEventTrigger(team, event, t -> t.units = OV_Pair.with(NHUnitTypes.gather, 8)));
	}
	
	public static AutoEventTrigger Block_1JuniorJumpGate(){
		return handle(new AutoEventTrigger(team, event, t -> t.buildings = OV_Pair.with(NHBlocks.jumpGateJunior, 1)));
	}
	
	public static AutoEventTrigger Block_2FusionGenerator(){
		return handle(new AutoEventTrigger(team, event, t -> t.buildings = OV_Pair.with(NHBlocks.fusionCollapser, 2)));
	}
	
	public static AutoEventTrigger Block_2HyperGenerator(){
		return handle(new AutoEventTrigger(team, event, t -> t.buildings = OV_Pair.with(NHBlocks.hyperGenerator, 2)));
	}
	
	public static AutoEventTrigger noneRequirement(){
		return handle(new AutoEventTrigger(team, event));
	}
	
	public static AutoEventTrigger Unit_4Destruction__Block_1SeniorJumpGate(){
		return handle(new AutoEventTrigger(team, event, t -> {
			t.buildings = OV_Pair.with(NHBlocks.jumpGate, 1);
			t.units = OV_Pair.with(NHUnitTypes.destruction, 4);
		}));
	}
	
	private static AutoEventTrigger handle(AutoEventTrigger trigger){
		if(directlyAdd)trigger.add();
		return trigger;
	}
	
	public static void set(boolean add, Team team, CutsceneEvent event){
		directlyAdd = add;
		TriggerGenerator.team = team;
		TriggerGenerator.event = event;
	}
	
	public static void setToDefault(CutsceneEvent event){
		directlyAdd = true;
		TriggerGenerator.team = Vars.state.rules.defaultTeam;
		TriggerGenerator.event = event;
	}
	
	public static void reset(){
		directlyAdd = false;
		TriggerGenerator.team = Team.derelict;
		TriggerGenerator.event = CutsceneEvent.NULL_EVENT;
	}
	
	public static boolean isDirectlyAdd(){
		return directlyAdd;
	}
	
	public static void setDirectlyAdd(boolean directlyAdd){
		TriggerGenerator.directlyAdd = directlyAdd;
	}
	
	public static Team getTeam(){
		return team;
	}
	
	public static void setTeam(Team team){
		TriggerGenerator.team = team;
	}
	
	public static CutsceneEvent getEvent(){
		return event;
	}
	
	public static void setEvent(CutsceneEvent event){
		TriggerGenerator.event = event;
	}
}
