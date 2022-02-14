package newhorizon.util;

import arc.Core;
import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import arc.util.Time;
import arc.util.serialization.Jval;
import arc.util.serialization.Jval.JsonArray;
import arc.util.serialization.Jval.JsonMap;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;
import newhorizon.util.func.OV_Pair;

/**
 *
 *
 *
 *
 *
 * */
public class Tool_JsonHandler{
	private static JsonMap initContext;
	
	
	public static final int TEAM_WAVE = -2, TEAM_DEFAULT = -1;
	public static final Prov<Team> DEFAULT = () -> Vars.state.rules.defaultTeam, ENEMY = () -> Vars.state.rules.waveTeam;
	
	public static final String
			ALL_SETTINGS = "nh-world-settings",
			JUMP_GATE_CHEAT_ENABLED = "jg-use-cheat-enabled",
			JUMP_GATE_USE_CORE_ITEMS = "jg-use-core-items",
	
			BEACON_CAPTURE_SCORE = "capture-score",
			BEACON_UNIT_FIELD = "enable-unit-capture",
			BEACON_ENABLE = "enable-beacon-capture",
			BEACON_FIELD_POLYGON = "use-polygon-field";
	
	public static final String
			KEY_TRIGGERS = "triggers", TRIGGER_NAME = "name", TRIGGER_OCCASION = "occasion",
		
			KEY_TRIGGER_WAVE = "wave",
			KEY_SPACING_BASE = "spacingBase", KEY_SPACING_RAND = "spacingRand",
			KEY_DISPOSABLE = "disposable",
		
			KEY_TEAM = "team",
			KEY_ITEMS = "items", CONTENT_ITEM = "content", CONTENT_AMOUNT = "amount",
			KEY_UNITS = "units",
			KEY_BLOCKS = "blocks",
			KEY_EVENT = "event",
			KEY_EVENT_PROV = "eventProv";
	
	public static void setContext(Jval jval){
		initContext = jval.asObject();
		if(NewHorizon.DEBUGGING){
			Log.info("Context Posted.");
			Core.app.post(() -> {
				if(initContext != null)throw new ArcRuntimeException("The context hasn't been rested!");
			});
		}
	}
	
	public static boolean getBool_Context(String key, boolean def){
		if(!initContext.containsKey(key))return def;
		else return initContext.get(key).asBool();
	}
	
	public static float getFloat_Context(String key, float def){
		if(!initContext.containsKey(key))return def;
		else return initContext.get(key).asFloat();
	}
	
	public static int getInt_Context(String key, int def){
		if(!initContext.containsKey(key))return def;
		else return initContext.get(key).asInt();
	}
	
	public static void initKey_Context(String key, Jval value){
		if(!initContext.containsKey(key))initContext.put(key, value);
	}
	
	public static void initKey_Context(String key, String value){
		if(!initContext.containsKey(key))initContext.put(key, Jval.valueOf(value));
	}
	
	public static void initKey_Context(String key, float value){
		if(!initContext.containsKey(key))initContext.put(key, Jval.valueOf(value));
	}
	
	public static void initKey_Context(String key, boolean value){
		if(!initContext.containsKey(key))initContext.put(key, Jval.valueOf(value));
	}
	
	public static void endContext(){
		initContext = null;
	}
	
	public static void initEventJval(Jval jval){
		JsonMap map = jval.asObject();
		
		setContext(jval);
		initKey_Context(KEY_TEAM, Jval.valueOf(-1));
		initKey_Context(KEY_ITEMS, Jval.newObject());
		initKey_Context(KEY_UNITS, Jval.newObject());
		initKey_Context(KEY_BLOCKS, Jval.newObject());
		initKey_Context(KEY_EVENT, "");
		initKey_Context(KEY_EVENT_PROV, "");
		initKey_Context(KEY_TRIGGER_WAVE, 10);
		initKey_Context(KEY_SPACING_BASE, 2 * Time.toMinutes);
		initKey_Context(KEY_SPACING_RAND, 2 * Time.toMinutes);
		initKey_Context(KEY_DISPOSABLE, false);
		endContext();
	}
	
	public static JsonArray triggers(Jval root){
		return triggersJval(root).asArray();
	}
	
	public static Jval triggersJval(Jval root){
		return root.get(KEY_TRIGGERS);
	}
	
	public static void addTrigger(String name, AutoEventTrigger trigger, Seq<Jval> seq){
		Jval map = Jval.newObject();
		map.put(TRIGGER_NAME, name);
		map.put(TRIGGER_OCCASION, writeTrigger(trigger, Jval.newObject()));
		
		seq.add(map);
	}
	
	public static ObjectMap<String, AutoEventTrigger> generators(Jval root){
		ObjectMap<String, AutoEventTrigger> gs = new ObjectMap<>();
		
		for(Jval j : triggers(root)){
			JsonMap map = j.asObject();
			gs.put(map.get(TRIGGER_NAME).asString(), readTrigger(map.get(TRIGGER_OCCASION)));
		}
		
		return gs;
	}
	
