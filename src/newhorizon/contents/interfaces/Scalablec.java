package newhorizon.contents.interfaces;

import mindustry.gen.Buildingc;

import newhorizon.contents.blocks.special.UpgraderBlock.UpgraderBlockBuild;
import newhorizon.contents.data.*;

public interface Scalablec extends Buildingc{
	public void resetUpgrade();
	
	public void updateUpgradeBase(UpgradeBaseData importBaseData);
	
	public void drawConnected();
	
	public boolean isConnected();
	
    public UpgraderBlockBuild upgrader();
    
    public void setBaseData(UpgradeBaseData baseData);
    
    public UpgradeBaseData getBaseData();
}

