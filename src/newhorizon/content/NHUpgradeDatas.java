package newhorizon.content;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.ctype.ContentList;
import mindustry.gen.Sounds;
import mindustry.type.ItemStack;
import newhorizon.feature.UpgradeData;

import static mindustry.Vars.tilesize;

public class NHUpgradeDatas implements ContentList{
	public static final Seq<UpgradeData> all = new Seq<>();
	
	public static UpgradeData none, darkEnrlaser, decayLaser, bombStorm, arc9000, curveBomb, airRaid, strikeRocket;
	
	@Override
	public void load(){
		none = new UpgradeData();
		
		strikeRocket = new UpgradeData("rocket-strike", NHBullets.strikeRocket, 500f, 5,
				new ItemStack(NHItems.seniorProcessor, 100),
				new ItemStack(NHItems.darkEnergy, 50),
				new ItemStack(NHItems.irayrondPanel, 1000),
				new ItemStack(Items.graphite, 125)
		){{
			shootSound = Sounds.railgun;
			burstSpacing = 5f;
			salvos = 11;
			randX = 3f * tilesize;
			
			reloadTime = 120f;
			
			isLeveled = true;
			reloadSpeedUp = 0.03f;
			defenceUp = 0.01f;
			maxLevel = 8;
		}};
		
		bombStorm = new UpgradeData(
				"bomb-storm", NHBullets.rapidBomb, 900f, 2,
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
			
			isLeveled = true;
			reloadSpeedUp = 0.05f;
			defenceUp = 0.0125f;
			maxLevel = 8;
			defaultLevel = 1;
		}};
		
		darkEnrlaser = new UpgradeData(
			"dark-enr-laser", NHBullets.darkEnrlaser, 300f, 7,
			new ItemStack(Items.surgeAlloy, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laserblast;
			continuousTime = 240f;
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = NHFx.darkEnergyChargeBegin.lifetime;
			
			isLeveled = true;
			reloadSpeedUp = 0.03f;
			defenceUp = 0.01f;
			maxLevel = 4;
		}};
		
		decayLaser = new UpgradeData(
				"decay-laser", NHBullets.decayLaser, 300f, 1,
				new ItemStack(Items.surgeAlloy, 250),
				new ItemStack(NHItems.irayrondPanel, 500),
				new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laser;
			burstSpacing = 6f;
			salvos = 8;
			randX = 2f * tilesize;
			inaccuracy = 5;
		}};
		
		arc9000 = new UpgradeData(
				"arc-9000", NHBullets.boltGene, 900f, 8,
				new ItemStack(NHItems.upgradeSort, 250),
				new ItemStack(NHItems.darkEnergy, 500),
				new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laserblast;
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = NHFx.darkEnergyChargeBegin.lifetime;
		}};
		
		curveBomb = new UpgradeData(
				"curve-bomb", NHBullets.curveBomb, 300f, 0,
				new ItemStack(NHItems.irayrondPanel, 300),
				new ItemStack(NHItems.metalOxhydrigen, 200)
		){{
			shootSound = Sounds.laser;
			randX = 2f * tilesize;
			salvos = 7;
			inaccuracy = 10;
			velocityInaccuracy = 0.08f;
		}};
		
		airRaid = new UpgradeData(
				"air-raid", NHBullets.airRaid, 500f, 6,
				new ItemStack(NHItems.upgradeSort, 250),
				new ItemStack(NHItems.darkEnergy, 500),
				new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = NHSounds.launch;
			inaccuracy = 6;
			velocityInaccuracy = 0.08f;
			burstSpacing = 9f;
			salvos = 6;
			randX = 2f * tilesize;
		}};
	}
	
	
}
