	public static Jval writeTrigger(AutoEventTrigger trigger, Jval jval){
		initEventJval(jval);
		
		Jval items = Jval.newArray();
		for(OV_Pair<Item> content : trigger.items){
			Jval map = Jval.newObject();
			map.put(CONTENT_ITEM, Jval.valueOf(content.item.name));
			map.put(CONTENT_AMOUNT, Jval.valueOf(content.value));
			items.add(map);
		}
		
		Jval units = Jval.newArray();
		for(OV_Pair<UnitType> content : trigger.units){
			Jval map = Jval.newObject();
			map.put(CONTENT_ITEM, Jval.valueOf(content.item.name));
			map.put(CONTENT_AMOUNT, Jval.valueOf(content.value));
			units.add(map);
		}
		
		Jval blocks = Jval.newArray();
		for(OV_Pair<Block> content : trigger.buildings){
			Jval map = Jval.newObject();
			map.put(CONTENT_ITEM, Jval.valueOf(content.item.name));
			map.put(CONTENT_AMOUNT, Jval.valueOf(content.value));
			blocks.add(map);
		}
		
		jval.put(KEY_ITEMS, items);
		jval.put(KEY_UNITS, units);
		jval.put(KEY_BLOCKS, blocks);
		
		jval.put(KEY_SPACING_BASE, trigger.spacingBase);
		jval.put(KEY_SPACING_RAND, trigger.spacingRand);
		jval.put(KEY_TRIGGER_WAVE, trigger.minTriggerWave);
		jval.put(KEY_DISPOSABLE, trigger.disposable);
		
		if(trigger.team == null)jval.put(KEY_TEAM, TEAM_DEFAULT);
		else jval.put(KEY_TEAM, trigger.team.id);
		
		if(trigger.eventProv != null)jval.put(KEY_EVENT_PROV, trigger.eventProv);
		jval.put(KEY_EVENT, trigger.eventType.name);
		
		return jval;
	}
	
	public static AutoEventTrigger readTrigger(Jval jval){
		initEventJval(jval);
		
		AutoEventTrigger trigger = new AutoEventTrigger();
		JsonMap map = jval.asObject();
		
		int teamID = map.get(KEY_TEAM).asInt();
		if(teamID < 0){
			switch(teamID){
				case TEAM_DEFAULT : trigger.teamProv = () -> Vars.state.rules.defaultTeam; break;
				case TEAM_WAVE : trigger.teamProv = () -> Vars.state.rules.waveTeam; break;
				default : throw new ArcRuntimeException("Wrong Team ID");
			}
		}else{
			trigger.team = Team.get(teamID);
		}
		
		String eventName = map.get(KEY_EVENT).asString();
		trigger.eventProv = map.get(KEY_EVENT_PROV).asString();
		if(trigger.eventProv != null && !trigger.eventProv.isEmpty()){
			trigger.eventType = CutsceneEvent.construct(trigger.eventProv);
		}
		
		trigger.eventType = CutsceneEvent.get(eventName);
		if(trigger.eventType == null)throw new IllegalArgumentException("Wrong Event Name or Wrong Constructor!");
		
		JsonArray items = map.get(KEY_ITEMS).asArray();
		Seq<OV_Pair<Item>> itemsSeq = new Seq<>(items.size);
		for(Jval obj : items){
			JsonMap i = obj.asObject();
			Item c = Vars.content.getByName(ContentType.item, i.get(CONTENT_ITEM).asString());
			if(c == null)continue;
			itemsSeq.add(new OV_Pair<>(c, Integer.parseInt(i.get(CONTENT_AMOUNT).asString())));
		}
		
		JsonArray units = map.get(KEY_UNITS).asArray();
		Seq<OV_Pair<UnitType>> unitsSeq = new Seq<>(units.size);
		for(Jval obj : units){
			JsonMap i = obj.asObject();
			UnitType c = Vars.content.getByName(ContentType.unit, i.get(CONTENT_ITEM).asString());
			if(c == null)continue;
			unitsSeq.add(new OV_Pair<>(c, Integer.parseInt(i.get(CONTENT_AMOUNT).asString())));
		}
		
		JsonArray blocks = map.get(KEY_BLOCKS).asArray();
		Seq<OV_Pair<Block>> blocksSeq = new Seq<>(blocks.size);
		for(Jval obj : blocks){
			JsonMap i = obj.asObject();
			Block c = Vars.content.getByName(ContentType.block, i.get(CONTENT_ITEM).asString());
			if(c == null)continue;
			blocksSeq.add(new OV_Pair<>(c, Integer.parseInt(i.get(CONTENT_AMOUNT).asString())));
		}
		
		trigger.items = itemsSeq;
		trigger.units = unitsSeq;
		trigger.buildings = blocksSeq;
		
		trigger.spacingBase = map.get(KEY_SPACING_BASE).asFloat();
		trigger.spacingRand = map.get(KEY_SPACING_RAND).asFloat();
		trigger.minTriggerWave = map.get(KEY_TRIGGER_WAVE).asInt();
		trigger.disposable = map.get(KEY_DISPOSABLE).asBool();
		
		return trigger;
	}
}
