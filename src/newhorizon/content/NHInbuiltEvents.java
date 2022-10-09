package newhorizon.content;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.entities.pattern.ShootSummon;
import mindustry.game.EventType;
import newhorizon.NHGroups;
import newhorizon.NewHorizon;
import newhorizon.expand.eventsys.*;
import newhorizon.util.annotation.ClientDisabled;
import newhorizon.util.func.OV_Pair;

public class NHInbuiltEvents{
	public static WorldEventType
		intervention_std;
	
	@ClientDisabled
	public static final Seq<AutoEventTrigger> autoTriggers = new Seq<>();
	
	private static void loadEventTriggers(){
		autoTriggers.addAll(new AutoEventTrigger(){{
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
			spacingBase = 2000 * 60;
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
			eventType = WorldEventType.inbuilt(new RaidEventType("inbuilt-raid-arc"){{
				ShootPattern shootPattern = new ShootMulti(new ShootSummon(0, 0, 40, 0){{
					shots = 4;
					shotDelay = 18f;
				}}, new ShootSpread(){{
					shots = 1;
					spread = 8f;
				}});
				
				ammo(NHBullets.arc_9000, shootPattern);
				radius = 200;
				reloadTime = 300 * 60;
			}});
			
			minTriggerWave = 0;
			spacingBase = 2400 * 60;
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
		
		if(Vars.headless || NewHorizon.DEBUGGING){
			Events.on(EventType.WorldLoadEvent.class, e -> {
				Core.app.post(() -> Core.app.post(() -> Core.app.post(() -> {
					if(!Vars.state.rules.pvp && NHGroups.autoEventTrigger.size() < autoTriggers.size){
						EventHandler.runEventOnce("setup-events", () -> autoTriggers.each(t -> t.copy().add()));
					}
				})));
			});
		}
	}
	
	public static void load(){
		loadEventTriggers();
		
		intervention_std = new InterventionEventType("intervention_std"){{
			spawn(NHUnitTypes.branch, 5);
		}};
		
		WorldEventType.addInbuilt(intervention_std);
	}
}
