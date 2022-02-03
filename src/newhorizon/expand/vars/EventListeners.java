package newhorizon.expand.vars;

import arc.Core;
import arc.Events;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.content.*;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.block.special.PlayerJumpGate;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.expand.entities.NHGroups;
import newhorizon.util.annotation.ClientDisabled;
import newhorizon.util.feature.cutscene.CutsceneScript;
import newhorizon.util.feature.cutscene.EventSamples;
import newhorizon.util.feature.cutscene.Triggers;
import newhorizon.util.feature.cutscene.events.FleetEvent;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;
import newhorizon.util.feature.cutscene.events.util.PreMadeRaids;
import newhorizon.util.func.NHSetting;
import newhorizon.util.func.OV_Pair;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.ShadowProcessor;
import newhorizon.util.ui.ScreenInterferencer;

import static mindustry.Vars.control;
import static mindustry.Vars.player;


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
	
	@ClientDisabled
	public static final Seq<AutoEventTrigger> autoTriggers = new Seq<>();
	
	public static void loadAfterContent(){
		autoTriggers.addAll(
			new AutoEventTrigger(){{
				items = OV_Pair.seqWith(Items.surgeAlloy, 50, NHItems.juniorProcessor, 150);
				eventType = PreMadeRaids.standardRaid1;
				spacingBase *= 1.75f;
				spacingRand = 60 * 60;
			}},
			new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.multipleSteel, 150, Items.plastanium, 100);
				eventType = PreMadeRaids.raid2;
				spacingBase *= 2;
				spacingRand = 120 * 60;
			}},
			new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.irayrondPanel, 150, NHItems.seniorProcessor, 150);
				eventType = PreMadeRaids.deadlyRaid1;
				spacingBase *= 2.25f;
				spacingRand = 180 * 60;
			}},
			new AutoEventTrigger(){{
				buildings = OV_Pair.seqWith(NHBlocks.jumpGate, 1);
				eventType = new FleetEvent("inbuilt-inbound-server-1"){{
					unitTypeMap = ObjectMap.of(NHUnitTypes.naxos, 2, NHUnitTypes.striker, 4, NHUnitTypes.warper, 10, NHUnitTypes.assaulter, 4);
					reloadTime = 20 * 60;
					removeAfterTriggered = cannotBeRemove = true;
				}};
				
				minTriggerWave = 25;
				spacingBase = 120 * 60;
				spacingRand = 120 * 60;
			}},
			new AutoEventTrigger(){{
				items = OV_Pair.seqWith(Items.thorium, 50, NHItems.zeta, 80, NHItems.presstanium, 30);
				eventType = new FleetEvent("inbuilt-inbound-server-2"){{
					unitTypeMap = ObjectMap.of(NHUnitTypes.branch, 4, NHUnitTypes.sharp, 4);
					reloadTime = 20 * 60;
					removeAfterTriggered = cannotBeRemove = true;
				}};
				
				
				spacingBase = 90 * 60;
				spacingRand = 120 * 60;
			}},
			new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.darkEnergy, 300);
				eventType = EventSamples.fleetInbound;
				
				minTriggerWave = 35;
				spacingBase = 240 * 60;
				spacingRand = 120 * 60;
			}},
			new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.upgradeSort, 300);
				eventType = new FleetEvent("inbuilt-inbound-server-4"){{
					unitTypeMap = ObjectMap.of(NHUnitTypes.longinus, 4, NHUnitTypes.naxos, 10, NHUnitTypes.saviour, 2);
					reloadTime = 40 * 60;
					removeAfterTriggered = cannotBeRemove = true;
				}};
				
				minTriggerWave = 35;
				spacingBase = 240 * 60;
				spacingRand = 180 * 60;
			}}
		);
		
		if(Vars.headless || NewHorizon.DEBUGGING){
			Events.on(EventType.WorldLoadEvent.class, e -> {
				Core.app.post(() -> Core.app.post(() -> {
					if(!Vars.state.rules.pvp && NHGroups.autoEventTriggers.size() < autoTriggers.size){
						CutsceneScript.runEventOnce("server-trigger-init", () -> {
							autoTriggers.each(t -> t.copy().add());
						});
					}
				}));
			});
		}
	}
	
	public static void load(){
		kickWarn = Core.bundle.get("mod.ui.requite.need-override");
		
		Events.run(Triggers.raid_setup, () -> {
			raid_setup = true;
		});
		
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHVars.world.afterLoad();
			ShadowProcessor.clear();
			
			actAfterLoad.each(Runnable::run);
			actAfterLoad.clear();
		});
		
		Events.run(EventType.Trigger.update, () -> {
			if(Vars.state.isPlaying()){
				NHGroups.update();
			}
		});
		
		Events.on(EventType.ResetEvent.class, e -> {
			actAfterLoad.clear();
			NHVars.reset();
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu){
				actAfterLoad.clear();
				NHVars.reset();
				ShadowProcessor.clear();
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
			
			if(control.input.block instanceof JumpGate){
				DrawFunc.drawWhileHold(JumpGate.JumpGateBuild.class, build -> {
					Tmp.v1.trns(player.angleTo(build), player.unit().hitSize() + 15);
					Draw.color(Pal.gray);
					Fill.square(Tmp.v1.x + player.x, Tmp.v1.y + player.y, 5);
					Draw.color();
					if(build.isCalling())Draw.mixcol(Color.white, Mathf.absin(4f, 0.45f));
					else{
						if(build.cooling)Draw.mixcol(Pal.lancerLaser, Mathf.absin(4f, 0.45f));
						else Draw.mixcol(player.team().color, Mathf.absin(4f, 0.45f));
					}
					Draw.rect(build.block.fullIcon, Tmp.v1.x + player.x, Tmp.v1.y + player.y, 8, 8, 0);
					Tmp.v2.set(Tmp.v1).nor().scl(player.unit().hitSize() + 22).add(player.x, player.y);
					Drawf.arrow(Tmp.v1.x + player.x, Tmp.v1.y + player.y, Tmp.v2.x, Tmp.v2.y, 10f, 4f, player.team().color);
				});
			}
			
			if(control.input.block instanceof HyperSpaceWarper){
				DrawFunc.drawWhileHold(HyperSpaceWarper.HyperSpaceWarperBuild.class, build -> {
					Tmp.v1.trns(player.angleTo(build), player.unit().hitSize() + 15);
					Draw.color(Pal.gray);
					Fill.square(Tmp.v1.x + player.x, Tmp.v1.y + player.y, 5);
					Draw.color();
					if(build.chargeValid())Draw.mixcol(player.team().color, Mathf.absin(4f, 0.45f));
					else Draw.mixcol(Color.white, Mathf.absin(4f, 0.45f));
					
					Draw.rect(build.block.fullIcon, Tmp.v1.x + player.x, Tmp.v1.y + player.y, 8, 8, 0);
					Tmp.v2.set(Tmp.v1).nor().scl(player.unit().hitSize() + 22).add(player.x, player.y);
					Drawf.arrow(Tmp.v1.x + player.x, Tmp.v1.y + player.y, Tmp.v2.x, Tmp.v2.y, 10f, 4f, player.team().color);
				});
			}
			
			if(control.input.block instanceof PlayerJumpGate){
				Seq<Building> hasDrawnDash = new Seq<>();
				Seq<PlayerJumpGate.PlayerJumpGateBuild> builds = Groups.build.copy(new Seq<>()).filter(b -> b.team == Vars.player.team() && b instanceof PlayerJumpGate.PlayerJumpGateBuild).as();
				for(PlayerJumpGate.PlayerJumpGateBuild build : builds){
					Tmp.v1.trns(player.angleTo(build), player.unit().hitSize() + 15);
					Draw.color(Pal.gray);
					
					Draw.z(Layer.overlayUI + 1);
					Fill.square(Tmp.v1.x + player.x, Tmp.v1.y + player.y, 5);
					Draw.color();
					if(build.canFunction())Draw.mixcol(player.team().color, Mathf.absin(4f, 0.45f));
					else Draw.mixcol(Color.white, Mathf.absin(4f, 0.45f));
					
					Draw.rect(build.block.fullIcon, Tmp.v1.x + player.x, Tmp.v1.y + player.y, 8, 8, 0);
					Tmp.v2.set(Tmp.v1).nor().scl(player.unit().hitSize() + 22).add(player.x, player.y);
					Drawf.arrow(Tmp.v1.x + player.x, Tmp.v1.y + player.y, Tmp.v2.x, Tmp.v2.y, 10f, 4f, player.team().color);
					
					
					if(build.linkValid()){
						PlayerJumpGate.PlayerJumpGateBuild linked = (PlayerJumpGate.PlayerJumpGateBuild)build.link();
						Tmp.v1.trns(player.angleTo(build), player.unit().hitSize() + 15);
						Tmp.v3.trns(player.angleTo(linked), player.unit().hitSize() + 15).add(player.x, player.y);
						Draw.z(Layer.overlayUI + 0.9f);
						if(!hasDrawnDash.contains(build))Drawf.dashLine(player.team().color, Tmp.v1.x + player.x, Tmp.v1.y + player.y, Tmp.v3.x, Tmp.v3.y);
						Draw.z(Layer.overlayUI + 0.95f);
						Drawf.arrow(Tmp.v1.x + player.x, Tmp.v1.y + player.y, Tmp.v3.x, Tmp.v3.y, 10f, 3f, player.team().color);
						if(linked.link() == build)hasDrawnDash.add(linked);
					}
				}
			}
			
			toDraw.each(Runnable::run);
			
			ShadowProcessor.post();
			ShadowProcessor.clear();
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
