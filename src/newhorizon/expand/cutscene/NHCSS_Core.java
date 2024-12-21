package newhorizon.expand.cutscene;

import arc.Core;
import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.type.SectorPreset;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;
import newhorizon.NHVars;
import newhorizon.expand.cutscene.actions.CSSActions;
import newhorizon.expand.cutscene.stateoverride.UnitOverride;
import newhorizon.expand.eventsys.types.WorldEventType;

public class NHCSS_Core{
	public static final String WIN_KEY = "wingame";
	public static final String CUTSCENE_ID = "name";
	
	public static NHCSS_Core core = new NHCSS_Core();
	public static final float ACT_SPACING = 120;

	public NHCSS_Action.ActionBus mainBus;

	public Queue<NHCSS_Action.ActionBus> waitingBus = new Queue<>();
	public Seq<NHCSS_Action.ActionBus> subBuses = new Seq<>();
	
	public static ObjectMap<String, MapCutscene> cutscenes = new ObjectMap<>();
	public MapCutscene currentScene;
	
	public static ObjectSet<SectorPreset> skippingLanding = new ObjectSet<>();
	
	public static final Seq<Runnable> NULL_ACTIONS = new Seq<>(0);
	public Seq<Runnable> loadedUpdaters = NULL_ACTIONS;
	public Seq<Runnable> loadedIniters  = NULL_ACTIONS;
	public Seq<Runnable> loadedEnders   = NULL_ACTIONS;
	public float actReload = 0;
	
	public boolean loaded = false;
	
	public static void registerCutscene(String name, MapCutscene cutscene){
		cutscenes.put(name, cutscene);
	}
	
	public static void registerCutscene(MapCutscene cutscene){
		cutscenes.put(cutscene.mapName, cutscene);
	}
	
	static{
		Events.on(EventType.ResetEvent.class, e -> {
			core.loaded = false;
		});
		
		Events.on(EventType.SaveWriteEvent.class, e -> {
			if(core.mainBus != null && !core.mainBus.complete()){
				core.mainBus.skip();
			}
			
			if(!core.waitingBus.isEmpty()){
				core.waitingBus.each(NHCSS_Action.ActionBus::skip);
			}
		});
		
		Events.on(EventType.SectorLaunchLoadoutEvent.class, e -> {
			if(!e.sector.isCaptured() && e.sector.preset != null){
				if(skippingLanding.contains(e.sector.preset)){
					NHCSS_UI.skipCoreLanding();
					Core.app.post(NHCSS_UI::setSkippingLandingToDef);
				}
				
				if(cutscenes.containsKey(e.sector.preset.name)){
					load(e.sector.preset.name);
				}
			}
		});
		
		Events.on(EventType.WorldLoadEndEvent.class, e -> {
			WorldEventType.clearCustom();
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(core.loaded)return;
			
			Tile tileZero = Vars.world.tile(2, 2);
			
			String tagger = null;
			
			if(tileZero.build instanceof MessageBlock.MessageBuild){
				MessageBlock.MessageBuild build = (MessageBlock.MessageBuild)tileZero.build;
				String msg = build.message.toString();
				if(msg.startsWith("@CS-")){
					tagger = msg.replaceFirst("@CS-", "");
				}else if(msg.startsWith("@code:")){
					//Code support some day??
					String code = msg.replaceFirst("@code:", "");
				}
			}
			
			if(tagger != null)load(tagger);
			else core.loadCommon();
		});
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			if(core.loadedEnders != null)for(Runnable r : core.loadedEnders){
				r.run();
			}
			
			if(core.loadedUpdaters != null)core.loadedUpdaters.clear();
		});
	}
	
	public static void registerSkipping(SectorPreset preset){
		skippingLanding.add(preset);
	}
	
	public static boolean enabled(){
		return !Vars.state.isEditor() && !Vars.state.isMenu();
	}
	
	public static void load(String name){
		core.kill();
		core = new NHCSS_Core();
		NHVars.cutscene = core;
		
		if(cutscenes.containsKey(name)){
			MapCutscene currentScene = cutscenes.get(name);
			currentScene.core = core;
			
			Core.app.post(() -> {
				if(enabled())currentScene.load();
			});
			
			currentScene.register();
			
			core.currentScene = currentScene;
		}
		
		Core.app.post(() -> Core.app.post(() -> {
			if(enabled())for(Runnable r : core.loadedIniters){
				r.run();
			}
		}));
		
		if(core.loadedUpdaters == null)core.loadedUpdaters = NULL_ACTIONS;
		core.loaded = true;
	}
	
	public void loadCommon(){
		core.loaded = true;
		core.loadedUpdaters = core.loadedEnders = core.loadedIniters = NULL_ACTIONS;
		core.currentScene = null;
	}
	
	public void kill(){
		mainBus = null;
		waitingBus.clear();
		subBuses.clear();
		if(loadedUpdaters != null) loadedUpdaters.clear();
		NHCSS_UI.reset();
		UnitOverride.marked.clear();
	}
	
	public void applySubBus(NHCSS_Action... actions){
		NHCSS_Action.ActionBus bus = CSSActions.pack(actions);
		bus.current.setup();
		subBuses.add(bus);
	}
	
	public void applyMainBus(NHCSS_Action... actions){
		applyMainBus(CSSActions.pack(actions));
	}
	
	public void applyMainBus(NHCSS_Action.ActionBus bus){
		if(mainBus == null){
			mainBus = bus;
			mainBus.current.setup();
		}
		else waitingBus.addFirst(bus);
	}
	
	public void draw(){
		if(currentScene != null)currentScene.draw();
	}
	
	public void update(){
		if(mainBus != null){
			if(mainBus.complete())mainBus = null;
			else mainBus.update();
		}else if(!waitingBus.isEmpty()){
			actReload += Time.delta;
			if(actReload > ACT_SPACING){
				actReload = 0;
				mainBus = waitingBus.removeLast();
				mainBus.current.setup();
			}
		}
		
		for(int i = 0; i < subBuses.size; i++){
			NHCSS_Action.ActionBus bus = subBuses.get(i);
			if(bus.complete())subBuses.remove(i);
			bus.update();
		}
		
		NHCSS_UI.update();
		
		if(loadedUpdaters != null)for(Runnable r : loadedUpdaters){
			r.run();
		}
	}

}
