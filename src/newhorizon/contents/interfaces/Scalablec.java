package newhorizon.contents.interfaces;

import newhorizon.contents.blocks.special.UpgraderBlock.UpgraderBlockBuild;
import newhorizon.contents.data.*;

public interface Scalablec{
	public void resetUpgrade();
	
	public void updateUpgradeBase(UpgradeBaseData importBaseData);
	
	public void drawConnected();
	
	public boolean isConnected();
	
    public UpgraderBlockBuild upgrader();
    
    public void setBaseData(UpgradeBaseData baseData);
    
    public UpgradeBaseData getBaseData();
}

