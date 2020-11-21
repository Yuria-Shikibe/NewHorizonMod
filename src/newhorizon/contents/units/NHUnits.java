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
				constructor = EntityMapping.idMap[4];
				speed = 0.35f;
				hitSize = 40f;
				health = 12000f;
				buildSpeed = 1.8f;
				armor = 8f;
				
				canDrown = false;
				mechFrontSway = 0.3f;

				mechStepParticles = true;
				mechStepShake = 0.15f;

				//abilities.add(new ShieldFieldAbility(20f, 40f, 60f * 4, 60f));
				ammoType = AmmoTypes.power;

				weapons.add(
					new Weapon("stiken") {{
						top = false;
						shootY = 4f;
						reload = 60f;
						x = 16f;
						alternate = true;
						ejectEffect = Fx.none;
						recoil = 2f;

						bullet = new SapBulletType() {
					{
						damage = 130f;
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

					}}
				);
			}

		};

		//Load End
	}


}














