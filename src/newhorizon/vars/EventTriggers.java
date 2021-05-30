package newhorizon.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NewHorizon;
import newhorizon.content.NHStatusEffects;
import newhorizon.func.NHSetting;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.interfaces.ServerInitc;


public class EventTriggers{
	public static class BossGeneratedEvent{
		public final Unit unit;
		
		public BossGeneratedEvent(Unit unit){
			this.unit = unit;
		}
	}
	
	public static Block[] contents;
	
	private static boolean server = false;
	
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
		contents = new Block[]{
			Blocks.itemSource, Blocks.liquidSource, Blocks.powerSource
		};
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.reset();
			
			for(Tile tile : Vars.world.tiles)NHVars.world.intercepted.put(tile, new IntSeq(new int[Team.all.length]));
			
			for(BeforeLoadc c : NHVars.world.advancedLoad){
				c.beforeLoad();
			}
			
			NHVars.world.clearLast();
			NHVars.world.worldLoaded = true;
			
			if(Vars.player.admin){
				for(Block c : contents){
					c.buildVisibility = BuildVisibility.shown;
				}
			}else{
				for(Block c : contents){
					c.buildVisibility = BuildVisibility.sandboxOnly;
				}
			}
		});
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			NHSetting.log("Server Preload Run");
			
			if(!NHSetting.getBool("@active.override") && e.host.address.equals(NewHorizon.SERVER_ADDRESS
			)){
				Vars.net.disconnect();
				Vars.ui.showStartupInfo(Core.bundle.get("mod.ui.requite.need-override"));
				return;
			}
			
			for(ServerInitc c : NHVars.world.serverLoad){
				c.loadAfterConnect();
			}
		});
		
		Events.on(EventType.UnitChangeEvent.class, e -> e.unit.apply(NHStatusEffects.invincible, 180f));
		
		Events.on(EventType.TapEvent.class, e -> {
			if(Vars.headless)return;
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
