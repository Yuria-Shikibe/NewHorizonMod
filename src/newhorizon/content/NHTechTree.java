package newhorizon.content;

import mindustry.content.*;
import mindustry.content.TechTree.TechNode;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;


public class NHTechTree implements ContentList {
    
    public static void add(UnlockableContent root, UnlockableContent content){
        new TechNode(TechTree.get(root), content, content.researchRequirements());
    }
    
    @Override
    public void load(){
        //Blocks;
        add(Blocks.powerNode, NHBlocks.insulatedWall);
        add(Blocks.surgeWall, NHBlocks.heavyDefenceWall);
        add(NHBlocks.heavyDefenceWall, NHBlocks.heavyDefenceWallLarge);
        add(NHBlocks.heavyDefenceWall, NHBlocks.setonWall);
        add(NHBlocks.heavyDefenceWall, NHBlocks.heavyDefenceDoor);
        add(NHBlocks.setonWall, NHBlocks.setonWallLarge);
        add(NHBlocks.heavyDefenceDoor, NHBlocks.heavyDefenceDoorLarge);
        new TechNode(TechTree.get(Blocks.massDriver), NHBlocks.delivery, NHBlocks.delivery.researchRequirements());
        new TechNode(TechTree.get(Blocks.parallax), NHBlocks.divlusion, NHBlocks.divlusion.researchRequirements());
        new TechNode(TechTree.get(Blocks.parallax), NHBlocks.divlusion, NHBlocks.divlusion.researchRequirements());
        new TechNode(TechTree.get(Blocks.forceProjector), NHBlocks.largeShieldGenerator, NHBlocks.largeShieldGenerator.researchRequirements());
        new TechNode(TechTree.get(Blocks.spectre), NHBlocks.thurmix, NHBlocks.thurmix.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.thurmix), NHBlocks.ender, NHBlocks.ender.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.ender), NHBlocks.nemesisUpgrader, ItemStack.with());
        new TechNode(TechTree.get(Blocks.interplanetaryAccelerator), NHBlocks.jumpGate, NHBlocks.jumpGate.researchRequirements());
        new TechNode(TechTree.get(Blocks.phaseWall), NHBlocks.chargeWall, NHBlocks.chargeWall.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.chargeWall), NHBlocks.chargeWallLarge, NHBlocks.chargeWallLarge.researchRequirements());
        new TechNode(TechTree.get(Blocks.vault), NHBlocks.irdryonVault, NHBlocks.irdryonVault.researchRequirements());
        new TechNode(TechTree.get(Blocks.lancer), NHBlocks.argmot, NHBlocks.argmot.researchRequirements());
        new TechNode(TechTree.get(Blocks.shockMine), NHBlocks.blaster, NHBlocks.blaster.researchRequirements());
        new TechNode(TechTree.get(Blocks.pneumaticDrill), NHBlocks.presstaniumFactory, NHBlocks.presstaniumFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.siliconSmelter), NHBlocks.juniorProcessorFactory, NHBlocks.juniorProcessorFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.juniorProcessorFactory), NHBlocks.seniorProcessorFactory, NHBlocks.seniorProcessorFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.separator), NHBlocks.zateFactorySmall, NHBlocks.zateFactorySmall.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.zateFactorySmall), NHBlocks.zateFactoryLarge, NHBlocks.zateFactoryLarge.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.zateFactorySmall), NHBlocks.zateFluidFactory, NHBlocks.zateFluidFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.blastDrill), NHBlocks.fusionEnergyFactory, NHBlocks.fusionEnergyFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.kiln), NHBlocks.multipleSteelFactory, NHBlocks.multipleSteelFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.presstaniumFactory), NHBlocks.irayrondPanelFactory, NHBlocks.irayrondPanelFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.irayrondPanelFactory), NHBlocks.irayrondPanelFactorySmall, NHBlocks.irayrondPanelFactorySmall.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.irayrondPanelFactory), NHBlocks.setonAlloyFactory, NHBlocks.setonAlloyFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.setonAlloyFactory), NHBlocks.upgradeSortFactory, NHBlocks.upgradeSortFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.upgradeSortFactory), NHBlocks.darkEnergyFactory, NHBlocks.darkEnergyFactory.researchRequirements());
        new TechNode(TechTree.get(Blocks.pneumaticDrill), NHBlocks.metalOxhydrigenFactory, NHBlocks.metalOxhydrigenFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.fusionEnergyFactory), NHBlocks.thermoCoreFactory, NHBlocks.thermoCoreFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.thermoCoreFactory), NHBlocks.thermoCorePositiveFactory, NHBlocks.thermoCorePositiveFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.thermoCoreFactory), NHBlocks.thermoCoreNegativeFactory, NHBlocks.thermoCoreNegativeFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.irayrondPanelFactory), NHBlocks.irdryonFluidFactory, NHBlocks.irdryonFluidFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.zateFactorySmall), NHBlocks.xenBetaFactory, NHBlocks.xenBetaFactory.researchRequirements());
        new TechNode(TechTree.get(NHBlocks.xenBetaFactory), NHBlocks.xenGammaFactory, NHBlocks.xenGammaFactory.researchRequirements());
        add(NHBlocks.zateFactorySmall, NHBlocks.xenMelter);

        //Units;
        new TechNode(TechTree.get(UnitTypes.zenith), NHUnits.striker, NHUnits.striker.researchRequirements());
        new TechNode(TechTree.get(UnitTypes.scepter), NHUnits.tarlidor, NHUnits.tarlidor.researchRequirements());
        new TechNode(TechTree.get(UnitTypes.eclipse), NHUnits.hurricane, NHUnits.hurricane.researchRequirements());
        new TechNode(TechTree.get(NHUnits.tarlidor), NHUnits.annihilation, NHUnits.annihilation.researchRequirements());

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
        new TechNode(TechTree.get(Items.thorium), NHItems.zate, NHItems.zate.researchRequirements());
        new TechNode(TechTree.get(NHItems.irayrondPanel), NHLiquids.irdryonFluid, NHLiquids.irdryonFluid.researchRequirements());
        new TechNode(TechTree.get(NHItems.zate), NHLiquids.zateFluid, NHLiquids.zateFluid.researchRequirements());
        new TechNode(TechTree.get(Liquids.water), NHLiquids.xenAlpha, NHLiquids.xenAlpha.researchRequirements());
        new TechNode(TechTree.get(NHLiquids.xenAlpha), NHLiquids.xenBeta, NHLiquids.xenBeta.researchRequirements());
        new TechNode(TechTree.get(NHLiquids.xenBeta), NHLiquids.xenGamma, NHLiquids.xenGamma.researchRequirements());
    }
}
