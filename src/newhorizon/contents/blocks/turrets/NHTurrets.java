package newhorizon.contents.blocks.turrets;

import arc.*;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ctype.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
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
import newhorizon.contents.effects.NHFx;

import static mindustry.type.ItemStack.*;

public class NHTurrets implements ContentList {

	//Load Mod Turrets
	public static Block
	ender, thurmix, argmot;

	@Override
	public void load() {
		argmot = new SpeedupTurret("argmot") {
			{
				alternate = true;
				spread = 6f;
				shots = 2;
				health = 960;
				requirements(Category.turret, with(NHItems.upgradeSort, 400, NHItems.seniorProcessor, 280));
				maxSpeedupScl = 4f;
				speedupPerShoot = 0.5f;
				powerUse = 8f;
				size = 3;
				range = 240;
				reloadTime = 45f;
				shootCone = 24f;
				shootSound = Sounds.laser;
				shootType = new SapBulletType() {
					{
						damage = 130f;
						status = new StatusEffect("actted"){{
							speedMultiplier = 0.875f;
							damage = 0.8f;
							reloadMultiplier = 0.75f;
						}};
						sapStrength = 0.45f;
						length = 250f;
						drawSize = 500f;
						shootEffect = hitEffect = NHFx.lightSkyCircleSplash;
						hitColor = color = NHColor.lightSky;
						despawnEffect = Fx.none;
						width = 0.62f;
						lifetime = 35f;
					}
				};
			}
		};
		
		
		ender = new ScalableTurret("end-of-era") {
			{
				chargeTime = 60f;
				chargeEffects = 2;
				chargeMaxDelay = 0f;
				chargeEffect = new Effect(chargeTime, e -> {
					Angles.randLenVectors(e.id, 3, 60 * Mathf.curve(e.fout(), 0.25f, 1f), (x, y) -> {
						Draw.color(NHColor.darkEnrColor);
						Fill.circle(e.x + x, e.y + y, e.fin() * 13f);
						Draw.color(NHColor.darkEnrColor, Color.black, 0.8f);
						Fill.circle(e.x + x, e.y + y, e.fin() * 7f);
					});
				});
				chargeBeginEffect = new Effect(chargeTime, e -> {
					Draw.color(NHColor.darkEnrColor);
					Fill.circle(e.x, e.y, e.fin() * 32);
					Lines.stroke(e.fin() * 3.7f);
					Lines.circle(e.x, e.y, e.fout() * 80);
					Draw.color(NHColor.darkEnrColor, Color.black, 0.8f);
					Fill.circle(e.x, e.y, e.fin() * 20);
				});

				requirements(Category.turret, with(NHItems.upgradeSort, 400, NHItems.seniorProcessor, 280));
				shootType = NHBullets.boltGene;
				powerUse = 30;
				size = 8;
				health = 15000;
				hasItems = true;
				heatColor = NHColor.lightEnrColor;
				reloadTime = 180f;
				recoilAmount = 2f;
				range = 800f;
				inaccuracy = 1f;
				cooldown = 0.01f;
				shootCone = 45f;
				shootSound = Sounds.laserbig;
			}
		};
		
		thurmix = new ItemTurret("thurmix") {
			
			@Override
			public void load(){
				super.load();
				baseRegion = Core.atlas.find(NewHorizon.NHNAME + "block-" + size);
			}
			
			{
				requirements(Category.turret, with(Items.copper, 105, Items.graphite, 95, Items.titanium, 60));
				ammo(
					NHItems.fusionEnergy, NHBullets.curveBomb
				);

				size = 5;
				range = 360;
				reloadTime = 75f;
				restitution = 0.03f;
				ammoEjectBack = 3f;
				inaccuracy = 13f;
				cooldown = 0.03f;
				recoilAmount = 3f;
				shootShake = 1f;
				burstSpacing = 3f;
				shots = 4;
				ammoUseEffect = Fx.shellEjectBig;
				health = 300 * size * size;
				shootSound = Sounds.laser;
			}
		};
	}
}









