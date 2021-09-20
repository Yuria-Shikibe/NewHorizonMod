package newhorizon.content;

import mindustry.content.*;
import mindustry.content.TechTree.TechNode;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;


public class NHTechTree implements ContentList {
    public static void addProduce(UnlockableContent root, UnlockableContent content){
        TechNode node = new TechNode(TechTree.get(root), content, ItemStack.with());
        node.objectives.add(new Objectives.Produce(content));
    }
    
    public static void add(UnlockableContent root, UnlockableContent content){
        new TechNode(TechTree.get(root), content, content.researchRequirements());
    }
    
    public static void add(UnlockableContent root, UnlockableContent content, Objectives.Objective... objectives){
        TechNode node = new TechNode(TechTree.get(root), content, content.researchRequirements());
        node.objectives.addAll(objectives);
    }
    
    public static void addUnit(UnlockableContent root, UnitType type){
        ItemStack[] requirement;
        if((requirement = NHLoader.unitBuildCost.get(type)) != null){
            new TechNode(TechTree.get(root), type, requirement);
        }else new TechNode(TechTree.get(root), type, ItemStack.with(NHItems.juniorProcessor, 500));
    }
    
    @Override
    public void load(){
        //Blocks;
        add(NHBlocks.bombLauncher, NHBlocks.airRaider);
        
        add(Blocks.commandCenter, NHBlocks.jumpGatePrimary);
        add(NHBlocks.jumpGatePrimary, NHBlocks.jumpGateJunior, new Objectives.SectorComplete(NHSectorPresets.ruinedWarehouse));
        add(NHBlocks.jumpGateJunior, NHBlocks.jumpGate);
        add(NHBlocks.jumpGate, NHBlocks.hyperspaceWarper);
        
        add(NHBlocks.hyperspaceWarper, NHBlocks.gravityGully);
        add(Blocks.powerNode, NHBlocks.insulatedWall);
        add(Blocks.surgeWall, NHBlocks.heavyDefenceWall);
        add(NHBlocks.heavyDefenceWall, NHBlocks.heavyDefenceWallLarge);
        add(NHBlocks.heavyDefenceWall, NHBlocks.setonWall);
        add(NHBlocks.heavyDefenceWall, NHBlocks.heavyDefenceDoor);
        add(NHBlocks.setonWall, NHBlocks.setonWallLarge);
        add(NHBlocks.heavyDefenceDoor, NHBlocks.heavyDefenceDoorLarge);
        add(Blocks.batteryLarge, NHBlocks.armorBatteryLarge);
        //new TechNode(TechTree.get(Blocks.massDriver), NHBlocks.delivery, NHBlocks.delivery.researchRequirements());
        new TechNode(TechTree.get(Blocks.parallax), NHBlocks.divlusion, NHBlocks.divlusion.researchRequirements());
        add(NHBlocks.divlusion, NHBlocks.blastTurret, new Objectives.SectorComplete(NHSectorPresets.quantumCraters));
        new TechNode(TechTree.get(Blocks.forceProjector), NHBlocks.largeShieldGenerator, NHBlocks.largeShieldGenerator.researchRequirements());
        new TechNode(TechTree.get(Blocks.spectre), NHBlocks.thurmix, NHBlocks.thurmix.researchRequirements());
        add(NHBlocks.blastTurret, NHBlocks.endOfEra, new Objectives.SectorComplete(NHSectorPresets.downpour));
        new TechNode(TechTree.get(NHBlocks.endOfEra), NHBlocks.eoeUpgrader, ItemStack.with());
        add(NHBlocks.endOfEra, NHBlocks.eternity);
        new TechNode(TechTree.get(Blocks.phaseWall), NHBlocks.chargeWall, NHBlocks.chargeWall.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.chargeWall), NHBlocks.chargeWallLarge, NHBlocks.chargeWallLarge.researchRequirements());
    
        add(NHBlocks.chargeWall, NHBlocks.shapedWall, new Objectives.SectorComplete(NHSectorPresets.downpour));
        
