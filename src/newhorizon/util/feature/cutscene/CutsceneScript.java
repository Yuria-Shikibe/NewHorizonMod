package newhorizon.util.feature.cutscene;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Prov;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.editor.MapEditorDialog;
import mindustry.game.EventType;
import mindustry.game.Saves;
import mindustry.gen.Building;
import mindustry.maps.Map;
import mindustry.mod.Mods;
import mindustry.net.Net;
import mindustry.type.SectorPreset;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.expand.entities.NHGroups;
import newhorizon.util.feature.cutscene.packets.EventCompletePacket;
import newhorizon.util.feature.cutscene.packets.EventUICallPacket;
import newhorizon.util.feature.cutscene.packets.TagPacket;
import newhorizon.util.ui.CutsceneMenu;
import newhorizon.util.ui.TableFunc;

import java.io.IOException;
import java.lang.reflect.Field;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

/**
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * */
@SuppressWarnings({"CodeBlock2Expr", "SpellCheckingInspection"})
public class CutsceneScript{
	public static final String CCS_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod/wiki/Cutscene-Script-Custom-Guide";
	/**
	 * Each {@link SectorPreset} can has multiple actor.
	 * <p></p>
	 * {@code updaters} Maps the actions during playing.<p>
	 * {@code initer} Maps the actions after the world is loaded.<p>
	 * {@code ender} Maps the actions after the game is over.<p>
	 * <p></p>
	 * {@code curSectorPreset} useless now.
	 * <p></p>
	 * {@code initHasRun} Used to avoid repeating loading.
	 *
	 *
	 * @author Yuria
	 */
	public static Fi scriptDirectory;
	public static Fi currentScriptFile;
	
	public static Mods.LoadedMod mod;
	
	public static CCS_Scripts scripts;
	
	public static final String CUTSCENE_KEY = "custom-cutscene-script";
	public static final String CUSTOME_EVENTS_KEY = "custom-event-json";
	
	public static final ObjectMap<SectorPreset, Fi> presentJS = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Runnable>> updaters = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Runnable>> initer = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Cons<Boolean>>> ender = new ObjectMap<>(6); // true -> win, false -> lose
	
	public static @Nullable SectorPreset curSectorPreset = null;
	
	/**
	 * {@code curUpdater} Used to storage movements that is acted every update(Do not run during pause).
	 * {@code curIniter} Used to storage movements that is acted when the world is loaded.
	 * {@code curEnder} Used to stroage movements that is acted when game over. Param `Boolean`: true -> win; false -> lose.
	 * */
	
	public static final Seq<Runnable> curUpdater = new Seq<>(), curIniter = new Seq<>();
	public static final Seq<Cons<Boolean>> curEnder = new Seq<>();
	public static final Seq<Runnable> taskUpdater = new Seq<>();
	
	/** Used to storage movements that will be called when a specific type of block is destroyed*/
	public static final ObjectMap<Block, Cons<Building>> blockDestroyListener = new ObjectMap<>();
	
	public static boolean isPlayingCutscene = false;
	
	public static void addListener(Block type, Cons<Building> actor){
		blockDestroyListener.put(type, actor);
	}
	
	public static void addListener(Seq<Block> types, Cons<Building> actor){for(Block type : types)addListener(type, actor);}
	
	public static Interval timer = new Interval(6);
	
	protected static Interval packetTimer = new Interval();
	
	protected static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();
	
	protected static long latestTime = 0;
	
	protected static float autosaveReload = 0;
	
	public static Field saveReloadField;
	
	public static String scriptDirectoryPath(){
		return Vars.customMapDirectory + "/custom-cutscene";
	}
	
	public static void load(){
		try{
			saveReloadField = Saves.class.getDeclaredField("time");
			saveReloadField.setAccessible(true);
		}catch(NoSuchFieldException e){
			e.printStackTrace();
		}
		
		Net.registerPacket(TagPacket::new);
		Net.registerPacket(EventCompletePacket::new);
		Net.registerPacket(EventUICallPacket::new);
		
		mod = NewHorizon.MOD;
		
		Events.on(EventType.SaveLoadEvent.class, e -> {
			CutsceneEventEntity.afterIO();
		});
		
		Events.on(EventType.SaveWriteEvent.class, e -> {
			CutsceneEventEntity.afterIO();
			UIActions.skip();
		});
		
		Events.on(EventType.BlockDestroyEvent.class, e -> {
			if(e.tile.build == null || !blockDestroyListener.containsKey(e.tile.build.block()))return;
			
			blockDestroyListener.get(e.tile.build.block).get(e.tile.build);
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu)UIActions.lockInput = false;
			if((e.from == GameState.State.playing && e.to == GameState.State.menu)){
				exit();
				reset();
			}
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			init();
		});
		
