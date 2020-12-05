package newhorizon.contents.bullets;

import arc.audio.*;
import arc.math.geom.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.struct.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.io.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

import newhorizon.contents.bullets.special.*;
import newhorizon.contents.colors.*;
import newhorizon.contents.effects.*;

import static mindustry.Vars.*;
import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class NHBullets implements ContentList {
	public static 
	BulletType boltGene, rapidBomb, airRaid, decayLaser, longLaser, darkEnrlaser,
			   curveBomb;

	@Override
	public void load(){
		darkEnrlaser = new ContinuousLaserBulletType(1000){
			{
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{NHColor.darkEnrColor.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.darkEnrColor.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.darkEnrColor, NHColor.darkEnr};
				width = 18f;
				length = 1000f;
				oscScl = 0.4f;
				oscMag = 1.5f;
				lifetime = 35f;
				lightColor = NHColor.darkEnrColor;
				hitEffect = NHFx.darkEnrCircleSplash;
				shootEffect = NHFx.darkEnergyShootBig;
				smokeEffect = NHFx.darkEnergySmokeBig;
			}
			
			@Override
			public void update(Bullet b) {
				super.update(b);
				if (b.timer(0, 8)) {
					NHFx.darkEnergySpread.at(b);
				}
			}
			
			@Override
			public void draw(Bullet b) {
				super.draw(b);
				color(NHColor.darkEnrColor);
				Fill.circle(b.x, b.y, 26f);
				color(NHColor.darkEnr);
				Fill.circle(b.x, b.y, 9f + 9f * b.fout());
				Draw.reset();
			}
		};
		
		decayLaser = new NHLaserBulletType(2000){{
			colors = new Color[]{NHColor.darkEnrColor.cpy().mul(1f, 1f, 1f, 0.3f), NHColor.darkEnrColor, Color.white};
			laserEffect = NHFx.darkEnergyLaserShoot;
			length = 880f;
			width = 22f;
			lengthFalloff = 0.6f;
			sideLength = 90f;
			sideWidth = 1.35f;
			sideAngle = 35f;
			largeHit = true;
			shootEffect = NHFx.darkEnergyShoot;
			smokeEffect = NHFx.darkEnergySmoke;
		}};
		
		longLaser = new LaserBulletType(500){{
			colors = new Color[]{Pal.lancerLaser.cpy().mul(1f, 1f, 1f, 0.3f), Pal.lancerLaser, Color.white};
			length = 360f;
			width = 12.7f;
			lengthFalloff = 0.6f;
			sideLength = 68f;
			sideWidth = 0.9f;
			sideAngle = 90f;
			largeHit = false;
			shootEffect = smokeEffect = Fx.none;
		}};
		
		rapidBomb = new NHTrailBulletType(9f, 200, "new-horizon-strike"){{
			hitSound = Sounds.explosion;
			drawSize = 120f;
			hitShake = despawnShake = 1.3f;
			scaleVelocity = true;
			lightning = 2;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 4;
			splashDamageRadius = 18f;
			splashDamage = lightningDamage = 0.35f * damage;
			height = 42f;
			width = 11f;
			lifetime = 500;
			trailColor = backColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = Color.white;
			hitEffect = NHFx.darkEnrCircleSplash;
			shootEffect = NHFx.darkEnergyShoot;
			smokeEffect = NHFx.darkEnergySmoke;
			shrinkX = shrinkY = 0;
		}};
		
		airRaid = new NHTrailBulletType(9f, 800, "new-horizon-strike"){
			
			@Override
			public void init(Bullet b){
				super.init(b);
				b.lifetime(b.lifetime() + 9f);
			}

			{
				hitSound = Sounds.explosionbig;
				trailChance = 0.075f;
				trailEffect = NHFx.polyTrail;
				drawSize = 120f;
				homingPower = 0.08f;
				homingRange = 400f;
				homingDelay = 12;
				scaleVelocity = true;
				hitShake = despawnShake = 5f;
				lightning = 3;
				lightningCone = 360;
				lightningLengthRand = lightningLength = 12;
				shootEffect = NHFx.darkEnergyShoot;
				smokeEffect = NHFx.darkEnergySmoke;
				shrinkX = shrinkY = 0;
				splashDamageRadius = 120f;
				splashDamage = lightningDamage = 0.65f * damage;
				height = 66f;
				width = 20f;
				lifetime = 500;
				trailColor = backColor = lightColor = lightningColor = NHColor.darkEnrColor;
				frontColor = Color.white;
				
				hitEffect = NHFx.mediumDarkEnergyHit;
			}
				
		};
		
		curveBomb = new ArtilleryBulletType(4f, 0f) {
			@Override
			public void init(Bullet b) {
				if (b == null)return;
				b.data(new Vec2(b.x, b.y));
			}

			@Override
			public void update(Bullet b) {

			}

			@Override
			public void draw(Bullet b) {
				Vec2 from = (Vec2)b.data();
				float angle = b.angleTo(from.x, from.y) - 180;
				float dst = b.dst(from.x, from.y);

				Vec2
				vec1 = new Vec2().trns(angle, dst / 3),
				vec2 = new Vec2().trns(angle, dst / 3 * 2);

				color(lightColor, frontColor, b.fout());
				stroke(5f * b.fout());

				float len = Mathf.curve(b.fslope(), 0.1f, 0.8f) * 60 + b.fin() * 50;
				randLenVectors(b.id, 2, len, (x, y) -> {
					randLenVectors(b.id / 2 + 12, 1, len, (x2, y2) -> {
						curve(
							from.x,  		 	from.y,
							from.x + vec1.x + x,  from.y + vec1.y + y,
							from.x + vec2.x + x2, from.y + vec2.y + y2,
							b.x, b.y,
							16
						);
					});
				});
				Fill.circle(from.x, from.y, 3.5f * b.fout() * getStroke() / 2f);
				Fill.circle(b.x, b.y, 2 * b.finpow() + 4 * b.fslope());
				reset();
			}

			@Override
			public void despawned(Bullet b) {
				super.despawned(b);
				NHLightningBolt.generateRange(new Vec2(b.x, b.y), b.team(), 80, 5, 2, 120 * b.damageMultiplier(), lightColor, true, NHLightningBolt.WIDTH);
			}

			{
				hitShake = 8;
				hitSound = Sounds.explosionbig;
				drawSize = 400;
				lightColor = backColor = lightningColor = NHColor.thurmixRed;
				frontColor = NHColor.thurmixRedLight;
				
				shootEffect = new Effect(90f, 160f, e -> {
					color(lightColor, frontColor, e.fout());
					Drawf.tri(e.x, e.y, 5 * e.fout(), Mathf.curve(e.fout(), 0, 0.1f) * 80, e.rotation + 90);
					Drawf.tri(e.x, e.y, 5 * e.fout(), Mathf.curve(e.fout(), 0, 0.1f) * 80, e.rotation + 270);
				});

				despawnEffect = new Effect(32f, e -> {
					color(lightColor, frontColor, e.fout());
					stroke(e.fout() * 2);
					circle(e.x, e.y, e.fin() * 40);
					Fill.circle(e.x, e.y, e.fout() * e.fout() * 10);
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> {
						Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
					});
				});

				smokeEffect = new Effect(45f, e -> {
					color(lightColor, frontColor, e.fout());
					Drawf.tri(e.x, e.y, 4 * e.fout(), 28, e.rotation + 90);
					Drawf.tri(e.x, e.y, 4 * e.fout(), 28, e.rotation + 270);
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> {
						Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
					});
				});
			}

		};

		boltGene = new ArtilleryBulletType(2.75f, 100) {
			@Override
			public void update(Bullet b) {
				Effect.shake(2, 2, b);
				if (b.timer(0, 8)) {
					NHFx.darkEnergySpread.at(b);
				}

				if (b.timer(2, 8) && (b.lifetime - b.time) > NHLightningBolt.BOLTLIFE) {
					NHLightningBolt.generateRange(b, 240, 15, 1, splashDamage * b.damageMultiplier(), NHColor.darkEnrColor, Mathf.chance(Time.delta * 0.13), 1.33f * NHLightningBolt.WIDTH);
				}
			}

			@Override
			public void init(Bullet b) {
				b.vel.scl(1 + drag * b.lifetime / b.type.speed * 1.3f);
				b.lifetime(b.lifetime * 1.2f);
			}

			@Override
			public void draw(Bullet b) {
				color(NHColor.darkEnrColor);
				Fill.circle(b.x, b.y, 20);
				color(NHColor.darkEnr);
				Fill.circle(b.x, b.y, 4f + 8f * Mathf.curve(b.fout(), 0.1f, 0.35f));
			}

			@Override
			public void despawned(Bullet b) {
				for (int i = 0; i < Mathf.random(4f, 7f); i++) {
					Vec2 randomPos = new Vec2(b.x + Mathf.range(200), b.y + Mathf.range(200));
					hitSound.at(randomPos, Mathf.random(0.9f, 1.1f) );
					NHLightningBolt.generate(new Vec2(b.x, b.y), randomPos, b.team(), NHColor.darkEnrColor, 1.7f * NHLightningBolt.WIDTH, 2, hitPos -> {
						for (int j = 0; j < 4; j++) {
							Lightning.create(b.team(), NHColor.darkEnrColor, this.splashDamage * b.damageMultiplier(), hitPos.getX(), hitPos.getY(), Mathf.random(360), Mathf.random(8, 12));
						}
						Damage.damage(b.team(), hitPos.getX(), hitPos.getY(), 80f, 8 * this.splashDamage * b.damageMultiplier());
						NHFx.lightningHit.at(hitPos);
					});
				}

				super.despawned(b);
				
			}

			{
				drag = 0.0065f;
				fragLifeMin = 0.3f;
				fragBullets = 11;

				fragBullet = new ArtilleryBulletType(3.75f, 260) {
					@Override
					public void update(Bullet b) {
						if (b.timer(0, 2)) {
							new Effect(22, e -> {
								color(NHColor.darkEnrColor, Color.black, e.fin());
								Fill.poly(e.x, e.y, 6, 4.7f * e.fout(), e.rotation);
							}).at(b.x, b.y, b.rotation());
						}
					}

					{
						despawnEffect = hitEffect = NHFx.darkErnExplosion;
						knockback = 12f;
						lifetime = 90f;
						width = 17f;
						height = 42f;
						collidesTiles = false;
						splashDamageRadius = 80f;
						splashDamage = damage * 0.6f;
						backColor = lightColor = lightningColor = NHColor.darkEnrColor;
						frontColor = Color.white;
						lightning = 3;
						lightningLength = 8;
						smokeEffect = Fx.shootBigSmoke2;
						hitShake = 8f;
						hitSound = Sounds.plasmaboom;
						status = StatusEffects.sapped;
						statusDuration = 60f * 10;
					}
				};
				hitSound = Sounds.explosionbig;
				drawSize = 40;
				splashDamageRadius = 240;
				splashDamage = 8000;
				collidesTiles = true;
				pierce = false;
				collides = false;
				collidesAir = false;
				ammoMultiplier = 1;
				lifetime = 300;
				hitEffect = Fx.none;
				despawnEffect = Fx.none;
				hitEffect = NHFx.largeDarkEnergyHit;
				shootEffect = NHFx.darkEnergyShootBig;
				smokeEffect = NHFx.darkEnergySmokeBig;
			}

		};

	}
}














