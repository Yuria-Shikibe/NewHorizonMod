package newhorizon.content;

import arc.func.Prov;
import arc.math.Interp;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.expand.NHVars;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.cutscene.CSSTexts;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.cutscene.NHCSS_UI;
import newhorizon.expand.cutscene.actions.CSSActions;
import newhorizon.expand.cutscene.stateoverride.WorldOverride;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.expand.eventsys.types.SignalEvent;

import static newhorizon.expand.cutscene.CSSTexts.*;

public class NHSectorPresents{
	public static SectorPreset initialPlane, abandonedOutpost, hostileResearchStation;
	
	public static void load(){
		hostileResearchStation = new NHSectorPresent("hostile-research-station", NHPlanets.midantha, 65){{
			captureWave = 40;
			difficulty = 8;
			
			allowLaunchSchematics = false;
			
			rules = r -> {
				r.loadout.clear();
				r.loadout.addAll(ItemStack.with(NHItems.presstanium, 300, NHItems.juniorProcessor, 300, NHItems.zeta, 500, NHItems.metalOxhydrigen, 500, Items.tungsten, 500, Items.copper, 1000, Items.lead, 1000, Items.silicon, 800, Items.metaglass, 200, Items.titanium, 500, Items.thorium, 300));
				r.hiddenBuildItems.clear();
				r.attackMode = true;
				r.waves = false;
				r.tags.put(NHInbuiltEvents.APPLY_KEY, "true");
			};
			
			NHCSS_Core.registerSkipping(this);
			
			Prov<CharSequence> coreS = () -> "Core Signal Detected";
			
			SignalEvent[] events = {
				new SignalEvent(name + "-core-signal-1"){{
					info = coreS;
					initPos = Point2.pack(85, 24);
				}},
				new SignalEvent(name + "-core-signal-2"){{
					info = coreS;
					initPos = Point2.pack(255, 22);
				}},
				new SignalEvent(name + "-core-signal-3"){{
					info = coreS;
					initPos = Point2.pack(56, 256);
				}}
			};
			
			NHCSS_Core.register(NHCSS_Core.initers, Seq.with(() -> {
//				Seq<UnitType> untargetable = new Seq<>();
				Seq<UnitType> unhitableable = new Seq<>();
				NHOverride.coreUnits(u -> {
//					if(!u.targetable){
//						u.targetable = true;
//						untargetable.add(u);
//					}
//
					if(!u.hittable){
						u.hittable = true;
						unhitableable.add(u);
					}
				});
				NHVars.worldData.addTaskOnSave(() -> {
//					untargetable.each(u -> u.targetable = false);
					unhitableable.each(u -> u.hittable = false);
				});
				
				EventHandler.runEventOnce("init", () -> {
					NHVars.worldData.eventReloadSpeed = 0.35f;
					Vars.state.rules.hiddenBuildItems.clear();
					
					NHInbuiltEvents.autoTriggers.each(t -> t.copy().add());
					NHCSS_UI.opening();
					NHCSS_UI.setOverlayAlphaShiftSpeed(NHCSS_UI.OVERLAY_SPEED / 1.25f);
					
					CSSActions.beginCreateAction();
					NHCSS_Core.core.applyMainBus(
						CSSActions.pullCurtain(),
						CSSActions.delay(NHCSS_UI.overlayShiftTime() + 30f),
						CSSActions.cameraScl(1f),
						CSSActions.text(getBundle(name, 0), true),
						CSSActions.text(getBundle(name, 1), true),
						CSSActions.text(getBundle(name, 2), true),
						CSSActions.text(getBundle(name, 3), true),
						CSSActions.runnable(() -> {
							WorldOverride.getFov(828, 732, Vars.state.rules.defaultTeam, 12);
							if(!Vars.net.client()){
								for(SignalEvent e : events){
									e.create().team(Vars.state.rules.defaultTeam);
								}
							}
						}),
						CSSActions.cameraMove(452f, 2052f),
						CSSActions.text(getBundle(name, 4), true),
						CSSActions.text(endCommunicate(), true),
						CSSActions.cameraReturn(),
						CSSActions.withdrawCurtain()
					);
					CSSActions.endCreateAction();
				});
			}), name, localizedName);
			
			NHCSS_Core.register(NHCSS_Core.updaters, Seq.with(() -> {
				Seq<CoreBlock.CoreBuild> cores = Vars.state.rules.waveTeam.cores();
				
				Building enemyCore0 = Vars.world.build(56, 256);
				
				if(enemyCore0 instanceof CoreBlock.CoreBuild && enemyCore0.isValid() && WorldOverride.visible(enemyCore0)){
					EventHandler.runEventOnce("stronghold-turret", () -> {
						CSSActions.beginCreateAction();
						
						NHCSS_Core.core.applySubBus(
							CSSActions.parallel(
								CSSActions.runnable(() -> {
									NHCSS_UI.mark(41 * 8, 251 * 8, 14, 180f, NHColor.lightSky, () -> false);
								}),
								CSSActions.text(CSSTexts.getBundle(name, 5), true)
							)
						);
						
						CSSActions.endCreateAction();
					});
				}
				
				Building enemyMainJumpgate = Vars.world.build(181, 55);
				Building powerNode1 = Vars.world.build(231, 176);
				Building powerNode2 = Vars.world.build(124, 170);
				if(enemyMainJumpgate instanceof JumpGate.JumpGateBuild && enemyMainJumpgate.team == Vars.state.rules.waveTeam){
					if(enemyMainJumpgate.efficiency > 0.1f && enemyMainJumpgate.enabled){
						float x = enemyMainJumpgate.x;
						float y = enemyMainJumpgate.y;
						float size = enemyMainJumpgate.hitSize();
						EventHandler.runEventOnce("jumpgate-activated", () -> {
							CSSActions.beginCreateAction();
							
							WorldOverride.getFov(x, y, Vars.state.rules.defaultTeam, 16);
							
							NHCSS_Core.core.applyMainBus(
								CSSActions.pullCurtain(),
								CSSActions.cameraScl(3),
								CSSActions.cameraMove(x, y),
								CSSActions.parallel(
									CSSActions.alert(),
									CSSActions.caution(x, y, size * 0.75f, 120f, Vars.state.rules.waveTeam.color),
									CSSActions.text(CSSTexts.jumpgateTriggered(), true)
								),
								CSSActions.cameraReturn(),
								CSSActions.withdrawCurtain()
							);
							
							CSSActions.endCreateAction();
						});
					}
				}
				
				if(!EventHandler.has("cautioned") && WorldOverride.visible(powerNode1) || WorldOverride.visible(powerNode2)){
					EventHandler.runEventOnce("cautioned", () -> {
						Building target = WorldOverride.visible(powerNode1) ? powerNode1 : powerNode2;
						float x = target.x;
						float y = target.y;
						CSSActions.beginCreateAction();
						
						NHCSS_Core.core.applySubBus(
							CSSActions.parallel(
								CSSActions.runnable(() -> NHCSS_UI.mark(x, y, 18f, 300, Pal.heal, () -> !target.isValid())),
								CSSActions.text(CSSTexts.powerSuppressors())
							)
						);
						
						CSSActions.endCreateAction();
					});
				}
				
				if(cores.size == 3){
					EventHandler.runEventOnce("core-caution", () -> {
						CSSActions.beginCreateAction();
						
						NHCSS_Core.core.applySubBus(
							CSSActions.text(CSSTexts.getBundle(name, 6)),
							CSSActions.text(CSSTexts.getBundle(name, 7)),
							CSSActions.runnable(() -> NHVars.worldData.eventReloadSpeed = 2f)
						);
						
						CSSActions.endCreateAction();
					});
				}
				if(cores.size < 3 && cores.any()){
					EventHandler.runEventOnce("reinforcements", () -> {
						CSSActions.beginCreateAction ();
						
						CoreBlock.CoreBuild target = cores.first();
						CoreBlock.CoreBuild base = Vars.state.rules.defaultTeam.cores().firstOpt();
						float x = target.x;
						float y = target.y;
						NHVars.worldData.eventReloadSpeed = 4f;
						if(base == null)return;
						float x1 = base.x, y1 = base.y;
						WorldOverride.getFov(x, y, Vars.state.rules.defaultTeam, 32);
						NHCSS_Core.core.applyMainBus(
							CSSActions.pullCurtain(),
							CSSActions.cameraMove(x, y),
							CSSActions.parallel(
								CSSActions.caution(x, y, 18f, 150f, Vars.state.rules.waveTeam.color),
								CSSActions.text(CSSTexts.reinforcementsInbound())
							),
							CSSActions.cameraScl(3.4f),
							CSSActions.unitInbound(NHUnitTypes.pester, x, y, a -> {
								a.spawnAngle = 180;
								a.status = NHStatusEffects.overphased;
							}),
							CSSActions.delay(120f),
							CSSActions.cameraMove(x1, y1, 15, 2.02f, Interp.one),
							CSSActions.runnable(() -> {
								
								if(!Vars.headless){
									NHSounds.alarm.play(1);
									NHCSS_UI.mark(x1, y1, 48f, 240f, Pal.redderDust, () -> false);
								}
							}),
							CSSActions.text(incomingRaid()),
							CSSActions.triggerEvent(NHInbuiltEvents.raidQuick, new Vec2(x1, y1), () -> Vars.state.rules.waveTeam),
							CSSActions.cameraSustain(420f),
							CSSActions.text(takingDamage_Heavy()),
							CSSActions.withdrawCurtain()
						);
						
						CSSActions.endCreateAction();
					});
				}
			}), name, localizedName);
		}};
		
		abandonedOutpost = new NHSectorPresent("abandoned-outpost", NHPlanets.midantha, 15){{
			captureWave = 40;
			difficulty = 4;
			
			alwaysUnlocked = true;
			
			rules = r -> {
				r.winWave = captureWave;
			};
		}};
		
		initialPlane = new NHSectorPresent("initial-plane", NHPlanets.midantha, 0){{
			captureWave = 50;
			difficulty = 7;
			
			rules = r -> {
				r.tags.put(NHInbuiltEvents.APPLY_KEY, "true");
				r.winWave = captureWave;
//				NHRegister.postAfterLoad(() -> {
//					if(Vars.net.client())return;
//					WorldLabel l = Pools.obtain(WorldLabel.class, WorldLabel::create);
//					l.text(Core.bundle.get("nh.sector-hint.initialPlane.raider"));
//					l.set(307 * 8, 306 * 8);
//					l.add();
//				});
			};
		}};
	}
	
	public static class NHSectorPresent extends SectorPreset{
		
		public NHSectorPresent(String name, Planet planet, int sector){
			super(name, planet, sector);
		}
	}
}
