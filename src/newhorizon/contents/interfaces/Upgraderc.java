package newhorizon.contents.interfaces;

import newhorizon.contents.data.*;
import mindustry.gen.Buildingc;

public interface Upgraderc extends Buildingc{
	public Scalablec target();
	public boolean linkValid();
	public boolean isUpgrading();
	public boolean canUpgrade(UpgradeData data);
	
	public void updateTarget();
	public void upgraderTableBuild();
	public void updateUpgrading();
	public void completeUpgrade();
	public void upgradeData(UpgradeData data);
	
}

