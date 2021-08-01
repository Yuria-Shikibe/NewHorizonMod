package newhorizon.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.block.defence.GravityTrap;
import newhorizon.block.defence.HyperSpaceWarper;
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
	
	public static Seq<Block> banned = new Seq<>();
	
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
	
	
	public static void load(){
//		banned.addAll(Blocks.itemSource, Blocks.powerSource, Blocks.liquidSource, Blocks.payloadSource, Blocks.router);
//
//		Events.on(EventType.BlockBuildEndEvent.class, e -> {
//			Building b = e.tile.build;
//			if(b == null || e.breaking || e.unit == null || !e.unit.isPlayer() || Vars.state.rules.infiniteResources || !banned.contains(b.block))return;
//			Player player = (Player)e.unit.controller();
//			if(player.admin)return;
//			Log.info("Triggered Cheat");
//			Call.kick(player.con, "You Just Cheated!");
//			b.kill();
//			b.remove();
//			if(Vars.net.client()){
//				try{
//					Method method = NetClient.class.getDeclaredMethod("sync");
//					method.setAccessible(true);
//					method.invoke(Vars.netClient);
//				}catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ex){
//					ex.printStackTrace();
//				}
//			}
//		});
//
		if(Vars.headless)return;
		
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			float scl = 20;
			Building building = Vars.control.input.frag.config.getSelectedTile();
			
			float z = Draw.z();
			
			
			if(building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper)){
				for(GravityTrap.GravityTrapBuild b : NHVars.world.gravityTraps){
					if(!b.active())return;
					Draw.z(Layer.buildBeam + Mathf.num(b.team != Vars.player.team() ^ ((Time.time % (scl * 8 * Mathf.pi)) > scl * Mathf.pi && (Time.time % (scl * 8 * Mathf.pi)) < scl * Mathf.pi * 5)));
					Color c = b.team == Vars.player.team() ? Pal.lancerLaser : Pal.redderDust;
					Tmp.c1.set(c).lerp(Color.white, Mathf.absin(scl, 1f));
					Draw.color(Tmp.c1);
					Fill.poly(b.x, b.y,6, b.range());
					Drawf.light(b.x, b.y, b.range() * 1.25f, c, 0.8f);
				}
			}
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.reset();
			
			for(BeforeLoadc c : NHWorldVars.advancedLoad){
				c.beforeLoad();
			}
			
			NHVars.world.clearLast();
			NHVars.world.worldLoaded = true;
//
//			if(Vars.player.admin)for(Block c : contents)c.buildVisibility = BuildVisibility.shown;
//			else for(Block c : contents)c.buildVisibility = BuildVisibility.sandboxOnly;
//
			
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
