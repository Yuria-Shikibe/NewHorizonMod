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
	
	public static UpgradeData
			longRangeShoot, longRangeShootRapid, longRangeShootSplash, mineShoot,
			none, darkEnrlaser, decayLaser, bombStorm, arc9000, curveBomb, airRaid, strikeRocket, posLightning;
	
	@Override
	public void load(){
		none = new UpgradeData();
		
		longRangeShoot = new UpgradeData("long-range-shoot-0", NHBullets.longRangeShoot, 150f,
			new ItemStack(NHItems.presstanium, 80), new ItemStack(NHItems.juniorProcessor, 80)
		){{
			defaultLevel = 1;
			shootSound = Sounds.plasmaboom;
		}};
		
		longRangeShootRapid = new UpgradeData("long-range-shoot-2", NHBullets.longRangeShootSplash, 250f,
			new ItemStack(Items.plastanium, 80), new ItemStack(NHItems.juniorProcessor, 80), new ItemStack(NHItems.zeta, 120)
		){{shootSound = Sounds.plasmaboom;}};
		
		longRangeShootSplash = new UpgradeData("long-range-shoot-1", NHBullets.longRangeShootRapid, 250f,
			new ItemStack(Items.graphite, 120), new ItemStack(NHItems.juniorProcessor, 80), new ItemStack(NHItems.zeta, 120)
		){{shootSound = Sounds.plasmaboom;}};
		
		mineShoot = new UpgradeData("mine-shoot", NHBullets.mineShoot, 250f,
			new ItemStack(Items.blastCompound, 60), new ItemStack(NHItems.juniorProcessor, 80)
		){{
			burstSpacing = 6f;
			salvos = 6;
			inaccuracy = 12f;
			shootSound = Sounds.plasmaboom;
		}};
		
		posLightning = new UpgradeData("lightning", NHBullets.darkEnrLightning, 150f,
			new ItemStack(NHItems.seniorProcessor, 150),
			new ItemStack(NHItems.multipleSteel, 120)
		){{
			shootSound = NHSounds.gauss;
			defaultLevel = 1;
			burstSpacing = 7f;
			salvos = 7;
			randX = 3f * tilesize;
			reloadTime = 150f;
			
			isLeveled = true;
			reloadSpeedUp = 0.03f;
			defenceUp = 0.01f;
			maxLevel = 4;
		}};
		
		strikeRocket = new UpgradeData("rocket-strike", NHBullets.strikeRocket, 600f,
			new ItemStack(NHItems.seniorProcessor, 100),
			new ItemStack(NHItems.darkEnergy, 50),
			new ItemStack(NHItems.irayrondPanel, 100),
			new ItemStack(NHItems.upgradeSort, 400)
		){{
			shootSound = Sounds.railgun;
			burstSpacing = 5.5f;
			salvos = 9;
			randX = 3f * tilesize;
			
			reloadTime = 120f;
			
			isLeveled = true;
			reloadSpeedUp = 0.03f;
			defenceUp = 0.01f;
			maxLevel = 8;
		}};
		
		bombStorm = new UpgradeData(
			"bomb-storm", NHBullets.rapidBomb, 300f,
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
			"dark-enr-laser", NHBullets.darkEnrlaser, 900f,
			new ItemStack(Items.surgeAlloy, 250),
			new ItemStack(NHItems.darkEnergy, 500),
			new ItemStack(NHItems.thermoCoreNegative, 150)
		){{
			shootSound = Sounds.laserblast;
			continuousTime = 240f;
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = NHFx.darkEnergyChargeBegin.lifetime;
		}};
		
		decayLaser = new UpgradeData(
				"decay-laser", NHBullets.decayLaser, 700f,
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
				"arc-9000", NHBullets.arc_9000, 3600f,
				new ItemStack(NHItems.upgradeSort, 1200),
				new ItemStack(NHItems.darkEnergy, 1000),
				new ItemStack(NHItems.thermoCoreNegative, 800),
				new ItemStack(NHItems.seniorProcessor, 800)
		){{
			reloadTime = 360f;
			shootSound = Sounds.laserblast;
			chargeEffect = NHFx.darkEnergyCharge;
			chargeBeginEffect = NHFx.darkEnergyChargeBegin;
			chargeTime = NHFx.darkEnergyChargeBegin.lifetime;
			chargeSound = NHSounds.railGunCharge;
		}};
		
		curveBomb = new UpgradeData(
				"curve-bomb", NHBullets.curveBomb, 300f,
				new ItemStack(NHItems.irayrondPanel, 300),
				new ItemStack(NHItems.metalOxhydrigen, 200)
		){{
			shootSound = Sounds.laser;
			randX = 2f * tilesize;
			salvos = 8;
			burstSpacing = 8;
			reloadTime = 180f;
			inaccuracy = 10;
			velocityInaccuracy = 0.08f;
		}};
		
		airRaid = new UpgradeData(
				"air-raid", NHBullets.airRaid, 1200f,
				new ItemStack(NHItems.upgradeSort, 1000),
				new ItemStack(NHItems.darkEnergy, 500),
				new ItemStack(NHItems.thermoCoreNegative, 550)
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
















