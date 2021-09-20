package newhorizon.content;

import arc.Events;
import arc.func.Cons;
import arc.math.Interp;
import arc.scene.Action;
import arc.scene.actions.Actions;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.game.EventType;
import mindustry.game.SectorInfo;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.SectorPreset;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.feature.SectorScript;
import newhorizon.func.NHFunc;

import static mindustry.Vars.*;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class NHSectorPresets implements ContentList{
	public static ObjectMap<SectorPreset, Cons<Sector>> captureMap = new ObjectMap<>(), loseMap = new ObjectMap<>();
	
	public static SectorPreset
		hostileHQ, downpour, luminariOutpost, quantumCraters, ruinedWarehouse, shatteredRavine, deltaHQ, mainPath, ancientBattefield;
	
	@Override
	public void load(){
		ancientBattefield = new NHSectorPreset("ancient-battefield", NHPlanets.midantha, 96){{
			addStartingItems = true;
			difficulty = 10;
		}};
		
		mainPath = new NHSectorPreset("main-path", NHPlanets.midantha, 11){{
			addStartingItems = true;
			useAI = true;
			difficulty = 12;
			
			loseMap.put(this, NHSectorPresets::resetSector);
			
			SectorScript.updaters.put(this, Seq.with(() -> {
				if(SectorScript.timer.get(0, 2400f) && sector.save != null && sector.save.meta != null && sector.save.meta.timePlayed > 3000f){
					if(state.rules.attackMode && state.rules.waveTeam.cores().any() && state.rules.waveTeam.cores().size < 3){
						CoreBlock.CoreBuild core = state.rules.waveTeam.core();
						SectorScript.UIActions.actionSeq(
							Actions.parallel(
								SectorScript.CautionAction.at(core.x, core.y, core.block.size * tilesize / 2f, 6f, core.team.color),
								Actions.run(() -> {
									NHSounds.alarm.play();
									NHFunc.spawnUnit(core, core.x, core.y, core.angleTo(player.team().core()), 120f, 360f, 30f, NHUnitTypes.destruction, 6);
								}),
								SectorScript.LabelAction.labelAct(
									"[accent]Caution[]: @@@Hostile Fleet Incoming."
									, 0.75f, 3.26f, false, Interp.linear, t -> {
										t.image(Icon.warning).padRight(OFFSET);
									}
								)
							)
						);
					}
				}
			}));
			
			SectorScript.initer.put(this, Seq.with(() -> {
				if(sector == null)return;
				if(SectorScript.canInit(sector)){
					Time.run(30f, () -> {
						SectorScript.UIActions.screenHold(2f, 16f, 1f, Interp.fastSlow, Interp.slowFast, 0);
						
						SectorScript.UIActions.actionSeq(
							Actions.run(SectorScript.UIActions::pauseCamera),
							Actions.delay(2f),
							SectorScript.CameraMoveAction.moveTo(316, 612, 1f, Interp.pow3Out),
							SectorScript.LabelAction.labelAct(
								"[accent]Objective[]: @@@Use the limited resources and these gates to defeat enemies."
								, 0.75f, 3.25f, false, Interp.linear, t -> {
									t.image(Icon.download).size(LEN - OFFSET);
									t.image(Icon.upOpen).size(LEN - OFFSET);
									t.image(NHBlocks.jumpGate.fullIcon).size(LEN - OFFSET).padRight(OFFSET);
								}
							),
							SectorScript.CameraMoveAction.moveTo(316, 3712, 6f, Interp.pow3Out),
							SectorScript.LabelAction.labelAct(
								"[accent]Objective[]: @@@Destroy Enemy Base!"
								, 0.75f, 4.75f, false, Interp.linear, t -> {
									t.image(Icon.warning).padRight(OFFSET);
								}
							),
							Actions.run(SectorScript.UIActions::resumeCamera)
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
			
			SectorScript.updaters.put(this, Seq.with(() -> {
				if(SectorScript.timer.get(0, 1800f) && sector.save != null && sector.save.meta != null && sector.save.meta.timePlayed > 3000f){
					if(state.rules.attackMode && state.rules.waveTeam.cores().size == 1){
						CoreBlock.CoreBuild core = state.rules.waveTeam.core();
						SectorScript.UIActions.actionSeq(
							Actions.parallel(
								SectorScript.CautionAction.at(core.x, core.y, core.block.size * tilesize / 2f, 6f, core.team.color),
								Actions.run(() -> {
									NHSounds.alarm.play();
									NHFunc.spawnUnit(core, core.x, core.y, core.angleTo(player.team().core()), 120f, 300f, 30f, NHUnitTypes.destruction, 4);
									NHFunc.spawnUnit(core, core.x, core.y, core.angleTo(player.team().core()), 120f, 240f, 15f, NHUnitTypes.striker, 6);
									NHFunc.spawnUnit(core, core.x, core.y, core.angleTo(player.team().core()), 120f, 300f, 60f, NHUnitTypes.hurricane, 2);
								}),
								SectorScript.LabelAction.labelAct(
									"[accent]Caution[]: @@@Hostile Fleet Incoming."
									, 0.75f, 3.26f, false, Interp.linear, t -> {
										t.image(Icon.warning).padRight(OFFSET);
									}
								)
							)
						);
					}
				}
			}));
			
			SectorScript.initer.put(this, Seq.with(() -> {
				if(sector == null)return;
				if(SectorScript.canInit(sector)){
					Time.run(30f, () -> {
						Seq<CoreBlock.CoreBuild> cores = state.teams.cores(state.rules.waveTeam);
						Seq<Action> actions = new Seq<>(cores.size * 2);
						
						actions.add(Actions.run(SectorScript.UIActions::pauseCamera), Actions.delay(2f));
						
						actions.addAll(
							SectorScript.CameraMoveAction.moveTo(1512, 1968, 2f, Interp.pow3Out),
							Actions.parallel(
								SectorScript.CautionAction.at(1416, 1968, tilesize / 2f, 3.5f, Pal.heal),
								SectorScript.CautionAction.at(1512, 2040, tilesize / 2f, 3.5f, Pal.heal),
								SectorScript.LabelAction.labelAct(
									"[accent]Caution[]: @@@Don't destroy these [accent]Power Voids[] unless you have sufficient military power."
									, 0.75f, 4.26f, false, Interp.linear, t -> {
										t.image(Icon.warning).padRight(OFFSET);
									}
								)
							),
							SectorScript.CameraMoveAction.moveTo(1680, 1984, 1f, Interp.pow3Out),
							Actions.parallel(
								SectorScript.CautionAction.at(1864, 2032, tilesize * 3 / 2f, 3.5f, Pal.power),
								SectorScript.CautionAction.at(1512, 2040, tilesize / 2f, 3.5f, Pal.heal),
								SectorScript.LabelAction.labelAct(
									"[accent]Caution[]: @@@These [accent]Power Voids[] are linked to specific [sky]Jump Gates[]\nDestroy these voids will make these gates start to spawn units."
									, 0.75f, 3.25f, false, Interp.linear, t -> {
										t.image(NHBlocks.disposePowerVoid.fullIcon).size(LEN - OFFSET);
										t.image(Icon.rightOpen).size(LEN - OFFSET);
										t.image(NHBlocks.jumpGatePrimary.fullIcon).size(LEN - OFFSET).padRight(OFFSET);
									}
								)
							)
						);
						
						for(int i = 0; i < cores.size; i++){
							CoreBlock.CoreBuild core = cores.get(i);
							actions.add(SectorScript.CameraMoveAction.moveTo(core.x, core.y, 2f, Interp.circleOut));
							actions.add(Actions.parallel(
									SectorScript.LabelAction.labelAct(
											"Team<[#" + core.team.color +  "]" + core.team.name.toUpperCase() +
													"[]> : @@@" +
													core.block.localizedName + " [[" + core.tileX() + ", " + core.tileY() + "]", 0.5f, 2.5f, false, Interp.linear, t -> {
												if(!core.team.emoji.isEmpty()){
													t.add(core.team.emoji).padRight(OFFSET);
												}
											}
									),
									Actions.run(() -> {
										NHFunc.spawnUnit(core, core.x, core.y, core.angleTo(player.team().core()), 120f, 60f, 15f, NHUnitTypes.naxos, 4);
									})
							));
						}
						
						actions.add(Actions.run(SectorScript.UIActions::resumeCamera));
						
						SectorScript.UIActions.screenHold(2f, actions.size * 2, 1f, Interp.fastSlow, Interp.slowFast, 0);
						
						SectorScript.UIActions.actionSeq(actions.toArray(Action.class));
					});
				}
			}));
			
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
		}};
		
		quantumCraters = new NHSectorPreset("quantum-craters", NHPlanets.midantha, 86){{
			addStartingItems = true;
			captureWave = 150;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			if(e.sector != null && captureMap.containsKey(e.sector.preset)){
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
