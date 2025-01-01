package newhorizon.content;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.Mover;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.entities.pattern.ShootSummon;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.graphics.MinimapRenderer;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.modules.ItemModule;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.NewHorizon;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.cutscene.actions.CSSActions;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.AutoEventTrigger;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.expand.eventsys.types.*;
import newhorizon.util.annotation.ClientDisabled;
import newhorizon.util.func.NHFunc;
import newhorizon.util.struct.OV_Pair;

import static mindustry.Vars.tilesize;

public class NHInbuiltEvents{
	public static final String APPLY_KEY = "applyTriggers";
	
	public static WorldEventType
		raidDifficult_1, raidDifficult_2, raidDifficult_3, raidDifficult_4, raidDifficult_ent, raidErase,
		intervention_std, raidQuick, raidAncientAccurate, raidArtillery;
	
	@ClientDisabled
	public static final Seq<AutoEventTrigger> autoTriggers = new Seq<>(), campaignTriggers = new Seq<>();
	
	public static ItemStack[][] difficultCheck = {
		ItemStack.with(NHItems.ancimembrane, 8000, NHItems.darkEnergy, 2500), //9
		ItemStack.with(NHItems.ancimembrane, 2500, NHItems.darkEnergy, 1500), //8
		ItemStack.with(NHItems.upgradeSort, 2500), //7
		ItemStack.with(NHItems.irayrondPanel, 800, NHItems.seniorProcessor, 600, Items.surgeAlloy, 1200), //6
		ItemStack.with(Items.surgeAlloy, 2500, NHItems.multipleSteel, 3200, Items.plastanium, 2400), //5
		ItemStack.with(NHItems.multipleSteel, 3200, Items.plastanium, 2400, NHItems.presstanium, 3000, NHItems.juniorProcessor, 2500), //4
		ItemStack.with(NHItems.presstanium, 3000, NHItems.juniorProcessor, 2500), //3
		ItemStack.with(Items.silicon, 3000, Items.thorium, 2000), //2
		ItemStack.with(Items.copper, 5), //1
	};
	
	//Check team core for data, should be quick
	public static int dynamicGrowth(Team team){
		int difficult = 0;
		
		if(!team.active())return 0;
		CoreBlock.CoreBuild core = team.cores().firstOpt();
		if(core == null)return 0;
		ItemModule items = core.items;
		
		for(int i = 0; i < difficultCheck.length; i++){
			ItemStack[] stacks = difficultCheck[i];
			if(items.has(stacks))return difficultCheck.length - i;
		}
		
		return difficult;
	}
	
	public static BulletType copyAnd(BulletType source, Cons<BulletType> modifier){
		BulletType n = source.copy();
		modifier.get(n);
		return n;
	}
	
	@SuppressWarnings("DuplicateBranchesInSwitch")
	public static WorldEventType dynamicRaid(int difficult){
		switch(difficult){
			case 0 : return intervention_std;
			case 1 : return raidDifficult_1;
			case 2 : return raidDifficult_2;
			case 3 : return raidDifficult_3;
			case 4 : return raidDifficult_4;
			case 5 : return raidAncientAccurate;
			case 6 : return raidArtillery;
			case 7 : return raidQuick;
			case 8 : return raidDifficult_ent;
			case 9 : return raidErase;
			default : return intervention_std;
		}
	};
	
	private static void loadEventTriggers(){
		raidErase = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-erase"){{
			ammo(NHBullets.pesterBlackHole, new ShootPattern());
			radius = 280;
			reloadTime = 30 * 60;
			callSound = NHSounds.alarm;
		}
			
			@Override
			public void drawArrow(WorldEvent e){}
			
