package newhorizon.expand.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.feature.cutscene.Triggers;
import newhorizon.util.func.NHSetting;
import newhorizon.util.graphic.ShadowProcessor;
import newhorizon.util.ui.ScreenInterferencer;

import static mindustry.Vars.control;


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
	
	private static boolean connectCaution = false;
	
	public static final ObjectMap<Class<?>, Seq<Cons2<? extends Building, Tile>>> onTapActor = new ObjectMap<>();
	
	public static <T extends Building> void addActor(Class<T> type, Cons2<T, Tile> act){
		Seq<Cons2<? extends Building, Tile>> actions = onTapActor.get(type);
		if(actions == null){
			actions = new Seq<>();
			actions.add(act);
			onTapActor.put(type, actions);
		}else actions.add(act);
	}
	
	public static final Seq<Runnable> toDraw = new Seq<>();
	
	public static transient boolean raid_setup;
	
	public static void load(){
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		Events.run(Triggers.raid_setup, () -> {
			raid_setup = true;
		});
		
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.world.afterLoad();
			
			actAfterLoad.each(Runnable::run);
			actAfterLoad.clear();
		});
		
		Events.on(EventType.ResetEvent.class, e -> {
			actAfterLoad.clear();
			NHVars.reset();
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu){
				actAfterLoad.clear();
				NHVars.reset();
			}
		});
		
		if(Vars.headless)return;
		
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(connectCaution){
				connectCaution = false;
				Vars.ui.showCustomConfirm("@warning", kickWarn, "@settings", "@confirm", () -> new NHSetting.SettingDialog().show(), () -> {});
				Vars.player.con.close();
			}
		});

		Events.run(EventType.Trigger.update, () -> {
			ScreenInterferencer.update();
			NHSetting.update();
		});
		
		Events.on(ScreenInterferencer.ScreenHackEvent.class, e -> {
			ScreenInterferencer.generate(e.time);
		});
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			Vec2 vec2 = new Vec2().set(Vars.player);
			
			Building building = Vars.control.input.frag.config.getSelectedTile();
			
			if(NHSetting.alwaysShowGravityTrapFields || control.input.block instanceof GravityTrap || (building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper))){
				Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
					Vars.renderer.effectBuffer.begin(Color.clear);
					GravityTrapField.DRAWER.run();
					Vars.renderer.effectBuffer.end();
					Vars.renderer.effectBuffer.blit(NHShaders.gravityTrapShader);
				});
			}
			
			toDraw.each(Runnable::run);
			
			ShadowProcessor.post();
		});
		
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			if(!NHSetting.getBool("@active.override") && e.host.address.equals(NewHorizon.SERVER_ADDRESS)){
				connectCaution = true;
			}
			
			if(NewHorizon.DEBUGGING){
				Log.info(e.host.address);
				Log.info(e.host.port);
				Log.info(e.host.name);
				Log.info(e.host.description);
				Log.info(e.host.versionType);
			}
		});
		
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
	}
}
