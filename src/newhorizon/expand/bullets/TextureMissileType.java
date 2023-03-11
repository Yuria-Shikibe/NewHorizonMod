package newhorizon.expand.bullets;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHPixmap;

import static mindustry.Vars.headless;

public class TextureMissileType extends AccelBulletType{
	public TextureMissileType(float damage, String bulletSprite){
		super(damage, bulletSprite);
		
		absorbable = false;
	}
	
	@Override
	public void load(){
		backRegion = frontRegion = Core.atlas.find(sprite + NHPixmap.PCD_SUFFIX);
	}
	
	@Override
	public void drawTrail(Bullet b){
		if(trailLength > 0 && b.trail != null){
			b.trail.draw(trailColor, trailWidth);
			b.trail.drawCap(trailColor, trailWidth);
		}
	}
	
	@Override
	public void updateTrail(Bullet b){
		if(!headless && trailLength > 0 && b.time > 5f){
			if(b.trail == null){
				b.trail = new Trail(trailLength);
			}
			b.trail.length = trailLength;
			b.trail.update(b.x, b.y, trailInterp.apply(b.fin()));
		}
	}
	
	@Override
	public void draw(Bullet b){
		drawTrail(b);
		
		float z = Draw.z();
		Draw.z(Layer.flyingUnitLow - 0.2f);
		Tmp.v1.trns(b.rotation(), height / 1.75f).add(b);
		Drawf.shadow(Tmp.v1.x, Tmp.v1.y, height / 1.25f);
		Draw.rect(backRegion, Tmp.v1.x, Tmp.v1.y, b.rotation() - 90);
		Draw.z(z);
	}
	
	public void hit(Bullet b, float x, float y){
		hitEffect.at(x, y, b.rotation(), hitColor);
		hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
		
		Effect.shake(hitShake, hitShake, b);
		
		if(splashDamageRadius > 0 && !b.absorbed){
			Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
			
			if(status != StatusEffects.none){
				Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
			}
			
			if(makeFire){
				UltFire.createChance(x, y, splashDamageRadius, 0.35f, b.team);
			}
		}
		
		for(int i = 0; i < lightning; i++){
			Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
		}
	}
	
	public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
		UltFire.create(build.tile);
		
		if(build.team != b.team && direct){
			hit(b);
		}
	}
	
}
