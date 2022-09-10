package newhorizon.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;

import static mindustry.content.TechTree.*;

public class NHTechTree{
	public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
	
	public static void nodeUnit(UnitType type, Runnable children){
		node(type, unitBuildCost.get(type), children);
	}
	
	public static void load(){
		nodeRoot("new-horizon", NHBlocks.presstaniumFactory, () -> {
			node(NHBlocks.zetaGenerator, () -> {
				node(NHBlocks.hyperGenerator);
			});
			
			node(NHBlocks.armorPowerNode, () -> {
				node(NHBlocks.heavyPowerNode);
				node(NHBlocks.largeMendProjector, () -> {
					node(NHBlocks.gravityTrapSmall, () -> {
						node(NHBlocks.hyperspaceWarper);
						node(NHBlocks.gravityTrap);
					});
				});
				node(NHBlocks.largeShieldGenerator);
				node(NHBlocks.armorBatteryLarge, () -> {
					node(NHBlocks.hugeBattery);
				});
				node(NHBlocks.largeWaterExtractor, () -> {
					node(NHBlocks.beamDrill);
				});
			});
			
			node(NHBlocks.insulatedWall, () -> {
				node(NHBlocks.fireExtinguisher);
				node(NHBlocks.heavyDefenceWall, () -> {
					node(NHBlocks.heavyDefenceWallLarge);
					node(NHBlocks.heavyDefenceDoor, () -> {
						node(NHBlocks.heavyDefenceDoorLarge);
					});
					node(NHBlocks.setonWall, () -> {
						node(NHBlocks.setonWallLarge, () -> {
							node(NHBlocks.chargeWall, () -> {
								node(NHBlocks.shapedWall);
								node(NHBlocks.chargeWallLarge);
							});
						});
					});
				});
			});
			
			node(NHBlocks.jumpGatePrimary, () -> {
				node(NHBlocks.jumpGateJunior, () -> {
					node(NHBlocks.jumpGate);
				});
				nodeUnit(NHUnitTypes.origin, () -> {
					nodeUnit(NHUnitTypes.thynomo, () -> {
						nodeUnit(NHUnitTypes.aliotiat, () -> {
							nodeUnit(NHUnitTypes.tarlidor, () -> {
								nodeUnit(NHUnitTypes.annihilation, () -> {
									nodeUnit(NHUnitTypes.sin, () -> {});
								});
							});
						});
					});
				});
				nodeUnit(NHUnitTypes.assaulter, () -> {
					nodeUnit(NHUnitTypes.anvil, () -> {});
				});
				nodeUnit(NHUnitTypes.sharp, () -> {
					nodeUnit(NHUnitTypes.branch, () -> {
						nodeUnit(NHUnitTypes.warper, () -> {
							nodeUnit(NHUnitTypes.naxos, () -> {
								nodeUnit(NHUnitTypes.longinus, () -> {});
							});
						});
					});
				});
				nodeUnit(NHUnitTypes.ghost, () -> {
					nodeUnit(NHUnitTypes.zarkov, () -> {
						nodeUnit(NHUnitTypes.declining, () -> {});
					});
				});
				nodeUnit(NHUnitTypes.gather, () -> {
					node(NHUnitTypes.rhino, () -> {
						node(NHUnitTypes.saviour, () -> {
						
						});
					});
				});
			});
			
			node(NHBlocks.multiEfficientConveyor, () -> {
				node(NHBlocks.irdryonVault);
				node(NHBlocks.multiJunction);
				node(NHBlocks.multiRouter, () -> {
					node(NHBlocks.rapidUnloader);
				});
				node(NHBlocks.multiSteelItemBridge);
				node(NHBlocks.multiConduit, () -> {
					node(NHBlocks.irdryonTank);
					node(NHBlocks.multiSteelLiquidBridge);
				});
				node(NHBlocks.multiArmorConveyor, () -> {
					node(NHBlocks.multiConveyor);
				});
			});
			
			node(NHBlocks.pulseShotgun, () -> {
				node(NHBlocks.thermoTurret, () -> {
				
				});
				node(NHBlocks.synchro, () -> {
					node(NHBlocks.argmot);
					node(NHBlocks.multipleLauncher, () -> {
						node(NHBlocks.bloodStar, () -> {
							node(NHBlocks.endOfEra, () -> {
								node(NHBlocks.airRaider);
								node(NHBlocks.eternity, () -> {});
							});
						});
					});
					node(NHBlocks.antiBulletTurret, () -> {
						node(NHBlocks.laserWall);
						node(NHBlocks.atomSeparator, () -> {});
					});
				});
			});
			
			node(NHBlocks.zetaFactorySmall, () -> {
				node(NHBlocks.zetaFactoryLarge);
				node(NHBlocks.zetaFluidFactory);
				node(NHBlocks.xenMelter, () -> {
					node(NHBlocks.xenBetaFactory, () -> {
						node(NHBlocks.xenGammaFactory);
					});
				});
				node(NHBlocks.fusionEnergyFactory, () -> {
					node(NHBlocks.thermoCoreFactory, () -> {
						node(NHBlocks.thermoCoreNegativeFactory);
						node(NHBlocks.thermoCorePositiveFactory);
						node(NHBlocks.darkEnergyFactory);
					});
				});
			});
			node(NHBlocks.juniorProcessorFactory, () -> {
				node(NHBlocks.multiplePresstaniumFactory);
				node(NHBlocks.sandCracker, () -> {
					node(NHBlocks.oilRefiner);
				});
				node(NHBlocks.seniorProcessorFactory, () -> {
				
				});
				node(NHBlocks.metalOxhydrigenFactory, () -> {
					node(NHBlocks.metalOxhydrigenFactoryLarge);
					node(NHBlocks.multipleSteelFactory, () -> {
						node(NHBlocks.irayrondPanelFactory, () -> {
							node(NHBlocks.irayrondPanelFactorySmall);
							node(NHBlocks.irdryonFluidFactory, () -> {
								node(NHBlocks.multipleSurgeAlloyFactory);
								node(NHBlocks.setonAlloyFactory, () -> {
									node(NHBlocks.upgradeSortFactory);
								});
							});
						});
					});
				});
			});
			
			nodeProduce(NHItems.presstanium, () -> {
				nodeProduce(NHLiquids.quantumLiquid, () -> {
					node(NHBlocks.waterInstancer);
				});
				
				nodeProduce(NHItems.juniorProcessor, () -> {
					nodeProduce(NHItems.seniorProcessor, () -> {});
				});
				nodeProduce(NHItems.zeta, () -> {
					nodeProduce(NHLiquids.xenAlpha, () -> {
						nodeProduce(NHLiquids.xenBeta, () -> {
							nodeProduce(NHLiquids.xenGamma, () -> {
							
							});
						});
					});
					nodeProduce(NHLiquids.zetaFluid, () -> {});
					nodeProduce(NHItems.fusionEnergy, () -> {
						nodeProduce(NHItems.thermoCorePositive, () -> {});
						nodeProduce(NHItems.thermoCoreNegative, () -> {});
						node(NHItems.darkEnergy, Seq.with(new Objectives.Produce(NHItems.thermoCorePositive), new Objectives.Produce(NHItems.thermoCorePositive)), () -> {
							nodeUnit(NHUnitTypes.guardian, () -> {
							
							});
						});
					});
				});
				nodeProduce(NHItems.metalOxhydrigen, () -> {
					nodeProduce(NHItems.multipleSteel, () -> {
						nodeProduce(NHLiquids.irdryonFluid, () -> {
							nodeProduce(NHItems.irayrondPanel, () -> {
								nodeProduce(NHItems.setonAlloy, () -> {
									nodeProduce(NHItems.upgradeSort, () -> {});
								});
							});
						});
					});
				});
			});
		});
	}
}
