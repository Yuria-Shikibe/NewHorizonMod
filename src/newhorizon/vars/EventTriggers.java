package newhorizon.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NewHorizon;
import newhorizon.content.NHStatusEffects;
import newhorizon.func.NHSetting;
import newhorizon.func.SettingDialog;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.interfaces.ServerInitc;


public class EventTriggers{
	public static class BossGeneratedEvent{
		public final Unit unit;
		
		public BossGeneratedEvent(Unit unit){
			this.unit = unit;
		}
	}
	
	private static String kickWarn;
	
	public static Block[] contents;
	
	private static boolean server = false;
	private static boolean caution = false;
	
	public static final ObjectMap<Class<?>, Seq<Cons2<? extends Building, Tile>>> onTapActor = new ObjectMap<>();
	
	public static <E extends Building> void addActor(Class<E> type, Cons2<E, Tile> act){
		Seq<Cons2<? extends Building, Tile>> actions = onTapActor.get(type);
		if(actions == null){
			actions = new Seq<>();
			actions.add(act);
			onTapActor.put(type, actions);
		}else actions.add(act);
	}
	
	
	public static void load(){
		if(Vars.headless)return;
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		contents = new Block[]{
			Blocks.itemSource, Blocks.liquidSource, Blocks.powerSource
		};
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.reset();
			
			for(BeforeLoadc c : NHWorldVars.advancedLoad){
				c.beforeLoad();
			}
			NHWorldVars.advancedLoad.clear();
			NHVars.world.clearLast();
			NHVars.world.worldLoaded = true;
			
			if(Vars.player.admin)for(Block c : contents)c.buildVisibility = BuildVisibility.shown;
			else for(Block c : contents)c.buildVisibility = BuildVisibility.sandboxOnly;
			
			if(caution){
				caution = false;
				Vars.ui.showCustomConfirm("@warning", kickWarn, "@settings", "@confirm", () -> new SettingDialog().show(), () -> {});
				Vars.player.con.kick(kickWarn, 1);
			}
		});
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			if(!NHSetting.getBool("@active.override") && e.host.name.equals(NewHorizon.SERVER_AUZ_NAME)){
				caution = true;
			}
			
			for(ServerInitc c : NHWorldVars.serverLoad){
				c.loadAfterConnect();
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
