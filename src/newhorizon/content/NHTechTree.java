package newhorizon.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import newhorizon.content.blocks.*;
import newhorizon.expand.units.unitType.NHUnitType;

import static mindustry.content.TechTree.*;

@SuppressWarnings("CodeBlock2Expr")
public class NHTechTree{
	public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
	
	public static void nodeUnit(UnitType type, Runnable children){
		node(type, unitBuildCost.get(type), children);
	}
	
	public static void nodeUnit(UnitType type, Seq<Objectives.Objective> objectives, Runnable children){
		node(type, unitBuildCost.get(type), objectives, children);
	}
	
	public static void load(){
		unitBuildCost.each((u, is) -> {
			if(u instanceof NHUnitType){
				((NHUnitType)u).setRequirements(is);
			}
		});
		
		TechNode root = nodeRoot("new-horizon", NHPlanets.midantha, () -> {
			node(NHSectorPresents.abandonedOutpost, ItemStack.with(/*NHItems.juniorProcessor, 1000*/), () -> {
				node(NHSectorPresents.initialPlane, ItemStack.with(NHItems.juniorProcessor, 1500), Seq.with(new Objectives.SectorComplete(NHSectorPresents.abandonedOutpost)), () -> {
					node(NHSectorPresents.hostileResearchStation, ItemStack.with(NHItems.seniorProcessor, 1500), Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
						//node(NHSectorPresents.ancientShipyard, Seq.with(new Objectives.SectorComplete(NHSectorPresents.hostileResearchStation)), () -> {
						//
						//});
					});
				});
			});

			node(SpecialBlock.nexusCore, () -> {
				node(ProductionBlocks.resonanceMiningFacility, () -> {
					node(ProductionBlocks.beamMiningFacility, () -> {
						node(ProductionBlocks.implosionMiningFacility, () -> {
							node(ProductionBlocks.deliveryModule);
						});
						node(ProductionBlocks.speedModule);
					});
					node(ProductionBlocks.refineModule);
				});
				node(DistributionBlock.conveyor, () -> {
					node(DistributionBlock.conveyorJunction);
					node(DistributionBlock.conveyorRouter, () -> {
						node(DistributionBlock.conveyorMerger);
						node(DistributionBlock.conveyorGate);
					});
					node(DistributionBlock.conveyorBridge, () -> {
						node(DistributionBlock.liquidBridge);
					});
					node(DistributionBlock.conveyorUnloader, () -> {
						node(DistributionBlock.liquidUnloader);
					});
				});
			});

			node(NHBlocks.zetaGenerator, () -> {
				node(NHBlocks.hyperGenerator);
			});
			
			node(NHBlocks.armorPowerNode, () -> {
				node(NHBlocks.hydroFuelCell);
				node(NHBlocks.heavyPowerNode);
				node(NHBlocks.largeMendProjector, () -> {
					node(NHBlocks.gravityTrapSmall, () -> {
						node(NHBlocks.gravityTrap);
						node(NHBlocks.assignOverdrive);
					});
				});
				node(NHBlocks.largeShieldGenerator, () -> {
					node(DefenseBlock.riftShield);
				});
				node(NHBlocks.armorBatteryLarge, () -> {
					node(NHBlocks.hugeBattery);
				});
				node(NHBlocks.largeWaterExtractor);
			});

			node(DefenseBlock.presstaniumWall, () -> {
				node(DefenseBlock.refactoringMultiWall, () -> {
					node(DefenseBlock.setonPhasedWall, () -> {
						node(DefenseBlock.shapedWall);
					});
				});
			});
			
			node(NHBlocks.jumpGatePrimary, () -> {
				node(NHBlocks.remoteStorage);
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
					nodeUnit(NHUnitTypes.anvil, () -> {
						nodeUnit(NHUnitTypes.collapser, () -> {
						
						});
					});
				});
				nodeUnit(NHUnitTypes.sharp, () -> {
					nodeUnit(NHUnitTypes.branch, () -> {
						nodeUnit(NHUnitTypes.warper, () -> {
							nodeUnit(NHUnitTypes.naxos, () -> {
								nodeUnit(NHUnitTypes.destruction, () -> {
								
								});
								nodeUnit(NHUnitTypes.longinus, () -> {
									nodeUnit(NHUnitTypes.hurricane, () -> {
									
									});
								});
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
				node(NHBlocks.beamLaserTurret);
				node(NHBlocks.thermoTurret, () -> {
					node(NHBlocks.blaster);
				});
				
				node(NHBlocks.synchro, () -> {
					node(NHBlocks.argmot, () -> {
						node(NHBlocks.prism, Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
							node(NHBlocks.concentration);
						});
						node(NHBlocks.gravity, () -> {
							node(NHBlocks.webber, () -> {
								node(NHBlocks.executor);
							});
						});
					node(TurretBlock.electro);
					});
					
					node(NHBlocks.multipleLauncher, () -> {
						node(NHBlocks.hive, Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
							node(NHBlocks.multipleArtillery);
						});
						node(NHBlocks.bombLauncher);
						node(NHBlocks.bloodStar, () -> {
							node(NHBlocks.railGun);
							node(NHBlocks.endOfEra, Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
								node(NHBlocks.airRaider);
								node(NHBlocks.eternity/*, Seq.with(new Objectives.SectorComplete(NHSectorPresents.ancientShipyard)), () -> {}*/);
							});
						});
					});
					node(NHBlocks.antiBulletTurret, () -> {
						node(NHBlocks.laserWall);
						node(NHBlocks.atomSeparator, () -> {});
					});
				});
			});
			
			node(NHBlocks.presstaniumFactory, () -> {
				node(CraftingBlock.glassQuantifier);
				node(NHBlocks.zetaFactorySmall, () -> {
					node(NHBlocks.zetaFactoryLarge, () -> {
						node(CraftingBlock.hyperZetaFactory, () -> {
							node(CraftingBlock.fluxPhaser, () -> {});
						});
					});
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
							node(NHBlocks.darkEnergyFactory, Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
							
							});
						});
					});
				});
				node(NHBlocks.juniorProcessorFactory, () -> {
					node(NHBlocks.multiplePresstaniumFactory);
					node(NHBlocks.sandCracker, () -> {
						node(NHBlocks.oilRefiner, () -> {
							node(NHBlocks.arkyciteCompactor);
						});
					});
					node(NHBlocks.seniorProcessorFactory, () -> {
						node(NHBlocks.processorCompactor, Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
						
						});
					});
					node(NHBlocks.metalOxhydrigenFactory, () -> {
						node(NHBlocks.metalOxhydrigenFactoryLarge);
						node(NHBlocks.multipleSteelFactory, () -> {
							node(NHBlocks.reconstructPlastaniumFactory);
							node(NHBlocks.irayrondPanelFactory, () -> {
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
			});
			
			nodeProduce(NHItems.presstanium, () -> {
				nodeProduce(NHLiquids.quantumEntity, () -> {
					node(NHBlocks.waterInstancer, () -> {
						node(NHBlocks.ventExtractor);
					});
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
							node(NHItems.ancimembrane, ItemStack.with(NHItems.seniorProcessor, 3000, Items.tungsten, 10000, NHItems.setonAlloy, 1000), () -> {
								node(NHBlocks.ancimembraneConcentrator, () -> {
									node(NHBlocks.antibody, () -> {
										node(NHBlocks.interferon, () -> {
											node(NHBlocks.dendrite);
										});
									});
									
									node(NHBlocks.hyperspaceWarper);

									node(NHBlocks.ancientPowerNode, () -> {
										node(NHBlocks.ancientLaserWall);
									});
									
									nodeUnit(NHUnitTypes.restrictionEnzyme, () -> {
										nodeUnit(NHUnitTypes.macrophage, Seq.with(new Objectives.SectorComplete(NHSectorPresents.initialPlane)), () -> {
											nodeUnit(NHUnitTypes.laugra, () -> {
												nodeUnit(NHUnitTypes.pester/*, Seq.with(new Objectives.SectorComplete(NHSectorPresents.ancientShipyard))*/, () -> {
												
												});
											});
										});
									});
									
								});
							});
							
							nodeUnit(NHUnitTypes.guardian, () -> {
							
							});
						}).children.each(c -> {
//							c.researchCostMultipliers
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
		
		root.planet = NHPlanets.midantha;
		root.children.each(c -> c.planet = NHPlanets.midantha);
	}
}
