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
import mindustry.gen.Building;
import mindustry.type.SectorPreset;
import newhorizon.expand.NHVars;
import newhorizon.expand.cutscene.actions.CSSActions;

public class NHCSS_Core{
	public static final String WIN_KEY = "wingame";
	public static final String CUTSCENE_ID = "name";
	
	public static NHCSS_Core core = new NHCSS_Core();
	public static final float ACT_SPACING = 120;
	
	public static void loadRegisters(){
	
	}
	
	public Queue<NHCSS_Action.ActionBus> waitingBus = new Queue<>();
	public NHCSS_Action.ActionBus mainBus;
	public Seq<NHCSS_Action.ActionBus> subBuses = new Seq<>();
	
	public static ObjectMap<String, Seq<Runnable>> updaters = new ObjectMap<>();
	public static ObjectMap<String, Seq<Runnable>> initers = new ObjectMap<>();
	public static ObjectMap<String, Seq<Runnable>> enders = new ObjectMap<>();
	
	public static ObjectSet<SectorPreset> skippingLanding = new ObjectSet<>();
	
	public static final Seq<Runnable> NULL_ACTIONS = new Seq<>(0);
	public Seq<Runnable> loadedActions = NULL_ACTIONS;
	public float actReload = 0;
	
	public static void register(ObjectMap<String, Seq<Runnable>> set, Seq<Runnable> runs, String... names){
		for(String t : names){
			set.put(t, runs);
		}
	}
	
	static{
		Events.on(EventType.SaveWriteEvent.class, e -> {
			if(core.mainBus != null && !core.mainBus.complete()){
				core.mainBus.skip();
			}
			
			if(!core.waitingBus.isEmpty()){
				core.waitingBus.each(NHCSS_Action.ActionBus::skip);
			}
		});
		
		Events.on(EventType.SectorLaunchEvent.class, e -> {
			if(e.sector.preset != null){
				if(skippingLanding.contains(e.sector.preset)){
					NHCSS_UI.skipCoreLanding();
					Core.app.post(NHCSS_UI::setSkippingLandingToDef);
				}
			}
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			core.kill();
			core = new NHCSS_Core();
			NHVars.cutscene = core;
			
			if(!Vars.state.isEditor()){
			
//				Vars.state.map.tags.put("cutsceneID", "new-horizon-hostile-research-station")
//
//				NewHorizon.debugLog("test:" + initers.containsKey(Vars.state.map.name()));
//				NewHorizon.debugLog(Vars.state.map.name());
				Core.app.post(() -> {
					if(updaters.containsKey(Vars.state.map.tag(CUTSCENE_ID))){
						core.loadedActions = updaters.get(Vars.state.map.tag(CUTSCENE_ID)).copy();
					}
					
					if(initers.containsKey(Vars.state.map.tag(CUTSCENE_ID)))for(Runnable r : initers.get(Vars.state.map.tag(CUTSCENE_ID))){
						r.run();
					}
				});
			}
			
			if(core.loadedActions == null)core.loadedActions = NULL_ACTIONS;
		});
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			if(e.sector.preset != null && Vars.state.getSector() == e.sector && enders.containsKey(Vars.state.map.tag(CUTSCENE_ID)))for(Runnable r : enders.get(Vars.state.map.tag(CUTSCENE_ID))){
				r.run();
			}
			
			if(core.loadedActions != null)core.loadedActions.clear();
		});
	}
	
	public static void registerSkipping(SectorPreset preset){
		skippingLanding.add(preset);
	}
	
	public void kill(){
		mainBus = null;
		waitingBus.clear();
		subBuses.clear();
		if(loadedActions != null)loadedActions.clear();
		NHCSS_UI.reset();
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
//		NHCSS_UI.draw();
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
		
		for(Runnable r : loadedActions){
			r.run();
		}
	}
	
	public enum Trigger{
		enemyCoreDestruction;
		
		Building monitorTarget;
		int wave;
	}
}
