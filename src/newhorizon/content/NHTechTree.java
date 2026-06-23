package newhorizon.content;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.TechTree.TechNode;
import mindustry.content.UnitTypes;
import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.content.blocks.*;
import newhorizon.content.units.AirUnitTypes;
import newhorizon.content.units.GroundUnitTypes;
import newhorizon.expand.units.unitType.NHUnitType;

import static mindustry.content.TechTree.*;

public class NHTechTree {
    public static final ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
    private static final ObjectSet<UnlockableContent> addedProductionContents = new ObjectSet<>();
    private static final int defaultResearchCostMultiplier = 5;

    public static Seq<ProductionNode> itemProductionTree = new Seq<>();
    public static Seq<ProductionNode> liquidProductionTree = new Seq<>();
    public static Seq<ProductionNode> blockTechTree = new Seq<>();
    public static TechNode root;

    public static void load() {
        unitBuildCost.each((u, is) -> {
            if (u instanceof NHUnitType) {
                ((NHUnitType) u).setRequirements(is);
            }
        });

        itemProductionTree = buildItemProductionTree();
        liquidProductionTree = buildLiquidProductionTree();
        blockTechTree = buildBlockTechTree();
        addedProductionContents.clear();

        root = nodeRoot("new-horizon", NHPlanets.midantha, () -> {
            context().planet = NHPlanets.midantha;

            for (ProductionNode node : itemProductionTree) {
                addProductionNode(node);
            }

            for (ProductionNode node : liquidProductionTree) {
                addProductionNode(node);
            }

            for (ProductionNode node : blockTechTree) {
                addBlockTechNode(node);
            }
        });
    }

    private static void addProductionNode(ProductionNode productionNode) {
        if (addedProductionContents.contains(productionNode.content)) return;

        addedProductionContents.add(productionNode.content);

        nodeProduce(productionNode.content, () -> {
            for (ProductionNode child : productionNode.children) {
                addProductionNode(child);
            }
        });
    }

    private static void addBlockTechNode(ProductionNode techNode) {
        if (techNode.content == null || addedProductionContents.contains(techNode.content)) return;

        addedProductionContents.add(techNode.content);

        node(techNode.content, getResearchRequirements(techNode), () -> {
            for (ProductionNode child : techNode.children) {
                addBlockTechNode(child);
            }
        });
    }

    private static ItemStack[] getResearchRequirements(ProductionNode techNode) {
        if (techNode.requirements != null) return techNode.requirements;

        if (techNode.content instanceof UnitType) {
            ItemStack[] requirements = unitBuildCost.get((UnitType) techNode.content);
            return requirements == null ? ItemStack.empty : requirements;
        }

        if (!(techNode.content instanceof Block)) return ItemStack.empty;

        Block block = (Block) techNode.content;
        if (block.requirements == null || block.requirements.length == 0) return ItemStack.empty;

        ItemStack[] result = new ItemStack[block.requirements.length];

        for (int i = 0; i < block.requirements.length; i++) {
            ItemStack stack = block.requirements[i];
            result[i] = new ItemStack(stack.item, stack.amount * defaultResearchCostMultiplier);
        }

        return result;
    }

    public static final class ProductionNode {
        public final UnlockableContent content;
        public final Seq<ProductionNode> children;
        public final ItemStack[] requirements;

        private ProductionNode(UnlockableContent content, ItemStack[] requirements, ProductionNode... children) {
            this.content = content;
            this.requirements = requirements;
            this.children = Seq.with(children);
        }

        public static ProductionNode node(UnlockableContent content, ProductionNode... children) {
            return new ProductionNode(content, null, children);
        }

        public static ProductionNode node(UnlockableContent content, ItemStack[] requirements, ProductionNode... children) {
            return new ProductionNode(content, requirements, children);
        }
    }

