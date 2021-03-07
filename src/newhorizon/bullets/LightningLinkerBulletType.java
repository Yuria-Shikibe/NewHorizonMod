package newhorizon.bullets;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import newhorizon.content.NHFx;
import newhorizon.effects.EffectTrail;
import newhorizon.feature.PosLightning;

import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;

public class LightningLinkerBulletType extends NHTrailBulletType{
	public Color
			outColor = Color.white,
			innerColor = Color.white;
	
	public float generateDelay = 6f;
	public float size = 30f;
	public float linkRange = 240f;
	public float boltWidth = PosLightning.WIDTH;
	
	public float randomGenerateRange = -1f;
	public float randomGenerateChance = 0.03f;
	public float randomLightningChance = 0.1f;
	public int randomLightningNum = 4;
	public Sound randomGenerateSound = Sounds.plasmaboom;
	
	public float range = -1;
	
	public int maxHit = 20;
	public int boltNum = 2;
	
	public Effect slopeEffect = NHFx.boolSelector, liHitEffect = NHFx.boolSelector, spreadEffect = NHFx.boolSelector;
	
	public LightningLinkerBulletType(float speed, float damage) {
		super(speed, damage);
		collidesGround = collidesAir = true;
		collides = false;
		scaleVelocity = true;
		hitShake = 3.0F;
		hitSound = Sounds.explosion;
		shootEffect = Fx.shootBig;
		trailEffect = Fx.artilleryTrail;
		lightning = 4;
		trails = 0;
		lightningLength = 3;
		lightningLengthRand = 12;
		lightningCone = 360f;
	}
	
	public LightningLinkerBulletType(){
		this(1f, 1f);
	}
	
	@Override
	public float range(){
		return range < 0 ? super.range() : range;
	}
	
	@Override
	public void init(){
		super.init();
		if(this.slopeEffect == NHFx.boolSelector)this.slopeEffect = new Effect(25, e -> {
			if(!(e.data instanceof Integer))return;
			int i = e.data();
			Draw.color(outColor);
			Angles.randLenVectors(e.id, (int)(size / 8f), size / 4f + size * 2f * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 13f));
			Lines.stroke((i < 0 ? e.fin() : e.fout()) * 3f);
			Lines.circle(e.x, e.y, (i > 0 ? e.fin() : e.fout()) * size * 1.1f);
		});
		if(this.spreadEffect == NHFx.boolSelector)this.spreadEffect = new Effect(32f, e -> randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
			color(outColor);
			Fill.circle(e.x + x, e.y + y, e.fout() * size / 2f);
			color(innerColor);
			Fill.circle(e.x + x, e.y + y, e.fout() * (size / 3f - 1f));
		}));
		if(this.liHitEffect == NHFx.boolSelector)this.liHitEffect = NHFx.lightningHitSmall(outColor);
	}
	
	@Override
	public void update(Bullet b) {
		Effect.shake(hitShake, hitShake, b);
		if (b.timer(5, generateDelay)) {
			for(int i : Mathf.signs)slopeEffect.at(b.x + Mathf.range(size / 4f), b.y + Mathf.range(size / 4f), b.rotation(), i);
			spreadEffect.at(b);
			PosLightning.createRange(b, collidesAir, collidesGround, b, b.team, linkRange, maxHit, outColor, Mathf.chanceDelta(randomLightningChance), 0, 0, PosLightning.WIDTH, boltNum, p -> liHitEffect.at(p));
		}
		
		if(randomGenerateRange > 0f && Mathf.chance(Time.delta * randomGenerateChance))PosLightning.createRandomRange(b, b.team, b, randomGenerateRange, outColor, Mathf.chanceDelta(randomLightningChance), 0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
			randomGenerateSound.at(hitPos, Mathf.random(0.9f, 1.1f));
			Damage.damage(b.team, hitPos.getX(), hitPos.getY(), this.splashDamageRadius, this.splashDamage * b.damageMultiplier(), this.collidesAir, this.collidesGround);
			NHFx.lightningHitLarge(outColor).at(hitPos);
		});
		
		if(!(b.data instanceof EffectTrail[]))return;
		super.updateTrail(b, (EffectTrail[])b.data());
		//super.update(b);
	}
	
	@Override
	public void init(Bullet b) {
		super.init(b);
		
		b.vel.scl(1 + b.lifetime / lifetime);
	}
	
	@Override
	public void draw(Bullet b) {
		color(outColor);
		Fill.circle(b.x, b.y, size);
		color(innerColor);
		Fill.circle(b.x, b.y, size / 7f + size / 3 * Mathf.curve(b.fout(), 0.1f, 0.35f));
		drawTrail(b);
	}
	
	@Override
	public void despawned(Bullet b) {
		PosLightning.createRandomRange(b, b.team, b, randomGenerateRange, outColor, Mathf.chanceDelta(randomLightningChance), 0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
			Damage.damage(b.team, hitPos.getX(), hitPos.getY(), this.splashDamageRadius, this.splashDamage * b.damageMultiplier(), this.collidesAir, this.collidesGround);
			NHFx.lightningHitLarge(outColor).at(hitPos);
			liHitEffect.at(hitPos);
			for (int j = 0; j < lightning; j++) {
				Lightning.create(b, this.lightningColor, this.lightningDamage < 0.0F ? this.damage : this.lightningDamage, b.x, b.y, b.rotation() + Mathf.range(this.lightningCone / 2.0F) + this.lightningAngle, this.lightningLength + Mathf.random(this.lightningLengthRand));
			}
			hitSound.at(hitPos, Mathf.random(0.9f, 1.1f));
		});
		super.despawned(b);
	}
}
