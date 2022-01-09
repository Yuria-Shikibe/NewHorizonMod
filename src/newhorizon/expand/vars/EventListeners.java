package newhorizon.expand.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.content.NHShaders;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.util.feature.ScreenInterferencer;
import newhorizon.util.feature.cutscene.Triggers;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHSetting;
import newhorizon.util.ui.UnitInfo;


public class EventListeners{
	public static class BossGeneratedEvent{
		public final Unit unit;
		
		public BossGeneratedEvent(Unit unit){
			this.unit = unit;
		}
	}
	
	public static final Seq<Runnable> actAfterLoad = new Seq<>();
	
	public static Interval timer = new Interval(6);
	
	private static String kickWarn;
	
	private static boolean caution = false;
	
	public static final ObjectMap<Class<?>, Seq<Cons2<? extends Building, Tile>>> onTapActor = new ObjectMap<>();
	
	public static <T extends Building> void addActor(Class<T> type, Cons2<T, Tile> act){
		Seq<Cons2<? extends Building, Tile>> actions = onTapActor.get(type);
		if(actions == null){
			actions = new Seq<>();
			actions.add(act);
			onTapActor.put(type, actions);
		}else actions.add(act);
	}
	
	
	public static transient boolean raid_setup;
	
	public static void load(){
		Events.run(Triggers.raid_setup, () -> {
			raid_setup = true;
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.world.worldLoaded = true;
			
			NHVars.world.afterLoad();
			
			actAfterLoad.each(Runnable::run);
			actAfterLoad.clear();
		});
		
		Events.on(EventType.ResetEvent.class, e -> {
			actAfterLoad.clear();
			NHVars.reset();
			RemoteCoreStorage.clear();
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu){
				NHVars.reset();
				
				actAfterLoad.clear();
				RemoteCoreStorage.clear();
				
				NHVars.world.worldLoaded = false;
			}
		});
		
		if(Vars.headless)return;
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(caution){
				caution = false;
				Vars.ui.showCustomConfirm("@warning", kickWarn, "@settings", "@confirm", () -> new NHSetting.SettingDialog().show(), () -> {});
				Vars.player.con.kick(kickWarn, 1);
			}
			
			UnitInfo.added.clear();
//			UnitInfo.addBars();
		});

		Events.run(EventType.Trigger.update, () -> {
			ScreenInterferencer.update();
			NHSetting.update();
//			UnitInfo.update();
		});
		
		Events.on(ScreenInterferencer.ScreenHackEvent.class, e -> {
			ScreenInterferencer.generate(e.time);
		});
		
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			Draw.drawRange(Layer.light + 5, 1, () -> Vars.renderer.effectBuffer.begin(Color.clear), () -> {
				Vars.renderer.effectBuffer.end();
				Vars.renderer.effectBuffer.blit(NHShaders.gravityTrapShader);
			});
			
			Building building = Vars.control.input.frag.config.getSelectedTile();
			
			if(building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper)){
				Seq<GravityTrap.TrapField> bi = NHFunc.getObjects(NHVars.world.gravityTraps);
				
				Draw.z(Layer.overlayUI + 0.1f);
				
				Draw.reset();
				
//				Draw.blend(Blending.additive);
				Draw.z(Layer.light + 5);
				for(GravityTrap.TrapField i : bi){
					i.draw();
				}
				Draw.reset();
//				Draw.blend();
			}
		});
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			if(!NHSetting.getBool("@active.override") && e.host.name.equals(NewHorizon.SERVER_AUZ_NAME)){
				caution = true;
			}
		});
		
		Events.on(EventType.UnitChangeEvent.class, e -> e.unit.apply(NHStatusEffects.invincible, 180f));
		
		Events.on(EventType.TapEvent.class, e -> {
			Building selecting = Vars.control.input.frag.config.getSelectedTile();
			if(selecting != null)for(Class<?> type : onTapActor.keys()){
				if(type == selecting.getClass()){
					for(Cons2 actor : onTapActor.get(type)){
						actor.get(selecting, e.tile);
					}
				}
			}
		});
		
//		Events.on(EventType.ClientPreConnectEvent.class, e -> {
//			server = true;
//			if(Vars.headless)return;
//		});
//
//		Events.on(EventType.StateChangeEvent.class, e -> {
//			if(server){
//				server = false;
//				for(Block c : contents){
//					c.buildVisibility = BuildVisibility.sandboxOnly;
//				}
//			}
//			if(Vars.headless)return;
//		});
		
//		Events.on(EventType.StateChangeEvent.class, e -> {
//			NHSetting.log("Event", "Server Preload Run");
//
//			if(NHWorldVars.worldLoaded){
//				NHSetting.log("Event", "Leaving World");
//				NHWorldVars.worldLoaded= false;
//			}
//		});
	}
}
