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
	BulletType boltGene, airRaid,
			   curveBomb;

	@Override
	public void load() {
		airRaid = new FlakcBulletType(1.5f, 1250, "strike"){
			public float maxSpeedScl = 0.8f;
			public float maxSpeedCoeff = 16;
			
			@Override
			public float range(){
				return (speed + speed * maxSpeedScl * maxSpeedCoeff) / 2 * lifetime;
			}
    
			@Override
			public void draw(Bullet b){
				super.draw(b);
				Draw.color(lightColor);
				for(int i : Mathf.signs){
					Drawf.tri(b.x, b.y, 4.3f, 55 * b.fout(), b.rotation - 180 + 30 * i);
				}
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				
				Healthc frontEntity = Damage.linecast(b, b.x, b.y, b.rotation(), range());
				b.data = frontEntity;
				if(b.data == null)b.data = new Vec2().trns(b.rotation(), range()).add(b.x, b.y);
				if(b.data instanceof Position){
					Position target = (Position)b.data;
					Tmp.v1.trns(b.angleTo(target) - 180 + Mathf.range(60), 8 * tilesize).add(b.x, b.y);
					b.set(Tmp.v1);
					b.rotation(b.angleTo(target));
				}else{
					b.remove();
					return;
				}
				
				new Effect(28f, e -> {
					for(int i : Mathf.signs){
						Drawf.tri(e.x, e.y, 5.4f, 72 * e.fout(), e.rotation + 90 * i);
					}
				}).at(b.x, b.y, b.rot());
			}
			
			@Override
			public void update(Bullet b){
				if(!(b.data instanceof Position))b.remove();
				Position target = (Position)b.data;
				super.update(b);
				
				b.vel().setAngle(Angles.moveToward(b.vel().angle(), b.angleTo(target), 2f));
				b.vel().scl(Mathf.curve(b.finpow(), 0, maxSpeedScl) * maxSpeedCoeff + 1);
				
				if(b.time() > 6){
					new Effect(32f, e -> {
						color(lightColor, Pal.gray, e.fin());
						stroke(e.fout() * 5);
						lineAngleCenter(e.x, e.y, e.rotation - 180, e.fout() * 44 + 56); 
					}).at(b.x, b.y, b.rot());
				}
				
				if(b.timer.get(0,3)){
					new Effect(25f, e -> {
						color(lightColor, Pal.gray, e.fin() * 0.65);
						randLenVectors(e.id, 4, 60f * e.fin(), e.rotation - 180, 20, (x, y) -> {
							Fill.poly(e.x + x, e.y + y, 6, 5.5 * e.fslope() * e.fout());
						});
					}).at(b.x, b.y, b.rot());
				}
			}

			@Override
			public void hit(b, x, y){
				super.hit(b, x, y);
				NHLightningBolt.generateRange(Tmp.v1.set(b.x, b.y), b.team(), 80, 8, 2, lightColor, NHLightningBolt.WIDTH, target -> {
					Damage.damage(b.team(), target.getX(), target.getY(), 40f, damage * b.damageMultiplier());
					NHFx.lightningHit.at(target);
				});
				
			{
				shrinkX = shrinkY = 0;
				splashDamageRadius = 80f;
				splashDamage = damage * 0.7f;
				height = 60f;
				width = 18f;
				lifetime = 150;
				backColor = lightColor = NHColor.darkEnrColor;
				frontColor = Color.white;
				hitEffect = new Effect(25, e -> {
					color(lightColor);
					stroke(e.fout() * 3);
					circle(e.x, e.y, e.fin() * 60);
					stroke(e.fout() * 1.75f);
					circle(e.x, e.y, e.fin() * 45);
					
					randLenVectors(e.id + 1, 12, 1f + 60f * e.finpow(), (x, y) -> {
						lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 2f + e.fout() * 8f);
					});
			
					Fill.circle(e.x, e.y, e.fout() * 20);
					color(frontColor);
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

				color(NHColor.thurmixRed, NHColor.thurmixRedLight, b.fout());
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
				Effect.shake(10f, 8f, (Position)b);
				despawnEffect.at(b);
				Sounds.explosionbig.at(b, Mathf.random(0.9f, 1.1f));
				NHLightningBolt.generateRange(new Vec2(b.x, b.y), b.team(), 80, 5, 2, 120 * b.damageMultiplier(), NHColor.thurmixRed, true, NHLightningBolt.WIDTH);
				Damage.damage(b.team(), b.x, b.y, this.splashDamageRadius, this.splashDamage * b.damageMultiplier());
			}

			{
				drawSize = 400;

				shootEffect = new Effect(90f, 160f, e -> {
					color(NHColor.thurmixRed, NHColor.thurmixRedLight, e.fout());
					Drawf.tri(e.x, e.y, 5 * e.fout(), Mathf.curve(e.fout(), 0, 0.1f) * 80, e.rotation + 90);
					Drawf.tri(e.x, e.y, 5 * e.fout(), Mathf.curve(e.fout(), 0, 0.1f) * 80, e.rotation + 270);
				});

				despawnEffect = new Effect(32f, e -> {
					color(NHColor.thurmixRed, NHColor.thurmixRedLight, e.fout());
					stroke(e.fout() * 2);
					circle(e.x, e.y, e.fin() * 40);
					Fill.circle(e.x, e.y, e.fout() * e.fout() * 10);
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> {
						Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
					});
				});

				smokeEffect = new Effect(45f, e -> {
					color(NHColor.thurmixRed, NHColor.thurmixRedLight, e.fout());
					Drawf.tri(e.x, e.y, 4 * e.fout(), 28, e.rotation + 90);
					Drawf.tri(e.x, e.y, 4 * e.fout(), 28, e.rotation + 270);
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> {
						Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
					});
				});
			}

		};

		boltGene = new ArtilleryBulletType(2.75f, 1000) {
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
						splashDamage = 150f;
						backColor = lightningColor = NHColor.darkEnrColor;
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














