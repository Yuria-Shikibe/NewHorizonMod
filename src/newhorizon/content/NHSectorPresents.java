package newhorizon.content;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.Logic;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.NHRegister;
import newhorizon.NHVars;
import newhorizon.expand.block.ancient.CaptureableTurret;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.cutscene.CSSTexts;
import newhorizon.expand.cutscene.MapCutscene;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.cutscene.NHCSS_UI;
import newhorizon.expand.cutscene.actions.CSSActions;
import newhorizon.expand.cutscene.stateoverride.UnitOverride;
import newhorizon.expand.cutscene.stateoverride.WorldOverride;
import newhorizon.expand.entities.Carrier;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.AutoEventTrigger;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.expand.eventsys.types.ReloadEventType;
import newhorizon.expand.eventsys.types.SignalEvent;
import newhorizon.expand.eventsys.types.WorldEventType;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

import java.util.Arrays;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static newhorizon.expand.cutscene.CSSTexts.*;

public class NHSectorPresents{
	public static SectorPreset initialPlane, abandonedOutpost, hostileResearchStation, ancientShipyard;
	
	public static void load(){
		/*
		ancientShipyard = new NHSectorPresent("ancient-shipyard", NHPlanets.midantha, 31){{
			difficulty = 9;
			
			rules = r -> {
				r.loadout.clear();
				r.loadout.addAll(ItemStack.with(NHItems.presstanium, 300, NHItems.juniorProcessor, 300, NHItems.zeta, 500, NHItems.metalOxhydrigen, 500, Items.tungsten, 500, Items.copper, 1000, Items.lead, 1000, Items.silicon, 800, Items.metaglass, 200, Items.titanium, 500, Items.thorium, 300));
				r.hiddenBuildItems.clear();
				r.attackMode = true;
				r.waves = true;
			};
			
			MapCutscene cutscene = new MapCutscene(name);
			NHCSS_Core.registerCutscene(cutscene);
			long nucleoidFlagLong = 123456789098765L;
			
			String aTurretsKey = "aTurrets";
			Seq<WorldEventType> types = new Seq<>();
			WorldEventType[] hyperspaceCharge = new WorldEventType[1];
			Prov<Seq<Building>> aTurrets = () -> cutscene.targetGroups.get(aTurretsKey);
			
			float cX = 255 * tilesize - 4, cY = 275 * tilesize - 4;
			float aTurretsChargeReload = 7200f;
			
			cutscene.register = () -> {
				cutscene.checkers.put(aTurretsKey, new Seq<>());
				
				Seq<Building> tgts = Seq.with(
					WorldOverride.getDummy(Team.derelict, 1788, 1908),
					WorldOverride.getDummy(Team.derelict, 1788, 2484),
					WorldOverride.getDummy(Team.derelict, 2284, 2484),
					WorldOverride.getDummy(Team.derelict, 2284, 1908)
				);
				
				int i = 0;
				types.clear();
				for(Building b : tgts){
					Prov<Building> buildingProv = () -> Vars.world.build(b.tileX(), b.tileY());
					
					types.add(new ReloadEventType("aTurretsKey-" + (i++)){{
						reloadTime = 60 * 10;
						hasCoord = true;
						initPos = b.pos();
						barRatio = (e) -> buildingProv.get() != null ? buildingProv.get().team == Vars.state.rules.defaultTeam ? e.type.progressRatio(e) : 1 : 0;
						colorFunc = (e) -> buildingProv.get() != null ? buildingProv.get().team == Vars.state.rules.defaultTeam ? NHColor.ancient : Pal.redderDust : Color.white;
						info = (e) ->
								buildingProv.get() != null ? buildingProv.get().team == Vars.state.rules.defaultTeam ?
							Core.bundle.format("nh.cutscene.ui.capture-ratio", e.ratio()) : Core.bundle.format("nh.cutscene.ui.waiting-capture", e.coordText()) : "";
					}
						
						@Override
						public void buildSpeInfo(Table table){
							table.label(() -> buildingProv.get().team == Vars.state.rules.defaultTeam ?
									Core.bundle.format("nh.cutscene.ui.capture-ratio", "/") : Core.bundle.format("nh.cutscene.ui.waiting-capture", "/")).update(l -> l.color.set(buildingProv.get().team == Vars.state.rules.defaultTeam ? NHColor.ancient : Pal.redderDust));
						}
						
						@Override
						public void onAdd(WorldEvent event){
							super.onAdd(event);
							
							cutscene.checkers.get(aTurretsKey).add(() -> {
								CaptureableTurret.CaptureableTurretBuild build = (CaptureableTurret.CaptureableTurretBuild)buildingProv.get();
								return event.reload > reloadTime && Angles.within(build.rotation, build.angleTo(cX, cY), 5);
							});
						}
						
						@Override
						public void updateEvent(WorldEvent e){
							if(cutscene.getValueBool("Stage00Completed"))e.remove();
						
							if(buildingProv.get() == null)return;
							
							CaptureableTurret.CaptureableTurretBuild build = (CaptureableTurret.CaptureableTurretBuild)buildingProv.get();
							if(build.team != Vars.state.rules.defaultTeam || !build.hasAmmo()){
								e.reload = 0;
								return;
							}
							
							e.reload += Time.delta;
							e.reload = Math.min(e.reload, reloadTime + 1f);
							
							if(e.reload > reloadTime){
								build.control(LAccess.shoot, cX / tilesize, cY / tilesize, Angles.within(build.rotation, build.angleTo(cX, cY), 5) ? 1.0 : 0.0, 0.0);
								build.reloadCounter = Math.min(build.reloadCounter, ((CaptureableTurret)build.block).reload * 0.9f);
							}
						}
					});
				}
				
				hyperspaceCharge[0] = new ReloadEventType("hyperspaceCharge"){{
					info = e -> standbyHyperspace();
					reloadTime = 60 * 60;
					colorFunc = e -> Tmp.c1.set(NHColor.ancient).lerp(NHColor.ancientLight, e.ratio());
					act = e -> {
						Unit nucleoid = UnitOverride.marked.get(nucleoidFlagLong);
						
						if(nucleoid != null){
							cutscene.putTag("Stage01Completed");
							nucleoid.heal();
							
							CSSActions.beginCreateAction();
							NHCSS_Core.core.applyMainBus(
								CSSActions.pullCurtain(),
								CSSActions.cameraScl(1.5f),
								CSSActions.cameraMove(cX, cY),
								CSSActions.text(getBundle("new-horizon-ancient-shipyard", 14)),
								CSSActions.runnable(() -> Sounds.unlock.play()),
								CSSActions.text(missionAccomplished()),
								CSSActions.waitUntil(() -> state.gameOver),
								CSSActions.withdrawCurtain()
							);
							CSSActions.endCreateAction();
							
							Carrier carrier = Carrier.create(nucleoid, new Vec2(cX + 10000, cY));
							NHCSS_Core.core.loadedUpdaters.add(() -> {
								if(carrier.onMove && carrier.contained){
									carrier.remove();
									if(!Vars.net.active()){
										EventHandler.runEventOnce("endgame", () -> {
											Logic.gameOver(Vars.state.rules.defaultTeam);
										});
									}
									
									if(Vars.state.hasSector()){
										if(Vars.state.rules.sector.preset != null){
											Logic.sectorCapture();
										}
									}else Logic.updateGameOver(Vars.state.rules.defaultTeam);
								}
							});
						}
					};
				}};
			};
			
			cutscene.drawers.add(csn -> {
				if(csn.reloadParam0 > 30f){
					float f = Mathf.curve(csn.reloadParam0 / aTurretsChargeReload, 30 / 480f, 1);
					float rad = 640 * Interp.pow4Out.apply(f) * Interp.pow4Out.apply(Mathf.curve(1 - f, 0, 0.035f));
					
					float z = Draw.z();
					Draw.z(Layer.effect);
					Draw.color(NHColor.ancient, NHColor.ancientLight, f);
					Lines.stroke(4f * f);
					
					Lines.stroke(f * 10.55f);
					DrawFunc.circlePercentFlip(cX, cY, rad, csn.reloadParam0 * 1.25f, 30f);
					
					Lines.stroke(f * 7.05f);
					DrawFunc.circlePercentFlip(cX, cY, rad * 1.25f, csn.reloadParam0 * 1.35f * 0.95f, 45f);
					
					Fill.circle(cX, cY, 12 * f);
					
					Draw.color(state.rules.waveTeam.color, Color.white, state.rules.defaultTeam.color, f);
					Lines.stroke(3f * Mathf.curve(1 - f, 0, 0.025f));
					DrawFunc.circlePercent(cX, cY, 280, f, 0);
					
					Draw.z(z);
				}
			});
			
			cutscene.initers.add(() -> {
				cutscene.targetGroups.put(aTurretsKey, Seq.with(
						Vars.world.buildWorld(1788, 1908),
						Vars.world.buildWorld(1788, 2484),
						Vars.world.buildWorld(2284, 2484),
						Vars.world.buildWorld(2284, 1908)
				));
				
				if(cutscene.getValueBool("Stage01Completed")){{
					if(!Vars.net.active()){
						Logic.gameOver(Vars.state.rules.defaultTeam);
					}
					
					if(Vars.state.hasSector()){
						if(Vars.state.rules.sector.preset != null){
							Logic.sectorCapture();
						}
					}else Logic.updateGameOver(Vars.state.rules.defaultTeam);
				}}
				
				makeECoreUnitsKillable();
				
				if(!cutscene.getValueBool("Stage00Completed")){
					NHOverride.coreUnits(u -> {
						NHUnitTypes.nucleoid.targetable = false;
					});
					NHRegister.addTaskOnSave(() -> {
						NHUnitTypes.nucleoid.targetable = true;
					});
				}
				
				if(!state.isEditor())EventHandler.runEventOnce("init", () -> {
					types.each(WorldEventType::create);
					
					NHVars.worldData.eventReloadSpeed = 0.35f;
					Vars.state.rules.hiddenBuildItems.clear();
					AutoEventTrigger.addAll();
					
					NHCSS_UI.opening();
					NHCSS_UI.setOverlayAlphaShiftSpeed(NHCSS_UI.OVERLAY_SPEED / 1.35f);
					
					NHFunc.spawnSingleUnit(Vars.state.rules.waveTeam, cX, cY, 0, 20, NHUnitTypes.nucleoid, s -> {
						s.setStatus(NHStatusEffects.marker, Float.POSITIVE_INFINITY);
						s.setFlagToApply(nucleoidFlagLong);
					});
					
					CSSActions.beginCreateAction();
					NHCSS_Core.core.applyMainBus(
							CSSActions.pullCurtain(),
							CSSActions.delay(NHCSS_UI.overlayShiftTime() + 30f),
							CSSActions.cameraScl(1f),
							CSSActions.text(getBundle(name, 0), true),
							CSSActions.text(getBundle(name, 1), true),
							CSSActions.text(getBundle(name, 2), true),
							CSSActions.cameraScl(2f),
							CSSActions.cameraMove(2636, 1268),
							CSSActions.parallel(
									CSSActions.runnable(() -> NHCSS_UI.mark(2632, 1264, 18f, 300, Pal.heal, () -> false)),
									CSSActions.text(CSSTexts.powerSuppressors(), true)
							),
							CSSActions.runnable(() -> WorldOverride.getFov(255 * tilesize, 275 * tilesize, Vars.state.rules.defaultTeam, 60)),
							CSSActions.cameraMove(255 * tilesize, 275 * tilesize),
							CSSActions.cameraScl(1f),
							CSSActions.text(getBundle(name, 3), true),
							CSSActions.text(getBundle(name, 4), true),
							CSSActions.parallel(
								CSSActions.text(getBundle(name, 5), true),
								CSSActions.runnable(() -> {
									aTurrets.get().each(b -> {
										NHCSS_UI.mark(b.x, b.y, b.hitSize() * 0.75f, 500, NHColor.ancient, NHCSS_UI.MarkStyle.defaultNoLines, () -> !b.isValid());
									});
								})
							),
							CSSActions.text(getBundle(name, 6), true),
							CSSActions.text(CSSTexts.endCommunicate(), true),
							CSSActions.cameraReturn(),
							CSSActions.withdrawCurtain()
					);
					CSSActions.endCreateAction();
				});
			});
			
			cutscene.updaters.add(() -> {
				Unit nucleoid = UnitOverride.marked.get(nucleoidFlagLong);
				
				if(nucleoid != null){
					if(nucleoid.physref != null && nucleoid.physref.body != null){
						nucleoid.physref.body.radius = 0;
					}
					
					if(!cutscene.getValueBool("Stage00Completed")){
						nucleoid.apply(NHStatusEffects.quiet, Float.POSITIVE_INFINITY);
						nucleoid.apply(NHStatusEffects.healthLocker, Float.POSITIVE_INFINITY);
					}
					
					if(nucleoid.dead){
						Time.run(300f, () -> Logic.updateGameOver(Vars.state.rules.waveTeam));
						Arrays.fill(nucleoid.abilities, UnitOverride.deathExplode.copy());
					}
				}else Logic.updateGameOver(state.rules.waveTeam);
				
				if(cutscene.checked(aTurretsKey)){
					EventHandler.runEventOnce("Stage00", () -> {
						NHVars.worldData.eventReloadSpeed = 1.235f;
						
						CSSActions.beginCreateAction();
						NHCSS_Core.core.applyMainBus(
								CSSActions.pullCurtain(),
								CSSActions.cameraScl(1f),
								CSSActions.cameraMove(cX, cY),
								CSSActions.text(getBundle(name, 7), true),
								CSSActions.text(getBundle(name, 8), true),
								CSSActions.cameraReturn(),
								CSSActions.withdrawCurtain()
							);
						CSSActions.endCreateAction();
					});
					
					Seq<Building> prov = cutscene.targetGroups.get(aTurretsKey);
					if(prov != null){
						prov.each(b -> {
							CaptureableTurret.CaptureableTurretBuild build = (CaptureableTurret.CaptureableTurretBuild)b;
							build.reloadCounter = Math.min(build.reloadCounter, ((CaptureableTurret)build.block).reload * 0.9f);
							build.heal();
							
							if(Mathf.chanceDelta(0.175 * cutscene.reloadParam0 / aTurretsChargeReload)){
								for(int i = 0; i < 4; i++){
									Lightning.create(Vars.state.rules.defaultTeam, NHColor.ancient, 10000, build.x + Mathf.range(12), build.y + Mathf.range(12), Mathf.random(360), Mathf.random(8, 32));
								}
							}
							
							Tmp.v1.set(cX, cY);
							if(cutscene.timer.get(2, 5)){
								if(!Vars.headless && Mathf.chanceDelta(0.1)) NHSounds.shock.at(cX, cY, Mathf.random(0.925f, 1.075f), Mathf.random(0.75f, 1f));
								
								Units.nearbyEnemies(state.rules.defaultTeam, cX, cY, 560, u -> {
									if(Mathf.chanceDelta(0.45)) PosLightning.create(nucleoid, state.rules.defaultTeam, Tmp.v1, u, NHColor.ancient, true, 1500, Mathf.random(4, 10), 2, 1, p -> {
										NHFx.hitSpark.at(p.getX(), p.getY(), NHColor.ancientLightMid);
									});
								});
								
								Vars.indexer.eachBlock(null, cX, cY, 560, bu -> bu.team != state.rules.defaultTeam, u -> {
									if(Mathf.chanceDelta(0.45)) PosLightning.create(nucleoid, state.rules.defaultTeam, Tmp.v1, u, NHColor.ancient, true, 1500, Mathf.random(4, 10), 2, 1, p -> {
										NHFx.hitSpark.at(p.getX(), p.getY(), NHColor.ancientLightMid);
									});
								});
							}
							
							if(!Vars.headless && build.shootWarmup > 0.9f && Mathf.chanceDelta(0.105 * build.shootWarmup)){
								NHFx.chainLightningFadeReversed.at(cX, cY, 12, NHColor.ancient, build);
								if(Mathf.chance(0.33)) PosLightning.createEffect(Tmp.v1.set(cX, cY), build, NHColor.ancient, 2, 2);
							}
						});
					}
					
					if(cutscene.getValueBool("Stage00Completed")){
						cutscene.reloadParam0 = 0;
					}else{
						cutscene.reloadParam0 += Time.delta;
						if(!Vars.headless){
							Effect.shake(3, 5, cX, cY);
							if(Mathf.chanceDelta(0.125))NHFunc.randFadeLightningEffect(cX + Mathf.range(22), cY + Mathf.range(22), Mathf.random(360), Mathf.random(12, 32), Tmp.c1.set(state.rules.waveTeam.color).lerp(state.rules.defaultTeam.color, cutscene.reloadParam0 / aTurretsChargeReload), Mathf.chance(0.5));
						}
					}
					
					if(Mathf.chanceDelta(0.115 * cutscene.reloadParam0 / aTurretsChargeReload)){
						for(int i = 0; i < 4; i++){
							Lightning.create(Vars.state.rules.defaultTeam, NHColor.ancient, 10000, cX + Mathf.range(36), cY + Mathf.range(36), Mathf.random(360), Mathf.random(12, 56));
						}
					}
					
					float size = NHUnitTypes.nucleoid.hitSize * 1.55f;
					
					if(!Vars.headless && Mathf.chanceDelta(0.073)){
						Tmp.v1.rnd(Mathf.random(size / 3.5f, size));
						NHFx.shuttleLerp.at(cX + Tmp.v1.x, cY + Tmp.v1.y, Tmp.v1.angle(), NHColor.ancientLightMid, Tmp.v1.len() / 2f);
					}
					
					if(!Vars.headless && cutscene.timer.get(1, 15f)){
						NHFx.dataTransport.at(cX + Mathf.range(120f), cY + Mathf.range(120f), 0, Vars.state.rules.defaultTeam.color);
					}
					
					if(cutscene.reloadParam0 > aTurretsChargeReload){
						cutscene.reloadParam0 = 0;
						
						if(nucleoid != null){
							if(!Vars.headless){
								Effect.shake(8, 4, cX, cY);
								NHBullets.ancientArtilleryProjectile.hitEffect.at(cX, cY, 0, NHColor.ancientLightMid);
								NHFx.hitSparkHuge.at(cX, cY, 0, NHColor.ancientLightMid);
								NHFx.square45_8_45.at(cX, cY, 0, NHColor.ancientLightMid);
								NHFx.circleOutLong.at(cX, cY, 800, NHColor.ancientLightMid);
							}
							
							nucleoid.heal();
							nucleoid.team(Vars.state.rules.defaultTeam);
							cutscene.putTag("Stage00Completed");
							if(!Vars.headless) Sounds.unlock.play();
							nucleoid.unapply(NHStatusEffects.healthLocker);
							NHUnitTypes.nucleoid.targetable = true;
						}
						
						{
							
							CSSActions.beginCreateAction();
							NHCSS_Core.core.applyMainBus(
								CSSActions.pullCurtain(),
								CSSActions.cameraScl(1.5f),
								CSSActions.cameraMove(cX, cY),
								CSSActions.text(getBundle(name, 9), true),
								CSSActions.runnable(() -> WorldOverride.getFov(1124, 3276, Vars.state.rules.defaultTeam, 22)), CSSActions.delay(12f), CSSActions.parallel(CSSActions.cameraMove(1124, 3276, 10, 1.3f, Interp.one), CSSActions.runnable(() -> {
									NHFunc.spawnSingleUnit(Vars.state.rules.waveTeam, 1124, 3276, Angles.angle(1124, 3276, cX, cY), 45f, NHUnitTypes.pester, s -> {
										s.setStatus(NHStatusEffects.overphased, 60);
									});
								})), CSSActions.delay(80f),
								
								CSSActions.runnable(() -> WorldOverride.getFov(780, 764, Vars.state.rules.defaultTeam, 22)), CSSActions.delay(12f), CSSActions.parallel(CSSActions.cameraMove(780, 764, 10, 1.3f, Interp.one), CSSActions.runnable(() -> {
									NHFunc.spawnSingleUnit(Vars.state.rules.waveTeam, 780, 764, Angles.angle(780, 764, cX, cY), 45f, NHUnitTypes.pester, s -> {
										s.setStatus(NHStatusEffects.overphased, 60);
										s.commandPos.set(cX, cY);
									});
								})), CSSActions.delay(80f),
								
								CSSActions.runnable(() -> WorldOverride.getFov(2964, 2200, Vars.state.rules.defaultTeam, 22)), CSSActions.delay(12f), CSSActions.parallel(CSSActions.cameraMove(2964, 2200, 10, 1.3f, Interp.one), CSSActions.runnable(() -> {
									NHFunc.spawnSingleUnit(Vars.state.rules.waveTeam, 2964, 2200, Angles.angle(2964, 2200, cX, cY), 45f, NHUnitTypes.pester, s -> {
										s.setStatus(NHStatusEffects.phased, Float.POSITIVE_INFINITY);
										s.commandPos.set(cX, cY);
										s.setFlagToApply(10L);
									});
								})), CSSActions.delay(70f),
								
								
								CSSActions.runnable(() -> {
									hyperspaceCharge[0].create();
									CSSActions.beginCreateAction();
									NHCSS_Core.core.applySubBus(CSSActions.text(getBundle(name, 10), true), CSSActions.text(getBundle(name, 11), true), CSSActions.text(getBundle(name, 12), true), CSSActions.text(getBundle(name, 13), true));
									CSSActions.endCreateAction();
								}), CSSActions.cameraReturn(), CSSActions.withdrawCurtain()
							);
							CSSActions.endCreateAction();
						}
					}
				}else cutscene.reloadParam0 = Mathf.lerpDelta(cutscene.reloadParam0, 0, 0.0035f);
			});
		}};
		*/
		
		hostileResearchStation = new NHSectorPresent("hostile-research-station", NHPlanets.midantha, 65){{
			captureWave = 40;
			difficulty = 8;
			
			allowLaunchSchematics = false;
			
			rules = r -> {
				r.loadout.clear();
				r.loadout.addAll(ItemStack.with(NHItems.presstanium, 300, NHItems.juniorProcessor, 300, NHItems.zeta, 500, NHItems.metalOxhydrigen, 500, Items.tungsten, 500, Items.copper, 1000, Items.lead, 1000, Items.silicon, 800, Items.metaglass, 200, Items.titanium, 500, Items.thorium, 300));
				r.hiddenBuildItems.clear();
				r.attackMode = true;
				r.waves = true;
			};
			
			NHCSS_Core.registerSkipping(this);
			
			Prov<CharSequence> coreS = () -> "Core Signal Detected";
			
			MapCutscene cutscene = new MapCutscene(name);
			
			SignalEvent[] events = new SignalEvent[3];
			
			cutscene.register = () -> {
				events[0] = new SignalEvent(name + "-core-signal-1"){{
					info = coreS;
					initPos = Point2.pack(85, 24);
				}};
				events[1] = new SignalEvent(name + "-core-signal-2"){{
					info = coreS;
					initPos = Point2.pack(255, 22);
				}};
				events[2] = new SignalEvent(name + "-core-signal-3"){{
					info = coreS;
					initPos = Point2.pack(56, 256);
				}};
			};
			
			cutscene.initers.add(() -> {
				makeECoreUnitsKillable();
				
				EventHandler.runEventOnce("init", () -> {
					NHVars.worldData.eventReloadSpeed = 0.35f;
					Vars.state.rules.hiddenBuildItems.clear();
					
					AutoEventTrigger.addAll();
					NHCSS_UI.opening();
					NHCSS_UI.setOverlayAlphaShiftSpeed(NHCSS_UI.OVERLAY_SPEED / 1.35f);
					
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
			});
			
			cutscene.updaters.add(() -> {
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
			});
			
			NHCSS_Core.registerCutscene(cutscene);
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
	
	private static void makeECoreUnitsKillable(){
		Seq<UnitType> unhitableable = new Seq<>();
		NHOverride.coreUnits(u -> {
			if(!u.hittable){
				u.hittable = true;
				unhitableable.add(u);
			}
		});
		NHRegister.addTaskOnSave(() -> {
			unhitableable.each(u -> u.hittable = false);
		});
	}
	
	public static class NHSectorPresent extends SectorPreset{
		
		public NHSectorPresent(String name, Planet planet, int sector){
			super(name, planet, sector);
		}
	}
}
