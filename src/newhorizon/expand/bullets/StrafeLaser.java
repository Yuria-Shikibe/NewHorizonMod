package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import newhorizon.content.NHFx;
import newhorizon.util.func.NHFunc;

public class StrafeLaser extends BulletType{
	public float strafeAngle = 75;
	public float width = 18f;
	
	public StrafeLaser(float damage){
		this.damage = damage;
		
		lifetime = 150f;
		
		speed = 0;
		hitEffect = NHFx.square45_4_45;
		shootEffect = Fx.none;
		smokeEffect = Fx.none;
		maxRange = 600f;
		
		reflectable = false;
		despawnEffect = Fx.none;
		impact = true;
		keepVelocity = false;
		collides = false;
		pierce = true;
		hittable = false;
		absorbable = false;
	}
	
	@Override
	public void init(){
		super.init();
		
		drawSize = Math.max(drawSize, maxRange * 2);
	}
	
	public float getRotation(Bullet b){
		return -strafeAngle / 2 + strafeAngle * b.fin(Interp.pow3);
	}
	
	@Override
	public float estimateDPS(){
		return continuousDamage();
	}
	
	@Override
	public void draw(Bullet b){
		float rotation = b.rotation() + getRotation(b);
		
		Color[] colors = {b.team.color.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), b.team.color.cpy().mul(1f, 1f, 1f, 0.5f), b.team.color, Color.white};
		
		float fout = b.fout(0.25f) * Mathf.curve(b.fin(), 0, 0.175f);
		float maxRange = (b.fdata <= 0 ? this.maxRange : b.fdata) * fout;
		float realLength = NHFunc.findLaserLength(b, rotation, maxRange);
		float baseLen = realLength * fout;
		
		Tmp.v1.trns(rotation, baseLen);
		
		Tmp.v2.trns(rotation, 0, width / 2 * fout);
		
		Tmp.v3.setZero();
		if(realLength < maxRange){
			Tmp.v3.set(Tmp.v2).scl((maxRange - realLength) / maxRange);
		}
		
		Draw.color(b.team.color);
		Tmp.v2.scl(0.9f);
		Tmp.v3.scl(0.9f);
		Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
		if(realLength < maxRange)Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
		
		Tmp.v2.scl(1.15f);
		Tmp.v3.scl(1.15f);
		Draw.alpha(0.5f);
		Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
		if(realLength < maxRange)Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
		
		Draw.alpha(1f);
		Draw.color(Tmp.c1.set(b.team.color).lerp(Color.white, 0.7f));
		Tmp.v2.scl(0.5f);
		Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + (Tmp.v1.x + Tmp.v3.x) / 3, b.y + (Tmp.v1.y + Tmp.v3.y) / 3, b.x + (Tmp.v1.x - Tmp.v3.x) / 3, b.y + (Tmp.v1.y - Tmp.v3.y) / 3);
		
		Drawf.light(b.team, b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.5f, b.team.color, 0.7f);
		Draw.reset();
	}
	
	@Override
	public float continuousDamage(){
		return damage / 5f * 60f;
	}
	
	@Override
	public void update(Bullet b){
		if(b.timer.get(1, 5)){
			float maxRange = (b.fdata <= 0 ? this.maxRange : b.fdata) * b.fout(0.25f) * Mathf.curve(b.fin(), 0, 0.175f);
			NHFunc.collideLine(b, b.team, Fx.none, b.x, b.y, b.rotation() + getRotation(b), maxRange, true, true);
		}
		
		if(hitShake > 0){
			Effect.shake(hitShake, hitShake, b);
		}
	}
	
	@Override
	public void hit(Bullet b, float x, float y){
		hitEffect.at(x, y, b.rotation() + getRotation(b), b.team.color);
	}
}
