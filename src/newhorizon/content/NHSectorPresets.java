package newhorizon.content;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.Action;
import arc.scene.actions.Actions;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.SectorPresets;
import mindustry.ctype.ContentList;
import mindustry.game.EventType;
import mindustry.game.SectorInfo;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.SectorPreset;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.NewHorizon;
import newhorizon.util.feature.cutscene.*;
import newhorizon.util.func.NHFunc;

import static mindustry.Vars.*;
import static newhorizon.util.feature.cutscene.KeyFormat.ENEMY_CORE_DESTROYED_EVENT;
import static newhorizon.util.feature.cutscene.KeyFormat.generateName;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class NHSectorPresets implements ContentList{
	public static ObjectMap<SectorPreset, Cons<Sector>> captureMap = new ObjectMap<>(), loseMap = new ObjectMap<>();
	
	public static SectorPreset
		hostileHQ, downpour, luminariOutpost, quantumCraters, ruinedWarehouse, shatteredRavine, deltaHQ, mainPath, ancientBattefield;
	
	protected static Fi scriptsDic = null;
	
	protected static String loadJS(String name){
		return scriptsDic.child(name.replaceFirst(NewHorizon.MOD_NAME + "-", "") + ".js").readString();
	}
	
	@Override
	public void load(){
		scriptsDic = NewHorizon.MOD.root.child("custom-cutscene");
		
		CutsceneScript.ender.put(SectorPresets.craters, Seq.with((Boolean b) -> {
			if(!NHBlocks.jumpGatePrimary.unlocked() && b){
				CutsceneScript.runEventOnce("JGP", () -> {
					EventSamples.jumpgateUnlockObjective.setup();
					EventSamples.jumpgateUnlock.setup();
				});
			}
		}));
		
		CutsceneScript.initer.put(SectorPresets.craters, Seq.with(() -> {
			if(!NHBlocks.jumpGatePrimary.unlocked() && state.getSector().isCaptured()){
				CutsceneScript.runEventOnce("JGP", () -> {
					EventSamples.jumpgateUnlockObjective.setup();
					EventSamples.jumpgateUnlock.setup();
				});
			}
		}));
		
		ancientBattefield = new NHSectorPreset("ancient-battefield", NHPlanets.midantha, 96){{
			addStartingItems = true;
			difficulty = 10;
		}};
		
		mainPath = new NHSectorPreset("main-path", NHPlanets.midantha, 11){{
			addStartingItems = true;
			useAI = true;
			difficulty = 12;
			
			loseMap.put(this, NHSectorPresets::resetSector);
			
			CutsceneScript.updaters.put(this, Seq.with(() -> {
				if(CutsceneScript.timer.get(0, 2400f) && sector.save != null && sector.save.meta != null && sector.save.meta.timePlayed > 3000f){
					if(state.rules.attackMode && state.rules.waveTeam.cores().any() && state.rules.waveTeam.cores().size < 3){
						CoreBlock.CoreBuild core = state.rules.waveTeam.core();
						UIActions.actionSeqMinor(
							Actions.parallel(
								UIActions.cautionAt(core.x, core.y, core.block.size * tilesize / 2f, 6f, core.team.color),
								Actions.run(() -> {
									NHSounds.alarm.play();
									NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(player.team().core()), 120f, 360f, 30f, NHUnitTypes.destruction, 6);
								}),
								UIActions.labelAct(
									"[accent]Caution[]: @@@Hostile Fleet Incoming."
									, 0.75f, 3.26f, Interp.linear, t -> {
										t.image(Icon.warning).padRight(OFFSET);
									}
								)
							)
						);
					}
				}
			}));
			
			CutsceneScript.initer.put(this, Seq.with(() -> {
				if(sector == null)return;
				if(CutsceneScript.canInit()){
					Time.run(30f, () -> {
						UIActions.actionSeq(
							UIActions.startCutsceneDefault(),
							UIActions.moveTo(316, 612, 1f, Interp.pow3Out),
							UIActions.labelAct(
								"[accent]Objective[]: @@@Use the limited resources and these gates to defeat enemies."
								, 0.75f, 3.25f, Interp.linear, t -> {
									t.image(Icon.download).size(LEN - OFFSET);
									t.image(Icon.upOpen).size(LEN - OFFSET);
									t.image(NHBlocks.jumpGate.fullIcon).size(LEN - OFFSET).padRight(OFFSET);
								}
							),
							UIActions.moveTo(316, 3712, 6f, Interp.pow3Out),
							UIActions.labelAct(
								"[accent]Objective[]: @@@Destroy Enemy Base!"
								, 0.75f, 4.75f, Interp.linear, t -> {
									t.image(Icon.warning).padRight(OFFSET);
								}
							),
							UIActions.endCutsceneDefault()
						);
					});
				}
			}));
		}
			@Override
			public void loadIcon(){
				if(Icon.layers != null)uiIcon = fullIcon = Icon.link.getRegion();
			}
		};
		
		deltaHQ = new NHSectorPreset("delta-HQ", NHPlanets.midantha, 79){{
			addStartingItems = true;
			useAI = false;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		shatteredRavine = new NHSectorPreset("shattered-ravine", NHPlanets.midantha, 64){{
			addStartingItems = true;
			useAI = false;
			difficulty = 10;
			startWaveTimeMultiplier = 2.5f;
			captureWave = 30;
		}};
		
		ruinedWarehouse = new NHSectorPreset("ruined-warehouse", NHPlanets.midantha, 0){{
			addStartingItems = true;
			useAI = false;
			difficulty = 3;
			startWaveTimeMultiplier = 2.5f;
			captureWave = 40;
		}};
		
		hostileHQ = new NHSectorPreset("hostile-HQ", NHPlanets.midantha, 24){{
			addStartingItems = true;
			useAI = false;
			difficulty = 20;
			startWaveTimeMultiplier = 2.5f;
			
			loseMap.put(this, NHSectorPresets::resetSector);
			
			String eventNameFlagship = generateName("Hostile Flagships Arriving", Pal.redderDust, 60 * 60 * 10);
			
			CutsceneScript.updaters.put(this, Seq.with(() -> {
				if(sector != null && sector.info.wasCaptured)return;
				
				if(CutsceneScript.eventHasData(ENEMY_CORE_DESTROYED_EVENT)){
					if(!CutsceneScript.isPlayingCutscene){
						CutsceneScript.runEventOnce("setup-reinforcements", () -> EventSamples.fleetApproaching.setup());
						CutsceneScript.runEventOnce("setup-raid", () -> EventSamples.waveTeamRaid.setup());
						
						
					}
					
					CutsceneScript.reload(eventNameFlagship, Time.delta, 60 * 60 * 10, () -> !CutsceneScript.getBool("SpawnedBoss"), () -> true, () -> {
						CutsceneScript.runEventOnce("SpawnedBoss", () -> {
							float sX = world.unitWidth() - 240, sY =  world.unitHeight() - 240;
							
							UIActions.actionSeq(
								Actions.parallel(Actions.delay(2f), UIActions.curtainIn(2f, Interp.pow2Out)), Actions.run(UIActions::pauseCamera),
								UIActions.moveTo(sX - 360, sY - 120, 2f, Interp.pow3),
								Actions.parallel(
									UIActions.holdCamera(sX - 360, sY - 120, 13),
									Actions.sequence(
										Actions.parallel(
											UIActions.labelAct(
													"[accent]Caution[]: @@@Flagship Group Incoming.",
													0.75f, 6.25f, Interp.linear, t -> {
														t.image(Icon.warning).padRight(OFFSET);
													}),
											Actions.run(() -> {
												Angles.randLenVectors(Time.millis(), 3, 400f, (i, j) -> {
													WorldActions.raidDirection(state.rules.waveTeam.cores().firstOpt(), state.rules.waveTeam, NHBullets.eternity,
															sX + 480 + i, sY + 800 + j, 225, Mathf.dst(sX + 480 + i, sY + 800 + j, sX, sY) + Mathf.random(600, 900), b -> {}
													).run();
												});
											})
										),
										Actions.parallel(
											UIActions.cautionAt(sX, sY, 30f, 4.5f, Pal.redderDust),
											Actions.run(() -> {
												NHSounds.alarm.play();
												NHFunc.spawnUnit(state.rules.waveTeam, sX, sY, 225, 300f, 180f, 90f, NHUnitTypes.collapser, 4);
											}),
											UIActions.labelActFull(
											"[accent]Caution[]: @@@Collapsers Approaching",
											0.75f, 6.25f, Interp.linear, Interp.one, t -> {
												t.image(Icon.warning).padRight(OFFSET);
											})
										)
									)
								),
								Actions.run(UIActions::resumeCamera),
								UIActions.curtainOut(1f, Interp.pow2In)
							);
						});
					});
				}
			}));
			
			CutsceneScript.initer.put(this, Seq.with(() -> {
				if(sector != null && sector.info.wasCaptured)return;
				
				CutsceneScript.addListener(Blocks.coreNucleus, b -> {
					if(b.team != state.rules.waveTeam)return;
					
					CutsceneScript.runEventOnce(ENEMY_CORE_DESTROYED_EVENT, () -> {
						CoreBlock.CoreBuild core = state.teams.cores(state.rules.waveTeam).first();
						
						UIActions.reloadBarDelay("Hostile Flagships Arriving", 60 * 60 * 10, Pal.redderDust);
						
						UIActions.actionSeq(
							Actions.parallel(Actions.delay(2f), UIActions.curtainIn(2f, Interp.pow2Out)), Actions.run(UIActions::pauseCamera),
							UIActions.moveTo(core.x, core.y, 2f, Interp.pow3),
							Actions.parallel(
								UIActions.holdCamera(core.x, core.y, 8f),
								Actions.sequence(
									UIActions.labelAct(
											"[accent]Caution[]: @@@Reinforcements Incoming."
											, 0.75f, 3.25f, Interp.linear, t -> {
												t.image(Icon.warning).padRight(OFFSET);
											}
									),
									Actions.parallel(
										UIActions.cautionAt(core.x, core.y, core.block.size / 3f * tilesize, 3.5f, state.rules.waveTeam.color),
										Actions.run(() -> {
											NHSounds.alarm.play();
											NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(player.core()), 120f, 60f, 30f, NHUnitTypes.longinus, 6);
											NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(player.core()), 240f, 60f, 30f, NHUnitTypes.guardian, 2);
										}),
										UIActions.labelActFull(
											"[accent]Caution[]: @@@Multiple hostile units detected."
											, 0.75f, 3.25f, Interp.linear, Interp.one, t -> {
												t.image(Icon.warning).padRight(OFFSET);
											}
										)
									)
								)
							),
							Actions.run(UIActions::resumeCamera),
							UIActions.curtainOut(1f, Interp.pow2In)
						);
					});
				});
				
				if(CutsceneScript.canInit()){
					Time.run(30f, () -> {
						Seq<CoreBlock.CoreBuild> cores = state.teams.cores(state.rules.waveTeam);
						Seq<Action> actions = new Seq<>(cores.size * 2);
						
						actions.add(UIActions.startCutsceneDefault());
						
						actions.addAll(
							UIActions.moveTo(1512, 1968, 2f, Interp.pow3),
							Actions.parallel(
								UIActions.holdCamera(1512, 1968, 5),
								UIActions.cautionAt(1416, 1968, tilesize / 2f, 3.5f, Pal.heal),
								UIActions.cautionAt(1512, 2040, tilesize / 2f, 3.5f, Pal.heal),
								UIActions.labelAct(
									"[accent]Caution[]: @@@Don't destroy these [accent]Power Voids[] unless you have sufficient military power."
									, 0.75f, 4.26f, Interp.linear, t -> {
										t.image(Icon.warning).padRight(OFFSET);
									}
								)
							),
							UIActions.moveTo(1680, 1984, 1f, Interp.pow3),
							Actions.parallel(
								UIActions.holdCamera(1680, 1984, 4),
								UIActions.cautionAt(1864, 2032, tilesize * 3 / 2f, 3.5f, Pal.power),
								UIActions.cautionAt(1512, 2040, tilesize / 2f, 3.5f, Pal.heal),
								UIActions.labelAct(
									"[accent]Caution[]: @@@These [accent]Power Voids[] are linked to specific [sky]Jump Gates[]\nDestroy these voids will make these gates start to spawn units."
									, 0.75f, 3.25f, Interp.linear, t -> {
										t.image(NHBlocks.disposePowerVoid.fullIcon).size(LEN - OFFSET);
										t.image(Icon.rightOpen).size(LEN - OFFSET);
										t.image(NHBlocks.jumpGatePrimary.fullIcon).size(LEN - OFFSET).padRight(OFFSET);
									}
								)
							)
						);
						
						for(int i = 0; i < cores.size; i++){
							CoreBlock.CoreBuild core = cores.get(i);
							actions.add(UIActions.moveTo(core.x, core.y, 2f, Interp.smooth2));
							actions.add(Actions.parallel(
								UIActions.holdCamera(core.x, core.y, 3f),
								UIActions.labelAct(
										"Team<[#" + core.team.color +  "]" + core.team.name.toUpperCase() +
												"[]> : @@@" +
												core.block.localizedName + " [[" + core.tileX() + ", " + core.tileY() + "]", 0.5f, 2.5f, Interp.linear, t -> {
											if(!core.team.emoji.isEmpty()){
												t.add(core.team.emoji).padRight(OFFSET);
											}
										}
								),
								Actions.run(() -> {
									NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(player.team().core()), 120f, 60f, 15f, NHUnitTypes.naxos, 4);
								})
							));
						}
						
						actions.add(UIActions.endCutsceneDefault());
						
						
						UIActions.actionSeq(actions.toArray(Action.class));
					});
				}
			}));
			
			CutsceneScript.ender.put(this, Seq.with(
				bool -> {
					if(bool){
						state.rules.tags.clear();
						CutsceneEventEntity.events.clear();
					}
				}, b -> {}
			));
		}
			@Override
			public void loadIcon(){
				if(Icon.layers != null)uiIcon = fullIcon = Icon.layers.getRegion();
			}
		};
		
		downpour = new NHSectorPreset("downpour", NHPlanets.midantha, 55){{
			addStartingItems = true;
			captureWave = 80;
			difficulty = 5;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		luminariOutpost = new NHSectorPreset("luminari-outpost", NHPlanets.midantha, 102){{
			addStartingItems = true;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
			
			CutsceneScript.presentJS.put(this, loadJS(name));
		}};
		
		quantumCraters = new NHSectorPreset("quantum-craters", NHPlanets.midantha, 86){{
			addStartingItems = true;
			captureWave = 150;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			if(e.sector != null && e.sector.preset != null && captureMap.containsKey(e.sector.preset)){
				captureMap.get(e.sector.preset).get(e.sector);
			}
		});
		
		if(Vars.headless)return;
		
//		Events.on(EventType.SectorInvasionEvent.class, e -> {
//
//		});
//
		Events.on(EventType.SectorLoseEvent.class, e -> {
			Sector sector = e.sector;
			if(sector.preset != null && loseMap.containsKey(sector.preset)){
				loseMap.get(sector.preset).get(sector);
			}
		});
		
		Events.on(EventType.LoseEvent.class, e -> {
			if(!Vars.state.isGame() || !Vars.state.isCampaign())return;
			Sector sector = Vars.state.getSector();
			if(sector.preset != null && loseMap.containsKey(sector.preset)){
				loseMap.get(sector.preset).get(sector);
			}
		});
	}
	
	public static void resetSector(Sector sector){
		sector.save.delete();
		sector.save = null;
		sector.info = new SectorInfo();
	}
	
	public static class NHSectorPreset extends mindustry.type.SectorPreset{
		
		public NHSectorPreset(String name, Planet planet, int sector){
			super(name, planet, sector);
		}
		
		@Override
		public boolean isHidden(){
			return false;
		}
	}
}
