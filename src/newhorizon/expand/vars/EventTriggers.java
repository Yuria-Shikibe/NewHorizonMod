package newhorizon.expand.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.core.GameState;
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
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.util.feature.ScreenHack;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHSetting;


public class EventTriggers{
	public static class BossGeneratedEvent{
		public final Unit unit;
		
		public BossGeneratedEvent(Unit unit){
			this.unit = unit;
		}
	}
	
	public static final Seq<Runnable> actAfterLoad = new Seq<>();
	public static Seq<Block> banned = new Seq<>();
	
	public static Interval timer = new Interval();
	
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
//		Events.run(EventType.Trigger.update, () -> {
//			if(timer.get(60f) && !Vars.state.isMenu())TileSortMap.continueUpdateAll();
//		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.world.worldLoaded = true;
			
			NHVars.world.afterLoad();
			
			actAfterLoad.each(Runnable::run);
			actAfterLoad.clear();
			
//			for(Teams.TeamData data : Vars.state.teams.getActive()){
//				data.mineItems.add(NHItems.zeta);
//			}
			
//			TileSortMap.init();
//			TileSortMap.softUpdateAll();
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.from == GameState.State.playing && e.to == GameState.State.menu){
				NHVars.reset();
				
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
		});

		Events.run(EventType.Trigger.update, ScreenHack::update);
		
		Events.on(ScreenHack.ScreenHackEvent.class, e -> {
			ScreenHack.generate(e.target, e.time);
		});
		
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		Events.on(BossGeneratedEvent.class, e -> {
			Vars.ui.hudfrag.showToast(Icon.warning, e.unit.type.localizedName + " Approaching");
		});
		
		/*Events.run(EventType.Trigger.preDraw, () -> {
			float scl = 20;
			Building building = Vars.control.input.frag.config.getSelectedTile();
			
			float z = Draw.z();
			
			
			if(building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper)){
				for(GravityTrap.GravityTrapBuild b : NHVars.world.gravityTraps){
					if(!b.active())return;
					Draw.draw(Layer.overlayUI, () -> {
						NHShaders.gravityTrapShader.apply();
						NHShaders.gravityTrapShader.bind();
						Draw.shader(NHShaders.gravityTrapShader);
						
						
						Color c = b.team == Vars.player.team() ? Pal.lancerLaser : Pal.redderDust;
						Tmp.c1.set(c).lerp(Color.white, Mathf.absin(scl, 1f));
						Draw.color(Tmp.c1);
						Fill.poly(b.x, b.y,6, b.range());
						//						Drawf.light(b.x, b.y, b.range() * 1.25f, c, 0.8f);
						Draw.shader();
						
						Draw.reset();
					});
					
					
				}
			}
		});
		
		Events.run(EventType.Trigger.postDraw, () -> {
					Draw.drawRange(Layer.overlayUI, 1f, () -> Vars.renderer.effectBuffer.begin(Color.clear), () -> {
						Vars.renderer.effectBuffer.end();
						Vars.renderer.effectBuffer.blit(NHShaders.gravityTrapShader);
					});
		});*/
		
		Events.run(EventType.Trigger.draw, () -> {
			float scl = 20;
			Building building = Vars.control.input.frag.config.getSelectedTile();
			
			float z = Draw.z();
			
			if(building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper)){
				for(GravityTrap.TrapField bi : NHFunc.getObjects(NHVars.world.gravityTraps)){
					GravityTrap.GravityTrapBuild b = bi.build;
					if(!b.active())return;
					Draw.z(Layer.buildBeam + Mathf.num(b.team != Vars.player.team() ^ ((Time.time % (scl * 8 * Mathf.pi)) > scl * Mathf.pi && (Time.time % (scl * 8 * Mathf.pi)) < scl * Mathf.pi * 5)));
					
					Color c = b.team == Vars.player.team() ? Pal.lancerLaser : Pal.redderDust;
					Tmp.c1.set(c).lerp(Color.white, Mathf.absin(scl, 1f));
					Draw.color(Tmp.c1);
					Fill.poly(b.x, b.y,6, b.range());
					Drawf.light(b.x, b.y, b.range() * 1.25f, c, 0.8f);
					
					Draw.reset();
				}
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
