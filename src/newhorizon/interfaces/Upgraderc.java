package newhorizon.interfaces;

import arc.scene.ui.layout.Table;
import mindustry.gen.Buildingc;
import newhorizon.feature.UpgradeData.DataEntity;

public interface Upgraderc extends Buildingc, Linkablec{
	void buildSwitchAmmoTable(Table t, boolean setting);
	boolean isUpgrading();
	boolean canUpgrade(DataEntity data);
	void consumeItems(DataEntity data);
	void updateTarget();
	void upgraderTableBuild();
	void updateUpgrading();
	void completeUpgrade();
	void upgradeData(DataEntity data);
}

