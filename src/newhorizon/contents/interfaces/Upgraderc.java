package newhorizon.contents.interfaces;

import mindustry.gen.Buildingc;

public interface Upgraderc extends Buildingc{
	public Scalablec target();
	public void updateTarget();
	public boolean linkValid();
	
	public void updateUpgrading();
	public void completeUpgrade();
}

