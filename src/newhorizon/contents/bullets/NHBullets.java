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
import newhorizon.contents.effects.NHFx;

import static mindustry.Vars.*;
import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class NHBullets implements ContentList {
	public static 
	BulletType boltGene, airRaid, decayLaser, 
			   curveBomb;

	@Override
	public void load() {
		decayLaser = new LaserBulletType(1350f){{
			colors = {NHColor.darkEnrColor.cpy().mul(1f, 1f, 1f, 0.4f), NHColor.darkEnrColor, Color.white};
			laserEffect = NHFx.darkEnergyLaserShoot;
			length = 880f;
			width = 16f;
			lengthFalloff = 0.6f;
			sideLength = 45f;
			sideWidth = 1f;
			sideAngle = 60f;
			largeHit = true;
			shootEffect = NHFx.darkEnergyShoot;
			smokeEffect = NHFx.darkEnergySmoke;
		}};
		
		airRaid = new BasicBulletType(9f, 750, "new-horizon-strike"){
			
			@Override
			public void init(Bullet b){
				super.init(b);
				b.lifetime(b.lifetime() + 16f);
			}
			
			@Override
			public void draw(Bullet b){
				float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);

				Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

				mixcol(mix, mix.a);
				color(backColor);
				
				for(int i : Mathf.signs){
					Drawf.tri(b.x, b.y, 4.3f, 55 * b.fout(), b.rotation() - 180 + 30 * i);
				}
				
				rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
				color(frontColor);
				rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);
				Draw.reset();
        
				color(lightColor);
				Draw.reset();
			}
			
			@Override
			public void update(Bullet b){
				if(b.time() > 11){
					new Effect(32f, e -> {
						color(lightColor, Pal.gray, e.fin() * 0.7f);
						stroke(e.fout() * 4.2f);
						lineAngle(e.x, e.y, e.rotation - 180, e.fout() * 40 + 50); 
					}).at(b.x, b.y, b.rotation());
				}
				
				if(b.timer.get(0,3)){
					new Effect(25f, e -> {
						color(lightColor, Pal.gray, e.fin() * 0.7f);
						randLenVectors(e.id, 4, 60f * e.fin(), e.rotation - 180, 20, (x, y) -> {
							Fill.poly(e.x + x, e.y + y, 6, 5.5f * e.fslope() * e.fout());
						});
					}).at(b.x, b.y, b.rotation());
				}
			}

			{
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
				height = 60f;
				width = 18f;
				lifetime = 500;
				backColor = lightColor = lightningColor = NHColor.darkEnrColor;
				frontColor = Color.white;
				despawnEffect = Fx.none;
				hitEffect = new Effect(25, e -> {
					color(lightColor);
					stroke(e.fout() * 3);
					circle(e.x, e.y, e.fin() * 80);
					stroke(e.fout() * 1.75f);
					circle(e.x, e.y, e.fin() * 60);
					
					stroke(e.fout() * 2.25f);
					randLenVectors(e.id + 1, 12, 1f + 60f * e.finpow(), (x, y) -> {
						lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 12f);
					});
			
					Fill.circle(e.x, e.y, e.fout() * 22);
					color(lightColor, Color.black, 0.8f);
					Fill.circle(e.x, e.y, e.fout() * 14);
				});
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
				Effect.shake(10f, 8f, b);
				despawnEffect.at(b);
				Sounds.explosionbig.at(b, Mathf.random(0.9f, 1.1f));
				NHLightningBolt.generateRange(new Vec2(b.x, b.y), b.team(), 80, 5, 2, 120 * b.damageMultiplier(), lightColor, true, NHLightningBolt.WIDTH);
				Damage.damage(b.team(), b.x, b.y, this.splashDamageRadius, this.splashDamage * b.damageMultiplier());
			}

			{
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

		boltGene = new ArtilleryBulletType(2.75f, 1500) {
			@Override
			public void update(Bullet b) {
				Effect.shake(2, 1, b);
				if (b.timer(0, 9)) {
					Effect.shake(2, 2, b);
					new Effect(32f, e -> {
						randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
							color(NHColor.darkEnrColor);
							Fill.circle(e.x + x, e.y + y, e.fout() * 15f);
							color(NHColor.darkEnrColor, Color.black, 0.8f);
							Fill.circle(e.x + x, e.y + y, e.fout() * 9f);
						});
					}).at(b);
				}


				if (b.timer(2, 8) && (b.lifetime - b.time) > NHLightningBolt.BOLTLIFE) {
					NHLightningBolt.generateRange(b, 240, 15, 1, 300 * b.damageMultiplier(), NHColor.darkEnrColor, Mathf.chance(Time.delta * 0.13), NHLightningBolt.WIDTH);
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
				color(NHColor.darkEnrColor, Color.black, 0.8f);
				Fill.circle(b.x, b.y, 4f + 8f * Mathf.curve(b.fout(), 0.1f, 0.35f));
			}

			@Override
			public void despawned(Bullet b) {
				for (int i = 0; i < Mathf.random(4f, 7f); i++) {
					Vec2 randomPos = new Vec2(b.x + Mathf.range(200), b.y + Mathf.range(200));
					
					NHLightningBolt.generate(new Vec2(b.x, b.y), randomPos, b.team(), NHColor.darkEnrColor, 1f + NHLightningBolt.WIDTH, 2, hitPos -> {
						for (int j = 0; j < 4; j++) {
							Lightning.create(b.team(), NHColor.darkEnrColor, this.splashDamage * b.damageMultiplier(), hitPos.getX(), hitPos.getY(), Mathf.random(360), Mathf.random(8, 12));
						}
						Damage.damage(b.team(), hitPos.getX(), hitPos.getY(), 80f, this.splashDamage * b.damageMultiplier());
						NHFx.lightningHit.at(hitPos);
					});
				}

				for (int i = 0; i < fragBullets; i++) {
					float len = Mathf.random(1f, 7f);
					float a = b.rotation() + Mathf.range(fragCone / 2);
					fragBullet.create(b, b.x + trnsx(a, len), b.y + trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
				}

				Effect.shake(20f, 16f, (Position)b);
				Damage.status(b.team, b.x, b.y, splashDamageRadius, status, statusDuration, true, true);
				Sounds.explosionbig.at(b);
				Damage.damage(b.team(), b.x, b.y, this.splashDamageRadius, this.splashDamage * b.damageMultiplier());
				new Effect(60, e -> {
					color(NHColor.darkEnrColor);
					Fill.circle(e.x, e.y, e.fout() * 44);
					stroke(e.fout() * 3.7f);
					circle(e.x, e.y, e.fin() * 80);
					stroke(e.fout() * 2.5f);
					circle(e.x, e.y, e.fin() * 45);
					randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
						stroke(e.fout() * 3.2f);
						lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
					});
					color(NHColor.darkEnrColor, Color.black, 0.8f);
					Fill.circle(e.x, e.y, e.fout() * 30);
				}).at(b);
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
						splashDamage = damage;
						backColor = lightColor = lightningColor = NHColor.darkEnrColor;
						frontColor = Color.white;
						lightning = 3;
						lightningLength = 8;
						smokeEffect = Fx.shootBigSmoke2;
						hitShake = 8f;

						status = StatusEffects.sapped;
						statusDuration = 60f * 10;
					}
				};

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
				shootEffect = new Effect(40f, 100, e -> {
					color(NHColor.darkEnrColor);
					stroke(e.fout() * 3.7f);
					circle(e.x, e.y, e.fin() * 100 + 15);
					stroke(e.fout() * 2.5f);
					circle(e.x, e.y, e.fin() * 60 + 15);
				});
				smokeEffect = new Effect(30f, e -> {
					color(NHColor.darkEnrColor);
					Fill.circle(e.x, e.y, e.fout() * 32);
					color(NHColor.darkEnrColor, Color.black, 0.8f);
					Fill.circle(e.x, e.y, e.fout() * 20);
				});
			}

		};

	}
}