    private static Seq<ProductionNode> buildBlockTechTree() {
        return Seq.with(
                ProductionNode.node(SpecialBlock.coreConflux,
                        ProductionNode.node(SpecialBlock.coreArray,
                                ProductionNode.node(SpecialBlock.coreNexus,
                                        ProductionNode.node(SpecialBlock.coreCluster)
                                )
                        ),
                        ProductionNode.node(PowerBlock.photonPanel,
                                ProductionNode.node(PowerBlock.fluxNodeMK1,
                                        ProductionNode.node(PowerBlock.fluxNodeMK2),
                                        ProductionNode.node(PowerBlock.fluxNodeLargeMK1,
                                                ProductionNode.node(PowerBlock.fluxNodeLargeMK2)
                                        )
                                ),
                                ProductionNode.node(PowerBlock.neutralizationGenerator,
                                        ProductionNode.node(PowerBlock.hydrazineGenerator,
                                                ProductionNode.node(PowerBlock.fissionReactor,
                                                        ProductionNode.node(PowerBlock.fusionReactor,
                                                                ProductionNode.node(PowerBlock.hyperReactor)
                                                        )
                                                )
                                        )
                                ),
                                ProductionNode.node(PowerBlock.gravityTrapSmall,
                                        ProductionNode.node(PowerBlock.gravityTrap)
                                ),
                                ProductionNode.node(PowerBlock.armorBattery,
                                        ProductionNode.node(PowerBlock.armorBatteryLarge,
                                                ProductionNode.node(PowerBlock.armorBatteryHuge)
                                        )
                                )
                        ),
                        ProductionNode.node(ProductionBlock.scanCollector,
                                ProductionNode.node(ProductionBlock.sandCracker),
                                ProductionNode.node(NHBlocks.largeWaterExtractor),
                                ProductionNode.node(ProductionBlock.tungstenReconstructor,
                                        ProductionNode.node(ProductionBlock.titaniumReconstructor)
                                ),
                                ProductionNode.node(ProductionBlock.decoherenceReverser),
                                ProductionNode.node(ProductionBlock.resonanceMiningFacility,
                                        ProductionNode.node(ProductionBlock.airRadiator),
                                        ProductionNode.node(ProductionBlock.beamMiningFacility,
                                                ProductionNode.node(ProductionBlock.liquidRadiator)
                                        )
                                ),
                                ProductionNode.node(ProductionBlock.resourceConvertor),
                                ProductionNode.node(ProductionBlock.oilRefiner)
                        ),
                        ProductionNode.node(CraftingBlock.silicarCrusher,
                                ProductionNode.node(CraftingBlock.stampingFacility,
                                        ProductionNode.node(CraftingBlock.heavyStampingFacility),
                                        ProductionNode.node(CraftingBlock.multipleRollingMill,
                                                ProductionNode.node(CraftingBlock.mixedRollingMill,
                                                        ProductionNode.node(CraftingBlock.heavyRollingMill)
                                                ),
                                                ProductionNode.node(CraftingBlock.denseFactory,
                                                        ProductionNode.node(CraftingBlock.nodexFactory,
                                                                ProductionNode.node(CraftingBlock.hadronCompositeBuilder),
                                                                ProductionNode.node(CraftingBlock.darkEnergyTrap)
                                                        )
                                                ),
                                                ProductionNode.node(CraftingBlock.irayrondFactory,
                                                        ProductionNode.node(CraftingBlock.largeIrayrondFactory),
                                                        ProductionNode.node(CraftingBlock.ancimembraneConcentrator,
                                                                ProductionNode.node(CraftingBlock.hyperProcessor)
                                                        )
                                                )
                                        ),
                                        ProductionNode.node(CraftingBlock.crystallizer,
                                                ProductionNode.node(CraftingBlock.metalOxhydrigenRestructuror)
                                        )
                                ),
                                ProductionNode.node(CraftingBlock.processorManuFactory,
                                        ProductionNode.node(CraftingBlock.processorPrinter),
                                        ProductionNode.node(CraftingBlock.processorEtchingFacility,
                                                ProductionNode.node(CraftingBlock.processorCompactor)
                                        )
                                ),
                                ProductionNode.node(CraftingBlock.plasticator,
                                        ProductionNode.node(CraftingBlock.alloySmelter,
                                                ProductionNode.node(CraftingBlock.surgeSynthesizer)
                                        )
                                ),
                                ProductionNode.node(CraftingBlock.crucibleFoundry,
                                        ProductionNode.node(CraftingBlock.castingFoundry),
                                        ProductionNode.node(CraftingBlock.thoriumTransmuter,
                                                ProductionNode.node(CraftingBlock.fabricRestructuror,
                                                        ProductionNode.node(CraftingBlock.fabricSynthesizer)
                                                ),
                                                ProductionNode.node(CraftingBlock.fusionCoreEnergyFactory,
                                                        ProductionNode.node(CraftingBlock.zetaFactory,
                                                                ProductionNode.node(CraftingBlock.positivePhaseDecayer),
                                                                ProductionNode.node(CraftingBlock.negativePhaseDecayer)
                                                        )
                                                )
                                        )
                                ),
                                ProductionNode.node(CraftingBlock.rectificatior,
                                        ProductionNode.node(CraftingBlock.phaseRectificatior)
                                ),
                                ProductionNode.node(CraftingBlock.subCooler,
                                        ProductionNode.node(CraftingBlock.hyperCooler)
                                ),
                                ProductionNode.node(CraftingBlock.photocatalystFactory,
                                        ProductionNode.node(CraftingBlock.irdryonFluidFactory,
                                                ProductionNode.node(CraftingBlock.irdryonPhaseAscender)
                                        )
                                ),
                                ProductionNode.node(CraftingBlock.particleActivator,
                                        ProductionNode.node(CraftingBlock.plasmaActivator),
                                        ProductionNode.node(CraftingBlock.xenSeparator)
                                ),
                                ProductionNode.node(CraftingBlock.reverseCollapseFacility)
                        ),
                        ProductionNode.node(DistributionBlock.conveyor,
                                ProductionNode.node(DistributionBlock.logisticsRouter,
                                        ProductionNode.node(DistributionBlock.logisticsJunction),
                                        ProductionNode.node(DistributionBlock.logisticsDirectionalRouter,
                                                ProductionNode.node(DistributionBlock.logisticsDirectionalMerger),
                                                ProductionNode.node(DistributionBlock.logisticsDirectionalOverflowGate,
                                                        ProductionNode.node(DistributionBlock.logisticsDirectionalUnderflowGate)
                                                )
                                        ),
                                        ProductionNode.node(DistributionBlock.logisticsOmniOverflowGate,
                                                ProductionNode.node(DistributionBlock.logisticsOmniUnderflowGate),
                                                ProductionNode.node(DistributionBlock.logisticsOmniSorter,
                                                        ProductionNode.node(DistributionBlock.logisticsOmniBlocker)
                                                )
                                        )
                                ),
                                ProductionNode.node(DistributionBlock.conveyorBridge,
                                        ProductionNode.node(DistributionBlock.conveyorBridgeExtend)
                                ),
                                ProductionNode.node(DistributionBlock.conveyorUnloader,
                                        ProductionNode.node(DistributionBlock.conveyorUnloaderFast)
                                ),
                                ProductionNode.node(DistributionBlock.omniUnloader,
                                        ProductionNode.node(DistributionBlock.rapidUnloader)
                                ),
                                ProductionNode.node(DistributionBlock.stackRail,
                                        ProductionNode.node(DistributionBlock.steadyStackRail),
                                        ProductionNode.node(DistributionBlock.lightStackLoader,
                                                ProductionNode.node(DistributionBlock.heavyStackLoader)
                                        )
                                ),
                                ProductionNode.node(DistributionBlock.multiRouter,
                                        ProductionNode.node(DistributionBlock.multiJunction,
                                                ProductionNode.node(DistributionBlock.multiArmorConveyor)
                                        )
                                )
                        ),
                        ProductionNode.node(DistributionBlock.conduit,
                                ProductionNode.node(DistributionBlock.conduitJunction),
                                ProductionNode.node(DistributionBlock.conduitRouter),
                                ProductionNode.node(DistributionBlock.liquidBridge,
                                        ProductionNode.node(DistributionBlock.liquidBridgeExtend)
                                ),
                                ProductionNode.node(DistributionBlock.liquidUnloader),
                                ProductionNode.node(LiquidBlock.turboPumpSmall,
                                        ProductionNode.node(LiquidBlock.turboPump)
                                ),
                                ProductionNode.node(LiquidBlock.standardLiquidStorage,
                                        ProductionNode.node(LiquidBlock.heavyLiquidStorage)
                                )
                        ),
                        ProductionNode.node(DefenseBlock.titaniumWall,
                                ProductionNode.node(DefenseBlock.presstaniumWall,
                                        ProductionNode.node(DefenseBlock.refactoringMultiWall,
                                                ProductionNode.node(DefenseBlock.setonPhasedWall,
                                                        ProductionNode.node(DefenseBlock.shapedWall)
                                                )
                                        )
                                ),
                                ProductionNode.node(NHBlocks.fireExtinguisher),
                                ProductionNode.node(DefenseBlock.standardRegenProjector,
                                        ProductionNode.node(DefenseBlock.heavyRegenProjector)
                                ),
                                ProductionNode.node(DefenseBlock.standardForceProjector,
                                        ProductionNode.node(DefenseBlock.largeShieldGenerator,
                                                ProductionNode.node(DefenseBlock.riftShield)
                                        )
                                )
                        ),
                        ProductionNode.node(TurretBlock.thermo,
                                ProductionNode.node(TurretBlock.pulse,
                                        ProductionNode.node(TurretBlock.beam,
                                                ProductionNode.node(TurretBlock.electro)
                                        )
                                ),
                                ProductionNode.node(TurretBlock.synchro,
                                        ProductionNode.node(TurretBlock.argmot,
                                                ProductionNode.node(TurretBlock.bombard)
                                        )
                                ),
                                ProductionNode.node(TurretBlock.vortex,
                                        ProductionNode.node(TurretBlock.concentration)
                                ),
                                ProductionNode.node(NHBlocks.multipleLauncher,
                                        ProductionNode.node(NHBlocks.hive),
                                        ProductionNode.node(NHBlocks.bombLauncher,
                                                ProductionNode.node(NHBlocks.airRaider)
                                        )
                                ),
                                ProductionNode.node(NHBlocks.gravity,
                                        ProductionNode.node(NHBlocks.antiBulletTurret,
                                                ProductionNode.node(NHBlocks.webber)
                                        )
                                ),
                                ProductionNode.node(NHBlocks.blaster),
                                ProductionNode.node(NHBlocks.antibody,
                                        ProductionNode.node(NHBlocks.interferon,
                                                ProductionNode.node(NHBlocks.prism),
                                                ProductionNode.node(NHBlocks.executor,
                                                        ProductionNode.node(NHBlocks.dendrite),
                                                        ProductionNode.node(NHBlocks.endOfEra,
                                                                ProductionNode.node(NHBlocks.eternity)
                                                        )
                                                )
                                        )
                                ),
                                ProductionNode.node(NHBlocks.atomSeparator,
                                        ProductionNode.node(NHBlocks.railGun)
                                ),
                                ProductionNode.node(TurretBlock.slavio,
                                        //ProductionNode.node(TurretBlock.ancientArtillery),
                                        ProductionNode.node(NHBlocks.bloodStar)
                                )
                        ),
                        ProductionNode.node(SpecialBlock.standardStorage,
                                ProductionNode.node(SpecialBlock.heavyStorage,
                                        ProductionNode.node(SpecialBlock.remoteStorage)
                                ),
                                ProductionNode.node(SpecialBlock.juniorModuleBeacon,
                                        ProductionNode.node(SpecialBlock.seniorModuleBeacon)
                                )
                        ),
                        ProductionNode.node(LogicBlock.iconDisplay,
                                ProductionNode.node(LogicBlock.iconDisplaySmall),
                                ProductionNode.node(LogicBlock.characterDisplay,
                                        ProductionNode.node(LogicBlock.characterDisplaySmall)
                                )
                        ),
                        ProductionNode.node(UnitBlock.jumpGateBasic,
                                ProductionNode.node(GroundUnitTypes.origin),
                                ProductionNode.node(GroundUnitTypes.thynomo),
                                ProductionNode.node(NHUnitTypes.sharp),
                                ProductionNode.node(NHUnitTypes.branch),
                                ProductionNode.node(NHUnitTypes.relay),
                                ProductionNode.node(NHUnitTypes.histone),

                                ProductionNode.node(UnitBlock.jumpGatePrimary,
                                        ProductionNode.node(NHUnitTypes.assaulter),
                                        ProductionNode.node(AirUnitTypes.apparition),
                                        ProductionNode.node(NHUnitTypes.ghost),
                                        ProductionNode.node(NHUnitTypes.warper),
                                        ProductionNode.node(NHUnitTypes.aliotiat),
                                        ProductionNode.node(NHUnitTypes.rhino),
                                        ProductionNode.node(NHUnitTypes.gather),
                                        ProductionNode.node(NHUnitTypes.restrictionEnzyme),

                                        ProductionNode.node(UnitBlock.jumpGateStandard,
                                                ProductionNode.node(NHUnitTypes.naxos),
                                                ProductionNode.node(NHUnitTypes.striker),
                                                ProductionNode.node(NHUnitTypes.tarlidor),
                                                ProductionNode.node(NHUnitTypes.zarkov),
                                                ProductionNode.node(NHUnitTypes.macrophage),
                                                ProductionNode.node(NHUnitTypes.lymph),

                                                ProductionNode.node(UnitBlock.jumpGateHyper,
                                                        ProductionNode.node(NHUnitTypes.destruction),
                                                        ProductionNode.node(NHUnitTypes.longinus),
                                                        ProductionNode.node(GroundUnitTypes.annihilation),
                                                        ProductionNode.node(NHUnitTypes.saviour),
                                                        ProductionNode.node(NHUnitTypes.declining),
                                                        ProductionNode.node(NHUnitTypes.hurricane),
                                                        ProductionNode.node(NHUnitTypes.anvil),
                                                        ProductionNode.node(NHUnitTypes.sin),
                                                        ProductionNode.node(NHUnitTypes.collapser),
                                                        ProductionNode.node(NHUnitTypes.laugra),
                                                        ProductionNode.node(NHUnitTypes.guardian),
                                                        ProductionNode.node(NHUnitTypes.pester)
                                                        //ProductionNode.node(NHUnitTypes.nucleoid)
                                                )
                                        )
                                ),
                                ProductionNode.node(NHBlocks.hyperspaceWarper)
                        )
                )
        );
    }

