package newhorizon.content;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.FireBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHInterp;

public class NHBullets{
	public static String CIRCLE_BOLT, STRIKE, MISSILE_LARGE = "missile-large";
	
	public static BulletType ultFireball, basicSkyFrag;
	
	public static void load(){
		CIRCLE_BOLT = NewHorizon.name("circle-bolt");
		STRIKE = NewHorizon.name("strike");
		
		basicSkyFrag = new BasicBulletType(3.8f, 50){
			{
				speed = 12f;
				trailLength = 12;
				trailWidth = 2f;
				lifetime = 60;
				despawnEffect = NHFx.square45_4_45;
				hitEffect = new Effect(45f, e -> {
					Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin());
					Lines.stroke(1.75f * e.fout());
					if(NHSetting.enableDetails())Lines.spikes(e.x, e.y, 28 * e.finpow(), 5 * e.fout() + 8 * e.fin(NHInterp.parabola4Reversed), 4, 45);
					Lines.square(e.x, e.y, 14 * e.fin(Interp.pow3Out), 45);
				});
				knockback = 4f;
				width = 15f;
				height = 37f;
				lightningDamage = damage * 0.65f;
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				lightning = 2;
				lightningLength = lightningLengthRand = 3;
				smokeEffect = Fx.shootBigSmoke2;
				trailChance = 0.2f;
				trailEffect = NHFx.skyTrail;
				drag = 0.015f;
				hitShake = 2f;
				hitSound = Sounds.explosion;
			}
			
			@Override
			public void hit(Bullet b){
				super.hit(b);
				UltFire.createChance(b, 12, 0.0075f);
			}
		};
		
		ultFireball = new FireBulletType(1f, 10){{
			colorFrom = NHColor.lightSkyFront;
			colorMid = NHColor.lightSkyBack;
			
			lifetime = 12f;
			radius = 4f;
			
			trailEffect = NHFx.ultFireBurn;
		}
			@Override
			public void draw(Bullet b){
				Draw.color(colorFrom, colorMid, colorTo, b.fin());
				Fill.square(b.x, b.y, radius * b.fout(), 45);
				Draw.reset();
			}
			
			@Override
			public void update(Bullet b){
				if(Mathf.chanceDelta(fireTrailChance)){
					UltFire.create(b.tileOn());
				}
				
				if(Mathf.chanceDelta(fireEffectChance)){
					trailEffect.at(b.x, b.y);
				}
				
				if(Mathf.chanceDelta(fireEffectChance2)){
					trailEffect2.at(b.x, b.y);
				}
			}
		};
	}
}
