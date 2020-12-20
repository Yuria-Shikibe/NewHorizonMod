package newhorizon.contents.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.Effect;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.*;

import newhorizon.contents.bullets.*;
import newhorizon.contents.colors.NHColor;
import newhorizon.contents.effects.NHFx;

import static arc.graphics.g2d.Draw.color;

public class NHUnits implements ContentList {

	public static
	UnitType
	tarlidor;
	
	@Override
	public void load() {
		tarlidor = new UnitType("tarlidor") {
			{
				abilities.add(new ShieldRegenFieldAbility(50.0F, 50F, 600.0F, 800.0F));

				constructor = UnitMechBoost::new;

				trailLength = 6;
				trailScl = 0.8f;
				engineOffset = 13.0F;
				engineSize = 6.5F;
				speed = 0.4f;
				hitSize = 20f;
				health = 15000f;
				buildSpeed = 1.8f;
				armor = 12f;
				rotateSpeed = 3.3f;
				hovering = true;
				canDrown = true;
            	fallSpeed = 0.016f;
				mechStepParticles = true;
				mechStepShake = 0.15f;
				canBoost = true;
				landShake = 6f;
				boostMultiplier = 3.5f;
				ammoType = AmmoTypes.powerHigh;

				weapons.add(
					new Weapon("new-horizon-stiken") {{
						top = false;
						shake = 3f;
						shootY = 13f;
						reload = 50f;
						shots = 2;
						shotDelay = 7f;
						x = 17.5f;
						inaccuracy = 3.0F;
						alternate = true;
						ejectEffect = Fx.none;
						recoil = 4.4f;
						bullet = new NHTrailBulletType(7.4f, 60) {{
							hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
							lifetime = 80f;
							pierce = pierceBuilding = true;
							width = 13f;
							height = 40f;
							backColor = lightColor = lightningColor = trailColor = NHColor.lightSky;
							frontColor = Color.white;
							lightning = 3;
							lightningDamage = damage / 2;
							lightningLength = lightningLengthRand = 5;
							smokeEffect = Fx.shootBigSmoke2;
							hitShake = 4f;
							hitSound = Sounds.plasmaboom;
							shrinkX = shrinkY = 0f;
						}};
						shootSound = Sounds.laser;
					}},

                    new Weapon("new-horizon-arc-blaster") {{
                        top = true;
                        rotate = true;
                        shootY = 12f;
                        reload = 30f;

						shots = 3;
						inaccuracy = 6.0F;
						velocityRnd = 0.38f;
                        x = 8f;
                        alternate = false;
                        ejectEffect = Fx.none;
                        recoil = 1.7f;
                        bullet = new BasicBulletType(3.3f, 80) {
							@Override
							public void update(Bullet b) {
								if (b.timer(0, 2)) {
									new Effect(22, e -> {
										color(NHColor.lightSky, Pal.gray, e.fin());
										Fill.poly(e.x, e.y, 6, 4.7f * e.fout(), e.rotation);
									}).at(b.x, b.y, b.rotation());
								}
							}

							{
								lifetime = 200f;
								despawnEffect = hitEffect = NHFx.lightSkyCircleSplash;
								knockback = 12f;
								width = 15f;
								height = 37f;
								splashDamageRadius = 40f;
								splashDamage = lightningDamage = damage * 0.6f;
								backColor = lightColor = lightningColor = NHColor.lightSky;
								frontColor = Color.white;
								lightning = 3;
								lightningLength = 8;
								smokeEffect = Fx.shootBigSmoke2;
								hitShake = 2f;
								hitSound = Sounds.spark;
							}};
                        shootSound = Sounds.plasmaboom;
                    }}

				);
			}

		};

		//Load End
	}

}














