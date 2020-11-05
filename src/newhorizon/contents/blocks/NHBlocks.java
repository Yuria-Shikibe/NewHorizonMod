package newhorizon.contents.blocks;

import arc.*;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ctype.*;
import mindustry.content.*;

import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import newhorizon.NewHorizon;
import newhorizon.contents.items.*;
import newhorizon.contents.colors.*;
import newhorizon.contents.bullets.*;
import newhorizon.contents.blocks.drawers.*;
import newhorizon.contents.blocks.turrets.*;
import newhorizon.contents.blocks.special.*;
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.data.*;
import static newhorizon.contents.data.UpgradeData.*;

import static mindustry.type.ItemStack.*;
import static mindustry.Vars.*;

public class NHBlocks implements ContentList {

	//Load Mod Factories

	public static Block
	chargeWall, chargeWallLarge, eoeUpgrader,
	irdryonVault, blaster, unitSpawner;

	@Override
	public void load() {
		eoeUpgrader = new UpgraderBlock("end-of-era-upgrader"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 3;
			maxLevel = 6;
			toUpgradeClass = NHTurrets.ender;
			initUpgradeBaseData = new UpgradeBaseData();
			addUpgrades(
				new UpgradeAmmoData(
					"arc-9000", "description00", 0, NHBullets.boltGene, 300f,
					
					new ItemStack(NHItems.irayrondPanel, 3), new ItemStack(NHItems.metalOxhydrigen, 2)
				),
				new UpgradeAmmoData(
					"curve-bomb", "description01", 0, NHBullets.curveBomb, 300f,
					
					new ItemStack(NHItems.irayrondPanel, 3), new ItemStack(NHItems.metalOxhydrigen, 2)
				)
			);
			
            health = 1350;
		}};
		
		chargeWall = new ChargeWall("charge-wall"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 1;
			range = 120;
            health = 1350;
            effectColor = NHColor.lightSky;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.effect, with(NHItems.presstanium, 600, NHItems.metalOxhydrigen, 200, NHItems.irayrondPanel, 300));
			size = 2;
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
        
        unitSpawner = new UnitSpawner("unit-spawner"){{
			size = 2;
			requirements(Category.units, BuildVisibility.sandboxOnly, with());
			health = 1000000;
		}};
	}
}