			@Override
			protected void bullet(WorldEvent e, Team team, BulletType bullet, Position source, Position target, Mover mover){
				bullet.create(e, team, target.getX(), target.getY(), 0, 10000, 0.0001f, 1, radius, mover, target.getX(), target.getY());
			}
		});
		
		raidDifficult_1 = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-difficult-1"){{
			ShootPattern shootPattern = new ShootPattern(){{
				shots = 36;
				shotDelay = 4f;
			}};
			
			inaccuracy = 0;
			ammo(copyAnd(NHBullets.synchroThermoPst, b -> {
				b.collides = b.collidesAir = false;
				b.scaleLife = true;
				b.scaledSplashDamage = true;
				b.splashDamage += b.damage * 1.5f;
				b.lightning += 1;
				b.lightningLength += 4;
			}), shootPattern);
			radius = 80;
			reloadTime = 15 * 60;
		}});
		
		raidDifficult_2 = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-difficult-2"){{
			ShootPattern shootPattern = new ShootPattern(){{
				shots = 24;
				shotDelay = 8f;
			}};
			
			inaccuracy = 0.145f;
			ammo(copyAnd(NHBullets.saviourBullet, b -> {
				b.collidesAir = false;
				b.scaleLife = true;
				b.scaledSplashDamage = true;
				b.splashDamage += b.damage;
			}), shootPattern);
			radius = 160;
			reloadTime = 15 * 60;
		}});
		
		raidDifficult_3 = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-difficult-3"){{
			ammo(copyAnd(NHBullets.blastEnergyNgt, b -> {
				b.collides = b.collidesAir = false;
				b.scaleLife = true;
				b.scaledSplashDamage = true;
				b.splashDamage += b.damage;
				b.lightningDamage = b.damage / 2;
				b.splashDamageRadius = 56f;
				b.lightning = 4;
				b.lightningLength = 3;
				b.lightningLengthRand = 14;
			}), new ShootPattern(){{
				shots = 24;
				shotDelay = 6f;
			}});
			radius = 80;
			inaccuracy = 0;
			reloadTime = 20 * 60;
		}});
		
		raidDifficult_4 = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-difficult-4"){{
			ammo(copyAnd(NHBullets.railGun3, b -> {
				b.collides = b.collidesAir = false;
				b.scaleLife = true;
				b.scaledSplashDamage = true;
				b.splashDamage = 150;
				b.splashDamageRadius = 60;
				b.despawnEffect = NHFx.hitSparkHuge;
			}), new ShootPattern(){{
				shots = 16;
				shotDelay = 12f;
			}});
			radius = 80;
			inaccuracy = 0;
			reloadTime = 30 * 60;
		}});
		
		raidDifficult_ent = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-eternity"){{
			ammo(NHBullets.eternity, new ShootPattern(){{
				firstShotDelay = 25f;
			}}, NHBullets.shieldDestroyer, new ShootPattern(){{
				shots = 10;
				shotDelay = 6f;
			}});
			radius = 80;
			inaccuracy = 0;
			reloadTime = 30 * 60;
		}});
		
		raidArtillery = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-artillery"){{
			ShootPattern shootPattern = new ShootMulti(new ShootSummon(0, 0, 0, 0){{
				shots = 8;
				shotDelay = 18f;
			}}, new ShootSpread(){{
				shots = 6;
				spread = 2f;
				shotDelay = 3f;
			}});
			
			ammo(NHBullets.declineProjectile, shootPattern);
			radius = 230;
			reloadTime = 20 * 60;
		}});
		
		raidAncientAccurate = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-ancientAccurate"){{
			ShootPattern shootPattern = new ShootPattern(){{
				shots = 2;
				shotDelay = 16f;
			}};
			
			velocityRnd /= 3f;
			inaccuracy = 0.145f;
			ammo(NHBullets.ancientArtilleryProjectile, shootPattern, NHBullets.shieldDestroyer, shootPattern);
			radius = 80;
			reloadTime = 15 * 60;
		}});
		
		raidQuick = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-quick"){{
			ShootPattern shootPattern = new ShootMulti(new ShootSummon(0, 0, 40, 0){{
				shots = 6;
				shotDelay = 18f;
				firstShotDelay = 12;
			}}, new ShootSpread(){{
				shots = 8;
				spread = 3f;
				shotDelay = 3f;
			}});
			
			inaccuracy = 0;
			
			ammo(NHBullets.airRaidBomb, shootPattern, NHBullets.shieldDestroyer, new ShootSummon(0, 0, 80, 0){{
				shots = 3;
			}});
			radius = 200;
			reloadTime = 3 * 60;
		}});
		
		AutoEventTrigger wave1 = new AutoEventTrigger(){{
			disposable = triggerAfterAdd = true;
			reload = spacing = 10000;
			spacingBase = spacingRand = 0;
			items = OV_Pair.seqWith();
			units = OV_Pair.seqWith();
			buildings = OV_Pair.seqWith();
			minTriggerWave = 0;
			eventType = WorldEventType.inbuilt(new ReachWaveEvent("inbuilt-wave-10"){{
				targetWave = 10;
				toTrigger = new InterventionEventType("wave10-reward"){{
					spawn(NHUnitTypes.rhino, 1, NHUnitTypes.gather, 2);
					removeAfterTrigger = true;
					defaultTeam = () -> Vars.state.rules.defaultTeam;
				}
					@Override public Position target(WorldEvent e){
						Position p = defaultTeam.get().core();
						return p == null ? new Vec2().set(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f) : p;
					}
					
					@Override
					public Position source(WorldEvent event){
						Position t = target(event);
						if(t != null && Vars.spawner.getFirstSpawn() != null){
							Tmp.v1.set(t).sub(Vars.spawner.getFirstSpawn()).nor().add(t);
							return Tmp.v1;
						}else return Vec2.ZERO;
					}
				};
			}});
		}};
		
		AutoEventTrigger wave2 = new AutoEventTrigger(){{
			disposable = triggerAfterAdd = true;
			reload = spacing = 10000;
			spacingBase = spacingRand = 0;
			items = OV_Pair.seqWith();
			units = OV_Pair.seqWith();
			buildings = OV_Pair.seqWith();
			minTriggerWave = 0;
			eventType = WorldEventType.inbuilt(new ReachWaveEvent("inbuilt-wave-55"){{
				targetWave = 55;
				toTrigger = new InterventionEventType("wave55-reward"){{
					spawn(NHUnitTypes.saviour, 1, NHUnitTypes.naxos, 2);
					removeAfterTrigger = true;
					defaultTeam = () -> Vars.state.rules.defaultTeam;
				}
					@Override public Position target(WorldEvent e){
						Position p = defaultTeam.get().core();
						return p == null ? new Vec2().set(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f) : p;
					}
					
					@Override
					public Position source(WorldEvent event){
						Position t = target(event);
						if(t != null && Vars.spawner.getFirstSpawn() != null){
							Tmp.v1.set(t).sub(Vars.spawner.getFirstSpawn()).nor().add(t);
							return Tmp.v1;
						}else return Vec2.ZERO;
					}
				};
			}});
		}};
		
		AutoEventTrigger wave3 = new AutoEventTrigger(){{
			disposable = triggerAfterAdd = true;
			reload = spacing = 10000;
			spacingBase = spacingRand = 0;
			items = OV_Pair.seqWith();
			units = OV_Pair.seqWith();
			buildings = OV_Pair.seqWith();
			minTriggerWave = 0;
			eventType = WorldEventType.inbuilt(new ReachWaveEvent("inbuilt-wave-100"){{
				targetWave = 100;
				toTrigger = new InterventionEventType("wave100-reward"){{
					spawn(NHUnitTypes.hurricane, 2, NHUnitTypes.longinus, 6);
					removeAfterTrigger = true;
					defaultTeam = () -> Vars.state.rules.defaultTeam;
				}
					@Override public Position target(WorldEvent e){
						Position p = defaultTeam.get().core();
						return p == null ? new Vec2().set(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f) : p;
					}
					
					@Override
					public Position source(WorldEvent event){
						Position t = target(event);
						if(t != null && Vars.spawner.getFirstSpawn() != null){
							Tmp.v1.set(t).sub(Vars.spawner.getFirstSpawn()).nor().add(t);
							return Tmp.v1;
						}else return Vec2.ZERO;
					}
				};
			}});
		}};
		
		autoTriggers.add(wave1, wave2, wave3);
		campaignTriggers.add(wave1, wave2, wave3);
		
		{
			autoTriggers.addAll(new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.juniorProcessor, 800);
				eventType = WorldEventType.inbuilt(new InterventionEventType("probe-inbound"){{
					spawn(NHUnitTypes.ancientProbe, 1);
					callSound = Sounds.none;
					
					drawable = minimapMarkable = false;
					warnOnHUD = false;
					spawnRange = 12f;
					reloadTime = 30f;
				}
					@Override
					public void warnOnTrigger(WorldEvent event){
					}
					
					@Override
					public void buildTable(WorldEvent e, Table table){
						if(Vars.state.rules.infiniteResources)super.buildTable(e, table);
					}
					
					@Override
					public Table buildSimpleTable(WorldEvent e){
						if(Vars.state.rules.infiniteResources)return super.buildSimpleTable(e);
						else return new Table(t -> t.setSize(0, 0));
					}
				});
				
				minTriggerWave = 0;
				spacingBase = 120 * 60;
				spacingRand = 240 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.metalOxhydrigen, 1000, NHItems.presstanium, 1000);
				units = OV_Pair.seqWith(NHUnitTypes.gather, 5);
				eventType = WorldEventType.inbuilt(new WeatherEvent("inbuilt-weather-sun-storm", NHWeathers.solarStorm, Pal.ammo));
				
				minTriggerWave = 0;
				spacingBase = 900 * 60;
				spacingRand = 600 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.seniorProcessor, 1000, NHItems.irayrondPanel, 1000);
				buildings = OV_Pair.seqWith(NHBlocks.jumpGateJunior, 1);
				eventType = WorldEventType.inbuilt(new WeatherEvent("inbuilt-weather-quantum-storm", NHWeathers.quantumStorm, NHColor.darkEnrColor));
				
				minTriggerWave = 0;
				spacingBase = 1500 * 60;
				spacingRand = 600 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.ancimembrane, 1200);
				eventType = raidQuick;
				
				minTriggerWave = 0;
				spacingBase = 2400 * 60;
				spacingRand = 800 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.seniorProcessor, 1200, NHItems.presstanium, 5000, NHItems.upgradeSort, 500);
				eventType = raidArtillery;
				
				minTriggerWave = 0;
				spacingBase = 1800 * 60;
				spacingRand = 600 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.multipleSteel, 1500, NHItems.presstanium, 1000, Items.plastanium, 1000);
				eventType = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-std"){{
					ShootPattern shootPattern = new ShootMulti(new ShootSummon(0, 0, 30, 0){{
						shots = 8;
						shotDelay = 18f;
					}}, new ShootSpread(){{
						shots = 30;
						spread = 8f;
						shotDelay = 4f;
					}});
					
					ammo(NHBullets.synchroFusion, shootPattern, NHBullets.synchroThermoPst, shootPattern);
					radius = 230;
					reloadTime = 300 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 1200 * 60;
				spacingRand = 300 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.setonAlloy, 2000, NHItems.darkEnergy, 2000);
				eventType = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-raid"){{
					ammo(NHBullets.airRaidBomb, new ShootMulti(new ShootSummon(0, 0, 220, 0){{
						shots = 6;
						shotDelay = 18f;
					}}, new ShootSpread(){{
						shots = 12;
						spread = 8f;
						shotDelay = 4;
					}}));
					reloadTime = 420 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 1800 * 60;
				spacingRand = 600 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.irayrondPanel, 1500, NHItems.presstanium, 3000, Items.phaseFabric, 100);
				eventType = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-sav"){{
					ShootPattern shootPattern = new ShootMulti(new ShootSummon(0, 0, 40, 0){{
						shots = 8;
						shotDelay = 18f;
					}}, new ShootSpread(){{
						shots = 30;
						spread = 8f;
						shotDelay = 8f;
					}});
					
					ammo(NHBullets.saviourBullet, shootPattern);
					radius = 340;
					reloadTime = 300 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 1800 * 60;
				spacingRand = 120 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.darkEnergy, 1500, NHItems.upgradeSort, 3000);
				eventType = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-ancient"){{
					ShootPattern shootPattern = new ShootMulti(new ShootSummon(0, 0, 80, 0){{
						shots = 5;
						shotDelay = 12f;
					}}, new ShootSpread(){{
						shots = 1;
						spread = 8f;
					}});
					
					ammo(NHBullets.ancientArtilleryProjectile, shootPattern);
					radius = 200;
					reloadTime = 300 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 2400 * 60;
				spacingRand = 120 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.upgradeSort, 3000, NHItems.thermoCorePositive, 2300, NHItems.thermoCoreNegative, 2300);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-collapser"){{
					spawn(NHUnitTypes.collapser, 1);
					reloadTime = 30 * 60;
					status = StatusEffects.overdrive;
				}});
				
				spacingBase = 1200 * 60;
				spacingRand = 300 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.multipleSteel, 1500, NHItems.seniorProcessor, 800);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-gunship"){{
					spawn(NHUnitTypes.macrophage, 4);
					reloadTime = 30 * 60;
				}});
				
				spacingBase = 900 * 60;
				spacingRand = 600 * 60;
			}}, new AutoEventTrigger(){{
				buildings = OV_Pair.seqWith(NHBlocks.jumpGate, 1);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-destruction"){{
					spawn(NHUnitTypes.destruction, 3, NHUnitTypes.naxos, 2);
					reloadTime = 30 * 60;
				}});
				
				spacingBase = 1500 * 60
				;
				spacingRand = 300 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.darkEnergy, 1000);
				buildings = OV_Pair.seqWith(NHBlocks.eternity, 5);
				units = OV_Pair.seqWith(NHUnitTypes.pester, 2);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-ancient-fleet"){
					{
						spawn(NHUnitTypes.nucleoid, 1, NHUnitTypes.pester, 2, NHUnitTypes.guardian, 5);
						reloadTime = 45 * 60;
						status = NHStatusEffects.overphased;
						callSound = NHSounds.alarm;
					}
					
					@Override
					public void drawMinimap(WorldEvent event, MinimapRenderer minimap){
						minimap.transform(Tmp.v1.set(event.x, event.y));
						
						float rad = minimap.scale(range(event));
						float fin = Interp.pow2Out.apply((Time.globalTime / 100f) % 1f);
						
						Draw.color(Tmp.c1.set(event.team.color).lerp(Color.white, Mathf.absin(Time.globalTime, 4f, 0.4f)));
						
						float size = minimap.scale((float)NHContent.pointerRegion.width / tilesize * 3f);
						Draw.rect(NHContent.pointerRegion, Tmp.v1.x, Tmp.v1.y, size, size);
						
						Lines.stroke(Scl.scl((1f - fin) * 4.5f + 0.15f));
						Lines.circle(Tmp.v1.x, Tmp.v1.y, rad * fin);
						
						fin = Interp.circleOut.apply((Time.globalTime / 50f) % 1f);
						Lines.stroke(Scl.scl((1f - fin) * 2.5f));
						Lines.circle(Tmp.v1.x, Tmp.v1.y, rad);
						
						Draw.reset();
					}
					
					@Override
					public void draw(WorldEvent e){
						super.draw(e);
						Team team = e.team;
						float fin = progressRatio(e);
						float fout = 1 - fin;
						float scl = Interp.pow3Out.apply(Mathf.curve(fout, 0, 0.01f));
						
						Draw.blend(Blending.additive);
						Draw.z(Layer.legUnit + 1);
						Draw.color(team.color, Color.white, 0.075f);
						Draw.alpha(0.65f);
						
						TextureRegion arrowRegion = NHContent.arrowRegion;
						
						float regSize = 1.1f;
						for(int l : Mathf.signs){
							float angle = 90 + 90 * l;
							for(int i = 0; i < 4; i++){
								Tmp.v1.trns(angle, i * 50 + spawnRange * 1.22f);
								float f = (100 - (Time.time + 25 * i) % 100) / 100;
								
								Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, arrowRegion.width * regSize * f * scl, arrowRegion.height * regSize * f * scl, angle + 90);
							}
						}
						
						Draw.reset();
						Draw.blend();
					}
					
					@Override
					public void trigger(WorldEvent e){
						Team team = e.team;
						float x = e.x, y = e.y;
						
						e.reload = 0;
						
						float angle = source(e).angleTo(e);
						
						CSSActions.beginCreateAction();
						
						NHCSS_Core.core.applyMainBus(
							CSSActions.pack(
								CSSActions.pullCurtain(),
								CSSActions.cameraScl(Vars.headless ? 1 : Vars.renderer.minScale()), CSSActions.cameraMove(x, y),
								CSSActions.parallel(
									CSSActions.text("{TOKEN=BLINK}[lightgray]INBOUND ENEMIES"),
									CSSActions.text("{TOKEN=GRADIENT}[ancient]Ancient Flagships[lightgray] Approaching[]"),
									CSSActions.cameraSustain(35f)
								),
								CSSActions.parallel(
									CSSActions.delay(90f), CSSActions.runnable(() -> {
										for(ObjectIntMap.Entry<UnitType> spawn : spawner.entries()){
											NHFunc.spawnUnit(team, e.x, e.y, angle, spawnRange, 150f, 15f, spawn.key, Math.min(spawn.value, Units.getCap(team) - team.data().countType(spawn.key)), status, statusDuration);
										}
									})),
								CSSActions.cameraReturn(),
								CSSActions.withdrawCurtain()));

						CSSActions.endCreateAction();
						
						
						if(removeAfterTrigger) e.remove();
						else e.set(target(e));
					}
				});
				
				spacingBase = 60 * 60;
				spacingRand = 0;
				disposable = true;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.darkEnergy, 1000);
				units = OV_Pair.seqWith(NHUnitTypes.pester, 1);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-pester"){{
					spawn(NHUnitTypes.pester, 1);
					reloadTime = 30 * 60;
					
				}});
				spacingBase = 3600 * 60;
				spacingRand = 600 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(Items.plastanium, 1000, NHItems.metalOxhydrigen, 400);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-0"){{
					spawn(NHUnitTypes.warper, 8, NHUnitTypes.assaulter, 4, NHUnitTypes.branch, 4);
					reloadTime = 30 * 60;
					
				}});
				spacingBase = 480 * 60;
				spacingRand = 60 * 60;
			}}, new AutoEventTrigger(){{
				buildings = OV_Pair.seqWith(NHBlocks.jumpGate, 1);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-1"){{
					spawn(NHUnitTypes.naxos, 2, NHUnitTypes.branch, 4, NHUnitTypes.warper, 10, NHUnitTypes.assaulter, 4);
					reloadTime = 30 * 60;
				}});
				
				minTriggerWave = 25;
				spacingBase = 600 * 60;
				spacingRand = 300 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(Items.thorium, 50, NHItems.zeta, 80, NHItems.presstanium, 30);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-2"){{
					spawn(NHUnitTypes.branch, 4, NHUnitTypes.sharp, 4);
					reloadTime = 15 * 60;
				}});
				
				
				spacingBase = 180 * 60;
				spacingRand = 180 * 60;
			}}, new AutoEventTrigger(){{
				units = OV_Pair.seqWith(NHUnitTypes.guardian, 1);
				items = OV_Pair.seqWith(NHItems.darkEnergy, 2000);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-3"){{
					spawn(NHUnitTypes.guardian, 2);
					reloadTime = 45 * 60;
				}});
				
				minTriggerWave = 35;
				spacingBase = 1800 * 60;
				spacingRand = 120 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(NHItems.upgradeSort, 3000, NHItems.darkEnergy, 1000);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-4"){{
					spawn(NHUnitTypes.longinus, 4, NHUnitTypes.naxos, 10, NHUnitTypes.saviour, 2);
					reloadTime = 45 * 60;
				}});
				
				minTriggerWave = 35;
				spacingBase = 1200 * 60;
				spacingRand = 300 * 60;
			}}, new AutoEventTrigger(){{
				items = OV_Pair.seqWith(Items.graphite, 800, Items.silicon, 800, Items.thorium, 1000);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-5"){{
					spawn(UnitTypes.horizon, 20, NHUnitTypes.sharp, 6);
					reloadTime = 45 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 240 * 60;
				spacingRand = 60 * 60;
			}}, new AutoEventTrigger(){{
				buildings = OV_Pair.seqWith(NHBlocks.jumpGate, 1);
				items = OV_Pair.seqWith(NHItems.juniorProcessor, 800, NHItems.presstanium, 800, NHItems.multipleSteel, 400);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-6"){{
					spawn(NHUnitTypes.warper, 4, NHUnitTypes.sharp, 6);
					reloadTime = 120 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 360 * 60;
				spacingRand = 360 * 60;
			}}, new AutoEventTrigger(){{
				buildings = OV_Pair.seqWith(NHBlocks.jumpGate, 1);
				items = OV_Pair.seqWith(NHItems.seniorProcessor, 1200, NHItems.irayrondPanel, 800, NHItems.setonAlloy, 400, NHItems.upgradeSort, 200);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-7"){{
					spawn(NHUnitTypes.saviour, 2, NHUnitTypes.naxos, 2);
					reloadTime = 60 * 60;
				}});
				
				minTriggerWave = 0;
				spacingBase = 600 * 60;
				spacingRand = 240 * 60;
			}}, new AutoEventTrigger(){{
				buildings = OV_Pair.seqWith(NHBlocks.jumpGate, 1);
				items = OV_Pair.seqWith(NHItems.upgradeSort, 1200, NHItems.setonAlloy, 800, NHItems.seniorProcessor, 400);
				eventType = WorldEventType.inbuilt(new InterventionEventType("inbuilt-inbound-8"){{
					spawn(NHUnitTypes.anvil, 1);
					reloadTime = 60 * 60;
					
				}});
				
				minTriggerWave = 0;
				spacingBase = 600 * 60;
				spacingRand = 300 * 60;
			}});
		}
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(Vars.state.isCampaign() && Vars.state.rules.sector.isCaptured()){
				NHGroups.events.clear();
				NHGroups.autoEventTrigger.clear();
				Vars.state.rules.tags.remove(APPLY_KEY);
				return;
			}
			
			Core.app.post(() -> {
				if(Vars.state.isMenu() || Vars.net.client() || Vars.state.isEditor() || Vars.state.rules.pvp || (Vars.state.rules.infiniteResources && !NewHorizon.DEBUGGING) || NHGroups.autoEventTrigger.size() >= autoTriggers.size)return;
				if(Vars.headless || (NewHorizon.DEBUGGING && !Vars.net.client())){
					Core.app.post(() -> Core.app.post(() -> Core.app.post(() -> {
						if(!Vars.state.rules.pvp)EventHandler.runEventOnce("setup-triggers", () -> {
							if(NHGroups.autoEventTrigger.isEmpty()){
								autoTriggers.each(t -> t.copy().add());
							}
						});
					})));
				}else if(Vars.state.isCampaign() && Vars.state.rules.sector.planet == NHPlanets.midantha){
					Core.app.post(() -> {
						if(Float.isNaN(NHVars.worldData.eventReloadSpeed) || NHVars.worldData.eventReloadSpeed < 0)NHVars.worldData.eventReloadSpeed = 0.55f;
						if(Vars.state.isCampaign() && Vars.state.rules.tags.containsKey(APPLY_KEY) && !Vars.state.rules.sector.isCaptured()){
							if(NHGroups.autoEventTrigger.isEmpty())autoTriggers.each(t -> t.copy().add());
						}
					});
				}else Core.app.post(() -> Core.app.post(() -> {
					NHVars.worldData.eventReloadSpeed = 0.55f;
					if(NHVars.worldData.applyEventTriggers){
						if(NHGroups.autoEventTrigger.isEmpty())autoTriggers.each(t -> t.copy().add());
					}
				}));
			});
			
		});
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			NHGroups.events.clear();
			NHGroups.autoEventTrigger.clear();
		});
	}
	
	public static void load(){
		loadEventTriggers();
		
		intervention_std = new InterventionEventType("intervention_std"){{
			spawn(NHUnitTypes.branch, 5);
		}};
		
		WorldEventType.addInbuilt(intervention_std);
	}
}
