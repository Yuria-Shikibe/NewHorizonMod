package newhorizon.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.*;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Teamc;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import newhorizon.effects.EffectTrail;

import static mindustry.Vars.indexer;
import static mindustry.Vars.world;

public class NHTrailBulletType extends SpeedUpBulletType {
	public int trailLength = -1;
	public float trailWidth = -1f;
	
	public float trailWeaveScale = 0f;
	public float trailWeaveMag = -1f;
	public float trailWeaveScaleOffset = 0;
	public float trailOffset = 0f;
	public int trails = 1;
	public float sideOffset = 0f;
	public boolean flip;
	public boolean combine = false;
	public boolean flipWhileTwin = true;
	public boolean useTeamColor = false;
	public boolean homingHit = false;
	
	public float initRotation = 0, initRotationRand = 0;
	
	public Color trailToColor = Pal.gray;
	
	public NHTrailBulletType(float speed, float damage, String bulletSprite){
		super(speed, damage, bulletSprite);
		this.despawnEffect = Fx.none;
    }
	
	public NHTrailBulletType(float speed, float damage){
		this(speed, damage, "bullet");
	}
	
	public NHTrailBulletType(){
		this(1f, 1f, "bullet");
	}
    
    @Override
	public void init(){
		super.init();
	    if(trailLength < 0)trailLength = 12;
	    drawSize = Math.max(EffectTrail.DRAW_SIZE, Math.max(drawSize, 1.5f * trailLength * (speed + velocityEnd)));
	    if(trailWidth < 0)trailWidth = width / 6f;
    }
    
    public void despawnedEffect(Bullet b){
	    despawnEffect.at(b.x, b.y, b.rotation(), useTeamColor ? b.team.color : hitColor);
	    Effect.shake(despawnShake, despawnShake, b);
	    if (!(b.data instanceof EffectTrail[]))return;
	    EffectTrail[] t = (EffectTrail[])b.data();
	    for(EffectTrail trail : t){
		    if(combine)trail.update(b.x, b.y);
		    trail.disappear();
	    }
    }
    
    @Override
	public void despawned(Bullet b){
		despawnedEffect(b);
		hit(b);
	}
	
	@Override
	public void init(Bullet b) {
		super.init(b);
		EffectTrail[] data = new EffectTrail[trails];
		
		for(int i = 0; i < data.length; i++){
			data[i] = new EffectTrail(trailLength, trailWidth, useTeamColor ? b.team.color : trailColor, useTeamColor ? b.team.color : trailToColor);
		}
		
		b.data(data);
		
		b.vel.rotate(initRotation + Mathf.range(initRotationRand / 2f));
	}
	
	@Override
	public void hit(Bullet b, float x, float y){
		b.hit = true;
		hitEffect.at(x, y, b.rotation(), useTeamColor ? b.team.color : hitColor);
		hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
		
		Effect.shake(hitShake, hitShake, b);
		
		if(fragBullet != null){
			for(int i = 0; i < fragBullets; i++){
				float len = Mathf.random(1f, 7f);
				float a = b.rotation() + Mathf.range(fragCone/2) + fragAngle;
				fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
			}
		}
		
		if(puddleLiquid != null && puddles > 0){
			for(int i = 0; i < puddles; i++){
				Tile tile = world.tileWorld(x + Mathf.range(puddleRange), y + Mathf.range(puddleRange));
				Puddles.deposit(tile, puddleLiquid, puddleAmount);
			}
		}
		
		if(Mathf.chance(incendChance)){
			Damage.createIncend(x, y, incendSpread, incendAmount);
		}
		
		if(splashDamageRadius > 0 && !b.absorbed){
			Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
			
			if(status != StatusEffects.none){
				Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
			}
			
			if(healPercent > 0f){
				indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
					Fx.healBlockFull.at(other.x, other.y, other.block.size, Pal.heal);
					other.heal(healPercent / 100f * other.maxHealth());
				});
			}
			
			if(makeFire){
				indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> {
					Fires.create(other.tile);
				});
			}
		}
		
		for(int i = 0; i < lightning; i++){
			Lightning.create(b, useTeamColor ? b.team.color : lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
		}
	}

	@Override
	public void draw(Bullet b) {
		drawTrail(b);
		
		float height = this.height * ((1f - shrinkY) + shrinkY * b.fout());
		float width = this.width * ((1f - shrinkX) + shrinkX * b.fout());
		float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);
		
		Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());
		
		Draw.mixcol(mix, mix.a);
		
		Draw.color(useTeamColor ? b.team.color : backColor);
		Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
		Draw.color(useTeamColor ? Tmp.c1.set(b.team.color).lerp(Color.white, 0.45f) : frontColor);
		Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);
		
		Draw.reset();
	}
	
	public void drawTrail(Bullet b){
		if (!(b.data instanceof EffectTrail[]))return;
		EffectTrail[] t = (EffectTrail[])b.data();
		
		for(EffectTrail trail : t){
			trail.draw();
		}
	}

	public void homing(Bullet b){
		if(homingPower > 0.0001f && b.time >= homingDelay){
			Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> ((e.isGrounded() && collidesGround) || (e.isFlying() && collidesAir)) && (homingHit || !b.collided.contains(e.id)), t -> collidesGround);
			if(target != null){
				b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
			}
		}
	}
	
	@Override
	public void update(Bullet b) {
		if (!(b.data instanceof EffectTrail[]))return;
		EffectTrail[] trail = (EffectTrail[])b.data;
		if(!Vars.headless && b.timer(3, Mathf.clamp(1 / Time.delta, 0, 1))){
			updateTrail(b, trail);
		}
		
		superFunc: {
			b.vel.setLength(velocityBegin + accelCurve.get(b) * velocityEnd);
			
			homing(b);
			
			if(weaveMag > 0){
				b.vel.rotate(Mathf.sin(b.time + Mathf.PI * weaveScale / 2f, weaveScale, weaveMag * (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
			}
			
			if(trailChance > 0){
				if(Mathf.chanceDelta(trailChance)){
					trailEffect.at(b.x, b.y, trailParam, useTeamColor ? b.team.color : trailColor);
				}
			}
		}
	}
	
	public void updateTrail(Bullet b, EffectTrail[] t){
		for(int i = 0; i < t.length; i++){
			int offsetParma = (i - (flipWhileTwin ? i % 2 : 0));
			Tmp.v1.trns(b.rotation(), -b.vel.len() / 2 - trailOffset - sideOffset * offsetParma, (flip ? Mathf.sign(i % 2 == 0) : 1) * Mathf.absin(b.time, trailWeaveScale + offsetParma * trailWeaveScaleOffset, trailWeaveMag)).add(b);
			t[i].update(Tmp.v1.x, Tmp.v1.y);
		}
	}
}














