package newhorizon.bullets;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import newhorizon.content.NHFx;
import newhorizon.feature.PosLightning;

import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;

public class LightningLinkerBulletType extends SpeedUpBulletType{
	public Color
			outColor = Color.white,
			innerColor = Color.white;
	
	public float generateDelay = 10f;
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
	public int boltNum = 1;
	
	public int   effectLingtning = 2;
	public float effectLightningChance = 0.35f;
	public float effectLightningLength = -1;
	public float effectLightningLengthRand = -1;
	
	public Effect slopeEffect = NHFx.boolSelector, liHitEffect = NHFx.boolSelector, spreadEffect = NHFx.boolSelector;
	
	public static final Vec2 randVec = new Vec2();
	
	public LightningLinkerBulletType(float speed, float damage) {
		super(speed, damage);
		collidesGround = collidesAir = true;
		collides = false;
		scaleVelocity = true;
		hitShake = 3.0F;
		hitSound = Sounds.explosion;
		shootEffect = Fx.shootBig;
		lightning = 4;
		lightningLength = 3;
		lightningLengthRand = 12;
		lightningCone = 360f;
		
		trailWidth = -1;
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
			Angles.randLenVectors(e.id, (int)(size / 8f), size / 4f + size * 2f * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * size / 1.65f));
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
		
		if(trailWidth < 0)trailWidth = size * 0.75f;
		if(trailLength < 0)trailLength = 12;
		
		drawSize = Math.max(drawSize, size * 2f);
		
		if(effectLightningLength < 0)effectLightningLength = size * 1.5f;
		if(effectLightningLengthRand < 0)effectLightningLengthRand = size * 2f;
	}
	
	@Override
	public void update(Bullet b) {
		super.update(b);
		
		Effect.shake(hitShake, hitShake, b);
		if (b.timer(5, generateDelay)) {
			for(int i : Mathf.signs)slopeEffect.at(b.x + Mathf.range(size / 4f), b.y + Mathf.range(size / 4f), b.rotation(), i);
			spreadEffect.at(b);
			PosLightning.createRange(b, collidesAir, collidesGround, b, b.team, linkRange, maxHit, outColor, Mathf.chanceDelta(randomLightningChance), 0, 0, PosLightning.WIDTH, boltNum, p -> liHitEffect.at(p));
		}
		
		if(randomGenerateRange > 0f && Mathf.chance(Time.delta * randomGenerateChance))PosLightning.createRandomRange(b, b.team, b, randomGenerateRange, outColor, Mathf.chanceDelta(randomLightningChance), 0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
			randomGenerateSound.at(hitPos, Mathf.random(0.9f, 1.1f));
			Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius / 8, splashDamage * b.damageMultiplier() / 8, collidesAir, collidesGround);
			NHFx.lightningHitLarge(outColor).at(hitPos);
		});
		
		if(Mathf.chanceDelta(effectLightningChance) && b.lifetime - b.time > Fx.chainLightning.lifetime && Core.settings.getBool("enableeffectdetails")){
			for(int i = 0; i < effectLingtning; i++){
				Vec2 v = randVec.rnd(effectLightningLength + Mathf.random(effectLightningLengthRand)).add(b).add(Tmp.v1.set(b.vel).scl(Fx.chainLightning.lifetime / 2)).cpy();
				Fx.chainLightning.at(v.x, v.y, 12f, outColor, b);
				NHFx.lightningHitSmall.at(v.x, v.y, 20f, outColor);
			}
		}
	}
	
	@Override
	public void init(Bullet b) {
		super.init(b);
		
		b.vel.scl(1 + b.lifetime / lifetime);
	}
	
	@Override
	public void draw(Bullet b) {
		drawTrail(b);
		
		color(outColor);
		Fill.circle(b.x, b.y, size);
		color(innerColor);
		Fill.circle(b.x, b.y, size / 7f + size / 3 * Mathf.curve(b.fout(), 0.1f, 0.35f));
		
		Drawf.light(b.x, b.y, size * 1.85f, outColor, 0.7f);
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