    private static Seq<ProductionNode> buildItemProductionTree() {
        return Seq.with(
                ProductionNode.node(NHItems.silicar,
                        ProductionNode.node(NHItems.hardLight),
                        ProductionNode.node(NHItems.graphite,
                                ProductionNode.node(NHItems.presstanium,
                                        ProductionNode.node(NHItems.multipleSteel,
                                                ProductionNode.node(NHItems.setonAlloy,
                                                        ProductionNode.node(NHItems.nodexPlate,
                                                                ProductionNode.node(NHItems.hadronicomp),
                                                                ProductionNode.node(NHItems.darkEnergy)
                                                        )
                                                ),
                                                ProductionNode.node(NHItems.irayrondPanel,
                                                        ProductionNode.node(NHItems.ancimembrane,
                                                                ProductionNode.node(NHItems.hyperProcessor)
                                                        )
                                                )
                                        ),
                                        ProductionNode.node(NHItems.metalOxhydrigen)
                                )
                        ),
                        ProductionNode.node(Items.titanium,
                                ProductionNode.node(NHItems.plastanium,
                                        ProductionNode.node(NHItems.surgeAlloy)
                                )
                        ),
                        ProductionNode.node(NHItems.silicon,
                                ProductionNode.node(NHItems.juniorProcessor,
                                        ProductionNode.node(NHItems.seniorProcessor)
                                )
                        ),
                        ProductionNode.node(Items.tungsten,
                                ProductionNode.node(Items.carbide),
                                ProductionNode.node(Items.thorium,
                                        ProductionNode.node(Items.fissileMatter,
                                                ProductionNode.node(NHItems.zeta,
                                                        ProductionNode.node(NHItems.thermoCorePositive),
                                                        ProductionNode.node(NHItems.thermoCoreNegative)
                                                ),
                                                ProductionNode.node(Items.phaseFabric)
                                        ),
                                        ProductionNode.node(NHItems.fusionEnergy)
                                )
                        )
                )
        );
    }

    private static Seq<ProductionNode> buildLiquidProductionTree() {
        return Seq.with(
                ProductionNode.node(NHLiquids.ammonia,
                        ProductionNode.node(NHLiquids.hydrazine,
                                ProductionNode.node(NHLiquids.irdryonFluid)
                        ),
                        ProductionNode.node(NHLiquids.xenFluid,
                                ProductionNode.node(NHLiquids.neutron),
                                ProductionNode.node(NHLiquids.proton)
                        ),
                        ProductionNode.node(NHLiquids.quantumLiquid,
                                ProductionNode.node(NHLiquids.antiMatter)
                        ),
                        ProductionNode.node(NHLiquids.cryofluid),
                        ProductionNode.node(NHLiquids.particle),
                        ProductionNode.node(NHLiquids.zetaPositive,
                                ProductionNode.node(NHLiquids.zetaNegative)
                        )
                )
        );
    }

    @SuppressWarnings("all")
    public static class TechTreeNodeContent extends StatusEffect {
        public Seq<UnlockableContent> unlockables;

        public TechTreeNodeContent(String name) {
            super(name);
        }

        @Override
        public void onUnlock() {
            unlockables.each(UnlockableContent::quietUnlock);
        }
    }
}