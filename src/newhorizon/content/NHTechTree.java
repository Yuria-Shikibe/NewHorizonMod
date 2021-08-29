package newhorizon.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.content.TechTree.TechNode;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class NHTechTree implements ContentList {
    public static Method node1 = null, node2 = null, nodeObjectives = null;
    
    static{
        try{
            node1 = TechTree.class.getDeclaredMethod("node", UnlockableContent.class, Runnable.class);
            node1.setAccessible(true);
    
            node2 = TechTree.class.getDeclaredMethod("node", UnlockableContent.class, Seq.class, Runnable.class);
            node2.setAccessible(true);
            
            nodeObjectives = TechTree.class.getDeclaredMethod("nodeProduce", UnlockableContent.class, Seq.class, Runnable.class);
            nodeObjectives.setAccessible(true);
        }catch(NoSuchMethodException e){
            e.printStackTrace();
        }
    }
    
    public static void node(UnlockableContent root, Seq<Objectives.Objective> objectives, Runnable children){
        try{
            node2.invoke(null, root, objectives, children);
        }catch(IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }
    
    public static void node(UnlockableContent root, Runnable children){
        try{
            node1.invoke(null, root, children);
        }catch(IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }
    
    public static void nodeProduce(UnlockableContent root, Seq<Objectives.Objective> objectives, Runnable children){
        try{
            nodeObjectives.invoke(null, root, objectives, children);
        }catch(IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
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
        add(NHBlocks.jumpGatePrimary, NHBlocks.jumpGateJunior);
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
        add(NHBlocks.divlusion, NHBlocks.blastTurret);
        new TechNode(TechTree.get(Blocks.forceProjector), NHBlocks.largeShieldGenerator, NHBlocks.largeShieldGenerator.researchRequirements());
        new TechNode(TechTree.get(Blocks.spectre), NHBlocks.thurmix, NHBlocks.thurmix.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.thurmix), NHBlocks.endOfEra, NHBlocks.endOfEra.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.endOfEra), NHBlocks.eoeUpgrader, ItemStack.with());
        add(NHBlocks.endOfEra, NHBlocks.eternity);
        new TechNode(TechTree.get(Blocks.phaseWall), NHBlocks.chargeWall, NHBlocks.chargeWall.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.chargeWall), NHBlocks.chargeWallLarge, NHBlocks.chargeWallLarge.researchRequirements());
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
        new TechNode(TechTree.get(Items.titanium), NHItems.metalOxhydrigen, NHItems.metalOxhydrigen.researchRequirements());
        new TechNode(TechTree.get(Items.metaglass), NHItems.multipleSteel, NHItems.multipleSteel.researchRequirements());
        new TechNode(TechTree.get(Items.plastanium), NHItems.presstanium, NHItems.presstanium.researchRequirements());
        new TechNode(TechTree.get(Items.silicon), NHItems.juniorProcessor, NHItems.juniorProcessor.researchRequirements());
        new TechNode(TechTree.get(NHItems.juniorProcessor), NHItems.seniorProcessor, NHItems.seniorProcessor.researchRequirements());
        new TechNode(TechTree.get(Items.surgeAlloy), NHItems.irayrondPanel, NHItems.irayrondPanel.researchRequirements());
        new TechNode(TechTree.get(NHItems.irayrondPanel), NHItems.setonAlloy, NHItems.setonAlloy.researchRequirements());
        new TechNode(TechTree.get(NHItems.setonAlloy), NHItems.upgradeSort, NHItems.upgradeSort.researchRequirements());
        new TechNode(TechTree.get(Items.phaseFabric), NHItems.fusionEnergy, NHItems.fusionEnergy.researchRequirements());
        new TechNode(TechTree.get(NHItems.fusionEnergy), NHItems.thermoCorePositive, NHItems.thermoCorePositive.researchRequirements());
        new TechNode(TechTree.get(NHItems.thermoCorePositive), NHItems.thermoCoreNegative, NHItems.thermoCoreNegative.researchRequirements());
        new TechNode(TechTree.get(NHItems.upgradeSort), NHItems.darkEnergy, NHItems.darkEnergy.researchRequirements());
        new TechNode(TechTree.get(Items.thorium), NHItems.zeta, NHItems.zeta.researchRequirements());
        new TechNode(TechTree.get(NHItems.irayrondPanel), NHLiquids.irdryonFluid, NHLiquids.irdryonFluid.researchRequirements());
        new TechNode(TechTree.get(NHItems.zeta), NHLiquids.zetaFluid, NHLiquids.zetaFluid.researchRequirements());
        new TechNode(TechTree.get(Liquids.water), NHLiquids.xenAlpha, NHLiquids.xenAlpha.researchRequirements());
        new TechNode(TechTree.get(NHLiquids.xenAlpha), NHLiquids.xenBeta, NHLiquids.xenBeta.researchRequirements());
        new TechNode(TechTree.get(NHLiquids.xenBeta), NHLiquids.xenGamma, NHLiquids.xenGamma.researchRequirements());
        
        add(SectorPresets.planetaryTerminal, NHSectorPresets.ruinedWarehouse, new Objectives.SectorComplete(SectorPresets.planetaryTerminal));
        add(NHSectorPresets.ruinedWarehouse, NHSectorPresets.shatteredRavine, new Objectives.SectorComplete(NHSectorPresets.ruinedWarehouse));
        add(NHSectorPresets.ruinedWarehouse, NHSectorPresets.quantumCraters, new Objectives.SectorComplete(NHSectorPresets.ruinedWarehouse));
        add(NHSectorPresets.quantumCraters, NHSectorPresets.luminariOutpost, new Objectives.SectorComplete(NHSectorPresets.quantumCraters));
        add(NHSectorPresets.luminariOutpost, NHSectorPresets.downpour, new Objectives.SectorComplete(NHSectorPresets.luminariOutpost));
        add(NHSectorPresets.downpour, NHSectorPresets.quantumCraters, new Objectives.SectorComplete(NHSectorPresets.downpour));
        add(NHSectorPresets.downpour, NHSectorPresets.hostileHQ, new Objectives.SectorComplete(NHSectorPresets.downpour));
        add(NHSectorPresets.quantumCraters, NHSectorPresets.deltaHQ, new Objectives.SectorComplete(NHSectorPresets.quantumCraters));
       
        add(Liquids.water, NHLiquids.quantumLiquid, new Objectives.Produce(NHLiquids.quantumLiquid));
    }
}