        new TechNode(TechTree.get(Blocks.vault), NHBlocks.irdryonVault, NHBlocks.irdryonVault.researchRequirements());
        new TechNode(TechTree.get(Blocks.lancer), NHBlocks.argmot, NHBlocks.argmot.researchRequirements());
        new TechNode(TechTree.get(Blocks.shockMine), NHBlocks.blaster, NHBlocks.blaster.researchRequirements());
        new TechNode(TechTree.get(Blocks.pneumaticDrill), NHBlocks.presstaniumFactory, NHBlocks.presstaniumFactory.researchRequirements());
        add(NHBlocks.presstaniumFactory, NHBlocks.multiplePresstaniumFactory);
        new TechNode(TechTree.get(Blocks.siliconSmelter), NHBlocks.juniorProcessorFactory, NHBlocks.juniorProcessorFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.juniorProcessorFactory), NHBlocks.seniorProcessorFactory, NHBlocks.seniorProcessorFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.separator), NHBlocks.zetaFactorySmall, NHBlocks.zetaFactorySmall.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.zetaFactorySmall), NHBlocks.zetaFactoryLarge, NHBlocks.zetaFactoryLarge.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.zetaFactorySmall), NHBlocks.zetaFluidFactory, NHBlocks.zetaFluidFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.blastDrill), NHBlocks.fusionEnergyFactory, NHBlocks.fusionEnergyFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.kiln), NHBlocks.multipleSteelFactory, NHBlocks.multipleSteelFactory.researchRequirements());
        
        add(NHBlocks.presstaniumFactory, NHBlocks.irayrondPanelFactorySmall);
        add(NHBlocks.irayrondPanelFactorySmall, NHBlocks.irayrondPanelFactory);
        add(NHBlocks.irayrondPanelFactory, NHBlocks.setonAlloyFactory);
        add(NHBlocks.irayrondPanelFactory, NHBlocks.multipleSurgeAlloyFactory);
        
        new TechNode(TechTree.get(NHBlocks.setonAlloyFactory), NHBlocks.upgradeSortFactory, NHBlocks.upgradeSortFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.upgradeSortFactory), NHBlocks.darkEnergyFactory, NHBlocks.darkEnergyFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.pneumaticDrill), NHBlocks.metalOxhydrigenFactory, NHBlocks.metalOxhydrigenFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.fusionEnergyFactory), NHBlocks.thermoCoreFactory, NHBlocks.thermoCoreFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.thermoCoreFactory), NHBlocks.thermoCorePositiveFactory, NHBlocks.thermoCorePositiveFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.thermoCoreFactory), NHBlocks.thermoCoreNegativeFactory, NHBlocks.thermoCoreNegativeFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.irayrondPanelFactory), NHBlocks.irdryonFluidFactory, NHBlocks.irdryonFluidFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.zetaFactorySmall), NHBlocks.xenBetaFactory, NHBlocks.xenBetaFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.xenBetaFactory), NHBlocks.xenGammaFactory, NHBlocks.xenGammaFactory.researchRequirements());
        add(NHBlocks.zetaFactorySmall, NHBlocks.xenMelter);

        add(NHBlocks.empTurret, NHBlocks.scrambler);
        //Units;
        addUnit(UnitTypes.mono, NHUnitTypes.gather);
        addUnit(UnitTypes.poly, NHUnitTypes.rhino);
    
        addUnit(Blocks.airFactory, NHUnitTypes.sharp);
        addUnit(NHUnitTypes.sharp, NHUnitTypes.branch);
        addUnit(NHUnitTypes.branch, NHUnitTypes.warper);
        addUnit(NHUnitTypes.warper, NHUnitTypes.striker);
        addUnit(NHUnitTypes.warper, NHUnitTypes.naxos);
        addUnit(NHUnitTypes.striker, NHUnitTypes.longinus);
        addUnit(NHUnitTypes.striker, NHUnitTypes.destruction);
        addUnit(NHUnitTypes.destruction, NHUnitTypes.hurricane);
        
        addUnit(Blocks.groundFactory, NHUnitTypes.origin);
        addUnit(NHUnitTypes.origin, NHUnitTypes.thynomo);
        addUnit(NHUnitTypes.thynomo, NHUnitTypes.aliotiat);
        addUnit(NHUnitTypes.aliotiat, NHUnitTypes.tarlidor);
        addUnit(NHUnitTypes.tarlidor, NHUnitTypes.annihilation);
        
        addUnit(Blocks.navalFactory, NHUnitTypes.relay);
        addUnit(NHUnitTypes.relay, NHUnitTypes.ghost);
        addUnit(NHUnitTypes.ghost, NHUnitTypes.zarkov);
        addUnit(NHUnitTypes.zarkov, NHUnitTypes.declining);
        
        addUnit(NHBlocks.darkEnergyFactory, NHUnitTypes.guardian);
        
        //Items / liquids;
        addProduce(Items.titanium, NHItems.metalOxhydrigen);
        addProduce(Items.metaglass, NHItems.multipleSteel);
        addProduce(Items.plastanium, NHItems.presstanium);
        addProduce(Items.silicon, NHItems.juniorProcessor);
        addProduce(NHItems.juniorProcessor, NHItems.seniorProcessor);
        addProduce(Items.surgeAlloy, NHItems.irayrondPanel);
        addProduce(NHItems.irayrondPanel, NHItems.setonAlloy);
        addProduce(NHItems.setonAlloy, NHItems.upgradeSort);
        addProduce(Items.phaseFabric, NHItems.fusionEnergy);
        addProduce(NHItems.fusionEnergy, NHItems.thermoCorePositive);
        addProduce(NHItems.thermoCorePositive, NHItems.thermoCoreNegative);
        addProduce(NHItems.upgradeSort, NHItems.darkEnergy);
        addProduce(Items.thorium, NHItems.zeta);
        addProduce(NHItems.irayrondPanel, NHLiquids.irdryonFluid);
        addProduce(NHItems.zeta, NHLiquids.zetaFluid);
        addProduce(Liquids.water, NHLiquids.xenAlpha);
        addProduce(NHLiquids.xenAlpha, NHLiquids.xenBeta);
        addProduce(NHLiquids.xenBeta, NHLiquids.xenGamma);
        
        add(SectorPresets.planetaryTerminal, NHSectorPresets.ruinedWarehouse, new Objectives.SectorComplete(SectorPresets.planetaryTerminal));
        add(NHSectorPresets.ruinedWarehouse, NHSectorPresets.shatteredRavine, new Objectives.SectorComplete(NHSectorPresets.ruinedWarehouse));
        add(NHSectorPresets.ruinedWarehouse, NHSectorPresets.quantumCraters, new Objectives.SectorComplete(NHSectorPresets.ruinedWarehouse));
        add(NHSectorPresets.quantumCraters, NHSectorPresets.luminariOutpost, new Objectives.SectorComplete(NHSectorPresets.quantumCraters));
        add(NHSectorPresets.luminariOutpost, NHSectorPresets.downpour, new Objectives.SectorComplete(NHSectorPresets.luminariOutpost));
        add(NHSectorPresets.downpour, NHSectorPresets.quantumCraters, new Objectives.SectorComplete(NHSectorPresets.downpour));
        add(NHSectorPresets.downpour, NHSectorPresets.ancientBattefield, new Objectives.SectorComplete(NHSectorPresets.downpour));
        add(NHSectorPresets.downpour, NHSectorPresets.mainPath, new Objectives.SectorComplete(NHSectorPresets.downpour));
        add(NHSectorPresets.mainPath, NHSectorPresets.hostileHQ, new Objectives.SectorComplete(NHSectorPresets.mainPath));
        add(NHSectorPresets.quantumCraters, NHSectorPresets.deltaHQ, new Objectives.SectorComplete(NHSectorPresets.quantumCraters));
       
        add(Liquids.water, NHLiquids.quantumLiquid, new Objectives.Produce(NHLiquids.quantumLiquid));
    }
}
