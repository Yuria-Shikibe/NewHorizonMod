package newhorizon.contents.interfaces;

import mindustry.gen.Buildingc;

import newhorizon.contents.blocks.special.UpgraderBlock.UpgraderBlockBuild;
import newhorizon.contents.data.*;

public interface Scalablec extends Buildingc{
	public void resetUpgrade();
	
	public void drawConnected();
	
	public boolean isConnected();
	
    public UpgraderBlockBuild upgrader();
    
    public void setBaseData(UpgradeBaseData baseData);
    public void setAmmoData(UpgradeAmmoData baseData);
    
    public UpgradeBaseData getBaseData();
    public UpgradeAmmoData getAmmoData();
}

