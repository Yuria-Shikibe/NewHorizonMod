package newhorizon.contents.data;

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
	public static final UpgradeData
		basicData = new UpgradeBaseData("Upgrade turret", "N/A", 600f, 
			new ItemStack(NHItems.upgradeSort, 100),
			new ItemStack(NHItems.darkEnergy, 50),
			new ItemStack(NHItems.metalOxhydrigen, 300),
			new ItemStack(Items.surgeAlloy, 125)
		){{
			timeCostCoefficien = 0.25f;
			itemCostCoefficien = 2;
			speedMPL = defenceMPL = 0.15f;
		}},
		
		//UpgradeAmmoDatas ↓
		
		arc9000 = new UpgradeAmmoData(
			"arc-9000", "description00", NHBullets.boltGene, 300f, 0,
			new ItemStack(NHItems.upgradeSort, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = 60f;
		}},
		
		curveBomb = new UpgradeAmmoData(
			"curve-bomb", "description01", NHBullets.curveBomb, 300f, 0,
			new ItemStack(NHItems.irayrondPanel, 3),
			new ItemStack(NHItems.metalOxhydrigen, 2)
		){{
			randX = 2f * tilesize;
			salvos = 7;
			inaccuracy = 10;
			velocityInaccuracy = 0.08f;
		}},
		
		airRaid = new UpgradeAmmoData(
			"air-raid", "description02", NHBullets.airRaid, 300f, 2,
			new ItemStack(NHItems.upgradeSort, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			inaccuracy = 6;
			velocityInaccuracy = 0.08f;
			burstSpacing = 9f;
			salvos = 6;
			randX = 2f * tilesize;
		}};
}
