		Events.run(EventType.Trigger.update, () -> {
			if(!state.isMenu())taskUpdater.each(Runnable::run);
			if(!Vars.state.isPlaying())return;
			if(packetTimer.get(0, 900))handleTagData();
			if(curUpdater.any())curUpdater.each(Runnable::run);
			UIActions.actor.act(Core.graphics.getDeltaTime());
			UIActions.multiActor.act(Core.graphics.getDeltaTime());
		});
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			if(e.sector == state.getSector()){
				curEnder.each(c -> c.get(true));
				NHGroups.event.each(et -> et.eventType.removeAfterVictory, CutsceneEventEntity::remove);
			}
		});
		
		Events.on(EventType.SectorLoseEvent.class, e -> {
			if(e.sector == state.getSector()){
				curEnder.each(c -> c.get(false));
				NHGroups.event.clear();
			}
		});
		
		Events.on(EventType.ClientLoadEvent.class, e -> {
			try{
				BaseDialog menu;
				Field field = MapEditorDialog.class.getDeclaredField("menu");
				field.setAccessible(true);
				menu = (BaseDialog)field.get(ui.editor);
				
				menu.cont.row().button("@mod.ui.cutscene-menu", new TextureRegionDrawable(NHContent.objective), LEN - OFFSET, () -> {
					new CutsceneMenu().show();
				}).size(180f * 2 + 10f, 60f);
			}catch(IllegalAccessException | NoSuchFieldException ex){
				ui.showErrorMessage(ex.toString());
			}
			
			control.input.addLock(() -> UIActions.lockInput);
			
			UIActions.extendX = Core.settings.getFloat("nh-ccs-extendx", 0);
			UIActions.extendY = Core.settings.getFloat("nh-ccs-extendy", 0);
		});
		
		if(!headless){
			initDirectory();
			
			Events.on(EventType.SaveWriteEvent.class, e -> {
				Core.settings.put("nh-ccs-extendx", UIActions.extendX);
				Core.settings.put("nh-ccs-extendy", UIActions.extendY);
			});
		}
	}
	
	protected static void initDirectory(){
		scriptDirectory = new Fi(scriptDirectoryPath());
		if (!scriptDirectory.exists()) {
			scriptDirectory.mkdirs();
		}
	}
	
	public static boolean hasScript(){
		if(state.getSector() != null && state.getSector().preset != null){
			SectorPreset sp = state.getSector().preset;
			return initer.containsKey(sp) || updaters.containsKey(sp) || ender.containsKey(sp) || presentJS.containsKey(sp);
		}else if(state.map != null){
			return state.map.hasTag(CUTSCENE_KEY) || (state.map.name().contains("(@HC)") && !headless && scriptDirectory.child(state.map.name() + "-cutscene.js").exists());
		}else return false;
	}
	
	public static String getScript(){
		return getScript(state.map);
	}
	
	public static String getScript(Map map){
		if(curSectorPreset != null && presentJS.containsKey(curSectorPreset))return presentJS.get(curSectorPreset).readString();
		
		if(map.tags.containsKey(CUTSCENE_KEY)){
			return map.tag(CUTSCENE_KEY);
		}else if(map.name().contains("(@HC)") && !headless){
			currentScriptFile = scriptDirectory.child(map.name() + "-cutscene.js");
			if(!currentScriptFile.exists()){
				if(map.tags.containsKey(CUTSCENE_KEY)){
					return map.tag(CUTSCENE_KEY);
				}else try{
					Log.info("Tried Create Script File: " + currentScriptFile.file().createNewFile() + " | " + currentScriptFile.file().getAbsolutePath());
				}catch(IOException e){
					Vars.ui.showErrorMessage(e.toString());
				}
			}
			String code = currentScriptFile.readString();
			TableFunc.textArea.setText(code);
			return code;
		}else return "";
	}
	
	protected static void reset(){
		curSectorPreset = null;
		currentScriptFile = null;
		
		curEnder.clear();
		curIniter.clear();
		curUpdater.clear();
		timer.clear();
		blockDestroyListener.clear();
		UIActions.waitingPool.clear();
		UIActions.multiActor.clearChildren();
		taskUpdater.clear();
		
		if(!Vars.headless)TableFunc.textArea.clearText();
		
		isPlayingCutscene = false;
	}
	
	public static Fi getModGlobalJS(){
		return mod.root.child("scripts").child("cutsceneLoader.js");
	}
	
	public static String getModGlobalJSCode(){
		return getModGlobalJS().readString();
	}
	
	/**
	 * Run js codes.
	 *
	 * */
	public static void runJS(String js){
		if(js == null || js.isEmpty())return;
		
		scripts.run(mod, js);
	}
	
	
	protected static void init(){
		if(scripts == null)scripts = new CCS_Scripts();
		
		reset();
		
		SectorPreset sector = Vars.state.getSector() == null ? null : Vars.state.getSector().preset;
		
		UIActions.init();
		
		curSectorPreset = sector;
		
		if(state.map.hasTag(CUSTOME_EVENTS_KEY)){
			try{
				runEventOnce("SetUpTriggers", () -> {
					CCS_JsonHandler.generators(Jval.read(state.map.tags.get(CUSTOME_EVENTS_KEY))).each((n, t) -> {
						t.add();
					});
				});
			}catch(Exception e){
				Log.err(e);
			}
		}
		
		if(sector != null){
			if(updaters.containsKey(sector)){
				curUpdater.addAll(updaters.get(sector));
			}
			
			if(initer.containsKey(sector)){
				curIniter.addAll(initer.get(sector));
			}
			
			if(ender.containsKey(sector)){
				curEnder.addAll(ender.get(sector));
			}
			
			if(presentJS.containsKey(sector)){
				Core.app.post(() -> {
					runJS(presentJS.get(sector).readString());
					curIniter.each(Runnable::run);
				});
			}else curIniter.each(Runnable::run);
		}else{
			Core.app.post(() -> {
				runJS(getScript(Vars.state.map));
				curIniter.each(Runnable::run);
			});
		}
	}
	
	protected static void exit(){
		UIActions.reset();
		CutsceneEvent.cutsceneEvents.each((s, e) -> {
			if(!e.cannotBeRemove)CutsceneEvent.cutsceneEvents.remove(s);
		});
	}
	
	public static boolean canInit(){
		boolean b = !state.rules.tags.containsKey("inited") || !Boolean.parseBoolean(state.rules.tags.get("inited"));
		state.rules.tags.put("inited", "true");
		return b;
	}
	
	public static boolean eventHasData(String key){
		return state.rules.tags.containsKey(key);
	}
	
	public static void run(String key, Boolf<String> boolf, Runnable run){
		if(state.rules.tags.containsKey(key) && boolf.get(state.rules.tags.get(key))){
			run.run();
		}
	}
	
	public static boolean getBool(String key){
		return state.rules.tags.containsKey(key) && Boolean.parseBoolean(state.rules.tags.get(key));
	}
	
	public static float getFloat(String key){
		return Float.parseFloat(state.rules.tags.get(key));
	}
	
	public static float getFloatOrNaN(String key){
		float f = Float.NaN;
		try{
			f = Float.parseFloat(state.rules.tags.get(key));
		}catch(Exception ignore){
		
		}
		return f;
	}
	
	/**
	 * Run an event that only happens once.
	 *
	 * */
	@SuppressWarnings("UnusedReturnValue")
	public static boolean runEventOnce(String eventName, Runnable run){
		boolean hasRun = false;
		if(!state.rules.tags.containsKey(eventName) || !Boolean.parseBoolean(state.rules.tags.get(eventName))){
			run.run();
			hasRun = true;
		}
		
		state.rules.tags.put(eventName, "true");
		
		return hasRun;
	}
	
	/**
	 * Run an event that can happens times.
	 *
	 * */
	public static boolean runEventMulti(String eventName, int maxTimes, Runnable run){
		int num = 0;
		
		boolean hasRun = false;
		
		if(state.rules.tags.containsKey(eventName)){
			num = Integer.parseInt(state.rules.tags.get(eventName));
			if(num < maxTimes){
				run.run();
				hasRun = true;
			}
		}
		
		state.rules.tags.put(eventName, String.valueOf(++num));
		
		return hasRun;
	}
	
	/**
	 * ProvSet the progress of a certain event.
	 * @param time Use tick format
	 * */
	public static void setReload(String eventName, float time){
		state.rules.tags.put(eventName, String.valueOf(time));
	}
	
	/**
	 * ProvSet the total reload time of a certain event.
	 *
	 * @param reloadTime Use tick format
	 * @param speed Usually {@link Time}{@code .delta} or its multiple.
	 * @param exist If false, the data of the event will be removed.
	 * @param canContinue Used to check whether the progress of the event should continue.
	 * @param run Actions
	 *
	 * @see Prov
	 * @see Runnable
	 *
	 * */
	public static void reload(String eventName, float speed, float reloadTime, Prov<Boolean> exist, Prov<Boolean> canContinue, Runnable run){
		if(exist.get()){
			if(canContinue.get()){
				if(state.rules.tags.containsKey(eventName)){
					float time = Float.parseFloat(state.rules.tags.get(eventName));
					time += speed;
					if(time > reloadTime){
						setReload(eventName, 0);
						run.run();
					}else setReload(eventName, time);
				}else setReload(eventName, 0);
			}
		}else state.rules.tags.remove(eventName);
	}
	
	public static void handleTagData(){
		if(state.rules.tags == null || state.rules.tags.isEmpty())return;
		
		if(net.server()){
			TagPacket packet = new TagPacket();
			packet.tags = state.rules.tags;
			Vars.net.send(packet, true);
		}
	}
}
