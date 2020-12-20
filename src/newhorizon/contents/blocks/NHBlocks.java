package newhorizon.contents.blocks;

import mindustry.content.UnitTypes;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

import newhorizon.contents.effects.*;
import newhorizon.contents.items.*;
import newhorizon.contents.colors.*;
import newhorizon.contents.blocks.turrets.*;
import newhorizon.contents.blocks.special.*;
import newhorizon.contents.data.UpgradeDatas;

import static mindustry.type.ItemStack.*;

public class NHBlocks implements ContentList {

	//Load Mod Factories

	public static Block
	chargeWall, chargeWallLarge, eoeUpgrader, jumpGate,
	irdryonVault, blaster, unitSpawner;

	@Override
	public void load() {
		eoeUpgrader = new UpgraderBlock("end-of-era-upgrader"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 3;
			linkTarget = NHTurrets.ender;
			health = 2350;
			baseColor = NHColor.darkEnrColor;
			maxLevel = 10;
			toUpgradeClass = NHTurrets.ender;
			initUpgradeBaseData = UpgradeDatas.basicData;
			addUpgrades(
				UpgradeDatas.darkEnrlaser,
				UpgradeDatas.arc9000,
				UpgradeDatas.curveBomb,
				UpgradeDatas.airRaid,
				UpgradeDatas.decayLaser,
				UpgradeDatas.bombStorm
			);
		}};
		
		chargeWall = new ChargeWall("charge-wall"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 1;
			absorbLasers = true;
			range = 120;
            health = 1350;
            effectColor = NHColor.lightSky;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.effect, with(NHItems.presstanium, 600, NHItems.metalOxhydrigen, 200, NHItems.irayrondPanel, 300));
			size = 2;
			absorbLasers = true;
			range = 200;
            health = 1350 * size * size;
            effectColor = NHColor.lightSky;
		}};
		
		irdryonVault = new StorageBlock("irdryon-vault"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            health = 3500;
            itemCapacity = 2500;
        }};
        
        blaster = new Influencer("blaster"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            chargerOffset = 5.65f;
            rotateOffset = -45f;
            damage = 40;
            lightningDamage = 150;
            generateLiLen = 12;
            generateLiRand = 4;
            gettingBoltNum = 8;
            lightningColor = heatColor = NHColor.darkEnrColor;
            generateEffect = NHFx.blastgenerate;
            acceptEffect = NHFx.blastAccept;
            status = new StatusEffect("emp"){{
            	reloadMultiplier = 0.01f;
            	damage = 12f;
          	  effect = NHFx.emped;
				speedMultiplier = 0.24f;
            }};
            range = 280;
            health = 5000;
            knockback = 80f;         
            consumes.power(10f);
        }};

		jumpGate = new JumpGate("jump-gate"){{
			consumes.power(8f);
			health = 20000;
			spawnDelay = 20f;
			spawnReloadTime = 200f;
			spawnRange = 160f;
			range = 400f;
			squareStroke = 2.2f;

			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 8;
			addSets(
					new UnitsSet(0, UnitTypes.zenith),
					new UnitsSet(0, UnitTypes.eclipse),
					new UnitsSet(0, UnitTypes.quad)
			);
			//baseColor = NHColor.darkEnrColor;

		}};

        unitSpawner = new UnitSpawner("unit-spawner"){{
			size = 2;
		}};
	}
}












