package newhorizon.contents.data;

import arc.struct.*;
import mindustry.gen.*;
import mindustry.type.ItemStack;
import mindustry.content.Items;
import mindustry.content.Bullets;
import mindustry.content.Fx;

import newhorizon.contents.items.NHItems;
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.bullets.NHBullets;
import newhorizon.contents.bullets.NHBullets;

import static mindustry.Vars.*;

public class UpgradeDatas{ 
	public static final UpgradeBaseData
		basicData = new UpgradeBaseData("Upgrade turret", "N/A", 600f, 
			new ItemStack(NHItems.upgradeSort, 100),
			new ItemStack(NHItems.darkEnergy, 50),
			new ItemStack(NHItems.metalOxhydrigen, 300),
			new ItemStack(Items.surgeAlloy, 125)
		){{
			timeCostCoefficien = 0.25f;
			itemCostCoefficien = 2;
			speedMPL = 0.035f;
			defenceMPL = 0.05f;
		}};
		
	//UpgradeAmmoDatas ↓
		
	public static final UpgradeAmmoData
		darkEnrlaser = new UpgradeAmmoData(
			"dark-enr-laser", "description08", NHBullets.darkEnrlaser, 300f, 7,
			new ItemStack(Items.surgeAlloy, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laserblast;
			continuousTime = 240f;
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = NHFx.darkEnergyChargeBegin.lifetime;
		}},
		
		decayLaser = new UpgradeAmmoData(
			"decay-laser", "description", NHBullets.decayLaser, 300f, 1,
			new ItemStack(Items.surgeAlloy, 250),
			new ItemStack(NHItems.irayrondPanel, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laser;
			burstSpacing = 6f;
			salvos = 8;
			randX = 2f * tilesize;
			inaccuracy = 5;
		}},
		
		bombStorm = new UpgradeAmmoData(
			"bomb-storm", "description00", NHBullets.rapidBomb, 900f, 2,
			new ItemStack(NHItems.upgradeSort, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.bigshot;
			inaccuracy = 9f;
			velocityInaccuracy = 0.095f;
			burstSpacing = 2f;
			salvos = 28;
			randX = 2.1f * tilesize;
		}},
		
		arc9000 = new UpgradeAmmoData(
			"arc-9000", "description00", NHBullets.boltGene, 900f, 8,
			new ItemStack(NHItems.upgradeSort, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laserblast;
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = NHFx.darkEnergyChargeBegin.lifetime;
		}},
		
		curveBomb = new UpgradeAmmoData(
			"curve-bomb", "description01", NHBullets.curveBomb, 300f, 0,
			new ItemStack(NHItems.irayrondPanel, 300),
			new ItemStack(NHItems.metalOxhydrigen, 200)
		){{
			shootSound = Sounds.laser;
			randX = 2f * tilesize;
			salvos = 7;
			inaccuracy = 10;
			velocityInaccuracy = 0.08f;
		}},
		
		airRaid = new UpgradeAmmoData(
			"air-raid", "description02", NHBullets.airRaid, 500f, 6,
			new ItemStack(NHItems.upgradeSort, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.railgun;
			inaccuracy = 6;
			velocityInaccuracy = 0.08f;
			burstSpacing = 9f;
			salvos = 6;
			randX = 2f * tilesize;
		}};
}
















