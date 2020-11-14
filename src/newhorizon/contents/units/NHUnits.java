package newhorizon.contents.units;

import arc.audio.*;
import arc.math.geom.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.struct.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.io.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

import newhorizon.contents.bullets.*;
import newhorizon.contents.colors.*;
import newhorizon.contents.effects.*;

import static mindustry.Vars.*;

public class NHUnits implements ContentList {
	public static
	UnitType
	tarlidor;

	@Override
	public void load() {
		tarlidor = new UnitType("tarlidor") {
			{
				defaultController = GroundAI::new;
				constructor = EntityMapping.map(25);
				canBoost = true;
				boostMultiplier = 1.5f;
				speed = 0.35f;
				hitSize = 40f;
				health = 12000f;
				buildSpeed = 1.8f;
				armor = 3f;
				commandLimit = 4;

				abilities.add(new ShieldFieldAbility(20f, 40f, 60f * 4, 60f));
				ammoType = AmmoTypes.power;

				weapons.add(
				new Weapon("stiken") {
					{
						shootY = 4f;
						reload = 60f;
						x = 13f;
						alternate = true;
						ejectEffect = Fx.none;
						recoil = 2f;
						shootSound = Sounds.lasershoot;

						bullet = new NHLightningBoltBulletType(350) {
							{
								healPercent = 5f;
								splashDamage = damage * 0.7f;
								splashDamageRadius = 40f;
								shootEffect = hitEffect = new Effect(26f, e -> {
									Draw.color(Pal.lancerLaser);
									Angles.randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
										Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
									});
								});
								lightningColor = Pal.lancerLaser;
							}
						};

					}
				},

				new Weapon("arc-blaster") {
					{
						rotate = true;
						top = false;
						shootY = 2f;
						reload = 30f;
						x = 4.5f;
						alternate = true;
						ejectEffect = Fx.none;
						recoil = 2f;
						shootSound = Sounds.lasershoot;

						bullet = new LightningBulletType() {{
							lightningColor = hitColor = Pal.lancerLaser;
							damage = 80f;
							lightningLength = 12;
							lightningLengthRand = 10;
							shootEffect = Fx.hitLancer;

							lightningType = new BulletType(0.0001f, 0f) {{
								lifetime = Fx.lightning.lifetime;
								hitEffect = Fx.hitLancer;
								despawnEffect = Fx.none;
								status = StatusEffects.shocked;
								statusDuration = 10f;
							}};
						}};
					}
				}

				);
			}

		};

		//Load End
	}


}














