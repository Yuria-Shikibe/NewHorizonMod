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
				speed = 0.455f;
				hitSize = 20f;
				health = 12000f;
				buildSpeed = 1.8f;
				armor = 8f;
				rotateSpeed = 2.3f;
				
				canBoost = true;
          	  boostMultiplier = 1.5f;
            	fallSpeed = 0.016f;
				canDrown = false;
				mechStepParticles = true;
				mechStepShake = 0.15f;

				//abilities.add(new ShieldFieldAbility(20f, 40f, 60f * 4, 60f));
				ammoType = AmmoTypes.power;

				weapons.add(
					new Weapon("new-horizon-stiken") {{
						top = false;
						shootY = 8f;
						reload = 10f;
						x = 16f;
						alternate = true;
						ejectEffect = Fx.none;
						recoil = 2f;

						bullet = NHBullets.longLaser;
						shootSound = Sounds.laser;
					}}
				);
			}

		};

		//Load End
	}


}














