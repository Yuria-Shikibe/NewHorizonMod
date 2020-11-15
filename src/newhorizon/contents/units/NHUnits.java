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
import newhorizon.NewHorizon;

import static mindustry.Vars.*;

public class NHUnits implements ContentList {
	public static
	UnitType
	tarlidor;
	
	@Override
	public void load() {
		tarlidor = new UnitType("tarlidor") {
			{
				canBoost = true;
				boostMultiplier = 1.5f;
				speed = 0.35f;
				hitSize = 40f;
				health = 12000f;
				buildSpeed = 1.8f;
				armor = 3f;
				commandLimit = 4;

				//abilities.add(new ShieldFieldAbility(20f, 40f, 60f * 4, 60f));
				ammoType = AmmoTypes.power;

				weapons.add(
					new Weapon("stiken") {{
						shootY = 4f;
						reload = 60f;
						x = 13f;
						alternate = true;
						ejectEffect = Fx.none;
						recoil = 2f;
						shootSound = Sounds.lasershoot;

						bullet = new NHLightningBoltBulletType(350) {
							{
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

					}}
				);
			}

		};

		//Load End
	}


}














