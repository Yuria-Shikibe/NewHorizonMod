package newhorizon.content;

import arc.math.geom.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.StatusEffect;
import newhorizon.NewHorizon;
import newhorizon.bullets.DelayLaserType;
import newhorizon.bullets.NHTrailBulletType;
import newhorizon.bullets.ShieldBreaker;
import newhorizon.colors.*;
import newhorizon.func.PosLightning;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class NHBullets {
	public static final
	BulletType
		strikeLaser = new DelayLaserType(660f, 60f){
			@Override
			public void effectDraw(Bullet b){
				Draw.color(Pal.accent);
				float fin = Mathf.clamp(b.time / 60f);
				float fout = Mathf.clamp(1f - fin);
				float fslope = (0.5F - Math.abs(fin - 0.5F)) * 2.0F;
				if(b.time < 60f){
					randLenVectors(b.id, 6, 3 + 50 * fout, (x, y) -> Fill.circle(b.x + x, b.y + y, fin * fin * 5f));
					Lines.stroke(fslope * 2.0F);
					Lines.circle(b.x, b.y, fout * 40f);
					randLenVectors(b.id + 1, 16, 3 + 70 * fout, (x, y) -> lineAngle(b.x + x, b.y + y, Mathf.angle(x, y), fslope * 18 + 5));
				}
			}

			{
				colors = new Color[]{Pal.accent.cpy().mul(1f, 1f, 1f, 0.3f), Pal.accent, Color.white};
				length = 320f;
				width = 25f;
				lengthFalloff = 0.6f;
				sideLength = 90f;
				sideWidth = 1.35f;
				sideAngle = 40f;
				this.lightningSpacing = 40.0F;
				this.lightningLength = 2;
				this.lightningDelay = 1.1F;
				this.lightningLengthRand = 10;
				this.lightningDamage = 20.0F;
				this.lightningAngleRand = 40.0F;
				this.lightningColor = Pal.accent;
				smokeEffect = shootEffect = Fx.none;
				splashDamage = 42.0F;
				splashDamageRadius = 20.0F;
				collidesGround = true;
				lifetime = 38.0F;
				status = StatusEffects.blasted;
				statusDuration = 60.0F;
			}
		},

		tear = new ShieldBreaker(3.4f, 60f, 1500f){{
			pierceCap = 8;
			rotateSpeed = 2.75f;
			width = 16f;
			height = 37f;
			shrinkX = shrinkY = 0.001f;
			splashDamage = lightningDamage = damage * 0.6f;
			backColor = lightColor = lightningColor = trailColor = Items.plastanium.color;
			frontColor = Color.white;
			lightning = 2;
			lightningLengthRand = 8;
			lightningLength = 3;
			lifetime = 90f;
			hitSound = Sounds.plasmaboom;
			hitShake = 4f;
			splashDamageRadius = 20f;
			despawnEffect = hitEffect = Fx.plasticExplosionFlak;
			smokeEffect = Fx.shootBigSmoke2;
			shootEffect = Fx.plasticExplosion;
		}},

		skyFrag = new BasicBulletType(3.3f, 80) {
		@Override public float range(){return 320f;}
			{
				lifetime = 170f;
				despawnEffect = hitEffect = NHFx.lightSkyCircleSplash;
				knockback = 12f;
				width = 15f;
				height = 37f;
				splashDamageRadius = 40f;
				splashDamage = lightningDamage = damage * 0.6f;
				backColor = lightColor = lightningColor = trailColor = NHColor.lightSky;
				frontColor = Color.white;
				lightning = 3;
				lightningLength = 8;
				smokeEffect = Fx.shootBigSmoke2;
				trailChance = 0.6f;
				trailEffect = NHFx.skyTrail;
				hitShake = 2f;
				hitSound = Sounds.spark;
			}
		},

		hurricaneLaser = new ContinuousLaserBulletType(680){
			{
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{NHColor.lightSky.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.lightSky.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.lightSky, Color.white};
				width = 7f;
				length = 500f;
				oscScl = 0.4f;
				oscMag = 1.5f;
				lifetime = 160f;
				lightColor = NHColor.lightSky;
				hitEffect = NHFx.lightSkyCircleSplash;
				shootEffect = NHFx.chargeEffectSmall(NHColor.lightSky);
				smokeEffect = NHFx.lightSkyCircleSplash;
			}

			@Override
			public void init(Bullet b) {
				super.init(b);
				Sounds.laserblast.at(b);
			}

			@Override
			public void update(Bullet b) {
				super.update(b);
				if (b.timer(0, 8)) {
					NHFx.lightSkyCircleSplash.at(b);
				}
			}

			@Override
			public void draw(Bullet b) {
				super.draw(b);
				float f = Mathf.clamp(b.time > b.lifetime - this.fadeTime ? 1.0F - (b.time - (this.lifetime - this.fadeTime)) / this.fadeTime : 1.0F);
				color(NHColor.lightSky);
				Fill.circle(b.x, b.y, 24f * f);
				for (int i : Mathf.signs){
					for (int j : Mathf.signs){
						color(NHColor.lightSky);
						Drawf.tri(b.x, b.y, 16f * f, 86f + Mathf.absin(Time.time * j, 6f, 20f) * f, 90 + 90 * i + Time.time * j);
				}}

				for (int i : Mathf.signs){
					for (int j : Mathf.signs) {
						color(Color.white);
						Drawf.tri(b.x, b.y, 7f * f, 63f + Mathf.absin(Time.time * j, 6f, 12f) * f, 90 + 90 * i + Time.time * j);
				}}

				color(Color.white);
				Fill.circle(b.x, b.y, 17f * f);
				Draw.reset();
			}
		},

		huriEnergyCloud = new NHTrailBulletType(6, 60){
			@Override public float range(){return 400f;}
			@Override
			public void update(Bullet b){
				b.vel().scl(Mathf.curve(b.finpow(), 0.12f, 0.85f));
				new Effect(40,e -> {
					Draw.color(e.color, Pal.gray, e.fin());
					Vec2 trnsB = new Vec2();
					trnsB.trns(e.rotation, e.fin() * (22));
					Fill.poly(e.x + trnsB.x, e.y + trnsB.y, 6, e.fout() * 6, e.rotation);
				}).at(b.x, b.y, b.rotation(), NHColor.lightSky);
				super.update(b);
			}

		{
			width = height = 0f;
			splashDamage = 25;
			splashDamageRadius = 20;
			homingDelay = 60f;
			homingPower = 0.15f;
			homingRange = 200f;
			lifetime = 210;
			pierceBuilding = pierce = true;

			hitEffect = new Effect(40, e -> {
				Draw.color(e.color);
				Angles.randLenVectors(e.id, 2, 60 * e.fin(), 0, 360, (x, y) ->
					Fill.poly(e.x + x, e.y + y, 6, 4 * e.fout())
				);
			});
			shootEffect = new Effect(25f, e -> {
				Draw.color(e.color, Color.white, e.fin() * 0.5f);
				Drawf.tri(e.x, e.y, 3 * e.fout(), 40 * e.fout(), e.rotation + 90);
				Drawf.tri(e.x, e.y, 3 * e.fout(), 40 * e.fout(), e.rotation + 270);
			});
			smokeEffect = new Effect(25f, e -> {
				Draw.color(e.color, Color.white, e.fin() * 0.5f);
				Angles.randLenVectors(e.id, 3, 40 * e.fin(), e.rotation, 55, (x, y) ->
						Fill.circle(e.x + x, e.y + y, e.fout() * 3)
				);
			});
		}
	},

		none = new BasicBulletType(0, 1, "none") {{
			instantDisappear = true;
			trailEffect = smokeEffect = shootEffect = hitEffect = despawnEffect = Fx.none;
		}},

		supSky = new SapBulletType(){{
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
		}},

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
		},

		decayLaser = new LaserBulletType(2000){{
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
		}},

		longLaser = new LaserBulletType(500){{
			colors = new Color[]{NHColor.lightSky.cpy().mul(1f, 1f, 1f, 0.3f), NHColor.lightSky, Color.white};
			length = 360f;
			width = 12.7f;
			lengthFalloff = 0.6f;
			sideLength = 68f;
			sideWidth = 0.9f;
			sideAngle = 90f;
			largeHit = false;
			shootEffect = smokeEffect = Fx.none;
		}},

		rapidBomb = new NHTrailBulletType(9f, 200, NewHorizon.NHNAME + "strike"){{
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
		}},

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
				
		},

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
				randLenVectors(b.id, 2, len, (x, y) -> randLenVectors(b.id / 2 + 12, 1, len, (x2, y2) -> curve(
					from.x,  		 	from.y,
					from.x + vec1.x + x,  from.y + vec1.y + y,
					from.x + vec2.x + x2, from.y + vec2.y + y2,
					b.x, b.y,
					16
				)));
				Fill.circle(from.x, from.y, 3.5f * b.fout() * getStroke() / 2f);
				Fill.circle(b.x, b.y, 2 * b.finpow() + 4 * b.fslope());
				reset();
			}

			@Override
			public void despawned(Bullet b) {
				super.despawned(b);
				PosLightning.createRange(new Vec2(b.x, b.y), b.team(), 80, 5, 2, 120 * b.damageMultiplier(), lightColor, true, PosLightning.WIDTH);
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
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));
				});

				smokeEffect = new Effect(45f, e -> {
					color(lightColor, frontColor, e.fout());
					Drawf.tri(e.x, e.y, 4 * e.fout(), 28, e.rotation + 90);
					Drawf.tri(e.x, e.y, 4 * e.fout(), 28, e.rotation + 270);
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
				});
			}

		},

		boltGene = new ArtilleryBulletType(2.75f, 100) {
			@Override
			public void update(Bullet b) {
				Effect.shake(2, 2, b);
				if (b.timer(0, 8)) {
					for(int i : Mathf.signs){
						new Effect(25, e -> {
							Draw.color(NHColor.darkEnrColor);
							Angles.randLenVectors(e.id, 4, 3 + 60 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 13f));
							Lines.stroke((i < 0 ? e.fin() : e.fout()) * 3f);
							Lines.circle(e.x, e.y, (i > 0 ? e.fin() : e.fout()) * 33f);
						}).at(b.x + Mathf.range(8f), b.y + Mathf.range(8f), b.rotation());
					}
					NHFx.darkEnergySpread.at(b);
				}

				if (b.timer(2, 8) && (b.lifetime - b.time) > PosLightning.lifetime) {
					PosLightning.createRange(b, 240, 15, 1, splashDamage * b.damageMultiplier(), NHColor.darkEnrColor, Mathf.chance(Time.delta * 0.13), 1.33f * PosLightning.WIDTH);
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
					PosLightning.create(new Vec2(b.x, b.y), randomPos, b.team(), NHColor.darkEnrColor, 1.7f * PosLightning.WIDTH, 2, hitPos -> {
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
			}};

}














